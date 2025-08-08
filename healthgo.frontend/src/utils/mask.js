export function maskName(full) {
  if (!full) return "—";
  const parts = full.trim().split(/\s+/);
  const first = parts[0]?.[0]?.toUpperCase() ?? "";
  const last = parts.length > 1 ? parts[parts.length - 1][0]?.toUpperCase() : "";
  return `${first}. ${last}.`;
}
