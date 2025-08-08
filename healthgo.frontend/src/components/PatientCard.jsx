
import React from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Chip from '@mui/material/Chip';
import Sparkline from './Sparkline.jsx';
import { maskName } from '../utils/mask.js';

export default function PatientCard({ patient, showPII = false }) {
  const latest = patient.series.at(-1) || {};
  const hrSeries = patient.series.map(s => s.hr);
  const spo2Series = patient.series.map(s => s.spo2);
  const tempSeries = patient.series.map(s => s.temp);
  const sys = latest.sys ?? '—';
  const dia = latest.dia ?? '—';

  const name = showPII ? patient.name : maskName(patient.name);

  return (
    <Card variant="outlined" sx={{ bgcolor: 'background.paper', borderColor: 'divider' }}>
      <CardContent>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1.5}>
          <Typography variant="h6">
            {name} {patient.age ? <Typography variant="subtitle2" component="span" color="text.secondary"> {patient.age}</Typography> : null}
          </Typography>
          {patient.alert && <Chip size="small" color="secondary" label="ALERT" />}
        </Stack>

        {/* HR */}
        <Stack direction="row" alignItems="center" spacing={2} mb={1}>
          <Typography variant="body2" color="text.secondary" sx={{ width: 62 }}>HR</Typography>
          <Typography variant="h4" color={patient.alert ? 'secondary.main' : 'primary.main'} sx={{ width: 80 }}>
            {latest.hr ?? '—'}
          </Typography>
          <Sparkline values={hrSeries.slice(-40)} />
        </Stack>

        {/* SpO2 */}
        <Stack direction="row" alignItems="center" spacing={2} mb={1}>
          <Typography variant="body2" color="text.secondary" sx={{ width: 62 }}>SpO₂</Typography>
          <Typography variant="h4" color="primary.main" sx={{ width: 80 }}>
            {latest.spo2 ?? '—'}
          </Typography>
          <Sparkline values={spo2Series.slice(-40)} />
        </Stack>

        {/* SYS/DIA */}
        <Stack direction="row" alignItems="center" spacing={2} mb={1}>
          <Typography variant="body2" color="text.secondary" sx={{ width: 62 }}>SYS/DIA</Typography>
          <Typography variant="h5" sx={{ width: 100 }}>
            {sys}/{dia}
          </Typography>
          <Sparkline values={patient.series.slice(-40).map(s => (s.sys ?? 0) - (s.dia ?? 0))} color="#9ca3af" />
        </Stack>

        {/* Temp */}
        <Stack direction="row" alignItems="center" spacing={2}>
          <Typography variant="body2" color="text.secondary" sx={{ width: 62 }}>°C</Typography>
          <Typography variant="h4" color={patient.alert ? 'secondary.main' : 'text.primary'} sx={{ width: 80 }}>
            {latest.temp?.toFixed ? latest.temp.toFixed(1) : latest.temp ?? '—'}
          </Typography>
          <Sparkline values={tempSeries.slice(-40)} />
        </Stack>
      </CardContent>
    </Card>
  );
}
