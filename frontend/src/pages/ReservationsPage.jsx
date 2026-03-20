import {
  Button,
  Card,
  Col,
  DatePicker,
  Drawer,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Statistic,
  Table,
  Tag,
  Typography,
  message,
} from 'antd'
import { observer } from 'mobx-react-lite'
import { useEffect, useState } from 'react'
import { PlusOutlined } from '@ant-design/icons'
import { useStore } from '../stores/StoreProvider'

const { RangePicker } = DatePicker
const { Title, Paragraph } = Typography

const statusColors = {
  PENDING_PAYMENT: 'gold',
  CONFIRMED: 'green',
  CANCELLED: 'red',
  REJECTED: 'volcano',
  COMPLETED: 'blue',
}

const riskColors = {
  LOW: 'green',
  MEDIUM: 'orange',
  HIGH: 'red',
}

const ReservationsPage = observer(() => {
  const store = useStore()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [form] = Form.useForm()

  useEffect(() => {
    if (!store.reservations.length) {
      store.fetchReservations()
    }
    if (!store.sites.length) {
      store.fetchSites({})
    }
  }, [store])

  const pendingCount = store.reservations.filter((item) => item.status === 'PENDING_PAYMENT').length
  const mediumRiskCount = store.reservations.filter((item) => item.riskLevel === 'MEDIUM').length

  const columns = [
    {
      title: '预订编号',
      dataIndex: 'reservationNo',
      width: 180,
    },
    {
      title: '营地',
      dataIndex: 'siteName',
      render: (_, record) => (
        <div>
          <div className="table-title">{record.siteName}</div>
          <div className="table-subtitle">{record.city}</div>
        </div>
      ),
    },
    {
      title: '日期',
      render: (_, record) => `${record.startDate} 至 ${record.endDate}`,
    },
    {
      title: '人数/帐篷',
      render: (_, record) => `${record.guestCount}人 / ${record.tentCount}顶`,
    },
    {
      title: '金额',
      dataIndex: 'totalAmount',
      render: (value) => `¥${value}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (value) => <Tag color={statusColors[value]}>{value}</Tag>,
    },
    {
      title: '风险',
      render: (_, record) => (
        <Space wrap>
          <Tag color={riskColors[record.riskLevel]}>{record.riskLevel}</Tag>
          {record.riskTags
            ?.split(',')
            .filter(Boolean)
            .map((tag) => <Tag key={tag}>{tag}</Tag>)}
        </Space>
      ),
    },
    {
      title: '操作',
      render: (_, record) => (
        <Space wrap>
          {(record.status === 'PENDING_PAYMENT' || record.status === 'CONFIRMED') && (
            <Button size="small" danger onClick={() => store.cancelReservation(record.id)}>
              取消
            </Button>
          )}
          {store.isAdmin && record.status === 'CONFIRMED' && (
            <Button size="small" onClick={() => store.updateReservationStatus(record.id, 'COMPLETED')}>
              标记完成
            </Button>
          )}
          {store.isAdmin && record.status === 'PENDING_PAYMENT' && (
            <Button size="small" onClick={() => store.updateReservationStatus(record.id, 'REJECTED')}>
              驳回
            </Button>
          )}
        </Space>
      ),
    },
  ]

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      await store.createReservation({
        ...values,
        startDate: values.dateRange[0].format('YYYY-MM-DD'),
        endDate: values.dateRange[1].format('YYYY-MM-DD'),
      })
      setDrawerOpen(false)
      form.resetFields()
    } catch (error) {
      if (error?.message) {
        message.error(error.message)
      }
    }
  }

  return (
    <div className="page-stack">
      <div className="page-hero">
        <div>
          <div className="page-kicker">在线预订中心</div>
          <Title level={2} style={{ marginBottom: 8 }}>
            预订创建、状态跟踪与风险标记
          </Title>
          <Paragraph className="page-description">
            用户可以快速完成预订，系统会自动进行容量校验和防刷单识别。管理员还能在这里查看风险标签、取消订单或将订单标记为完成。
          </Paragraph>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setDrawerOpen(true)}>
          新建预订
        </Button>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="预订总数" value={store.reservations.length} suffix="单" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="待支付预订" value={pendingCount} suffix="单" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="中风险标记" value={mediumRiskCount} suffix="单" />
          </Card>
        </Col>
      </Row>

      <Card className="glass-card" title="预订总表">
        <Table rowKey="id" columns={columns} dataSource={store.reservations} pagination={{ pageSize: 6 }} scroll={{ x: 1080 }} />
      </Card>

      <Drawer
        title="创建预订"
        open={drawerOpen}
        width={520}
        onClose={() => setDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            <Button type="primary" onClick={handleSubmit}>
              提交预订
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item name="siteId" label="选择露营点" rules={[{ required: true, message: '请选择露营点' }]}>
            <Select showSearch options={store.siteOptions} placeholder="请选择露营点" />
          </Form.Item>
          <Form.Item name="dateRange" label="预订日期" rules={[{ required: true, message: '请选择日期' }]}>
            <RangePicker style={{ width: '100%' }} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="guestCount" label="同行人数" rules={[{ required: true, message: '请输入人数' }]}>
                <InputNumber style={{ width: '100%' }} min={1} max={20} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="tentCount" label="帐篷数量" rules={[{ required: true, message: '请输入帐篷数量' }]}>
                <InputNumber style={{ width: '100%' }} min={1} max={10} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="contactName" label="联系人" rules={[{ required: true, message: '请输入联系人' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="contactPhone" label="联系电话" rules={[{ required: true, message: '请输入联系电话' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={4} placeholder="例如：观星、亲子、生态观察等需求" />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  )
})

export default ReservationsPage
