const { post } = require('./request')

function clearAuth() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('userId')
}

function isLoggedIn() {
  return !!wx.getStorageSync('token')
}

function login(options = {}) {
  const force = options.force === true
  return new Promise((resolve, reject) => {
    if (!force && wx.getStorageSync('token')) {
      resolve(wx.getStorageSync('token'))
      return
    }
    if (force) clearAuth()
    wx.login({
      success(res) {
        post('/api/user/login', { code: res.code || 'dev', nickname: '微信用户' })
          .then(data => {
            wx.setStorageSync('token', data.token)
            wx.setStorageSync('userId', data.userId)
            resolve(data.token)
          })
          .catch(reject)
      },
      fail: reject
    })
  })
}

module.exports = { login, clearAuth, isLoggedIn }
