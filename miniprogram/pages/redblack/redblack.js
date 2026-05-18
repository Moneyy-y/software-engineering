const { get } = require('../../utils/request')

Page({
  data: { tab: 'red', redList: [], blackList: [], currentList: [] },
  onLoad() { this.load() },
  async load() {
    const data = await get('/api/recommend/redblack') || {}
    this.setData({ redList: data.red || [], blackList: data.black || [], currentList: data.red || [] })
  },
  switchTab(e) {
    const tab = e.currentTarget.dataset.t
    this.setData({ tab, currentList: tab === 'red' ? this.data.redList : this.data.blackList })
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  }
})
