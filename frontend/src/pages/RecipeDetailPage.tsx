import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getRecipe, addIngredient, deleteIngredient, addIngredientToList,
         removeIngredientFromList, uploadImages, deleteImage, deleteRecipe, updateRecipe } from '../api/recipes'
import { searchProducts } from '../api/products'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { ChevronLeft, Trash2, Pencil, Plus, Minus, X } from 'lucide-react'

const UNITS = ['pcs', 'g', 'kg', 'ml', 'dl', 'L', 'tsp', 'tbsp', 'cup']

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const recipeId = Number(id)
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [selectedListId, setSelectedListId] = useState<number | undefined>()
  const [productSearch, setProductSearch] = useState('')
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null)
  const [quantity, setQuantity] = useState('')
  const [unit, setUnit] = useState('pcs')
  const [editing, setEditing] = useState(false)
  const [editName, setEditName] = useState('')
  const [editDesc, setEditDesc] = useState('')
  const [lightbox, setLightbox] = useState<string | null>(null)

  const { data: recipe, isLoading } = useQuery({
    queryKey: ['recipe', recipeId, selectedListId],
    queryFn: () => getRecipe(recipeId, selectedListId)
  })

  const { data: searchResults = [] } = useQuery({
    queryKey: ['product-search', productSearch],
    queryFn: () => searchProducts(productSearch),
    enabled: productSearch.length > 1
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['recipe', recipeId] })

  const addIng = useMutation({ mutationFn: () =>
    addIngredient(recipeId, { productId: selectedProductId!, quantity: quantity ? Number(quantity) : undefined, unit }),
    onSuccess: () => { invalidate(); setProductSearch(''); setSelectedProductId(null); setQuantity('') }
  })

  const removeIng = useMutation({ mutationFn: (ingId: number) => deleteIngredient(recipeId, ingId), onSuccess: invalidate })
  const addToList = useMutation({ mutationFn: (ingId: number) => addIngredientToList(recipeId, ingId, selectedListId!), onSuccess: invalidate })
  const removeFromList = useMutation({ mutationFn: (ingId: number) => removeIngredientFromList(recipeId, ingId, selectedListId!), onSuccess: invalidate })
  const delRecipe = useMutation({ mutationFn: () => deleteRecipe(recipeId), onSuccess: () => navigate('/recipes') })
  const editRecipe = useMutation({ mutationFn: () => updateRecipe(recipeId, { name: editName, description: editDesc }),
    onSuccess: () => { setEditing(false); invalidate() }
  })
  const uploadImg = useMutation({ mutationFn: (files: FileList) => uploadImages(recipeId, files), onSuccess: invalidate })
  const delImg = useMutation({ mutationFn: (imgId: number) => deleteImage(recipeId, imgId), onSuccess: invalidate })

  if (isLoading || !recipe) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <Link to="/recipes" className="inline-flex items-center gap-1 text-[#7a5c3a] hover:text-[#c96a2b] text-sm mb-6">
        <ChevronLeft className="w-4 h-4" /> Back to recipes
      </Link>

      {/* Title / edit section */}
      <div className="bg-white border border-[#e8c9a0] rounded-lg p-6 mb-6">
        {editing ? (
          <form onSubmit={e => { e.preventDefault(); editRecipe.mutate() }} className="flex flex-col gap-3">
            <Input value={editName} onChange={e => setEditName(e.target.value)} required className="border-[#e8c9a0] text-lg font-bold" />
            <textarea
              value={editDesc}
              onChange={e => setEditDesc(e.target.value)}
              rows={3}
              className="w-full border border-[#e8c9a0] rounded-md px-3 py-2 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-[#c96a2b]"
            />
            <div className="flex gap-2">
              <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white">Save</Button>
              <Button type="button" variant="outline" onClick={() => setEditing(false)} className="border-[#e8c9a0] text-[#7a5c3a]">Cancel</Button>
            </div>
          </form>
        ) : (
          <div>
            <div className="flex items-start justify-between gap-4 mb-2">
              <h1 className="text-2xl font-bold text-[#3d1f08]">{recipe.name}</h1>
              <div className="flex gap-2 flex-shrink-0">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => { setEditing(true); setEditName(recipe.name); setEditDesc(recipe.description) }}
                  className="border-[#e8c9a0] text-[#7a5c3a] hover:text-[#3d1f08]"
                >
                  <Pencil className="w-3.5 h-3.5 mr-1" /> Edit
                </Button>
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => { if (confirm('Delete this recipe?')) delRecipe.mutate() }}
                >
                  <Trash2 className="w-3.5 h-3.5 mr-1" /> Delete
                </Button>
              </div>
            </div>
            {recipe.description && <p className="text-[#7a5c3a] text-sm">{recipe.description}</p>}
          </div>
        )}
      </div>

      {/* Shopping list selector */}
      <div className="bg-white border border-[#e8c9a0] rounded-lg p-4 mb-6 flex items-center gap-3">
        <Label htmlFor="list-select" className="text-[#3d1f08] whitespace-nowrap">Shopping list:</Label>
        <select
          id="list-select"
          value={selectedListId ?? ''}
          onChange={e => setSelectedListId(e.target.value ? Number(e.target.value) : undefined)}
          className="border border-[#e8c9a0] rounded-md px-2 py-1.5 text-sm text-[#3d1f08] bg-white flex-1"
        >
          <option value="">— none —</option>
          {recipe.shoppingLists.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
        </select>
      </div>

      {/* Ingredients */}
      <div className="bg-white border border-[#e8c9a0] rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-[#3d1f08] mb-4">Ingredients</h2>
        {recipe.ingredients.length === 0 ? (
          <p className="text-[#7a5c3a] text-sm">No ingredients yet.</p>
        ) : (
          <div className="flex flex-col gap-2 mb-4">
            {recipe.ingredients.map(ing => (
              <div key={ing.id} className="flex items-center gap-2 py-2 border-b border-[#f0e0cc] last:border-0">
                <span className="text-[#7a5c3a] text-sm flex-1">
                  {ing.quantity && `${ing.quantity} ${ing.unit} `}{ing.productName}
                </span>
                {selectedListId && (
                  <div className="flex items-center gap-1">
                    <button onClick={() => removeFromList.mutate(ing.id)} className="text-[#7a5c3a] hover:text-[#c96a2b] p-1">
                      <Minus className="w-3 h-3" />
                    </button>
                    <span className="text-sm text-[#3d1f08] w-5 text-center">{recipe.ingredientCounts[ing.id] ?? 0}</span>
                    <button onClick={() => addToList.mutate(ing.id)} className="text-[#7a5c3a] hover:text-[#c96a2b] p-1">
                      <Plus className="w-3 h-3" />
                    </button>
                  </div>
                )}
                <button onClick={() => removeIng.mutate(ing.id)} className="text-[#7a5c3a] hover:text-red-500 p-1">
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Add ingredient form */}
        <form onSubmit={e => { e.preventDefault(); addIng.mutate() }} className="flex flex-wrap gap-2 items-end border-t border-[#f0e0cc] pt-4">
          <div className="flex flex-col gap-1 flex-1 min-w-[140px] relative">
            <Label className="text-[#3d1f08] text-xs">Product</Label>
            <Input
              placeholder="Search product…"
              value={productSearch}
              onChange={e => { setProductSearch(e.target.value); setSelectedProductId(null) }}
              className="border-[#e8c9a0]"
            />
            {searchResults.length > 0 && !selectedProductId && (
              <div className="absolute top-full left-0 right-0 bg-white border border-[#e8c9a0] rounded-md shadow-md z-10 mt-1">
                {searchResults.map(p => (
                  <button
                    key={p.id}
                    type="button"
                    onClick={() => { setSelectedProductId(p.id); setProductSearch(p.name) }}
                    className="w-full text-left px-3 py-2 text-sm text-[#3d1f08] hover:bg-[#fdf0e0]"
                  >
                    {p.name}
                  </button>
                ))}
              </div>
            )}
          </div>
          <div className="flex flex-col gap-1 w-20">
            <Label className="text-[#3d1f08] text-xs">Qty</Label>
            <Input type="number" placeholder="0" value={quantity} onChange={e => setQuantity(e.target.value)} className="border-[#e8c9a0]" />
          </div>
          <div className="flex flex-col gap-1 w-24">
            <Label className="text-[#3d1f08] text-xs">Unit</Label>
            <select value={unit} onChange={e => setUnit(e.target.value)} className="border border-[#e8c9a0] rounded-md px-2 py-2 text-sm bg-white">
              {UNITS.map(u => <option key={u} value={u}>{u}</option>)}
            </select>
          </div>
          <Button type="submit" disabled={!selectedProductId} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white self-end">
            Add
          </Button>
        </form>
      </div>

      {/* Photos */}
      <div className="bg-white border border-[#e8c9a0] rounded-lg p-6">
        <h2 className="text-lg font-semibold text-[#3d1f08] mb-4">Photos</h2>
        {recipe.images.length > 0 && (
          <div className="flex flex-wrap gap-3 mb-4">
            {recipe.images.map(img => (
              <div key={img.id} className="relative group">
                <img
                  src={`/api/recipe-images/${img.filename}`}
                  alt={img.originalName}
                  className="w-28 h-28 object-cover rounded-lg border border-[#e8c9a0] cursor-pointer"
                  onClick={() => setLightbox(`/api/recipe-images/${img.filename}`)}
                />
                <button
                  onClick={() => delImg.mutate(img.id)}
                  className="absolute top-1 right-1 bg-red-600 text-white rounded-full w-5 h-5 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                >
                  <X className="w-3 h-3" />
                </button>
              </div>
            ))}
          </div>
        )}
        <label className="inline-flex items-center gap-2 cursor-pointer text-sm text-[#c96a2b] hover:text-[#a8571f]">
          <span>+ Add photos</span>
          <input type="file" multiple accept="image/*" className="hidden"
            onChange={e => { if (e.target.files?.length) uploadImg.mutate(e.target.files) }} />
        </label>
      </div>
      {lightbox && (
        <div
          className="fixed inset-0 bg-black/80 z-50 flex items-center justify-center p-4"
          onClick={() => setLightbox(null)}
          onKeyDown={e => e.key === 'Escape' && setLightbox(null)}
          tabIndex={-1}
        >
          <img src={lightbox} alt="" className="max-w-full max-h-full rounded-lg object-contain" onClick={e => e.stopPropagation()} />
          <button onClick={() => setLightbox(null)} className="absolute top-4 right-4 text-white hover:text-gray-300">
            <X className="w-6 h-6" />
          </button>
        </div>
      )}
    </div>
  )
}
