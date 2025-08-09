// src/services/authentication.js
import { redirect } from "react-router-dom";

const TOKEN_KEY = "authToken";

export const isAuthenticated = () => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) throw redirect("/"); // já logado? manda pro dashboard
  return null;
};

export const handleVerificationProtected = () => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) throw redirect("/login"); // sem token? manda pro login
  return null;
};

// helpers opcionais
export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const clearAuth = () => {
  localStorage.removeItem("authToken");
  localStorage.removeItem("authRole");
  localStorage.removeItem("authUser");
};
