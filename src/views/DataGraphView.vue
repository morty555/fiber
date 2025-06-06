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
import { API_BASE_URL } from '@/config'
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
 const authToken = localStorage.getItem('jwtToken')

let pieChart, barChart, lineChart

const loadCharts = async () => {
  // 饼图 + 柱状图数据
  const typeRes = await axios.get(`${API_BASE_URL}/function/analysis/type-count`,{
     headers: 
          { 
            'Authorization': `Bearer ${authToken}`
           }
  })
  const typeData = typeRes.data
  console.log('饼图/柱状图数据:', typeData)

  const pieOption = {
    title: { text: '类型占比（饼图）', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      name: '类型',
      type: 'pie',
      radius: '50%',
      data: typeData.data.map(item => ({
        name: item.type,
        value: item.count
      }))
    }]
  }

  const barOption = {
    title: { text: '类型数量（柱状图）', left: 'center' },
    xAxis: { type: 'category', data: typeData.data.map(item => item.type) },
    yAxis: { type: 'value' },
    series: [{
      data: typeData.data.map(item => item.count),
      type: 'bar',
      color: '#409EFF'
    }]
  }

  // 饼图和柱状图渲染
  if (pieChart) {
    pieChart.setOption(pieOption)
  }

  if (barChart) {
    barChart.setOption(barOption)
  }

  // 折线图数据
  const lineRes = await axios.get(`${API_BASE_URL}/function/analysis/daily-count`,{
    headers:{
        'Authorization': `Bearer ${authToken}`
    }
  })
  const lineData = lineRes.data
  console.log('折线图数据:', lineData)

  const lineOption = {
    title: { text: '每日新增数量（折线图）', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: lineData.data.map(item => item.date),  // 使用 lineData.data
    },
    yAxis: { type: 'value' },
    series: [{
      data: lineData.data.map(item => item.count),  // 使用 lineData.data
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  }

  // 折线图渲染
  if (lineChart) {
    lineChart.setOption(lineOption)
  }
}

// 在 onMounted 中初始化
onMounted(() => {
  // 初始化 pieChart, barChart, lineChart
  pieChart = echarts.init(pieChartRef.value)
  barChart = echarts.init(barChartRef.value)
  lineChart = echarts.init(lineChartRef.value)

  // 确保实例正确初始化
  if (pieChart && barChart && lineChart) {
    console.log('Charts initialized successfully')
  } else {
    console.error('Charts initialization failed')
  }

  // 加载数据并设置图表
  loadCharts()

  // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', () => {
    pieChart.resize()
    barChart.resize()
    lineChart.resize()
  })
})




// onBeforeUnmount(() => {
//   window.removeEventListener('resize', () => {
//     pieChart.resize()
//     barChart.resize()
//     lineChart.resize()
//   })
// })

onMounted(() => {
  pieChart = echarts.init(pieChartRef.value)
  barChart = echarts.init(barChartRef.value)
  lineChart = echarts.init(lineChartRef.value)
  loadCharts()
   // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', () => {
    pieChart.resize()
    barChart.resize()
    lineChart.resize()
  })
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

