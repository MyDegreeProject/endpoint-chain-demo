package com.wust.endpoint.chain.controller;

import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.service.EndpointSummaryDataService;
import com.wust.endpoint.chain.vo.req.DataReq;
import com.wust.endpoint.chain.vo.req.DataSaveReq;
import com.wust.endpoint.chain.vo.req.DataValidReq;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 *存证数据管理控制器
 *@author xujiao
 *@date 2022-02-24
 */
@RestController
@RequestMapping("endpointChain/data")
public class EndpointSummaryDataController {

    private final EndpointSummaryDataService dataService;

    public EndpointSummaryDataController(EndpointSummaryDataService dataService) {
        this.dataService = dataService;
    }

    /**
     * 存证数据列表接口
     * @author xujiao
     * @date 2022-02-24
     * @param dataReq 存证数据查询请求
     * @return EndpointResponse
     */
    @PostMapping("/list")
    public EndpointResponse list(@RequestBody DataReq dataReq) {
        return dataService.dataList(dataReq);
    }

    /**
     * 模拟数据存证接口
     * @author yibi
     * @date 2021-06-25 10:51
     * @param dataSaveReq 存证数据
     * @return EndpointResponse
     */
    @PostMapping("/bqSave")
    public EndpointResponse bqSave(@RequestBody DataSaveReq dataSaveReq) throws IOException {
        return dataService.bqSave(dataSaveReq);
    }

    /**
     * 存证数据链上校验接口
     * @author yibi
     * @date 2021-06-25 11:26
     * @param validReq 存证校验请求
     * @return EndpointResponse
     */
    @PostMapping("/validChain")
    public EndpointResponse validChain(@RequestBody DataValidReq validReq) {
        return dataService.validChain(validReq.getDataId());
    }

    /**
     * 存证数据预览接口
     * @author yibi
     * @date 2021-06-25 11:26
     * @param validReq 存证校验请求
     * @return EndpointResponse
     */
    @PostMapping("/previewChain")
    public EndpointResponse previewChain(@RequestBody DataValidReq validReq) {
        return dataService.previewChain(validReq.getDataId());
    }

    /**
     * 获取存证上链统计信息接口
     * @author yibi
     * @date 2021-06-25 16:07
     * @return EndpointResponse
     */
    @PostMapping("/getTotalInfo")
    public EndpointResponse getTotalInfo() {
        return dataService.getTotalInfo();
    }
}
