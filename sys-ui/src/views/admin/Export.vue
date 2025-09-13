<template>
  <AdminLayout>
    <div class="export-page">
      <h2 class="page-title">成果导出</h2>
      
      <!-- 查询条件 -->
      <div class="query-section">
        <el-form :model="queryForm" inline class="query-form">
          <el-form-item label="考核领域">
            <el-select 
              v-model="queryForm.fieldId" 
              placeholder="选择考核领域"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="field in fields"
                :key="field.areaId"
                :label="field.areaName"
                :value="field.areaId"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="负责人">
            <el-input 
              v-model="queryForm.submitterName" 
              placeholder="输入姓名"
              style="width: 120px"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="queryForm.startDate"
              type="date"
              placeholder="年/月/日"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              style="width: 150px"
            />
          </el-form-item>
          
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="queryForm.endDate"
              type="date"
              placeholder="年/月/日"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              style="width: 150px"
            />
          </el-form-item>
          
          <el-form-item label="导出类型">
            <el-select 
              v-model="queryForm.exportType" 
              placeholder="选择导出类型"
              style="width: 100px"
            >
              <el-option label="全部数据" :value="1" />
              <el-option label="查询条件" :value="2" />
            </el-select>
          </el-form-item>
          
          <el-form-item class="form-actions">
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 数据预览 -->
      <div class="preview-section">
        <h3 class="section-title">数据预览</h3>
        <el-table :data="records" style="width: 100%">
          <el-table-column prop="year" label="年度" width="80" />
          <el-table-column prop="department" label="部门" width="100" />
          <el-table-column prop="submitterName" label="负责人" width="80" />
          <el-table-column prop="achievementName" label="成果名称" min-width="80" align="center"/>
          <el-table-column prop="fieldName" label="考核领域" width="120" />
          <el-table-column prop="indicatorName" label="关键指标" width="120" />
          <el-table-column label="评价标准" min-width="180">
            <template #default="{ row }">
              <div class="standard-text">
                <span 
                  v-if="!row.showFull"
                  class="ellipsis-line"
                  @click="toggleStandard(row)"
                  title="点击展开"
                >
                  {{ row.standardName }}
                </span>
                <div 
                  v-else
                  class="full-standard"
                  @click="toggleStandard(row)"
                  title="点击收起"
                >
                  {{ row.standardName }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="80" />
          <el-table-column prop="scoringInstruction" label="赋分说明" width="120" />
          <el-table-column prop="obtainDate" label="获得日期" width="120" />
          <el-table-column label="提交日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusType(row.status)"
                size="small"
              >
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            :pager-count="5"
            prev-text="上一页"
            next-text="下一页"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AdminLayout from '../../components/AdminLayout.vue'
import { exportQuery, exportExcel } from '../../api/admin'
import { getAllField } from '../../api/common'

// 查询条件
const queryForm = reactive({
  fieldId: '',
  submitterName: '',
  startDate: '',
  endDate: '',
  exportType: 1
})

// 考核领域选项
const fields = ref([])

// 记录列表
const records = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取考核领域
const loadFields = async () => {
  try {
    const data = await getAllField()
    fields.value = data
  } catch (error) {
    console.error('获取考核领域失败:', error)
  }
}

// 查询记录
const loadRecords = async () => {
  try {
    const data = await exportQuery({
      fieldId: queryForm.fieldId,
      submitterName: queryForm.submitterName,
      startDate: queryForm.startDate,
      endDate: queryForm.endDate
          })
      // 只显示已通过的数据（status = 2）
      records.value = data.rows
        .filter(r => Number(r.status) === 2)
        .map(record => ({
          ...record,
          showFull: false
        }))
    total.value = data.total
  } catch (error) {
    console.error('查询记录失败:', error)
  }
}

// 执行查询
const handleQuery = () => {
  currentPage.value = 1
  loadRecords()
}

// 导出Excel
const handleExport = async () => {
  try {
    const exportData = {
      exportType: queryForm.exportType,
      fieldId: queryForm.fieldId,
      submitterName: queryForm.submitterName,
      startDate: queryForm.startDate,
      endDate: queryForm.endDate
    }
    
    // 调用导出接口获取文件流
    const response = await exportExcel(exportData)
    
    // 创建blob URL并触发下载
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `KPI成果导出_${new Date().toISOString().split('T')[0]}.xlsx`
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

// 切换评价标准显示
const toggleStandard = (row) => {
  row.showFull = !row.showFull
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    0: 'info',    // 草稿
    1: 'warning', // 审核中
    2: 'success', // 已通过
    3: 'danger'   // 已退回
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '草稿',
    1: '审核中',
    2: '已通过',
    3: '已退回'
  }
  return statusMap[status] || '未知'
}

// 日期格式化
const formatDate = (value) => {
  if (!value) return '-'
  const date = typeof value === 'string' ? new Date(value) : value
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadRecords()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadRecords()
}

// 页面加载时获取数据
onMounted(() => {
  loadFields()
  loadRecords()
})
</script>

<style scoped>
.export-page {
  max-width: 1600px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.query-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.standard-text {
  position: relative;
}

.full-standard {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.ellipsis-line {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.query-form {
  display: flex;
  flex-wrap: nowrap; /* 同一行显示 */
  align-items: center;
  column-gap: 5px;
}

.form-actions {
  margin-left: auto; /* 靠右 */
}

.query-form :deep(.el-form-item) {
  margin-bottom: 0;
  flex: 0 0 auto;
}

.query-form :deep(.el-input),
.query-form :deep(.el-select),
.query-form :deep(.el-date-editor) {
  width: 180px; /* 统一输入框宽度，可按需微调 */
}

.query-form :deep(.el-select .el-select__wrapper) {
  width: 100%;
}

.form-actions :deep(.el-form-item__content) {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination {
  margin-top: 24px;
  text-align: right;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}
</style>


