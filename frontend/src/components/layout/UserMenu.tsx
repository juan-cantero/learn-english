import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from '@tanstack/react-router';
import { useAuth } from '../../context/AuthContext';

export function UserMenu() {
  const { user, loading, signOut } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSignOut = async () => {
    await signOut();
    setIsOpen(false);
    navigate({ to: '/' });
  };

  if (loading) {
    return (
      <div className="h-8 w-8 animate-pulse rounded-full bg-bg-elevated" />
    );
  }

  if (!user) {
    return (
      <Link
        to="/login"
        className="rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover"
      >
        Sign In
      </Link>
    );
  }

  const userInitial = user.email?.charAt(0).toUpperCase() || 'U';

  return (
    <div className="relative" ref={menuRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex h-9 w-9 items-center justify-center rounded-full bg-brand text-sm font-semibold text-white transition-transform hover:scale-105"
        title={user.email || 'User'}
      >
        {userInitial}
      </button>

      {isOpen && (
        <div className="absolute right-0 top-full mt-2 w-56 rounded-lg border border-edge-default bg-bg-card py-1 shadow-xl">
          <div className="border-b border-edge-default px-4 py-3">
            <p className="text-sm font-medium text-content-primary">Signed in as</p>
            <p className="truncate text-sm text-content-secondary">{user.email}</p>
          </div>
          <div className="py-1">
            <Link
              to="/progress"
              onClick={() => setIsOpen(false)}
              className="block px-4 py-2 text-sm text-content-secondary transition-colors hover:bg-bg-elevated hover:text-content-primary"
            >
              My Progress
            </Link>
          </div>
          <div className="border-t border-edge-default py-1">
            <button
              onClick={handleSignOut}
              className="block w-full px-4 py-2 text-left text-sm text-error transition-colors hover:bg-bg-elevated"
            >
              Sign Out
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
