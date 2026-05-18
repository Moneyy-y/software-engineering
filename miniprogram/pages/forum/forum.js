const { get, post } = require('../../utils/request')
const { login } = require('../../utils/auth')

Page({
  data: { posts: [] },
  onShow() {
    login().then(() => this.load())
  },
  async load() {
    const posts = await get('/api/post/list') || []
    this.setData({ posts })
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/forum-detail/forum-detail?id=${id}` })
  },
  showPublish() {
    wx.showModal({
      title: '发帖标题',
      editable: true,
      success: (res) => {
        if (!res.confirm || !res.content) return
        const title = res.content
        wx.showModal({
          title: '帖子内容',
          editable: true,
          success: async (r2) => {
            if (r2.confirm && r2.content) {
              await post('/api/post/publish', { title, content: r2.content, zone: 'general' })
              wx.showToast({ title: '已提交审核' })
              this.load()
            }
          }
        })
      }
    })
  }
})
