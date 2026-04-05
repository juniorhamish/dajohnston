import { defineConfig } from "@hey-api/openapi-ts";

export default defineConfig({
  input: "../api/v1/portal.yaml",
  output: {
    path: "src/generated",
    postProcess: ["biome:format"],
  },
});
