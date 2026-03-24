import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getShoppingLists, createShoppingList } from '../api/shoppingLists'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Badge } from '../components/ui/badge'
import { ChevronRight } from 'lucide-react'

export default function ShoppingListsPage() {
  const qc = useQueryClient()
  const [name, setName] = useState('')
  const [showForm, setShowForm] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['shopping-lists'], queryFn: getShoppingLists })
  const create = useMutation({
    mutationFn: createShoppingList,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['shopping-lists'] }); setName(''); setShowForm(false) }
  })

  if (isLoading) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  const owned = data?.owned ?? []
  const shared = data?.shared ?? []

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#3d1f08]">Shopping Lists</h1>
        <Button onClick={() => setShowForm(v => !v)} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white">
          + New List
        </Button>
      </div>

      {showForm && (
        <form
          onSubmit={e => { e.preventDefault(); create.mutate(name) }}
          className="flex gap-2 mb-6 bg-white border border-[#e8c9a0] rounded-lg p-4"
        >
          <Input value={name} onChange={e => setName(e.target.value)} placeholder="List name" required className="border-[#e8c9a0] flex-1" autoFocus />
          <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white">Create</Button>
          <Button type="button" variant="outline" onClick={() => setShowForm(false)} className="border-[#e8c9a0] text-[#7a5c3a]">Cancel</Button>
        </form>
      )}

      {owned.length > 0 && (
        <div className="mb-6">
          <h2 className="text-sm font-semibold text-[#7a5c3a] uppercase tracking-wide mb-2">My Lists</h2>
          <div className="flex flex-col gap-2">
            {owned.map(l => (
              <Link key={l.id} to={`/shopping-lists/${l.id}`}>
                <div className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3 hover:border-[#c96a2b] transition-colors">
                  <span className="text-xl">🛒</span>
                  <span className="font-medium text-[#3d1f08] flex-1">{l.name}</span>
                  <ChevronRight className="text-[#c96a2b] w-4 h-4" />
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      {shared.length > 0 && (
        <div>
          <h2 className="text-sm font-semibold text-[#7a5c3a] uppercase tracking-wide mb-2">Shared With Me</h2>
          <div className="flex flex-col gap-2">
            {shared.map(l => (
              <Link key={l.id} to={`/shopping-lists/${l.id}`}>
                <div className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3 hover:border-[#c96a2b] transition-colors">
                  <span className="text-xl">🛒</span>
                  <span className="font-medium text-[#3d1f08] flex-1">{l.name}</span>
                  <Badge variant="outline" className="border-[#e8c9a0] text-[#7a5c3a] text-xs">{l.ownerEmail}</Badge>
                  <ChevronRight className="text-[#c96a2b] w-4 h-4" />
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      {owned.length === 0 && shared.length === 0 && (
        <p className="text-[#7a5c3a] text-center py-12">No lists yet. Create your first one!</p>
      )}
    </div>
  )
}
