const { post } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { uploadFile } = require('../../utils/upload')
const { baseUrl } = require('../../utils/config')

function draftKey(dishId) {
  return `review_draft_${dishId}`
}

Page({
  data: { dishId: null, score: 5, content: '', anonymous: true, images: [], isResubmit: false, dishName: '' },
  onLoad(options) {
    this.setData({
      dishId: options.dishId,
      isResubmit: options.resubmit === '1'
    })
    login()
    this.loadDraft(options)
  },
  loadDraft(options) {
    const key = draftKey(options.dishId)
    const draft = wx.getStorageSync(key)
    if (draft) {
      this.setData({
        score: draft.score || 5,
        content: draft.content || '',
        anonymous: draft.anonymous !== false,
        images: draft.images || []
      })
      if (options.prefill === '1') {
        wx.showToast({ title: '已加载被拒评价内容', icon: 'none' })
      } else {
        wx.showToast({ title: '已恢复草稿', icon: 'none' })
      }
    } else if (options.prefillContent) {
      this.setData({
        score: Number(options.score) || 5,
        content: decodeURIComponent(options.prefillContent)
      })
    }
  },
  saveDraft() {
    const { dishId, score, content, anonymous, images } = this.data
    if (!content) {
      wx.showToast({ title: '暂无内容可保存', icon: 'none' })
      return
    }
    wx.setStorageSync(draftKey(dishId), {
      score,
      content,
      anonymous,
      images,
      updatedAt: new Date().toLocaleString()
    })
    wx.showToast({ title: '草稿已保存', icon: 'none' })
  },
  setScore(e) {
    this.setData({ score: e.currentTarget.dataset.s })
  },
  onContent(e) {
    this.setData({ content: e.detail.value })
  },
  onAnon(e) {
    this.setData({ anonymous: e.detail.value })
  },
  chooseImage() {
    wx.chooseImage({
      count: 9 - this.data.images.length,
      success: async (res) => {
        wx.showLoading({ title: '上传中' })
        const urls = [...this.data.images]
        try {
          for (const path of res.tempFilePaths) {
            let url = await uploadFile(path)
            if (!url.startsWith('http')) url = baseUrl + url
            urls.push(url)
          }
          this.setData({ images: urls })
        } finally {
          wx.hideLoading()
        }
      }
    })
  },
  async submit() {
    if (this.data.content.length < 10) {
      wx.showToast({ title: '至少10字', icon: 'none' })
      return
    }
    await post('/api/review/submit', {
      dishId: Number(this.data.dishId),
      score: this.data.score,
      content: this.data.content,
      images: this.data.images,
      isAnonymous: this.data.anonymous ? 1 : 0,
      resubmit: this.data.isResubmit
    })
    wx.removeStorageSync(draftKey(this.data.dishId))
    wx.showToast({ title: '已提交，待审核' })
    setTimeout(() => wx.navigateBack(), 1500)
  }
})
