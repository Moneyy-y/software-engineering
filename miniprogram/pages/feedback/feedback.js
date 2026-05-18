const { post } = require('../../utils/request')

Page({
  data: {
    types: ['hygiene', 'price', 'service', 'other'],
    typeLabels: { hygiene: '卫生', price: '价格', service: '服务', other: '其他' },
    type: 'other',
    typeLabel: '其他',
    description: ''
  },
  onType(e) {
    const type = this.data.types[e.detail.value]
    this.setData({ type, typeLabel: this.data.typeLabels[type] })
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
