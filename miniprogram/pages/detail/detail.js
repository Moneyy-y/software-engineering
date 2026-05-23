const { get, post } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { baseUrl } = require('../../utils/config')

Page({
  data: { dish: null, id: null, lat: null, lng: null },
  onLoad(options) {
    this.setData({ id: options.id })
    this.getLocation()
    login().then(() => {
      this.loadDetail()
      this.recordBrowse()
    })
  },
  getLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => this.setData({ lat: res.latitude, lng: res.longitude }),
      fail: () => this.setData({ lat: 39.916527, lng: 116.397128 })
    })
  },
  async recordBrowse() {
    try {
      await post('/api/user/browse', { params: { dishId: Number(this.data.id) } })
    } catch (e) {
      // 未登录或网络异常时不阻断详情页
    }
  },
  async loadDetail() {
    const params = {}
    if (this.data.lat) {
      params.lat = this.data.lat
      params.lng = this.data.lng
    }
    const dish = await get(`/api/dish/${this.data.id}`, params)
    if (dish.images) {
      dish.images = dish.images.map((img) => (img.startsWith('http') ? img : baseUrl + img))
    }
    if (dish.coverImage && !dish.coverImage.startsWith('http')) {
      dish.coverImage = baseUrl + dish.coverImage
    }
    this.setData({ dish })
  },
  async toggleFavorite() {
    const { dish } = this.data
    if (dish.favorited) {
      await post(`/api/user/favorite/remove?dishId=${dish.dishId}`)
    } else {
      await post(`/api/user/favorite/add?dishId=${dish.dishId}`)
    }
    dish.favorited = !dish.favorited
    this.setData({ dish })
    wx.showToast({ title: dish.favorited ? '已收藏' : '已取消', icon: 'none' })
  },
  goReview() {
    wx.navigateTo({ url: `/pages/review/review?dishId=${this.data.id}` })
  },
  goReport(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/report/report?targetType=review&targetId=${id}`
    })
  }
})
