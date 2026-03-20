import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'antd/dist/reset.css'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import 'leaflet/dist/leaflet.css'
import './index.css'
import App from './App.jsx'
import { StoreProvider } from './stores/StoreProvider.jsx'

dayjs.locale('zh-cn')

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <StoreProvider>
      <App />
    </StoreProvider>
  </StrictMode>,
)
