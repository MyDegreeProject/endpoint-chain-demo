package com.wust.endpoint.chain.vo.req;

import lombok.Data;
import java.util.List;

/**
 *端点请求
 *@author xujiao
 *@date 2022-02-20
 */
@Data
public class EndpointReq extends EndpointPage {
    /**端点id*/
    private Integer id;
    /**用户id*/
    private Integer userId;
    /**端点地址*/
    private String endpointUrl;
    /**端点名称*/
    private String endpointName;
    /**端点地址集合*/
    private List<String> endpointUrlList;
}
