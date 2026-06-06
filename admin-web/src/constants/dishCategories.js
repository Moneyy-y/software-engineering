/** 菜品分类选项（与小程序首页筛选保持一致，汉堡炸鸡替代原小吃炸串展示名） */
export const DISH_CATEGORIES = [
  '特色小炒',
  '面食粥粉',
  '快餐便当',
  '奶茶饮品',
  '汉堡炸鸡'
]

/**
 * 将数据库中的分类规范为表单展示值
 * @param {string} category
 * @returns {string}
 */
export function normalizeCategory(category) {
  if (category === '小吃炸串') return '汉堡炸鸡'
  return category || DISH_CATEGORIES[0]
}

/**
 * 将表单分类映射为档口表中的品类字段
 * @param {string} category
 * @returns {string}
 */
export function toStallCategory(category) {
  if (category === '汉堡炸鸡') return '小吃炸串'
  return category
}
