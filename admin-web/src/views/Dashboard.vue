<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="exportReport('csv')">导出 CSV</el-button>
      <el-button type="success" @click="exportReport('excel')">导出 Excel</el-button>
      <el-button :loading="refreshing" @click="refresh">刷新数据</el-button>
    </div>

    <el-row :gutter="16" class="stats">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-num">{{ data.todayReviewCount || 0 }}</div>
          <div class="stat-label">今日评价</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="todo-card" @click="goTodo('/audit')">
          <div class="stat-num warn">{{ data.pendingReviewCount || 0 }}</div>
          <div class="stat-label">待审核评价</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="todo-card" @click="goTodo('/post-audit')">
          <div class="stat-num warn">{{ data.pendingPostCount || 0 }}</div>
          <div class="stat-label">待审核帖子</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-num">{{ data.pendingFeedbackCount || 0 }}</div>
          <div class="stat-label">待处理反馈</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12"><el-card><div ref="trendRef" style="height:300px"></div></el-card></el-col>
      <el-col :span="12"><el-card><div ref="complaintRef" style="height:300px"></div></el-card></el-col>
    </el-row>
    <el-row style="margin-top:16px">
      <el-col :span="24"><el-card><div ref="hotRef" style="height:300px"></div></el-card></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '../utils/request'

const router = useRouter()
const data = ref({})
const refreshing = ref(false)
const trendRef = ref()
const complaintRef = ref()
const hotRef = ref()
let trendChart
let complaintChart
let hotChart

onMounted(refresh)

async function refresh() {
  refreshing.value = true
  try {
    data.value = await request.get('/api/statistics/dashboard') || {}
    await nextTick()
    initCharts()
  } finally {
    refreshing.value = false
  }
}

function initCharts() {
  if (!trendChart) trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    title: { text: '近7天评分趋势' },
    xAxis: { type: 'category', data: (data.value.scoreTrendData || []).map(d => d.date) },
    yAxis: { type: 'value', min: 0, max: 5 },
    series: [{ type: 'line', data: (data.value.scoreTrendData || []).map(d => d.avgScore), smooth: true }]
  })
  if (!complaintChart) complaintChart = echarts.init(complaintRef.value)
  complaintChart.setOption({
    title: { text: '投诉分布' },
    series: [{ type: 'pie', radius: '60%', data: (data.value.complaintDistData || []).map(d => ({ name: d.type, value: d.count })) }]
  })
  if (!hotChart) hotChart = echarts.init(hotRef.value)
  hotChart.setOption({
    title: { text: '热门菜品 TOP10' },
    xAxis: { type: 'category', data: (data.value.hotDishTop10 || []).map(d => d.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: (data.value.hotDishTop10 || []).map(d => d.saleCount) }]
  })
}

function goTodo(path) {
  router.push(path)
}

async function exportReport(type) {
  const token = localStorage.getItem('token')
  const url = type === 'excel' ? '/api/statistics/export/excel' : '/api/statistics/export'
  const filename = type === 'excel' ? 'dashboard-report.xlsx' : 'dashboard-report.csv'
  const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } })
  if (!res.ok) {
    return
  }
  const blob = await res.blob()
  const link = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = link
  a.download = filename
  a.click()
  URL.revokeObjectURL(link)
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.stat-num { font-size: 32px; font-weight: bold; color: #409EFF; }
.stat-num.warn { color: #E6A23C; }
.stat-label { color: #909399; margin-top: 8px; }
.stats .el-card { text-align: center; padding: 16px; }
.todo-card { cursor: pointer; }
.todo-card:hover { border-color: #409EFF; }
</style>
