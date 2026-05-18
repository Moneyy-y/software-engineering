const { login } = require('./utils/auth')

App({
  onLaunch() {
    login().catch(() => {})
  },
  globalData: {
    userInfo: null
  }
})
