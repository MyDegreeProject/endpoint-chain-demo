package com.wust.endpoint.chain.vo.req;

import lombok.Data;

/**
 * 分页信息
 * @author xujiao
 * @date 2022-02-20 12:14
 */
@Data
public class EndpointPage {
    /**每页显示数，默认10*/
    private Integer pageSize = 10;
    /**当前页，默认1*/
    private Integer currentPage = 1;
}
