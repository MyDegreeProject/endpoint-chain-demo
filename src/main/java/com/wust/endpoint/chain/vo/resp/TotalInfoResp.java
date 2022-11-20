package com.wust.endpoint.chain.vo.resp;

import lombok.Data;

/**
 *存证上链统计信息
 *@author yibi
 *@date 2021-06-25
 */
@Data
public class TotalInfoResp {
    /**端点总数量*/
    private Integer totalEndpointCount;
    /**摘要上链总数量*/
    private Integer totalChainCount;
}
