const { get, put, del } = require('../../utils/request')
const { login } = require('../../utils/auth')

const TYPE_LABEL = {
  review_approve: '评价通过',
  review_reject: '评价未通过',
  feedback_reply: '反馈回复',
  feedback_resolved: '反馈完成',
  post_submit: '帖子提交',
  post_approve: '帖子通过',
  post_reject: '帖子未通过'
}

function normalizeMessage(m, typeLabel) {
  const messageId = m.messageId != null ? m.messageId : m.message_id
  const relatedType = m.relatedType || m.related_type || ''
  const relatedId = m.relatedId != null ? m.relatedId : m.related_id
  const dishId = m.dishId != null ? m.dishId : m.dish_id
  const type = m.type || ''
  let isRead = m.isRead
  if (isRead == null) isRead = m.is_read
  if (isRead == null) isRead = m.read
  return {
    messageId,
    title: m.title || '',
    content: m.content || '',
    type,
    typeLabelText: (typeLabel && typeLabel[type]) || '系统',
    createTime: m.createTime || m.create_time || '',
    isRead: !!isRead,
    relatedType,
    relatedId,
    dishId,
    linkable: !!(relatedType && relatedId != null)
  }
}

function parseMessageList(raw) {
  if (Array.isArray(raw)) return raw
  if (raw && Array.isArray(raw.list)) return raw.list
  if (raw && Array.isArray(raw.records)) return raw.records
  return []
}

Page({
  data: {
    list: [],
    loading: true,
    loadError: ''
  },
  onShow() {
    login().then(() => this.load()).catch((e) => {
      this.setData({
        loading: false,
        loadError: (e && e.message) || '登录失败，请重试'
      })
    })
  },
  onUnload() {
    const pages = getCurrentPages()
    const prev = pages[pages.length - 2]
    if (prev && typeof prev.loadUnread === 'function') {
      prev.loadUnread()
    }
  },
  onPullDownRefresh() {
    this.load().finally(() => wx.stopPullDownRefresh())
  },
  async load() {
    this.setData({ loading: true, loadError: '' })
    try {
      const raw = await get('/api/user/message/list')
      const list = parseMessageList(raw)
        .map((m) => normalizeMessage(m, TYPE_LABEL))
        .filter((m) => m.messageId != null)
      this.setData({ list, loading: false, loadError: '' })
      if (list.some((m) => !m.isRead)) {
        try {
          await put('/api/user/message/read')
        } catch (e) { /* 标记已读失败不影响列表展示 */ }
      }
    } catch (e) {
      this.setData({
        list: [],
        loading: false,
        loadError: (e && e.message) || '加载失败，请下拉刷新'
      })
    }
  },
  openMessage(e) {
    const index = e.currentTarget.dataset.index
    const item = this.data.list[index]
    if (!item) return
    if (!item.linkable) {
      wx.showToast({ title: '该消息暂不支持跳转', icon: 'none' })
      return
    }
    if (item.relatedType === 'post') {
      if (item.type === 'post_approve') {
        wx.navigateTo({ url: `/pages/forum-detail/forum-detail?id=${item.relatedId}` })
      } else if (item.type === 'post_reject') {
        wx.navigateTo({ url: `/pages/forum/forum?tab=my&resubmitPostId=${item.relatedId}` })
      } else {
        wx.navigateTo({ url: '/pages/forum/forum?tab=my' })
      }
      return
    }
    if (item.relatedType === 'review') {
      if (item.type === 'review_reject') {
        wx.navigateTo({ url: `/pages/my-reviews/my-reviews?reviewId=${item.relatedId}` })
      } else if (item.dishId) {
        wx.navigateTo({ url: `/pages/detail/detail?id=${item.dishId}` })
      } else {
        wx.navigateTo({ url: '/pages/my-reviews/my-reviews' })
      }
      return
    }
    if (item.relatedType === 'feedback') {
      wx.navigateTo({ url: '/pages/feedback/feedback' })
    }
  },
  clearAll() {
    wx.showModal({
      title: '清空消息',
      content: '确定删除全部消息吗？',
      success: async (res) => {
        if (!res.confirm) return
        await del('/api/user/message/clear')
        wx.showToast({ title: '已清空', icon: 'none' })
        this.setData({ list: [] })
      }
    })
  },
  async removeOne(e) {
    const id = e.currentTarget.dataset.id
    await del(`/api/user/message/${id}`)
    this.load()
  }
})
