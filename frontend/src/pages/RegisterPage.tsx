import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { register } from '../api/auth'

export default function RegisterPage() {
  const { loginWithToken } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (password !== confirmPassword) { setError('Passwords do not match.'); return }
    setError(null)
    setLoading(true)
    try {
      // register returns a JWT directly — no second login call needed
      const { token, email: registeredEmail } = await register({ email, password, confirmPassword })
      loginWithToken(token, registeredEmail)
      navigate('/dashboard', { replace: true })
    } catch {
      setError('Registration failed. Email may already be in use.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>Create account</h1>
      {error && <p role="alert">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        <label htmlFor="password">Password</label>
        <input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} />
        <label htmlFor="confirm">Confirm password</label>
        <input id="confirm" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
        <button type="submit" disabled={loading}>Create account</button>
      </form>
      <Link to="/login">Already have an account?</Link>
    </div>
  )
}
