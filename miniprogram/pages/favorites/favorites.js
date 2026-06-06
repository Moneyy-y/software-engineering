const { get, post } = require('../../utils/request')
const { baseUrl } = require('../../utils/config')

Page({
  data: { list: [] },
  onShow() {
    this.load()
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
  },
  async remove(e) {
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先前往「我的」页登录', icon: 'none', duration: 2500 })
      return
    }
    const { id, index } = e.currentTarget.dataset
    const item = this.data.list[index]
    wx.showModal({
      title: '取消收藏',
      content: `确定不再收藏「${item.name}」吗？`,
      success: async (res) => {
        if (!res.confirm) return
        await post(`/api/user/favorite/remove?dishId=${id}`)
        const list = [...this.data.list]
        list.splice(index, 1)
        this.setData({ list })
        wx.showToast({ title: '已取消收藏', icon: 'none' })
      }
    })
  }
})
