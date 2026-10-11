<template>
  <div class="app-container">
    <el-card>
      <el-button type="primary" icon="Plus" @click="openEdit()">新增 Banner</el-button>
      <el-button type="success" icon="Refresh" @click="loadList" style="margin-left: 8px">刷新</el-button>
    </el-card>
    <el-card style="margin-top: 12px">
      <el-table v-loading="loading" :data="dataList" stripe>
        <el-table-column label="ID" prop="banner_id" width="80" />
        <el-table-column label="标题" prop="title" min-width="160" />
        <el-table-column label="图片" width="200">
          <template #default="{ row }">
            <el-image :src="row.image" :preview-src-list="[row.image]" style="width: 160px; height: 80px" fit="cover" hide-on-click-modal />
          </template>
        </el-table-column>
        <el-table-column label="链接" prop="link" min-width="200" show-overflow-tooltip />
        <el-table-column label="排序" prop="sort" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" :title="form.bannerId ? '编辑 Banner' : '新增 Banner'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="图片">
          <ImageUpload v-model="form.image" />
        </el-form-item>
        <el-form-item label="链接"><el-input v-model="form.link" placeholder="如 /pages/home/home" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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

<script setup name="OpsBanner">
import { ref, reactive, onMounted } from 'vue'
import { listBanner, saveBanner, delBanner } from '@/api/takeout/ops'
import ImageUpload from '@/components/ImageUpload/index.vue'

const loading = ref(false)
const dataList = ref([])
const editVisible = ref(false)
const form = reactive({ bannerId: null, title: '', image: '', link: '', sort: 0, status: '0' })

async function loadList() {
  loading.value = true
  try {
    const res = await listBanner()
    dataList.value = res.rows || []
  } finally { loading.value = false }
}
function openEdit(row) {
  if (row) Object.assign(form, { bannerId: row.banner_id, title: row.title, image: row.image, link: row.link, sort: row.sort, status: row.status })
  else Object.assign(form, { bannerId: null, title: '', image: '', link: '', sort: 0, status: '0' })
  editVisible.value = true
}
async function submit() {
  await saveBanner({ ...form })
  ElMessage.success('已保存')
  editVisible.value = false
  loadList()
}
async function remove(row) {
  await ElMessageBox.confirm('确定删除该 Banner?', '提示', { type: 'warning' })
  await delBanner(row.banner_id)
  ElMessage.success('已删除')
  loadList()
}
onMounted(loadList)
</script>
