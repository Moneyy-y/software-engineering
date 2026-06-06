import request from '../utils/request'

/**
 * 获取全部食堂/商铺（管理端，含已停用）
 * @returns {Promise<Array>}
 */
export function listAllShops() {
  return request.get('/api/admin/shop/list')
}

/**
 * 获取可选食堂列表（启用中，与小程序一致）
 * @returns {Promise<Array>}
 */
export function listSelectableShops() {
  return request.get('/api/shop/list')
}

/**
 * 保存食堂/商铺
 * @param {object} data
 * @returns {Promise<object>}
 */
export function saveShop(data) {
  return request.post('/api/admin/shop/save', data)
}

/**
 * 删除食堂/商铺（通过 save 接口软删除，避免独立 delete 路由 404）
 * @param {{ shopId: number, name: string, type: number, address?: string, logo?: string }} shop
 * @returns {Promise<object>}
 */
export function deleteShop(shop) {
  return saveShop({
    shopId: shop.shopId,
    name: shop.name,
    type: shop.type,
    address: shop.address || '',
    logo: shop.logo || '',
    status: 0
  })
}

/**
 * 获取档口列表
 * @param {number} shopId
 * @returns {Promise<Array>}
 */
export function listStalls(shopId) {
  return request.get('/api/admin/stall/list', { params: { shopId } })
}

/**
 * 保存档口
 * @param {object} data
 * @returns {Promise<object>}
 */
export function saveStall(data) {
  return request.post('/api/admin/stall/save', data)
}

/**
 * 删除档口（通过 save 接口软删除）
 * @param {{ stallId: number, shopId: number, name: string, category?: string }} stall
 * @returns {Promise<object>}
 */
export function deleteStall(stall) {
  return saveStall({
    stallId: stall.stallId,
    shopId: stall.shopId,
    name: stall.name,
    category: stall.category || '',
    status: 0
  })
}
