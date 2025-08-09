import { useEffect, useState } from "react";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid2";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import DownloadIcon from "@mui/icons-material/Download";
import LogoutIcon from "@mui/icons-material/Logout";
import PatientCard from "../components/PatientCard.jsx";
import { useNavigate } from "react-router-dom";
import { clearAuth } from "../services/authentication.js";
import api from "../services/api.js";
import { openPatientStream } from "../services/sse.js";

export default function Dashboard() {
  const navigate = useNavigate();
  const [patients, setPatients] = useState({});

  useEffect(() => {
    let es;

    (async () => {
      // 1) snapshot inicial
      const { data } = await api.get("/api/v1/patients");
      const map = {};
      data.forEach((p) => {
        map[p.patientId] = p;
      });
      setPatients(map);

      // 2) abre SSE com JWT
      const token = localStorage.getItem("authToken");
      es = openPatientStream(token, (up) => {
        setPatients((prev) => ({
          ...prev,
          [up.patientId]: { ...(prev[up.patientId] || {}), ...up },
        }));
      });
    })();

    return () => es?.close();
  }, []);

  const list = Object.values(patients);

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

  const onLogout = () => {
    // fecha SSE se tiver
    // closeSSE?.();

    clearAuth();
    delete api.defaults?.headers?.common?.Authorization; // se já setou em algum lugar global
    navigate("/login", { replace: true });
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" mb={3}>
        <Typography variant="h4">Multiparametric Monitoring</Typography>
        <Stack direction="row" spacing={3}>
          <Button variant="contained" startIcon={<DownloadIcon />} onClick={onDownload}>
            Download
          </Button>
          <Button variant="contained" onClick={onLogout}>
            <LogoutIcon />
          </Button>
        </Stack>
      </Stack>

      <Grid container spacing={2}>
        {/* {patients.map((p) => (
          <Grid key={p.code} size={{ xs: 12, md: 6, lg: 4 }}>
            <PatientCard patient={p} showPII={false} />
          </Grid>
        ))} */}
        {list.map((p) => (
          <Grid key={p.patientId} size={{ xs: 12, md: 6, lg: 4 }}>
            <PatientCard patient={p} />
          </Grid>
        ))}
      </Grid>

      <Typography variant="caption" color="text.secondary" sx={{ mt: 3, display: "block" }}>
        *LGPD: nomes mascarados por padrão (iniciais). Para ver PII, exigir perfil e base legal de tratamento.
      </Typography>
    </Container>
  );
}
