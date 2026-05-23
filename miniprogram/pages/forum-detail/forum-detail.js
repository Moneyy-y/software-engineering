const { get, post } = require('../../utils/request')
const { login } = require('../../utils/auth')
const { baseUrl } = require('../../utils/config')

Page({
  data: { post: null, comment: '', postId: null },
  onLoad(options) {
    this.setData({ postId: options.id })
    login().then(() => this.load())
  },
  async load() {
    const post = await get(`/api/post/${this.data.postId}`)
    if (post.images) {
      post.images = post.images.map((img) => (img.startsWith('http') ? img : baseUrl + img))
    }
    this.setData({ post })
  },
  onComment(e) {
    this.setData({ comment: e.detail.value })
  },
  async sendComment() {
    if (!this.data.comment.trim()) return
    await post('/api/post/comment', {
      params: { postId: this.data.postId, content: this.data.comment }
    })
    this.setData({ comment: '' })
    this.load()
  },
  async toggleLike() {
    await post('/api/post/like', { params: { postId: this.data.postId } })
    this.load()
  },
  goReport() {
    wx.navigateTo({
      url: `/pages/report/report?targetType=post&targetId=${this.data.postId}`
    })
  }
})
