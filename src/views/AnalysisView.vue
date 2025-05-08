<template>
  <div class="analysis-view">
    <h2>图像分析</h2>
    
    <div class="analysis-tools">
      <div 
        class="tool-card" 
        v-for="tool in tools" 
        :key="tool.id"
        @click="selectTool(tool.id)"
        :class="{ active: selectedTool === tool.id }"
      >
        <div class="tool-icon">
          {{ tool.icon }}
        </div>
        <h3>{{ tool.name }}</h3>
        <p>{{ tool.description }}</p>
      </div>
    </div>
    
    <div class="analysis-area" v-if="selectedTool">
      <h3>{{ selectedToolData.name }}</h3>
      <div class="tool-content">
        <div class="image-container">
          <img :src="sampleImage" alt="样本图片">
          <div class="analysis-overlay" v-if="showAnalysisResult"></div>
        </div>
        <div class="tool-controls">
          <button class="upload-btn" @click="uploadImage">上传图片</button>
          <button class="analyze-btn" @click="runAnalysis" :disabled="!sampleImage">
            执行分析
          </button>
          <div class="tool-params" v-if="selectedToolData.params">
            <div class="param-item" v-for="param in selectedToolData.params" :key="param.name">
              <label :for="param.name">{{ param.label }}:</label>
              <input 
                :type="param.type" 
                :id="param.name" 
                v-model="paramValues[param.name]"
                :step="param.step || 1"
                :min="param.min || 0"
              >
            </div>
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
      tools: [
        {
          id: 'density',
          name: '历史记录',
          icon: '📊',
          description: '纤维图片与分析结果的历史记录',
          params: [
            { name: 'threshold', label: '阈值', type: 'number', min: 0, max: 255, step: 1 },
            { name: 'minArea', label: '最小面积', type: 'number', min: 1, step: 1 }
          ]
        },
        {
          id: 'length',
          name: '功能',
          icon: '📏',
          description: '功能描述'
        },
        {
          id: 'color',
          name: '功能',
          icon: '🎨',
          description: '功能描述'
        },
        {
          id: 'orientation',
          name: '功能',
          icon: '🧭',
          description: '功能描述'
        }
      ],
      selectedTool: null,
      sampleImage: 'https://via.placeholder.com/500x300?text=纤维样本图片',
      showAnalysisResult: false,
      paramValues: {}
    }
  },
  computed: {
    selectedToolData() {
      return this.tools.find(tool => tool.id === this.selectedTool) || {}
    }
  },
  methods: {
    selectTool(toolId) {
      this.selectedTool = toolId
      this.showAnalysisResult = false
      // 初始化参数值
      if (this.selectedToolData.params) {
        this.selectedToolData.params.forEach(param => {
          this.$set(this.paramValues, param.name, param.default || 0)
        })
      }
    },
    uploadImage() {
      // 实际项目中实现图片上传逻辑
      alert('图片上传功能将在实际项目中实现')
    },
    runAnalysis() {
      // 模拟分析过程
      this.showAnalysisResult = true
    }
  }
}
</script>

<style scoped>
.analysis-view {
  padding: 20px;
}

.analysis-tools {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.tool-card {
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.tool-card:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  transform: translateY(-5px);
}

.tool-card.active {
  border-color: #1a5fb4;
  background-color: #f5f9ff;
}

.tool-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.analysis-area {
  margin-top: 30px;
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.tool-content {
  display: flex;
  gap: 20px;
  margin-top: 15px;
}

.image-container {
  flex: 1;
  position: relative;
}

.image-container img {
  width: 100%;
  height: auto;
  border-radius: 4px;
}

.analysis-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(26, 95, 180, 0.2);
  pointer-events: none;
}

.tool-controls {
  flex: 0 0 250px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.upload-btn, .analyze-btn {
  padding: 10px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.upload-btn {
  background-color: #f0f0f0;
}

.upload-btn:hover {
  background-color: #e0e0e0;
}

.analyze-btn {
  background-color: #1a5fb4;
  color: white;
}

.analyze-btn:hover {
  background-color: #0d4b9e;
}

.analyze-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.tool-params {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.param-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.param-item input {
  width: 80px;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style>