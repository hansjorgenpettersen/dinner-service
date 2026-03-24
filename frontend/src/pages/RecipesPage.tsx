import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { getRecipes, createRecipe } from '../api/recipes'

export default function RecipesPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const { data: recipes = [], isLoading } = useQuery({ queryKey: ['recipes'], queryFn: getRecipes })
  const create = useMutation({
    mutationFn: createRecipe,
    onSuccess: (recipe) => { qc.invalidateQueries({ queryKey: ['recipes'] }); navigate(`/recipes/${recipe.id}`) }
  })

  if (isLoading) return <p>Loading...</p>

  return (
    <div>
      <h1>Recipes</h1>
      <form onSubmit={e => { e.preventDefault(); create.mutate({ name }) }}>
        <input value={name} onChange={e => setName(e.target.value)} placeholder="New recipe name" required />
        <button type="submit">Add Recipe</button>
      </form>
      <ul>
        {recipes.map(r => (
          <li key={r.id}>
            {r.previewImage && <img src={`/recipe-images/${r.previewImage}`} alt={r.name} width={60} />}
            <Link to={`/recipes/${r.id}`}>{r.name}</Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
