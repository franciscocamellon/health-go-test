import { EventSourcePolyfill } from "event-source-polyfill";

export function openPatientStream(token, onUpdate) {
  const url = `${import.meta.env.VITE_API_URL}/api/v1/patients/stream`;

  const es = new EventSourcePolyfill(url, {
    headers: { Authorization: `Bearer ${token}` },
    heartbeatTimeout: 60_000,
    withCredentials: false,
  });

  es.addEventListener("patient-update", (ev) => {
    try {
      const data = JSON.parse(ev.data);
      onUpdate?.(data);
    } catch {}
  });

  es.addEventListener("heartbeat", () => {
    console.debug("heartbeat");
  });

  es.onerror = () => {
    // o polyfill tenta reconectar sozinho
  };

  return es;
}
