const { baseUrl } = require('./config')

const PLACEHOLDER = '/assets/placeholder.png'

/**
 * 将后端返回的图片路径转为小程序可加载的完整地址
 * @param {string} url
 * @returns {string}
 */
function resolveImageUrl(url) {
  if (!url) return PLACEHOLDER
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return baseUrl + url
  return baseUrl + '/' + url
}

module.exports = { resolveImageUrl, PLACEHOLDER }
