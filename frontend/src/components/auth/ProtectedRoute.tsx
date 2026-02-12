import { useNavigate, useLocation } from '@tanstack/react-router'
import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useAuth } from '../../context/AuthContext'

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    if (!loading && !user) {
      navigate({
        to: '/login',
        search: { redirect: location.pathname },
      })
    }
  }, [user, loading, navigate, location.pathname])

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-content-secondary">Loading...</div>
      </div>
    )
  }

  if (!user) {
    return null
  }

  return <>{children}</>
}
