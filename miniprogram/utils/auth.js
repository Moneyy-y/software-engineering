const { post } = require('./request')

/**
 * 清除本地登录态
 * @returns {void}
 */
function clearAuth() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('userId')
}

/**
 * 是否已登录
 * @returns {boolean}
 */
function isLoggedIn() {
  return !!wx.getStorageSync('token')
}

/**
 * 是否为开发者工具或开发版环境（此类环境跳过 wx.login，避免 access_token expired 报错）
 * @returns {boolean}
 */
function isDevEnvironment() {
  try {
    if (wx.getSystemInfoSync().platform === 'devtools') return true
    const env = wx.getAccountInfoSync().miniProgram.envVersion
    return env === 'develop' || env === 'trial'
  } catch {
    return true
  }
}

/**
 * 使用后端 mock 登录（开发期标准登录方式）
 * @returns {Promise<string>}
 */
function mockDevLogin() {
  return post('/api/user/login', { code: 'dev', nickname: '微信用户' })
    .then((data) => {
      wx.setStorageSync('token', data.token)
      wx.setStorageSync('userId', data.userId)
      return data.token
    })
}

/**
 * 登录：开发环境直接 mock；正式版才调用 wx.login
 * @param {{ force?: boolean }} options
 * @returns {Promise<string>}
 */
function login(options = {}) {
  const force = options.force === true
  return new Promise((resolve, reject) => {
    if (!force && wx.getStorageSync('token')) {
      resolve(wx.getStorageSync('token'))
      return
    }
    if (force) clearAuth()

    if (isDevEnvironment()) {
      mockDevLogin().then(resolve).catch(reject)
      return
    }

    wx.login({
      success(res) {
        post('/api/user/login', { code: res.code || 'dev', nickname: '微信用户' })
          .then((data) => {
            wx.setStorageSync('token', data.token)
            wx.setStorageSync('userId', data.userId)
            resolve(data.token)
          })
          .catch(() => mockDevLogin().then(resolve).catch(reject))
      },
      fail() {
        mockDevLogin().then(resolve).catch(reject)
      }
    })
  })
}

module.exports = { login, clearAuth, isLoggedIn, mockDevLogin, isDevEnvironment }
