import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Empty turbopack config silences the webpack/turbopack conflict warning.
  // File watch polling for WSL is handled via WATCHPACK_POLLING=true in the dev script.
  turbopack: {},
};

export default nextConfig;
