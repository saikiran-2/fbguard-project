import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { Box, TextField, Button, Typography, Alert, Link } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { loginSchema } from '../schemas/validationSchemas';
import { useAuth } from '../context/AuthContext';
import BrutalCard from '../components/BrutalCard';
import { colors } from '../theme/theme';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState('');
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: yupResolver(loginSchema) });

  const onSubmit = async (values) => {
    setServerError('');
    try {
      const user = await login(values.username, values.password);
      navigate(user.role === 'ADMIN' ? '/admin' : '/dashboard');
    } catch (err) {
      setServerError(err?.response?.data?.message || 'Invalid username or password.');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        bgcolor: colors.bg,
        backgroundImage:
          'linear-gradient(#00000012 2px, transparent 2px), linear-gradient(90deg, #00000012 2px, transparent 2px)',
        backgroundSize: '32px 32px',
        p: 2,
      }}
    >
      <BrutalCard sx={{ width: 400, maxWidth: '100%' }} accent={colors.white}>
        <Typography variant="h4" sx={{ mb: 0.5 }}>
          FBGuard
        </Typography>
        <Typography sx={{ mb: 3 }} color="text.secondary">
          Sign in to check apps, add friends and chat.
        </Typography>

        {serverError && (
          <Alert severity="error" sx={{ mb: 2, border: `2px solid ${colors.ink}` }}>
            {serverError}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <TextField
            fullWidth
            label="Username"
            margin="normal"
            {...register('username')}
            error={!!errors.username}
            helperText={errors.username?.message}
          />
          <TextField
            fullWidth
            type="password"
            label="Password"
            margin="normal"
            {...register('password')}
            error={!!errors.password}
            helperText={errors.password?.message}
          />
          <Button
            fullWidth
            type="submit"
            variant="contained"
            color="warning"
            size="large"
            disabled={isSubmitting}
            sx={{ mt: 2, py: 1.3 }}
          >
            {isSubmitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </Box>

        <Typography sx={{ mt: 3 }}>
          New here?{' '}
          <Link component="button" onClick={() => navigate('/register')} sx={{ fontWeight: 700 }}>
            Create an account
          </Link>
        </Typography>
      </BrutalCard>
    </Box>
  );
}
