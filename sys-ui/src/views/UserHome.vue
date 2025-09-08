<template>
  <div class="user-home">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-left">
        <h2>科研KPI成果收集系统</h2>
      </div>
      <div class="header-right">
        <el-button type="text" @click="showChangePassword = true">
          <el-icon><Lock /></el-icon>
          修改密码
        </el-button>
        <el-button type="text" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
        <div class="user-info">
          <el-avatar size="small" class="user-avatar">{{ userName?.[0] || '用' }}</el-avatar>
          <span class="user-name">{{ userName || '用户' }}</span>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 提交KPI成果表单 -->
      <div class="form-section">
        <h3 class="section-title">提交KPI成果</h3>
        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="formRules" 
          label-width="120px"
          class="achievement-form"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="填报人" prop="submitterName">
                <el-input v-model="form.submitterName" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="成果名称" prop="achievementName">
                <el-input 
                  v-model="form.achievementName" 
                  placeholder="请输入成果名称"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="获得日期" prop="obtainDate">
                <el-date-picker
                  v-model="form.obtainDate"
                  type="date"
                  placeholder="yyyy/mm/日"
                  format="YYYY/MM/DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="团队成员" prop="teamMembers">
                <el-input 
                  v-model="form.teamMembers" 
                  placeholder="如有团队成员请填写"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="考核领域" prop="fieldId">
                <el-select 
                  v-model="form.fieldId" 
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
                  v-model="form.indicatorId" 
                  placeholder="请先选择领域"
                  style="width: 100%"
                  :disabled="!form.fieldId"
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
            <el-col :span="12">
              <el-form-item label="评价标准" prop="standardId">
                <el-select 
                  v-model="form.standardId" 
                  placeholder="请先选择指标"
                  style="width: 100%"
                  :disabled="!form.indicatorId"
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
            <el-col :span="12">
              <el-form-item label="佐证材料" prop="evidenceFile">
                <div class="upload-container">
                  <el-upload
                    ref="uploadRef"
                    :auto-upload="false"
                    :on-change="handleFileChange"
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
          
          <el-form-item class="form-buttons">
            <el-button @click="handleSaveDraft">保存草稿</el-button>
            <el-button type="primary" @click="handleSubmit">提交审核</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 我的提交记录 -->
      <div class="records-section">
        <h3 class="section-title">我的提交记录</h3>
        <el-table :data="records" style="width: 100%">
          <el-table-column prop="achievementName" label="成果名称" />
          <el-table-column prop="fieldName" label="考核领域" />
          <el-table-column prop="indicatorName" label="关键指标" />
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
          <el-table-column label="佐证材料">
            <template #default="{ row }">
              <el-link 
                type="primary" 
                @click="downloadFile(row.evidenceFile)"
              >
                {{ getFileName(row.evidenceFile) }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="提交日期">
            <template #default="{ row }">
              {{ formatDate(row.submitDate || row.obtainDate) }}
            </template>
          </el-table-column>
          <el-table-column label="状态">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusType(row.status)"
                size="small"
              >
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button 
                v-if="Number(row.status) === 0 || Number(row.status) === 3"
                type="primary" 
                size="small"
                @click="handleEdit(row)"
              >
                修改
              </el-button>
              <el-button 
                v-else
                type="success" 
                size="small"
                @click="handleView(row)"
              >
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
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

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showChangePassword" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑KPI成果" width="800px">
      <el-form 
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="120px"
        class="achievement-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="填报人" prop="submitterName">
              <el-input v-model="editForm.submitterName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果名称" prop="achievementName">
              <el-input v-model="editForm.achievementName" placeholder="请输入成果名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="获得日期" prop="obtainDate">
              <el-date-picker
                v-model="editForm.obtainDate"
                type="date"
                placeholder="yyyy/mm/日"
                format="YYYY/MM/DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队成员" prop="teamMembers">
              <el-input v-model="editForm.teamMembers" placeholder="如有团队成员请填写" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考核领域" prop="fieldId">
              <el-select 
                v-model="editForm.fieldId" 
                placeholder="请选择领域"
                style="width: 100%"
                @change="handleEditFieldChange"
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
                v-model="editForm.indicatorId" 
                placeholder="请先选择领域"
                style="width: 100%"
                :disabled="!editForm.fieldId"
                @change="handleEditIndicatorChange"
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
                v-model="editForm.standardId" 
                placeholder="请先选择指标"
                style="width: 100%"
                :disabled="!editForm.indicatorId"
                @change="handleEditStandardChange"
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
          <el-col :span="24">
            <el-form-item label="佐证材料" prop="evidenceMaterial">
              <div class="upload-container">
                <el-upload
                  ref="editUploadRef"
                  :auto-upload="false"
                  :on-change="handleEditFileChange"
                  :file-list="editFileList"
                  :limit="1"
                  class="inline-upload"
                >
                  <el-button type="primary">选择附件</el-button>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="Number(editForm.status) === 3 && editForm.rejectReason" :gutter="20">
          <el-col :span="24">
            <el-form-item label="退回原因">
              <el-input v-model="editForm.rejectReason" type="textarea" :rows="3" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button @click="handleEditSaveDraft" :loading="editLoading">保存草稿</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, SwitchButton } from '@element-plus/icons-vue'
import { 
  submitRecord, 
  saveDraft, 
  updateRecord, 
  getAllRecords,
  getRecordDetail,
  updatePassword,
  logout
} from '../api/user'
import { getAllField, getKeyIndicators, getStandards, uploadFile } from '../api/common'
import { getUserInfo } from '../utils/auth'
import AchievementDetail from '../components/AchievementDetail.vue'

const router = useRouter()

// 表单数据
const form = reactive({
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
  evidenceFile: '',
  evidenceFileName: ''
})

// 表单验证规则
const formRules = {
  achievementName: [
    { required: true, message: '请输入成果名称', trigger: ['blur', 'change'] }
  ],
  obtainDate: [
    { required: true, message: '请选择获得日期', trigger: ['blur', 'change'] }
  ],
  fieldId: [
    { required: true, message: '请选择考核领域', trigger: ['blur', 'change'] }
  ],
  indicatorId: [
    { required: true, message: '请选择关键指标', trigger: ['blur', 'change'] }
  ],
  standardId: [
    { required: true, message: '请选择评价标准', trigger: ['blur', 'change'] }
  ],
  evidenceFile: [
    { 
      required: true, 
      message: '请上传佐证材料', 
      trigger: ['blur', 'change'],
      validator: (rule, value, callback) => {
        if (!value || value.trim() === '') {
          callback(new Error('请上传佐证材料'))
        } else {
          callback()
        }
      }
    }
  ]
}

// 下拉选项数据
const fields = ref([])
const indicators = ref([])
const standards = ref([])

// 记录列表
const records = ref([])
// 详情组件引用
const detailRef = ref()
// 表单引用
const formRef = ref()

// 用户名
const userName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 文件上传
const fileList = ref([])
const uploadRef = ref()
const editUploadRef = ref()

// 修改密码
const showChangePassword = ref(false)
const showEditDialog = ref(false)
const editLoading = ref(false)
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
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

// 考核领域变化
const handleFieldChange = async (fieldId) => {
  form.indicatorId = ''
  form.standardId = ''
  indicators.value = []
  standards.value = []
  
  if (fieldId) {
    try {
      const data = await getKeyIndicators(fieldId)
      indicators.value = data
      const field = fields.value.find(f => f.areaId === fieldId)
      if (field) {
        form.fieldName = field.areaName
      }
    } catch (error) {
      console.error('获取关键指标失败:', error)
    }
  }
}

// 关键指标变化
const handleIndicatorChange = async (indicatorId) => {
  form.standardId = ''
  form.standardName = ''
  standards.value = []
  
  if (indicatorId) {
    try {
      const data = await getStandards(indicatorId)
      standards.value = data
      const indicator = indicators.value.find(i => i.indicatorId === indicatorId)
      if (indicator) {
        form.indicatorName = indicator.indicatorName
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
      form.standardName = standard.standardName
    }
  } else {
    form.standardName = ''
  }
}

// 文件变化
const handleFileChange = (file) => {
  if (file && file.raw) {
    form.evidenceFile = file.name // 保存文件名用于提交给后端
    form.evidenceFileName = file.name // 保存文件名用于显示
    // 更新文件列表显示
    fileList.value = [file]
    // 手动触发验证
    formRef.value?.validateField('evidenceFile')
  }
}
const editFileList = ref([])
const handleEditFileChange = (file) => {
  if (file && file.raw) {
    editForm.evidenceFile = file.name // 保存文件名用于提交给后端
    editForm.evidenceFileName = file.name // 保存文件名用于显示
    // 更新文件列表显示
    editFileList.value = [file]
    // 手动触发验证
    editFormRef.value?.validateField('evidenceFile')
  }
}

// 获取记录列表
const loadRecords = async () => {
  try {
    const data = await getAllRecords({
      page: currentPage.value,
      size: pageSize.value
    })
    records.value = data.rows.map(record => ({
      ...record,
      showFull: false
    }))
    total.value = data.total
  } catch (error) {
    console.error('获取记录失败:', error)
  }
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!formRef.value) return
  
  try {
    // 先进行表单验证
    await formRef.value.validate()
    
    // 验证通过，发起请求
    const submitData = { ...form }
    delete submitData.evidenceFileName // 删除不需要的字段
    await saveDraft(submitData)
    ElMessage.success('保存草稿成功')
    loadRecords()
    // 仅清除表单校验状态，不重置字段
    formRef.value?.resetFields()
    // 重设提交人，避免被 resetFields 清空
    const __info = getUserInfo()
    if (__info) {
      form.submitterId = __info.id || ''
      form.submitterName = __info.name || ''
    }

  } catch (error) {
    if (error.message) {
      // 这是验证错误，显示具体的验证失败信息
      ElMessage.error(error.message)
    } else {
      console.error('保存草稿失败:', error)
      ElMessage.error(getErrorMsg(error, '保存草稿失败'))
    }
  }
}

// 提交审核
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // 先进行表单验证
    await formRef.value.validate()
    
    // 二次确认
    await ElMessageBox.confirm('确定要提交审核吗？提交后将进入审核流程，无法再修改。', '确认提交', {
      confirmButtonText: '确定提交',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 验证通过，发起请求
    const submitData = { ...form }
    delete submitData.evidenceFileName // 删除不需要的字段
    await submitRecord(submitData)
    ElMessage.success('提交审核成功')
    loadRecords()
    
    // 先清除表单验证状态，再重置表单数据
    formRef.value?.clearValidate()
    fileList.value = []
    editFileList.value = []
    
    // 重置表单数据
    Object.assign(form, {
      achievementName: '',
      obtainDate: '',
      teamMembers: '',
      fieldId: '',
      fieldName: '',
      indicatorId: '',
      indicatorName: '',
      standardId: '',
      standardName: '',
      evidenceFile: '',
      evidenceFileName: ''
    })
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消操作，不做任何处理
      return
    }
    if (error.message) {
      // 这是验证错误，显示具体的验证失败信息
      ElMessage.error(error.message)
    } else {
      console.error('提交审核失败:', error)
      ElMessage.error(getErrorMsg(error, '提交审核失败'))
    }
  }
}

// 编辑记录
const handleEdit = (row) => {
  // 预填编辑表单
  Object.assign(editForm, row)
  const field = fields.value.find(f => f.areaId === row.fieldId)
  editForm.fieldName = field ? field.areaName : row.fieldName
  // 回显已保存的附件：Element Plus 需要 { name, url }
  if (row && row.evidenceFile) {
    editFileList.value = [{
      name: getFileName(row.evidenceFile),
      url: resolveFileUrl(row.evidenceFile)
    }]
  } else {
    editFileList.value = []
  }
  // 联动加载
  handleFieldChange(row.fieldId)
    .then(() => handleIndicatorChange(row.indicatorId))
    .finally(() => {
      showEditDialog.value = true
    })
}

// 查看记录
const handleView = async (row) => {
  try {
    const data = await getRecordDetail(row.id)
    // 打开详情弹窗
    detailRef.value?.open({ ...row, ...data })
  } catch (e) {
    // 回退：用行数据打开
    detailRef.value?.open({ ...row })
  }
}

// 切换评价标准显示
const toggleStandard = (row) => {
  row.showFull = !row.showFull
}

// 解析文件URL（支持文件名或完整URL）
const resolveFileUrl = (file) => {
  if (!file) return ''
  if (/^https?:\/\//i.test(file)) return file
  if (file.startsWith('/')) return file
  return `/api/files/${encodeURIComponent(file)}`
}

// 下载文件
const downloadFile = (file) => {
  const url = resolveFileUrl(file)
  if (!url) return
  const link = document.createElement('a')
  link.href = url
  link.download = getFileName(file)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 获取文件名
const getFileName = (file) => {
  if (!file) return ''
  
  // 如果是文件对象
  if (file instanceof File) {
    return file.name
  }
  
  // 如果是字符串（文件URL或文件名）
  if (typeof file === 'string') {
    // 如果包含路径分隔符，取最后一部分
    if (file.includes('/')) {
      return file.split('/').pop()
    }
    // 否则直接返回文件名
    return file
  }
  
  return ''
}

// 日期格式化
const formatDate = (value) => {
  if (!value) return '-'
  // 兼容 Date 对象或字符串
  const date = typeof value === 'string' ? new Date(value) : value
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// 编辑表单与规则
const editFormRef = ref()
const editForm = reactive({
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
  evidenceFile: '',
  evidenceFileName: '',
  status: 0,
  rejectReason: ''
})
const editFormRules = {
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
  ]
}

// 编辑-领域联动
const handleEditFieldChange = async (fieldId) => {
  editForm.indicatorId = ''
  editForm.standardId = ''
  indicators.value = []
  standards.value = []
  if (fieldId) {
    const data = await getKeyIndicators(fieldId)
    indicators.value = data
    const field = fields.value.find(f => f.areaId === fieldId)
    if (field) editForm.fieldName = field.areaName
  }
}

// 编辑-指标联动
const handleEditIndicatorChange = async (indicatorId) => {
  editForm.standardId = ''
  editForm.standardName = ''
  standards.value = []
  if (indicatorId) {
    const data = await getStandards(indicatorId)
    standards.value = data
    const indicator = indicators.value.find(i => i.indicatorId === indicatorId)
    if (indicator) editForm.indicatorName = indicator.indicatorName
  }
}

// 编辑-评价标准变化
const handleEditStandardChange = (standardId) => {
  if (standardId) {
    const standard = standards.value.find(s => s.standardId === standardId)
    if (standard) {
      editForm.standardName = standard.standardName
    }
  } else {
    editForm.standardName = ''
  }
}



// 编辑-保存草稿
const handleEditSaveDraft = async () => {
  if (!editFormRef.value) return
  
  try {
    // 先进行表单验证
    await editFormRef.value.validate()
    
          editLoading.value = true
      const payload = { ...editForm, status: 0 }
      delete payload.evidenceFileName // 删除不需要的字段
      await updateRecord(payload)
    ElMessage.success('保存草稿成功')
    showEditDialog.value = false
    loadRecords()
    // 清除编辑表单验证状态
    editFormRef.value.clearValidate()
  } catch (error) {
    if (error.message) {
      // 这是验证错误，显示具体的验证失败信息
      ElMessage.error(error.message)
    } else {
      console.error('编辑保存草稿失败:', error)
      ElMessage.error(getErrorMsg(error, '保存草稿失败'))
    }
  } finally {
    editLoading.value = false
  }
}

// 编辑-提交审核
const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  
  try {
    // 先进行表单验证
    await editFormRef.value.validate()
    
    // 二次确认
    await ElMessageBox.confirm('确定要提交审核吗？提交后将进入审核流程，无法再修改。', '确认提交', {
      confirmButtonText: '确定提交',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    editLoading.value = true
    const payload = { ...editForm, status: 1 }
    delete payload.evidenceFileName // 删除不需要的字段
    await updateRecord(payload)
    ElMessage.success('提交审核成功')
    showEditDialog.value = false
    loadRecords()
    // 清除编辑表单验证状态
    editFormRef.value.clearValidate()
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消操作，不做任何处理
      return
    }
    if (error.message) {
      // 这是验证错误，显示具体的验证失败信息
      ElMessage.error(error.message)
    } else {
      console.error('编辑提交审核失败:', error)
      ElMessage.error(getErrorMsg(error, '提交审核失败'))
    }
  } finally {
    editLoading.value = false
  }
}

// 提取后端错误消息
const getErrorMsg = (err, fallback) => {
  return err?.response?.data?.msg || err?.message || fallback || '操作失败'
}

// 获取状态类型
const getStatusType = (status) => {
  const s = Number(status)
  const statusMap = {
    0: 'info',    // 草稿
    1: 'warning', // 审核中
    2: 'success', // 审核通过
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
    2: '审核通过',
    3: '已退回'
  }
  return statusMap[s] || '未知'
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

// 修改密码
const handleChangePassword = async () => {
  try {
    const userId = localStorage.getItem('userId')
    await updatePassword({
      id: userId,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    showChangePassword.value = false
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // 清除本地存储的用户信息
    localStorage.clear()
    // 跳转到登录页面
    router.push('/login')
  } catch (error) {
    console.error('修改密码失败:', error)
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await logout()
    localStorage.clear()
    router.push('/login')
    ElMessage.success('退出登录成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadFields()
  loadRecords()
  const info = getUserInfo()
  if (info) {
    userName.value = info.name || ''
    form.submitterId = info.id || ''
    form.submitterName = info.name || ''
  }
})
</script>

<style scoped>
.user-home {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background-color: #1e3a8a;
  color: white;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right .el-button {
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background-color: #3b82f6;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.main-content {
  padding: 24px;
  max-width: 1600px;
  margin: 0 auto;
}

.form-section,
.records-section {
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

.achievement-form {
  margin-bottom: 24px;
}

.form-buttons {
  text-align: right;
  margin-top: 24px;
}

.file-info {
  margin-top: 8px;
  color: #666;
  font-size: 14px;
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

.upload-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.inline-upload {
  display: flex;
  align-items: center;
}

.inline-upload :deep(.el-upload-list) {
  margin-left: 10px;
}

.inline-upload :deep(.el-upload-list__item) {
  margin-top: 0;
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


