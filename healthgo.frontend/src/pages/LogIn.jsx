import { useState } from "react";
import { Box, Button, Card, CardContent, CircularProgress, TextField, Typography, Alert } from "@mui/material";
import { login } from "../services/auth";
import { useNavigate, useLocation } from "react-router-dom";

export default function LogIn() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || "/";

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login({ username, password });
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.response?.data || "Falha no login");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: "100vh", display: "grid", placeItems: "center", bgcolor: "background.default", p: 2 }}>
      <Card sx={{ width: 380 }}>
        <CardContent>
          <Typography variant="h5" fontWeight={700} mb={2}>
            Entrar
          </Typography>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {String(error)}
            </Alert>
          )}
          <Box component="form" onSubmit={onSubmit}>
            <TextField
              label="Username"
              fullWidth
              margin="normal"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
            />
            <TextField
              label="Password"
              type="password"
              fullWidth
              margin="normal"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
            <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }} disabled={loading}>
              {loading ? <CircularProgress size={22} /> : "Login"}
            </Button>
          </Box>
          <Box mt={2}>
            <Typography variant="body2" mb={2}>
              Para acesso como médico utilize username: <strong>medico</strong> e senha: <strong>medico</strong>
            </Typography>
            <Typography variant="body2" mb={2}>
              Para acesso como visitante utilize username: <strong>visitante</strong> e senha:{" "}
              <strong>visitante</strong>
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
