import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Standalone output for Docker — produces a self-contained server.js
  output: "standalone",
  turbopack: {},
};

export default nextConfig;
