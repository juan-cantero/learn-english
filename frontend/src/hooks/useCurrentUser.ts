import { useQuery } from '@tanstack/react-query';
import { apiGet } from '../api/client';
import { useAuth } from '../context/AuthContext';

export interface CurrentUser {
  id: string;
  email: string;
  displayName: string | null;
  avatarUrl: string | null;
  role: 'LEARNER' | 'TEACHER';
  preferredDifficulty: string | null;
  createdAt: string;
}

export function useCurrentUser() {
  const { user, loading: authLoading } = useAuth();

  return useQuery({
    queryKey: ['currentUser'],
    queryFn: () => apiGet<CurrentUser>('/me'),
    enabled: !!user && !authLoading,
    staleTime: 1000 * 60 * 5, // 5 minutes
    retry: 1,
  });
}
