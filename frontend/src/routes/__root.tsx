import { createRootRoute, createRoute, Outlet } from '@tanstack/react-router';
import { Header } from '../components/layout/Header';
import { HomePage } from './index';
import { ShowPage } from './shows/$slug';
import { LessonPage } from './shows/$slug.episodes.$episodeSlug';
import { ProgressPage } from './progress';
import { GeneratePage } from './generate';
import { ShowDetailPage } from './generate/shows.$tmdbId';
import { EpisodeListPage } from './generate/shows.$tmdbId.seasons.$season';
import { EpisodeConfirmationPage } from './generate/shows.$tmdbId.seasons.$season.episodes.$episode';

const rootRoute = createRootRoute({
  component: () => (
    <div className="min-h-screen bg-bg-dark">
      <Header />
      <main>
        <Outlet />
      </main>
    </div>
  ),
});

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

const lessonRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/shows/$slug/episodes/$episodeSlug',
  component: LessonPage,
});

const progressRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/progress',
  component: ProgressPage,
});

const generateRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate',
  component: GeneratePage,
});

const generateShowDetailRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate/shows/$tmdbId',
  component: ShowDetailPage,
});

const generateEpisodeListRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate/shows/$tmdbId/seasons/$season',
  component: EpisodeListPage,
});

const generateEpisodeConfirmRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/generate/shows/$tmdbId/seasons/$season/episodes/$episode',
  component: EpisodeConfirmationPage,
});

export const routeTree = rootRoute.addChildren([
  indexRoute,
  showRoute,
  lessonRoute,
  progressRoute,
  generateRoute,
  generateShowDetailRoute,
  generateEpisodeListRoute,
  generateEpisodeConfirmRoute,
]);
