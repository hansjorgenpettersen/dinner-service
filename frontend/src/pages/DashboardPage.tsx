import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Card, CardContent } from '../components/ui/card'

export default function DashboardPage() {
  const { currentEmail } = useAuth()

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-[#3d1f08] mb-2">Welcome back</h1>
      <p className="text-[#7a5c3a] mb-8">{currentEmail}</p>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Link to="/recipes">
          <Card className="border-[#e8c9a0] shadow-sm hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 flex flex-col items-center gap-2">
              <span className="text-4xl">📖</span>
              <span className="font-semibold text-[#3d1f08]">Recipes</span>
              <span className="text-sm text-[#7a5c3a]">Browse your recipes</span>
            </CardContent>
          </Card>
        </Link>

        <Link to="/shopping-lists">
          <Card className="border-[#e8c9a0] shadow-sm hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 flex flex-col items-center gap-2">
              <span className="text-4xl">🛒</span>
              <span className="font-semibold text-[#3d1f08]">Shopping Lists</span>
              <span className="text-sm text-[#7a5c3a]">Manage your lists</span>
            </CardContent>
          </Card>
        </Link>

        <Link to="/products">
          <Card className="border-[#e8c9a0] shadow-sm hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 flex flex-col items-center gap-2">
              <span className="text-4xl">📦</span>
              <span className="font-semibold text-[#3d1f08]">Products</span>
              <span className="text-sm text-[#7a5c3a]">Manage products</span>
            </CardContent>
          </Card>
        </Link>
      </div>
    </div>
  )
}
