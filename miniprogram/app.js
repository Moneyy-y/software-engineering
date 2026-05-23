const { login } = require('./utils/auth')

const PROTOCOL_KEY = 'user_protocol_agreed'

function showProtocol() {
  wx.showModal({
    title: '用户协议与隐私政策',
    content: '欢迎使用高校餐饮服务小程序。我们将收集位置信息用于距离排序、浏览记录用于个性化推荐。继续使用即表示您同意本协议。',
    confirmText: '同意',
    cancelText: '不同意',
    success(res) {
      if (res.confirm) {
        wx.setStorageSync(PROTOCOL_KEY, true)
        login().catch(() => {})
      } else {
        wx.showToast({ title: '需同意协议后使用', icon: 'none' })
      }
    }
  })
}

App({
  onLaunch() {
    if (wx.getStorageSync(PROTOCOL_KEY)) {
      login().catch(() => {})
    } else {
      showProtocol()
    }
  },
  globalData: {
    userInfo: null
  }
})
