import { useState } from 'react';
import { Link } from '@tanstack/react-router';
import { useMyClassrooms, useCreateClassroom, useUpgradeToTeacher, useJoinClassroom, useLeaveClassroom } from '../hooks/useClassrooms';
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

const LinkIcon = () => (
  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
  </svg>
);

const LogoutIcon = () => (
  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
  </svg>
);

interface ClassroomCardProps {
  classroom: Classroom;
  isTeaching: boolean;
  onLeave?: (classroomId: string) => void;
  isLeaving?: boolean;
}

function ClassroomCard({ classroom, isTeaching, onLeave, isLeaving }: ClassroomCardProps) {
  const [copied, setCopied] = useState(false);

  const copyJoinCode = (e: React.MouseEvent) => {
    e.preventDefault();
    navigator.clipboard.writeText(classroom.joinCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleLeave = (e: React.MouseEvent) => {
    e.preventDefault();
    if (onLeave && confirm(`Leave "${classroom.name}"? You can rejoin later with the code.`)) {
      onLeave(classroom.id);
    }
  };

  return (
    <Link
      to="/classrooms/$classroomId"
      params={{ classroomId: classroom.id }}
      className="block rounded-xl border border-edge-default bg-bg-card p-6 transition-all hover:border-brand/50 hover:bg-bg-card-hover"
    >
      <div className="mb-4 flex items-start justify-between gap-4">
        <div className="min-w-0 flex-1">
          <h3 className="mb-1 truncate text-xl font-semibold text-content-primary">
            {classroom.name}
          </h3>
          {classroom.description && (
            <p className="line-clamp-2 text-sm text-content-secondary">
              {classroom.description}
            </p>
          )}
        </div>
        {isTeaching ? (
          <span className="shrink-0 rounded-full bg-brand-muted px-3 py-1 text-xs font-medium text-brand">
            Teacher
          </span>
        ) : (
          <span className="shrink-0 rounded-full bg-success/20 px-3 py-1 text-xs font-medium text-success">
            Student
          </span>
        )}
      </div>

      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-content-secondary">
          <UsersIcon />
          <span className="text-sm">
            {classroom.studentCount} {classroom.studentCount === 1 ? 'student' : 'students'}
          </span>
        </div>

        {isTeaching ? (
          <button
            onClick={copyJoinCode}
            className="flex items-center gap-2 rounded-lg bg-bg-inset px-3 py-1.5 font-mono text-sm text-content-primary transition-colors hover:bg-brand-muted hover:text-brand"
          >
            <CodeIcon />
            <span>{classroom.joinCode}</span>
            {copied && (
              <span className="text-xs text-success">Copied!</span>
            )}
          </button>
        ) : onLeave && (
          <button
            onClick={handleLeave}
            disabled={isLeaving}
            className="flex items-center gap-2 rounded-lg bg-bg-inset px-3 py-1.5 text-sm text-content-secondary transition-colors hover:bg-error/10 hover:text-error disabled:opacity-50"
          >
            <LogoutIcon />
            {isLeaving ? 'Leaving...' : 'Leave'}
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
      <div className="w-full max-w-md rounded-xl border border-edge-default bg-bg-card p-6">
        <h2 className="mb-4 text-2xl font-bold text-content-primary">Create Classroom</h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="name" className="mb-2 block text-sm font-medium text-content-primary">
              Classroom Name
            </label>
            <input
              id="name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g., English 101"
              className="w-full rounded-lg border border-edge-default bg-bg-inset px-4 py-2 text-content-primary placeholder-content-secondary focus:border-brand focus:outline-none"
              required
              autoFocus
            />
          </div>

          <div>
            <label htmlFor="description" className="mb-2 block text-sm font-medium text-content-primary">
              Description (optional)
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe your classroom..."
              rows={3}
              className="w-full rounded-lg border border-edge-default bg-bg-inset px-4 py-2 text-content-primary placeholder-content-secondary focus:border-brand focus:outline-none"
            />
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={isCreating}
              className="flex-1 rounded-lg border border-edge-default bg-bg-inset px-4 py-2 text-sm font-medium text-content-primary transition-colors hover:bg-bg-card disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isCreating || !name.trim()}
              className="flex-1 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover disabled:opacity-50"
            >
              {isCreating ? 'Creating...' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

interface JoinClassroomModalProps {
  onClose: () => void;
  onJoin: (joinCode: string) => void;
  isJoining: boolean;
  error: string | null;
}

function JoinClassroomModal({ onClose, onJoin, isJoining, error }: JoinClassroomModalProps) {
  const [joinCode, setJoinCode] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (joinCode.trim()) {
      onJoin(joinCode.trim().toUpperCase());
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-md rounded-xl border border-edge-default bg-bg-card p-6">
        <h2 className="mb-4 text-2xl font-bold text-content-primary">Join Classroom</h2>
        <p className="mb-4 text-sm text-content-secondary">
          Enter the code your teacher shared with you.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="rounded-lg bg-error/10 px-4 py-3 text-sm text-error">
              {error}
            </div>
          )}

          <div>
            <label htmlFor="joinCode" className="mb-2 block text-sm font-medium text-content-primary">
              Join Code
            </label>
            <input
              id="joinCode"
              type="text"
              value={joinCode}
              onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
              placeholder="e.g., ABC123"
              className="w-full rounded-lg border border-edge-default bg-bg-inset px-4 py-3 text-center font-mono text-2xl font-bold tracking-wider text-content-primary placeholder-content-tertiary uppercase focus:border-brand focus:outline-none"
              required
              autoFocus
              maxLength={10}
            />
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={isJoining}
              className="flex-1 rounded-lg border border-edge-default bg-bg-inset px-4 py-2 text-sm font-medium text-content-primary transition-colors hover:bg-bg-card disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isJoining || !joinCode.trim()}
              className="flex-1 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover disabled:opacity-50"
            >
              {isJoining ? 'Joining...' : 'Join'}
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
  const joinMutation = useJoinClassroom();
  const leaveMutation = useLeaveClassroom();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [joinError, setJoinError] = useState<string | null>(null);

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

  const handleJoinClassroom = (joinCode: string) => {
    setJoinError(null);
    joinMutation.mutate(joinCode, {
      onSuccess: () => {
        setShowJoinModal(false);
        setJoinError(null);
      },
      onError: (err) => {
        setJoinError(err.message || 'Invalid join code. Please check and try again.');
      },
    });
  };

  const handleLeaveClassroom = (classroomId: string) => {
    leaveMutation.mutate(classroomId);
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
        <h1 className="mb-8 text-3xl font-bold text-content-primary">My Classrooms</h1>
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
          <div className="rounded-lg bg-brand-muted p-2 text-brand">
            <GraduationCapIcon />
          </div>
          <h1 className="text-3xl font-bold text-content-primary">My Classrooms</h1>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowJoinModal(true)}
            className="flex items-center gap-2 rounded-lg border border-edge-default bg-bg-card px-4 py-2 text-sm font-medium text-content-primary transition-colors hover:bg-bg-card-hover"
          >
            <LinkIcon />
            Join Classroom
          </button>

          {!isTeacher ? (
            <button
              onClick={handleUpgradeToTeacher}
              disabled={upgradeMutation.isPending}
              className="flex items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover disabled:opacity-50"
            >
              <GraduationCapIcon />
              {upgradeMutation.isPending ? 'Upgrading...' : 'Become a Teacher'}
            </button>
          ) : (
            <button
              onClick={() => setShowCreateModal(true)}
              className="flex items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover"
            >
              <PlusIcon />
              Create Classroom
            </button>
          )}
        </div>
      </div>

      {/* Teaching Section */}
      {isTeacher && (
        <div className="mb-12">
          <h2 className="mb-4 text-xl font-semibold text-content-primary">Teaching</h2>
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
                className="mt-4 inline-flex items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover"
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
          <h2 className="mb-4 text-xl font-semibold text-content-primary">Enrolled</h2>
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {classrooms.enrolled.map((classroom) => (
              <ClassroomCard
                key={classroom.id}
                classroom={classroom}
                isTeaching={false}
                onLeave={handleLeaveClassroom}
                isLeaving={leaveMutation.isPending}
              />
            ))}
          </div>
        </div>
      )}

      {/* Empty state for students with no classrooms */}
      {!isTeacher && !hasEnrolledClassrooms && (
        <EmptyState
          icon="empty"
          title="No classrooms yet"
          description="Join a classroom using the code from your teacher."
        >
          <button
            onClick={() => setShowJoinModal(true)}
            className="mt-4 inline-flex items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover"
          >
            <LinkIcon />
            Join Classroom
          </button>
        </EmptyState>
      )}

      {/* Modals */}
      {showCreateModal && (
        <CreateClassroomModal
          onClose={() => setShowCreateModal(false)}
          onCreate={handleCreateClassroom}
          isCreating={createMutation.isPending}
        />
      )}

      {showJoinModal && (
        <JoinClassroomModal
          onClose={() => { setShowJoinModal(false); setJoinError(null); }}
          onJoin={handleJoinClassroom}
          isJoining={joinMutation.isPending}
          error={joinError}
        />
      )}
    </div>
  );
}
