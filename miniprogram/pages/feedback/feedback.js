const { post, get } = require('../../utils/request')
const { login } = require('../../utils/auth')

const TYPE_OPTIONS = [
  { value: 'hygiene', label: '卫生' },
  { value: 'price', label: '价格' },
  { value: 'service', label: '服务' },
  { value: 'other', label: '其他' }
]

const STATUS_LABEL = {
  pending: '待处理',
  processing: '处理中',
  resolved: '已办结'
}

const STATUS_COLOR = {
  pending: 'orange',
  processing: 'blue',
  resolved: 'green'
}

Page({
  data: {
    typeLabels: TYPE_OPTIONS.map((o) => o.label),
    typeIndex: 3,
    type: 'other',
    typeLabel: '其他',
    description: '',
    feedbacks: [],
    loading: true
  },
  onShow() {
    login().then(() => this.loadFeedbacks()).catch(() => {})
  },
  onType(e) {
    const idx = Number(e.detail.value)
    const opt = TYPE_OPTIONS[idx]
    this.setData({
      typeIndex: idx,
      type: opt.value,
      typeLabel: opt.label
    })
  },
  onDesc(e) { this.setData({ description: e.detail.value }) },
  async loadFeedbacks() {
    this.setData({ loading: true })
    try {
      const data = await get('/api/feedback/my') || []
      const feedbacks = Array.isArray(data) ? data : []
      this.setData({ feedbacks })
    } catch (e) {
      this.setData({ feedbacks: [] })
    } finally {
      this.setData({ loading: false })
    }
  },
  async submit() {
    if (this.data.description.length < 10) {
      wx.showToast({ title: '至少10字', icon: 'none' }); return
    }
    await post('/api/feedback/submit', { type: this.data.type, description: this.data.description })
    wx.showToast({ title: '提交成功' })
    this.setData({ description: '' })
    this.loadFeedbacks()
  },
  getStatusLabel(status) {
    return STATUS_LABEL[status] || status
  },
  getStatusColor(status) {
    return STATUS_COLOR[status] || 'gray'
  },
  getTypeLabel(type) {
    const opt = TYPE_OPTIONS.find(o => o.value === type)
    return opt ? opt.label : type
  },
  formatTime(time) {
    if (!time) return ''
    return time.replace('T', ' ')
  }
})
