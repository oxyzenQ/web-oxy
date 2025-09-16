import { defineConfig } from 'vite'
import legacy from '@vitejs/plugin-legacy'
import { visualizer } from 'rollup-plugin-visualizer'
import replace from '@rollup/plugin-replace'

// Production-optimized Vite configuration for LTS stability
export default defineConfig({
  root: '.',
  base: './',
  mode: 'production',
  
  // Plugin configuration for production
  plugins: [
    // Legacy browser support with maximum compatibility
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
        'es.promise.finally',
        'es.object.assign'
      ]
    }),
    
    // Environment variable replacement
    replace({
      __MODE__: '"production"',
      __BUILD_TIME__: JSON.stringify(new Date().toISOString()),
      __APP_VERSION__: '"2.0.0"',
      preventAssignment: true
    }),
    
    // Bundle analyzer for optimization insights
    visualizer({
      filename: 'dist/bundle-analysis.html',
      open: false,
      gzipSize: true,
      brotliSize: true,
      template: 'treemap'
    })
  ],
  
  // Optimized build configuration
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    
    // Advanced Terser optimization
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.warn'],
        passes: 3,
        unsafe: false,
        unsafe_comps: false,
        unsafe_math: false,
        unsafe_proto: false,
        unsafe_regexp: false,
        unsafe_undefined: false
      },
      mangle: {
        safari10: true,
        keep_classnames: false,
        keep_fnames: false
      },
      format: {
        safari10: true,
        comments: false
      }
    },
    
    // Optimized rollup configuration
    rollupOptions: {
      input: 'index.html',
      output: {
        // Strategic chunking for optimal caching
        manualChunks: {
          // Vendor chunks (rarely change)
          'vendor-fonts': ['@fontsource/inter'],
          'vendor-icons': ['@fortawesome/fontawesome-free'],
          'vendor-charts': ['chart.js'],
          
          // App chunks (change more frequently)
          'app-core': ['./main.js'],
          'app-config': ['./config.js'],
          'app-chart': ['./chart.js']
        },
        
        // Optimized file naming for long-term caching
        chunkFileNames: 'assets/js/[name]-[hash:8].js',
        entryFileNames: 'assets/js/[name]-[hash:8].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          const ext = info[info.length - 1]
          if (/\.(css)$/.test(assetInfo.name)) {
            return `assets/css/[name]-[hash:8].[ext]`
          }
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/.test(assetInfo.name)) {
            return `assets/img/[name]-[hash:8].[ext]`
          }
          if (/\.(woff2?|eot|ttf|otf)$/.test(assetInfo.name)) {
            return `assets/fonts/[name]-[hash:8].[ext]`
          }
          return `assets/[ext]/[name]-[hash:8].[ext]`
        }
      }
    },
    
    // Modern browser targets for optimal performance
    target: ['es2020', 'chrome88', 'firefox78', 'safari14', 'edge88'],
    
    // Performance optimizations
    chunkSizeWarningLimit: 800,
    cssCodeSplit: true,
    assetsInlineLimit: 2048,
    
    // Compression settings
    reportCompressedSize: true,
    
    // Rollup external dependencies (if needed)
    external: []
  },
  
  // ESBuild configuration for maximum optimization
  esbuild: {
    target: 'es2020',
    drop: ['console', 'debugger'],
    legalComments: 'none',
    minifyIdentifiers: true,
    minifySyntax: true,
    minifyWhitespace: true
  },
  
  // CSS optimization
  css: {
    devSourcemap: false,
    preprocessorOptions: {
      css: {
        charset: false
      }
    }
  },
  
  // Dependency pre-bundling optimization
  optimizeDeps: {
    include: [
      '@fontsource/inter',
      '@fortawesome/fontawesome-free',
      'chart.js'
    ],
    exclude: [],
    force: false
  },
  
  // Global constants for production
  define: {
    __APP_VERSION__: '"2.0.0"',
    __BUILD_MODE__: '"production"',
    __DEV__: false,
    'process.env.NODE_ENV': '"production"'
  }
})
