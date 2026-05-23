const { get, post } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { uploadFile } = require('../../utils/upload')
const { baseUrl } = require('../../utils/config')

const ZONES = [
  { key: '', label: '全部' },
  { key: 'recommend', label: '推荐' },
  { key: 'warning', label: '避雷' },
  { key: 'general', label: '综合' }
]

const PUBLISH_ZONES = [
  { key: 'recommend', label: '推荐区' },
  { key: 'warning', label: '避雷区' },
  { key: 'general', label: '综合区' }
]

const MAIN_TABS = [
  { key: 'forum', label: '论坛' },
  { key: 'my', label: '我的帖子' }
]

Page({
  data: {
    mainTabs: MAIN_TABS,
    activeMainTab: 'forum',
    posts: [],
    myPosts: [],
    zones: ZONES,
    activeZone: '',
    showPublish: false,
    publishTitle: '',
    publishContent: '',
    publishImages: [],
    publishZoneIndex: 0,
    publishZones: PUBLISH_ZONES,
    resubmitPostId: null,
    statusText: { pending: '待审核', approved: '已通过', rejected: '已拒绝' },
    zoneText: { recommend: '推荐区', warning: '避雷区', general: '综合区' }
  },
  onLoad(options) {
    if (options.tab === 'my') {
      this.setData({ activeMainTab: 'my' })
    }
    if (options.resubmitPostId) {
      this._pendingResubmitId = Number(options.resubmitPostId)
    }
  },
  onShow() {
    login().then(async () => {
      if (this.data.activeMainTab === 'my') {
        await this.loadMyPosts()
      } else {
        await this.load()
      }
      if (this._pendingResubmitId) {
        const id = this._pendingResubmitId
        this._pendingResubmitId = null
        this.setData({ activeMainTab: 'my' })
        await this.loadMyPosts()
        await this.openResubmit(id)
      }
    })
  },
  switchMainTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeMainTab: tab })
    if (tab === 'my') {
      login().then(() => this.loadMyPosts())
    } else {
      this.load()
    }
  },
  switchZone(e) {
    this.setData({ activeZone: e.currentTarget.dataset.zone })
    this.load()
  },
  async load() {
    const params = {}
    if (this.data.activeZone) params.zone = this.data.activeZone
    const posts = (await get('/api/post/list', params)) || []
    const formatted = posts.map((p) => ({
      ...p,
      images: (p.images || []).map((img) => (img.startsWith('http') ? img : baseUrl + img))
    }))
    this.setData({ posts: formatted })
  },
  async loadMyPosts() {
    const list = (await get('/api/post/my')) || []
    this.setData({ myPosts: list })
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/forum-detail/forum-detail?id=${id}` })
  },
  openPublish() {
    this.setData({
      showPublish: true,
      resubmitPostId: null,
      publishTitle: '',
      publishContent: '',
      publishImages: [],
      publishZoneIndex: 0
    })
  },
  async openResubmit(postId) {
    const list = (await get('/api/post/my')) || []
    const postItem = list.find((p) => p.postId === postId)
    if (!postItem || postItem.auditStatus !== 'rejected') {
      wx.showToast({ title: '帖子不可修改', icon: 'none' })
      return
    }
    const zoneIndex = Math.max(0, PUBLISH_ZONES.findIndex((z) => z.key === postItem.zone))
    const images = (postItem.images || []).map((img) =>
      img.startsWith('http') ? img : baseUrl + img
    )
    this.setData({
      showPublish: true,
      resubmitPostId: postId,
      publishTitle: postItem.title,
      publishContent: postItem.content,
      publishImages: images,
      publishZoneIndex: zoneIndex
    })
  },
  resubmitMyPost(e) {
    const postId = e.currentTarget.dataset.id
    this.openResubmit(postId)
  },
  closePublish() {
    this.setData({ showPublish: false, resubmitPostId: null })
  },
  preventBubble() {},
  onPublishTitle(e) {
    this.setData({ publishTitle: e.detail.value })
  },
  onPublishContent(e) {
    this.setData({ publishContent: e.detail.value })
  },
  onPublishZone(e) {
    this.setData({ publishZoneIndex: Number(e.detail.value) })
  },
  choosePublishImage() {
    const remain = 3 - this.data.publishImages.length
    if (remain <= 0) {
      wx.showToast({ title: '最多 3 张图', icon: 'none' })
      return
    }
    wx.chooseImage({
      count: remain,
      success: async (res) => {
        wx.showLoading({ title: '上传中' })
        const urls = [...this.data.publishImages]
        try {
          for (const path of res.tempFilePaths) {
            let url = await uploadFile(path)
            if (!url.startsWith('http')) url = baseUrl + url
            urls.push(url)
          }
          this.setData({ publishImages: urls })
        } finally {
          wx.hideLoading()
        }
      }
    })
  },
  removePublishImage(e) {
    const idx = e.currentTarget.dataset.index
    const urls = [...this.data.publishImages]
    urls.splice(idx, 1)
    this.setData({ publishImages: urls })
  },
  async submitPublish() {
    const title = (this.data.publishTitle || '').trim()
    const content = (this.data.publishContent || '').trim()
    if (!title) {
      wx.showToast({ title: '请填写标题', icon: 'none' })
      return
    }
    if (content.length < 5) {
      wx.showToast({ title: '内容至少 5 字', icon: 'none' })
      return
    }
    const zone = PUBLISH_ZONES[this.data.publishZoneIndex].key
    const images = this.data.publishImages.map((u) =>
      u.startsWith(baseUrl) ? u.slice(baseUrl.length) : u
    )
    if (this.data.resubmitPostId) {
      await post('/api/post/resubmit', {
        postId: this.data.resubmitPostId,
        title,
        content,
        zone,
        images
      })
      wx.showToast({ title: '已重新提交审核' })
    } else {
      await post('/api/post/publish', { title, content, zone, images })
      wx.showToast({ title: '已提交审核' })
    }
    this.setData({ showPublish: false, resubmitPostId: null })
    if (this.data.activeMainTab === 'my') {
      this.loadMyPosts()
    } else {
      this.load()
    }
  }
})
