package com.wust.endpoint.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wust.endpoint.chain.persist.entity.EndpointInfoEntity;
import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.vo.req.EndpointReq;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 配置端点表 服务类
 * </p>
 *
 * @author xujiao
 * @since 2022-02-20
 */
public interface EndpointService extends IService<EndpointInfoEntity> {
    /**
     * 获取产品信息分页列表
     * @author xujiao
     * @since 2022-02-20
     * @param endpointReq 端点请求
     * @return EndpointResponse
     */
    EndpointResponse endpointList(EndpointReq endpointReq);

    /**
     * 获取产品信息下拉框列表
     * @author xujiao
     * @since 2022-02-20
     * @return EndpointResponse
     */
    EndpointResponse selectList();

    /**
     * 通过端点id获取名称
     * @author xujiao
     * @since 2022-02-20
     * @param endpointId 端点id
     * @return EndpointInfoEntity
     */
    EndpointInfoEntity getEndpointById(Integer endpointId);

    /**
     * 添加端点信息
     * @author xujiao
     * @since 2022-02-20
     * @param endpointReq 端点请求实体
     * @return EndpointResponse
     */
    EndpointResponse addEndpoint(EndpointReq endpointReq);

    /**
     * 修改端点信息
     * @author xujiao
     * @since 2022-02-20
     * @param EndpointReq 端点请求实体
     * @return EndpointResponse
     */
    EndpointResponse modifyEndpoint(EndpointReq EndpointReq);

    /**
     * 生成摘要信息
     * @param endpointReq
     * @return EndpointResponse
     */
    EndpointResponse generatorSummaries(EndpointReq endpointReq) throws IOException;

    /**
     * sparql查询接口
     * @param request 查询语句
     * @return EndpointResponse
     */
    void sparqlQuery(HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据用户id获取发布的端点列表
     * @param userId
     * @return
     */
    List<Integer> getEndpointIdsByUserId(Integer userId);
}
