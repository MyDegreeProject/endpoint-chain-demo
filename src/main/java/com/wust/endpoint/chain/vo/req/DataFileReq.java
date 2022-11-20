package com.wust.endpoint.chain.vo.req;

import lombok.Data;

/**
 * 存证附件实体
 *@author xujiao
 *@date 2022-02-26
 */
@Data
public class DataFileReq {
    /**存证附件名称*/
    private String fileName;
    /**附件在ipfs上的hash*/
    private String fileHash;
}
