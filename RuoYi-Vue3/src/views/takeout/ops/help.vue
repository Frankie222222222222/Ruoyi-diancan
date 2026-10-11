<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>分类</span>
            <el-button type="primary" size="small" link style="float: right" @click="openCatEdit()">+ 新增</el-button>
          </template>
          <el-table v-loading="catLoading" :data="catList" highlight-current-row @row-click="selectCat">
            <el-table-column label="ID" prop="category_id" width="60" />
            <el-table-column label="名称" prop="name" />
            <el-table-column label="排序" prop="sort" width="60" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link @click.stop="openCatEdit(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>{{ currentCat ? `「${currentCat.name}」下的文章` : '请先选择左侧分类' }}</span>
            <el-button v-if="currentCat" type="primary" size="small" link style="float: right" @click="openArtEdit()">+ 新增文章</el-button>
          </template>
          <el-table v-loading="artLoading" :data="artList">
            <el-table-column label="ID" prop="article_id" width="60" />
            <el-table-column label="标题" prop="title" />
            <el-table-column label="排序" prop="sort" width="60" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" link @click="openArtEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="removeArt(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="catVisible" :title="catForm.categoryId ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="catForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="catForm.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="catForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCat">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="artVisible" :title="artForm.articleId ? '编辑文章' : '新增文章'" width="640px">
      <el-form :model="artForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="artForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="artForm.content" type="textarea" :rows="8" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="artForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="artVisible = false">取消</el-button>
        <el-button type="primary" @click="saveArt">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="OpsHelp">
import { ref, reactive, onMounted } from 'vue'
import { listHelpCategory, saveHelpCategory, listHelpArticle, saveHelpArticle, delHelpArticle } from '@/api/takeout/ops'

const catLoading = ref(false)
const artLoading = ref(false)
const catList = ref([])
const artList = ref([])
const currentCat = ref(null)
const catVisible = ref(false)
const artVisible = ref(false)
const catForm = reactive({ categoryId: null, name: '', sort: 0 })
const artForm = reactive({ articleId: null, categoryId: null, title: '', content: '', sort: 0 })

async function loadCat() {
  catLoading.value = true
  try {
    const res = await listHelpCategory()
    catList.value = res.rows || []
  } finally { catLoading.value = false }
}
async function loadArt(categoryId) {
  artLoading.value = true
  try {
    const res = await listHelpArticle(categoryId)
    artList.value = res.rows || []
  } finally { artLoading.value = false }
}
function selectCat(row) {
  currentCat.value = row
  artForm.categoryId = row.category_id
  loadArt(row.category_id)
}
function openCatEdit(row) {
  if (row) Object.assign(catForm, { categoryId: row.category_id, name: row.name, sort: row.sort })
  else Object.assign(catForm, { categoryId: null, name: '', sort: 0 })
  catVisible.value = true
}
async function saveCat() {
  await saveHelpCategory({ ...catForm })
  ElMessage.success('已保存'); catVisible.value = false; loadCat()
}
function openArtEdit(row) {
  if (row) Object.assign(artForm, { articleId: row.article_id, categoryId: row.category_id, title: row.title, content: row.content, sort: row.sort })
  else Object.assign(artForm, { articleId: null, categoryId: currentCat.value.category_id, title: '', content: '', sort: 0 })
  artVisible.value = true
}
async function saveArt() {
  await saveHelpArticle({ ...artForm })
  ElMessage.success('已保存'); artVisible.value = false; loadArt(artForm.categoryId)
}
async function removeArt(row) {
  await ElMessageBox.confirm('确定删除该文章?', '提示', { type: 'warning' })
  await delHelpArticle(row.article_id)
  ElMessage.success('已删除'); loadArt(artForm.categoryId)
}
onMounted(loadCat)
</script>
