import { createRootRoute, createRoute, Outlet } from '@tanstack/react-router';
import { Header } from '../components/layout/Header';
import { HomePage } from './index';
import { ShowPage } from './shows/$slug';
import { LessonPage } from './shows/$slug.episodes.$episodeSlug';
import { ProgressPage } from './progress';

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

export const routeTree = rootRoute.addChildren([
  indexRoute,
  showRoute,
  lessonRoute,
  progressRoute,
]);
