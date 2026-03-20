import {
  Button,
  Card,
  Col,
  Drawer,
  Form,
  Input,
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
import { CreditCardOutlined } from '@ant-design/icons'
import { useStore } from '../stores/StoreProvider'

const { Title, Paragraph } = Typography

const statusColors = {
  CREATED: 'default',
  PAID: 'green',
  SETTLED: 'cyan',
  FAILED: 'red',
  REFUNDED: 'volcano',
}

const channelOptions = [
  { label: '微信支付', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '银行卡', value: 'BANK_CARD' },
]

const PaymentsPage = observer(() => {
  const store = useStore()
  const [payDrawerOpen, setPayDrawerOpen] = useState(false)
  const [settleDrawerOpen, setSettleDrawerOpen] = useState(false)
  const [settleTarget, setSettleTarget] = useState(null)
  const [payForm] = Form.useForm()
  const [settleForm] = Form.useForm()

  useEffect(() => {
    if (!store.payments.length) {
      store.fetchPayments()
    }
    if (!store.reservations.length) {
      store.fetchReservations()
    }
  }, [store])

  const revenue = store.payments
    .filter((item) => item.status === 'PAID' || item.status === 'SETTLED')
    .reduce((sum, item) => sum + Number(item.amount), 0)

  const pendingSettlement = store.payments.filter((item) => item.status === 'PAID').length

  const columns = [
    {
      title: '支付单号',
      dataIndex: 'orderNo',
      width: 180,
    },
    {
      title: '营地/预订',
      render: (_, record) => (
        <div>
          <div className="table-title">{record.siteName}</div>
          <div className="table-subtitle">{record.reservationNo}</div>
        </div>
      ),
    },
    {
      title: '支付方式',
      dataIndex: 'channel',
    },
    {
      title: '金额',
      dataIndex: 'amount',
      render: (value) => `¥${value}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (value) => <Tag color={statusColors[value]}>{value}</Tag>,
    },
    {
      title: '交易流水',
      dataIndex: 'transactionNo',
    },
    {
      title: '时间',
      render: (_, record) => record.settledAt || record.paidAt || record.createdAt,
    },
    store.isAdmin
      ? {
          title: '操作',
          render: (_, record) => (
            <Space>
              {record.status === 'PAID' && (
                <Button
                  size="small"
                  onClick={() => {
                    setSettleTarget(record)
                    setSettleDrawerOpen(true)
                    settleForm.resetFields()
                  }}
                >
                  发起结算
                </Button>
              )}
            </Space>
          ),
        }
      : null,
  ].filter(Boolean)

  const pendingReservationOptions = store.pendingReservations.map((item) => ({
    label: `${item.siteName} · ${item.startDate} 至 ${item.endDate} · ¥${item.totalAmount}`,
    value: item.id,
  }))

  const handlePay = async () => {
    try {
      const values = await payForm.validateFields()
      await store.payReservation(values)
      setPayDrawerOpen(false)
      payForm.resetFields()
    } catch (error) {
      if (error?.message) {
        message.error(error.message)
      }
    }
  }

  const handleSettle = async () => {
    try {
      const values = await settleForm.validateFields()
      await store.settlePayment(settleTarget.id, values.note)
      setSettleDrawerOpen(false)
      setSettleTarget(null)
      settleForm.resetFields()
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
          <div className="page-kicker">支付结算台</div>
          <Title level={2} style={{ marginBottom: 8 }}>
            在线支付与清算处理
          </Title>
          <Paragraph className="page-description">
            平台支持基于预订单发起模拟支付，并将支付结果同步回预订状态。管理员可对已支付订单继续执行结算操作，形成资金闭环。
          </Paragraph>
        </div>
        <Button type="primary" icon={<CreditCardOutlined />} onClick={() => setPayDrawerOpen(true)}>
          发起支付
        </Button>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="支付订单数" value={store.payments.length} suffix="单" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="累计到账" value={revenue} prefix="¥" precision={2} />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="待结算订单" value={pendingSettlement} suffix="单" />
          </Card>
        </Col>
      </Row>

      <Card className="glass-card" title="支付流水">
        <Table rowKey="id" columns={columns} dataSource={store.payments} pagination={{ pageSize: 6 }} scroll={{ x: 980 }} />
      </Card>

      <Drawer
        title="发起支付"
        open={payDrawerOpen}
        width={460}
        onClose={() => setPayDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setPayDrawerOpen(false)}>取消</Button>
            <Button type="primary" onClick={handlePay}>
              支付
            </Button>
          </Space>
        }
      >
        <Form form={payForm} layout="vertical">
          <Form.Item
            name="reservationId"
            label="待支付预订单"
            rules={[{ required: true, message: '请选择预订单' }]}
          >
            <Select options={pendingReservationOptions} placeholder="请选择预订单" />
          </Form.Item>
          <Form.Item name="channel" label="支付渠道" rules={[{ required: true, message: '请选择渠道' }]}>
            <Select options={channelOptions} />
          </Form.Item>
        </Form>
      </Drawer>

      <Drawer
        title="订单结算"
        open={settleDrawerOpen}
        width={420}
        onClose={() => setSettleDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setSettleDrawerOpen(false)}>取消</Button>
            <Button type="primary" onClick={handleSettle}>
              确认结算
            </Button>
          </Space>
        }
      >
        <div className="drawer-brief">
          当前订单：{settleTarget?.orderNo}
          <br />
          站点：{settleTarget?.siteName}
        </div>
        <Form form={settleForm} layout="vertical">
          <Form.Item name="note" label="结算备注">
            <Input.TextArea rows={4} placeholder="例如：已纳入日终清算批次，人工复核通过" />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  )
})

export default PaymentsPage
