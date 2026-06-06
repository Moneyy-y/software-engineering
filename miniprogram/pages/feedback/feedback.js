const { post, get } = require('../../utils/request')

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
    this.loadFeedbacks()
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
  isDuplicateSubmission(type, description) {
    const desc = (description || '').trim()
    if (this._lastSubmit
        && this._lastSubmit.type === type
        && this._lastSubmit.description === desc) {
      return true
    }
    const latest = (this.data.feedbacks || [])[0]
    if (latest
        && latest.type === type
        && (latest.description || '').trim() === desc) {
      return true
    }
    return false
  },
  confirmDuplicateSubmit() {
    return new Promise((resolve) => {
      wx.showModal({
        title: '重复提交',
        content: '你刚刚提交了一样的信息，是否还要提交？',
        confirmText: '继续提交',
        cancelText: '取消',
        success: resolve
      })
    })
  },
  async loadFeedbacks() {
    this.setData({ loading: true })
    try {
      const data = await get('/api/feedback/my') || []
      const feedbacks = (Array.isArray(data) ? data : []).map((item) => ({
        ...item,
        typeLabel: this.getTypeLabel(item.type),
        statusLabel: this.getStatusLabel(item.status),
        statusColor: this.getStatusColor(item.status),
        createTimeText: this.formatTime(item.createTime),
        acceptTimeText: this.formatTime(item.acceptTime),
        resolveTimeText: this.formatTime(item.resolveTime)
      }))
      this.setData({ feedbacks })
    } catch (e) {
      this.setData({ feedbacks: [] })
    } finally {
      this.setData({ loading: false })
    }
  },
  onSubmit() {
    this.doSubmit(false)
  },
  async doSubmit(confirmed) {
    const force = confirmed === true
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先前往「我的」页登录', icon: 'none', duration: 2500 })
      return
    }
    const description = (this.data.description || '').trim()
    if (description.length < 10) {
      wx.showToast({ title: '至少10字', icon: 'none' })
      return
    }
    if (!force && this.isDuplicateSubmission(this.data.type, description)) {
      const res = await this.confirmDuplicateSubmit()
      if (!res.confirm) return
      return this.doSubmit(true)
    }
    try {
      await post('/api/feedback/submit', {
        type: this.data.type,
        description,
        confirmDuplicate: force
      })
      this._lastSubmit = { type: this.data.type, description }
      wx.showToast({ title: '提交成功' })
      this.setData({ description: '' })
      this.loadFeedbacks()
    } catch (e) {
      if (e && e.status === 2002 && !force) {
        const res = await this.confirmDuplicateSubmit()
        if (res.confirm) return this.doSubmit(true)
      }
    }
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
