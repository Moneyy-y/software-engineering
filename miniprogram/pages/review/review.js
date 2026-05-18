const { post } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { uploadFile } = require('../../utils/upload')
const { baseUrl } = require('../../utils/config')

Page({
  data: { dishId: null, score: 5, content: '', anonymous: true, images: [] },
  onLoad(options) {
    this.setData({ dishId: options.dishId })
    login()
  },
  setScore(e) { this.setData({ score: e.currentTarget.dataset.s }) },
  onContent(e) { this.setData({ content: e.detail.value }) },
  onAnon(e) { this.setData({ anonymous: e.detail.value }) },
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
      wx.showToast({ title: '至少10字', icon: 'none' }); return
    }
    await post('/api/review/submit', {
      dishId: Number(this.data.dishId),
      score: this.data.score,
      content: this.data.content,
      images: this.data.images,
      isAnonymous: this.data.anonymous ? 1 : 0
    })
    wx.showToast({ title: '已提交，待审核' })
    setTimeout(() => wx.navigateBack(), 1500)
  }
})
