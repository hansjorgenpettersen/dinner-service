import { useState, FormEvent } from 'react'
import { forgotPassword } from '../api/auth'

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

  if (sent) return <p>If that email exists, a reset link has been sent.</p>

  return (
    <div>
      <h1>Reset password</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        <button type="submit" disabled={loading}>Send reset link</button>
      </form>
    </div>
  )
}
