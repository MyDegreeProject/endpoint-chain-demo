package com.wust.endpoint.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wust.endpoint.chain.persist.entity.FileEntity;
import com.wust.endpoint.chain.response.EndpointResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 存证附件表 服务类
 * @author xujiao
 * @since 2022-02-24
 */
public interface FileService extends IService<FileEntity> {
    /**
     * 文件上传实现
     * @author xujiao
     * @since 2022-02-24
     * @param multipartFile 上传文件
     * @return EndpointResponse
     */
    EndpointResponse uploadFile(MultipartFile multipartFile);

    /**
     * 根据附件编号获取附件实体
     * @author xujiao
     * @since 2022-02-24
     * @param fileNo 附件编号
     * @return FileEntity
     */
    FileEntity getByFileNo(String fileNo);

    /**
     * 生成附件hash
     * @author xujiao
     * @since 2022-02-24
     * @param attachPath 附件路径
     * @return java.lang.String
     */
    String generateFileHash(String attachPath);

    /**
     * 根据存证数据id查询附件列表
     * @author xujiao
     * @since 2022-02-24
     * @param dataId 存证数据id
     * @return FileEntity
     */
    List<FileEntity> listByDataId(Integer dataId);
}
