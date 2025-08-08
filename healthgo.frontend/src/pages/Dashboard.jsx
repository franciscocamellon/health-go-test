import React, { useMemo, useState } from "react";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid2";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import DownloadIcon from "@mui/icons-material/Download";
import PatientCard from "../components/PatientCard.jsx";
import { useRealtimeMock } from "../hooks/useRealtimeMock.js";

function mkInitialSeries(n = 40) {
  const base = { t: Date.now(), hr: 78, spo2: 98, sys: 121, dia: 79, temp: 36.6 };
  const arr = [base];
  const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
  for (let i = 1; i < n; i++) {
    const prev = arr[i - 1];
    const hr = Math.round(clamp(prev.hr + (Math.random() * 2 - 1) * 2, 60, 120));
    const spo2 = Math.round(clamp((prev.spo2 ?? 98) + (Math.random() - 0.5) * 0.8, 92, 100));
    const temp = Number(clamp((prev.temp ?? 36.6) + (Math.random() - 0.5) * 0.05, 35.8, 39.0).toFixed(1));
    const sys = Math.round(clamp((prev.sys ?? 120) + (Math.random() - 0.5) * 2.0, 90, 160));
    const dia = Math.round(clamp((prev.dia ?? 80) + (Math.random() - 0.5) * 2.0, 50, 100));
    arr.push({ t: Date.now(), hr, spo2, sys, dia, temp });
  }
  return arr;
}

export default function Dashboard() {
  const [patients, setPatients] = useState(() => [
    { code: "PAC001", name: "João Silva", age: 65, alert: false, series: mkInitialSeries() },
    { code: "PAC002", name: "Maria Santos", age: 59, alert: true, series: mkInitialSeries() },
    { code: "PAC003", name: "Pedro Oliveira", age: 71, alert: true, series: mkInitialSeries() },
  ]);

  useRealtimeMock(setPatients);

  const onDownload = () => {
    const data = patients.map((p) => ({ code: p.code, latest: p.series.at(-1) }));
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "monitoring-export.json";
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" mb={3}>
        <Typography variant="h4">Multiparametric Monitoring</Typography>
        <Button variant="contained" startIcon={<DownloadIcon />} onClick={onDownload}>
          Download
        </Button>
      </Stack>

      <Grid container spacing={2}>
        {patients.map((p) => (
          <Grid key={p.code} size={{ xs: 12, md: 6, lg: 4 }}>
            <PatientCard patient={p} showPII={false} />
          </Grid>
        ))}
      </Grid>

      <Typography variant="caption" color="text.secondary" sx={{ mt: 3, display: "block" }}>
        *LGPD: nomes mascarados por padrão (iniciais). Para ver PII, exigir perfil e base legal de tratamento.
      </Typography>
    </Container>
  );
}
