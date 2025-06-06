<template>
  <div class="image-view">
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
      <button class="analyze-btn" @click="analyzeImage" :disabled="!imagePreview || isLoading">
      {{ isLoading ? '识别中...' : '开始识别' }}
    </button>
      <div class="controls">
      <div class="pagination-controls">
        <span>共 {{ totalRecords }} 条记录</span>
        <select v-model="pageSize" @change="handlePageSizeChange">
          <option value="5">每页 5 条</option>
          <option value="10">每页 10 条</option>
          <option value="20">每页 20 条</option>
        </select>
        <button @click="prevPage" :disabled="currentPage === 1 || loading">上一页</button>
        <span class="page-indicator">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <button @click="nextPage" :disabled="currentPage === totalPages || loading">下一页</button>
      </div>
    </div>
    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <span>{{ error }}</span>
      <button @click="analyzeImage">重试</button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="loading-indicator">
      <div class="spinner"></div>
      加载中...
    </div>

    <!-- 没有记录 -->
    <div v-else-if="!loading && totalRecords === 0" class="empty-state">
      <p>暂无相似记录</p>
      <button @click="analyzeImage">刷新</button>
    </div>

    <div v-else class="history-table">
      <table>
        <thead>
          <tr>
            <th >ID</th>
            <th>样本图片</th>
            <th >分析类型</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in paginatedRecords" :key="record.id">
            <td>{{ record.id }}</td>
            <td>
              <img 
                :src="getImageUrl(record.originalImage)" 
                class="thumbnail" 
                @click="showImagePreview(getImageUrl(record.originalImage))"
              >
            </td>
            <td class="result-column">{{ getAnalysisDetail(record.detail) }}</td>
            <td>{{ getAnalysisDetail(record.type) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="modal" v-if="showModal" @click.self="closeModal">
      <div class="modal-content">
        <span class="close-btn" @click="closeModal">&times;</span>
        <img :src="previewImage" alt="预览图片">
      </div>
    </div>
    </div>
   
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import { API_BASE_URL } from '@/config'
import Tiff from 'tiff.js'

export default {
  setup() {
    const authToken = localStorage.getItem('jwtToken')
    const currentPage = ref(1)
    const pageSize = ref(5)
    const records = ref([])
    const totalRecords = ref(0)
    const loading = ref(false)
    const error = ref(null)
    const fileInput = ref(null)
    const imagePreview = ref(null)
    const isLoading = ref(false)

    const triggerUpload = () => {
      if (fileInput.value) {
        fileInput.value.click()
      }
    }

   const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();

    // 判断文件类型，是否为 TIFF
    if (file.type === 'image/tiff') {
      reader.onload = e => {
        const arrayBuffer = e.target.result;
        try {
          const tiff = new Tiff({buffer: arrayBuffer});
          const canvas = tiff.toCanvas();
          // 直接获取 PNG 数据
          imagePreview.value = canvas.toDataURL('image/png');
          records.value = [];
          totalRecords.value = 0;
        } catch (error) {
          console.error('TIFF 转换失败:', error);
          error.value = '转换 TIFF 图片失败';
        }
      };
      reader.readAsArrayBuffer(file); // 读取文件为 ArrayBuffer
    } else {
      reader.onload = e => {
        // 普通的图片处理
        imagePreview.value = e.target.result.replace(/^data:image\/\w+;base64,/, 'data:image/png;base64,');
        records.value = [];
        totalRecords.value = 0;
      };
      reader.readAsDataURL(file);
    }
  }
}


    const showImagePreview = (imageUrl) => {
      previewImage.value = imageUrl
      showModal.value = true
    }

    // 关闭
    const closeModal = () => {
      showModal.value = false
      previewImage.value = ''
    }

    const closeParamsModal = () => {
      showParamsModal.value = false
      currentParams.value = {}
    }

    // 图片处理
    const getImageUrl = (path) => {
      // 将图片路径更改为 PNG 格式
      return path ? `${path.split('?')[0]}.png` : 'https://via.placeholder.com/150?text=No+Image'
    }

    // 分析类型
    const getAnalysisDetail = (imageDetail) => {
      return imageDetail
    }

    // 上传图片 + 请求分页数据
    const analyzeImage = async () => {
      if (!fileInput.value.files.length) {
        error.value = '请先选择文件'
        return
      }
      isLoading.value = true
      error.value = null
      try {
        const formData = new FormData()
        formData.append('file', fileInput.value.files[0])
        formData.append('pageNum', currentPage.value)
        formData.append('pageSize', pageSize.value)

        const response = await axios.post(`${API_BASE_URL}/function/image`, formData, {
          headers: {
            'Authorization': `Bearer ${authToken}`,
          }
        })

        records.value = response.data.data  // 这是分页对象
        totalRecords.value = response.data.data.total || 0
      } catch (err) {
        error.value = err.message || '请求失败'
      } finally {
        isLoading.value = false
      }
    }

    const totalPages = computed(() => {
      return Math.ceil(totalRecords.value / pageSize.value) || 1
    })

    const paginatedRecords = computed(() => {
      return records.value?.records || []
    })

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--
        analyzeImage()
      }
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++
        analyzeImage()
      }
    }

    const handlePageSizeChange = () => {
      currentPage.value = 1
      analyzeImage()
    }

    watch(currentPage, () => {
      analyzeImage()
    })

    return {
      currentPage,
      pageSize,
      records,
      totalRecords,
      imagePreview,
      isLoading,
      error,
      fileInput,
      analyzeImage,
      handleFileUpload,
      prevPage,
      nextPage,
      handlePageSizeChange,
      triggerUpload,
      totalPages,
      paginatedRecords,
      getImageUrl,
      getAnalysisDetail,
      showImagePreview,
      closeModal,
      closeParamsModal,
    }
  }
}
</script>


<style scoped>

.image-view {
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
.controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  flex-wrap: wrap;
  gap: 15px;
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
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-controls button {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.pagination-controls button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-controls select {
  padding: 6px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.page-indicator {
  margin: 0 5px;
}

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1a5fb4;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.history-table {
  overflow-x: auto;
  margin-top: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background-color: #f9f9f9;
  font-weight: 600;
  cursor: pointer;
  user-select: none;
}

th:hover {
  background-color: #f0f0f0;
}

.thumbnail {
  width: 60px;
  height: 40px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s;
}

.result-column {
  max-width: 300px;    
  white-space: nowrap;  /* 不换行 */
  overflow: hidden;     /* 超出隐藏 */
  text-overflow: ellipsis; /* 超出显示省略号 */
}

.thumbnail:hover {
  transform: scale(1.1);
}
.action-btn {
  padding: 6px 12px;
  margin-right: 5px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  position: relative;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  max-width: 90%;
  max-height: 90%;
}
.modal-content img {
  max-width: 100%;
  max-height: 80vh;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 24px;
  cursor: pointer;
}
.pagination-controls {
    justify-content: center;
  }
  
  th, td {
    padding: 8px;
    font-size: 14px;
  }
  .error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 12px 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.error-message button {
  padding: 5px 10px;
  background: #c62828;
  color: white;
  border: none;
  border-radius: 3px;
  margin-left: 10px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-top: 20px;
}

.empty-state p {
  color: #666;
  margin-bottom: 15px;
}

.empty-state button {
  padding: 8px 16px;
  background: #1a5fb4;
  color: white;
  border: none;
  border-radius: 4px;
}
</style>
