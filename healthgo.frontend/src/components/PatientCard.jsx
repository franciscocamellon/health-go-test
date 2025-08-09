import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import Chip from "@mui/material/Chip";
import Sparkline from "./Sparkline.jsx";

export default function PatientCard({ patient }) {
  const latest = patient.status;
  const hrSeries = patient.heartRate;
  const spo2Series = patient.spo2;
  const tempSeries = patient.temperature;
  const sys = patient.systolicPressure ?? "—";
  const dia = patient.diastolicPressure ?? "—";

  const name = patient.displayName;

  return (
    <Card variant="outlined" sx={{ bgcolor: "background.paper", borderColor: "divider" }}>
      <CardContent>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1.5}>
          <Typography variant="h6">
            {name}{" "}
            {patient.age ? (
              <Typography variant="subtitle2" component="span" color="text.secondary">
                {" "}
                {patient.age}
              </Typography>
            ) : null}
          </Typography>
          {patient.alert && <Chip size="small" color="secondary" label="ALERT" />}
        </Stack>

        {/* HR */}
        <Stack direction="row" alignItems="center" spacing={2} mb={1}>
          <Typography variant="body2" color="text.secondary" sx={{ width: 62 }}>
            HR
          </Typography>
          <Typography variant="h4" color={patient.alert ? "secondary.main" : "primary.main"} sx={{ width: 80 }}>
            {latest.hr ?? "—"}
          </Typography>
          <Sparkline values={hrSeries} />
        </Stack>

        {/* SpO2 */}
        <Stack direction="row" alignItems="center" spacing={2} mb={1}>
          <Typography variant="body2" color="text.secondary" sx={{ width: 62 }}>
            SpO₂
          </Typography>
          <Typography variant="h4" color="primary.main" sx={{ width: 80 }}>
            {latest.spo2 ?? "—"}
          </Typography>
          <Sparkline values={spo2Series} />
        </Stack>

        {/* SYS/DIA */}
        <Stack direction="row" alignItems="center" spacing={2} mb={1}>
          <Typography variant="body2" color="text.secondary" sx={{ width: 62 }}>
            SYS/DIA
          </Typography>
          <Typography variant="h5" sx={{ width: 100 }}>
            {sys}/{dia}
          </Typography>
          <Sparkline values={patient} color="#9ca3af" />
        </Stack>

        {/* Temp */}
        <Stack direction="row" alignItems="center" spacing={2}>
          <Typography variant="body2" color="text.secondary" sx={{ width: 62 }}>
            °C
          </Typography>
          <Typography variant="h4" color={patient.alert ? "secondary.main" : "text.primary"} sx={{ width: 80 }}>
            {latest.temp?.toFixed ? latest.temp.toFixed(1) : latest.temp ?? "—"}
          </Typography>
          <Sparkline values={tempSeries} />
        </Stack>
      </CardContent>
    </Card>
  );
}
