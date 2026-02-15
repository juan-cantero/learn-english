import { createRootRoute, createRoute, redirect, Outlet } from '@tanstack/react-router';
import { Header } from '../components/layout/Header';
import { BottomNavigation } from '../components/layout/BottomNavigation';
import { OfflineBanner } from '../components/shared/OfflineBanner';
import { ProtectedRoute } from '../components/auth/ProtectedRoute';
import { HomePage } from './index';
import { ShowPage } from './shows/$slug';
import { LessonPage } from './shows/$slug.episodes.$episodeSlug';
import { ProgressPage } from './progress';
import { GeneratePage } from './generate';
import { ShowDetailPage } from './generate/shows.$tmdbId';
import { EpisodeListPage } from './generate/shows.$tmdbId.seasons.$season';
import { EpisodeConfirmationPage } from './generate/shows.$tmdbId.seasons.$season.episodes.$episode';
import { LoginPage } from './LoginPage';
import { RegisterPage } from './RegisterPage';
import { ClassroomsPage } from './classrooms';
import { ClassroomDetailPage } from './classrooms.$classroomId';
import { PhonemesPage } from './phonemes';

const rootRoute = createRootRoute({
  component: () => (
    <div className="min-h-screen bg-bg-primary">
      <Header />
      <OfflineBanner />
      <main className="pb-20 md:pb-0">
        <Outlet />
      </main>
      <BottomNavigation />
    </div>
  ),
});

// --- Public routes ---

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/',
  component: HomePage,
});

const showRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/shows/$slug',
  component: ShowPage,
});

const loginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/login',
  component: LoginPage,
});

const registerRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/register',
  component: RegisterPage,
});

const phonemesRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/phonemes',
  component: PhonemesPage,
});

const showsRedirectRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/shows',
  beforeLoad: () => {
    throw redirect({ to: '/' });
  },
});

// --- Protected routes ---

const lessonRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/shows/$slug/episodes/$episodeSlug',
  component: () => (
    <ProtectedRoute>
      <LessonPage />
    </ProtectedRoute>
  ),
});

const progressRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/progress',
  component: () => (
    <ProtectedRoute>
      <ProgressPage />
    </ProtectedRoute>
  ),
});

const generateRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate',
  component: () => (
    <ProtectedRoute>
      <GeneratePage />
    </ProtectedRoute>
  ),
});

const generateShowDetailRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate/shows/$tmdbId',
  component: () => (
    <ProtectedRoute>
      <ShowDetailPage />
    </ProtectedRoute>
  ),
});

const generateEpisodeListRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate/shows/$tmdbId/seasons/$season',
  component: () => (
    <ProtectedRoute>
      <EpisodeListPage />
    </ProtectedRoute>
  ),
});

const generateEpisodeConfirmRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate/shows/$tmdbId/seasons/$season/episodes/$episode',
  component: () => (
    <ProtectedRoute>
      <EpisodeConfirmationPage />
    </ProtectedRoute>
  ),
});

const classroomsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/classrooms',
  component: () => (
    <ProtectedRoute>
      <ClassroomsPage />
    </ProtectedRoute>
  ),
});

const classroomDetailRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/classrooms/$classroomId',
  component: () => (
    <ProtectedRoute>
      <ClassroomDetailPage />
    </ProtectedRoute>
  ),
});

export const routeTree = rootRoute.addChildren([
  indexRoute,
  showsRedirectRoute,
  showRoute,
  lessonRoute,
  progressRoute,
  generateRoute,
  generateShowDetailRoute,
  generateEpisodeListRoute,
  generateEpisodeConfirmRoute,
  classroomsRoute,
  classroomDetailRoute,
  loginRoute,
  registerRoute,
  phonemesRoute,
]);
