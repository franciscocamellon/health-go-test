import { createBrowserRouter, createRoutesFromElements, Route, RouterProvider } from "react-router-dom";

import { isAuthenticated, handleVerificationProtected } from "../services/authentication";
import Dashboard from "../pages/Dashboard";
import LogIn from "../pages/LogIn";

const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/">
      <Route index element={<Dashboard />} loader={() => handleVerificationProtected()} />
      <Route path="login" element={<LogIn />} loader={() => isAuthenticated()} />
    </Route>
  )
);

export default router;
