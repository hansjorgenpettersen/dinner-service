import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClientProvider, QueryClient } from '@tanstack/react-query'
import { AuthProvider } from '../context/AuthContext'
import LoginPage from '../pages/LoginPage'
import { vi } from 'vitest'

vi.mock('../api/auth', () => ({
  login: vi.fn().mockResolvedValue({ token: 'tok', email: 'user@test.com' }),
  logout: vi.fn()
}))

function renderLogin() {
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={<div>Dashboard</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

test('renders email and password fields', () => {
  renderLogin()
  expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
  expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
})

test('submitting valid credentials navigates to dashboard', async () => {
  renderLogin()
  fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'user@test.com' } })
  fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } })
  fireEvent.click(screen.getByRole('button', { name: /sign in/i }))
  await waitFor(() => {
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
  })
})
