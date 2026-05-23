/**
 * 双列瀑布流：将列表按估算/实测高度分配到左右列，使两列总高度尽量接近
 */

function formatDish(dish) {
  return {
    ...dish,
    distanceText: dish.distanceKm != null ? `${dish.distanceKm.toFixed(1)}km` : ''
  }
}

/** 根据文案与 dishId 估算卡片高度（rpx 量级数值，仅用于比较） */
function estimateHeight(dish) {
  const nameLen = (dish.name || '').length
  const nameLines = nameLen <= 7 ? 1 : nameLen <= 14 ? 2 : 3
  const imgH = 200 + ((dish.dishId || 0) % 5) * 36
  const infoH = 72 + nameLines * 36 + (dish.category ? 28 : 0) + 32
  return imgH + infoH
}

/**
 * @param {Array} dishes
 * @param {Function} [getHeight] 返回单项高度，默认 estimateHeight
 */
function splitWaterfall(dishes, getHeight) {
  const heightOf = getHeight || estimateHeight
  const leftColumn = []
  const rightColumn = []
  let leftH = 0
  let rightH = 0
  const gap = 16

  dishes.forEach((raw) => {
    const dish = formatDish(raw)
    const h = heightOf(raw)
    if (leftH <= rightH) {
      leftColumn.push(dish)
      leftH += h + gap
    } else {
      rightColumn.push(dish)
      rightH += h + gap
    }
  })

  return { leftColumn, rightColumn }
}

module.exports = { splitWaterfall, estimateHeight, formatDish }
