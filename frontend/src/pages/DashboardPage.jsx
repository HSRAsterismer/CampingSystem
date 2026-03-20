import { Alert, Card, Col, List, Progress, Row, Space, Spin, Statistic, Tag, Typography } from 'antd'
import { observer } from 'mobx-react-lite'
import { useEffect } from 'react'
import ReactECharts from 'echarts-for-react'
import { useStore } from '../stores/StoreProvider'

const { Title, Paragraph, Text } = Typography

const DashboardPage = observer(() => {
  const store = useStore()
  const dashboard = store.dashboard

  useEffect(() => {
    if (!dashboard) {
      store.fetchDashboard()
    }
  }, [dashboard, store])

  if (!dashboard) {
    return (
      <div className="loading-screen">
        <Spin size="large" />
      </div>
    )
  }

  const trendOption = {
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: '#d9f7ff' } },
    grid: { top: 40, left: 30, right: 20, bottom: 28 },
    xAxis: {
      type: 'category',
      data: dashboard.trends.map((item) => item.date.slice(5)),
      axisLine: { lineStyle: { color: '#5e85ad' } },
    },
    yAxis: [
      {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      },
      {
        type: 'value',
        axisLine: { show: false },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: '预订量',
        type: 'line',
        smooth: true,
        data: dashboard.trends.map((item) => item.reservations),
        lineStyle: { color: '#35f4c5' },
        areaStyle: { color: 'rgba(53,244,197,0.16)' },
      },
      {
        name: '观察记录',
        type: 'bar',
        data: dashboard.trends.map((item) => item.observations),
        itemStyle: { color: '#4fd7ff' },
      },
      {
        name: '营收',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: dashboard.trends.map((item) => Number(item.revenue)),
        lineStyle: { color: '#ffcf5a' },
      },
    ],
  }

  const categoryOption = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#d9f7ff' } },
    series: [
      {
        type: 'pie',
        radius: ['45%', '72%'],
        data: dashboard.observationCategories.map((item) => ({ name: item.name, value: item.value })),
        label: { color: '#e6f4ff' },
      },
    ],
  }

  const rankingOption = {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, left: 100, right: 20, bottom: 20 },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
    },
    yAxis: {
      type: 'category',
      data: dashboard.siteRankings.map((item) => item.siteName),
      axisLabel: { color: '#e6f4ff' },
    },
    series: [
      {
        type: 'bar',
        data: dashboard.siteRankings.map((item) => Number(item.occupancyRate)),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#35f4c5' },
              { offset: 1, color: '#1b7bff' },
            ],
          },
        },
      },
    ],
  }

  const metrics = [
    { title: '露营点总量', value: dashboard.overview.siteCount, suffix: '个' },
    { title: '开放站点', value: dashboard.overview.activeSiteCount, suffix: '个' },
    { title: '今日预订', value: dashboard.overview.todayReservationCount, suffix: '单' },
    { title: '已确认预订', value: dashboard.overview.confirmedReservationCount, suffix: '单' },
    { title: '累计营收', value: Number(dashboard.overview.totalRevenue), prefix: '¥' },
    { title: '已核验观察', value: dashboard.overview.verifiedObservationCount, suffix: '条' },
  ]

  return (
    <div className="page-stack">
      <div className="page-hero">
        <div>
          <div className="page-kicker">运营驾驶舱</div>
          <Title level={2} style={{ marginBottom: 8 }}>
            露营、生态与结算数据同屏洞察
          </Title>
          <Paragraph className="page-description">
            这里汇总了站点健康度、预约转化、营收趋势、生态观察分类和风控告警，适合值班管理员做当日运营判断，也适合项目汇报时直接展示。
          </Paragraph>
        </div>
        <Alert
          type="success"
          showIcon
          message="系统状态稳定"
          description="当前仪表盘已同步加载站点、订单、支付、风控与生态观测数据。"
          className="hero-alert"
        />
      </div>

      <Row gutter={[16, 16]}>
        {metrics.map((item) => (
          <Col xs={24} sm={12} xl={8} key={item.title}>
            <Card className="glass-card metric-card">
              <Statistic title={item.title} value={item.value} suffix={item.suffix} prefix={item.prefix} />
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={15}>
          <Card className="glass-card" title="七日运营趋势">
            <ReactECharts option={trendOption} style={{ height: 340 }} />
          </Card>
        </Col>
        <Col xs={24} xl={9}>
          <Card className="glass-card" title="观察类别分布">
            <ReactECharts option={categoryOption} style={{ height: 340 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={10}>
          <Card className="glass-card" title="站点占用率排名">
            <ReactECharts option={rankingOption} style={{ height: 320 }} />
          </Card>
        </Col>
        <Col xs={24} xl={14}>
          <Card className="glass-card" title="空间态势板">
            <Row gutter={[16, 16]}>
              {dashboard.geoBoard.map((item) => (
                <Col xs={24} md={12} key={item.siteId}>
                  <div className="geo-card">
                    <div className="geo-header">
                      <div>
                        <div className="geo-name">{item.siteName}</div>
                        <Text className="geo-meta">{item.city}</Text>
                      </div>
                      <Tag color="cyan">生态指数 {item.ecoIndex}</Tag>
                    </div>
                    <Progress percent={Number(item.occupancyRate)} strokeColor="#35f4c5" trailColor="rgba(255,255,255,0.12)" />
                    <div className="geo-inline">
                      <span>预订 {item.reservationCount}</span>
                      <span>观察 {item.observationCount}</span>
                      <span>
                        坐标 {item.latitude}, {item.longitude}
                      </span>
                    </div>
                  </div>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>
      </Row>

      <Card className="glass-card" title="风控告警时间线">
        <List
          dataSource={dashboard.fraudAlerts}
          renderItem={(item) => (
            <List.Item className="alert-item">
              <div>
                <Space size={8}>
                  <Tag color={item.riskLevel === 'HIGH' ? 'red' : 'orange'}>{item.riskLevel}</Tag>
                  <Text strong>{item.eventType}</Text>
                  <Text type="secondary">{item.createdAt.replace('T', ' ')}</Text>
                </Space>
                <div className="alert-detail">{item.detail}</div>
              </div>
              <Tag color="blue">{item.sourceIp}</Tag>
            </List.Item>
          )}
        />
      </Card>
    </div>
  )
})

export default DashboardPage
