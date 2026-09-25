import { Paper } from '@mui/material';

export default function BrutalCard({ children, sx = {}, accent, ...rest }) {
  return (
    <Paper
      sx={{
        p: 3,
        bgcolor: accent || 'background.paper',
        ...sx,
      }}
      {...rest}
    >
      {children}
    </Paper>
  );
}
