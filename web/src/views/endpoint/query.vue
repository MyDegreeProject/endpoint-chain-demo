<template>
  <el-card>
    <div id="yasgui" />
  </el-card>
</template>

<script>
import Yasgui from '@triply/yasgui'
import '@triply/yasgui/build/yasgui.min.css'
import Vue from 'vue'
export default {
  name: "query",
  data() {
    return {
      yasgui: ''
    }
  },
  methods: {
    initYasguiEditor() {
      this.yasgui = new Yasgui(document.getElementById('yasgui'), {
        requestConfig: {
          ndpoint: 'http://47.108.202.60:9082/endpointChain/endpoint/sparql'
        },
        copyEndpointOnNewTab: false,
      })
    }
  },
  mounted() {
    this.initYasguiEditor()
  }
}
</script>

<style scoped>

</style>