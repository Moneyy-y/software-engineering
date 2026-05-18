<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card>
        <template #header><span style="color:#f56c6c">红榜 TOP10</span></template>
        <el-table :data="redList">
          <el-table-column type="index" width="50" />
          <el-table-column prop="name" label="菜品" />
          <el-table-column prop="avgScore" label="评分" width="80" />
          <el-table-column prop="saleCount" label="销量" width="80" />
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <template #header><span style="color:#909399">黑榜 TOP10</span></template>
        <el-table :data="blackList">
          <el-table-column type="index" width="50" />
          <el-table-column prop="name" label="菜品" />
          <el-table-column prop="avgScore" label="评分" width="80" />
          <el-table-column prop="reviewCount" label="评价数" width="80" />
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const redList = ref([])
const blackList = ref([])

onMounted(async () => {
  const data = await request.get('/api/recommend/redblack')
  redList.value = data.red || []
  blackList.value = data.black || []
})
</script>
