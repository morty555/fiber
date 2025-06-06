<template>
  <div>
    <div class="chart-grid">
      <div ref="pieChartRef" style="height: 300px;"></div>
      <div ref="barChartRef" style="height: 300px;"></div>
    </div>
    <div ref="lineChartRef" style="height: 400px; margin-top: 30px;"></div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { ref, onMounted } from 'vue'
import axios from 'axios'

const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)

let pieChart, barChart, lineChart

const loadCharts = async () => {
  // 饼图 + 柱状图数据
  const typeRes = await axios.get('/api/function/analysis/type-count')
  const typeData = typeRes.data

  const pieOption = {
    title: { text: '类型占比（饼图）', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      name: '类型',
      type: 'pie',
      radius: '50%',
      data: typeData.map(item => ({
        name: item.type,
        value: item.count
      }))
    }]
  }

  const barOption = {
    title: { text: '类型数量（柱状图）', left: 'center' },
    xAxis: { type: 'category', data: typeData.map(item => item.type) },
    yAxis: { type: 'value' },
    series: [{
      data: typeData.map(item => item.count),
      type: 'bar',
      color: '#409EFF'
    }]
  }

  pieChart.setOption(pieOption)
  barChart.setOption(barOption)

  // 折线图数据
  const lineRes = await axios.get('/api/analysis/daily-count')
  const lineData = lineRes.data

  const lineOption = {
    title: { text: '每日新增数量（折线图）', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: lineData.map(item => item.date) },
    yAxis: { type: 'value' },
    series: [{
      data: lineData.map(item => item.count),
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  }

  lineChart.setOption(lineOption)
}

onMounted(() => {
  pieChart = echarts.init(pieChartRef.value)
  barChart = echarts.init(barChartRef.value)
  lineChart = echarts.init(lineChartRef.value)
  loadCharts()
})
</script>

<style scoped>
.chart-grid {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}
.chart-grid > div {
  width: 48%;
}
</style>

