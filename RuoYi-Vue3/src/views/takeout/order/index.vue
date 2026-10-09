<template>
  <div class="app-container">
    <!-- 查询区 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商家" prop="merchantId">
        <el-select v-model="queryParams.merchantId" placeholder="全部商家" clearable style="width: 200px">
          <el-option v-for="m in merchantOptions" :key="m.merchantId" :label="m.merchantName" :value="m.merchantId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 160px">
          <el-option v-for="(label, value) in statusDict" :key="value" :label="label" :value="value" />
        </el-select>
      </el-form-item>
      <el-form-item label="收货人" prop="receiverName">
        <el-input
          v-model="queryParams.receiverName"
          placeholder="请输入收货人"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="收货电话" prop="receiverPhone">
        <el-input
          v-model="queryParams.receiverPhone"
          placeholder="请输入电话"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始"
          end-placeholder="结束"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['takeout:order:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['takeout:order:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Money"
          @click="openPaymentLog"
        >支付流水</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Close"
          :disabled="single"
          @click="handleCancel"
          v-hasPermi="['takeout:order:cancel']"
        >取消订单</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['takeout:order:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="订单ID" align="center" prop="orderId" width="80" />
      <el-table-column label="订单号" align="center" prop="orderNo" :show-overflow-tooltip="true" />
      <el-table-column label="商家" align="center" prop="merchantName" :show-overflow-tooltip="true" width="160" />
      <el-table-column label="收货人" align="center" prop="receiverName" width="100" />
      <el-table-column label="联系电话" align="center" prop="receiverPhone" width="130" />
      <el-table-column label="订单金额" align="center" prop="totalAmount" width="100">
        <template #default="scope">¥ {{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="优惠金额" align="center" prop="discountAmount" width="100">
        <template #default="scope">
          <span v-if="Number(scope.row.discountAmount) > 0" style="color: #F56C6C">-¥ {{ scope.row.discountAmount }}</span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="实付金额" align="center" prop="actualAmount" width="100">
        <template #default="scope">¥ {{ scope.row.actualAmount }}</template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="payTime" width="160">
        <template #default="scope">
          <span>{{ scope.row.payTime ? parseTime(scope.row.payTime) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusDict[scope.row.status] || scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" prop="payStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.payStatus === '1'" type="success">已支付</el-tag>
          <el-tag v-else type="info">未支付</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="340" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['takeout:order:query']">详情</el-button>
          <el-button link type="primary" icon="Shop" @click="jumpToMerchant(scope.row)" v-hasPermi="['takeout:merchant:query']">商家</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['takeout:order:edit']">修改</el-button>
          <el-button link type="primary" icon="Refresh" @click="handleStatusDialog(scope.row)" v-hasPermi="['takeout:order:changeStatus']">改状态</el-button>
          <!-- P2: 仅待支付订单显示"模拟支付"按钮 -->
          <el-button v-if="String(scope.row.status) === '0'" link type="success" icon="Money" @click="openPayDialog(scope.row)">模拟支付</el-button>
          <el-button v-else-if="String(scope.row.status) === '1'" link type="warning" icon="Refresh" @click="openRefundDialog(scope.row)">退款</el-button>
          <el-button link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['takeout:order:cancel']">取消</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['takeout:order:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增 / 修改 弹窗（基础信息） -->
    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form :model="form" :rules="rules" ref="orderRef" label-width="100px">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="留空则自动生成" maxlength="64" />
        </el-form-item>
        <el-form-item label="商家" prop="merchantId">
          <el-select v-model="form.merchantId" placeholder="请选择商家" style="width: 100%" @change="onMerchantChange">
            <el-option v-for="m in merchantOptions" :key="m.merchantId" :label="m.merchantName" :value="m.merchantId" />
          </el-select>
        </el-form-item>

        <!-- 订单明细：从菜品选择 -->
        <el-form-item label="订单明细">
          <div style="width: 100%">
            <el-button size="small" type="primary" icon="Plus" plain :disabled="!form.merchantId" @click="openDishPicker">
              选择菜品
            </el-button>
            <el-button size="small" icon="Delete" plain :disabled="!form.orderItems || form.orderItems.length===0" @click="clearOrderItems">
              清空
            </el-button>
            <el-table v-if="form.orderItems && form.orderItems.length" :data="form.orderItems" size="small" style="margin-top: 8px">
              <el-table-column label="菜品" align="center" prop="dishName" />
              <el-table-column label="单价" align="center" width="100">
                <template #default="scope">¥ {{ scope.row.price }}</template>
              </el-table-column>
              <el-table-column label="数量" align="center" width="140">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" :max="999" size="small" controls-position="right" @change="recalcTotal" />
                </template>
              </el-table-column>
              <el-table-column label="小计" align="center" width="100">
                <template #default="scope">¥ {{ (Number(scope.row.price) * scope.row.quantity).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="80">
                <template #default="scope">
                  <el-button link type="danger" icon="Delete" @click="removeOrderItem(scope.$index)" />
                </template>
              </el-table-column>
            </el-table>
            <div v-else style="color: #909399; font-size: 12px; margin-top: 8px">尚未选择菜品，下方金额仍可手输</div>
          </div>
        </el-form-item>

        <el-form-item label="订单金额" prop="totalAmount">
          <el-input-number v-model="form.totalAmount" :min="0" :precision="2" :step="1" controls-position="right" style="width: 180px" />
          <span style="margin-left: 8px; color: #909399">元（选择菜品后自动计算，可手输覆盖）</span>
        </el-form-item>
        <el-form-item label="配送费" prop="deliveryFee">
          <el-input-number v-model="form.deliveryFee" :min="0" :precision="2" :step="1" controls-position="right" style="width: 180px" />
          <span style="margin-left: 8px; color: #909399">元</span>
        </el-form-item>
        <el-form-item label="优惠金额" prop="discountAmount">
          <el-input-number v-model="form.discountAmount" :min="0" :precision="2" :step="1" controls-position="right" style="width: 180px" />
          <span style="margin-left: 8px; color: #909399">元（可空，留空按0处理）</span>
        </el-form-item>
        <el-form-item label="实付金额" prop="actualAmount">
          <el-input-number v-model="form.actualAmount" :min="0" :precision="2" :step="1" controls-position="right" style="width: 180px" />
          <span style="margin-left: 8px; color: #909399">元（系统自动计算：total + 配送 - 优惠）</span>
        </el-form-item>
        <el-form-item label="收件人" prop="userId">
          <el-select
            v-model="form.userId"
            placeholder="请选择C端用户(自动填充电话地址)"
            style="width: 100%"
            filterable
            @change="onUserChange"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="`${u.nickname || u.username} (${u.phone})`"
              :value="u.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="选择收件人后自动填充" maxlength="20" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入收货地址" maxlength="255" />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-input v-model="form.payMethod" placeholder="如：微信 / 支付宝 / 余额" maxlength="20" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 选择菜品子弹窗 -->
    <el-dialog title="选择菜品" v-model="pickerOpen" width="800px" append-to-body>
      <el-form :inline="true" size="small">
        <el-form-item label="搜索">
          <el-input v-model="dishKeyword" placeholder="菜品名" clearable @keyup.enter="loadPickerDishes" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadPickerDishes">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="pickerLoading" :data="pickerDishes" @selection-change="onPickerSelect" max-height="420">
        <el-table-column type="selection" width="50" />
        <el-table-column label="图片" align="center" width="70">
          <template #default="scope">
            <image-preview v-if="scope.row.image" :src="scope.row.image" :width="40" :height="40" />
            <span v-else style="color: #c0c4cc">无</span>
          </template>
        </el-table-column>
        <el-table-column label="菜品" prop="dishName" />
        <el-table-column label="分类" prop="categoryName" width="100" />
        <el-table-column label="价格" prop="price" width="100">
          <template #default="scope">¥ {{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column label="库存" prop="stock" width="80" />
        <el-table-column label="销量" prop="sales" width="80" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmPickedDishes">加入订单</el-button>
          <el-button @click="pickerOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情 弹窗 -->
    <el-dialog title="订单详情" v-model="detailOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)">{{ statusDict[detail.status] || detail.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商家">
          <el-link type="primary" :underline="false" @click="jumpToMerchant(detail)" v-hasPermi="['takeout:merchant:query']">
            {{ detail.merchantName }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥ {{ detail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="配送费">¥ {{ detail.deliveryFee }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥ {{ detail.discountAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥ {{ detail.actualAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="用券ID">{{ detail.couponUserId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detail.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag v-if="detail.payStatus === '1'" type="success">已支付</el-tag>
          <el-tag v-else type="info">未支付</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.payMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detail.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ detail.address }}</el-descriptions-item>
        <el-descriptions-item label="期望送达">{{ detail.deliveryTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detail.completeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" :span="2">{{ detail.cancelReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
      </el-descriptions>

      <div style="margin-top: 16px; font-weight: 600">订单商品</div>
      <el-table :data="detail.orderItems || []" size="small" style="margin-top: 8px">
        <el-table-column label="菜品" align="center" prop="dishName">
          <template #default="scope">
            <el-link type="primary" :underline="false" @click="jumpToDish(scope.row)" v-hasPermi="['takeout:dish:query']">
              {{ scope.row.dishName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="单价" align="center" prop="price" width="100">
          <template #default="scope">¥ {{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column label="数量" align="center" prop="quantity" width="80" />
        <el-table-column label="小计" align="center" prop="subtotal" width="120">
          <template #default="scope">¥ {{ scope.row.subtotal }}</template>
        </el-table-column>
      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 改状态 弹窗 -->
    <el-dialog title="修改订单状态" v-model="statusOpen" width="500px" append-to-body>
      <el-form :model="statusForm" ref="statusRef" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ statusForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="statusTagType(statusForm.currentStatus)">{{ statusDict[statusForm.currentStatus] }}</el-tag>
        </el-form-item>
        <el-form-item label="变更后" prop="targetStatus">
          <el-select v-model="statusForm.targetStatus" placeholder="选择目标状态" style="width: 100%">
            <el-option
              v-for="opt in nextStatusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitStatus">确 定</el-button>
          <el-button @click="statusOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 取消订单 弹窗 -->
    <el-dialog title="取消订单" v-model="cancelOpen" width="500px" append-to-body>
      <el-form :model="cancelForm" ref="cancelRef" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ cancelForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="取消原因" prop="cancelReason">
          <el-input v-model="cancelForm.cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitCancel">确 定</el-button>
          <el-button @click="cancelOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- P2: 支付弹窗(Mock 演示) -->
    <el-dialog title="模拟支付" v-model="payOpen" width="560px" append-to-body>
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="订单号"><span>{{ payForm.orderNo }}</span></el-form-item>
        <el-form-item label="支付金额"><b style="color:#E6A23C;font-size:18px">¥ {{ payForm.amount }}</b></el-form-item>
        <el-form-item label="支付渠道">
          <el-radio-group v-model="payForm.channel">
            <el-radio value="MOCK">Mock(开发演示)</el-radio>
            <el-radio value="WECHAT" disabled>微信支付(待接入)</el-radio>
            <el-radio value="ALIPAY" disabled>支付宝(待接入)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="payResult.prepayInfo" label="二维码内容">
          <el-input v-model="payResult.prepayInfo" readonly type="textarea" :rows="2" />
          <div style="color:#909399;font-size:12px;margin-top:4px">真实场景:此处展示扫码二维码图片</div>
        </el-form-item>
        <el-form-item v-if="payResult.tradeNo" label="渠道流水号">
          <el-tag>{{ payResult.tradeNo }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="paying" @click="submitPay">发起支付</el-button>
        <el-button :disabled="!payResult.tradeNo" @click="submitQuery">我已支付(主动查账)</el-button>
        <el-button @click="payOpen = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- P2: 退款弹窗 -->
    <el-dialog title="订单退款" v-model="refundOpen" width="500px" append-to-body>
      <el-form :model="refundForm" label-width="90px">
        <el-form-item label="订单号"><span>{{ refundForm.orderNo }}</span></el-form-item>
        <el-form-item label="退款金额"><b>¥ {{ refundForm.amount }}</b></el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" placeholder="请输入退款原因" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" :loading="refunding" @click="submitRefund">确认退款</el-button>
        <el-button @click="refundOpen = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- P2: 支付流水 -->
    <el-dialog title="支付流水" v-model="paymentLogOpen" width="900px" append-to-body>
      <el-table :data="paymentList" v-loading="paymentLoading" size="small" border>
        <el-table-column label="ID" prop="id" width="60" align="center" />
        <el-table-column label="订单号" prop="orderNo" width="170" :show-overflow-tooltip="true" />
        <el-table-column label="渠道" prop="channel" width="80" align="center" />
        <el-table-column label="金额" prop="amount" width="100" align="center">
          <template #default="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100" align="center">
          <template #default="scope">
            <el-tag :type="paymentStatusType(scope.row.status)">{{ paymentStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="渠道流水号" prop="tradeNo" width="200" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutOrder">
import { listOrder, getOrder, addOrder, updateOrder, delOrder, changeOrderStatus, cancelOrder, getOrderStatusDict, exportOrder, listDishByMerchant } from "@/api/takeout/order"
import { listMerchant } from "@/api/takeout/merchant"
import { listUser } from "@/api/takeout/user"
import { addDateRange, parseTime } from "@/utils/ruoyi"
import { listPayment, createPayment, queryPayment, refundPayment } from "@/api/takeout/payment"

const { proxy } = getCurrentInstance()

const orderList = ref([])
const merchantOptions = ref([])
const userOptions = ref([])
const open = ref(false)
const detailOpen = ref(false)
const statusOpen = ref(false)
const cancelOpen = ref(false)
// P2: 支付/退款/流水弹窗
const payOpen = ref(false)
const refundOpen = ref(false)
const paymentLogOpen = ref(false)
const paying = ref(false)
const refunding = ref(false)
const paymentLoading = ref(false)
const paymentList = ref([])
const payResult = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])

// 状态字典 (key=code value=label)
const statusDict = ref({})

// 状态机：可作为下一态
const NEXT_ALLOWED = {
  '0': [['1', '已支付'], ['6', '已取消']],
  '1': [['2', '商家接单'], ['7', '已退款'], ['6', '已取消']],
  '2': [['3', '配送中'], ['7', '已退款']],
  '3': [['4', '已送达']],
  '4': [['5', '已完成']],
  '5': [],
  '6': [],
  '7': []
}

const nextStatusOptions = ref([])
const detail = ref({})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    merchantId: undefined,
    status: undefined,
    receiverName: undefined,
    receiverPhone: undefined
  },
  form: {},
  rules: {
    merchantId: [{ required: true, message: "请选择商家", trigger: "change" }],
    userId: [{ required: true, message: "请选择C端收件人", trigger: "change" }],
    totalAmount: [{ required: true, message: "请输入订单金额", trigger: "blur" }],
    receiverName: [{ required: true, message: "请选择收件人后自动填充", trigger: "change" }],
    receiverPhone: [
      { required: true, message: "请选择收件人后自动填充电话", trigger: "change" },
      { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
    ]
  },
  statusForm: {
    orderId: undefined,
    orderNo: undefined,
    currentStatus: undefined,
    targetStatus: undefined
  },
  cancelForm: {
    orderId: undefined,
    orderNo: undefined,
    cancelReason: undefined
  },
  payForm: {
    paymentId: undefined,
    orderId: undefined,
    orderNo: undefined,
    amount: undefined,
    channel: 'MOCK'
  },
  refundForm: {
    paymentId: undefined,
    orderId: undefined,
    orderNo: undefined,
    amount: undefined,
    reason: undefined
  }
})

const { queryParams, form, rules, statusForm, cancelForm, payForm, refundForm } = toRefs(data)

// ===== 菜品选择子表 =====
const pickerOpen = ref(false)
const pickerLoading = ref(false)
const pickerDishes = ref([])
const pickerSelected = ref([])
const dishKeyword = ref('')

// 打开选菜弹窗
function openDishPicker() {
  if (!form.value.merchantId) {
    proxy.$modal.msgWarning('请先选择商家')
    return
  }
  pickerSelected.value = []
  dishKeyword.value = ''
  pickerOpen.value = true
  loadPickerDishes()
}

function loadPickerDishes() {
  pickerLoading.value = true
  listDishByMerchant(form.value.merchantId).then(res => {
    pickerDishes.value = res.data || []
    pickerLoading.value = false
  }).catch(() => { pickerLoading.value = false })
}

function onPickerSelect(rows) {
  pickerSelected.value = rows
}

function confirmPickedDishes() {
  if (!pickerSelected.value.length) {
    proxy.$modal.msgWarning('请勾选菜品')
    return
  }
  if (!form.value.orderItems) form.value.orderItems = []
  // 合并（去重：同 dishId 数量累加）
  for (const d of pickerSelected.value) {
    const exist = form.value.orderItems.find(x => x.dishId === d.dishId)
    if (exist) {
      exist.quantity += 1
    } else {
      form.value.orderItems.push({
        dishId: d.dishId,
        dishName: d.dishName,
        dishImage: d.image,
        price: Number(d.price) || 0,
        quantity: 1,
        subtotal: Number(d.price) || 0
      })
    }
  }
  recalcTotal()
  pickerOpen.value = false
}

function removeOrderItem(idx) {
  form.value.orderItems.splice(idx, 1)
  recalcTotal()
}

function clearOrderItems() {
  form.value.orderItems = []
  recalcTotal()
}

function recalcTotal() {
  if (!form.value.orderItems) return
  let total = 0
  for (const it of form.value.orderItems) {
    it.subtotal = +((Number(it.price) || 0) * (Number(it.quantity) || 0)).toFixed(2)
    total += it.subtotal
  }
  form.value.totalAmount = +total.toFixed(2)
}

function onMerchantChange(merchantId) {
  // 切换商家时清空已选菜品
  form.value.orderItems = []
  recalcTotal()
}

function statusTagType(s) {
  switch (s) {
    case '0': return 'info'
    case '1': return 'primary'
    case '2': return 'warning'
    case '3': return 'warning'
    case '4': return 'success'
    case '5': return 'success'
    case '6': return 'danger'
    case '7': return 'danger'
    default: return ''
  }
}

function getList() {
  loading.value = true
  const params = addDateRange(queryParams.value, dateRange.value, 'Time')
  listOrder(params).then(res => {
    loading.value = false
    orderList.value = res.rows
    total.value = res.total
  })
}

function loadDicts() {
  getOrderStatusDict().then(res => { statusDict.value = res.data || {} })
}

function loadMerchants() {
  listMerchant({ pageNum: 1, pageSize: 200 }).then(res => {
    merchantOptions.value = (res.rows || []).map(m => ({ merchantId: m.merchantId, merchantName: m.merchantName }))
  })
}

function loadUsers() {
  listUser({ pageNum: 1, pageSize: 500 }).then(res => {
    userOptions.value = (res.rows || []).map(u => ({
      userId: u.userId,
      username: u.username,
      nickname: u.nickname,
      phone: u.phone
    }))
  })
}

// 选中C端用户后自动填充收件人/电话
function onUserChange(userId) {
  const u = userOptions.value.find(x => x.userId === userId)
  if (u) {
    form.value.receiverName = u.nickname || u.username
    form.value.receiverPhone = u.phone
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.orderId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    orderId: undefined,
    orderNo: undefined,
    userId: undefined,
    merchantId: undefined,
    totalAmount: 0,
    deliveryFee: 0,
    discountAmount: 0,
    actualAmount: 0,
    couponUserId: undefined,
    payTime: undefined,
    status: '0',
    payStatus: '0',
    payMethod: undefined,
    receiverName: undefined,
    receiverPhone: undefined,
    address: undefined,
    remark: undefined,
    orderItems: []
  }
  proxy.resetForm("orderRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增订单"
}

function handleUpdate(row) {
  reset()
  const orderId = row.orderId || ids.value
  getOrder(orderId).then(res => {
    form.value = res.data
    open.value = true
    title.value = "修改订单"
  })
}

function submitForm() {
  proxy.$refs["orderRef"].validate(valid => {
    if (valid) {
      // ★ 若已选菜品，则按明细重算 totalAmount
      if (form.value.orderItems && form.value.orderItems.length) {
        recalcTotal()
      }
      // ★ 自动计算实付金额 = total + delivery - discount
      const t = Number(form.value.totalAmount) || 0
      const d = Number(form.value.deliveryFee) || 0
      const dis = Number(form.value.discountAmount) || 0
      form.value.actualAmount = Math.max(0, +(t + d - dis).toFixed(2))
      if (form.value.orderId != undefined) {
        updateOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        // 自动生成订单号
        if (!form.value.orderNo) {
          const ts = new Date()
          const pad = n => (n < 10 ? '0' + n : '' + n)
          form.value.orderNo = 'TKO' + ts.getFullYear() + pad(ts.getMonth() + 1) + pad(ts.getDate()) + pad(ts.getHours()) + pad(ts.getMinutes()) + pad(ts.getSeconds()) + Math.floor(Math.random() * 1000)
        }
        addOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const orderIds = row.orderId || ids.value
  proxy.$modal.confirm('是否确认删除订单编号为"' + orderIds + '"的订单？').then(() => {
    return delOrder(orderIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

function handleExport() {
  const params = addDateRange(queryParams.value, dateRange.value, 'Time')
  proxy.download('takeout/order/export', params, `order_${new Date().getTime()}.xlsx`)
}

function handleView(row) {
  getOrder(row.orderId).then(res => {
    detail.value = res.data
    detailOpen.value = true
  })
}

function handleStatusDialog(row) {
  const orderId = row.orderId
  const cur = row.status
  const opts = NEXT_ALLOWED[cur] || []
  if (opts.length === 0) {
    proxy.$modal.msgWarning("当前状态已是终态，无可变更状态")
    return
  }
  statusForm.value = {
    orderId: orderId,
    orderNo: row.orderNo,
    currentStatus: cur,
    targetStatus: opts[0][0]
  }
  nextStatusOptions.value = opts.map(o => ({ value: o[0], label: o[1] }))
  statusOpen.value = true
}

function submitStatus() {
  if (!statusForm.value.targetStatus) {
    proxy.$modal.msgWarning("请选择目标状态")
    return
  }
  changeOrderStatus(statusForm.value.orderId, statusForm.value.targetStatus).then(() => {
    proxy.$modal.msgSuccess("状态已变更")
    statusOpen.value = false
    getList()
  })
}

function handleCancel(row) {
  const orderId = row.orderId || ids.value
  const orderNo = row?.orderNo
  cancelForm.value = {
    orderId: orderId,
    orderNo: orderNo,
    cancelReason: undefined
  }
  cancelOpen.value = true
}

function submitCancel() {
  if (!cancelForm.value.cancelReason) {
    proxy.$modal.msgWarning("请输入取消原因")
    return
  }
  cancelOrder(cancelForm.value.orderId, cancelForm.value.cancelReason).then(() => {
    proxy.$modal.msgSuccess("订单已取消")
    cancelOpen.value = false
    getList()
  })
}

function cancel() {
  open.value = false
  reset()
}

// 跳转到菜品管理页（带 dishId 参数）
function jumpToDish(item) {
  if (!item || !item.dishId) {
    proxy.$modal.msgWarning('该订单明细缺少菜品ID')
    return
  }
  proxy.$router.push({ path: '/takeout/dish', query: { dishId: item.dishId } })
}

// 跳转到商家管理页（带 merchantId 参数）
function jumpToMerchant(row) {
  const mid = row?.merchantId
  if (!mid) {
    proxy.$modal.msgWarning('该订单未关联商家')
    return
  }
  proxy.$router.push({ path: '/takeout/merchant', query: { merchantId: mid } })
}

// ===== P2: 支付相关函数 =====

/**
 * 打开支付弹窗前先查该订单的支付流水记录(取最新一条 PENDING 的 paymentId)
 */
async function openPayDialog(row) {
  try {
    paymentLoading.value = true
    const list = await listPayment({ orderId: row.orderId })
    const rows = Array.isArray(list) ? list : (list.rows || list.data || [])
    const pending = rows.find(p => p.status === 'PENDING') || rows[0]
    if (!pending) {
      proxy.$modal.msgWarning('未找到该订单的支付挂单记录')
      return
    }
    payForm.value = {
      paymentId: pending.id,
      orderId: row.orderId,
      orderNo: row.orderNo,
      amount: row.totalAmount,
      channel: 'MOCK'
    }
    payResult.value = {}
    payOpen.value = true
  } finally {
    paymentLoading.value = false
  }
}

async function submitPay() {
  paying.value = true
  try {
    const r = await createPayment(payForm.value.paymentId)
    if (r && r.code === 'SUCCESS') {
      payResult.value = r
      proxy.$modal.msgSuccess('Mock 支付已成功,订单状态已推进')
      getList()
    } else {
      proxy.$modal.msgError(r?.errorMsg || '发起支付失败')
    }
  } finally {
    paying.value = false
  }
}

async function submitQuery() {
  paying.value = true
  try {
    const r = await queryPayment(payForm.value.paymentId)
    if (r && r.code === 'SUCCESS') {
      proxy.$modal.msgSuccess('查账完成,状态=' + r.code)
      payOpen.value = false
      getList()
    }
  } finally {
    paying.value = false
  }
}

async function openRefundDialog(row) {
  try {
    paymentLoading.value = true
    const list = await listPayment({ orderId: row.orderId })
    const rows = Array.isArray(list) ? list : (list.rows || list.data || [])
    const success = rows.find(p => p.status === 'SUCCESS')
    if (!success) {
      proxy.$modal.msgWarning('该订单没有已支付的记录')
      return
    }
    refundForm.value = {
      paymentId: success.id,
      orderId: row.orderId,
      orderNo: row.orderNo,
      amount: success.amount,
      reason: undefined
    }
    refundOpen.value = true
  } finally {
    paymentLoading.value = false
  }
}

async function submitRefund() {
  refunding.value = true
  try {
    const r = await refundPayment(refundForm.value.paymentId, refundForm.value.reason || '管理员手动退款')
    if (r && r.code === 'SUCCESS') {
      proxy.$modal.msgSuccess('退款已发起')
      refundOpen.value = false
      getList()
    } else {
      proxy.$modal.msgError(r?.errorMsg || '退款失败')
    }
  } finally {
    refunding.value = false
  }
}

function paymentStatusType(s) {
  return ({ 'PENDING': 'info', 'SUCCESS': 'success', 'FAILED': 'danger', 'REFUNDED': 'warning', 'CLOSED': '' })[s] || ''
}

function paymentStatusLabel(s) {
  return ({ 'PENDING': '待支付', 'SUCCESS': '已支付', 'FAILED': '失败', 'REFUNDED': '已退款', 'CLOSED': '已关闭' })[s] || s
}

async function openPaymentLog() {
  paymentLogOpen.value = true
  paymentLoading.value = true
  try {
    const res = await listPayment({ pageNum: 1, pageSize: 50 })
    paymentList.value = Array.isArray(res) ? res : (res.rows || res.data || [])
  } finally {
    paymentLoading.value = false
  }
}

onMounted(() => {
  loadDicts()
  loadMerchants()
  loadUsers()
  getList()
})
</script>
