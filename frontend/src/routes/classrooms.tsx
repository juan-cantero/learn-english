import { useState } from 'react';
import { Link } from '@tanstack/react-router';
import { useMyClassrooms, useCreateClassroom, useUpgradeToTeacher } from '../hooks/useClassrooms';
import { useCurrentUser } from '../hooks/useCurrentUser';
import { EmptyState } from '../components/shared/EmptyState';
import type { Classroom } from '../types/classroom';

const GraduationCapIcon = () => (
  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 14l9-5-9-5-9 5 9 5z" />
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z" />
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 14l9-5-9-5-9 5 9 5zm0 0l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14zm-4 6v-7.5l4-2.222" />
  </svg>
);

const UsersIcon = () => (
  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
  </svg>
);

const PlusIcon = () => (
  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
  </svg>
);

const CodeIcon = () => (
  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
  </svg>
);

interface ClassroomCardProps {
  classroom: Classroom;
  isTeaching: boolean;
}

function ClassroomCard({ classroom, isTeaching }: ClassroomCardProps) {
  const [copied, setCopied] = useState(false);

  const copyJoinCode = (e: React.MouseEvent) => {
    e.preventDefault();
    navigator.clipboard.writeText(classroom.joinCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <Link
      to="/classrooms/$classroomId"
      params={{ classroomId: classroom.id }}
      className="block rounded-xl border border-border bg-bg-card p-6 transition-all hover:border-accent-primary/50 hover:bg-bg-card-hover"
    >
      <div className="mb-4 flex items-start justify-between gap-4">
        <div className="min-w-0 flex-1">
          <h3 className="mb-1 truncate text-xl font-semibold text-text-primary">
            {classroom.name}
          </h3>
          {classroom.description && (
            <p className="line-clamp-2 text-sm text-text-secondary">
              {classroom.description}
            </p>
          )}
        </div>
        {isTeaching && (
          <span className="shrink-0 rounded-full bg-accent-primary/20 px-3 py-1 text-xs font-medium text-accent-primary">
            Teacher
          </span>
        )}
      </div>

      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-text-secondary">
          <UsersIcon />
          <span className="text-sm">
            {classroom.studentCount} {classroom.studentCount === 1 ? 'student' : 'students'}
          </span>
        </div>

        {isTeaching && (
          <button
            onClick={copyJoinCode}
            className="flex items-center gap-2 rounded-lg bg-bg-dark px-3 py-1.5 font-mono text-sm text-text-primary transition-colors hover:bg-accent-primary/10 hover:text-accent-primary"
          >
            <CodeIcon />
            <span>{classroom.joinCode}</span>
            {copied && (
              <span className="text-xs text-success">Copied!</span>
            )}
          </button>
        )}
      </div>
    </Link>
  );
}

interface CreateClassroomModalProps {
  onClose: () => void;
  onCreate: (name: string, description: string) => void;
  isCreating: boolean;
}

function CreateClassroomModal({ onClose, onCreate, isCreating }: CreateClassroomModalProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim()) {
      onCreate(name.trim(), description.trim() || '');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-md rounded-xl border border-border bg-bg-card p-6">
        <h2 className="mb-4 text-2xl font-bold text-text-primary">Create Classroom</h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="name" className="mb-2 block text-sm font-medium text-text-primary">
              Classroom Name
            </label>
            <input
              id="name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g., English 101"
              className="w-full rounded-lg border border-border bg-bg-dark px-4 py-2 text-text-primary placeholder-text-secondary focus:border-accent-primary focus:outline-none"
              required
              autoFocus
            />
          </div>

          <div>
            <label htmlFor="description" className="mb-2 block text-sm font-medium text-text-primary">
              Description (optional)
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe your classroom..."
              rows={3}
              className="w-full rounded-lg border border-border bg-bg-dark px-4 py-2 text-text-primary placeholder-text-secondary focus:border-accent-primary focus:outline-none"
            />
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={isCreating}
              className="flex-1 rounded-lg border border-border bg-bg-dark px-4 py-2 text-sm font-medium text-text-primary transition-colors hover:bg-bg-card disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isCreating || !name.trim()}
              className="flex-1 rounded-lg bg-accent-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-accent-secondary disabled:opacity-50"
            >
              {isCreating ? 'Creating...' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export function ClassroomsPage() {
  const { data: currentUser } = useCurrentUser();
  const { data: classrooms, isLoading, error, refetch } = useMyClassrooms();
  const createMutation = useCreateClassroom();
  const upgradeMutation = useUpgradeToTeacher();
  const [showCreateModal, setShowCreateModal] = useState(false);

  const handleUpgradeToTeacher = () => {
    upgradeMutation.mutate();
  };

  const handleCreateClassroom = (name: string, description: string) => {
    createMutation.mutate(
      { name, description: description || null },
      {
        onSuccess: () => {
          setShowCreateModal(false);
        },
      }
    );
  };

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse">
          <div className="mb-8 h-8 w-48 rounded bg-bg-card" />
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-40 rounded-xl bg-bg-card" />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <h1 className="mb-8 text-3xl font-bold text-text-primary">My Classrooms</h1>
        <EmptyState
          icon="connection"
          title="Failed to load classrooms"
          description={error.message || "We couldn't connect to the server. Please try again."}
          action={{
            label: 'Try Again',
            onClick: () => refetch(),
          }}
        />
      </div>
    );
  }

  if (!classrooms) return null;

  const isTeacher = currentUser?.role === 'TEACHER';
  const hasTeachingClassrooms = classrooms.teaching.length > 0;
  const hasEnrolledClassrooms = classrooms.enrolled.length > 0;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="rounded-lg bg-accent-primary/20 p-2 text-accent-primary">
            <GraduationCapIcon />
          </div>
          <h1 className="text-3xl font-bold text-text-primary">My Classrooms</h1>
        </div>

        {!isTeacher ? (
          <button
            onClick={handleUpgradeToTeacher}
            disabled={upgradeMutation.isPending}
            className="flex items-center gap-2 rounded-lg bg-accent-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-accent-secondary disabled:opacity-50"
          >
            <GraduationCapIcon />
            {upgradeMutation.isPending ? 'Upgrading...' : 'Become a Teacher'}
          </button>
        ) : (
          <button
            onClick={() => setShowCreateModal(true)}
            className="flex items-center gap-2 rounded-lg bg-accent-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-accent-secondary"
          >
            <PlusIcon />
            Create Classroom
          </button>
        )}
      </div>

      {/* Teaching Section */}
      {isTeacher && (
        <div className="mb-12">
          <h2 className="mb-4 text-xl font-semibold text-text-primary">Teaching</h2>
          {hasTeachingClassrooms ? (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {classrooms.teaching.map((classroom) => (
                <ClassroomCard
                  key={classroom.id}
                  classroom={classroom}
                  isTeaching={true}
                />
              ))}
            </div>
          ) : (
            <EmptyState
              icon="empty"
              title="No classrooms yet"
              description="Create your first classroom to start teaching."
            >
              <button
                onClick={() => setShowCreateModal(true)}
                className="mt-4 inline-flex items-center gap-2 rounded-lg bg-accent-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-accent-secondary"
              >
                <PlusIcon />
                Create Classroom
              </button>
            </EmptyState>
          )}
        </div>
      )}

      {/* Enrolled Section */}
      {hasEnrolledClassrooms && (
        <div>
          <h2 className="mb-4 text-xl font-semibold text-text-primary">Enrolled</h2>
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {classrooms.enrolled.map((classroom) => (
              <ClassroomCard
                key={classroom.id}
                classroom={classroom}
                isTeaching={false}
              />
            ))}
          </div>
        </div>
      )}

      {/* Show modal */}
      {showCreateModal && (
        <CreateClassroomModal
          onClose={() => setShowCreateModal(false)}
          onCreate={handleCreateClassroom}
          isCreating={createMutation.isPending}
        />
      )}
    </div>
  );
}
