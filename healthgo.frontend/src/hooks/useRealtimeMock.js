
import { useEffect } from 'react';

function clamp(n, a, b) { return Math.max(a, Math.min(b, n)); }

function nextSample(prev) {
  const drift = (min, max, step = 1) => clamp(prev.hr + (Math.random() * 2 - 1) * step, min, max);
  const hr = Math.round(drift(60, 120, 2));
  const spo2 = Math.round(clamp((prev.spo2 ?? 98) + (Math.random() - 0.5) * 0.8, 92, 100));
  const temp = Number(clamp((prev.temp ?? 36.6) + (Math.random() - 0.5) * 0.05, 35.8, 39.0).toFixed(1));
  const sys = Math.round(clamp((prev.sys ?? 120) + (Math.random() - 0.5) * 2.0, 90, 160));
  const dia = Math.round(clamp((prev.dia ?? 80) + (Math.random() - 0.5) * 2.0, 50, 100));
  return { t: Date.now(), hr, spo2, sys, dia, temp };
}

/** 
 * useRealtimeMock
 * - calls setPatients(prev => ...) every 200ms
 * - keeps last 120 samples, sets alert when hr>100 or temp>=37.1
 */
export function useRealtimeMock(setPatients) {
  useEffect(() => {
    const id = setInterval(() => {
      setPatients(prev => prev.map(p => {
        const next = nextSample(p.series[p.series.length - 1]);
        const updated = [...p.series.slice(-120), next];
        const alert = next.hr > 100 || next.temp >= 37.1;
        return { ...p, series: updated, alert };
      }));
    }, 200);
    return () => clearInterval(id);
  }, [setPatients]);
}
