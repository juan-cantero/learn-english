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
