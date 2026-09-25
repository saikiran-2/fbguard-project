import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { Box, TextField, Button, Typography, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { addAppSchema } from '../schemas/validationSchemas';
import axiosClient from '../api/axiosClient';
import BrutalCard from '../components/BrutalCard';

export default function AddApp() {
  const navigate = useNavigate();
  const [icon, setIcon] = useState(null);
  const [serverMsg, setServerMsg] = useState(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: yupResolver(addAppSchema) });

  const onSubmit = async (values) => {
    setServerMsg(null);
    try {
      const formData = new FormData();
      Object.entries(values).forEach(([k, v]) => formData.append(k, v));
      if (icon) formData.append('icon', icon);

      const { data } = await axiosClient.post('/apps', formData);
      setServerMsg({
        type: data.riskLevel === 'HIGH' ? 'error' : 'success',
        text: `Submitted. Verification result: ${data.riskLevel} risk (score ${data.riskScore}/100). ${data.message}`,
      });
      setTimeout(() => navigate('/apps'), 2500);
    } catch (err) {
      setServerMsg({ type: 'error', text: err?.response?.data?.message || 'Submission failed.' });
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 4 }, maxWidth: 640 }}>
      <Typography variant="h4" sx={{ mb: 1 }}>Submit an app</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        We run every submitted URL through our verification engine (blacklist + heuristics +
        Safe Browsing + reputation checks) before it goes live.
      </Typography>

      <BrutalCard>
        {serverMsg && <Alert severity={serverMsg.type} sx={{ mb: 2 }}>{serverMsg.text}</Alert>}
        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <TextField fullWidth label="App name" margin="normal" {...register('appname')}
            error={!!errors.appname} helperText={errors.appname?.message} />
          <TextField fullWidth label="App ID" margin="normal" {...register('appid')}
            error={!!errors.appid} helperText={errors.appid?.message} />
          <TextField fullWidth label="App URL" margin="normal"
            placeholder="https://apps.facebook.com/yourapp"
            {...register('appurl')} error={!!errors.appurl} helperText={errors.appurl?.message} />
          <Button variant="contained" component="label" color="warning" sx={{ mt: 2 }}>
            {icon ? icon.name : 'Upload app icon'}
            <input hidden type="file" accept="image/*" onChange={(e) => setIcon(e.target.files[0])} />
          </Button>
          <Button fullWidth type="submit" variant="contained" color="success" size="large"
            disabled={isSubmitting} sx={{ mt: 3, py: 1.3 }}>
            {isSubmitting ? 'Verifying…' : 'Submit for verification'}
          </Button>
        </Box>
      </BrutalCard>
    </Box>
  );
}
