package com.wust.endpoint.chain.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wust.endpoint.chain.constant.EndpointConstant;
import com.wust.endpoint.chain.persist.entity.FileEntity;
import com.wust.endpoint.chain.persist.mapper.FileMapper;
import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.service.FileService;
import com.wust.endpoint.chain.util.DateUtils;
import com.wust.endpoint.chain.util.PathUtils;
import com.wust.endpoint.chain.util.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 存证附件表 服务实现类
 * @author xujiao
 * @since 2022-02-24
 */
@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileEntity> implements FileService {

    @Value("${store.dir:null}")
    private String storeDir;

    @Override
    public EndpointResponse uploadFile(MultipartFile multipartFile) {
        String attachName = multipartFile.getName();
        try (InputStream inputStream = multipartFile.getInputStream()) {
            String fileNo = SnowflakeUtils.createNo();
            String attachPath = createAttachPath(attachName, fileNo);
            String fullPath = createFullPath(storeDir, attachPath);
            FileUtil.mkParentDirs(fullPath);
            FileUtil.writeFromStream(inputStream, fullPath);
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileNo(fileNo);
            fileEntity.setAttachName(attachName);
            fileEntity.setAttachPath(attachPath);
            this.save(fileEntity);
            return EndpointResponse.ok(fileNo);
        } catch (IOException e) {
            log.warn("上传附件处理出现异常,附件名称={}", attachName, e);
            return EndpointResponse.error("上传附件处理失败");
        }
    }

    @Override
    public FileEntity getByFileNo(String fileNo) {
        LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileEntity::getFileNo, fileNo);
        return this.getOne(wrapper);
    }

    @Override
    public String generateFileHash(String attachPath) {
        //String fullPath = createFullPath(storeDir, attachPath);
        try (FileInputStream fis = new FileInputStream(attachPath)) {
            return DigestUtil.sha256Hex(fis);
        } catch (FileNotFoundException e) {
            log.warn("存证附件不存在,fullPath={}", attachPath, e);
        } catch (IOException e) {
            log.warn("计算存证附件hash出现IO异常,fullPath={}", attachPath, e);
        }
        return null;
    }

    @Override
    public List<FileEntity> listByDataId(Integer dataId) {
        LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileEntity::getDataId, dataId);
        wrapper.orderByDesc(FileEntity::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 拼接附件全路径
     * @author xujiao
     * @date 2022-02-28 16:05
     * @param storeDir 附件根目录
     * @param attachPath 附件子目录
     * @return java.lang.String
     */
    private String createFullPath(String storeDir, String attachPath) {
        if (StringUtils.isBlank(storeDir)) {
            storeDir = System.getProperty("user.home");
        }
        return PathUtils.concat(storeDir, attachPath);
    }

    /**
     * 创建附件路径
     * @author xujiao
     * @date 2022-02-28 16:05
     * @param attachName 附件名称
     * @param fileNo 附件编号
     * @return java.lang.String
     */
    private String createAttachPath(String attachName, String fileNo) {
        String extName = FileUtil.extName(attachName);
        String fileName = fileNo + EndpointConstant.DOT + extName;
        String pathPrefix = DateUtils.dateTimeNow(DateUtils.PATH_FORMAT);
        return PathUtils.concat(pathPrefix, fileName);
    }
}
