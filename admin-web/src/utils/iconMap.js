import {
  DataAnalysis,
  DataLine,
  DataBoard,
  OfficeBuilding,
  Shop,
  Goods,
  Food,
  Dish,
  DocumentChecked,
  Edit,
  Document,
  ChatLineSquare,
  Warning,
  ChatDotRound,
  ChatDotSquare,
  Trophy,
  Lock,
  User,
  Setting
} from '@element-plus/icons-vue'

const iconMap = {
  'DataLine': DataLine,
  'DataAnalysis': DataAnalysis,
  'DataBoard': DataBoard,
  'Shop': Shop,
  'OfficeBuilding': OfficeBuilding,
  'Food': Food,
  'Goods': Goods,
  'Dish': Dish,
  'Edit': Edit,
  'Document': Document,
  'DocumentChecked': DocumentChecked,
  'ChatLineSquare': ChatLineSquare,
  'Warning': Warning,
  'ChatDotRound': ChatDotRound,
  'ChatDotSquare': ChatDotSquare,
  'Trophy': Trophy,
  'Lock': Lock,
  'User': User,
  'Setting': Setting
}

export function getIconComponent(iconName) {
  return iconMap[iconName] || null
}
