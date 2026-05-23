const { get } = require('../../utils/request')
const { splitWaterfall } = require('../../utils/waterfall')
const { baseUrl } = require('../../utils/config')
const { gatePageShow, markAgreed } = require('../../utils/protocol')

const CATEGORIES = ['全部菜系', '特色小炒', '面食粥粉', '快餐便当', '奶茶饮品', '小吃炸串']
const PRICE_RANGES = [
  { label: '不限价格', min: null, max: null },
  { label: '¥10 以下', min: null, max: 10 },
  { label: '¥10-15', min: 10, max: 15 },
  { label: '¥15 以上', min: 15, max: null }
]
const DISTANCE_OPTIONS = [
  { label: '不限距离', km: null },
  { label: '1km 内', km: 1 },
  { label: '3km 内', km: 3 },
  { label: '5km 内', km: 5 }
]
const SORT_OPTIONS = [
  { label: '综合推荐', value: '' },
  { label: '评分最高', value: 'score' },
  { label: '销量最多', value: 'sale' },
  { label: '距离最近', value: 'distance' }
]

Page({
  data: {
    leftColumn: [],
    rightColumn: [],
    shops: [],
    shopId: null,
    shopName: '',
    keyword: '',
    lat: null,
    lng: null,
    showFilter: false,
    categories: CATEGORIES,
    categoryIndex: 0,
    categoryLabel: '全部菜系',
    priceRanges: PRICE_RANGES,
    priceIndex: 0,
    priceLabel: '不限价格',
    distanceOptions: DISTANCE_OPTIONS,
    distanceIndex: 0,
    distanceLabel: '不限距离',
    sortOptions: SORT_OPTIONS,
    sortIndex: 0,
    sortLabel: '综合推荐',
    showProtocol: false
  },
  onLoad() {
    this.getLocation()
    this.loadShops()
  },
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
    this.loadDishes()
  },
  onPullDownRefresh() {
    this.loadDishes().finally(() => wx.stopPullDownRefresh())
  },
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
  onSearch(e) {
    this.setData({ keyword: e.detail.value })
  },
  onShopChange(e) {
    const shop = this.data.shops[e.detail.value]
    this.setData({ shopId: shop.shopId, shopName: shop.name })
    this.loadDishes()
  },
  toggleFilter() {
    this.setData({ showFilter: !this.data.showFilter })
  },
  onCategoryChange(e) {
    const i = Number(e.detail.value)
    this.setData({ categoryIndex: i, categoryLabel: CATEGORIES[i] })
  },
  onPriceChange(e) {
    const i = Number(e.detail.value)
    this.setData({ priceIndex: i, priceLabel: PRICE_RANGES[i].label })
  },
  onDistanceChange(e) {
    const i = Number(e.detail.value)
    this.setData({ distanceIndex: i, distanceLabel: DISTANCE_OPTIONS[i].label })
  },
  onSortChange(e) {
    const i = Number(e.detail.value)
    this.setData({ sortIndex: i, sortLabel: SORT_OPTIONS[i].label })
  },
  resetFilter() {
    this.setData({
      categoryIndex: 0,
      categoryLabel: '全部菜系',
      priceIndex: 0,
      priceLabel: '不限价格',
      distanceIndex: 0,
      distanceLabel: '不限距离',
      sortIndex: 0,
      sortLabel: '综合推荐'
    })
  },
  applyFilter() {
    this.setData({ showFilter: false })
    this.loadDishes()
  },
  async loadDishes() {
    const params = { page: 1, size: 50, keyword: this.data.keyword }
    if (this.data.shopId) params.shopId = this.data.shopId
    if (this.data.lat) {
      params.lat = this.data.lat
      params.lng = this.data.lng
    }
    const cat = CATEGORIES[this.data.categoryIndex]
    if (cat && cat !== '全部菜系') params.category = cat
    const price = PRICE_RANGES[this.data.priceIndex]
    if (price.min != null) params.minPrice = price.min
    if (price.max != null) params.maxPrice = price.max
    const sort = SORT_OPTIONS[this.data.sortIndex]
    if (sort.value) params.sortBy = sort.value

    const res = await get('/api/dish/list', params)
    let dishes = res.records || []
    const maxKm = DISTANCE_OPTIONS[this.data.distanceIndex].km
    if (maxKm != null) {
      dishes = dishes.filter((d) => d.distanceKm == null || d.distanceKm <= maxKm)
    }
    dishes = dishes.map((d) => ({
      ...d,
      coverImage: d.coverImage
        ? (d.coverImage.startsWith('http') ? d.coverImage : baseUrl + d.coverImage)
        : '/assets/placeholder.png'
    }))
    const { leftColumn, rightColumn } = splitWaterfall(dishes)
    this.setData({ leftColumn, rightColumn })
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  }
})
