const { get } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { gatePageShow, markAgreed } = require('../../utils/protocol')

Page({
  data: { list: [], showProtocol: false },
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
