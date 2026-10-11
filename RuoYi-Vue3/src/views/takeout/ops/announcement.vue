<template>
  <div class="app-container">
    <el-card>
      <el-button type="primary" icon="Plus" @click="openEdit()">发布公告</el-button>
    </el-card>
    <el-card style="margin-top: 12px">
      <el-table v-loading="loading" :data="dataList" stripe>
        <el-table-column label="ID" prop="announcement_id" width="80" />
        <el-table-column label="标题" prop="title" min-width="180" />
        <el-table-column label="内容" prop="content" min-width="280" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="create_time" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="editVisible" :title="form.announcementId ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="OpsAnnouncement">
import { ref, reactive, onMounted } from 'vue'
import { listAnnouncement, saveAnnouncement, delAnnouncement } from '@/api/takeout/ops'

const loading = ref(false)
const dataList = ref([])
const editVisible = ref(false)
const form = reactive({ announcementId: null, title: '', content: '', status: '0' })

async function loadList() {
  loading.value = true
  try {
    const res = await listAnnouncement()
    dataList.value = res.rows || []
  } finally { loading.value = false }
}
function openEdit(row) {
  if (row) Object.assign(form, { announcementId: row.announcement_id, title: row.title, content: row.content, status: row.status })
  else Object.assign(form, { announcementId: null, title: '', content: '', status: '0' })
  editVisible.value = true
}
async function submit() {
  await saveAnnouncement({ ...form })
  ElMessage.success('已保存'); editVisible.value = false; loadList()
}
async function remove(row) {
  await ElMessageBox.confirm('确定删除该公告?', '提示', { type: 'warning' })
  await delAnnouncement(row.announcement_id)
  ElMessage.success('已删除'); loadList()
}
onMounted(loadList)
</script>
