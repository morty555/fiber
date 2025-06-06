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
      <button @click="fetchRecords">重试</button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="loading-indicator">
      <div class="spinner"></div>
      加载中...
    </div>

    <!-- 没有记录 -->
    <div v-else-if="!loading && totalRecords === 0" class="empty-state">
      <p>暂无相似记录</p>
      <button @click="fetchRecords">刷新</button>
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
                :src="getImageUrl(record.originalImagePath)" 
                class="thumbnail" 
                @click="showImagePreview(getImageUrl(record.originalImagePath))"
              >
            </td>
            <td>{{ getAnalysisDetail(record.imageDetail) }}</td>
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
import { ref, computed, onMounted, watch,onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
export default {
  setup() {
    const router = useRouter()
    const API_BASE_URL = 'http://localhost:8000'
    const authToken = localStorage.getItem('authToken')
    const currentPage = ref(1)
    const pageSize = ref(5)
    const records = ref([])
    const loading = ref(false)
    const showModal = ref(false)
    const previewImage = ref('')
    const imagePreview =ref(null)
    const isLoading=ref(false)
    const fileInput = ref(null)
   
    const error = ref(null)
    const showTable = ref(false);

    const triggerUpload=()=> {
      fileInput.value.click();
    }
    const handleFileUpload=(event)=> {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          imagePreview.value = e.target.result;
          records.value=[]
        };
        reader.readAsDataURL(file);
      }
    }

  const  analyzeImage=async()=> {
      const token = localStorage.getItem('jwtToken');
    if (!fileInput.value || !fileInput.value.files || fileInput.value.files.length === 0)
    {
      error.value = '请先选择一个文件';
      return;
}
 isLoading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', fileInput.value.files[0]); // 直接上传原始文件，不用 base64
    const response = await axios.post(`${API_BASE_URL}/function/history`,
      {
       headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData,
    })

    if (!response.ok) {
      throw new Error('分析请求失败');
    }
    records.value = response.data.data
    
  } catch (error) {
    alert('未找到相似图片')
    error.value = error.message;
  } finally {
   isLoading.value = false;
  }
    }

    const fetchRecords = async () => {
      loading.value = true
      error.value = null
      try {
       const response = await axios.post(
  `${API_BASE_URL}/function/history`,
  {
    page: currentPage.value,
    pageSize: pageSize.value,
  },
  {
    headers: {
      'Authorization': `Bearer ${authToken}`
    }
  }
)
        records.value = response.data.data
        if(records.value.length===0)currentPage.value=0
      } catch (error) {
        handleApiError(error)
      } finally {
        loading.value = false
      }
    }
    const handleApiError = (err) => {
      const status = err.response?.status
      
      if (status === 401) {
        error.value = '登录已过期，请重新登录'
        router.push('/login')
      } else if (status === 403) {
        error.value = '没有权限访问此资源'
      } else if (status === 404) {
        error.value = '请求的资源不存在'
      } else {
        error.value =err.response?.data?.message || err.message || '网络请求失败'
      }
      
      alert(error.value)
    }

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--
        window.scrollTo({ top: 0, behavior: 'smooth' })
        fetchRecords()
      }
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++
        window.scrollTo({ top: 0, behavior: 'smooth' })
        fetchRecords()
      }
    }

    const handlePageSizeChange = () => {
      currentPage.value = 1
      fetchRecords()
    }
// 图片URL处理
    const getImageUrl = (path) => {
      return path ? `${API_BASE_URL}/uploads/${path}` : 'https://via.placeholder.com/150?text=No+Image'
    }

    const getAnalysisDetail = (imageDetail) => {
      return imageDetail || '无详细信息'
    }

    const showImagePreview = (imageUrl) => {
      previewImage.value = imageUrl
      showModal.value = true
    }
    const closeModal = () => {
      showModal.value = false
      previewImage.value = ''
    }
    const totalRecords = computed(() => records.value.length)

    const totalPages = computed(() => {
      return Math.ceil(totalRecords.value / pageSize.value)
    })
    const paginatedRecords = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      return records.value.slice(start, start + pageSize.value)
      //return records.value
    })
    onMounted(fetchRecords)
    onMounted(() => {
      window.addEventListener('refresh-history', fetchRecords)
    })
    
    onBeforeUnmount(() => {
      window.removeEventListener('refresh-history', fetchRecords)
    })
    watch(
      () => router.currentRoute.value,
      (to) => {
        if (to.name === 'image') {
          fetchRecords()
        }
      }
    )

    return {
      loading,
      error,
      previewImage,
     imagePreview,
      isLoading,
      getImageUrl,
      totalPages,
      paginatedRecords,
      fileInput,
      totalRecords,
      showTable,
      currentPage,
      pageSize,
      nextPage,
      prevPage,
      triggerUpload,
      handleFileUpload,
      getAnalysisDetail,
      showImagePreview,
      closeModal,
      handlePageSizeChange,
     analyzeImage,
      fetchRecords,
      handleApiError
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
