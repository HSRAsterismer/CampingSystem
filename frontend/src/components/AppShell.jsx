import {
  Alert,
  Avatar,
  Badge,
  Button,
  Layout,
  Menu,
  Space,
  Tag,
  Typography,
} from 'antd'
import {
  AppstoreOutlined,
  EnvironmentOutlined,
  FundProjectionScreenOutlined,
  LogoutOutlined,
  RadarChartOutlined,
  ScheduleOutlined,
} from '@ant-design/icons'
import { observer } from 'mobx-react-lite'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import { useStore } from '../stores/StoreProvider'

const { Header, Sider, Content } = Layout
const { Title, Text } = Typography

const menuItems = [
  { key: '/', icon: <FundProjectionScreenOutlined />, label: '驾驶舱总览' },
  { key: '/sites', icon: <EnvironmentOutlined />, label: '露营点地图' },
  { key: '/reservations', icon: <ScheduleOutlined />, label: '在线预订中心' },
  { key: '/payments', icon: <AppstoreOutlined />, label: '支付结算台' },
  { key: '/observations', icon: <RadarChartOutlined />, label: '生态观察记录' },
]

const AppShell = observer(() => {
  const store = useStore()
  const navigate = useNavigate()
  const location = useLocation()

  const currentKey =
    menuItems.find((item) => item.key !== '/' && location.pathname.startsWith(item.key))?.key ||
    (location.pathname === '/' ? '/' : menuItems[0].key)

  return (
    <Layout className="app-shell">
      <Sider breakpoint="lg" collapsedWidth="0" width={250} className="side-panel">
        <div className="brand-panel">
          <div className="brand-chip">CAMPING SYSTEM</div>
          <Title level={3} className="brand-title">
            野外露营点与生态观察记录平台
          </Title>
          <Text className="brand-subtitle">
            预订、风控、结算、观测、地图、可视化一体联动
          </Text>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[currentKey]}
          items={menuItems}
          className="tech-menu"
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header className="top-header">
          <div>
            <div className="header-kicker">实时运营坐席</div>
            <Title level={3} className="header-title">
              {dayjs().format('YYYY年MM月DD日 HH:mm')} · 营地运营态势
            </Title>
          </div>
          <Space size="middle">
            <Badge
              count={store.dashboard?.overview?.activeFraudAlerts || 0}
              className="header-badge"
              overflowCount={99}
            >
              <Alert
                type="warning"
                showIcon
                message="防刷单监测"
                description="高频请求与异常订单正持续被拦截或重点监测"
                className="header-alert"
              />
            </Badge>
            <Space className="profile-chip">
              <Avatar size={42}>{store.user?.displayName?.slice(0, 1)}</Avatar>
              <div>
                <div className="profile-name">{store.user?.displayName}</div>
                <Tag color={store.isAdmin ? 'cyan' : 'gold'}>{store.user?.role}</Tag>
              </div>
            </Space>
            <Button
              type="primary"
              ghost
              icon={<LogoutOutlined />}
              onClick={() => {
                store.logout()
                navigate('/login')
              }}
            >
              退出
            </Button>
          </Space>
        </Header>
        <Content className="page-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
})

export default AppShell
