const { post } = require('./request')

function login() {
  return new Promise((resolve, reject) => {
    if (wx.getStorageSync('token')) {
      resolve(wx.getStorageSync('token'))
      return
    }
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

module.exports = { login }
