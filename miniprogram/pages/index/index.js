const { get } = require('../../utils/request')
const { login } = require('../../utils/auth')

Page({
  data: { dishes: [], shops: [], shopId: null, shopName: '', keyword: '', lat: null, lng: null },
  onLoad() {
    this.getLocation()
    this.loadShops()
  },
  onShow() { login().then(() => this.loadDishes()) },
  onPullDownRefresh() { this.loadDishes().finally(() => wx.stopPullDownRefresh()) },
  getLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => this.setData({ lat: res.latitude, lng: res.longitude }),
      fail: () => this.setData({ lat: 39.916527, lng: 116.397128 })
    })
  },
  async loadShops() {
    const shops = await get('/api/shop/list') || []
    this.setData({ shops: [{ shopId: null, name: '全部食堂' }, ...shops] })
  },
  onSearch(e) { this.setData({ keyword: e.detail.value }) },
  onShopChange(e) {
    const shop = this.data.shops[e.detail.value]
    this.setData({ shopId: shop.shopId, shopName: shop.name })
    this.loadDishes()
  },
  async loadDishes() {
    const params = { page: 1, size: 50, keyword: this.data.keyword }
    if (this.data.shopId) params.shopId = this.data.shopId
    if (this.data.lat) { params.lat = this.data.lat; params.lng = this.data.lng }
    const res = await get('/api/dish/list', params)
    this.setData({ dishes: res.records || [] })
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  }
})
