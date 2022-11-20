package com.wust.endpoint.chain.vo.resp;

import lombok.Data;

import java.util.List;

/**
 * 链上核验返回实体
 * @author xujiao
 * @date 2022-02-25 11:33
 */
@Data
public class ChainValidResp {
    /**数据上链地址*/
    private String chainAddress;
    /**存证数据内容*/
    private String dataJson;
    /**数据hash*/
    private String dataHash;
    /**hash算法*/
    private String hashCal;
    /**存证时间*/
    private String saveTime;
}
