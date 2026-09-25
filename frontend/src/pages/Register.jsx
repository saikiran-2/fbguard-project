import { useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import {
  Box, TextField, Button, Typography, Alert, Link, MenuItem, Grid,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { registerSchema } from '../schemas/validationSchemas';
import { useAuth } from '../context/AuthContext';
import BrutalCard from '../components/BrutalCard';
import { colors } from '../theme/theme';

export default function Register() {
  const { register: doRegister } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState('');
  const [photo, setPhoto] = useState(null);
  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: yupResolver(registerSchema) });

  const onSubmit = async (values) => {
    setServerError('');
    try {
      const formData = new FormData();
      Object.entries(values).forEach(([k, v]) => formData.append(k, v));
      if (photo) formData.append('file', photo);
      await doRegister(formData);
      navigate('/login');
    } catch (err) {
      setServerError(err?.response?.data?.message || 'Registration failed. Try a different username.');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        bgcolor: colors.bg,
        p: 2,
      }}
    >
      <BrutalCard sx={{ width: 560, maxWidth: '100%' }} accent={colors.white}>
        <Typography variant="h4" sx={{ mb: 0.5 }}>
          Create your account
        </Typography>
        <Typography sx={{ mb: 3 }} color="text.secondary">
          Join FBGuard to submit apps and add friends.
        </Typography>

        {serverError && <Alert severity="error" sx={{ mb: 2 }}>{serverError}</Alert>}

        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Username" {...register('username')}
                error={!!errors.username} helperText={errors.username?.message} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth type="password" label="Password" {...register('password')}
                error={!!errors.password} helperText={errors.password?.message} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Email" {...register('email')}
                error={!!errors.email} helperText={errors.email?.message} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller
                name="gender"
                control={control}
                defaultValue=""
                render={({ field }) => (
                  <TextField {...field} select fullWidth label="Gender"
                    error={!!errors.gender} helperText={errors.gender?.message}>
                    <MenuItem value="Male">Male</MenuItem>
                    <MenuItem value="Female">Female</MenuItem>
                    <MenuItem value="Other">Other</MenuItem>
                  </TextField>
                )}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Country" {...register('country')}
                error={!!errors.country} helperText={errors.country?.message} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Phone number" {...register('phoneno')}
                error={!!errors.phoneno} helperText={errors.phoneno?.message} />
            </Grid>
            <Grid item xs={12}>
              <Button variant="contained" component="label" color="warning">
                {photo ? photo.name : 'Upload profile photo'}
                <input hidden type="file" accept="image/*" onChange={(e) => setPhoto(e.target.files[0])} />
              </Button>
            </Grid>
          </Grid>

          <Button fullWidth type="submit" variant="contained" color="success" size="large"
            disabled={isSubmitting} sx={{ mt: 3, py: 1.3 }}>
            {isSubmitting ? 'Creating account…' : 'Create account'}
          </Button>
        </Box>

        <Typography sx={{ mt: 3 }}>
          Already have an account?{' '}
          <Link component="button" onClick={() => navigate('/login')} sx={{ fontWeight: 700 }}>
            Sign in
          </Link>
        </Typography>
      </BrutalCard>
    </Box>
  );
}
