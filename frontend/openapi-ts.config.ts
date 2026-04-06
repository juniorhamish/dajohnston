import { defineConfig } from "@hey-api/openapi-ts";

export default defineConfig({
  input: "../api/v1/portal.yaml",
  output: "src/generated",
  plugins: [
    { name: "@hey-api/client-next", runtimeConfigPath: "@/api/hey-api" },
  ],
});
