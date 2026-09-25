import { useEffect, useState } from 'react';
import { Box, Typography, Grid, CircularProgress } from '@mui/material';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import axiosClient from '../api/axiosClient';
import BrutalCard from '../components/BrutalCard';
import { colors } from '../theme/theme';

const PIE_COLORS = [colors.green, colors.red, colors.yellow, colors.blue];

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axiosClient
      .get('/dashboard/stats')
      .then(({ data }) => setStats(data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'grid', placeItems: 'center', height: '60vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  const statusPieData = stats?.appsByStatus
    ? Object.entries(stats.appsByStatus).map(([name, value]) => ({ name, value }))
    : [];

  const weeklyBarData = stats?.submissionsPerDay || [];

  return (
    <Box sx={{ p: { xs: 2, md: 4 } }}>
      <Typography variant="h4" sx={{ mb: 3 }}>
        Welcome back{stats?.username ? `, ${stats.username}` : ''}
      </Typography>

      <Grid container spacing={3} sx={{ mb: 1 }}>
        <StatBox label="Apps submitted" value={stats?.totalApps ?? 0} accent={colors.yellow} />
        <StatBox label="Licensed apps" value={stats?.licensedApps ?? 0} accent={colors.green} />
        <StatBox label="Blocked (malicious)" value={stats?.blockedApps ?? 0} accent={colors.red} />
        <StatBox label="Friends" value={stats?.friendCount ?? 0} accent={colors.blue} />
      </Grid>

      <Grid container spacing={3} sx={{ mt: 1 }}>
        <Grid item xs={12} md={7}>
          <BrutalCard>
            <Typography variant="h6" sx={{ mb: 2 }}>App submissions — last 7 days</Typography>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={weeklyBarData}>
                <CartesianGrid stroke="#00000022" />
                <XAxis dataKey="day" stroke={colors.ink} />
                <YAxis allowDecimals={false} stroke={colors.ink} />
                <Tooltip contentStyle={{ border: `2px solid ${colors.ink}` }} />
                <Bar dataKey="count" fill={colors.blue} stroke={colors.ink} strokeWidth={2} />
              </BarChart>
            </ResponsiveContainer>
          </BrutalCard>
        </Grid>

        <Grid item xs={12} md={5}>
          <BrutalCard>
            <Typography variant="h6" sx={{ mb: 2 }}>App status breakdown</Typography>
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={statusPieData}
                  dataKey="value"
                  nameKey="name"
                  outerRadius={100}
                  stroke={colors.ink}
                  strokeWidth={2}
                  label
                >
                  {statusPieData.map((entry, idx) => (
                    <Cell key={entry.name} fill={PIE_COLORS[idx % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip contentStyle={{ border: `2px solid ${colors.ink}` }} />
              </PieChart>
            </ResponsiveContainer>
          </BrutalCard>
        </Grid>
      </Grid>
    </Box>
  );
}

function StatBox({ label, value, accent }) {
  return (
    <Grid item xs={6} md={3}>
      <BrutalCard accent={accent} sx={{ textAlign: 'center', py: 2 }}>
        <Typography variant="h3">{value}</Typography>
        <Typography sx={{ fontWeight: 700 }}>{label}</Typography>
      </BrutalCard>
    </Grid>
  );
}
