const { get } = require('../../utils/request')
const { gatePageShow, markAgreed } = require('../../utils/protocol')

Page({
  data: { tab: 'red', redList: [], blackList: [], currentList: [], showProtocol: false },
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
    this.load()
  },
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
