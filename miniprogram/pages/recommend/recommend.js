const { get } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { gatePageShow, markAgreed } = require('../../utils/protocol')
const { resolveImageUrl, PLACEHOLDER } = require('../../utils/image')

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
    const list = (await get('/api/recommend/list', { lat: 39.916527, lng: 116.397128, limit: 20 }) || [])
      .map((item) => ({
        ...item,
        coverImage: resolveImageUrl(item.coverImage)
      }))
    this.setData({ list })
  },
  onPullDownRefresh() {
    this.load().finally(() => wx.stopPullDownRefresh())
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  },
  onCoverImageError(e) {
    const index = e.currentTarget.dataset.index
    const list = [...this.data.list]
    if (list[index] && list[index].coverImage !== PLACEHOLDER) {
      list[index] = { ...list[index], coverImage: PLACEHOLDER }
      this.setData({ list })
    }
  }
})
