const { get, post } = require('../../utils/request')
const { login } = require('../../utils/auth')

Page({
  data: { user: {} },
  onShow() {
    login().then(() => this.loadUser())
  },
  async loadUser() {
    const user = await get('/api/user/info') || {}
    this.setData({ user })
  },
  goFavorites() { wx.navigateTo({ url: '/pages/favorites/favorites' }) },
  goMyReviews() { wx.navigateTo({ url: '/pages/my-reviews/my-reviews' }) },
  goFeedback() { wx.navigateTo({ url: '/pages/feedback/feedback' }) },
  goForum() { wx.navigateTo({ url: '/pages/forum/forum' }) },
  logout() {
    post('/api/user/logout').finally(() => {
      wx.removeStorageSync('token')
      wx.showToast({ title: '已退出' })
      this.setData({ user: {} })
    })
  }
})
