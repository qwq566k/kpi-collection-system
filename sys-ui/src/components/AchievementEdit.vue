<template>
  <el-dialog 
    v-model="visible" 
    :title="`编辑成果 - ${achievement.achievementName || '未命名'}`" 
    width="900px"
    class="achievement-edit-dialog"
  >
    <div class="achievement-edit">
      <el-form 
        ref="formRef"
        :model="achievement" 
        :rules="formRules"
        label-width="120px" 
        class="edit-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="填报人">
              <el-input v-model="achievement.submitterName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果名称" prop="achievementName">
              <el-input v-model="achievement.achievementName" placeholder="请输入成果名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="获得日期" prop="obtainDate">
              <el-date-picker
                v-model="achievement.obtainDate"
                type="date"
                placeholder="选择日期"
                format="YYYY/MM/DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队成员">
              <el-input v-model="achievement.teamMembers" placeholder="如有团队成员请填写" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考核领域" prop="fieldId">
              <el-select 
                v-model="achievement.fieldId" 
                placeholder="请选择领域"
                style="width: 100%"
                @change="handleFieldChange"
              >
                <el-option
                  v-for="field in fields"
                  :key="field.areaId"
                  :label="field.areaName"
                  :value="field.areaId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关键指标" prop="indicatorId">
              <el-select 
                v-model="achievement.indicatorId" 
                placeholder="请先选择领域"
                style="width: 100%"
                :disabled="!achievement.fieldId"
                @change="handleIndicatorChange"
              >
                <el-option
                  v-for="indicator in indicators"
                  :key="indicator.indicatorId"
                  :label="indicator.indicatorName"
                  :value="indicator.indicatorId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="评价标准" prop="standardId">
              <el-select 
                v-model="achievement.standardId" 
                placeholder="请先选择指标"
                style="width: 100%"
                :disabled="!achievement.indicatorId"
                @change="handleStandardChange"
              >
                <el-option
                  v-for="standard in standards"
                  :key="standard.standardId"
                  :label="standard.standardName"
                  :value="standard.standardId"
                />
              </el-select>
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
            <el-form-item label="佐证材料" prop="evidenceFile">
              <div class="upload-container">
                <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :on-change="handleFileChange"
                  :on-remove="handleFileRemove"
                  :file-list="fileList"
                  :limit="1"
                  class="inline-upload"
                >
                  <el-button type="primary">选择附件</el-button>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="achievement.status === 3 && achievement.rejectReason" :gutter="20">
          <el-col :span="24">
            <el-form-item label="退回原因">
              <el-input v-model="achievement.rejectReason" type="textarea" :rows="3" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { updateRecord } from '../api/user'
import { getAllField, getKeyIndicators, getStandards, uploadFile } from '../api/common'

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

const visible = ref(false)
const loading = ref(false)
const formRef = ref()
const uploadRef = ref()
const achievement = reactive({})
const fileList = ref([])

// 下拉选项数据
const fields = ref([])
const indicators = ref([])
const standards = ref([])

// 表单验证规则
const formRules = {
  achievementName: [
    { required: true, message: '请输入成果名称', trigger: 'blur' }
  ],
  obtainDate: [
    { required: true, message: '请选择获得日期', trigger: 'change' }
  ],
  fieldId: [
    { required: true, message: '请选择考核领域', trigger: 'change' }
  ],
  indicatorId: [
    { required: true, message: '请选择关键指标', trigger: 'change' }
  ],
  standardId: [
    { required: true, message: '请选择评价标准', trigger: 'change' }
  ],
  evidenceFile: [
    { 
      required: true, 
      message: '请上传佐证材料', 
      trigger: ['blur', 'change'],
      validator: (rule, value, callback) => {
        if (!value || (typeof value === 'string' && value.trim() === '')) {
          callback(new Error('请上传佐证材料'))
        } else {
          callback()
        }
      }
    }
  ]
}

// 获取文件名
const getFileName = (file) => {
  if (!file) return ''
  if (file instanceof File) {
    return file.name
  }
  if (typeof file === 'string') {
    let last = file
    if (file.includes('/')) {
      last = file.split('/').pop()
    }
    const m = last.match(/^[0-9a-fA-F]{32}_(.+)$/)
    if (m) {
      try {
        return decodeURIComponent(m[1])
      } catch (e) {
        return m[1]
      }
    }
    return last
  }
  return ''
}

// 解析文件URL
const resolveFileUrl = (file) => {
  if (!file) return ''
  if (/^https?:\/\//i.test(file)) return file
  if (file.startsWith('/')) return file
  return `/api/files/${encodeURIComponent(file)}`
}

// 禁用今天之后的日期
const disabledDate = (time) => {
  return time.getTime() > Date.now()
}

// 上传文件到后端
const uploadFileToServer = async (file) => {
  if (!file) return ''
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await uploadFile(fd)
    return res?.fileUrl || ''
  } catch (e) {
    ElMessage.error('上传附件失败，请重试')
    throw e
  }
}

// 文件选择
const handleFileChange = (file) => {
  if (!file || !file.raw) return
  achievement.evidenceFile = file.raw
  achievement.evidenceFileName = file.name
  fileList.value = [{ ...file, name: file.name }]
  formRef.value?.validateField('evidenceFile')
}

// 文件删除
const handleFileRemove = () => {
  achievement.evidenceFile = ''
  achievement.evidenceFileName = ''
  fileList.value = []
  formRef.value?.validateField('evidenceFile')
}

// 考核领域变化
const handleFieldChange = async (fieldId) => {
  achievement.indicatorId = ''
  achievement.standardId = ''
  indicators.value = []
  standards.value = []
  
  if (fieldId) {
    try {
      const data = await getKeyIndicators(fieldId)
      indicators.value = data
      const field = fields.value.find(f => f.areaId === fieldId)
      if (field) {
        achievement.fieldName = field.areaName
      }
    } catch (error) {
      console.error('获取关键指标失败:', error)
    }
  }
}

// 关键指标变化
const handleIndicatorChange = async (indicatorId) => {
  achievement.standardId = ''
  achievement.standardName = ''
  standards.value = []
  
  if (indicatorId) {
    try {
      const data = await getStandards(indicatorId)
      standards.value = data
      const indicator = indicators.value.find(i => i.indicatorId === indicatorId)
      if (indicator) {
        achievement.indicatorName = indicator.indicatorName
      }
    } catch (error) {
      console.error('获取评价标准失败:', error)
    }
  }
}

// 评价标准变化
const handleStandardChange = (standardId) => {
  if (standardId) {
    const standard = standards.value.find(s => s.standardId === standardId)
    if (standard) {
      achievement.standardName = standard.standardName
    }
  } else {
    achievement.standardName = ''
  }
}

// 保存
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 如果有新文件，先上传文件
    let fileUrl = ''
    if (achievement.evidenceFile && achievement.evidenceFile instanceof File) {
      fileUrl = await uploadFileToServer(achievement.evidenceFile)
    } else if (achievement.evidenceFile && typeof achievement.evidenceFile === 'string') {
      // 如果已经是文件路径，直接使用
      fileUrl = achievement.evidenceFile
    }
    
    // 准备提交数据
    const payload = { ...achievement }
    if (fileUrl) {
      payload.evidenceFile = fileUrl
    }
    delete payload.evidenceFileName
    
    await updateRecord(payload)
    ElMessage.success('保存成功')
    visible.value = false
    formRef.value?.clearValidate()
    
    // 触发保存成功事件，让父组件刷新列表
    emit('saved')
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      console.error('保存失败:', error)
      ElMessage.error('保存失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 打开对话框
const open = async (data) => {
  // 重置表单
  Object.assign(achievement, {
    id: '',
    submitterId: '',
    submitterName: '',
    year: new Date().getFullYear(),
    achievementName: '',
    obtainDate: '',
    teamMembers: '',
    fieldId: '',
    fieldName: '',
    indicatorId: '',
    indicatorName: '',
    standardId: '',
    standardName: '',
    score: '',
    assessmentOrg: '',
    scoringInstruction: '',
    evidenceFile: '',
    evidenceFileName: '',
    status: 0,
    rejectReason: '',
    updatedAt: '',
    submitDate: ''
  })
  
  // 加载考核领域
  try {
    const fieldData = await getAllField()
    fields.value = fieldData
  } catch (error) {
    console.error('获取考核领域失败:', error)
  }
  
  // 填充数据
  Object.assign(achievement, data)
  
  // 回显已保存的附件
  if (data && data.evidenceFile) {
    fileList.value = [{
      name: getFileName(data.evidenceFile),
      url: resolveFileUrl(data.evidenceFile)
    }]
    // 保留原始文件路径
    achievement.evidenceFile = data.evidenceFile
  } else {
    fileList.value = []
  }
  
  // 联动加载
    if (data.fieldId) {
    try {
      // 1. 加载该领域下的指标
      const indicatorsData = await getKeyIndicators(data.fieldId)
      indicators.value = indicatorsData
      
      // 2. 找到对应的领域名称
      const field = fields.value.find(f => f.areaId === data.fieldId)
      if (field) {
        achievement.fieldName = field.areaName
      }
      
      // 3. 如果还有指标ID，加载该指标下的标准
      if (data.indicatorId) {
        const standardsData = await getStandards(data.indicatorId)
        standards.value = standardsData
        
        // 4. 找到对应的指标名称
        const indicator = indicatorsData.find(i => i.indicatorId === data.indicatorId)
        if (indicator) {
          achievement.indicatorName = indicator.indicatorName
        }
        
        // 5. 如果还有标准ID，找到对应的标准名称
        if (data.standardId) {
          const standard = standardsData.find(s => s.standardId === data.standardId)
          if (standard) {
            achievement.standardName = standard.standardName
          }
        }
      }
    } catch (error) {
      console.error('加载联动数据失败:', error)
    }
  }
  
  visible.value = true
}

const emit = defineEmits(['saved'])

// 暴露open方法
defineExpose({ open })
</script>

<style scoped>
.achievement-edit-dialog :deep(.el-dialog) {
  border: 2px solid #000000;
  border-radius: 8px;
}

.achievement-edit-dialog :deep(.el-dialog__body) {
  background-color: #ffffff;
  padding: 20px;
}

.achievement-edit {
  max-height: 60vh;
  overflow-y: auto;
}

.upload-container {
  width: 100%;
}

.inline-upload {
  display: inline-block;
}
</style>

