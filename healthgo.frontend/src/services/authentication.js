import { redirect } from "react-router-dom";

const TOKEN_KEY = "authToken";

export const isAuthenticated = () => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) throw redirect("/");
  return null;
};

export const handleVerificationProtected = () => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) throw redirect("/login");
  return null;
};

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const clearAuth = () => {
  localStorage.removeItem("authToken");
  localStorage.removeItem("authRole");
  localStorage.removeItem("authUser");
};
