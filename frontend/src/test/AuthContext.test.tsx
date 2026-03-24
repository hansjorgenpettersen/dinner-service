import { render, screen, act } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider, useAuth } from '../context/AuthContext'
import { vi } from 'vitest'

// Stub the api module
vi.mock('../api/auth', () => ({
  login: vi.fn().mockResolvedValue({ token: 'fake.jwt.token', email: 'user@test.com' }),
  logout: vi.fn().mockResolvedValue(undefined)
}))

function TestConsumer() {
  const { currentEmail, isAuthenticated, login, logout } = useAuth()
  return (
    <div>
      <span data-testid="email">{currentEmail ?? 'none'}</span>
      <span data-testid="auth">{String(isAuthenticated)}</span>
      <button onClick={() => login({ email: 'user@test.com', password: 'pw' })}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  )
}

function renderWithProviders() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    </MemoryRouter>
  )
}

test('initially not authenticated when no token in localStorage', () => {
  localStorage.clear()
  renderWithProviders()
  expect(screen.getByTestId('auth').textContent).toBe('false')
  expect(screen.getByTestId('email').textContent).toBe('none')
})

test('login sets currentEmail and stores token', async () => {
  localStorage.clear()
  renderWithProviders()
  await act(async () => {
    screen.getByRole('button', { name: 'Login' }).click()
  })
  expect(localStorage.getItem('jwt')).toBe('fake.jwt.token')
  expect(screen.getByTestId('email').textContent).toBe('user@test.com')
  expect(screen.getByTestId('auth').textContent).toBe('true')
})

test('logout clears token and email', async () => {
  // A valid-looking JWT with sub: "user@test.com" so decodeEmail returns a real value
  const payload = btoa(JSON.stringify({ sub: 'user@test.com', exp: 9999999999 }))
  const mockJwt = `header.${payload}.signature`
  localStorage.setItem('jwt', mockJwt)
  renderWithProviders()
  // Confirm we start authenticated
  expect(screen.getByTestId('auth').textContent).toBe('true')
  await act(async () => {
    screen.getByRole('button', { name: 'Logout' }).click()
  })
  expect(localStorage.getItem('jwt')).toBeNull()
  expect(screen.getByTestId('auth').textContent).toBe('false')
})
