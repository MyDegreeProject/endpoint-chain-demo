package com.wust.endpoint.chain.vo.req;

import lombok.Data;

import java.util.List;

/**
 *端点数据存证请求
 *@author xujiao
 *@date 2022-02-24
 */
@Data
public class DataSaveReq {
    /**端点id*/
    private Integer endpointId;
    /**摘要路径*/
    private String filePath;
    /**存证附件数据*/
    private DataFileReq dataFileReq;
}
