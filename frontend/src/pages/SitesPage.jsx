import {
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Modal,
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
import { CircleMarker, MapContainer, Popup, TileLayer } from 'react-leaflet'
import { EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { useStore } from '../stores/StoreProvider'

const { RangePicker } = DatePicker
const { Title, Paragraph, Text } = Typography

const siteStatusColors = {
  OPEN: 'green',
  MAINTENANCE: 'orange',
  FULL: 'red',
}

const siteStatusText = {
  OPEN: '开放',
  MAINTENANCE: '维护中',
  FULL: '已满',
}

const statusOptions = [
  { label: '开放', value: 'OPEN' },
  { label: '维护中', value: 'MAINTENANCE' },
  { label: '已满', value: 'FULL' },
]

const SitesPage = observer(() => {
  const store = useStore()
  const [queryForm] = Form.useForm()
  const [modalForm] = Form.useForm()
  const [modalOpen, setModalOpen] = useState(false)
  const [editingRecord, setEditingRecord] = useState(null)

  useEffect(() => {
    if (!store.sites.length) {
      store.fetchSites({})
    }
  }, [store])

  const center = !store.sites.length
    ? [35.8617, 104.1954]
    : [Number(store.sites[0].latitude), Number(store.sites[0].longitude)]

  const openSiteCount = store.sites.filter((item) => item.status === 'OPEN').length
  const averageEcoIndex = store.sites.length
    ? Math.round(store.sites.reduce((sum, item) => sum + item.ecoIndex, 0) / store.sites.length)
    : 0

  const columns = [
    {
      title: '露营点',
      dataIndex: 'name',
      render: (_, record) => (
        <div>
          <div className="table-title">{record.name}</div>
          <div className="table-subtitle">
            {record.city} · {record.address}
          </div>
        </div>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (value) => <Tag color={siteStatusColors[value]}>{siteStatusText[value]}</Tag>,
    },
    {
      title: '价格',
      dataIndex: 'basePrice',
      render: (value) => `¥${value}/晚`,
    },
    {
      title: '可用营位',
      dataIndex: 'availableTents',
      render: (value, record) => `${value}/${record.capacity}`,
    },
    {
      title: '景观/生态',
      render: (_, record) => `${record.scenicLevel}星 / ${record.ecoIndex}`,
    },
    {
      title: '占用率',
      dataIndex: 'occupancyRate',
      render: (value) => `${value}%`,
    },
    store.isAdmin
      ? {
          title: '操作',
          render: (_, record) => (
            <Space wrap>
              <Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
                编辑
              </Button>
              <Button size="small" onClick={() => store.updateSiteStatus(record.id, 'OPEN')}>
                开放
              </Button>
              <Button size="small" onClick={() => store.updateSiteStatus(record.id, 'MAINTENANCE')}>
                维护
              </Button>
            </Space>
          ),
        }
      : null,
  ].filter(Boolean)

  const handleSearch = async (values) => {
    try {
      await store.fetchSites({
        keyword: values.keyword,
        city: values.city,
        status: values.status,
        startDate: values.dateRange?.[0]?.format('YYYY-MM-DD'),
        endDate: values.dateRange?.[1]?.format('YYYY-MM-DD'),
      })
    } catch (error) {
      message.error(error.message)
    }
  }

  const handleEdit = (record) => {
    setEditingRecord(record)
    setModalOpen(true)
    modalForm.setFieldsValue({
      ...record,
      facilitiesText: record.facilities?.join('，'),
      tagsText: record.tags?.join('，'),
    })
  }

  const handleCreate = () => {
    setEditingRecord(null)
    setModalOpen(true)
    modalForm.setFieldsValue({
      scenicLevel: 5,
      ecoIndex: 90,
      capacity: 12,
      basePrice: 268,
      status: 'OPEN',
    })
  }

  const handleSubmit = async () => {
    try {
      const values = await modalForm.validateFields()
      await store.saveSite(values, editingRecord?.id)
      setModalOpen(false)
      modalForm.resetFields()
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
          <div className="page-kicker">地图查询与站点配置</div>
          <Title level={2} style={{ marginBottom: 8 }}>
            露营点地图与容量态势
          </Title>
          <Paragraph className="page-description">
            支持按城市、状态、日期范围检索营地，并实时查看价格、可用营位、生态指数和地理坐标。管理员可直接在这里维护站点资料。
          </Paragraph>
        </div>
        <Space wrap>
          <Button icon={<ReloadOutlined />} onClick={() => store.fetchSites(store.siteFilters)}>
            刷新
          </Button>
          {store.isAdmin && (
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新增露营点
            </Button>
          )}
        </Space>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="当前站点数" value={store.sites.length} suffix="个" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="开放站点" value={openSiteCount} suffix="个" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="glass-card">
            <Statistic title="平均生态指数" value={averageEcoIndex} />
          </Card>
        </Col>
      </Row>

      <Card className="glass-card">
        <Form form={queryForm} layout="inline" onFinish={handleSearch} className="filter-form">
          <Form.Item name="keyword">
            <Input placeholder="搜索名称、城市或标签" allowClear style={{ width: 220 }} />
          </Form.Item>
          <Form.Item name="city">
            <Input placeholder="城市" allowClear style={{ width: 160 }} />
          </Form.Item>
          <Form.Item name="status">
            <Select placeholder="状态" allowClear options={statusOptions} style={{ width: 140 }} />
          </Form.Item>
          <Form.Item name="dateRange">
            <RangePicker />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              查询
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={13}>
          <Card className="glass-card" title="空间地图">
            <div className="map-wrap">
              <MapContainer center={center} zoom={4} scrollWheelZoom className="map-panel">
                <TileLayer
                  url="https://rt{s}.map.gtimg.com/tile?z={z}&x={x}&y={-y}&styleid=3&version=297"
                  subdomains={['0', '1', '2', '3']}
                  attribution="Tencent Map"
                />
                {store.sites.map((site) => (
                  <CircleMarker
                    key={site.id}
                    center={[Number(site.latitude), Number(site.longitude)]}
                    radius={10}
                    pathOptions={{
                      color: site.status === 'OPEN' ? '#35f4c5' : site.status === 'FULL' ? '#ff5a6a' : '#ffcf5a',
                      fillOpacity: 0.85,
                    }}
                  >
                    <Popup>
                      <div className="map-popup">
                        <strong>{site.name}</strong>
                        <div>{site.city}</div>
                        <div>价格：¥{site.basePrice}/晚</div>
                        <div>可用营位：{site.availableTents}</div>
                        <div>生态指数：{site.ecoIndex}</div>
                      </div>
                    </Popup>
                  </CircleMarker>
                ))}
              </MapContainer>
            </div>
          </Card>
        </Col>
        <Col xs={24} xl={11}>
          <Card className="glass-card" title="站点情报卡">
            <Space direction="vertical" size={12} style={{ width: '100%' }}>
              {store.sites.map((site) => (
                <div className="site-card" key={site.id}>
                  <div className="site-card-top">
                    <div>
                      <div className="site-card-title">{site.name}</div>
                      <div className="table-subtitle">{site.city}</div>
                    </div>
                    <Tag color={siteStatusColors[site.status]}>{siteStatusText[site.status]}</Tag>
                  </div>
                  <div className="site-card-metrics">
                    <span>¥{site.basePrice}/晚</span>
                    <span>余量 {site.availableTents}/{site.capacity}</span>
                    <span>生态 {site.ecoIndex}</span>
                  </div>
                  <div className="site-card-tags">
                    {site.tags.map((tag) => (
                      <Tag key={tag}>{tag}</Tag>
                    ))}
                  </div>
                  <Text className="site-card-desc">{site.description}</Text>
                </div>
              ))}
            </Space>
          </Card>
        </Col>
      </Row>

      <Card className="glass-card" title="站点明细表">
        <Table rowKey="id" columns={columns} dataSource={store.sites} pagination={{ pageSize: 6 }} scroll={{ x: 980 }} />
      </Card>

      <Modal
        title={editingRecord ? '编辑露营点' : '新增露营点'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        width={860}
      >
        <Form form={modalForm} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="code" label="营地编码" rules={[{ required: true, message: '请输入编码' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="name" label="营地名称" rules={[{ required: true, message: '请输入名称' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="province" label="省份" rules={[{ required: true, message: '请输入省份' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="city" label="城市" rules={[{ required: true, message: '请输入城市' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item name="address" label="详细地址" rules={[{ required: true, message: '请输入地址' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="latitude" label="纬度" rules={[{ required: true, message: '请输入纬度' }]}>
                <InputNumber style={{ width: '100%' }} precision={6} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="longitude" label="经度" rules={[{ required: true, message: '请输入经度' }]}>
                <InputNumber style={{ width: '100%' }} precision={6} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="capacity" label="容量" rules={[{ required: true, message: '请输入容量' }]}>
                <InputNumber style={{ width: '100%' }} min={1} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="basePrice" label="基础价格" rules={[{ required: true, message: '请输入价格' }]}>
                <InputNumber style={{ width: '100%' }} min={1} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
                <Select options={statusOptions} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="scenicLevel" label="景观等级" rules={[{ required: true, message: '请输入景观等级' }]}>
                <InputNumber style={{ width: '100%' }} min={1} max={5} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="ecoIndex" label="生态指数" rules={[{ required: true, message: '请输入生态指数' }]}>
                <InputNumber style={{ width: '100%' }} min={1} max={100} />
              </Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item name="facilitiesText" label="设施" extra="用中文逗号或英文逗号分隔">
                <Input />
              </Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item name="tagsText" label="标签" extra="用中文逗号或英文逗号分隔">
                <Input />
              </Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item name="description" label="介绍">
                <Input.TextArea rows={4} />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  )
})

export default SitesPage
