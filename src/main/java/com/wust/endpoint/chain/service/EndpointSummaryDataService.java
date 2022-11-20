package com.wust.endpoint.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wust.endpoint.chain.persist.entity.EndpointSummaryDataEntity;
import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.vo.req.DataReq;
import com.wust.endpoint.chain.vo.req.DataSaveReq;

import java.io.IOException;

/**
 * 端点摘要存证主数据表 服务类
 * @author yibi
 * @since 2021-06-24
 */
public interface EndpointSummaryDataService extends IService<EndpointSummaryDataEntity> {
    /**
     * 获取存证数据信息分页列表
     * @author xujiao
     * @date 2022-02-24 14:47
     * @param dataReq
     * @return EndpointResponse
     */
    EndpointResponse dataList(DataReq dataReq);

    /**
     * 处理数据存证实现
     * @author xujiao
     * @date 2022-02-24 14:47
     * @param dataSaveReq 存证数据
     * @return EndpointResponse
     */
    EndpointResponse bqSave(DataSaveReq dataSaveReq) throws IOException;

    /**
     * 存证数据链上核验实现
     * @author xujiao
     * @date 2022-02-24 14:47
     * @param dataId 存证数据id
     * @return EndpointResponse
     */
    EndpointResponse validChain(Integer dataId);

    /**
     * 获取存证上链统计信息实现
     * @author xujiao
     * @date 2022-02-24 14:47
     * @return EndpointResponse
     */
    EndpointResponse getTotalInfo();

    /**
     * 存证数据预览实现
     * @author xujiao
     * @date 2022-02-24 14:47
     * @param dataId 存证数据id
     * @return EndpointResponse
     */
    EndpointResponse previewChain(Integer dataId);
}
