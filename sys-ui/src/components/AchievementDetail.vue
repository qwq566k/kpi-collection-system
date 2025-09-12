<template>
  <el-dialog 
    v-model="visible" 
    :title="`成果详情 - ${achievement.achievementName || '未命名'}`" 
    width="900px"
    class="achievement-detail-dialog"
  >
    <div class="achievement-detail">
      <el-form :model="achievement" label-width="120px" class="detail-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="填报人">
              <el-input v-model="achievement.submitterName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果名称">
              <el-input v-model="achievement.achievementName" disabled placeholder="请输入成果名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="获得日期">
              <el-input :model-value="formatDate(achievement.obtainDate)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队成员">
              <el-input v-model="achievement.teamMembers" disabled placeholder="如有团队成员请填写" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考核领域">
              <el-input v-model="achievement.fieldName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关键指标">
              <el-input v-model="achievement.indicatorName" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="评价标准">
              <el-input v-model="achievement.standardName" type="textarea" :rows="3" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分值">
              <el-input v-model="achievement.score" disabled placeholder="无数据" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考核组织">
              <el-input v-model="achievement.assessmentOrg" disabled placeholder="无数据" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="赋分说明">
              <el-input v-model="achievement.scoringInstruction" type="textarea" :rows="3" disabled placeholder="无数据" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="提交日期">
              <el-input :model-value="formatDateTime(achievement.updatedAt || achievement.submitDate)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-tag :type="getStatusType(achievement.status)">
                {{ getStatusText(achievement.status) }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="佐证材料">
              <el-link 
                v-if="achievement.evidenceFile"
                type="primary" 
                @click="downloadFile(achievement.evidenceFile)"
              >
                {{ getFileName(achievement.evidenceFile) }}
              </el-link>
              <span v-else>-</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="achievement.status === 3 && achievement.rejectReason" :gutter="20">
          <el-col :span="24">
            <el-form-item label="退回原因">
              <div class="reject-reason">{{ achievement.rejectReason }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button 
        v-if="achievement.evidenceFile"
        type="primary" 
        @click="downloadFile(achievement.evidenceFile)"
      >
        下载附件
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadByPath } from '../api/common'

const visible = ref(false)
const achievement = reactive({})

// 打开对话框
const open = (data) => {
  Object.assign(achievement, data)
  visible.value = true
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

// 下载文件
const resolveFileUrl = (file) => {
  if (!file) return ''
  if (/^https?:\/\//i.test(file)) return file
  if (file.startsWith('/')) return file
  return `/api/files/${encodeURIComponent(file)}`
}

const downloadFile = async (file) => {
  const path = typeof file === 'string' ? file : ''
  if (!path) {
    ElMessage.warning('没有可下载的附件')
    return
  }
  try {
    const blob = await downloadByPath(path)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = getFileName(path)
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载失败，请稍后重试')
  }
}

// 获取文件名
const getFileName = (fileUrl) => {
  return fileUrl ? fileUrl.split('/').pop() : ''
}

// 日期格式化
const formatDate = (value) => {
  if (!value) return ''
  const d = typeof value === 'string' ? new Date(value) : value
  if (Number.isNaN(d.getTime())) return value
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

// 日期时间格式化 YYYY-MM-DD HH:mm:ss
const formatDateTime = (value) => {
  if (!value) return ''
  const d = typeof value === 'string' ? new Date(value) : value
  if (Number.isNaN(d.getTime())) return value
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
}

// 暴露open方法
defineExpose({ open })
</script>

<style scoped>
.achievement-detail-dialog :deep(.el-dialog) {
  border: 2px solid #000000;
  border-radius: 8px;
}

.achievement-detail-dialog :deep(.el-dialog__body) {
  background-color: #ffffff;
  padding: 20px;
}

.achievement-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 8px;
}

.detail-item {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
}

.detail-item label {
  font-weight: 600;
  color: #666;
  min-width: 80px;
  margin-right: 8px;
}

.detail-item span {
  color: #333;
  flex: 1;
}

.standard-content {
  background-color: #fff;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  margin-top: 8px;
  line-height: 1.6;
}

.reject-reason {
  background-color: #fff5f5;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #fed7d7;
  color: #e53e3e;
  line-height: 1.6;
}

.file-link {
  margin-left: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-item label {
    margin-bottom: 4px;
  }
}
</style>
