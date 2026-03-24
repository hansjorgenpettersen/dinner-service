import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getShoppingLists, createShoppingList } from '../api/shoppingLists'

export default function ShoppingListsPage() {
  const qc = useQueryClient()
  const [name, setName] = useState('')
  const { data, isLoading } = useQuery({ queryKey: ['shopping-lists'], queryFn: getShoppingLists })
  const create = useMutation({
    mutationFn: createShoppingList,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['shopping-lists'] }); setName('') }
  })

  if (isLoading) return <p>Loading...</p>

  return (
    <div>
      <h1>Shopping Lists</h1>
      <form onSubmit={e => { e.preventDefault(); create.mutate(name) }}>
        <input value={name} onChange={e => setName(e.target.value)} placeholder="New list name" required />
        <button type="submit">Create</button>
      </form>
      <h2>My Lists</h2>
      <ul>{(data?.owned ?? []).map(l => <li key={l.id}><Link to={`/shopping-lists/${l.id}`}>{l.name}</Link></li>)}</ul>
      <h2>Shared With Me</h2>
      <ul>{(data?.shared ?? []).map(l => <li key={l.id}><Link to={`/shopping-lists/${l.id}`}>{l.name} (by {l.ownerEmail})</Link></li>)}</ul>
    </div>
  )
}
