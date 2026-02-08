import { useState } from 'react';
import { useParams, useNavigate } from '@tanstack/react-router';
import {
  useClassroom,
  useClassroomStudents,
  useUpdateClassroom,
  useDeleteClassroom,
  useRegenerateJoinCode,
  useRemoveStudent,
  useLeaveClassroom,
} from '../hooks/useClassrooms';
import { useCurrentUser } from '../hooks/useCurrentUser';
import { EmptyState } from '../components/shared/EmptyState';

const BackIcon = () => (
  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
  </svg>
);

const EditIcon = () => (
  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
  </svg>
);

const TrashIcon = () => (
  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
  </svg>
);

const RefreshIcon = () => (
  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
  </svg>
);

const CopyIcon = () => (
  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
  </svg>
);

const UserIcon = () => (
  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
  </svg>
);

interface EditModalProps {
  classroomId: string;
  initialName: string;
  initialDescription: string | null;
  onClose: () => void;
}

function EditModal({ classroomId, initialName, initialDescription, onClose }: EditModalProps) {
  const [name, setName] = useState(initialName);
  const [description, setDescription] = useState(initialDescription || '');
  const updateMutation = useUpdateClassroom(classroomId);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim()) {
      updateMutation.mutate(
        { name: name.trim(), description: description.trim() || null },
        {
          onSuccess: () => {
            onClose();
          },
        }
      );
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-md rounded-xl border border-border bg-bg-card p-6">
        <h2 className="mb-4 text-2xl font-bold text-text-primary">Edit Classroom</h2>

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
              className="w-full rounded-lg border border-border bg-bg-dark px-4 py-2 text-text-primary focus:border-accent-primary focus:outline-none"
              required
              autoFocus
            />
          </div>

          <div>
            <label htmlFor="description" className="mb-2 block text-sm font-medium text-text-primary">
              Description
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              className="w-full rounded-lg border border-border bg-bg-dark px-4 py-2 text-text-primary focus:border-accent-primary focus:outline-none"
            />
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={updateMutation.isPending}
              className="flex-1 rounded-lg border border-border bg-bg-dark px-4 py-2 text-sm font-medium text-text-primary transition-colors hover:bg-bg-card disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={updateMutation.isPending || !name.trim()}
              className="flex-1 rounded-lg bg-accent-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-accent-secondary disabled:opacity-50"
            >
              {updateMutation.isPending ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

interface ConfirmDeleteModalProps {
  classroomName: string;
  onConfirm: () => void;
  onCancel: () => void;
  isDeleting: boolean;
}

function ConfirmDeleteModal({ classroomName, onConfirm, onCancel, isDeleting }: ConfirmDeleteModalProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-md rounded-xl border border-border bg-bg-card p-6">
        <h2 className="mb-4 text-2xl font-bold text-text-primary">Delete Classroom</h2>
        <p className="mb-6 text-text-secondary">
          Are you sure you want to delete <span className="font-semibold text-text-primary">{classroomName}</span>?
          This action cannot be undone and all students will be removed.
        </p>
        <div className="flex gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isDeleting}
            className="flex-1 rounded-lg border border-border bg-bg-dark px-4 py-2 text-sm font-medium text-text-primary transition-colors hover:bg-bg-card disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isDeleting}
            className="flex-1 rounded-lg bg-error px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-error/80 disabled:opacity-50"
          >
            {isDeleting ? 'Deleting...' : 'Delete'}
          </button>
        </div>
      </div>
    </div>
  );
}

export function ClassroomDetailPage() {
  const { classroomId } = useParams({ from: '/classrooms/$classroomId' });
  const navigate = useNavigate();
  const { data: currentUser } = useCurrentUser();
  const { data: classroom, isLoading, error } = useClassroom(classroomId);
  const { data: students, isLoading: studentsLoading } = useClassroomStudents(classroomId);
  const regenerateMutation = useRegenerateJoinCode(classroomId);
  const deleteMutation = useDeleteClassroom();
  const removeMutation = useRemoveStudent(classroomId);
  const leaveMutation = useLeaveClassroom();

  const [copied, setCopied] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const isTeacher = currentUser?.id === classroom?.teacherId;

  const copyJoinCode = () => {
    if (classroom) {
      navigator.clipboard.writeText(classroom.joinCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleRegenerateCode = () => {
    if (confirm('Are you sure you want to regenerate the join code? The old code will no longer work.')) {
      regenerateMutation.mutate();
    }
  };

  const handleDeleteClassroom = () => {
    deleteMutation.mutate(classroomId, {
      onSuccess: () => {
        navigate({ to: '/classrooms' });
      },
    });
  };

  const handleRemoveStudent = (studentId: string, studentName: string) => {
    if (confirm(`Remove ${studentName} from this classroom?`)) {
      removeMutation.mutate(studentId);
    }
  };

  const handleLeaveClassroom = () => {
    if (confirm(`Leave "${classroom?.name}"? You can rejoin later with the code.`)) {
      leaveMutation.mutate(classroomId, {
        onSuccess: () => {
          navigate({ to: '/classrooms' });
        },
      });
    }
  };

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse space-y-6">
          <div className="h-8 w-64 rounded bg-bg-card" />
          <div className="h-40 rounded-xl bg-bg-card" />
          <div className="h-64 rounded-xl bg-bg-card" />
        </div>
      </div>
    );
  }

  if (error || !classroom) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <EmptyState
          icon="connection"
          title="Classroom not found"
          description="This classroom doesn't exist or you don't have access to it."
          action={{
            label: 'Back to Classrooms',
            onClick: () => navigate({ to: '/classrooms' }),
          }}
        />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <button
        onClick={() => navigate({ to: '/classrooms' })}
        className="mb-6 flex items-center gap-2 text-text-secondary transition-colors hover:text-text-primary"
      >
        <BackIcon />
        <span className="text-sm">Back to Classrooms</span>
      </button>

      {/* Classroom Header */}
      <div className="mb-8 rounded-xl border border-border bg-bg-card p-6">
        <div className="mb-6 flex items-start justify-between gap-4">
          <div className="min-w-0 flex-1">
            <h1 className="mb-2 text-3xl font-bold text-text-primary">{classroom.name}</h1>
            {classroom.description && (
              <p className="text-text-secondary">{classroom.description}</p>
            )}
          </div>

          {isTeacher ? (
            <div className="flex shrink-0 gap-2">
              <button
                onClick={() => setShowEditModal(true)}
                className="rounded-lg border border-border bg-bg-dark px-3 py-2 text-sm font-medium text-text-primary transition-colors hover:bg-bg-card"
              >
                <EditIcon />
              </button>
              <button
                onClick={() => setShowDeleteModal(true)}
                className="rounded-lg border border-error bg-error/10 px-3 py-2 text-sm font-medium text-error transition-colors hover:bg-error/20"
              >
                <TrashIcon />
              </button>
            </div>
          ) : (
            <button
              onClick={handleLeaveClassroom}
              disabled={leaveMutation.isPending}
              className="shrink-0 rounded-lg border border-error bg-error/10 px-4 py-2 text-sm font-medium text-error transition-colors hover:bg-error/20 disabled:opacity-50"
            >
              {leaveMutation.isPending ? 'Leaving...' : 'Leave Classroom'}
            </button>
          )}
        </div>

        {/* Join Code */}
        {isTeacher && (
          <div className="space-y-3">
            <div className="flex items-center justify-between gap-4">
              <label className="text-sm font-medium text-text-secondary">Join Code</label>
              <button
                onClick={handleRegenerateCode}
                disabled={regenerateMutation.isPending}
                className="flex items-center gap-2 rounded-lg bg-bg-dark px-3 py-1.5 text-xs font-medium text-text-secondary transition-colors hover:bg-accent-primary/10 hover:text-accent-primary disabled:opacity-50"
              >
                <RefreshIcon />
                Regenerate
              </button>
            </div>
            <div className="flex items-center gap-3">
              <div className="flex-1 rounded-lg border border-border bg-bg-dark px-4 py-3">
                <p className="font-mono text-2xl font-bold tracking-wider text-accent-primary">
                  {classroom.joinCode}
                </p>
              </div>
              <button
                onClick={copyJoinCode}
                className="rounded-lg bg-accent-primary px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-accent-secondary"
              >
                <div className="flex items-center gap-2">
                  <CopyIcon />
                  {copied ? 'Copied!' : 'Copy'}
                </div>
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Students List */}
      <div className="rounded-xl border border-border bg-bg-card p-6">
        <h2 className="mb-4 text-xl font-semibold text-text-primary">
          Students ({classroom.studentCount})
        </h2>

        {studentsLoading ? (
          <div className="space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-16 animate-pulse rounded-lg bg-bg-dark" />
            ))}
          </div>
        ) : !students || students.length === 0 ? (
          <EmptyState
            icon="empty"
            title="No students yet"
            description={
              isTeacher
                ? 'Share the join code with students to let them join this classroom.'
                : 'This classroom has no students yet.'
            }
          />
        ) : (
          <div className="space-y-3">
            {students.map((student) => (
              <div
                key={student.id}
                className="flex items-center justify-between gap-4 rounded-lg border border-border bg-bg-dark p-4"
              >
                <div className="flex items-center gap-3">
                  <div className="rounded-full bg-accent-primary/20 p-2 text-accent-primary">
                    <UserIcon />
                  </div>
                  <div>
                    <p className="font-medium text-text-primary">
                      {student.displayName || student.email}
                    </p>
                    {student.displayName && (
                      <p className="text-sm text-text-secondary">{student.email}</p>
                    )}
                    <p className="text-xs text-text-secondary">
                      Joined {new Date(student.joinedAt).toLocaleDateString()}
                    </p>
                  </div>
                </div>

                {isTeacher && (
                  <button
                    onClick={() => handleRemoveStudent(student.id, student.displayName || student.email)}
                    disabled={removeMutation.isPending}
                    className="rounded-lg border border-error bg-error/10 px-3 py-1.5 text-xs font-medium text-error transition-colors hover:bg-error/20 disabled:opacity-50"
                  >
                    Remove
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modals */}
      {showEditModal && (
        <EditModal
          classroomId={classroomId}
          initialName={classroom.name}
          initialDescription={classroom.description}
          onClose={() => setShowEditModal(false)}
        />
      )}

      {showDeleteModal && (
        <ConfirmDeleteModal
          classroomName={classroom.name}
          onConfirm={handleDeleteClassroom}
          onCancel={() => setShowDeleteModal(false)}
          isDeleting={deleteMutation.isPending}
        />
      )}
    </div>
  );
}
