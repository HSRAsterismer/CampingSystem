import {
  Alert,
  Button,
  Card,
  Col,
  Form,
  Input,
  Row,
  Space,
  Statistic,
  Tag,
  Typography,
  message,
} from 'antd'
import { observer } from 'mobx-react-lite'
import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useStore } from '../stores/StoreProvider'

const { Title, Paragraph, Text } = Typography

const demoAccounts = [
  { username: 'admin', password: 'admin123', label: '管理员', desc: '营地配置、风控、结算、审核全量权限' },
  { username: 'camper', password: 'camper123', label: '露营用户', desc: '查询地图、创建预订、发起支付、提交观察' },
  { username: 'eco', password: 'eco123', label: '观察员', desc: '快速记录生态观察并直接进入已核验状态' },
]

const LoginPage = observer(() => {
  const store = useStore()
  const navigate = useNavigate()
  const [submitting, setSubmitting] = useState(false)
  const [form] = Form.useForm()

  const handleLogin = async (values) => {
    try {
      setSubmitting(true)
      await store.login(values)
      message.success('登录成功，欢迎进入营地驾驶舱')
      navigate('/')
    } catch (error) {
      message.error(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-grid" />
      <Row gutter={[24, 24]} className="login-wrap">
        <Col xs={24} xl={14}>
          <div className="hero-panel">
            <Tag className="hero-tag">CAMP OPERATION CORE</Tag>
            <Title className="hero-title">
              面向野外露营点运营与生态观察的
              <span> 高科技一体化平台</span>
            </Title>
            <Paragraph className="hero-desc">
              将露营点地图检索、在线预订、防刷单风控、支付结算、生态记录和可视化决策集成到同一套运营中枢，让营地管理、生态保护和用户体验一起变得可追踪、可量化、可优化。
            </Paragraph>
            <Row gutter={[16, 16]} className="hero-stats">
              <Col xs={24} md={8}>
                <Card className="glass-card">
                  <Statistic title="运营模块" value={6} suffix="个" />
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card className="glass-card">
                  <Statistic title="风控策略" value={4} suffix="层" />
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card className="glass-card">
                  <Statistic title="数据视角" value={360} suffix="°" />
                </Card>
              </Col>
            </Row>
            <div className="hero-list">
              <div className="hero-list-item">
                <div className="hero-list-title">露营点地图联动</div>
                <Text>地图点位、容量余量、生态指数、价格状态同屏联动展示。</Text>
              </div>
              <div className="hero-list-item">
                <div className="hero-list-title">防刷单策略闭环</div>
                <Text>重复预订拦截、短时高频拦截、周末异常高峰标记、IP 爆发监测。</Text>
              </div>
              <div className="hero-list-item">
                <div className="hero-list-title">生态观察驾驶舱</div>
                <Text>按站点、类别、时间与空间位置沉淀生态观察数据资产。</Text>
              </div>
            </div>
          </div>
        </Col>
        <Col xs={24} xl={10}>
          <Card className="login-card">
            <Space direction="vertical" size={18} style={{ width: '100%' }}>
              <div>
                <div className="panel-kicker">统一登录入口</div>
                <Title level={2} style={{ marginBottom: 8 }}>
                  平台身份认证
                </Title>
                <Paragraph style={{ color: 'rgba(230,244,255,0.72)', marginBottom: 0 }}>
                  直接使用系统内置的演示账号即可进入完整业务流。
                </Paragraph>
              </div>
              <Alert
                type="info"
                showIcon
                message="示例账号已自动预置在后端种子数据中"
                description="若首次启动后端，数据库会自动生成演示用户、营地、订单、支付和生态观察数据。"
              />
              <Form layout="vertical" form={form} onFinish={handleLogin}>
                <Form.Item
                  label="用户名"
                  name="username"
                  rules={[{ required: true, message: '请输入用户名' }]}
                >
                  <Input size="large" prefix={<UserOutlined />} placeholder="请输入用户名" />
                </Form.Item>
                <Form.Item
                  label="密码"
                  name="password"
                  rules={[{ required: true, message: '请输入密码' }]}
                >
                  <Input.Password size="large" prefix={<LockOutlined />} placeholder="请输入密码" />
                </Form.Item>
                <Button type="primary" htmlType="submit" block size="large" loading={submitting}>
                  进入平台
                </Button>
              </Form>
              <div>
                <div className="panel-kicker" style={{ marginBottom: 12 }}>
                  快速体验账号
                </div>
                <Space direction="vertical" style={{ width: '100%' }} size={12}>
                  {demoAccounts.map((account) => (
                    <button
                      type="button"
                      key={account.username}
                      className="demo-account"
                      onClick={() =>
                        form.setFieldsValue({
                          username: account.username,
                          password: account.password,
                        })
                      }
                    >
                      <div>
                        <div className="demo-account-title">{account.label}</div>
                        <div className="demo-account-desc">{account.desc}</div>
                      </div>
                      <Tag color="cyan">
                        {account.username} / {account.password}
                      </Tag>
                    </button>
                  ))}
                </Space>
              </div>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  )
})

export default LoginPage
