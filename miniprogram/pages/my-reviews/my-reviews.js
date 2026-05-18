const { get } = require('../../utils/request')
const { login } = require('../../utils/auth')

Page({
  data: {
    list: [],
    statusText: { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  },
  onShow() {
    login().then(() => this.load())
  },
  async load() {
    const list = await get('/api/user/review/my') || []
    this.setData({ list })
  }
})
