import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }
          if (id.includes('echarts')) {
            return 'charts-vendor'
          }
          if (id.includes('leaflet') || id.includes('react-leaflet')) {
            return 'map-vendor'
          }
          if (id.includes('antd') || id.includes('@ant-design')) {
            return 'antd-vendor'
          }
          if (id.includes('react') || id.includes('mobx') || id.includes('axios')) {
            return 'core-vendor'
          }
          return 'misc-vendor'
        },
      },
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
})
