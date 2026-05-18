const { get, post } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { baseUrl } = require('../../utils/config')

Page({
  data: { dish: null, id: null },
  onLoad(options) {
    this.setData({ id: options.id })
    login().then(() => this.loadDetail())
  },
  async loadDetail() {
    const dish = await get(`/api/dish/${this.data.id}`, { lat: 39.916527, lng: 116.397128 })
    if (dish.images) {
      dish.images = dish.images.map(img => img.startsWith('http') ? img : baseUrl + img)
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
  }
})
