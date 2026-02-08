import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiGet, apiPost, apiPut, apiDelete } from '../api/client';
import type {
  AssignmentResponse,
  AssignmentWithStatsResponse,
  StudentAssignmentResponse,
  SubmissionResponse,
  SubmissionWithStudentResponse,
  CreateAssignmentRequest,
  UpdateAssignmentRequest,
} from '../types/classroom';

export function useClassroomAssignments(classroomId: string | undefined) {
  return useQuery<AssignmentWithStatsResponse[], Error>({
    queryKey: ['classrooms', classroomId, 'assignments'],
    queryFn: () => apiGet<AssignmentWithStatsResponse[]>(`/classrooms/${classroomId}/assignments`),
    enabled: !!classroomId,
    staleTime: 1000 * 30,
  });
}

export function useMyAssignments() {
  return useQuery<StudentAssignmentResponse[], Error>({
    queryKey: ['assignments', 'my'],
    queryFn: () => apiGet<StudentAssignmentResponse[]>('/me/assignments'),
    staleTime: 1000 * 30,
  });
}

export function useAssignmentSubmissions(assignmentId: string | undefined) {
  return useQuery<SubmissionWithStudentResponse[], Error>({
    queryKey: ['assignments', assignmentId, 'submissions'],
    queryFn: () => apiGet<SubmissionWithStudentResponse[]>(`/assignments/${assignmentId}/submissions`),
    enabled: !!assignmentId,
    staleTime: 1000 * 30,
  });
}

export function useCreateAssignment(classroomId: string) {
  const queryClient = useQueryClient();

  return useMutation<AssignmentResponse, Error, CreateAssignmentRequest>({
    mutationFn: (data) => apiPost<CreateAssignmentRequest, AssignmentResponse>(`/classrooms/${classroomId}/assignments`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', classroomId, 'assignments'] });
    },
  });
}

export function useUpdateAssignment(assignmentId: string, classroomId: string) {
  const queryClient = useQueryClient();

  return useMutation<AssignmentResponse, Error, UpdateAssignmentRequest>({
    mutationFn: (data) => apiPut<UpdateAssignmentRequest, AssignmentResponse>(`/assignments/${assignmentId}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', classroomId, 'assignments'] });
    },
  });
}

export function useDeleteAssignment(classroomId: string) {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (assignmentId) => apiDelete<void>(`/assignments/${assignmentId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classrooms', classroomId, 'assignments'] });
    },
  });
}

export function useStartAssignment() {
  const queryClient = useQueryClient();

  return useMutation<SubmissionResponse, Error, string>({
    mutationFn: (assignmentId) => apiPost<Record<string, never>, SubmissionResponse>(`/assignments/${assignmentId}/start`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['assignments', 'my'] });
    },
  });
}

export function useCompleteAssignment() {
  const queryClient = useQueryClient();

  return useMutation<SubmissionResponse, Error, string>({
    mutationFn: (assignmentId) => apiPost<Record<string, never>, SubmissionResponse>(`/assignments/${assignmentId}/complete`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['assignments', 'my'] });
    },
  });
}
