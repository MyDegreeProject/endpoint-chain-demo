<template>
  <div class="app-container">
    <div class="btn-top">
      <el-button @click="createEndpoint" type="primary" plain>新建</el-button>
      <div class="text-left-label" style="margin-left: 20px">
        <span>端点名称：</span>
        <el-input v-model="endpointName" style="width: 150px"/>
      </div>
      <el-button @click="search" type="primary" plain style="margin-left: 10px">查询</el-button>
    </div>
    <el-table
        v-loading="listLoading"
        :data="list"
        element-loading-text="Loading"
    >
      <el-table-column align="center" label="序号">
        <template slot-scope="scope">
          {{ scope.$index +1}}
        </template>
      </el-table-column>
      <el-table-column label="端点地址" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ scope.row.endpointUrl }}
        </template>
      </el-table-column>
      <el-table-column label="端点名称" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ scope.row.endpointName }}
        </template>
      </el-table-column>
       <el-table-column label="所属组织名称" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ scope.row.orgName }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center">
        <template slot-scope="scope">
          <div v-if="scope.row.openStatus == '1'">
            <el-tag size="small">正常</el-tag>
          </div>
          <div v-else>
            <el-tag type="info" size="small">停用</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right">
        <template slot-scope="scope">
          <a class="btn-link" @click="handleModify(scope.$index, scope.row)" style="padding-right: 10px">修改</a>
          <a class="btn-link" @click="handleGeneratorSummary(scope.row)" style="padding-right: 10px">生成摘要</a>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="page" @size-change="handleSizeChange" @current-change="handleCurrentChange"
                   :current-page="currentPage" :page-sizes="[10, 20, 30, 50]" :page-size="pageSize"
                   layout=" sizes, prev, pager, next, jumper" :total="total"
    >
    </el-pagination>

    <!--初始化 -->
    <el-dialog title="初始化" :visible.sync="dialogInitVisible" :before-close="modelClose" class="dialog-wrapper"
               width="433px" :center="true" :show-close="false"
    >
      <div>
        <span>是否使用国密：</span>
        <el-input v-model="chainInfo.encryptTypeStr" :disabled="true" style="width:200px"/>
      </div>
      <div style="margin-top: 10px">
        <span>FISCO-BCOS：</span>
        <el-input v-model="chainInfo.fiscoBcosVersion" :disabled="true" style="width: 200px"/>
      </div>
      <div style="margin-top: 10px">
        <span>WeBASE版本：</span>
        <el-input v-model="chainInfo.webaseVersion" :disabled="true" style="width: 200px"/>
      </div>
      <div style="text-align: center">
        <el-button type="primary" @click="deployContract" class="input-top">部署端点合约</el-button>
      </div>
    </el-dialog>

    <!-- 端点添加改弹框 -->
    <el-dialog title="添加端点" :visible.sync="dialogAddVisible" width="433px" :center="true">
      <div>
        <span>端点地址：</span>
        <el-input v-model="endpointAddData.endpointUrl" style="width: 300px"/>
      </div>
      <div>
        <span>产品名称：</span>
        <el-input v-model="endpointAddData.endpointName" style="width: 300px"/>
      </div>
      <div style="text-align: center">
        <el-button type="primary" @click="sumbitAdd" class="input-top">提交</el-button>
      </div>
    </el-dialog>

    <!-- 端点修改改弹框 -->
    <el-dialog title="修改端点" :visible.sync="dialogModifyVisible" width="433px" :center="true">
      <div>
        <span>端点地址：</span>
        <el-input v-model="endpointModifyData.endpointUrl" style="width: 300px"/>
      </div>
      <div>
        <span>端点名称：</span>
        <el-input v-model="endpointModifyData.endpointName" style="width: 300px"/>
      </div>
      <div style="text-align: center">
        <el-button type="primary" @click="sumbitModify" class="input-top">提交</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { addEndpoint, deployContract, getChainInfo, getEndpointList, modifyEndpoint,generatorSummaries } from '@/api/table'
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
      dialogAddVisible: false,
      dialogModifyVisible: false,
      dialogInitVisible: false,
      chainInfo: {
        initStatus: false,
        encryptType: '',
        encryptTypeStr: '',
        fiscoBcosVersion: '',
        webaseVersion: ''
      },
      endpointAddData: {
        userId : getUserId(),
        endpointUrl: '',
        endpointName: ''
      },
      endpointModifyData: {
        id: '',
        userId : getUserId(),
        endpointUrl: '',
        endpointName: ''
      },
      endpointName: ''
    }
  },
  created() {
    getChainInfo().then(response => {
      this.chainInfo = response.data
      if (this.chainInfo.initStatus) {
        this.dialogInitVisible = false
      } else {
        this.dialogInitVisible = true
      }
      if (this.chainInfo.encryptType === 1) {
        this.chainInfo.encryptTypeStr = '国密'
      } else {
        this.chainInfo.encryptTypeStr = '非国密'
      }
    })
    this.fetchData()
  },
  methods: {
    createEndpoint() {
      this.dialogAddVisible = true
    },
    deployContract: function() {
      const userId = getUserId()
      let reqData = {
        userId: userId
      }
      deployContract(reqData).then(response => {
        if (response.code === 0) {
          this.$message({
            message: '合约部署成功',
            type: 'success'
          })
          this.dialogInitVisible = false
        }
      })
    },
    handleModify(index, row) {
      this.dialogModifyVisible = true
      this.endpointModifyData.endpointUrl = row.endpointUrl
      this.endpointModifyData.endpointName = row.endpointName
      this.endpointModifyData.id = row.id
    },
    sumbitAdd: function() {
      if (this.endpointAddData.endpointUrl === '') {
        this.$message({
          message: '端点地址不能为空',
          type: 'warning'
        })
        return false
      }else if (this.endpointAddData.endpointName === '') {
        this.$message({
          message: '端点名称不能为空',
          type: 'warning'
        })
        return false
      }
      addEndpoint(this.endpointAddData).then(response => {
        if (response.code === 0) {
          this.dialogAddVisible = false
          this.fetchData()
        }
      })
    },
    handleGeneratorSummary: function(row){
      const generatorSummariesData ={
         id: row.id,
         endpointUrl: row.endpointUrl,
         endpointName: row.endpointName
      }
      generatorSummaries(generatorSummariesData).then(response => {  
        if (response.code === 0) {
          return  this.$message({
            message: '生成摘要成功',
            type: 'success'
          })
        }
      })
    },
    sumbitModify: function() {
      if (this.endpointModifyData.endpointUrl === '') {
        this.$message({
          message: '端点地址不能为空',
          type: 'warning'
        })
        return false
      }else if (this.endpointModifyData.endpointName === '') {
        this.$message({
          message: '端点名称不能为空',
          type: 'warning'
        })
        return false
      }
      modifyEndpoint(this.endpointModifyData).then(response => {
        if (response.code === 0) {
          this.dialogModifyVisible = false
          this.fetchData()
        }
      })
    },
    search: function() {
      this.currentPage = 1
      this.fetchData()
    },
    clearText: function() {
      this.fetchData()
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
      let reqData = {
        currentPage: this.currentPage,
        pageSize: this.pageSize,
        userId: getUserId(),
        endpointUrl: this.endpointUrl,
        endpointName: this.endpointName
      }
      getEndpointList(reqData).then(response => {
        this.list = response.data.records
        this.total = response.data.total
        this.listLoading = false
      })
    },
    modelClose: function() {
      this.$emit('close')
    }
  }
}
</script>
<style lang="scss">
.page {
  padding: 24px 0 24px 0;
  text-align: center;
}

.search-part-right {
  float: right;
  padding-right: 40px
}

.btn-top {
  padding-bottom: 20px;
  display: flex;
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
