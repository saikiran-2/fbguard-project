import { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, List, ListItem, ListItemText, TextField, Button, Grid, Chip,
} from '@mui/material';
import axiosClient from '../api/axiosClient';
import { useChatSocket } from '../api/useChatSocket';
import BrutalCard from '../components/BrutalCard';
import { colors } from '../theme/theme';

export default function Messages() {
  const [friends, setFriends] = useState([]);
  const [selected, setSelected] = useState('');
  const [thread, setThread] = useState([]);
  const [text, setText] = useState('');
  const [liveUnread, setLiveUnread] = useState({}); // username -> count, for friends not currently open

  useEffect(() => {
    axiosClient.get('/friends').then(({ data }) => setFriends(data));
  }, []);

  useEffect(() => {
    if (selected) {
      axiosClient.get(`/messages/${selected}`).then(({ data }) => setThread(data));
      setLiveUnread((prev) => ({ ...prev, [selected]: 0 }));
    }
  }, [selected]);

  // Live delivery: appends instantly if you're already looking at that thread,
  // otherwise just bumps an unread badge next to that friend's name.
  const handleIncoming = useCallback((payload) => {
    setSelected((currentSelected) => {
      if (payload.msgfrom === currentSelected) {
        setThread((prev) => [...prev, payload]);
      } else {
        setLiveUnread((prev) => ({ ...prev, [payload.msgfrom]: (prev[payload.msgfrom] || 0) + 1 }));
      }
      return currentSelected;
    });
  }, []);

  useChatSocket(handleIncoming);

  const send = async () => {
    if (!text.trim() || !selected) return;
    const toSend = text.trim();
    setText('');
    await axiosClient.post('/messages', { msgto: selected, msg: toSend });
    const { data } = await axiosClient.get(`/messages/${selected}`);
    setThread(data);
  };

  return (
    <Box sx={{ p: { xs: 2, md: 4 } }}>
      <Typography variant="h4" sx={{ mb: 3 }}>Messages</Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <BrutalCard>
            <Typography variant="h6" sx={{ mb: 1 }}>Chats</Typography>
            <List>
              {friends.map((f) => (
                <ListItem
                  key={f.username}
                  button
                  selected={selected === f.username}
                  onClick={() => setSelected(f.username)}
                  sx={{
                    border: `2px solid ${colors.ink}`,
                    mb: 1,
                    bgcolor: selected === f.username ? colors.yellow : 'white',
                    display: 'flex',
                    justifyContent: 'space-between',
                  }}
                >
                  <ListItemText primary={f.username} />
                  {liveUnread[f.username] > 0 && (
                    <Chip size="small" label={liveUnread[f.username]} sx={{ bgcolor: colors.red, color: 'white' }} />
                  )}
                </ListItem>
              ))}
            </List>
          </BrutalCard>
        </Grid>

        <Grid item xs={12} md={8}>
          <BrutalCard sx={{ minHeight: 420, display: 'flex', flexDirection: 'column' }}>
            {!selected ? (
              <Typography color="text.secondary">Pick a friend to start chatting.</Typography>
            ) : (
              <>
                <Box sx={{ flexGrow: 1, overflowY: 'auto', mb: 2 }}>
                  {thread.map((m) => (
                    <Box
                      key={m.mid}
                      sx={{
                        mb: 1,
                        p: 1.2,
                        maxWidth: '70%',
                        ml: m.msgfrom === selected ? 0 : 'auto',
                        bgcolor: m.msgfrom === selected ? colors.white : colors.blue,
                        color: m.msgfrom === selected ? colors.ink : colors.white,
                        border: `2px solid ${colors.ink}`,
                      }}
                    >
                      {m.msg}
                    </Box>
                  ))}
                  {thread.length === 0 && <Typography color="text.secondary">No messages yet — say hi!</Typography>}
                </Box>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <TextField
                    fullWidth
                    placeholder="Type a message…"
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && send()}
                  />
                  <Button variant="contained" color="warning" onClick={send}>Send</Button>
                </Box>
              </>
            )}
          </BrutalCard>
        </Grid>
      </Grid>
    </Box>
  );
}
