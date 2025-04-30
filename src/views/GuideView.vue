<template>
  <div class="guide-view">
    <h2>操作指南</h2>
    <div class="guide-content">
      <div class="guide-item" v-for="(item, index) in guideItems" :key="index">
        <h3>{{ item.title }}</h3>
        <p>{{ item.content }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      guideItems: [
        {
          title: '系统登录',
          content: '使用校园统一身份认证账号登录系统，初始密码为身份证后六位。'
        },
        {
          title: '纤维识别',
          content: '上传纤维样本图片，系统将自动识别纤维类型并显示分析结果。'
        },
        {
          title: '其他功能',
          content: '其他功能的集合。'
        },
        {
          title: '数据导出',
          content: '分析结果可导出为Excel或PDF格式，便于进一步研究使用。'
        }
      ]
    }
  }
}
</script>

<style scoped>
.guide-view {
  padding: 20px;
}

.guide-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.guide-item {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.guide-item h3 {
  color: #1a5fb4;
  margin-top: 0;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
</style>