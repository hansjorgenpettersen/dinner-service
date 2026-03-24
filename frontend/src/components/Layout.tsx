import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { currentEmail, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div>
      <nav>
        <Link to="/dashboard">Home</Link>
        <Link to="/recipes">Recipes</Link>
        <Link to="/shopping-lists">Shopping Lists</Link>
        <Link to="/products">Products</Link>
        <Link to="/user">{currentEmail}</Link>
        <button onClick={handleLogout}>Logout</button>
      </nav>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
