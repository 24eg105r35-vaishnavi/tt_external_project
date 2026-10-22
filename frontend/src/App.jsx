import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import TopStrip from './components/TopStrip';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';

// Public & Common Pages
import HomePage from './pages/common/HomePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import LeaderboardPage from './pages/common/LeaderboardPage';
import AnalyticsPage from './pages/common/AnalyticsPage';
import PublicProfilePage from './pages/common/PublicProfilePage';
import SearchPage from './pages/common/SearchPage';
import NotificationsPage from './pages/common/NotificationsPage';
import NotFoundPage from './pages/common/NotFoundPage';

// Student Pages
import StudentDashboard from './pages/student/StudentDashboard';
import UploadAchievementPage from './pages/student/UploadAchievementPage';
import MyAchievementsPage from './pages/student/MyAchievementsPage';
import AchievementDetailPage from './pages/student/AchievementDetailPage';
import EditAchievementPage from './pages/student/EditAchievementPage';
import StudentProfilePage from './pages/student/StudentProfilePage';

// Faculty Pages
import FacultyDashboard from './pages/faculty/FacultyDashboard';
import FacultyQueuePage from './pages/faculty/FacultyQueuePage';
import VerificationReviewPage from './pages/faculty/VerificationReviewPage';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminCategoriesPage from './pages/admin/AdminCategoriesPage';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminAuditPage from './pages/admin/AdminAuditPage';

export default function App() {
  return (
    <AuthProvider>
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <TopStrip />
        <Navbar />
        <main style={{ flex: 1 }}>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/leaderboard" element={<LeaderboardPage />} />
            <Route path="/analytics" element={<AnalyticsPage />} />
            <Route path="/u/:slug" element={<PublicProfilePage />} />

            {/* Authenticated Common Routes */}
            <Route
              path="/search"
              element={
                <ProtectedRoute allowedRoles={['STUDENT', 'FACULTY', 'ADMIN']}>
                  <SearchPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/notifications"
              element={
                <ProtectedRoute allowedRoles={['STUDENT', 'FACULTY', 'ADMIN']}>
                  <NotificationsPage />
                </ProtectedRoute>
              }
            />

            {/* Student Protected Routes */}
            <Route
              path="/student/dashboard"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/upload"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <UploadAchievementPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/achievements"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <MyAchievementsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/achievements/:id"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <AchievementDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/achievements/:id/edit"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <EditAchievementPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/profile"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentProfilePage />
                </ProtectedRoute>
              }
            />

            {/* Faculty Protected Routes */}
            <Route
              path="/faculty/dashboard"
              element={
                <ProtectedRoute allowedRoles={['FACULTY']}>
                  <FacultyDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/faculty/queue"
              element={
                <ProtectedRoute allowedRoles={['FACULTY']}>
                  <FacultyQueuePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/faculty/review/:id"
              element={
                <ProtectedRoute allowedRoles={['FACULTY']}>
                  <VerificationReviewPage />
                </ProtectedRoute>
              }
            />

            {/* Admin Protected Routes */}
            <Route
              path="/admin/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/categories"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminCategoriesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/users"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminUsersPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/audit"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminAuditPage />
                </ProtectedRoute>
              }
            />

            {/* 404 Fallback */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </AuthProvider>
  );
}
