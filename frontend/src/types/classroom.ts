export interface Classroom {
  id: string;
  teacherId: string;
  name: string;
  description: string | null;
  joinCode: string;
  active: boolean;
  studentCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Student {
  id: string;
  email: string;
  displayName: string | null;
  joinedAt: string;
}

export interface MyClassroomsResponse {
  teaching: Classroom[];
  enrolled: Classroom[];
}

export interface CreateClassroomRequest {
  name: string;
  description: string | null;
}

export interface UpdateClassroomRequest {
  name: string;
  description: string | null;
}

// --- Assignments ---

export type SubmissionStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

export interface AssignmentResponse {
  id: string;
  classroomId: string;
  episodeId: string;
  title: string;
  instructions: string;
  dueDate: string | null;
  overdue: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AssignmentWithStatsResponse {
  id: string;
  classroomId: string;
  episodeId: string;
  title: string;
  instructions: string;
  dueDate: string | null;
  overdue: boolean;
  totalSubmissions: number;
  completedSubmissions: number;
  createdAt: string;
}

export interface StudentAssignmentResponse {
  id: string;
  classroomId: string;
  episodeId: string;
  title: string;
  instructions: string;
  dueDate: string | null;
  status: SubmissionStatus;
  score: number | null;
  overdue: boolean;
  createdAt: string;
}

export interface SubmissionResponse {
  id: string;
  assignmentId: string;
  studentId: string;
  status: SubmissionStatus;
  startedAt: string | null;
  completedAt: string | null;
  score: number | null;
  timeSpentMinutes: number | null;
}

export interface SubmissionWithStudentResponse {
  id: string;
  assignmentId: string;
  studentId: string;
  studentName: string;
  studentEmail: string;
  status: SubmissionStatus;
  startedAt: string | null;
  completedAt: string | null;
  score: number | null;
  timeSpentMinutes: number | null;
}

export interface CreateAssignmentRequest {
  episodeId: string;
  title: string;
  instructions: string;
  dueDate: string | null;
}

export interface UpdateAssignmentRequest {
  title: string | null;
  instructions: string | null;
  dueDate: string | null;
}
