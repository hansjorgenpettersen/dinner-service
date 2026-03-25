import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { getRecipes, createRecipe } from '../api/recipes'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { ChevronRight } from 'lucide-react'

export default function RecipesPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [showForm, setShowForm] = useState(false)
  const { data: recipes = [], isLoading } = useQuery({ queryKey: ['recipes'], queryFn: getRecipes })
  const create = useMutation({
    mutationFn: createRecipe,
    onSuccess: (recipe) => { qc.invalidateQueries({ queryKey: ['recipes'] }); navigate(`/recipes/${recipe.id}`) }
  })

  if (isLoading) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#3d1f08]">Recipes</h1>
        <Button
          onClick={() => setShowForm(v => !v)}
          className="bg-[#c96a2b] hover:bg-[#a8571f] text-white"
        >
          + New Recipe
        </Button>
      </div>

      {showForm && (
        <form
          onSubmit={e => { e.preventDefault(); create.mutate({ name }); setShowForm(false); setName('') }}
          className="flex gap-2 mb-6 bg-white border border-[#e8c9a0] rounded-lg p-4"
        >
          <Input
            value={name}
            onChange={e => setName(e.target.value)}
            placeholder="Recipe name"
            required
            className="border-[#e8c9a0] flex-1"
            autoFocus
          />
          <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white">Create</Button>
          <Button type="button" variant="outline" onClick={() => setShowForm(false)} className="border-[#e8c9a0] text-[#7a5c3a]">Cancel</Button>
        </form>
      )}

      {recipes.length === 0 ? (
        <p className="text-[#7a5c3a] text-center py-12">No recipes yet. Create your first one!</p>
      ) : (
        <div className="flex flex-col gap-2">
          {recipes.map(r => (
            <Link key={r.id} to={`/recipes/${r.id}`}>
              <div className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3 hover:border-[#c96a2b] transition-colors">
                {r.previewImage
                  ? <img src={`/api/recipe-images/${r.previewImage}`} alt={r.name} className="w-10 h-10 rounded object-cover flex-shrink-0" />
                  : <span className="text-2xl flex-shrink-0">🍽</span>
                }
                <span className="font-medium text-[#3d1f08] flex-1">{r.name}</span>
                <ChevronRight className="text-[#c96a2b] w-4 h-4 flex-shrink-0" />
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
