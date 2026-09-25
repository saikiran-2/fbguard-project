import { useEffect, useState } from 'react';
import {
  Box, Typography, Grid, Button, TextField, List, ListItem, ListItemText, Chip, Tabs, Tab,
} from '@mui/material';
import axiosClient from '../api/axiosClient';
import BrutalCard from '../components/BrutalCard';
import { colors } from '../theme/theme';

export default function Friends() {
  const [tab, setTab] = useState(0);
  const [friends, setFriends] = useState([]);
  const [pending, setPending] = useState([]);
  const [newFriend, setNewFriend] = useState('');

  const load = () => {
    axiosClient.get('/friends').then(({ data }) => setFriends(data));
    axiosClient.get('/friends/pending').then(({ data }) => setPending(data));
  };

  useEffect(load, []);

  const sendRequest = async () => {
    if (!newFriend.trim()) return;
    await axiosClient.post('/friends/request', { rto: newFriend.trim() });
    setNewFriend('');
    load();
  };

  const respond = async (rfrom, status) => {
    await axiosClient.post('/friends/respond', { rfrom, status });
    load();
  };

  return (
    <Box sx={{ p: { xs: 2, md: 4 } }}>
      <Typography variant="h4" sx={{ mb: 3 }}>Friends</Typography>

      <BrutalCard sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <TextField
          label="Send a friend request (username)"
          value={newFriend}
          onChange={(e) => setNewFriend(e.target.value)}
          sx={{ flexGrow: 1, minWidth: 220 }}
        />
        <Button variant="contained" color="warning" onClick={sendRequest}>
          Send request
        </Button>
      </BrutalCard>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label={`Friends (${friends.length})`} />
        <Tab label={`Pending (${pending.length})`} />
      </Tabs>

      {tab === 0 && (
        <Grid container spacing={2}>
          {friends.map((f) => (
            <Grid item xs={12} sm={6} md={4} key={f.username}>
              <BrutalCard sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Typography sx={{ fontWeight: 700 }}>{f.username}</Typography>
                <Chip label="Friends" sx={{ bgcolor: colors.green }} />
              </BrutalCard>
            </Grid>
          ))}
          {friends.length === 0 && <Typography sx={{ p: 2 }}>No friends yet.</Typography>}
        </Grid>
      )}

      {tab === 1 && (
        <List>
          {pending.map((p) => (
            <ListItem
              key={p.rfrom}
              sx={{ border: `3px solid ${colors.ink}`, mb: 1, bgcolor: 'white' }}
              secondaryAction={
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button size="small" variant="contained" color="success" onClick={() => respond(p.rfrom, 'Accept')}>
                    Accept
                  </Button>
                  <Button size="small" variant="contained" color="error" onClick={() => respond(p.rfrom, 'Reject')}>
                    Decline
                  </Button>
                </Box>
              }
            >
              <ListItemText primary={p.rfrom} secondary="wants to be friends" />
            </ListItem>
          ))}
          {pending.length === 0 && <Typography sx={{ p: 2 }}>No pending requests.</Typography>}
        </List>
      )}
    </Box>
  );
}
