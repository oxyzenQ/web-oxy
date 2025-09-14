import { defineConfig } from 'vite'

// https://vitejs.dev/config/
export default defineConfig({
  root: '.',
  server: {
    port: 3000,
    strictPort: true,
    host: true,
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
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['@fontsource/inter']
        }
      },
      input: 'index.html'
    }
  },
  esbuild: {
    drop: ['console', 'debugger']
  }
})
