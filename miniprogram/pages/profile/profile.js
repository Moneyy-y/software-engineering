const { get, post } = require('../../utils/request')
const { login, clearAuth, isLoggedIn } = require('../../utils/auth')
const { gatePageShow, markAgreed } = require('../../utils/protocol')

Page({
  data: { user: {}, unreadCount: 0, showProtocol: false, loggedIn: false },
  onShow() {
    gatePageShow(this, this.initPage)
  },
  onProtocolAgree() {
    markAgreed().then(() => {
      this.setData({ showProtocol: false })
      this.initPage()
    })
  },
  onProtocolReject() {
    wx.showToast({ title: '需同意协议后方可使用', icon: 'none' })
  },
  handleLogin() {
    if (this.data.loggedIn && this.data.user.nickname) return
    login({ force: true }).then(() => {
      wx.showToast({ title: '登录成功', icon: 'success' })
      this.loadUser()
      this.loadUnread()
    }).catch(() => {
      wx.showToast({ title: '登录失败，请检查网络', icon: 'none' })
    })
  },
  initPage() {
    if (!isLoggedIn()) {
      this.setData({ user: {}, unreadCount: 0, loggedIn: false })
      return
    }
    this.loadUser()
    this.loadUnread()
  },
  async loadUser() {
    try {
      const user = await get('/api/user/info') || {}
      this.setData({ user, loggedIn: !!user.userId })
    } catch (e) {
      this.setData({ user: {}, loggedIn: false })
    }
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
  goProtocol() {
    wx.navigateTo({ url: '/pages/protocol-detail/protocol-detail?type=user' })
  },
  logout() {
    if (!isLoggedIn()) {
      this.setData({ user: {}, unreadCount: 0, loggedIn: false })
      return
    }
    post('/api/user/logout').finally(() => {
      clearAuth()
      wx.showToast({ title: '已退出' })
      this.setData({ user: {}, unreadCount: 0, loggedIn: false })
    })
  }
})
