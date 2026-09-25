import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { Box, TextField, Button, Typography, Alert, Avatar, Grid } from '@mui/material';
import axiosClient from '../api/axiosClient';
import BrutalCard from '../components/BrutalCard';
import { profileSchema } from '../schemas/validationSchemas';
import { useAuth } from '../context/AuthContext';

export default function Profile() {
  const { user } = useAuth();
  const [serverMsg, setServerMsg] = useState(null);
  const [photo, setPhoto] = useState(null);
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } =
    useForm({ resolver: yupResolver(profileSchema) });

  useEffect(() => {
    axiosClient.get('/users/me').then(({ data }) => {
      reset({ email: data.email, country: data.country, phoneno: data.phoneno });
    });
  }, [reset]);

  const onSubmit = async (values) => {
    setServerMsg(null);
    try {
      const formData = new FormData();
      Object.entries(values).forEach(([k, v]) => formData.append(k, v));
      if (photo) formData.append('file', photo);
      await axiosClient.put('/users/me', formData);
      setServerMsg({ type: 'success', text: 'Profile updated.' });
    } catch (err) {
      setServerMsg({ type: 'error', text: err?.response?.data?.message || 'Update failed.' });
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 4 }, maxWidth: 640 }}>
      <Typography variant="h4" sx={{ mb: 3 }}>Your profile</Typography>
      <BrutalCard>
        {serverMsg && <Alert severity={serverMsg.type} sx={{ mb: 2 }}>{serverMsg.text}</Alert>}
        <Grid container spacing={2} alignItems="center" sx={{ mb: 2 }}>
          <Grid item>
            <Avatar src={user?.photoUrl} sx={{ width: 72, height: 72, border: '3px solid #111' }} />
          </Grid>
          <Grid item xs>
            <Typography variant="h6">{user?.username}</Typography>
          </Grid>
        </Grid>

        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <TextField fullWidth label="Email" margin="normal" {...register('email')}
            error={!!errors.email} helperText={errors.email?.message} />
          <TextField fullWidth label="Country" margin="normal" {...register('country')}
            error={!!errors.country} helperText={errors.country?.message} />
          <TextField fullWidth label="Phone number" margin="normal" {...register('phoneno')}
            error={!!errors.phoneno} helperText={errors.phoneno?.message} />
          <Button variant="contained" component="label" color="warning" sx={{ mt: 1 }}>
            {photo ? photo.name : 'Change profile photo'}
            <input hidden type="file" accept="image/*" onChange={(e) => setPhoto(e.target.files[0])} />
          </Button>
          <Button fullWidth type="submit" variant="contained" color="success" size="large"
            disabled={isSubmitting} sx={{ mt: 3, py: 1.3 }}>
            Save changes
          </Button>
        </Box>
      </BrutalCard>
    </Box>
  );
}
