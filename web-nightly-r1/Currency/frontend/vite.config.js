import { defineConfig } from 'vite'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  root: '.',
  server: {
    port: 3000,
    strictPort: true,
    host: true,
    open: false,
    cors: true,
    hmr: {
      overlay: false
    }
  },
  preview: {
    port: 3000,
    strictPort: true,
    open: false,
    cors: true
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: process.env.NODE_ENV === 'development',
    minify: 'terser',
    target: 'es2020',
    cssCodeSplit: true,
    chunkSizeWarningLimit: 1000,
    terserOptions: {
      compress: {
        drop_console: process.env.NODE_ENV === 'production',
        drop_debugger: true,
        pure_funcs: ['console.log', 'console.info'],
        passes: 2
      },
      mangle: {
        safari10: true
      }
    },
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html')
      },
      output: {
        manualChunks: {
          // Core libraries
          vendor: ['@fontsource/inter'],
          // Chart functionality
          chart: ['chart.js'],
          // Utils and config
          utils: ['config.js']
        },
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          const ext = info[info.length - 1]
          if (/\.(css)$/.test(assetInfo.name)) {
            return `assets/css/[name]-[hash].${ext}`
          }
          if (/\.(png|jpe?g|svg|gif|tiff|bmp|ico)$/i.test(assetInfo.name)) {
            return `assets/images/[name]-[hash].${ext}`
          }
          return `assets/[name]-[hash].${ext}`
        }
      },
      external: [],
      treeshake: {
        moduleSideEffects: false,
        propertyReadSideEffects: false,
        unknownGlobalSideEffects: false
      }
    }
  },
  esbuild: {
    drop: process.env.NODE_ENV === 'production' ? ['console', 'debugger'] : [],
    legalComments: 'none',
    target: 'es2020'
  },
  optimizeDeps: {
    include: ['@fontsource/inter'],
    exclude: [],
    esbuildOptions: {
      target: 'es2020'
    }
  },
  css: {
    devSourcemap: process.env.NODE_ENV === 'development',
    preprocessorOptions: {
      css: {
        charset: false
      }
    }
  },
  define: {
    __DEV__: process.env.NODE_ENV === 'development',
    __PROD__: process.env.NODE_ENV === 'production'
  }
})
