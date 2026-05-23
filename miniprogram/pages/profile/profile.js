const { get, post } = require('../../utils/request')
const { login } = require('../../utils/auth')

Page({
  data: { user: {}, unreadCount: 0 },
  onShow() {
    login().then(() => {
      this.loadUser()
      this.loadUnread()
    })
  },
  async loadUser() {
    const user = await get('/api/user/info') || {}
    this.setData({ user })
  },
  async loadUnread() {
    try {
      const unreadCount = (await get('/api/user/message/unread/count')) || 0
      this.setData({ unreadCount })
    } catch (e) {
      this.setData({ unreadCount: 0 })
    }
  },
  goBrowseHistory() { wx.navigateTo({ url: '/pages/browse-history/browse-history' }) },
  goMessages() { wx.navigateTo({ url: '/pages/messages/messages' }) },
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
