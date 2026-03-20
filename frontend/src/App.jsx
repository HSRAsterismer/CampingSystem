import { ConfigProvider, Result, Spin, theme } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { observer } from 'mobx-react-lite'
import { lazy, Suspense, useEffect } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import AppShell from './components/AppShell'
import { useStore } from './stores/StoreProvider'

const LoginPage = lazy(() => import('./pages/LoginPage'))
const DashboardPage = lazy(() => import('./pages/DashboardPage'))
const SitesPage = lazy(() => import('./pages/SitesPage'))
const ReservationsPage = lazy(() => import('./pages/ReservationsPage'))
const PaymentsPage = lazy(() => import('./pages/PaymentsPage'))
const ObservationsPage = lazy(() => import('./pages/ObservationsPage'))

const RouteLoader = () => (
  <div className="loading-screen">
    <Spin size="large" />
  </div>
)

const ProtectedRoutes = observer(() => {
  const store = useStore()

  if (store.bootstrapping) {
    return (
      <div className="loading-screen">
        <Spin size="large" />
      </div>
    )
  }

  if (!store.isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/sites" element={<SitesPage />} />
        <Route path="/reservations" element={<ReservationsPage />} />
        <Route path="/payments" element={<PaymentsPage />} />
        <Route path="/observations" element={<ObservationsPage />} />
      </Route>
      <Route
        path="*"
        element={<Result status="404" title="页面不存在" subTitle="请返回功能导航继续操作" />}
      />
    </Routes>
  )
})

const App = observer(() => {
  const store = useStore()

  useEffect(() => {
    store.bootstrap()
  }, [store])

  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: '#35f4c5',
          colorInfo: '#4fd7ff',
          colorBgBase: '#07111f',
          borderRadius: 18,
          fontFamily: '"Segoe UI", "Microsoft YaHei", sans-serif',
        },
      }}
    >
      <BrowserRouter>
        <Suspense fallback={<RouteLoader />}>
          <Routes>
            <Route path="/login" element={store.isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />
            <Route path="/*" element={<ProtectedRoutes />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </ConfigProvider>
  )
})

export default App
