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
import { RadarChartOutlined } from '@ant-design/icons'
import { useStore } from '../stores/StoreProvider'

const { Title, Paragraph } = Typography

const categoryOptions = [
  { label: '鸟类', value: '鸟类' },
  { label: '兽类', value: '兽类' },
  { label: '昆虫', value: '昆虫' },
  { label: '植物', value: '植物' },
  { label: '两栖爬行', value: '两栖爬行' },
]

const statusColors = {
  SUBMITTED: 'gold',
  VERIFIED: 'green',
}

const ObservationsPage = observer(() => {
  const store = useStore()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [form] = Form.useForm()

  useEffect(() => {
    if (!store.observations.length) {
      store.fetchObservations()
    }
    if (!store.sites.length) {
      store.fetchSites({})
    }
  }, [store])

  const verifiedCount = store.observations.filter((item) => item.status === 'VERIFIED').length
  const submittedCount = store.observations.filter((item) => item.status === 'SUBMITTED').length

  const categoryMap = new Map()
  store.observations.forEach((item) => {
    categoryMap.set(item.category, (categoryMap.get(item.category) || 0) + 1)
  })
  const categoryStats = Array.from(categoryMap.entries())

  const columns = [
    {
      title: '物种',
      render: (_, record) => (
        <div>
          <div className="table-title">{record.speciesName}</div>
          <div className="table-subtitle">
            {record.category} · {record.siteName}
          </div>
        </div>
      ),
    },
    {
      title: '观察员',
      dataIndex: 'observerName',
    },
    {
      title: '数量',
      dataIndex: 'quantity',
    },
    {
      title: '时间',
      dataIndex: 'observationTime',
    },
    {
      title: '生态评分',
      dataIndex: 'environmentalScore',
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (value) => <Tag color={statusColors[value]}>{value}</Tag>,
    },
    {
      title: '操作',
      render: (_, record) =>
        store.isAdmin && record.status === 'SUBMITTED' ? (
          <Button size="small" onClick={() => store.verifyObservation(record.id)}>
            审核通过
          </Button>
        ) : null,
    },
  ]

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      await store.createObservation({
        ...values,
        observationTime: values.observationTime.format('YYYY-MM-DDTHH:mm:ss'),
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
          <div className="page-kicker">生态观察记录台</div>
          <Title level={2} style={{ marginBottom: 8 }}>
            观察记录采集、审核与分类沉淀
          </Title>
          <Paragraph className="page-description">
            观察员或露营用户可以在这里提交物种、数量、栖息地、时间和环境评分。管理员可对用户上报记录进行审核，形成可信生态数据库。
          </Paragraph>
        </div>
        <Button type="primary" icon={<RadarChartOutlined />} onClick={() => setDrawerOpen(true)}>
          新增观察
        </Button>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="观察总数" value={store.observations.length} suffix="条" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="已核验" value={verifiedCount} suffix="条" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="待审核" value={submittedCount} suffix="条" />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={8}>
          <Card className="glass-card" title="分类速览">
            <Space direction="vertical" style={{ width: '100%' }}>
              {categoryStats.map(([name, value]) => (
                <div className="category-line" key={name}>
                  <span>{name}</span>
                  <Tag color="cyan">{value} 条</Tag>
                </div>
              ))}
            </Space>
          </Card>
        </Col>
        <Col xs={24} xl={16}>
          <Card className="glass-card" title="生态记录总表">
            <Table rowKey="id" columns={columns} dataSource={store.observations} pagination={{ pageSize: 6 }} scroll={{ x: 980 }} />
          </Card>
        </Col>
      </Row>

      <Drawer
        title="新增生态观察"
        open={drawerOpen}
        width={520}
        onClose={() => setDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            <Button type="primary" onClick={handleSubmit}>
              提交
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item name="siteId" label="露营点" rules={[{ required: true, message: '请选择露营点' }]}>
            <Select options={store.siteOptions} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="speciesName" label="物种名称" rules={[{ required: true, message: '请输入物种名称' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="category" label="分类" rules={[{ required: true, message: '请选择分类' }]}>
                <Select options={categoryOptions} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="quantity" label="数量" rules={[{ required: true, message: '请输入数量' }]}>
                <InputNumber style={{ width: '100%' }} min={1} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="weather" label="天气">
                <Input />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="observationTime" label="观察时间" rules={[{ required: true, message: '请选择观察时间' }]}>
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="habitat" label="栖息地">
            <Input />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="rarityLevel" label="稀有度">
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="environmentalScore" label="环境评分">
                <InputNumber style={{ width: '100%' }} min={1} max={100} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="coordinates" label="坐标">
            <Input placeholder="例如：30.638200,119.675900" />
          </Form.Item>
          <Form.Item name="photoUrl" label="照片链接">
            <Input />
          </Form.Item>
          <Form.Item name="notes" label="观察备注">
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  )
})

export default ObservationsPage
