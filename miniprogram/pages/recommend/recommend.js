const { get } = require('../../utils/request')
const { login } = require('../../utils/auth')

Page({
  data: { list: [] },
  onShow() {
    login().then(() => this.load())
  },
  async load() {
    const list = await get('/api/recommend/list', { lat: 39.916527, lng: 116.397128, limit: 20 }) || []
    this.setData({ list })
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  }
})
