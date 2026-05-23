Page({
  onLoad(options) {
    let url = '/pages/forum/forum?tab=my'
    if (options.resubmitPostId) {
      url += `&resubmitPostId=${options.resubmitPostId}`
    }
    wx.redirectTo({ url })
  }
})
