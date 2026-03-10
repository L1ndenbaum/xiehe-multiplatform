import type { RouteObject } from "react-router-dom";
import { lazy } from 'react';

const Home = lazy(() => import('../pages/home/page'));
const Dashboard = lazy(() => import('../pages/dashboard/page'));
const Patients = lazy(() => import('../pages/patients/page'));
const PatientDetail = lazy(() => import('../pages/patients/detail/page'));
const Images = lazy(() => import('../pages/images/page'));
const Profile = lazy(() => import('../pages/profile/page'));
const ProfileInfo = lazy(() => import('../pages/profile/info/page'));
const ChangePassword = lazy(() => import('../pages/profile/password/page'));
const Organization = lazy(() => import('../pages/profile/organization/page'));
const Settings = lazy(() => import('../pages/profile/settings/page'));
const Messages = lazy(() => import('../pages/messages/page'));
const Login = lazy(() => import('../pages/login/page'));
const ImageAnnotation = lazy(() => import('../pages/image-annotation/page'));
const NotFound = lazy(() => import('../pages/NotFound'));

const routes: RouteObject[] = [
  {
    path: '/',
    element: <Dashboard />
  },
  {
    path: '/dashboard',
    element: <Dashboard />
  },
  {
    path: '/patients',
    element: <Patients />
  },
  {
    path: '/patients/detail/:id',
    element: <PatientDetail />
  },
  {
    path: '/images',
    element: <Images />
  },
  {
    path: '/image-annotation',
    element: <ImageAnnotation />
  },
  {
    path: '/profile',
    element: <Profile />
  },
  {
    path: '/profile/info',
    element: <ProfileInfo />
  },
  {
    path: '/profile/password',
    element: <ChangePassword />
  },
  {
    path: '/profile/organization',
    element: <Organization />
  },
  {
    path: '/profile/settings',
    element: <Settings />
  },
  {
    path: '/messages',
    element: <Messages />
  },
  {
    path: '/login',
    element: <Login />
  },
  {
    path: '*',
    element: <NotFound />
  }
];

export default routes;
