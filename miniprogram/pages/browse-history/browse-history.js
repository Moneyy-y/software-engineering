const { get, del } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { baseUrl } = require('../../utils/config')

Page({
  data: { list: [] },
  onShow() {
    login().then(() => this.load())
  },
  onPullDownRefresh() {
    this.load().finally(() => wx.stopPullDownRefresh())
  },
  async load() {
    const list = (await get('/api/user/browse/history')) || []
    const formatted = list.map((item) => ({
      ...item,
      coverImage: item.coverImage
        ? (item.coverImage.startsWith('http') ? item.coverImage : baseUrl + item.coverImage)
        : '/assets/placeholder.png'
    }))
    this.setData({ list: formatted })
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  },
  clearAll() {
    wx.showModal({
      title: '清空浏览记录',
      content: '确定清空全部浏览记录吗？',
      success: async (res) => {
        if (!res.confirm) return
        await del('/api/user/browse/clear')
        wx.showToast({ title: '已清空', icon: 'none' })
        this.setData({ list: [] })
      }
    })
  }
})
