import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import theme from './theme/theme';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import NavBar from './components/NavBar';

import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import AppGallery from './pages/AppGallery';
import AddApp from './pages/AddApp';
import Friends from './pages/Friends';
import Messages from './pages/Messages';
import Profile from './pages/Profile';
import AdminPanel from './pages/AdminPanel';

function Shell({ children }) {
  return (
    <>
      <NavBar />
      {children}
    </>
  );
}

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
}

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<HomeRedirect />} />

            <Route path="/dashboard" element={
              <ProtectedRoute><Shell><Dashboard /></Shell></ProtectedRoute>
            } />
            <Route path="/apps" element={
              <ProtectedRoute><Shell><AppGallery /></Shell></ProtectedRoute>
            } />
            <Route path="/apps/new" element={
              <ProtectedRoute><Shell><AddApp /></Shell></ProtectedRoute>
            } />
            <Route path="/friends" element={
              <ProtectedRoute><Shell><Friends /></Shell></ProtectedRoute>
            } />
            <Route path="/messages" element={
              <ProtectedRoute><Shell><Messages /></Shell></ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute><Shell><Profile /></Shell></ProtectedRoute>
            } />
            <Route path="/admin" element={
              <ProtectedRoute adminOnly><Shell><AdminPanel /></Shell></ProtectedRoute>
            } />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}
