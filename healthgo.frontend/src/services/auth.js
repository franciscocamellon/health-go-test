import api from "./api";

const USER_FIELD = "username";

export async function login({ username, password }) {
  const payload = { [USER_FIELD]: username, password };
  const { data } = await api.post("/auth/login", payload);
  const token = data?.jwt;
  if (!token) throw new Error("Token não encontrado na resposta");
  localStorage.setItem("authToken", token);

  try {
    const claims = JSON.parse(atob(token.split(".")[1]));
    localStorage.setItem("authRole", claims.role);
    localStorage.setItem("authUser", claims.username || claims.sub);
  } catch {}

  return token;
}

export function logout() {
  localStorage.removeItem("authToken");
  localStorage.removeItem("authRole");
  localStorage.removeItem("authUser");
  delete api.defaults.headers.common.Authorization;
}

export function isAuthenticated() {
  return !!localStorage.getItem("authToken");
}
