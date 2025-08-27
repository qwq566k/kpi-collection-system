<template>
  <AdminLayout>
    <div class="stats-page">
      <h2 class="page-title">成果统计</h2>
      
      <!-- 提交量概览 -->
      <div class="overview-section">
        <h3 class="section-title">提交量概览</h3>
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-number">{{ submissionStats.total || 0 }}</div>
            <div class="stat-label">总提交量</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ submissionStats.approved || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ submissionStats.pending || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ submissionStats.rejected || 0 }}</div>
            <div class="stat-label">已退回</div>
          </div>
          <div class="stat-card total-score">
            <div class="stat-number">{{ submissionStats.totalScore || 0 }}</div>
            <div class="stat-label">总分统计</div>
          </div>
        </div>
      </div>
      
      <!-- 按考核领域统计 -->
      <div class="area-stats-section">
        <h3 class="section-title">按考核领域统计 (分数)</h3>
        <div class="stats-cards">
          <div 
            v-for="area in areaStats" 
            :key="area.fieldName"
            class="stat-card"
          >
            <div class="stat-number">{{ area.score || 0 }}</div>
            <div class="stat-label">{{ area.fieldName }}</div>
          </div>
        </div>
      </div>
      
      <!-- 按负责人统计 -->
      <div class="submitter-stats-section">
        <h3 class="section-title">按负责人统计 (分数)</h3>
        <div class="stats-cards">
          <div 
            v-for="submitter in submitterStats" 
            :key="submitter.submitterName"
            class="stat-card"
          >
            <div class="stat-number">{{ submitter.score || 0 }}</div>
            <div class="stat-label">{{ submitter.submitterName }}</div>
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
  getSubmitterStats 
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

// 页面加载时获取数据
onMounted(() => {
  loadSubmissionStats()
  loadAreaStats()
  loadSubmitterStats()
})
</script>

<style scoped>
.stats-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.overview-section,
.area-stats-section,
.submitter-stats-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
}

.stat-card {
  background: white;
  color: #333;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
  transition: all 0.2s ease;
  border: 1px solid #000000;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.stat-card.total-score {
  background: white;
  border: 1px solid #000000;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 11px;
  opacity: 0.9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 16px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-number {
    font-size: 28px;
  }
}
</style>
