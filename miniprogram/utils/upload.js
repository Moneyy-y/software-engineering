const { baseUrl } = require('./config')

function uploadFile(filePath) {
  const token = wx.getStorageSync('token')
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: baseUrl + '/api/file/upload',
      filePath,
      name: 'file',
      header: { Authorization: token ? `Bearer ${token}` : '' },
      success(res) {
        try {
          const body = JSON.parse(res.data)
          if (body.success) resolve(body.data.url)
          else reject(body)
        } catch (e) {
          reject(e)
        }
      },
      fail: reject
    })
  })
}

module.exports = { uploadFile }
