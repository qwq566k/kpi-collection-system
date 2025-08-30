<template>
  <AdminLayout>
    <div class="audit-page">
      <h2 class="page-title">KPI成果审核</h2>
      
      <!-- 查询条件 -->
      <div class="query-section">
        <el-form :model="queryForm" inline>
          <el-form-item label="考核领域">
            <el-select 
              v-model="queryForm.fieldId" 
              placeholder="选择考核领域"
              clearable
              style="width: 200px"
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
              placeholder="输入负责人姓名"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="状态">
            <el-select 
              v-model="queryForm.status" 
              placeholder="选择状态"
              clearable
              style="width: 200px"
            >
              <el-option label="草稿" :value="0" />
              <el-option label="审核中" :value="1" />
              <el-option label="已通过" :value="2" />
              <el-option label="已退回" :value="3" />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 成果列表 -->
      <div class="table-section">
        <el-table :data="records" style="width: 100%">
          <el-table-column prop="year" label="年度" width="80" />
          <el-table-column prop="department" label="部门" width="120" />
          <el-table-column prop="submitterName" label="负责人" width="100" />
          <el-table-column prop="achievementName" label="成果名称" min-width="80" align="center"/>
          <el-table-column prop="fieldName" label="考核领域" width="120" />
          <el-table-column prop="indicatorName" label="关键指标" width="120" />
          <el-table-column label="评价标准" min-width="200">
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
          <el-table-column label="提交日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.submitDate || row.obtainDate) }}
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
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button 
                :type="row.status === 3 ? 'danger' : row.status === 2 ? 'success' : 'primary'"
                size="small"
                @click="handleView(row)"
              >
                查看
              </el-button>
              <el-button 
                :disabled="row.status !== 1"
                type="success" 
                size="small"
                @click="handleApprove(row)"
              >
                通过
              </el-button>
              <el-button 
                :disabled="row.status !== 1"
                type="danger" 
                size="small"
                @click="handleReject(row)"
              >
                退回
              </el-button>
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

    <!-- 详情对话框 -->
    <AchievementDetail ref="detailRef" />

    <!-- 退回原因对话框 -->
    <el-dialog v-model="showRejectDialog" title="退回原因" width="500px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef">
        <el-form-item label="退回原因" prop="rejectReason">
          <el-input 
            v-model="rejectForm.rejectReason" 
            type="textarea" 
            :rows="4"
            placeholder="请输入退回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminLayout from '../../components/AdminLayout.vue'
import { queryRecords, approveRecord, rejectRecord } from '../../api/admin'
import { getAllField } from '../../api/common'
import { getRecordDetail } from '../../api/user'
import AchievementDetail from '../../components/AchievementDetail.vue'

// 查询条件
const queryForm = reactive({
  fieldId: '',
  submitterName: '',
  status: ''
})

// 考核领域选项
const fields = ref([])

// 记录列表
const records = ref([])
// 详情组件引用
const detailRef = ref()
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 退回对话框
const showRejectDialog = ref(false)
const rejectForm = reactive({
  id: null,
  rejectReason: ''
})
const rejectRules = {
  rejectReason: [
    { required: true, message: '请输入退回原因', trigger: 'blur' }
  ]
}

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
    const data = await queryRecords({
      ...queryForm,
      page: currentPage.value,
      size: pageSize.value
    })
    // 管理端不显示草稿(0)
    records.value = data.rows
      .filter(r => Number(r.status) !== 0)
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

// 查看记录
const handleView = async (row) => {
  try {
    const data = await getRecordDetail(row.id)
    detailRef.value?.open({ ...row, ...data })
  } catch (e) {
    detailRef.value?.open({ ...row })
  }
}

// 通过记录
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要通过这条记录吗？通过后将无法再修改审核结果。', '确认通过', {
      confirmButtonText: '确定通过',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await approveRecord(row.id)
    ElMessage.success('审核通过成功')
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
    }
  }
}

// 退回记录
const handleReject = (row) => {
  rejectForm.id = row.id
  rejectForm.rejectReason = ''
  showRejectDialog.value = true
}

// 确认退回
const confirmReject = async () => {
  try {
    // 二次确认
    await ElMessageBox.confirm('确定要退回这条记录吗？退回后用户需要重新修改并提交。', '确认退回', {
      confirmButtonText: '确定退回',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await rejectRecord(rejectForm)
    ElMessage.success('退回成功')
    showRejectDialog.value = false
    loadRecords()
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消操作，不做任何处理
      return
    }
    console.error('退回失败:', error)
  }
}

// 切换评价标准显示
const toggleStandard = (row) => {
  row.showFull = !row.showFull
}

// 获取状态类型
const getStatusType = (status) => {
  const s = Number(status)
  const statusMap = {
    0: 'info',    // 草稿
    1: 'warning', // 审核中
    2: 'success', // 已通过
    3: 'danger'   // 已退回
  }
  return statusMap[s] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const s = Number(status)
  const statusMap = {
    0: '草稿',
    1: '审核中',
    2: '已通过',
    3: '已退回'
  }
  return statusMap[s] || '未知'
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
.audit-page {
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

.table-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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


