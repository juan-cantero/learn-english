-- V19__enable_rls_policies.sql
-- Enable Row Level Security on all tables to restrict direct PostgREST access.
-- The Spring Boot backend connects as postgres (superuser) and bypasses RLS automatically.

-- ============================================================================
-- 0. PREREQUISITES — stub auth.uid() and roles for local dev (Supabase provides these)
-- ============================================================================

-- Create auth schema if it doesn't exist (Supabase has this natively)
CREATE SCHEMA IF NOT EXISTS auth;

-- Stub auth.uid() — returns NULL in local dev (policies won't match, but learntv
-- role bypasses RLS anyway). On Supabase this function already exists.
CREATE OR REPLACE FUNCTION auth.uid() RETURNS UUID AS $$
    SELECT NULLIF(current_setting('request.jwt.claims', true)::json->>'sub', '')::uuid;
$$ LANGUAGE sql STABLE;

-- Create authenticated role if it doesn't exist (Supabase has this natively)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'authenticated') THEN
        CREATE ROLE authenticated NOLOGIN;
    END IF;
END
$$;

-- ============================================================================
-- 1. ENABLE RLS ON ALL TABLES
-- ============================================================================
ALTER TABLE shows ENABLE ROW LEVEL SECURITY;
ALTER TABLE episodes ENABLE ROW LEVEL SECURITY;
ALTER TABLE vocabulary ENABLE ROW LEVEL SECURITY;
ALTER TABLE grammar_points ENABLE ROW LEVEL SECURITY;
ALTER TABLE expressions ENABLE ROW LEVEL SECURITY;
ALTER TABLE exercises ENABLE ROW LEVEL SECURITY;
ALTER TABLE episode_scripts ENABLE ROW LEVEL SECURITY;
ALTER TABLE shadowing_scenes ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_stats ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_episode_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_shows ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE classrooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE classroom_students ENABLE ROW LEVEL SECURITY;
ALTER TABLE assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE assignment_submissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE generation_jobs ENABLE ROW LEVEL SECURITY;
ALTER TABLE flyway_schema_history ENABLE ROW LEVEL SECURITY;

-- ============================================================================
-- 2. SYSTEM/INTERNAL TABLES — no policies = default deny for non-superusers
-- ============================================================================
-- flyway_schema_history: no policies needed
-- generation_jobs: no policies needed

-- ============================================================================
-- 3. SHARED CONTENT TABLES — authenticated read-only
-- ============================================================================
CREATE POLICY "Authenticated users can read shows"
    ON shows FOR SELECT TO authenticated USING (true);

CREATE POLICY "Authenticated users can read episodes"
    ON episodes FOR SELECT TO authenticated USING (true);

CREATE POLICY "Authenticated users can read vocabulary"
    ON vocabulary FOR SELECT TO authenticated USING (true);

CREATE POLICY "Authenticated users can read grammar_points"
    ON grammar_points FOR SELECT TO authenticated USING (true);

CREATE POLICY "Authenticated users can read expressions"
    ON expressions FOR SELECT TO authenticated USING (true);

CREATE POLICY "Authenticated users can read exercises"
    ON exercises FOR SELECT TO authenticated USING (true);

CREATE POLICY "Authenticated users can read episode_scripts"
    ON episode_scripts FOR SELECT TO authenticated USING (true);

CREATE POLICY "Authenticated users can read shadowing_scenes"
    ON shadowing_scenes FOR SELECT TO authenticated USING (true);

-- ============================================================================
-- 4. USER-OWNED TABLES — scoped to own rows via auth.uid()
-- ============================================================================

-- users: read and update own row only
CREATE POLICY "Users can read own profile"
    ON users FOR SELECT TO authenticated USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE TO authenticated USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

-- user_stats: read, insert, update own row
CREATE POLICY "Users can read own stats"
    ON user_stats FOR SELECT TO authenticated USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own stats"
    ON user_stats FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own stats"
    ON user_stats FOR UPDATE TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- user_episode_progress: full CRUD own rows
CREATE POLICY "Users can read own episode progress"
    ON user_episode_progress FOR SELECT TO authenticated USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own episode progress"
    ON user_episode_progress FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own episode progress"
    ON user_episode_progress FOR UPDATE TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own episode progress"
    ON user_episode_progress FOR DELETE TO authenticated USING (auth.uid() = user_id);

-- user_shows: full CRUD own rows
CREATE POLICY "Users can read own shows"
    ON user_shows FOR SELECT TO authenticated USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own shows"
    ON user_shows FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own shows"
    ON user_shows FOR DELETE TO authenticated USING (auth.uid() = user_id);

-- user_progress (legacy): read own rows only — user_id is VARCHAR so cast auth.uid()
CREATE POLICY "Users can read own legacy progress"
    ON user_progress FOR SELECT TO authenticated USING (auth.uid()::text = user_id);

-- ============================================================================
-- 5. CLASSROOM TABLES — teacher/student scoped
-- ============================================================================

-- classrooms: teachers CRUD own, students read classrooms they belong to
CREATE POLICY "Teachers can manage own classrooms"
    ON classrooms FOR ALL TO authenticated
    USING (auth.uid() = teacher_id)
    WITH CHECK (auth.uid() = teacher_id);

CREATE POLICY "Students can read their classrooms"
    ON classrooms FOR SELECT TO authenticated
    USING (
        id IN (
            SELECT classroom_id FROM classroom_students
            WHERE student_id = auth.uid() AND is_active = true
        )
    );

-- classroom_students: students manage own membership, teachers see their classroom members
CREATE POLICY "Students can read own memberships"
    ON classroom_students FOR SELECT TO authenticated
    USING (student_id = auth.uid());

CREATE POLICY "Students can insert own membership"
    ON classroom_students FOR INSERT TO authenticated
    WITH CHECK (student_id = auth.uid());

CREATE POLICY "Students can update own membership"
    ON classroom_students FOR UPDATE TO authenticated
    USING (student_id = auth.uid()) WITH CHECK (student_id = auth.uid());

CREATE POLICY "Teachers can read their classroom members"
    ON classroom_students FOR SELECT TO authenticated
    USING (
        classroom_id IN (
            SELECT id FROM classrooms WHERE teacher_id = auth.uid()
        )
    );

-- assignments: teachers CRUD for their classrooms, students read their classroom assignments
CREATE POLICY "Teachers can manage assignments"
    ON assignments FOR ALL TO authenticated
    USING (
        classroom_id IN (
            SELECT id FROM classrooms WHERE teacher_id = auth.uid()
        )
    )
    WITH CHECK (
        classroom_id IN (
            SELECT id FROM classrooms WHERE teacher_id = auth.uid()
        )
    );

CREATE POLICY "Students can read their assignments"
    ON assignments FOR SELECT TO authenticated
    USING (
        classroom_id IN (
            SELECT classroom_id FROM classroom_students
            WHERE student_id = auth.uid() AND is_active = true
        )
    );

-- assignment_submissions: students CRUD own, teachers read for their classrooms
CREATE POLICY "Students can manage own submissions"
    ON assignment_submissions FOR ALL TO authenticated
    USING (student_id = auth.uid())
    WITH CHECK (student_id = auth.uid());

CREATE POLICY "Teachers can read submissions for their classrooms"
    ON assignment_submissions FOR SELECT TO authenticated
    USING (
        assignment_id IN (
            SELECT a.id FROM assignments a
            JOIN classrooms c ON c.id = a.classroom_id
            WHERE c.teacher_id = auth.uid()
        )
    );

-- ============================================================================
-- 6. LOCAL DEV ROLE — bypass RLS so JDBC connection is unaffected
-- ============================================================================
ALTER ROLE learntv BYPASSRLS;
