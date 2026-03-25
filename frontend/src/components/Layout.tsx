import { NavLink, Outlet } from 'react-router-dom'

export default function Layout() {
  const navCls = ({ isActive }: { isActive: boolean }) =>
    isActive
      ? 'bg-[#c96a2b] text-white rounded px-3 py-1 text-sm font-medium'
      : 'text-[#d4a07a] hover:text-[#f5e6d3] px-3 py-1 text-sm transition-colors'

  return (
    <div className="min-h-screen bg-[#fdf6ee]">
      <nav className="bg-[#7a3a1a] px-3 py-2 flex items-center gap-1 sticky top-0 z-10">
        <span className="hidden sm:block text-[#f5e6d3] font-bold text-lg mr-3 select-none">🍽 Dinner Service</span>
        <span className="sm:hidden text-[#f5e6d3] font-bold text-lg mr-2 select-none">🍽</span>
        <NavLink to="/dashboard" className={navCls}>
          <span className="hidden sm:inline">Dashboard</span>
          <span className="sm:hidden">Home</span>
        </NavLink>
        <NavLink to="/recipes" className={navCls}>Recipes</NavLink>
        <NavLink to="/shopping-lists" className={navCls}>
          <span className="hidden sm:inline">Shopping Lists</span>
          <span className="sm:hidden">Lists</span>
        </NavLink>
        <NavLink to="/products" className={navCls}>Products</NavLink>
        <div className="ml-auto">
          <NavLink to="/user" className={navCls}>
            <span className="hidden sm:inline">User</span>
            <span className="sm:hidden">👤</span>
          </NavLink>
        </div>
      </nav>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
