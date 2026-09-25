import { useEffect, useState } from 'react';
import {
  Box, Typography, Grid, Table, TableHead, TableRow, TableCell, TableBody, Chip, Button,
  TextField, Tabs, Tab,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
} from 'recharts';
import axiosClient from '../api/axiosClient';
import BrutalCard from '../components/BrutalCard';
import { colors } from '../theme/theme';
import { blacklistSchema } from '../schemas/validationSchemas';

const STATUS_COLOR = { LICENSED: colors.green, PENDING: colors.yellow, REJECTED: colors.red };

export default function AdminPanel() {
  const [tab, setTab] = useState(0);
  const [pendingApps, setPendingApps] = useState([]);
  const [blacklist, setBlacklist] = useState([]);
  const [riskChart, setRiskChart] = useState([]);
  const { register, handleSubmit, reset, formState: { errors } } =
    useForm({ resolver: yupResolver(blacklistSchema) });

  const load = () => {
    axiosClient.get('/admin/apps/pending').then(({ data }) => setPendingApps(data));
    axiosClient.get('/admin/blacklist').then(({ data }) => setBlacklist(data));
    axiosClient.get('/admin/dashboard/risk-distribution').then(({ data }) => setRiskChart(data));
  };
  useEffect(load, []);

  const decide = async (aid, decision) => {
    await axiosClient.post(`/admin/apps/${aid}/decision`, { decision });
    load();
  };

  const addBlacklist = async (values) => {
    await axiosClient.post('/admin/blacklist', values);
    reset();
    load();
  };

  return (
    <Box sx={{ p: { xs: 2, md: 4 } }}>
      <Typography variant="h4" sx={{ mb: 3 }}>Admin Panel</Typography>

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <BrutalCard>
            <Typography variant="h6" sx={{ mb: 2 }}>Risk score distribution</Typography>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={riskChart}>
                <CartesianGrid stroke="#00000022" />
                <XAxis dataKey="bucket" stroke={colors.ink} />
                <YAxis allowDecimals={false} stroke={colors.ink} />
                <Tooltip contentStyle={{ border: `2px solid ${colors.ink}` }} />
                <Bar dataKey="count" fill={colors.pink} stroke={colors.ink} strokeWidth={2} />
              </BarChart>
            </ResponsiveContainer>
          </BrutalCard>
        </Grid>
        <Grid item xs={12} md={6}>
          <BrutalCard>
            <Typography variant="h6" sx={{ mb: 2 }}>Add a domain to the blacklist</Typography>
            <Box component="form" onSubmit={handleSubmit(addBlacklist)} sx={{ display: 'flex', gap: 1 }}>
              <TextField fullWidth label="Malicious domain / URL" {...register('malicious')}
                error={!!errors.malicious} helperText={errors.malicious?.message} />
              <Button variant="contained" color="error" type="submit">Block</Button>
            </Box>
          </BrutalCard>
        </Grid>
      </Grid>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label={`Pending review (${pendingApps.length})`} />
        <Tab label={`Blacklist (${blacklist.length})`} />
      </Tabs>

      {tab === 0 && (
        <BrutalCard sx={{ overflowX: 'auto' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>App</TableCell>
                <TableCell>Submitted by</TableCell>
                <TableCell>URL</TableCell>
                <TableCell>Risk score</TableCell>
                <TableCell>Signals</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {pendingApps.map((a) => (
                <TableRow key={a.aid}>
                  <TableCell sx={{ fontWeight: 700 }}>{a.appname}</TableCell>
                  <TableCell>{a.username}</TableCell>
                  <TableCell sx={{ maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {a.appurl}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={`${a.riskScore}/100`}
                      sx={{ bgcolor: a.riskScore > 60 ? colors.red : a.riskScore > 30 ? colors.yellow : colors.green }}
                    />
                  </TableCell>
                  <TableCell sx={{ fontSize: 12 }}>{a.riskSignals}</TableCell>
                  <TableCell sx={{ display: 'flex', gap: 1 }}>
                    <Button size="small" variant="contained" color="success" onClick={() => decide(a.aid, 'APPROVE')}>
                      Approve
                    </Button>
                    <Button size="small" variant="contained" color="error" onClick={() => decide(a.aid, 'REJECT')}>
                      Reject
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {pendingApps.length === 0 && (
                <TableRow><TableCell colSpan={6}>Nothing pending review.</TableCell></TableRow>
              )}
            </TableBody>
          </Table>
        </BrutalCard>
      )}

      {tab === 1 && (
        <BrutalCard sx={{ overflowX: 'auto' }}>
          <Table>
            <TableHead>
              <TableRow><TableCell>Domain / URL</TableCell></TableRow>
            </TableHead>
            <TableBody>
              {blacklist.map((b) => (
                <TableRow key={b.mid}><TableCell>{b.malicious}</TableCell></TableRow>
              ))}
            </TableBody>
          </Table>
        </BrutalCard>
      )}
    </Box>
  );
}
