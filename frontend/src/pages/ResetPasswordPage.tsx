import { useState, FormEvent } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { resetPassword } from '../api/auth'

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
    <div>
      <h1>Set new password</h1>
      {error && <p role="alert">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label htmlFor="password">New password</label>
        <input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} />
        <label htmlFor="confirm">Confirm password</label>
        <input id="confirm" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
        <button type="submit" disabled={loading}>Set password</button>
      </form>
    </div>
  )
}
