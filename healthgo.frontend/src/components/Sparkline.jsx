
import React from 'react';
import Box from '@mui/material/Box';

export default function Sparkline({ values, width = 140, height = 36, color = 'currentColor' }) {
  if (!values?.length) return null;
  const min = Math.min(...values);
  const max = Math.max(...values);
  const norm = values.map(v => (v - min) / Math.max(1e-6, max - min));
  const stepX = width / Math.max(1, values.length - 1);
  const d = norm.map((y, i) => `${i === 0 ? 'M' : 'L'}${i * stepX},${height - y * height}`).join(' ');
  return (
    <Box component="svg" width={width} height={height} sx={{ opacity: 0.9, color }}>
      <path d={d} fill="none" stroke="currentColor" strokeWidth={2} />
    </Box>
  );
}
