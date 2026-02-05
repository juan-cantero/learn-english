-- V13__classroom_context.sql
-- Classroom bounded context

-- Classrooms table (teacher-created)
CREATE TABLE classrooms (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    join_code VARCHAR(10) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_classrooms_teacher ON classrooms(teacher_id);
CREATE INDEX idx_classrooms_join_code ON classrooms(join_code);

-- Classroom students (many-to-many relationship)
CREATE TABLE classroom_students (
    id UUID PRIMARY KEY,
    classroom_id UUID NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id),
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_classroom_student UNIQUE(classroom_id, student_id)
);

CREATE INDEX idx_classroom_students_student ON classroom_students(student_id);
CREATE INDEX idx_classroom_students_classroom ON classroom_students(classroom_id);
