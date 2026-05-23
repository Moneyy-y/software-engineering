const { post } = require('../../utils/request')

const TYPE_OPTIONS = [
  { value: 'hygiene', label: '卫生' },
  { value: 'price', label: '价格' },
  { value: 'service', label: '服务' },
  { value: 'other', label: '其他' }
]

Page({
  data: {
    typeLabels: TYPE_OPTIONS.map((o) => o.label),
    typeIndex: 3,
    type: 'other',
    typeLabel: '其他',
    description: ''
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
  async submit() {
    if (this.data.description.length < 10) {
      wx.showToast({ title: '至少10字', icon: 'none' }); return
    }
    await post('/api/feedback/submit', { type: this.data.type, description: this.data.description })
    wx.showToast({ title: '提交成功' })
    setTimeout(() => wx.navigateBack(), 1500)
  }
})
