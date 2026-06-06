const { get, post } = require('../../utils/request')
const { resolveImageUrl } = require('../../utils/image')

Page({
  data: {
    dish: null,
    id: null,
    lat: null,
    lng: null,
    loading: true,
    loadError: '',
    reviewPage: 2,
    reviewLoading: false,
    reviewHasMore: false
  },
  onLoad(options) {
    const id = options.id
    if (!id) {
      this.setData({ loading: false, loadError: '菜品参数无效' })
      return
    }
    this.setData({ id })
    this.getLocation()
    this.loadDetail(id)
    this.recordBrowse(id)
  },
  getLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => this.setData({ lat: res.latitude, lng: res.longitude }),
      fail: () => this.setData({ lat: 39.916527, lng: 116.397128 })
    })
  },
  async recordBrowse(dishId) {
    try {
      await post('/api/user/browse', { params: { dishId: Number(dishId) } })
    } catch (e) {
      // 未登录或网络异常时不阻断详情页
    }
  },
  normalizeDish(dish) {
    if (!dish) return { dish: null, initialCount: 0 }
    if (dish.coverImage) {
      dish.coverImage = resolveImageUrl(dish.coverImage)
    }
    if (dish.images && dish.images.length) {
      dish.images = dish.images.map((img) => resolveImageUrl(img)).filter(Boolean)
    } else if (dish.coverImage) {
      dish.images = [dish.coverImage]
    } else {
      dish.images = []
    }
    const initialCount = (dish.reviews || []).length
    dish.reviews = this.normalizeReviewImages(dish.reviews || [])
    return { dish, initialCount }
  },
  async loadDetail(id) {
    this.setData({ loading: true, loadError: '' })
    try {
      const params = {}
      if (this.data.lat != null) {
        params.lat = this.data.lat
        params.lng = this.data.lng
      }
      const dish = await get(`/api/dish/${id}`, params)
      const { dish: normalized, initialCount } = this.normalizeDish(dish)
      if (!normalized) {
        this.setData({
          loading: false,
          loadError: '菜品数据为空',
          dish: null
        })
        return
      }
      this.setData({
        dish: normalized,
        loading: false,
        loadError: '',
        reviewPage: 2,
        reviewHasMore: initialCount >= 20
      })
    } catch (e) {
      const msg = (e && (e.message || e.errMsg)) || ''
      const tip = msg.includes('timeout')
        ? '请求超时：请将 config.js 中 baseUrl 设为 http://127.0.0.1:8080 并重启开发者工具'
        : (msg || '加载失败，请检查后端是否已启动')
      this.setData({
        loading: false,
        loadError: tip,
        dish: null
      })
    }
  },
  retryLoad() {
    if (this.data.id) this.loadDetail(this.data.id)
  },
  normalizeReviewImages(reviews) {
    return reviews.map(r => {
      let images = []
      if (r.images) {
        try {
          images = typeof r.images === 'string' ? JSON.parse(r.images) : r.images
        } catch { images = [] }
      }
      r.images = images.map(url => resolveImageUrl(url)).filter(Boolean)
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
    } catch (e) {
      wx.showToast({ title: '评价加载失败', icon: 'none' })
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
