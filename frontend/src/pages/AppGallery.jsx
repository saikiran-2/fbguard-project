import { useEffect, useState } from 'react';
import {
  Box, Typography, Grid, Chip, CardMedia, CardContent, CircularProgress, TextField, Pagination,
} from '@mui/material';
import axiosClient from '../api/axiosClient';
import BrutalCard from '../components/BrutalCard';
import { colors } from '../theme/theme';

const STATUS_COLOR = {
  LICENSED: colors.green,
  PENDING: colors.yellow,
  REJECTED: colors.red,
};

const PAGE_SIZE = 12;

export default function AppGallery() {
  const [apps, setApps] = useState([]);
  const [page, setPage] = useState(0); // backend pages are 0-indexed
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');

  useEffect(() => {
    setLoading(true);
    axiosClient
      .get('/apps', { params: { page, size: PAGE_SIZE } })
      .then(({ data }) => {
        setApps(data.content); // Spring's Page<T> wraps results in `content`
        setTotalPages(data.totalPages);
      })
      .finally(() => setLoading(false));
  }, [page]);

  const filtered = apps.filter((a) => a.appname.toLowerCase().includes(query.toLowerCase()));

  return (
    <Box sx={{ p: { xs: 2, md: 4 } }}>
      <Typography variant="h4" sx={{ mb: 1 }}>App Gallery</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Every app that's been submitted, with its current verification status.
      </Typography>

      <TextField
        placeholder="Search this page…"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        sx={{ mb: 3, width: 320, maxWidth: '100%' }}
      />

      {loading ? (
        <CircularProgress />
      ) : (
        <>
          <Grid container spacing={3}>
            {filtered.map((app) => (
              <Grid item xs={12} sm={6} md={4} key={app.aid}>
                <BrutalCard sx={{ p: 0, overflow: 'hidden' }}>
                  <CardMedia
                    component="img"
                    height="140"
                    image={app.appIconUrl || '/placeholder-app.png'}
                    alt={app.appname}
                    sx={{ borderBottom: `3px solid ${colors.ink}`, objectFit: 'cover' }}
                  />
                  <CardContent>
                    <Typography variant="h6">{app.appname}</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      By {app.username}
                    </Typography>
                    <Chip
                      label={app.status}
                      sx={{ bgcolor: STATUS_COLOR[app.status] || colors.white, fontWeight: 700 }}
                    />
                  </CardContent>
                </BrutalCard>
              </Grid>
            ))}
            {filtered.length === 0 && (
              <Typography sx={{ p: 2 }}>No apps match your search on this page.</Typography>
            )}
          </Grid>

          {totalPages > 1 && (
            <Pagination
              sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}
              count={totalPages}
              page={page + 1}
              onChange={(_, value) => setPage(value - 1)}
              shape="rounded"
            />
          )}
        </>
      )}
    </Box>
  );
}

