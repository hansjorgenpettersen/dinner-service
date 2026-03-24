import { useState, FormEvent } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { resetPassword } from '../api/auth'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

export default function ResetPasswordPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const token = params.get('token') ?? ''
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (password !== confirmPassword) { setError('Passwords do not match.'); return }
    setLoading(true)
    try {
      await resetPassword({ token, password, confirmPassword })
      navigate('/login?reset=1')
    } catch {
      setError('Reset failed. The link may have expired.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#fdf6ee] flex items-center justify-center px-4">
      <Card className="w-full max-w-sm border-[#e8c9a0] shadow-sm">
        <CardHeader className="text-center pb-2">
          <div className="text-4xl mb-1">🍽</div>
          <CardTitle className="text-[#3d1f08] text-xl">Set new password</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <p role="alert" className="text-red-600 text-sm mb-4 bg-red-50 border border-red-200 rounded px-3 py-2">
              {error}
            </p>
          )}
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password" className="text-[#3d1f08]">New password</Label>
              <Input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="confirm" className="text-[#3d1f08]">Confirm password</Label>
              <Input id="confirm" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <Button type="submit" disabled={loading} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white w-full">
              {loading ? 'Saving…' : 'Set password'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
