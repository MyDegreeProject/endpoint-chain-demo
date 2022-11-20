<template>
  <div class="app-container">
    <el-table v-loading="listLoading" :data="list" element-loading-text="Loading">
      <el-table-column align="center" label="序号" width="60px">
        <template slot-scope="scope">
          {{ scope.$index+1 }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="端点地址" show-overflow-tooltip width="190px">
        <template slot-scope="scope">
          {{ scope.row.endpointUrl }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="端点名称" width="190px">
        <template slot-scope="scope">
          {{ scope.row.endpointName }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="ipfs上摘要Cid" width="240px">
        <template slot-scope="scope">
          {{ scope.row.fileNo }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="摘要上链地址" width="190px">
        <template slot-scope="scope">
          {{ scope.row.chainAddress }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="存证时间" width="150px">
        <template slot-scope="scope">
          {{ scope.row.saveTimeStr }}
        </template>
      </el-table-column>
      <el-table-column label="上链状态" width="80px">
        <template slot-scope="scope">
          <div v-if="scope.row.chainStatus == '1'">
            <el-tag size="small">已上链</el-tag>
          </div>
          <div v-else>
            <el-tag type="info" size="small">未上链</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right">
        <template slot-scope="scope">
          <a class="btn-link" @click="handlePreview(scope.$index, scope.row)">查看历史记录</a>
          <span class="individer-line">|</span>
          <a class="btn-link" @click="handleValid(scope.$index, scope.row)">核验</a>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="page" 
        :current-page="currentPage" 
        :page-sizes="[10, 20, 30, 50]" 
        :page-size="pageSize"
        layout=" sizes, prev, pager, next, jumper" 
        :total="total"  
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    ></el-pagination>

    <!-- 证据查看弹框 -->
    <el-dialog title="证据详情信息" :visible.sync="dialogPreviewVisible" :center="true">
      <el-table :data="previewFile">
       <el-table-column align="center" label="序号" width="60px">
        <template slot-scope="scope">
          {{ scope.$index+1 }}
        </template>
       </el-table-column>
       <el-table-column
          property="filePath"
          label="摘要文件地址"
          width="150px"
        ></el-table-column>
        <el-table-column
            property="fileName"
            label="附件名"
            width="100px"
        ></el-table-column>
          <el-table-column
            property="saveTimeStr"
            label="保存时间"
            width="100px"
        ></el-table-column>
        <el-table-column
            property="chainAddress"
            width=""
            label="附件链上地址"
            show-overflow-tooltip
        ></el-table-column>
      </el-table>
    </el-dialog>

    <!-- 证据核验弹框 -->
    <el-dialog title="证据核验信息" :visible.sync="dialogValidVisible" :center="true">
      <div>
        <span>存证数据内容：</span>
        <el-input :value="mainData.dataJson" style="width: 400px" :readonly="true"></el-input>
      </div>
      <div class="input-top">
        <span>存证上链地址：</span>
        <el-input :value="mainData.chainAddress" style="width: 300px" :disabled="true"/>
      </div>
      <div class="input-top">
        <span>链上存证数据hash算法：</span>
        <el-input :value="mainData.hashCal" style="width: 150px" :disabled="true"/>
      </div>
      <div class="input-top">
        <span>链上存证数据hash：</span>
        <el-input :value="mainData.dataHash" style="width: 400px" :readonly="true"/>
      </div>
      <div class="input-top">
        <span>链上存证时间：</span>
        <el-input :value="mainData.saveTime" style="width: 200px" :disabled="true"/>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getEndpointSummaryDataList, validChain, previewChain } from '@/api/table'
import { getUserId } from '@/utils/auth'

export default {
  filters: {
    statusFilter(status) {
      const statusMap = {
        published: 'success',
        draft: 'gray',
        deleted: 'danger'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      currentPage: 1,
      pageSize: 10,
      total: 0,
      list: null,
      listLoading: true,
      previewData: [],
      previewFile: [],
      validFile: [],
      dialogPreviewVisible: false,
      dialogValidVisible: false,
      mainData: {}
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    handleValid(index, row) {
      const reqData = {
        dataId: row.id
      }
      validChain(reqData).then(response => {
        this.dialogValidVisible = true
        this.mainData = response.data
        this.validFile = response.data.fileValidRespList
      })
    },
    handlePreview(index, row) {
      const reqData = {
        dataId: row.id
      }
      previewChain(reqData).then(response => {
        this.dialogPreviewVisible = true
        this.previewData = response.data.dataPreviewRespList
        this.previewFile = response.data.filePreviewRespList
      })
    },
    handleSizeChange: function(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchData()
    },
    handleCurrentChange: function(val) {
      this.currentPage = val
      this.fetchData()
    },
    fetchData() {
      this.listLoading = true
      const reqData = {
        currentPage: this.currentPage,
        pageSize: this.pageSize,
        userId: getUserId()
      }
      getEndpointSummaryDataList(reqData).then(response => {
        this.list = response.data.records
        this.total = response.data.total
        this.listLoading = false
      })
    }
  }
}
</script>
<style lang="scss">
.page {
  padding: 24px 0 24px 0;
  text-align: center;
}

.text-left-label {
  display: inline;
}

.btn-top {
  padding-bottom: 20px;
}

.input-top {
  margin-top: 15px
}

.btn-link {
  color: #2d8cf0;
}

.individer-line {
  padding: 0 10px;
  color: #c2c2c2;
}

.el-divider--horizontal {
  margin: 35px 0;
}

.el-table th {
  background: #f8f8f9;
  color: #515a6e;
}

.sign-box {
  .need-sign-title {
    margin-top: 20px;
  }

  .el-dialog__body {
    padding-bottom: 60px;
    padding-top: 0;
  }
}
</style>
