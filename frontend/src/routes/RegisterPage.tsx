import { useState } from 'react'
import { useNavigate, Link } from '@tanstack/react-router'
import { useAuth } from '../context/AuthContext'

export function RegisterPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)
  const { signUp } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    setLoading(true)

    const { error } = await signUp(email, password)

    if (error) {
      setError(error.message)
      setLoading(false)
    } else {
      setSuccess(true)
      setTimeout(() => navigate({ to: '/login' }), 2000)
    }
  }

  if (success) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-bg-primary p-4">
        <div className="w-full max-w-md rounded-xl border border-edge-default bg-bg-card p-8 shadow-xl">
          <div className="mb-8 text-center">
            <h1 className="text-2xl font-semibold text-content-primary">Check Your Email</h1>
            <p className="mt-2 text-content-secondary">We've sent you a confirmation link. Please check your email to complete registration.</p>
            <p className="mt-4 text-sm text-content-tertiary">
              For local development, check <a href="http://127.0.0.1:54324" target="_blank" rel="noreferrer" className="text-brand hover:text-brand-light">Mailpit</a> for the confirmation email.
            </p>
          </div>
          <div className="text-center text-sm text-content-secondary">
            <p>
              <Link to="/login" className="font-medium text-brand hover:text-brand-light">Back to login</Link>
            </p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg-primary p-4">
      <div className="w-full max-w-md rounded-xl border border-edge-default bg-bg-card p-8 shadow-xl">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-semibold text-content-primary">Create Account</h1>
          <p className="mt-2 text-content-secondary">Start your English learning journey</p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          {error && (
            <div className="rounded-lg border border-error/30 bg-error-muted px-4 py-3 text-sm text-error">
              {error}
            </div>
          )}

          <div className="flex flex-col gap-2">
            <label htmlFor="email" className="text-sm font-medium text-content-secondary">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              required
              disabled={loading}
              className="rounded-lg border border-edge-default bg-bg-elevated px-4 py-3 text-content-primary placeholder:text-content-tertiary transition-colors focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/50 focus:ring-offset-2 focus:ring-offset-bg-primary disabled:opacity-60 disabled:cursor-not-allowed"
            />
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="password" className="text-sm font-medium text-content-secondary">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="At least 6 characters"
              required
              disabled={loading}
              className="rounded-lg border border-edge-default bg-bg-elevated px-4 py-3 text-content-primary placeholder:text-content-tertiary transition-colors focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/50 focus:ring-offset-2 focus:ring-offset-bg-primary disabled:opacity-60 disabled:cursor-not-allowed"
            />
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="confirmPassword" className="text-sm font-medium text-content-secondary">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Confirm your password"
              required
              disabled={loading}
              className="rounded-lg border border-edge-default bg-bg-elevated px-4 py-3 text-content-primary placeholder:text-content-tertiary transition-colors focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/50 focus:ring-offset-2 focus:ring-offset-bg-primary disabled:opacity-60 disabled:cursor-not-allowed"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="mt-2 rounded-lg bg-brand px-6 py-3 text-base font-semibold text-white transition-colors hover:bg-brand-hover active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? 'Creating account...' : 'Sign Up'}
          </button>
        </form>

        <div className="mt-6 text-center text-sm text-content-secondary">
          <p>
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-brand hover:text-brand-light">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
