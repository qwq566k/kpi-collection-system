<template>
  <AdminLayout>
    <div class="stats-page">
      <h2 class="page-title">成果统计</h2>
      
      <!-- 提交量概览 -->
      <div class="overview-section">
        <div class="card">
          <div class="card-title">提交量概览</div>
          <div class="stat-grid">
            <div class="stat-card">
              <div class="stat-title">总提交量</div>
              <div class="stat-value">{{ submissionStats.total || 0 }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-title">已通过</div>
              <div class="stat-value">{{ submissionStats.approved || 0 }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-title">待审核</div>
              <div class="stat-value">{{ submissionStats.pending || 0 }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-title">已退回</div>
              <div class="stat-value">{{ submissionStats.rejected || 0 }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-title">总分统计</div>
              <div class="stat-value">{{ submissionStats.totalScore || 0 }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 按考核领域统计 -->
      <div class="area-stats-section">
        <div class="card">
          <div class="card-title">按考核领域统计（分数）
            <el-button type="success" @click="handleExportField">导出Excel</el-button>
          </div>
          <div class="stat-grid">
            <div 
              v-for="area in areaStats" 
              :key="area.fieldName"
              class="stat-card"
            >
              <div class="stat-title">{{ area.fieldName }}</div>
              <div class="stat-value">{{ area.score || 0 }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 按负责人统计 -->
      <div class="submitter-stats-section">
        <div class="card">
          <div class="card-title">按负责人统计（分数）
            <el-button type="success" @click="handleExportSubmitter">导出Excel</el-button>
          </div>
          <div class="stat-grid">
            <div 
              v-for="submitter in submitterStats" 
              :key="submitter.submitterName"
              class="stat-card"
            >
              <div class="stat-title">{{ submitter.submitterName }}</div>
              <div class="stat-value">{{ submitter.score || 0 }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AdminLayout from '../../components/AdminLayout.vue'
import { 
  getSubmissionStats, 
  getAreaStats, 
  getSubmitterStats,
  exportFieldExcel,
  exportSubmitterExcel
} from '../../api/admin'

// 统计数据
const submissionStats = ref({})
const areaStats = ref([])
const submitterStats = ref([])

// 获取提交量统计
const loadSubmissionStats = async () => {
  try {
    const data = await getSubmissionStats()
    submissionStats.value = data
  } catch (error) {
    console.error('获取提交量统计失败:', error)
    ElMessage.error('获取提交量统计失败')
  }
}

// 获取考核领域统计
const loadAreaStats = async () => {
  try {
    const data = await getAreaStats()
    areaStats.value = data
  } catch (error) {
    console.error('获取考核领域统计失败:', error)
    ElMessage.error('获取考核领域统计失败')
  }
}

// 获取负责人统计
const loadSubmitterStats = async () => {
  try {
    const data = await getSubmitterStats()
    submitterStats.value = data
  } catch (error) {
    console.error('获取负责人统计失败:', error)
    ElMessage.error('获取负责人统计失败')
  }
}
// 导出考核领域数据
const handleExportField = async () => {
  try {
    // 调用导出接口获取文件流
    const response = await exportFieldExcel()
    
    // 创建blob URL并触发下载
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `考核领域数据导出_${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功，文件开始下载')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}
// 导出负责人数据
const handleExportSubmitter = async () => {
  try {
    // 调用导出接口获取文件流
    const response = await exportSubmitterExcel()
    
    // 创建blob URL并触发下载
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `负责人成果数据导出_${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功，文件开始下载')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}
// 页面加载时获取数据
onMounted(() => {
  loadSubmissionStats()
  loadAreaStats()
  loadSubmitterStats()
})
</script>

<style scoped>
.stats-page {
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-title {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  font-family: 'Microsoft YaHei', sans-serif;
}

.overview-section,
.area-stats-section,
.submitter-stats-section {
  margin-bottom: 20px;
}

/* 卡片样式 */
.card {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 统计网格样式 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

/* 统计卡片样式 */
.stat-card {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 15px;
  background-color: white;
  transition: box-shadow 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  font-family: 'Microsoft YaHei', sans-serif;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }
  
  .stat-card {
    padding: 12px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .stat-title {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .stat-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 10px;
  }
  
  .stat-card {
    padding: 10px;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .stat-title {
    font-size: 11px;
  }
}
</style>
