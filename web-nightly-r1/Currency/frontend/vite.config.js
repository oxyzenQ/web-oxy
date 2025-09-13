import { defineConfig } from 'vite'

// https://vitejs.dev/config/
export default defineConfig({
  root: '.',
  server: {
    port: 3000,
    strictPort: true,
    open: false
  },
  preview: {
    port: 3000,
    strictPort: true,
    open: false
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild',
    rollupOptions: {
      input: 'index.html'
    }
  },
  esbuild: {
    drop: process.env.NODE_ENV === 'production' ? ['console', 'debugger'] : []
  }
})
