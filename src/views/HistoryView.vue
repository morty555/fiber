
<template>
  <div class="history-view">
    <h2>分析历史记录</h2>
    
    <!-- 搜索和分页控制 -->
    <div class="controls">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索分析记录..."
          @input="handleSearch"
        >
        <button class="refresh-btn" @click="fetchRecords">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
          </svg>
        </button>
      </div>
      
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
    
      <p>暂无历史记录</p>
      <button @click="fetchRecords">刷新</button>
    </div>

    <!-- 表格 -->
    <div v-else class="history-table">
      <table>
        <thead>
          <tr>
            <th @click="sortBy('id')">ID 
              <span v-if="sortField === 'id'">
                {{ sortOrder === 'asc' ? '↑' : '↓' }}
              </span>
            </th>
            <th>样本图片</th>
            <th @click="sortBy('imageDetail')">分析结果
              <span v-if="sortField === 'imageDetail'">
                {{ sortOrder === 'asc' ? '↑' : '↓' }}
              </span>
            </th>
            <th @click="sortBy('create_time')">创建时间
              <span v-if="sortField === 'create_time'">
                {{ sortOrder === 'asc' ? '↑' : '↓' }}
              </span>
            </th>
            
            <th>操作</th>
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
            <td>{{ formatDateTime(record.createTime) }}</td>
          
            <td>
              <button class="action-btn view-btn" @click="showParams(record)">
                详情
              </button>
              <button 
                class="action-btn delete-btn" 
                @click="confirmDelete(record.id)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 图片预览-->
    <div class="modal" v-if="showModal" @click.self="closeModal">
      <div class="modal-content">
        <span class="close-btn" @click="closeModal">&times;</span>
        <img :src="previewImage" alt="预览图片">
      </div>
    </div>

    <!-- 详情 -->
    <div class="modal" v-show="showParamsModal" @click.self="closeParamsModal">
      <div class="modal-content params-modal">
        <span class="close-btn" @click="closeParamsModal">&times;</span>
        <h3>分析详情</h3>
        <ul>
          <li v-for="(value,key) in currentParams" :key="key">
           <strong>{{ key }}:</strong>
           <span v-if="key === 'image'">
          <img :src="value" alt="样本图片" class="params-image">
        </span>
        <span v-else>
          {{ value }}
        </span>
          </li>
        </ul>
       
      </div>
    </div>

    <!-- 删除-->
    <div class="confirm-dialog" v-show="showDeleteDialog">
      <div class="dialog-content">
        <p>确定要删除这条记录吗？</p>
        <div class="dialog-actions">
          <button @click="showDeleteDialog = false">取消</button>
          <button @click="executeDelete" class="confirm-btn">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch,onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { API_BASE_URL } from '@/config'

export default {
  setup() {
    const router = useRouter()
    
    const API_URL = API_BASE_URL
    const authToken = localStorage.getItem('jwtToken')

    const records = ref([])
    const loading = ref(false)
    const error = ref(null)
    const searchQuery = ref('')
    const currentPage = ref(1)
    const pageSize = ref(5)
    const sortField = ref('create_time')
    const sortOrder = ref('desc')
    const showModal = ref(false)
    const previewImage = ref('')
    const showParamsModal=ref(false)
    const currentParams = ref({})
    const showDeleteDialog = ref(false)
    const recordToDelete = ref(null)
    const filteredRecords = computed(() => {
  const raw = records.value?.records || []
  const query = searchQuery.value.toLowerCase()

  if (!query) return raw

  return raw.filter(record => 
    record.id.toString().includes(query) ||
    (record.imageDetail && record.imageDetail.toLowerCase().includes(query)) ||
    formatDateTime(record.createTime).toLowerCase().includes(query)
  )
})

const sortedRecords = computed(() => {
  return [...filteredRecords.value].sort((a, b) => {
    let valA = a[sortField.value]
    let valB = b[sortField.value]

    if (sortField.value === 'create_time') {
      valA = new Date(valA).getTime()
      valB = new Date(valB).getTime()
    } else if (sortField.value === 'imageDetail') {
      valA = a.imageDetail || ''
      valB = b.imageDetail || ''
    }

    return sortOrder.value === 'asc' 
      ? valA > valB ? 1 : -1 
      : valA < valB ? 1 : -1
  })
})

const totalRecords = computed(() => filteredRecords.value.length)

    
    const fetchRecords = async () => {
      loading.value = true
      error.value = null

      try {
       const response = await axios.post(
  `${API_URL}/function/history`,
  {
    page: currentPage.value,
    pageSize: pageSize.value,
    sortField: sortField.value,
    sortOrder: sortOrder.value,
    search: searchQuery.value
  },
  {
    headers: {
      'Authorization': `Bearer ${authToken}`
    }
  }
)

        records.value = response.data.data
       // totalRecords.value = response.data.total
            //console.log(records.value)
         //console.log(authToken)
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

    const deleteRecord = async (id) => {
      try {
        //console.log("do")
        await axios.delete(`${API_URL}/function/history/${id}`, {
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        })
        
        alert('删除成功')
        fetchRecords() 
        
        if (records.value.length === 1 && currentPage.value > 1) {
          currentPage.value--
        }
      } catch (err) {
        handleApiError(err)
      }
    }

    // 分页
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

    // 搜索
    const handleSearch = () => {
      currentPage.value = 1
      fetchRecords()
    }

    // 排序
    const sortBy = (field) => {
      if (sortField.value === field) {
        sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortField.value = field
        sortOrder.value = 'asc'
      }
      fetchRecords()  //触发重新请求
    }

    // 图片处理
    const getImageUrl = (path) => {
      //console.log(path)
      return path || 'https://via.placeholder.com/150?text=No+Image'
    }

    // 分析类型
    const getAnalysisDetail = (imageDetail) => {
      return  imageDetail
    }

    // 日期格式化
    const formatDateTime = (dateString) => {
      if (!dateString) return ''
      const options = { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit',
        hour: '2-digit', 
        minute: '2-digit',
        second: '2-digit'
      }
      return new Date(dateString).toLocaleString('zh-CN', options)
    }
    const showParams = (record) => {
      currentParams.value = record || {}
      showParamsModal.value = true
    }

    // 图片预览
    const showImagePreview = (imageUrl) => {
      previewImage.value = imageUrl
      showModal.value = true
    }

    // 确认删除
    const confirmDelete = (id) => {
     
      recordToDelete.value = id
      console.log(recordToDelete.value )
      showDeleteDialog.value = true
    }

    // 执行
    const executeDelete = () => {
     console.log(recordToDelete.value )
      if (recordToDelete.value) {
        deleteRecord(recordToDelete.value)
      }
      showDeleteDialog.value = false
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

    const totalPages = computed(() => {
      return Math.ceil(totalRecords.value / pageSize.value)
    })

    const paginatedRecords = computed(() => {
      //console.log(records.value)
      const start = (currentPage.value - 1) * pageSize.value
      return sortedRecords.value.slice(start, start + pageSize.value)
     // return records.value
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
        if (to.name === 'history') {
          fetchRecords()
        }
      }
    )

    return {
      // 状态
      loading,
      error,
      searchQuery,
      currentPage,
      pageSize,
      totalRecords,
      sortField,
      sortOrder,
      showModal,
      previewImage,
      showParamsModal,
      currentParams,
      showDeleteDialog,
      filteredRecords,
      sortedRecords,
      totalPages,
      paginatedRecords,
      
      // 方法
      fetchRecords,
      handleSearch,
      prevPage,
      nextPage,
      handlePageSizeChange,
      sortBy,
      getImageUrl,
      getAnalysisDetail,
      formatDateTime,
      showParams,
      showImagePreview,
  
      confirmDelete,
      executeDelete,
      closeModal,
      closeParamsModal,
      
    }
  }
}
</script>

<style scoped>
.history-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  flex-wrap: wrap;
  gap: 15px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-box input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 250px;
}

.refresh-btn {
  padding: 8px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.refresh-btn:hover {
  background: #eaeaea;
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

.view-btn {
  background-color: #1a5fb4;
  color: white;
}

.view-btn:hover {
  background-color: #0d4b9e;
}

.delete-btn {
  background-color: #e74c3c;
  color: white;
}

.delete-btn:hover {
  background-color: #c0392b;
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

.confirm-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1001;
}

.dialog-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  min-width: 300px;
  text-align: center;
}

.dialog-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.dialog-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.confirm-btn {
  background-color: #e74c3c;
  color: white;
}

.confirm-btn:hover {
  background-color: #c0392b;
}

@media (max-width: 768px) {
  .controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    width: 100%;
  }
  
  .pagination-controls {
    justify-content: center;
  }
  
  th, td {
    padding: 8px;
    font-size: 14px;
  }
}

.params-modal {
  max-width: 600px;
  max-height: 80vh;
  overflow: auto;
}

.params-modal pre {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
}
.params-image {
  max-width: 100%;
  height: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-top: 5px;
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

.empty-state img {
  width: 120px;
  opacity: 0.6;
  margin-bottom: 15px;
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