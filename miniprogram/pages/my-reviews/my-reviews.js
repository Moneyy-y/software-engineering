const { get } = require('../../utils/request')
const { login } = require('../../utils/auth')

const DRAFT_PREFIX = 'review_draft_'
const PAGE_TABS = [
  { key: 'reviews', label: '我的评价' },
  { key: 'drafts', label: '草稿' }
]

Page({
  data: {
    pageTabs: PAGE_TABS,
    activeTab: 'reviews',
    list: [],
    drafts: [],
    statusText: { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  },
  onLoad(options) {
    if (options.tab === 'drafts') {
      this.setData({ activeTab: 'drafts' })
    }
    if (options.reviewId) {
      this._pendingReviewId = Number(options.reviewId)
    }
  },
  onShow() {
    login().then(async () => {
      await this.loadReviews()
      if (this.data.activeTab === 'drafts') {
        this.loadDrafts()
      }
      if (this._pendingReviewId) {
        const id = this._pendingReviewId
        this._pendingReviewId = null
        this.openReviewFromMessage(id)
      }
    })
  },
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    if (tab === 'drafts') {
      this.loadDrafts()
    } else {
      this.loadReviews()
    }
  },
  async loadReviews() {
    const list = (await get('/api/user/review/my')) || []
    this.setData({ list })
  },
  loadDrafts() {
    try {
      const info = wx.getStorageInfoSync()
      const drafts = (info.keys || [])
        .filter((k) => k.startsWith(DRAFT_PREFIX))
        .map((key) => {
          const dishId = key.replace(DRAFT_PREFIX, '')
          const data = wx.getStorageSync(key) || {}
          const preview = (data.content || '').slice(0, 40)
          return {
            key,
            dishId,
            score: data.score || 5,
            preview: preview + ((data.content || '').length > 40 ? '…' : ''),
            imageCount: (data.images || []).length,
            updatedAt: data.updatedAt || ''
          }
        })
      this.setData({ drafts })
    } catch (e) {
      this.setData({ drafts: [] })
    }
  },
  openReviewFromMessage(reviewId) {
    const item = this.data.list.find((r) => r.reviewId === reviewId)
    if (!item) return
    if (item.auditStatus === 'rejected') {
      this.resubmit({ currentTarget: { dataset: { item } } })
      return
    }
    if (item.dishId) {
      wx.navigateTo({ url: `/pages/detail/detail?id=${item.dishId}` })
    }
  },
  resubmit(e) {
    const item = e.currentTarget.dataset.item
    const content = encodeURIComponent(item.content || '')
    wx.navigateTo({
      url: `/pages/review/review?dishId=${item.dishId}&prefill=1&resubmit=1&score=${item.score}&prefillContent=${content}`
    })
  },
  openDraft(e) {
    const dishId = e.currentTarget.dataset.dishId
    wx.navigateTo({ url: `/pages/review/review?dishId=${dishId}` })
  },
  removeDraft(e) {
    const key = e.currentTarget.dataset.key
    wx.showModal({
      title: '删除草稿',
      content: '确定删除该草稿吗？',
      success: (res) => {
        if (!res.confirm) return
        wx.removeStorageSync(key)
        wx.showToast({ title: '已删除', icon: 'none' })
        this.loadDrafts()
      }
    })
  }
})
