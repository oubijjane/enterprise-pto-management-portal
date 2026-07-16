import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      devOptions: {
        enabled: true
      },
      // The plugin will generate the manifest.json for you based on this object:
      manifest: {
    id: '/', // Fixes the missing ID warning
    name: 'Time Away Tracker',
    short_name: 'TimeAway',
    description: 'App for tracking vacations requests',
    theme_color: '#ffffff',
    background_color: '#ffffff',
    display: 'standalone',
    start_url: '/',
    icons: [
      // Fixes the fatal icon errors (ensure these files exist in public/)
      {
        "src": "/timeAway-192x192.png", // Make sure this 192x192 file is in public/
        "sizes": "192x192",
        "type": "image/png",
        "purpose": "any maskable"
    },
      {
        src: '/timeAway.png',
        sizes: '512x512',
        type: 'image/png',
        purpose: 'any'
      }
    ]
  }
    })
  ],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    },
    host: true, // allow network access
    allowedHosts: ['.ngrok-free.dev', 'verauto-tracker.loca.lt','.trycloudflare.com', 'verautomaroc.com']
  }
})
