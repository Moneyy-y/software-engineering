const { get, post } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { gatePageShow, markAgreed } = require('../../utils/protocol')

Page({
  data: { user: {}, unreadCount: 0, showProtocol: false },
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
  initPage() {
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
  goProtocol() {
    wx.navigateTo({ url: '/pages/protocol-detail/protocol-detail?type=user' })
  },
  logout() {
    post('/api/user/logout').finally(() => {
      wx.removeStorageSync('token')
      wx.showToast({ title: '已退出' })
      this.setData({ user: {} })
    })
  }
})
