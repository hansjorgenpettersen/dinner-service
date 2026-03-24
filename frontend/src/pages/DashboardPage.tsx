import { useAuth } from '../context/AuthContext'
import { Link } from 'react-router-dom'

export default function DashboardPage() {
  const { currentEmail } = useAuth()
  return (
    <div>
      <h1>Welcome, {currentEmail}</h1>
      <nav>
        <Link to="/recipes">Recipes</Link>
        <Link to="/shopping-lists">Shopping Lists</Link>
        <Link to="/products">Products</Link>
      </nav>
    </div>
  )
}
