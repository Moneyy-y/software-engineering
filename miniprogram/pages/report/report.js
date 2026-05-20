const { post } = require('../../utils/request')
const { login } = require('../../utils/auth')

const TYPE_LABEL = { post: '帖子', review: '评价', comment: '评论' }

Page({
  data: {
    targetType: 'post',
    targetId: null,
    targetLabel: '',
    reasons: ['虚假信息', '辱骂谩骂', '广告 spam', '违法违规', '其他'],
    reason: '其他',
    description: ''
  },
  onLoad(options) {
    const targetType = options.targetType || 'post'
    const targetId = options.targetId
    if (!targetId) {
      wx.showToast({ title: '举报对象无效', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    login().catch(() => {})
    this.setData({
      targetType,
      targetId,
      targetLabel: `${TYPE_LABEL[targetType] || targetType} #${targetId}`
    })
  },
  onReason(e) {
    this.setData({ reason: this.data.reasons[e.detail.value] })
  },
  onDesc(e) {
    this.setData({ description: e.detail.value })
  },
  async submit() {
    const desc = (this.data.description || '').trim()
    if (desc.length < 5) {
      wx.showToast({ title: '请至少填写5字说明', icon: 'none' })
      return
    }
    try {
      await post('/api/report/submit', {
        targetType: this.data.targetType,
        targetId: Number(this.data.targetId),
        reason: this.data.reason,
        description: desc
      })
      wx.showToast({ title: '举报已提交' })
      setTimeout(() => wx.navigateBack(), 1500)
    } catch (_) {}
  }
})
