const { baseUrl } = require('./config')

function request(url, method = 'GET', data = {}) {
  const token = wx.getStorageSync('token')
  return new Promise((resolve, reject) => {
    wx.request({
      url: baseUrl + url,
      method,
      data,
      header: {
        'Content-Type': method === 'GET' ? 'application/json' : 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success(res) {
        try {
          const body = res.data
          if (!body || typeof body !== 'object') {
            reject({ message: '响应格式错误' })
            return
          }
          if (res.statusCode === 401) {
            wx.showToast({ title: body.message || '请先登录', icon: 'none' })
            reject(body)
            return
          }
          if (body.success) {
            resolve(body.data)
          } else {
            wx.showToast({ title: body.message || '请求失败', icon: 'none' })
            reject(body)
          }
        } catch (err) {
          reject({ message: (err && err.message) || '解析响应失败' })
        }
      },
      fail(err) {
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

function post(url, data) {
  if (data && typeof data === 'object' && data.params) {
    const q = Object.keys(data.params).map(k => `${k}=${encodeURIComponent(data.params[k])}`).join('&')
    return request(url + (url.includes('?') ? '&' : '?') + q, 'POST', data.body || {})
  }
  return request(url, 'POST', data)
}

function put(url, data) {
  return request(url, 'PUT', data || {})
}

function del(url, data) {
  return request(url, 'DELETE', data || {})
}

module.exports = {
  request,
  get: (url, data) => request(url, 'GET', data),
  post,
  put,
  del
}
