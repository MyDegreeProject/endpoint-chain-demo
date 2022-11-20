package com.wust.endpoint.chain.vo.req;

import lombok.Data;

/**
 *数据存证校验请求
 *@author xujiao
 *@date 2022-02-24
 */
@Data
public class DataValidReq {
    /**端点摘要存证数据id*/
    private Integer dataId;
}
