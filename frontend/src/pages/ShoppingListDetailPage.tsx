import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getShoppingList, addItem, toggleItem, deleteItem, clearChecked, shareList, unshareList } from '../api/shoppingLists'
import { searchProducts } from '../api/products'
import type { ShoppingListDetail } from '../api/types'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { ChevronLeft, X } from 'lucide-react'

export default function ShoppingListDetailPage() {
  const { id } = useParams<{ id: string }>()
  const listId = Number(id)
  const qc = useQueryClient()
  const [itemName, setItemName] = useState('')
  const [count, setCount] = useState('1')
  const [unitPrice, setUnitPrice] = useState('')
  const [shareEmail, setShareEmail] = useState('')
  const [shareError, setShareError] = useState<string | null>(null)
  const [inputFocused, setInputFocused] = useState(false)

  const { data: suggestions = [] } = useQuery({
    queryKey: ['product-search', itemName],
    queryFn: () => searchProducts(itemName),
    enabled: itemName.length > 1
  })

  const { data: list, isLoading } = useQuery({
    queryKey: ['shopping-list', listId],
    queryFn: () => getShoppingList(listId)
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['shopping-list', listId] })

  const add = useMutation({ mutationFn: () =>
    addItem(listId, { name: itemName, count: count ? Number(count) : undefined, unitPrice: unitPrice ? Number(unitPrice) : undefined }),
    onSuccess: () => { invalidate(); setItemName(''); setCount('1'); setUnitPrice('') }
  })

  const toggle = useMutation({
    mutationFn: (itemId: number) => toggleItem(listId, itemId),
    onMutate: async (itemId) => {
      await qc.cancelQueries({ queryKey: ['shopping-list', listId] })
      const prev = qc.getQueryData(['shopping-list', listId])
      qc.setQueryData(['shopping-list', listId], (old: ShoppingListDetail | undefined) => old ? {
        ...old,
        items: old.items.map(i => i.id === itemId ? { ...i, checked: !i.checked } : i)
      } : old)
      return { prev }
    },
    onError: (_err, _id, ctx) => { if (ctx?.prev) qc.setQueryData(['shopping-list', listId], ctx.prev) },
    onSettled: invalidate
  })

  const remove = useMutation({ mutationFn: (itemId: number) => deleteItem(listId, itemId), onSuccess: invalidate })
  const clear = useMutation({ mutationFn: () => clearChecked(listId), onSuccess: invalidate })
  const share = useMutation({
    mutationFn: () => shareList(listId, shareEmail),
    onSuccess: () => { setShareEmail(''); setShareError(null); invalidate() },
    onError: () => setShareError('User not found.')
  })

  const unshare = useMutation({
    mutationFn: (email: string) => unshareList(listId, email),
    onSuccess: invalidate
  })

  if (isLoading || !list) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  const groupedByCategory = list.items.reduce<Record<string, typeof list.items>>((acc, item) => {
    const key = item.categoryName ?? 'Uncategorized'
    acc[key] = [...(acc[key] ?? []), item]
    return acc
  }, {})

  const checkedCount = list.items.filter(i => i.checked).length

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <Link to="/shopping-lists" className="inline-flex items-center gap-1 text-[#7a5c3a] hover:text-[#c96a2b] text-sm mb-6">
        <ChevronLeft className="w-4 h-4" /> Back to lists
      </Link>

      <div className="flex items-center justify-between mb-2">
        <h1 className="text-2xl font-bold text-[#3d1f08]">{list.name}</h1>
        {checkedCount > 0 && (
          <Button variant="outline" size="sm" onClick={() => clear.mutate()} className="border-[#e8c9a0] text-[#7a5c3a] hover:text-red-600">
            Clear checked ({checkedCount})
          </Button>
        )}
      </div>
      <p className="text-[#7a5c3a] text-sm mb-6">
        {list.items.length} items · Total {list.totalPrice.toFixed(2)}
      </p>

      {/* Add item form */}
      <form onSubmit={e => { e.preventDefault(); add.mutate() }} className="bg-white border border-[#e8c9a0] rounded-lg p-4 mb-6 flex flex-wrap gap-2 items-end">
        <div className="flex flex-col gap-1 flex-1 min-w-[140px] relative">
          <label className="text-xs text-[#7a5c3a]">Item</label>
          <Input
            value={itemName}
            onChange={e => setItemName(e.target.value)}
            onFocus={() => setInputFocused(true)}
            onBlur={() => setTimeout(() => setInputFocused(false), 150)}
            placeholder="Item name"
            required
            className="border-[#e8c9a0]"
          />
          {inputFocused && suggestions.length > 0 && (
            <div className="absolute top-full left-0 right-0 bg-white border border-[#e8c9a0] rounded-md shadow-md z-10 mt-1">
              {suggestions.map(p => (
                <button
                  key={p.id}
                  type="button"
                  onMouseDown={() => {
                    setItemName(p.name)
                    if (p.price != null) setUnitPrice(p.price.toString())
                    setShowSuggestions(false)
                  }}
                  className="w-full text-left px-3 py-2 text-sm text-[#3d1f08] hover:bg-[#fdf0e0] flex justify-between"
                >
                  <span>{p.name}</span>
                  {p.price != null && <span className="text-[#7a5c3a]">{p.price.toFixed(2)}</span>}
                </button>
              ))}
            </div>
          )}
        </div>
        <div className="flex flex-col gap-1 w-20">
          <label className="text-xs text-[#7a5c3a]">Qty</label>
          <Input type="number" value={count} onChange={e => setCount(e.target.value)} placeholder="0" className="border-[#e8c9a0]" />
        </div>
        <div className="flex flex-col gap-1 w-24">
          <label className="text-xs text-[#7a5c3a]">Unit price</label>
          <Input type="number" value={unitPrice} onChange={e => setUnitPrice(e.target.value)} placeholder="0.00" className="border-[#e8c9a0]" />
        </div>
        <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white self-end">Add</Button>
      </form>

      {/* Items grouped by category */}
      <div className="flex flex-col gap-4">
        {Object.entries(groupedByCategory).map(([category, items]) => (
          <div key={category} className="bg-white border border-[#e8c9a0] rounded-lg overflow-hidden">
            <div className="px-4 py-2 bg-[#fdf0e0] border-b border-[#e8c9a0]">
              <h3 className="text-sm font-semibold text-[#7a5c3a] uppercase tracking-wide">{category}</h3>
            </div>
            <div>
              {items.map(item => (
                <div key={item.id} className="flex items-center gap-3 px-4 py-3 border-b border-[#f5ebe0] last:border-0">
                  <input
                    type="checkbox"
                    checked={item.checked}
                    onChange={() => toggle.mutate(item.id)}
                    className="w-4 h-4 accent-[#c96a2b] flex-shrink-0"
                  />
                  <span className={`flex-1 text-sm ${item.checked ? 'line-through text-[#b0a090]' : 'text-[#3d1f08]'}`}>
                    {item.count && `${item.count} × `}{item.name}
                    {item.totalPrice != null && <span className="text-[#7a5c3a] ml-2">{item.totalPrice.toFixed(2)}</span>}
                  </span>
                  <button onClick={() => remove.mutate(item.id)} className="text-[#c9b09a] hover:text-red-500 flex-shrink-0">
                    <X className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Share section */}
      {list.isOwner && (
        <div className="mt-8 bg-white border border-[#e8c9a0] rounded-lg p-6">
          <h3 className="text-base font-semibold text-[#3d1f08] mb-3">Sharing</h3>
          {list.sharedWith.length > 0 && (
            <div className="flex flex-col gap-2 mb-4">
              {list.sharedWith.map(email => (
                <div key={email} className="flex items-center justify-between text-sm">
                  <span className="text-[#3d1f08]">{email}</span>
                  <button onClick={() => unshare.mutate(email)} className="text-[#c9b09a] hover:text-red-500 text-xs px-2 py-1">Remove</button>
                </div>
              ))}
            </div>
          )}
          {shareError && <p role="alert" className="text-red-600 text-sm mb-3">{shareError}</p>}
          <form onSubmit={e => { e.preventDefault(); share.mutate() }} className="flex gap-2">
            <Input type="email" value={shareEmail} onChange={e => setShareEmail(e.target.value)} placeholder="Email to share with" required className="border-[#e8c9a0] flex-1" />
            <Button type="submit" variant="outline" className="border-[#e8c9a0] text-[#7a5c3a] hover:text-[#3d1f08]">Share</Button>
          </form>
        </div>
      )}
    </div>
  )
}
