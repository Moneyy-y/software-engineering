const { get, post } = require('../../utils/request')
const { baseUrl } = require('../../utils/config')

Page({
  data: {
    dish: null, id: null, lat: null, lng: null,
    reviewPage: 2, reviewLoading: false, reviewHasMore: false
  },
  onLoad(options) {
    this.setData({ id: options.id })
    this.getLocation()
    this.loadDetail()
    this.recordBrowse()
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
    const initialCount = (dish.reviews || []).length
    dish.reviews = this.normalizeReviewImages(dish.reviews || [])
    this.setData({
      dish,
      reviewPage: 2,
      reviewHasMore: initialCount >= 20
    })
  },
  normalizeReviewImages(reviews) {
    return reviews.map(r => {
      let images = []
      if (r.images) {
        try {
          images = typeof r.images === 'string' ? JSON.parse(r.images) : r.images
        } catch { images = [] }
      }
      r.images = images.map(url => url.startsWith('http') ? url : baseUrl + url)
      return r
    })
  },
  previewReviewImage(e) {
    const { url, urls } = e.currentTarget.dataset
    wx.previewImage({ current: url, urls: urls || [url] })
  },
  onReachBottom() {
    if (!this.data.reviewHasMore || this.data.reviewLoading) return
    this.loadMoreReviews()
  },
  async loadMoreReviews() {
    this.setData({ reviewLoading: true })
    try {
      const res = await get(`/api/review/dish/${this.data.id}`, {
        page: this.data.reviewPage,
        size: 20
      })
      const newList = this.normalizeReviewImages(res.records || [])
      if (newList.length > 0) {
        const dish = this.data.dish
        dish.reviews = [...dish.reviews, ...newList]
        this.setData({
          dish,
          reviewPage: this.data.reviewPage + 1,
          reviewHasMore: newList.length >= 20
        })
      } else {
        this.setData({ reviewHasMore: false })
      }
    } finally {
      this.setData({ reviewLoading: false })
    }
  },
  async toggleFavorite() {
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先前往「我的」页登录', icon: 'none', duration: 2500 })
      return
    }
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
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先前往「我的」页登录', icon: 'none', duration: 2500 })
      return
    }
    wx.navigateTo({
      url: `/pages/report/report?targetType=review&targetId=${id}`
    })
  }
})
