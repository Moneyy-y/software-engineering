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
    if (!this.data.comment.trim()) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' })
      return
    }
    await post('/api/post/comment', {
      params: { postId: this.data.postId, content: this.data.comment }
    })
    this.setData({ comment: '' })
    this.load()
  },
  async toggleLike() {
    const wasLiked = this.data.post.liked
    await post('/api/post/like', { params: { postId: this.data.postId } })
    wx.showToast({ title: wasLiked ? '已取消点赞' : '已点赞', icon: 'none' })
    this.load()
  },
  goReport() {
    wx.navigateTo({
      url: `/pages/report/report?targetType=post&targetId=${this.data.postId}`
    })
  }
})
