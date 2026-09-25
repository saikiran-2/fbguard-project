import { AppBar, Toolbar, Typography, Button, Box, Chip } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { colors } from '../theme/theme';

const NAV_LINKS = [
  { label: 'Dashboard', path: '/dashboard' },
  { label: 'App Gallery', path: '/apps' },
  { label: 'Submit App', path: '/apps/new' },
  { label: 'Friends', path: '/friends' },
  { label: 'Messages', path: '/messages' },
];

export default function NavBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  if (!user) return null;

  return (
    <AppBar position="sticky">
      <Toolbar sx={{ gap: 1, flexWrap: 'wrap', py: 1 }}>
        <Box
          sx={{
            width: 36,
            height: 36,
            bgcolor: colors.yellow,
            border: `3px solid ${colors.ink}`,
            display: 'grid',
            placeItems: 'center',
            fontWeight: 900,
            mr: 1,
          }}
        >
          FG
        </Box>
        <Typography variant="h6" sx={{ mr: 3, fontWeight: 800 }}>
          FBGuard
        </Typography>

        {user.role === 'ADMIN' ? (
          <Button
            variant={location.pathname.startsWith('/admin') ? 'contained' : 'text'}
            color="secondary"
            onClick={() => navigate('/admin')}
          >
            Admin Panel
          </Button>
        ) : (
          NAV_LINKS.map((link) => (
            <Button
              key={link.path}
              variant={location.pathname === link.path ? 'contained' : 'text'}
              color="primary"
              onClick={() => navigate(link.path)}
            >
              {link.label}
            </Button>
          ))
        )}

        <Box sx={{ flexGrow: 1 }} />

        <Chip label={user.username} sx={{ bgcolor: colors.white }} />
        <Button
          variant="contained"
          color="error"
          onClick={() => {
            logout();
            navigate('/login');
          }}
        >
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  );
}
