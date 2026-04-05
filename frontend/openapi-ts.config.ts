import {defineConfig} from '@hey-api/openapi-ts';

export default defineConfig({
  input: '../api/v1/portal.yaml',
  output: './src/generated',
  client: 'fetch',
});
