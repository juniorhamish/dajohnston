/// <reference types="vitest/config" />
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],
  resolve: {
    tsconfigPaths: true,
  },
  test: {
    environment: "jsdom",
    setupFiles: ["./src/vitest-setup.ts"],
    coverage: {
      provider: "istanbul",
      include: ["src/**"],
      exclude: [
        "src/**/*.test.ts",
        "src/**/*.test.tsx",
        "src/next-env.d.ts",
        "src/app/globals.css",
      ],
      reporter: ["text", "lcov"],
    },
  },
});
