const { get } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { baseUrl } = require('../../utils/config')

Page({
  data: { list: [] },
  onShow() {
    login().then(() => this.load())
  },
  async load() {
    const res = await get('/api/user/favorite/list', { page: 1, size: 50 })
    const list = (res.records || []).map(item => {
      if (item.coverImage && !item.coverImage.startsWith('http')) {
        item.coverImage = baseUrl + item.coverImage
      }
      return item
    })
    this.setData({ list })
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  }
})
