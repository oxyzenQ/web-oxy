import { defineConfig, loadEnv } from 'vite'
import legacy from '@vitejs/plugin-legacy'
import { visualizer } from 'rollup-plugin-visualizer'
import replace from '@rollup/plugin-replace'

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const isProduction = mode === 'production'
  const isAnalyze = mode === 'analyze'
  
  return {
    root: '.',
    base: './',
    
    // Server configuration for development
    server: {
      port: 3000,
      strictPort: true,
      host: '0.0.0.0',
      open: false,
      cors: true,
      hmr: {
        overlay: true
      }
    },
    
    // Preview configuration
    preview: {
      port: 4173,
      strictPort: true,
      host: '0.0.0.0',
      open: false
    },
    
    // Plugin configuration
    plugins: [
      // Legacy browser support for LTS stability
      legacy({
        targets: [
          'Chrome >= 88',
          'Firefox >= 78', 
          'Safari >= 14',
          'Edge >= 88',
          '> 0.5%',
          'not dead',
          'not IE 11'
        ],
        additionalLegacyPolyfills: ['regenerator-runtime/runtime'],
        renderLegacyChunks: true,
        polyfills: [
          'es.symbol',
          'es.array.filter',
          'es.promise',
          'es.promise.finally'
        ]
      }),
      
      // Environment variable replacement
      replace({
        __MODE__: JSON.stringify(mode),
        __BUILD_TIME__: JSON.stringify(new Date().toISOString()),
        preventAssignment: true
      }),
      
      // Bundle analyzer (only in analyze mode)
      ...(isAnalyze ? [
        visualizer({
          filename: 'dist/stats.html',
          open: true,
          gzipSize: true,
          brotliSize: true
        })
      ] : [])
    ],
    
    // Build configuration optimized for LTS
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: !isProduction,
      minify: isProduction ? 'terser' : false,
      
      // Terser options for production optimization
      terserOptions: isProduction ? {
        compress: {
          drop_console: true,
          drop_debugger: true,
          pure_funcs: ['console.log', 'console.info', 'console.debug'],
          passes: 2
        },
        mangle: {
          safari10: true
        },
        format: {
          safari10: true
        }
      } : {},
      
      // Rollup options for chunking strategy
      rollupOptions: {
        input: 'index.html',
        output: {
          // Optimized chunking for long-term caching
          manualChunks: {
            // Core vendor libraries
            'vendor-core': ['@fontsource/inter'],
            'vendor-icons': ['@fortawesome/fontawesome-free'],
            'vendor-charts': ['chart.js'],
            
            // Application modules
            'app-config': ['./config.js'],
            'app-chart': ['./chart.js']
          },
          
          // File naming for cache busting
          chunkFileNames: isProduction 
            ? 'assets/js/[name]-[hash].js'
            : 'assets/js/[name].js',
          entryFileNames: isProduction
            ? 'assets/js/[name]-[hash].js' 
            : 'assets/js/[name].js',
          assetFileNames: isProduction
            ? 'assets/[ext]/[name]-[hash].[ext]'
            : 'assets/[ext]/[name].[ext]'
        }
      },
      
      // Target will be handled by legacy plugin
      // target: ['es2020', 'chrome88', 'firefox78', 'safari14', 'edge88'],
      
      // Chunk size warnings
      chunkSizeWarningLimit: 1000,
      
      // CSS code splitting
      cssCodeSplit: true,
      
      // Asset inlining threshold
      assetsInlineLimit: 4096
    },
    
    // ESBuild configuration
    esbuild: {
      target: 'es2020',
      drop: isProduction ? ['console', 'debugger'] : [],
      legalComments: 'none'
    },
    
    // CSS configuration
    css: {
      devSourcemap: !isProduction,
      preprocessorOptions: {
        css: {
          charset: false
        }
      }
    },
    
    // Dependency optimization
    optimizeDeps: {
      include: [
        '@fontsource/inter',
        '@fortawesome/fontawesome-free',
        'chart.js'
      ],
      exclude: []
    },
    
    // Define global constants
    define: {
      __APP_VERSION__: JSON.stringify(process.env.npm_package_version || '2.0.0'),
      __BUILD_MODE__: JSON.stringify(mode)
    },
    
    // Resolve configuration
    resolve: {
      alias: {
        '@': new URL('./', import.meta.url).pathname
      }
    }
  }
})
