import { createTheme } from '@mui/material/styles';

// Neo-brutalist design tokens
export const colors = {
  bg: '#F2F0E9',
  ink: '#111111',
  yellow: '#FFD400',
  pink: '#FF5CB8',
  blue: '#3D5AFE',
  green: '#00C853',
  red: '#FF3B30',
  white: '#FFFFFF',
};

export const brutalShadow = (offset = 5) => `${offset}px ${offset}px 0px ${colors.ink}`;

const theme = createTheme({
  palette: {
    mode: 'light',
    background: { default: colors.bg, paper: colors.white },
    primary: { main: colors.blue, contrastText: colors.white },
    secondary: { main: colors.pink, contrastText: colors.ink },
    warning: { main: colors.yellow, contrastText: colors.ink },
    success: { main: colors.green, contrastText: colors.ink },
    error: { main: colors.red, contrastText: colors.white },
    text: { primary: colors.ink },
  },
  shape: { borderRadius: 0 },
  typography: {
    fontFamily: '"Space Grotesk", "Archivo", sans-serif',
    h1: { fontWeight: 800, letterSpacing: '-0.02em' },
    h2: { fontWeight: 800, letterSpacing: '-0.02em' },
    h3: { fontWeight: 700 },
    h4: { fontWeight: 700 },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 700 },
    button: { fontWeight: 700, textTransform: 'none' },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          border: `3px solid ${colors.ink}`,
          boxShadow: brutalShadow(4),
          transition: 'transform 0.08s ease, box-shadow 0.08s ease',
          '&:hover': {
            boxShadow: brutalShadow(2),
            transform: 'translate(2px, 2px)',
            backgroundColor: undefined,
          },
          '&:active': {
            boxShadow: 'none',
            transform: 'translate(4px, 4px)',
          },
        },
        contained: { backgroundColor: colors.white, color: colors.ink },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          border: `3px solid ${colors.ink}`,
          boxShadow: brutalShadow(6),
          backgroundImage: 'none',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          border: `3px solid ${colors.ink}`,
          boxShadow: brutalShadow(6),
        },
      },
    },
    MuiTextField: {
      defaultProps: { variant: 'outlined' },
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            backgroundColor: colors.white,
            '& fieldset': { borderColor: colors.ink, borderWidth: 3 },
            '&:hover fieldset': { borderColor: colors.ink },
            '&.Mui-focused fieldset': { borderColor: colors.blue, borderWidth: 3 },
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          border: `2px solid ${colors.ink}`,
          fontWeight: 700,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: colors.white,
          color: colors.ink,
          boxShadow: `0 4px 0 0 ${colors.ink}`,
          borderBottom: `3px solid ${colors.ink}`,
        },
      },
    },
  },
});

export default theme;
