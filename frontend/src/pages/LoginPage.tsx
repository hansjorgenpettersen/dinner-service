import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    navigate('/dashboard', { replace: true })
    return null
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login({ email, password })
      navigate('/dashboard', { replace: true })
    } catch {
      setError('Invalid email or password.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#fdf6ee] flex items-center justify-center px-4">
      <Card className="w-full max-w-sm border-[#e8c9a0] shadow-sm">
        <CardHeader className="text-center pb-2">
          <div className="text-4xl mb-1">🍽</div>
          <CardTitle className="text-[#3d1f08] text-xl">Sign in</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <p role="alert" className="text-red-600 text-sm mb-4 bg-red-50 border border-red-200 rounded px-3 py-2">
              {error}
            </p>
          )}
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email" className="text-[#3d1f08]">Email</Label>
              <Input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password" className="text-[#3d1f08]">Password</Label>
              <Input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <Button type="submit" disabled={loading} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white w-full mt-1">
              {loading ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>
          <div className="flex justify-between mt-4 text-sm">
            <Link to="/register" className="text-[#7a5c3a] hover:text-[#c96a2b]">Create account</Link>
            <Link to="/forgot-password" className="text-[#7a5c3a] hover:text-[#c96a2b]">Forgot password?</Link>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
