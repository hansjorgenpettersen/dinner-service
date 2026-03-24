import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { currentEmail, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const navCls = ({ isActive }: { isActive: boolean }) =>
    isActive
      ? 'bg-[#c96a2b] text-white rounded px-3 py-1 text-sm font-medium'
      : 'text-[#d4a07a] hover:text-[#f5e6d3] px-3 py-1 text-sm transition-colors'

  return (
    <div className="min-h-screen bg-[#fdf6ee]">
      <nav className="bg-[#7a3a1a] px-4 py-3 flex items-center gap-1 sticky top-0 z-10">
        <span className="text-[#f5e6d3] font-bold text-lg mr-4 select-none">🍽 Dinner Service</span>
        <NavLink to="/dashboard" className={navCls}>Dashboard</NavLink>
        <NavLink to="/recipes" className={navCls}>Recipes</NavLink>
        <NavLink to="/shopping-lists" className={navCls}>Shopping Lists</NavLink>
        <NavLink to="/products" className={navCls}>Products</NavLink>
        <div className="ml-auto flex items-center gap-3">
          <NavLink to="/user" className={navCls}>
            <span className="max-w-[160px] truncate inline-block align-bottom">{currentEmail}</span>
          </NavLink>
          <button
            onClick={handleLogout}
            className="text-[#d4a07a] hover:text-[#f5e6d3] text-sm transition-colors"
          >
            Logout
          </button>
        </div>
      </nav>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
