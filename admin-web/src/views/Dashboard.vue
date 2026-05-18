<template>
  <div>
    <el-button type="primary" style="margin-bottom:16px" @click="exportReport">导出报表 CSV</el-button>
    <el-row :gutter="16" class="stats">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-num">{{ data.todayReviewCount || 0 }}</div>
          <div class="stat-label">今日评价</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-num">{{ data.pendingAuditCount || 0 }}</div>
          <div class="stat-label">待审核</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-num">{{ data.pendingFeedbackCount || 0 }}</div>
          <div class="stat-label">待处理反馈</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-num">{{ data.totalDishCount || 0 }}</div>
          <div class="stat-label">菜品总数</div>
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
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'

const data = ref({})
const trendRef = ref()
const complaintRef = ref()
const hotRef = ref()

onMounted(async () => {
  data.value = await request.get('/api/statistics/dashboard') || {}
  initCharts()
})

function initCharts() {
  const trend = echarts.init(trendRef.value)
  trend.setOption({
    title: { text: '近7天评分趋势' },
    xAxis: { type: 'category', data: (data.value.scoreTrendData || []).map(d => d.date) },
    yAxis: { type: 'value', min: 0, max: 5 },
    series: [{ type: 'line', data: (data.value.scoreTrendData || []).map(d => d.avgScore), smooth: true }]
  })
  const complaint = echarts.init(complaintRef.value)
  complaint.setOption({
    title: { text: '投诉分布' },
    series: [{ type: 'pie', radius: '60%', data: (data.value.complaintDistData || []).map(d => ({ name: d.type, value: d.count })) }]
  })
  const hot = echarts.init(hotRef.value)
  hot.setOption({
    title: { text: '热门菜品 TOP10' },
    xAxis: { type: 'category', data: (data.value.hotDishTop10 || []).map(d => d.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: (data.value.hotDishTop10 || []).map(d => d.saleCount) }]
  })
}

async function exportReport() {
  const token = localStorage.getItem('token')
  const res = await fetch('/api/statistics/export', {
    headers: { Authorization: `Bearer ${token}` }
  })
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'dashboard-report.csv'
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.stat-num { font-size: 32px; font-weight: bold; color: #409EFF; }
.stat-label { color: #909399; margin-top: 8px; }
.stats .el-card { text-align: center; padding: 16px; }
</style>
