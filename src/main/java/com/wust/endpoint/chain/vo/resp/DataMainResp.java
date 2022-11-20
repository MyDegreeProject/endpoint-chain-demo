package com.wust.endpoint.chain.vo.resp;

import lombok.Data;

/**
 *存证数据返回内容
 *@author xujiao
 *@date 2022-02-26
 */
@Data
public class DataMainResp {

    private Integer id;

    /**
     * 端点名称
     */
    private String endpointUrl;

    /**
     * 端点名称
     */
    private String endpointName;

    /**
     * 存证时间
     */
    private String saveTimeStr;

    /**
     * 上链状态
     */
    private Integer chainStatus;

    /**
     * 上链地址
     */
    private String chainAddress;

    /**
     * ipfs上摘要文件cid
     */
    private String fileNo;
}
