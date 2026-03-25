import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProducts, getCategories, createProduct, updateProduct, deleteProduct,
         createCategory, deleteCategory } from '../api/products'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Trash2, Pencil, Check, X } from 'lucide-react'

export default function ProductsPage() {
  const qc = useQueryClient()
  const { data: products = [] } = useQuery({ queryKey: ['products'], queryFn: getProducts })
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })

  const [tab, setTab] = useState<'products' | 'categories'>('products')
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null)
  const [newName, setNewName] = useState('')
  const [newPrice, setNewPrice] = useState('')
  const [newCatId, setNewCatId] = useState('')
  const [catName, setCatName] = useState('')
  const [catColor, setCatColor] = useState('#c96a2b')
  const [editId, setEditId] = useState<number | null>(null)
  const [editName, setEditName] = useState('')
  const [editPrice, setEditPrice] = useState('')
  const [editCatId, setEditCatId] = useState('')

  const inv = (keys: string[]) => keys.forEach(k => qc.invalidateQueries({ queryKey: [k] }))

  const create = useMutation({ mutationFn: () =>
    createProduct({ name: newName, price: newPrice ? Number(newPrice) : undefined, categoryId: newCatId ? Number(newCatId) : undefined }),
    onSuccess: () => { inv(['products']); setNewName(''); setNewPrice(''); setNewCatId('') }
  })

  const edit = useMutation({ mutationFn: () =>
    updateProduct(editId!, { name: editName, price: editPrice ? Number(editPrice) : undefined, categoryId: editCatId ? Number(editCatId) : undefined }),
    onSuccess: () => { inv(['products']); setEditId(null) }
  })

  const del = useMutation({ mutationFn: (id: number) => deleteProduct(id), onSuccess: () => inv(['products']) })

  const createCat = useMutation({ mutationFn: () => createCategory({ name: catName, color: catColor }),
    onSuccess: () => { inv(['categories']); setCatName(''); setCatColor('#c96a2b') }
  })

  const deleteCat = useMutation({ mutationFn: (id: number) => deleteCategory(id), onSuccess: () => inv(['categories']) })

  const visibleProducts = selectedCategoryId === null
    ? products
    : products.filter(p => p.categoryId === selectedCategoryId)

  const tabCls = (t: 'products' | 'categories') =>
    tab === t
      ? 'px-4 py-2 text-sm font-medium border-b-2 border-[#c96a2b] text-[#c96a2b]'
      : 'px-4 py-2 text-sm font-medium border-b-2 border-transparent text-[#7a5c3a] hover:text-[#3d1f08]'

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold text-[#3d1f08]">Products</h1>
        <Link to="/products/uncategorized" className="text-sm text-[#7a5c3a] hover:text-[#c96a2b]">
          Uncategorized →
        </Link>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-[#e8c9a0] mb-6">
        <button className={tabCls('products')} onClick={() => setTab('products')}>Products</button>
        <button className={tabCls('categories')} onClick={() => setTab('categories')}>Categories</button>
      </div>

      {tab === 'products' && (
        <div>
          {/* Category filter */}
          <div className="flex flex-wrap gap-1 mb-4">
            <button
              onClick={() => setSelectedCategoryId(null)}
              className={`px-3 py-1 rounded-full text-sm transition-colors ${selectedCategoryId === null ? 'bg-[#c96a2b] text-white' : 'bg-white border border-[#e8c9a0] text-[#3d1f08]'}`}
            >
              All
            </button>
            {categories.map(c => (
              <button
                key={c.id}
                onClick={() => setSelectedCategoryId(c.id)}
                className={`px-3 py-1 rounded-full text-sm transition-colors flex items-center gap-1.5 ${selectedCategoryId === c.id ? 'bg-[#c96a2b] text-white' : 'bg-white border border-[#e8c9a0] text-[#3d1f08]'}`}
              >
                <span className="w-2 h-2 rounded-full flex-shrink-0" style={{ background: c.color }} />
                {c.name}
              </button>
            ))}
          </div>

          {/* Add product form */}
          <form onSubmit={e => { e.preventDefault(); create.mutate() }} className="bg-white border border-[#e8c9a0] rounded-lg p-3 mb-3 flex flex-wrap gap-2 items-end">
            <Input value={newName} onChange={e => setNewName(e.target.value)} placeholder="Product name" required className="border-[#e8c9a0] flex-1 min-w-[120px] h-8 text-sm" />
            <Input type="number" value={newPrice} onChange={e => setNewPrice(e.target.value)} placeholder="Price" className="border-[#e8c9a0] w-20 h-8 text-sm" />
            <select value={newCatId} onChange={e => setNewCatId(e.target.value)} className="border border-[#e8c9a0] rounded-md px-2 h-8 text-sm bg-white">
              <option value="">No category</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <Button type="submit" size="sm" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white h-8">Add</Button>
          </form>

          {visibleProducts.length === 0 ? (
            <p className="text-[#7a5c3a] text-sm py-6 text-center">No products here yet.</p>
          ) : (
            <div className="flex flex-col gap-1">
              {visibleProducts.map(p => (
                <div key={p.id} className="bg-white border border-[#e8c9a0] rounded-lg px-3 py-2">
                  {editId === p.id ? (
                    <form onSubmit={e => { e.preventDefault(); edit.mutate() }} className="flex flex-wrap gap-2 items-center">
                      <Input value={editName} onChange={e => setEditName(e.target.value)} required className="border-[#e8c9a0] flex-1 h-7 text-sm" />
                      <Input type="number" value={editPrice} onChange={e => setEditPrice(e.target.value)} className="border-[#e8c9a0] w-20 h-7 text-sm" />
                      <select value={editCatId} onChange={e => setEditCatId(e.target.value)} className="border border-[#e8c9a0] rounded px-2 h-7 text-sm bg-white">
                        <option value="">No category</option>
                        {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                      </select>
                      <button type="submit" className="text-green-600 hover:text-green-800 p-1"><Check className="w-4 h-4" /></button>
                      <button type="button" onClick={() => setEditId(null)} className="text-[#7a5c3a] hover:text-[#3d1f08] p-1"><X className="w-4 h-4" /></button>
                    </form>
                  ) : (
                    <div className="flex items-center gap-2">
                      {p.categoryColor && (
                        <span className="w-2 h-2 rounded-full flex-shrink-0" style={{ background: p.categoryColor }} />
                      )}
                      <span className="flex-1 text-sm text-[#3d1f08]">{p.name}</span>
                      {p.price != null && <span className="text-sm text-[#7a5c3a]">{p.price.toFixed(2)}</span>}
                      <button
                        onClick={() => { setEditId(p.id); setEditName(p.name); setEditPrice(p.price?.toString() ?? ''); setEditCatId(p.categoryId?.toString() ?? '') }}
                        className="text-[#c9b09a] hover:text-[#c96a2b] p-1"
                      >
                        <Pencil className="w-3 h-3" />
                      </button>
                      <button onClick={() => del.mutate(p.id)} className="text-[#c9b09a] hover:text-red-500 p-1">
                        <Trash2 className="w-3 h-3" />
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {tab === 'categories' && (
        <div>
          <div className="flex flex-col gap-2 mb-6">
            {categories.map(c => (
              <div key={c.id} className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3">
                <span className="w-3 h-3 rounded-full flex-shrink-0" style={{ background: c.color }} />
                <span className="flex-1 text-sm text-[#3d1f08]">{c.name}</span>
                <button onClick={() => deleteCat.mutate(c.id)} className="text-[#c9b09a] hover:text-red-500 p-1">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
            {categories.length === 0 && <p className="text-[#7a5c3a] text-sm text-center py-6">No categories yet.</p>}
          </div>

          <form onSubmit={e => { e.preventDefault(); createCat.mutate() }} className="bg-white border border-[#e8c9a0] rounded-lg p-4 flex flex-col gap-3">
            <p className="text-sm font-semibold text-[#3d1f08]">Add category</p>
            <Input value={catName} onChange={e => setCatName(e.target.value)} placeholder="Category name" required className="border-[#e8c9a0]" />
            <div className="flex gap-3 items-center">
              <input type="color" value={catColor} onChange={e => setCatColor(e.target.value)} className="w-10 h-10 rounded border border-[#e8c9a0] cursor-pointer" />
              <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white flex-1">Add</Button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
