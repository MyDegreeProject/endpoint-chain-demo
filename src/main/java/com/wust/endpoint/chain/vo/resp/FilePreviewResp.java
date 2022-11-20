package com.wust.endpoint.chain.vo.resp;

import lombok.Data;

/**
 *摘要存证数据预览实体
 *@author xujiao
 *@date 2022-02-27
 */
@Data
public class FilePreviewResp {
    /**存证附件名称*/
    private String fileName;
    /**摘要地址*/
    private String filePath;
    /**存证保存时间*/
    private String saveTimeStr;
    /**存证附件上链地址*/
    private String chainAddress;
}
