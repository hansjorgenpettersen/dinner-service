import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getShoppingList, addItem, toggleItem, deleteItem, clearChecked, shareList } from '../api/shoppingLists'
import type { ShoppingListDetail } from '../api/types'

export default function ShoppingListDetailPage() {
  const { id } = useParams<{ id: string }>()
  const listId = Number(id)
  const qc = useQueryClient()
  const [itemName, setItemName] = useState('')
  const [count, setCount] = useState('')
  const [unitPrice, setUnitPrice] = useState('')
  const [shareEmail, setShareEmail] = useState('')
  const [shareError, setShareError] = useState<string | null>(null)

  const { data: list, isLoading } = useQuery({
    queryKey: ['shopping-list', listId],
    queryFn: () => getShoppingList(listId)
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['shopping-list', listId] })

  const add = useMutation({ mutationFn: () =>
    addItem(listId, { name: itemName, count: count ? Number(count) : undefined, unitPrice: unitPrice ? Number(unitPrice) : undefined }),
    onSuccess: () => { invalidate(); setItemName(''); setCount(''); setUnitPrice('') }
  })

  // Optimistic toggle for instant UI feedback on mobile
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

  if (isLoading || !list) return <p>Loading...</p>

  const groupedByCategory = list.items.reduce<Record<string, typeof list.items>>((acc, item) => {
    const key = item.categoryName ?? 'Uncategorized'
    acc[key] = [...(acc[key] ?? []), item]
    return acc
  }, {})

  return (
    <div>
      <h1>{list.name}</h1>
      <p>Total: €{list.totalPrice.toFixed(2)} | {list.items.length} items</p>

      <form onSubmit={e => { e.preventDefault(); add.mutate() }}>
        <input value={itemName} onChange={e => setItemName(e.target.value)} placeholder="Item name" required />
        <input type="number" value={count} onChange={e => setCount(e.target.value)} placeholder="Qty" />
        <input type="number" value={unitPrice} onChange={e => setUnitPrice(e.target.value)} placeholder="Price" />
        <button type="submit">Add</button>
      </form>

      {Object.entries(groupedByCategory).map(([category, items]) => (
        <div key={category}>
          <h3>{category}</h3>
          <ul>
            {items.map(item => (
              <li key={item.id} style={{ textDecoration: item.checked ? 'line-through' : 'none' }}>
                <input type="checkbox" checked={item.checked} onChange={() => toggle.mutate(item.id)} />
                {item.count} × {item.name}
                {item.totalPrice != null && ` €${item.totalPrice.toFixed(2)}`}
                <button onClick={() => remove.mutate(item.id)}>✕</button>
              </li>
            ))}
          </ul>
        </div>
      ))}

      <button onClick={() => clear.mutate()}>Clear checked</button>

      {list.isOwner && (
        <div>
          <h3>Share list</h3>
          {shareError && <p role="alert">{shareError}</p>}
          <form onSubmit={e => { e.preventDefault(); share.mutate() }}>
            <input type="email" value={shareEmail} onChange={e => setShareEmail(e.target.value)} placeholder="Email to share with" required />
            <button type="submit">Share</button>
          </form>
        </div>
      )}
    </div>
  )
}
