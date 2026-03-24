import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getUncategorizedProducts, getCategories, updateProduct } from '../api/products'

export default function UncategorizedProductsPage() {
  const qc = useQueryClient()
  const { data: products = [] } = useQuery({ queryKey: ['products-uncategorized'], queryFn: getUncategorizedProducts })
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })

  const assign = useMutation({
    mutationFn: ({ id, categoryId }: { id: number; categoryId: number }) =>
      updateProduct(id, { name: products.find(p => p.id === id)!.name, categoryId }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['products-uncategorized'] })
      qc.invalidateQueries({ queryKey: ['products'] })
    }
  })

  return (
    <div>
      <h1>Uncategorized Products</h1>
      {products.length === 0 && <p>All products have a category.</p>}
      <ul>
        {products.map(p => (
          <li key={p.id}>
            {p.name}
            <select
              defaultValue=""
              onChange={e => { if (e.target.value) assign.mutate({ id: p.id, categoryId: Number(e.target.value) }) }}
            >
              <option value="" disabled>Assign category…</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </li>
        ))}
      </ul>
    </div>
  )
}
