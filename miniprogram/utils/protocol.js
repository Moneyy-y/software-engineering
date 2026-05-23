const PROTOCOL_KEY = 'user_protocol_agreed'

const { login } = require('./auth')
const { get, post } = require('./request')

function isAgreed() {
  return !!wx.getStorageSync(PROTOCOL_KEY)
}

function setAgreedLocal() {
  wx.setStorageSync(PROTOCOL_KEY, true)
}

function clearAgreedLocal() {
  wx.removeStorageSync(PROTOCOL_KEY)
}

/** 登录后从服务端同步协议状态（换设备 / 清缓存场景） */
function syncFromServer() {
  if (isAgreed()) return Promise.resolve(true)
  return login()
    .then(() => get('/api/user/info'))
    .then((info) => {
      if (info && info.protocolAgreed) {
        setAgreedLocal()
        return true
      }
      return false
    })
    .catch(() => false)
}

/** 勾选同意后：本地标记 + 登录 + 后端记录 */
function markAgreed() {
  setAgreedLocal()
  return login()
    .then(() => post('/api/user/protocol/agree'))
    .catch(() => {})
}

/**
 * 在 Tab 页 onShow 中使用：返回是否已通过协议校验
 * @param {WechatMiniprogram.Page.Instance} page
 * @param {Function} onReady 协议通过后的回调
 */
function gatePageShow(page, onReady) {
  if (isAgreed()) {
    page.setData({ showProtocol: false })
    if (typeof onReady === 'function') onReady.call(page)
    return
  }
  syncFromServer().then((agreed) => {
    page.setData({ showProtocol: !agreed })
    if (agreed && typeof onReady === 'function') onReady.call(page)
  })
}

module.exports = {
  PROTOCOL_KEY,
  isAgreed,
  setAgreedLocal,
  clearAgreedLocal,
  syncFromServer,
  markAgreed,
  gatePageShow
}
