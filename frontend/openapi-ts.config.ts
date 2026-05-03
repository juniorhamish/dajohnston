import { defineConfig } from "@hey-api/openapi-ts";

export default defineConfig([
  {
    input: "../api/v1/portal.yaml",
    output: {
      clean: false,
      path: "src/generated",
    },
    plugins: [
      { name: "@hey-api/client-next", runtimeConfigPath: "@/api/hey-api" },
    ],
  },
  {
    input: "../api/v1/spice-tracker.yaml",
    output: {
      clean: false,
      path: "src/generated/spice-tracker",
    },
    plugins: [
      { name: "@hey-api/client-next", runtimeConfigPath: "@/api/hey-api" },
    ],
  },
]);
