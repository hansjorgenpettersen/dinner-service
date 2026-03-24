import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getRecipe, addIngredient, deleteIngredient, addIngredientToList,
         removeIngredientFromList, uploadImages, deleteImage, deleteRecipe, updateRecipe } from '../api/recipes'
import { searchProducts } from '../api/products'

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
    onSuccess: invalidate })

  const removeIng = useMutation({ mutationFn: (ingId: number) => deleteIngredient(recipeId, ingId), onSuccess: invalidate })

  const addToList = useMutation({ mutationFn: (ingId: number) =>
    addIngredientToList(recipeId, ingId, selectedListId!), onSuccess: invalidate })

  const removeFromList = useMutation({ mutationFn: (ingId: number) =>
    removeIngredientFromList(recipeId, ingId, selectedListId!), onSuccess: invalidate })

  const delRecipe = useMutation({ mutationFn: () => deleteRecipe(recipeId),
    onSuccess: () => navigate('/recipes') })

  const editRecipe = useMutation({ mutationFn: () =>
    updateRecipe(recipeId, { name: editName, description: editDesc }),
    onSuccess: () => { setEditing(false); invalidate() } })

  const uploadImg = useMutation({ mutationFn: (files: FileList) => uploadImages(recipeId, files), onSuccess: invalidate })
  const delImg = useMutation({ mutationFn: (imgId: number) => deleteImage(recipeId, imgId), onSuccess: invalidate })

  if (isLoading || !recipe) return <p>Loading...</p>

  return (
    <div>
      {editing ? (
        <form onSubmit={e => { e.preventDefault(); editRecipe.mutate() }}>
          <input value={editName} onChange={e => setEditName(e.target.value)} required />
          <textarea value={editDesc} onChange={e => setEditDesc(e.target.value)} />
          <button type="submit">Save</button>
          <button type="button" onClick={() => setEditing(false)}>Cancel</button>
        </form>
      ) : (
        <>
          <h1>{recipe.name}</h1>
          <p>{recipe.description}</p>
          <button onClick={() => { setEditing(true); setEditName(recipe.name); setEditDesc(recipe.description) }}>Edit</button>
          <button onClick={() => { if (confirm('Delete this recipe?')) delRecipe.mutate() }}>Delete</button>
        </>
      )}

      {/* Shopping list selector */}
      <label htmlFor="list-select">Shopping list:</label>
      <select id="list-select" value={selectedListId ?? ''} onChange={e => setSelectedListId(e.target.value ? Number(e.target.value) : undefined)}>
        <option value="">-- none --</option>
        {recipe.shoppingLists.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
      </select>

      {/* Ingredients */}
      <h2>Ingredients</h2>
      <ul>
        {recipe.ingredients.map(ing => (
          <li key={ing.id}>
            {ing.quantity} {ing.unit} {ing.productName}
            {selectedListId && (
              <>
                <button onClick={() => addToList.mutate(ing.id)}>+</button>
                <span>{recipe.ingredientCounts[ing.id] ?? 0}</span>
                <button onClick={() => removeFromList.mutate(ing.id)}>-</button>
              </>
            )}
            <button onClick={() => removeIng.mutate(ing.id)}>✕</button>
          </li>
        ))}
      </ul>

      {/* Add ingredient form */}
      <form onSubmit={e => { e.preventDefault(); addIng.mutate() }}>
        <input
          placeholder="Search product..."
          value={productSearch}
          onChange={e => { setProductSearch(e.target.value); setSelectedProductId(null) }}
        />
        {searchResults.length > 0 && !selectedProductId && (
          <ul>
            {searchResults.map(p => (
              <li key={p.id} onClick={() => { setSelectedProductId(p.id); setProductSearch(p.name) }} style={{ cursor: 'pointer' }}>
                {p.name}
              </li>
            ))}
          </ul>
        )}
        <input type="number" placeholder="Qty" value={quantity} onChange={e => setQuantity(e.target.value)} />
        <select value={unit} onChange={e => setUnit(e.target.value)}>
          {UNITS.map(u => <option key={u} value={u}>{u}</option>)}
        </select>
        <button type="submit" disabled={!selectedProductId}>Add ingredient</button>
      </form>

      {/* Images */}
      <h2>Photos</h2>
      <div>
        {recipe.images.map(img => (
          <span key={img.id}>
            <img src={`/recipe-images/${img.filename}`} alt={img.originalName} width={120} />
            <button onClick={() => delImg.mutate(img.id)}>Delete</button>
          </span>
        ))}
      </div>
      <input type="file" multiple accept="image/*"
        onChange={e => { if (e.target.files?.length) uploadImg.mutate(e.target.files) }} />
    </div>
  )
}
