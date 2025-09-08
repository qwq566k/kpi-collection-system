<template>
  <AdminLayout>
    <div class="users-page">
      <h2 class="page-title">用户管理</h2>
      
      <!-- 查询条件 -->
      <div class="query-section">
        <el-form :model="queryForm" inline>
          <el-form-item label="姓名">
            <el-input 
              v-model="queryForm.name" 
              placeholder="输入姓名"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="工号">
            <el-input 
              v-model="queryForm.employeeId" 
              placeholder="输入工号"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="部门名称">
            <el-select 
              v-model="queryForm.department" 
              placeholder="选择部门"
              clearable
              style="width: 200px"
            >
              <el-option
                v-for="dept in departments"
                :key="dept"
                :label="dept"
                :value="dept"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 操作按钮 -->
      <div class="actions-section">
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
        <el-button @click="showImportDialog = true">
          <el-icon><Upload /></el-icon>
          Excel导入
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedUsers.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </div>
      
      <!-- 用户列表 -->
      <div class="table-section">
        <el-table 
          :data="users" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="employeeId" label="工号"  />
          <el-table-column prop="name" label="姓名"  />
          <el-table-column prop="phone" label="手机号"  />
          <el-table-column prop="department" label="部门名称"  />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small"
                @click="handleDelete(row)"
              >
                删除
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

    <!-- 新增/编辑用户对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="editingUser.id ? '编辑用户' : '新增用户'" 
      width="500px"
    >
      <el-form :model="editingUser" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="工号" prop="employeeId">
          <el-input v-model="editingUser.employeeId" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editingUser.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editingUser.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="部门名称" prop="department">
          <el-input v-model="editingUser.department" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item v-if="!editingUser.id" label="密码" prop="password">
          <el-input 
            v-model="editingUser.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editingUser.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- Excel导入对话框 -->
    <el-dialog v-model="showImportDialog" title="Excel导入" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="importFileList"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-button type="primary">选择Excel文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            只能上传xlsx/xls文件，且不超过10MB
          </div>
        </template>
      </el-upload>

      
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          :disabled="!selectedFile"
          @click="handleImport"
        >
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import AdminLayout from '../../components/AdminLayout.vue'
import { 
  queryUsers, 
  addUser, 
  updateUser, 
  deleteUser, 
  importUsers 
} from '../../api/admin'

// 查询条件
const queryForm = reactive({
  name: '',
  employeeId: '',
  department: ''
})

// 部门选项（这里应该从后端获取，暂时写死）
const departments = ref(['计算机系', '软件工程系', '通信工程系', '数学系', '物理系'])

// 用户列表
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 选中的用户
const selectedUsers = ref([])

// 新增/编辑用户
const showAddDialog = ref(false)
const editingUser = reactive({
  id: '',
  employeeId: '',
  name: '',
  phone: '',
  department: '',
  password: '',
  role: 1
})

const userRules = {
  employeeId: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// Excel导入
const showImportDialog = ref(false)
const importFileList = ref([])
const selectedFile = ref(null)
const importResult = ref(null)

// 查询用户
const loadUsers = async () => {
  try {
    const data = await queryUsers({
      ...queryForm,
      page: currentPage.value,
      size: pageSize.value
    })
    users.value = data.rows
    total.value = data.total
  } catch (error) {
    console.error('查询用户失败:', error)
  }
}

// 执行查询
const handleQuery = () => {
  currentPage.value = 1
  loadUsers()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 新增用户
const handleAdd = () => {
  Object.assign(editingUser, {
    id: '',
    employeeId: '',
    name: '',
    phone: '',
    department: '',
    password: '',
    role: 1
  })
  showAddDialog.value = true
}

// 编辑用户
const handleEdit = (row) => {
  Object.assign(editingUser, row)
  showAddDialog.value = true
}

// 保存用户
const handleSaveUser = async () => {
  try {
    if (editingUser.id) {
      await updateUser(editingUser)
      ElMessage.success('编辑用户成功')
    } else {
      await addUser(editingUser)
      ElMessage.success('新增用户成功')
    }
    showAddDialog.value = false
    loadUsers()
  } catch (error) {
    console.error('保存用户失败:', error)
  }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteUser(row.id)
    ElMessage.success('删除用户成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    const ids = selectedUsers.value.map(user => user.id).join(',')
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedUsers.value.length} 个用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteUser(ids)
    ElMessage.success('批量删除成功')
    selectedUsers.value = []
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 文件变化
const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

// 导入Excel
const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择Excel文件')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    
    const result = await importUsers(formData)
    importResult.value = result
    
    if (result.successCount > 0) {
      ElMessage.success(`成功导入 ${result.successCount} 条数据`)
      loadUsers()
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败: ' + (error.response?.data?.message || error.message))
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadUsers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadUsers()
}

// 页面加载时获取数据
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-page {
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

.actions-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 16px;
}

.table-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.import-result {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.import-result h4 {
  margin: 0 0 12px 0;
  color: #333;
}

.import-result p {
  margin: 8px 0;
  color: #666;
}

.import-result h5 {
  margin: 16px 0 8px 0;
  color: #333;
}

.import-result ul {
  margin: 8px 0;
  padding-left: 20px;
}

.import-result li {
  color: #e74c3c;
  margin: 4px 0;
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


