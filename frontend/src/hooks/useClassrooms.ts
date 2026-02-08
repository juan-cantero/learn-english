import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiGet, apiPost, apiPut, apiDelete } from '../api/client';
import type {
  Classroom,
  Student,
  MyClassroomsResponse,
  CreateClassroomRequest,
  UpdateClassroomRequest,
} from '../types/classroom';
import type { CurrentUser } from './useCurrentUser';

export function useMyClassrooms() {
  return useQuery<MyClassroomsResponse, Error>({
    queryKey: ['classrooms', 'my'],
    queryFn: () => apiGet<MyClassroomsResponse>('/classrooms'),
    staleTime: 1000 * 60, // 1 minute
  });
}

export function useClassroom(id: string | undefined) {
  return useQuery<Classroom, Error>({
    queryKey: ['classrooms', id],
    queryFn: () => apiGet<Classroom>(`/classrooms/${id}`),
    enabled: !!id,
    staleTime: 1000 * 30, // 30 seconds
  });
}

export function useClassroomStudents(id: string | undefined) {
  return useQuery<Student[], Error>({
    queryKey: ['classrooms', id, 'students'],
    queryFn: () => apiGet<Student[]>(`/classrooms/${id}/students`),
    enabled: !!id,
    staleTime: 1000 * 30, // 30 seconds
  });
}

export function useCreateClassroom() {
  const queryClient = useQueryClient();

  return useMutation<Classroom, Error, CreateClassroomRequest>({
    mutationFn: (data) => apiPost<CreateClassroomRequest, Classroom>('/classrooms', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', 'my'] });
    },
  });
}

export function useUpdateClassroom(id: string) {
  const queryClient = useQueryClient();

  return useMutation<Classroom, Error, UpdateClassroomRequest>({
    mutationFn: (data) => apiPut<UpdateClassroomRequest, Classroom>(`/classrooms/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', 'my'] });
      queryClient.invalidateQueries({ queryKey: ['classrooms', id] });
    },
  });
}

export function useDeleteClassroom() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (id) => apiDelete<void>(`/classrooms/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', 'my'] });
    },
  });
}

export function useRegenerateJoinCode(id: string) {
  const queryClient = useQueryClient();

  return useMutation<Classroom, Error, void>({
    mutationFn: () => apiPost<Record<string, never>, Classroom>(`/classrooms/${id}/regenerate-code`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', 'my'] });
      queryClient.invalidateQueries({ queryKey: ['classrooms', id] });
    },
  });
}

export function useRemoveStudent(classroomId: string) {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (studentId) => apiDelete<void>(`/classrooms/${classroomId}/students/${studentId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', classroomId, 'students'] });
      queryClient.invalidateQueries({ queryKey: ['classrooms', classroomId] });
    },
  });
}

export function useJoinClassroom() {
  const queryClient = useQueryClient();

  return useMutation<Classroom, Error, string>({
    mutationFn: (joinCode) => apiPost<{ joinCode: string }, Classroom>('/classrooms/join', { joinCode }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', 'my'] });
    },
  });
}

export function useLeaveClassroom() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (classroomId) => apiDelete<void>(`/classrooms/${classroomId}/leave`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', 'my'] });
    },
  });
}

export function useUpgradeToTeacher() {
  const queryClient = useQueryClient();

  return useMutation<CurrentUser, Error, void>({
    mutationFn: () => apiPost<Record<string, never>, CurrentUser>('/me/upgrade-to-teacher', {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['currentUser'] });
    },
  });
}
