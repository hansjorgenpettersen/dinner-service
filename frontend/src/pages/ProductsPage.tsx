import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProducts, getCategories, createProduct, updateProduct, deleteProduct,
         createCategory, deleteCategory } from '../api/products'

export default function ProductsPage() {
  const qc = useQueryClient()
  const { data: products = [] } = useQuery({ queryKey: ['products'], queryFn: getProducts })
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })

  const [newName, setNewName] = useState('')
  const [newPrice, setNewPrice] = useState('')
  const [newCatId, setNewCatId] = useState('')
  const [catName, setCatName] = useState('')
  const [catColor, setCatColor] = useState('#cccccc')
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
    onSuccess: () => { inv(['categories']); setCatName(''); setCatColor('#cccccc') } })

  const deleteCat = useMutation({ mutationFn: (id: number) => deleteCategory(id), onSuccess: () => inv(['categories']) })

  return (
    <div>
      <h1>Products</h1>

      <form onSubmit={e => { e.preventDefault(); create.mutate() }}>
        <input value={newName} onChange={e => setNewName(e.target.value)} placeholder="Product name" required />
        <input type="number" value={newPrice} onChange={e => setNewPrice(e.target.value)} placeholder="Price" />
        <select value={newCatId} onChange={e => setNewCatId(e.target.value)}>
          <option value="">No category</option>
          {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <button type="submit">Add product</button>
      </form>

      <table>
        <tbody>
          {products.map(p => (
            <tr key={p.id}>
              {editId === p.id ? (
                <td colSpan={4}>
                  <form onSubmit={e => { e.preventDefault(); edit.mutate() }}>
                    <input value={editName} onChange={e => setEditName(e.target.value)} required />
                    <input type="number" value={editPrice} onChange={e => setEditPrice(e.target.value)} />
                    <select value={editCatId} onChange={e => setEditCatId(e.target.value)}>
                      <option value="">No category</option>
                      {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                    <button type="submit">Save</button>
                    <button type="button" onClick={() => setEditId(null)}>Cancel</button>
                  </form>
                </td>
              ) : (
                <>
                  <td>{p.categoryColor && <span style={{ background: p.categoryColor, padding: '2px 6px' }}>{p.categoryName}</span>}</td>
                  <td>{p.name}</td>
                  <td>{p.price != null ? `€${p.price.toFixed(2)}` : ''}</td>
                  <td>
                    <button onClick={() => { setEditId(p.id); setEditName(p.name); setEditPrice(p.price?.toString() ?? ''); setEditCatId(p.categoryId?.toString() ?? '') }}>Edit</button>
                    <button onClick={() => del.mutate(p.id)}>Delete</button>
                  </td>
                </>
              )}
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Categories</h2>
      <form onSubmit={e => { e.preventDefault(); createCat.mutate() }}>
        <input value={catName} onChange={e => setCatName(e.target.value)} placeholder="Category name" required />
        <input type="color" value={catColor} onChange={e => setCatColor(e.target.value)} />
        <button type="submit">Add category</button>
      </form>
      <ul>
        {categories.map(c => (
          <li key={c.id} style={{ color: c.color }}>
            {c.name}
            <button onClick={() => deleteCat.mutate(c.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  )
}
