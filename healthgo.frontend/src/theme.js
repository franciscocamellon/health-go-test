
import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#22c55e' }, // emerald-ish
    secondary: { main: '#f59e0b' }, // amber for alerts
    background: {
      default: '#0a0a0a',
      paper: '#111113',
    },
    text: {
      primary: '#e5e7eb',
      secondary: '#9ca3af',
    },
  },
  typography: {
    fontFamily: [
      'Inter', 'Roboto', 'Helvetica', 'Arial', 'sans-serif'
    ].join(','),
    h4: { fontWeight: 700 },
    h6: { fontWeight: 600 },
  },
  shape: { borderRadius: 12 },
});
export default theme;
