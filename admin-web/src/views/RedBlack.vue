<template>
  <div>
    <el-card style="margin-bottom:16px">
      <div class="toolbar">
        <span class="hint">人工干预将同步至小程序红黑榜；隐藏后用户端不可见，管理端仍可操作恢复。</span>
        <div>
          <el-button type="primary" @click="openAdd('red')">加入红榜</el-button>
          <el-button @click="openAdd('black')">加入黑榜</el-button>
          <el-button type="warning" :loading="calculating" @click="recalculate">重新计算榜单</el-button>
          <el-button @click="load">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span class="red-title">红榜管理</span></template>
          <el-table :data="redList" v-loading="loading" empty-text="暂无红榜菜品">
            <el-table-column label="#" width="50">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column prop="name" label="菜品" min-width="120" />
            <el-table-column prop="avgScore" label="评分" width="70" />
            <el-table-column prop="saleCount" label="销量" width="70" />
            <el-table-column label="状态" width="140">
              <template #default="{ row }">
                <el-tag v-if="row.boardHidden" type="info" size="small">已隐藏</el-tag>
                <el-tag v-if="row.pinned" type="warning" size="small">置顶</el-tag>
                <el-tag :type="row.source === 'manual' ? 'danger' : ''" size="small">
                  {{ row.source === 'manual' ? '人工' : '系统' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row, $index }">
                <el-button link type="primary" :disabled="$index === 0" @click="move(row, 'move_up_red')">上移</el-button>
                <el-button link type="primary" :disabled="$index === redList.length - 1" @click="move(row, 'move_down_red')">下移</el-button>
                <el-button link type="warning" @click="intervene(row.dishId, row.pinned ? 'unpin' : 'pin_red')">
                  {{ row.pinned ? '取消置顶' : '置顶' }}
                </el-button>
                <el-button link @click="intervene(row.dishId, row.boardHidden ? 'show' : 'hide')">
                  {{ row.boardHidden ? '显示' : '隐藏' }}
                </el-button>
                <el-popconfirm title="确认移出红榜？" @confirm="intervene(row.dishId, 'remove_red')">
                  <template #reference>
                    <el-button link type="danger">移出</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header><span class="black-title">黑榜管理</span></template>
          <el-table :data="blackList" v-loading="loading" empty-text="暂无黑榜菜品">
            <el-table-column label="#" width="50">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column prop="name" label="菜品" min-width="120" />
            <el-table-column prop="avgScore" label="评分" width="70" />
            <el-table-column prop="reviewCount" label="评价数" width="70" />
            <el-table-column label="状态" width="140">
              <template #default="{ row }">
                <el-tag v-if="row.boardHidden" type="info" size="small">已隐藏</el-tag>
                <el-tag v-if="row.pinned" type="warning" size="small">置顶</el-tag>
                <el-tag :type="row.source === 'manual' ? 'danger' : ''" size="small">
                  {{ row.source === 'manual' ? '人工' : '系统' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row, $index }">
                <el-button link type="primary" :disabled="$index === 0" @click="move(row, 'move_up_black')">上移</el-button>
                <el-button link type="primary" :disabled="$index === blackList.length - 1" @click="move(row, 'move_down_black')">下移</el-button>
                <el-button link type="warning" @click="intervene(row.dishId, row.pinned ? 'unpin' : 'pin_black')">
                  {{ row.pinned ? '取消置顶' : '置顶' }}
                </el-button>
                <el-button link @click="intervene(row.dishId, row.boardHidden ? 'show' : 'hide')">
                  {{ row.boardHidden ? '显示' : '隐藏' }}
                </el-button>
                <el-popconfirm title="确认移出黑榜？" @confirm="intervene(row.dishId, 'remove_black')">
                  <template #reference>
                    <el-button link type="danger">移出</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="addVisible" :title="addTarget === 'red' ? '加入红榜' : '加入黑榜'" width="480px">
      <el-select
        v-model="selectedDishId"
        filterable
        placeholder="搜索并选择菜品"
        style="width:100%"
        :loading="dishLoading"
      >
        <el-option
          v-for="d in dishOptions"
          :key="d.dishId"
          :label="`${d.name}（评分 ${d.avgScore ?? '-'}）`"
          :value="d.dishId"
        />
      </el-select>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedDishId" @click="confirmAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const redList = ref([])
const blackList = ref([])
const loading = ref(false)
const calculating = ref(false)
const addVisible = ref(false)
const addTarget = ref('red')
const selectedDishId = ref(null)
const dishOptions = ref([])
const dishLoading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await request.get('/api/admin/board/list') || {}
    redList.value = data.redList || []
    blackList.value = data.blackList || []
  } finally {
    loading.value = false
  }
}

async function intervene(dishId, action) {
  await request.post('/api/admin/board/intervene', null, { params: { dishId, action } })
  ElMessage.success('操作成功')
  await load()
}

async function move(row, action) {
  await intervene(row.dishId, action)
}

async function recalculate() {
  calculating.value = true
  try {
    await request.post('/api/admin/board/calculate')
    ElMessage.success('榜单已重新计算')
    await load()
  } finally {
    calculating.value = false
  }
}

async function openAdd(target) {
  addTarget.value = target
  selectedDishId.value = null
  addVisible.value = true
  dishLoading.value = true
  try {
    const res = await request.get('/api/dish/list', { params: { page: 1, size: 200 } })
    dishOptions.value = res.records || []
  } finally {
    dishLoading.value = false
  }
}

async function confirmAdd() {
  const action = addTarget.value === 'red' ? 'add_red' : 'add_black'
  await intervene(selectedDishId.value, action)
  addVisible.value = false
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.hint {
  color: #909399;
  font-size: 13px;
}
.red-title {
  color: #f56c6c;
  font-weight: 600;
}
.black-title {
  color: #606266;
  font-weight: 600;
}
</style>
