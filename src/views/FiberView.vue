<template>
  <!-- 模板部分保持不变 -->
  <div class="fiber-view">
    <h2>纤维识别</h2>
    <div class="upload-area">
      <div class="upload-box" @click="triggerUpload">
        <div v-if="!imagePreview">
          <span>+</span>
          <p>点击上传纤维图片</p>
        </div>
        <img v-else :src="imagePreview" alt="上传的纤维图片">
      </div>
      <input 
        type="file" 
        ref="fileInput" 
        @change="handleFileUpload" 
        accept="image/*"
        style="display: none;"
      >
    </div>
    
    <div class="result-area" v-if="analysisResult">
      <h3>分析结果</h3>
      <div class="result-content">
        <div class="result-item"> 
        <span class="result-label">返回值:</span>
        <span class="result-value">{{ analysisResult.code }}</span> 
      </div>
      <div class="result-item">
        <span class="result-label">错误信息:</span>
        <span class="result-value">{{ analysisResult.msg }}</span>
      </div>
      <div class="result-item">
       <span class="result-label">原始图片:</span>
        <span class="result-value">{{ analysisResult.data.originalImagePath}}</span>
      </div>
      <div class="result-item">
        <span class="result-label">分析后图片:</span>
        <span class="result-value">{{ analysisResult.data.analyzedImagePath }}</span>
      </div>
      <div class="result-item">
        <span class="result-label">分析结果:</span>
        <div v-html="formatImageDetail()" class="result-detail"></div>
       
      </div>  
      </div>
    </div>
    <button class="analyze-btn" @click="analyzeImage" :disabled="!imagePreview || isLoading">
      {{ isLoading ? '分析中...' : '开始分析' }}
    </button>
  </div>
</template>

<script>
import {API_BASE_URL} from '@/config'
import Tiff from 'tiff.js';

export default {
  data() {
    return {
      imagePreview: null,
      analysisResult: null,
      isLoading: false,
    }
  },
  methods: {
    triggerUpload() {
      this.$refs.fileInput.click()
    },
   handleFileUpload(event) {
  const file = event.target.files[0];
  if (file) {
    const fileType = file.type; // 检查文件类型
    if (fileType === 'image/tiff' || fileType === 'image/x-tiff') {
      this.convertTiffToPng(file);
    } else {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview = e.target.result;
        this.analysisResult = null;
      };
      reader.readAsDataURL(file);
    }
  }
},
       // 将 TIFF 图像转换为 PNG
    convertTiffToPng(file) {
  const reader = new FileReader();
  reader.onload = (e) => {
    const tiffData = e.target.result;
    try {
      // 使用 Tiff 库解析 TIFF 数据
      const tiff = new Tiff({ buffer: tiffData });
      const canvas = tiff.toCanvas();

      // 确保 Canvas 已正确生成并将其转为 PNG 格式
      const img = new Image();
      img.onload = () => {
        this.imagePreview = canvas.toDataURL('image/png'); // 将 PNG 图片预览
        this.analysisResult = null;
      };
      img.src = canvas.toDataURL('image/png'); // 使用 toDataURL 获取 PNG 格式

    } catch (error) {
      console.error('TIFF 转换失败:', error);
    }
  };
  reader.readAsArrayBuffer(file);
},
    async analyzeImage() {
      const token = localStorage.getItem('jwtToken');
  if (!this.$refs.fileInput.files[0]) return;

  this.isLoading = true;
  try {
    const formData = new FormData();
    formData.append('file', this.$refs.fileInput.files[0]); // 直接上传原始文件，不用 base64

    const response = await fetch(`${API_BASE_URL}/fiber/analyze`,
     {
      method: 'POST',
       headers: {
        'Authorization': `Bearer ${token}`
        // 注意：不要设置 Content-Type，浏览器会自动处理 multipart/form-data 的 boundary
      },
      body: formData,
      // 注意：不要手动设置 Content-Type，浏览器会自动设置带边界的 multipart/form-data
    });

    if (!response.ok) {
      throw new Error('分析请求失败');
    }

    const result = await response.json();
    this.analysisResult = result;
  } catch (error) {
    console.error('分析错误:', error);
    this.analysisResult = {
      '纤维类型': '请求失败',
      '错误': error.message
    };
  } finally {
    this.isLoading = false;
  }
    },
    formatImageDetail() {
      return this.analysisResult.data.imageDetail.replace(/,/g, '<br>');
    }
  }
}
</script>

<style scoped>
.fiber-view {
  padding: 20px;
}

.upload-area {
  margin: 20px 0;
  text-align: center;
}

.upload-box {
  width: 300px;
  height: 200px;
  border: 2px dashed #ccc;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-box:hover {
  border-color: #1a5fb4;
  background-color: #f5f9ff;
}

.upload-box img {
  max-width: 100%;
  max-height: 100%;
}

.result-area {
  margin-top: 30px;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.result-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.result-label {
  font-weight: bold;
  color: #1a5fb4;
}

.analyze-btn {
  display: block;
  margin: 20px auto;
  width: 80px;
  height: 40px;
  background-color: #1a5fb4;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.analyze-btn:hover {
  background-color: #0d4b9e;
}

.analyze-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>