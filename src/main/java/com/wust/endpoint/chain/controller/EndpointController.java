package com.wust.endpoint.chain.controller;

import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.service.EndpointService;
import com.wust.endpoint.chain.vo.req.EndpointReq;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *存证产品管理控制器
 *@author xujiao
 *@date 2022-02-20
 */
@RestController
@RequestMapping("endpointChain/endpoint")
public class EndpointController {

    private final EndpointService endpointService;

    public EndpointController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    /**
     * 端点分页列表接口
     * @author xujiao
     * @date 2022-02-20 12:54
     * @param endpointReq 端点请求实体
     * @return EndpointResponse
     */
    @PostMapping("/list")
    public EndpointResponse list(@RequestBody EndpointReq endpointReq) {
        return endpointService.endpointList(endpointReq);
    }

    /**
     * 端点下拉选择框接口
     * @author xujiao
     * @date 2021-06-27 22:54
     * @return EndpointResponse
     */
    @PostMapping("/selectList")
    public EndpointResponse selectList() {
        return endpointService.selectList();
    }

    /**
     * 端点添加接口
     * @author xujiao
     * @date 2022-02-20 12:54
     * @param endpointReq 产品请求实体
     * @return EndpointResponse
     */
    @PostMapping("/add")
    public EndpointResponse add(@RequestBody EndpointReq endpointReq) {
        return endpointService.addEndpoint(endpointReq);
    }

    /**
     * 端点修改接口
     * @author xujiao
     * @date 2022-02-20 12:54
     * @param endpointReq 端点请求实体
     * @return EndpointResponse
     */
    @PostMapping("/modify")
    public EndpointResponse modify(@RequestBody EndpointReq endpointReq) {
        return endpointService.modifyEndpoint(endpointReq);
    }

    /**
     * 摘要生成接口
     * @author xujiao
     * @date 2022-02-20 12:54
     * @param endpointReq 端点请求实体
     * @return EndpointResponse
     */
    @PostMapping("/generatorSummaries")
    public EndpointResponse generatorSummaries(@RequestBody EndpointReq endpointReq) throws IOException {
        return endpointService.generatorSummaries(endpointReq);
    }

    /**
     * sparql查询接口
     * @author xujiao
     * @date 2022-02-20 12:54
     * @param request sparql查询语句
     * @param response sparql查询响应
     * @return EndpointResponse
     */
    @PostMapping("/sparql")
    public void sparqlQuery(HttpServletRequest request, HttpServletResponse response) throws IOException {
        endpointService.sparqlQuery(request,response);
    }

}


