const { get, del } = require('../../utils/request')
const { baseUrl } = require('../../utils/config')

Page({
  data: { list: [] },
  onShow() {
    this.load()
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
    if (!this.data.list.length) return
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先前往「我的」页登录', icon: 'none', duration: 2500 })
      return
    }
    wx.showModal({
      title: '清空浏览记录',
      content: '确定清空全部浏览记录吗？',
      confirmText: '确认',
      success: async (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '清空中...', mask: true })
        try {
          await del('/api/user/browse/clear')
          this.setData({ list: [] })
          wx.showToast({ title: '已清空', icon: 'success' })
        } catch (e) {
          wx.showToast({ title: e.message || '清空失败', icon: 'none' })
        } finally {
          wx.hideLoading()
        }
      }
    })
  }
})
