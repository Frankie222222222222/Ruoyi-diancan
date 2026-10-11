<template>
  <div class="app-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" ref="queryForm" size="default">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model.number="queryParams.userId" placeholder="按用户ID筛选" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="dataList" stripe>
        <el-table-column label="地址ID" prop="address_id" width="90" />
        <el-table-column label="用户ID" prop="user_id" width="100" />
        <el-table-column label="收货人" prop="name" width="100" />
        <el-table-column label="手机号" prop="phone" width="130" />
        <el-table-column label="详细地址" min-width="220">
          <template #default="{ row }">{{ row.address }} {{ row.house_number }}</template>
        </el-table-column>
        <el-table-column label="经纬度" width="180">
          <template #default="{ row }">{{ row.lat }}, {{ row.lng }}</template>
        </el-table-column>
        <el-table-column label="默认" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.is_default === '1'" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="create_time" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.is_default !== '1'" type="success" link @click="setDefault(row)">设为默认</el-button>
            <el-button type="danger" link @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="loadList" />
    </el-card>

    <el-dialog v-model="editVisible" :title="form.addressId ? '编辑地址' : '新增地址'" width="560px">
      <el-form ref="editForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" />
        </el-form-item>
        <el-form-item label="收货人" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="门牌号">
          <el-input v-model="form.houseNumber" />
        </el-form-item>
        <el-form-item label="地图选址">
          <AMapPicker v-model:modelAddress="form.address" v-model:modelLat="form.lat" v-model:modelLng="form.lng" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefaultFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutAddress">
import { ref, reactive, onMounted } from 'vue'
import { listAddress, getAddress, saveAddress, setDefaultAddress, delAddress } from '@/api/takeout/address'
import AMapPicker from '@/components/AMapPicker/index.vue'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 20, userId: undefined })
const editVisible = ref(false)
const editForm = ref()
const form = reactive({
  addressId: null, userId: 1, name: '', phone: '', address: '', houseNumber: '',
  lat: null, lng: null, isDefaultFlag: 0
})
const rules = {
  userId: [{ required: true, message: '用户ID必填', trigger: 'blur' }],
  name: [{ required: true, message: '收货人必填', trigger: 'blur' }],
  phone: [{ required: true, message: '手机号必填', trigger: 'blur' }],
  address: [{ required: true, message: '详细地址必填', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await listAddress({ userId: queryParams.value.userId })
    dataList.value = res.rows || []
    total.value = res.total || 0
  } finally { loading.value = false }
}
function handleQuery() { queryParams.value.pageNum = 1; loadList() }
function resetQuery() { queryParams.value = { pageNum: 1, pageSize: 20, userId: undefined }; loadList() }

async function openEdit(row) {
  if (row) {
    const full = await getAddress(row.address_id)
    Object.assign(form, {
      addressId: full.address_id, userId: full.user_id, name: full.name, phone: full.phone,
      address: full.address, houseNumber: full.house_number,
      lat: full.lat ? Number(full.lat) : null,
      lng: full.lng ? Number(full.lng) : null,
      isDefaultFlag: full.is_default === '1' ? 1 : 0
    })
  } else {
    Object.assign(form, {
      addressId: null, userId: 1, name: '', phone: '', address: '', houseNumber: '',
      lat: null, lng: null, isDefaultFlag: 0
    })
  }
  editVisible.value = true
}

async function submitForm() {
  await editForm.value.validate()
  const payload = {
    addressId: form.addressId,
    userId: form.userId,
    name: form.name,
    phone: form.phone,
    address: form.address,
    houseNumber: form.houseNumber,
    lat: form.lat,
    lng: form.lng,
    isDefault: form.isDefaultFlag === 1 ? '1' : '0'
  }
  await saveAddress(payload)
  ElMessage.success('保存成功')
  editVisible.value = false
  loadList()
}

async function setDefault(row) {
  await setDefaultAddress(row.address_id)
  ElMessage.success('已设为默认')
  loadList()
}
async function removeRow(row) {
  await ElMessageBox.confirm('确定删除该地址?', '提示', { type: 'warning' })
  await delAddress(row.address_id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>

<style lang="scss" scoped>
.filter-card { margin-bottom: 16px; }
</style>
