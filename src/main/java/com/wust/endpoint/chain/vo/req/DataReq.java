package com.wust.endpoint.chain.vo.req;

import lombok.Data;

/**
 *查询存证数据请求
 *@author xujiao
 *@date 2022-02-24
 */
@Data
public class DataReq extends EndpointPage {
    /**用户id*/
    private Integer userId;
}
