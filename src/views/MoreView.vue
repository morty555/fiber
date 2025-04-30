<template>
  <div class="more-view">
    <h2>更多信息</h2>
    
    <div class="info-sections">
      <section class="info-section">
        <h3>关于我们</h3>
        <p>XX大学纤维研究中心成立于xxxx年，是国内领先的纤维材料研究机构。中心拥有先进的实验设备和专业的研究团队，致力于纤维材料的创新研究和应用开发。</p>
      </section>
      
      <section class="info-section">
        <h3>联系我们</h3>
        <ul class="contact-list">
          <li><strong>电话：</strong> 0123-4567890</li>
          <li><strong>邮箱：</strong> fiber@university.edu</li>
          <li><strong>地址：</strong> XX省XX市XX区XX路XX号 纤维研究中心大楼</li>
        </ul>
      </section>
      
      <section class="info-section">
        <h3>相关链接</h3>
        <div class="links">
          <a href="#" class="link-item">XX大学官网</a>
          <a href="#" class="link-item">xxx学院</a>
          <a href="#" class="link-item">研究成果数据库</a>
          <a href="#" class="link-item">学术期刊</a>
        </div>
      </section>
    </div>
    
    <div class="news-section">
      <h3>最新动态</h3>
      <div class="news-list">
        <div class="news-item" v-for="(news, index) in newsList" :key="index">
          <div class="news-date">{{ news.date }}</div>
          <div class="news-content">
            <h4>{{ news.title }}</h4>
            <p>{{ news.summary }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      newsList: [
        {
          date: '2023-05-15',
          title: '纤维研究中心举办年度学术研讨会',
          summary: '本次研讨会邀请了国内外多位知名专家，共同探讨纤维材料领域的最新研究成果和发展趋势。'
        },
        {
          date: '2023-04-20',
          title: '新型环保纤维材料研发成功',
          summary: '我中心研发团队成功开发出一种可降解的新型环保纤维材料，已申请国家专利。'
        },
        {
          date: '2023-03-10',
          title: '纤维识别系统升级公告',
          summary: '纤维识别系统已完成升级，新增多种纤维类型识别功能，识别准确率提升至95%以上。'
        }
      ]
    }
  }
}
</script>

<style scoped>
.more-view {
  padding: 20px;
}

.info-sections {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
  margin: 20px 0;
}

.info-section {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.contact-list {
  list-style: none;
  padding: 0;
}

.contact-list li {
  margin-bottom: 10px;
}

.links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.link-item {
  color: #1a5fb4;
  text-decoration: none;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  transition: color 0.3s;
}

.link-item:hover {
  color: #0d4b9e;
}

.news-section {
  margin-top: 40px;
}

.news-list {
  margin-top: 20px;
}

.news-item {
  display: flex;
  margin-bottom: 20px;
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.news-date {
  flex: 0 0 120px;
  background-color: #1a5fb4;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 15px;
  font-weight: bold;
}

.news-content {
  flex: 1;
  padding: 15px;
}

.news-content h4 {
  margin-top: 0;
  color: #1a5fb4;
}

.news-content p {
  margin-bottom: 0;
}
</style>