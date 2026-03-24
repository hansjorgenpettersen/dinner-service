import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getUncategorizedProducts, getCategories, updateProduct } from '../api/products'
import { ChevronLeft } from 'lucide-react'

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
    <div className="max-w-4xl mx-auto px-4 py-8">
      <Link to="/products" className="inline-flex items-center gap-1 text-[#7a5c3a] hover:text-[#c96a2b] text-sm mb-6">
        <ChevronLeft className="w-4 h-4" /> Back to products
      </Link>
      <h1 className="text-2xl font-bold text-[#3d1f08] mb-6">Uncategorized Products</h1>
      {products.length === 0 ? (
        <p className="text-[#7a5c3a] text-center py-12">All products have a category. 🎉</p>
      ) : (
        <div className="flex flex-col gap-2">
          {products.map(p => (
            <div key={p.id} className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3">
              <span className="font-medium text-[#3d1f08] flex-1">{p.name}</span>
              <select
                defaultValue=""
                onChange={e => { if (e.target.value) assign.mutate({ id: p.id, categoryId: Number(e.target.value) }) }}
                className="border border-[#e8c9a0] rounded-md px-2 py-1.5 text-sm bg-white text-[#3d1f08]"
              >
                <option value="" disabled>Assign category…</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
