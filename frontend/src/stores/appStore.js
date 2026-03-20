import { message } from 'antd'
import { makeAutoObservable, runInAction } from 'mobx'
import http, { TOKEN_KEY } from '../api/http'

class AppStore {
  token = localStorage.getItem(TOKEN_KEY) || ''
  user = null
  bootstrapping = true
  dashboard = null
  sites = []
  reservations = []
  payments = []
  observations = []
  siteFilters = {}

  constructor() {
    makeAutoObservable(this, {}, { autoBind: true })
  }

  get isAuthenticated() {
    return Boolean(this.token)
  }

  get isAdmin() {
    return this.user?.role === 'ADMIN'
  }

  get pendingReservations() {
    return this.reservations.filter((item) => item.status === 'PENDING_PAYMENT')
  }

  get siteOptions() {
    return this.sites.map((item) => ({
      label: `${item.name} · ${item.city} · ¥${item.basePrice}/晚`,
      value: item.id,
    }))
  }

  setToken(token) {
    this.token = token
    if (token) {
      localStorage.setItem(TOKEN_KEY, token)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
  }

  async bootstrap() {
    if (!this.token) {
      runInAction(() => {
        this.bootstrapping = false
      })
      return
    }

    try {
      const user = await http.get('/auth/me')
      runInAction(() => {
        this.user = user
      })
      await this.refreshAll()
    } catch (error) {
      this.logout(false)
      message.error(error.message)
    } finally {
      runInAction(() => {
        this.bootstrapping = false
      })
    }
  }

  async login(values) {
    const data = await http.post('/auth/login', values)
    runInAction(() => {
      this.setToken(data.token)
      this.user = data.user
    })
    await this.refreshAll()
    return data.user
  }

  logout(showMessage = true) {
    this.setToken('')
    this.user = null
    this.dashboard = null
    this.sites = []
    this.reservations = []
    this.payments = []
    this.observations = []
    this.siteFilters = {}
    if (showMessage) {
      message.success('已安全退出')
    }
  }

  async refreshAll() {
    await Promise.all([
      this.fetchDashboard(),
      this.fetchSites(this.siteFilters),
      this.fetchReservations(),
      this.fetchPayments(),
      this.fetchObservations(),
    ])
  }

  async fetchDashboard() {
    const data = await http.get('/dashboard')
    runInAction(() => {
      this.dashboard = data
    })
    return data
  }

  async fetchSites(filters = {}) {
    const params = Object.fromEntries(
      Object.entries(filters).filter(([, value]) => value !== undefined && value !== null && value !== ''),
    )
    const data = await http.get('/sites', { params })
    runInAction(() => {
      this.sites = data
      this.siteFilters = filters
    })
    return data
  }

  async saveSite(values, id) {
    const payload = {
      ...values,
      facilities: values.facilitiesText
        ? values.facilitiesText
            .split(/[,，]/)
            .map((item) => item.trim())
            .filter(Boolean)
        : [],
      tags: values.tagsText
        ? values.tagsText
            .split(/[,，]/)
            .map((item) => item.trim())
            .filter(Boolean)
        : [],
    }
    delete payload.facilitiesText
    delete payload.tagsText

    const data = id ? await http.put(`/sites/${id}`, payload) : await http.post('/sites', payload)
    await Promise.all([this.fetchSites(this.siteFilters), this.fetchDashboard()])
    message.success(id ? '露营点已更新' : '露营点已创建')
    return data
  }

  async updateSiteStatus(id, status) {
    const data = await http.patch(`/sites/${id}/status`, null, { params: { status } })
    await Promise.all([this.fetchSites(this.siteFilters), this.fetchDashboard()])
    message.success('露营点状态已更新')
    return data
  }

  async fetchReservations() {
    const data = await http.get('/reservations')
    runInAction(() => {
      this.reservations = data
    })
    return data
  }

  async createReservation(values) {
    const data = await http.post('/reservations', values)
    await Promise.all([this.fetchReservations(), this.fetchSites(this.siteFilters), this.fetchDashboard()])
    message.success('预订提交成功')
    return data
  }

  async cancelReservation(id) {
    const data = await http.patch(`/reservations/${id}/cancel`)
    await Promise.all([this.fetchReservations(), this.fetchSites(this.siteFilters), this.fetchDashboard()])
    message.success('预订已取消')
    return data
  }

  async updateReservationStatus(id, status) {
    const data = await http.patch(`/reservations/${id}/status`, { status })
    await Promise.all([this.fetchReservations(), this.fetchDashboard()])
    message.success('预订状态已更新')
    return data
  }

  async fetchPayments() {
    const data = await http.get('/payments')
    runInAction(() => {
      this.payments = data
    })
    return data
  }

  async payReservation(values) {
    const data = await http.post('/payments/pay', values)
    await Promise.all([this.fetchPayments(), this.fetchReservations(), this.fetchDashboard()])
    message.success('支付成功')
    return data
  }

  async settlePayment(id, note) {
    const data = await http.patch(`/payments/${id}/settle`, { note })
    await Promise.all([this.fetchPayments(), this.fetchDashboard()])
    message.success('结算完成')
    return data
  }

  async fetchObservations() {
    const data = await http.get('/observations')
    runInAction(() => {
      this.observations = data
    })
    return data
  }

  async createObservation(values) {
    const data = await http.post('/observations', values)
    await Promise.all([this.fetchObservations(), this.fetchDashboard()])
    message.success('生态记录提交成功')
    return data
  }

  async verifyObservation(id) {
    const data = await http.patch(`/observations/${id}/verify`)
    await Promise.all([this.fetchObservations(), this.fetchDashboard()])
    message.success('生态记录已审核')
    return data
  }
}

const appStore = new AppStore()

export default appStore
