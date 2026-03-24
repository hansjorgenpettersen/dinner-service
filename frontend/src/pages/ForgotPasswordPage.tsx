import { useState, FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../api/auth'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true)
    await forgotPassword({ email }).catch(() => {})
    setSent(true)
    setLoading(false)
  }

  return (
    <div className="min-h-screen bg-[#fdf6ee] flex items-center justify-center px-4">
      <Card className="w-full max-w-sm border-[#e8c9a0] shadow-sm">
        <CardHeader className="text-center pb-2">
          <div className="text-4xl mb-1">🍽</div>
          <CardTitle className="text-[#3d1f08] text-xl">Reset password</CardTitle>
        </CardHeader>
        <CardContent>
          {sent ? (
            <div className="text-center py-2">
              <p className="text-[#7a5c3a] mb-4">If that email exists, a reset link has been sent.</p>
              <Link to="/login" className="text-[#c96a2b] hover:underline text-sm">Back to sign in</Link>
            </div>
          ) : (
            <>
              <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="email" className="text-[#3d1f08]">Email</Label>
                  <Input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
                </div>
                <Button type="submit" disabled={loading} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white w-full">
                  {loading ? 'Sending…' : 'Send reset link'}
                </Button>
              </form>
              <div className="mt-4 text-center text-sm">
                <Link to="/login" className="text-[#7a5c3a] hover:text-[#c96a2b]">Back to sign in</Link>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
