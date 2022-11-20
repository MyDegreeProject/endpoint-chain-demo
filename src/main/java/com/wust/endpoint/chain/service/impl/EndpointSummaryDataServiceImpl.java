package com.wust.endpoint.chain.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.wust.endpoint.chain.enums.ChainStatusEnum;
import com.wust.endpoint.chain.enums.HashCalEnum;
import com.wust.endpoint.chain.persist.entity.EndpointInfoEntity;
import com.wust.endpoint.chain.persist.entity.EndpointSummaryDataEntity;
import com.wust.endpoint.chain.persist.entity.FileEntity;
import com.wust.endpoint.chain.persist.mapper.EndpointSummaryDataMapper;
import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.service.EndpointContractService;
import com.wust.endpoint.chain.service.EndpointService;
import com.wust.endpoint.chain.service.EndpointSummaryDataService;
import com.wust.endpoint.chain.service.FileService;
import com.wust.endpoint.chain.util.DateUtils;
import com.wust.endpoint.chain.vo.req.DataFileReq;
import com.wust.endpoint.chain.vo.req.DataReq;
import com.wust.endpoint.chain.vo.req.DataSaveReq;
import com.wust.endpoint.chain.vo.resp.*;
import io.ipfs.api.IPFS;
import io.ipfs.multiaddr.MultiAddress;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 端点摘要存证主数据表 服务实现类
 * @author xujiao
 * @date 2022-02-25 11:09
 */
@Slf4j
@Service
public class EndpointSummaryDataServiceImpl extends ServiceImpl<EndpointSummaryDataMapper, EndpointSummaryDataEntity> implements EndpointSummaryDataService {
    @Resource
    private EndpointService endpointService;
    @Resource
    private FileService fileService;
    @Resource
    private EndpointContractService contractService;
    @Resource
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public EndpointResponse dataList(DataReq dataReq) {
        //根据用户获取端点列表集合
        List<Integer> endpointIds = endpointService.getEndpointIdsByUserId(dataReq.getUserId());
        LambdaQueryWrapper<EndpointSummaryDataEntity> wrapper = new LambdaQueryWrapper<>();
        if(!CollectionUtils.isEmpty(endpointIds) && endpointIds.size() > 0){
            wrapper.in(EndpointSummaryDataEntity::getEndpointId,endpointIds);
        }
        wrapper.orderByDesc(EndpointSummaryDataEntity::getSaveTime);
        IPage<EndpointSummaryDataEntity> iPage = new Page<>(dataReq.getCurrentPage(), dataReq.getPageSize());
        IPage<EndpointSummaryDataEntity> dataMainPage = this.page(iPage,wrapper);
        IPage<DataMainResp> dataMainRespPage = convertDataPage(dataMainPage);
        return EndpointResponse.ok(dataMainRespPage);
    }

    @Override
    @Transactional
    public EndpointResponse bqSave(DataSaveReq dataSaveReq) throws IOException {
        /*IPFS ipfs = new IPFS(new MultiAddress("/ip4/47.108.202.60/tcp/9091"));
        Multihash filePointer = Multihash.fromBase58(dataSaveReq.getDataFileReq().getFileHash());
        byte[] data = ipfs.cat(filePointer);
        String dataContent = new String(data);*/
        String dataMap = dataSaveReq.getFilePath();
        String dataContent = JSONUtil.toJsonStr(dataMap);
        LambdaQueryWrapper<EndpointSummaryDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EndpointSummaryDataEntity::getEndpointId, dataSaveReq.getEndpointId());
        EndpointSummaryDataEntity dataMainEntity = this.baseMapper.selectOne(wrapper);
        if (ObjectUtils.isEmpty(dataMainEntity)) {
            dataMainEntity = new EndpointSummaryDataEntity();
            dataMainEntity.setEndpointId(dataSaveReq.getEndpointId());
            dataMainEntity.setDataJson(dataContent);
            dataMainEntity.setChainStatus(ChainStatusEnum.NO.code);
            boolean flag = this.save(dataMainEntity);
            if (!flag) {
                return EndpointResponse.error("存证处理失败");
            }
            log.info("存证处理成功，开始进行摘要数据上链,存证数据id={}", dataMainEntity.getId());
        }
        //回写摘要文件上链信息
        DataFileReq dataFileReq = dataSaveReq.getDataFileReq();
        if (ObjectUtil.isEmpty(dataFileReq)) {
            log.info("存证附件为空,无需处理");
            return EndpointResponse.ok();
        }
        handleDataFile(dataMainEntity, dataFileReq);
        return EndpointResponse.ok();
    }

    @Override
    public EndpointResponse validChain(Integer dataId) {
        EndpointSummaryDataEntity dataMainEntity = this.getById(dataId);
        if (ObjectUtil.isNull(dataMainEntity) || StringUtils.isBlank(dataMainEntity.getChainAddress())) {
            log.info("待核验的数据不存在或未上链,dataId={}", dataId);
            return EndpointResponse.error("待核验的数据不存在或未上链");
        }
        String chainAddress = dataMainEntity.getChainAddress();
        String chainResponse = contractService.searchChain(chainAddress);
        if (StringUtils.isBlank(chainResponse)) {
            log.info("核验数据返回链上数据为空,dataId={},chainAddress={}", dataId, chainAddress);
            return EndpointResponse.error("核验数据返回链上数据为空");
        }
        ChainValidResp validResp = new ChainValidResp();
        ChainQueryData queryResp = parseChainData(chainResponse);
        validResp.setDataHash(queryResp.getDataHash());
        validResp.setHashCal(queryResp.getHashCal());
        validResp.setSaveTime(queryResp.getSaveTime());
        validResp.setChainAddress(chainAddress);
        validResp.setDataJson(dataMainEntity.getDataJson());
        return EndpointResponse.ok(validResp);
    }

    @Override
    public EndpointResponse getTotalInfo() {
        TotalInfoResp totalInfoResp = new TotalInfoResp();
        Integer totalEndpointCount = endpointService.list().size();
        LambdaQueryWrapper<EndpointSummaryDataEntity> dataQw = new LambdaQueryWrapper<>();
        dataQw.eq(EndpointSummaryDataEntity::getChainStatus, ChainStatusEnum.YES.code);
        Integer totalChainCount = this.count(dataQw);
        totalInfoResp.setTotalChainCount(totalChainCount);
        totalInfoResp.setTotalEndpointCount(totalEndpointCount);
        return EndpointResponse.ok(totalInfoResp);
    }

    @Override
    public EndpointResponse previewChain(Integer dataId) {
        EndpointSummaryDataEntity dataMainEntity = this.getById(dataId);
        if (ObjectUtil.isNull(dataMainEntity)) {
            log.info("预览的存证数据不存在,dataId={}", dataId);
            return EndpointResponse.error("预览的存证数据不存在");
        }
        DataAndFilePreviewResp dataAndFilePreviewResp = new DataAndFilePreviewResp();
        List<FileEntity> attachEntityList = fileService.listByDataId(dataId);
        List<FilePreviewResp> filePreviewRespList = Lists.newArrayListWithCapacity(attachEntityList.size());
        attachEntityList.forEach((attachEntity) -> {
            FilePreviewResp filePreviewResp = new FilePreviewResp();
            filePreviewResp.setFileName(attachEntity.getAttachName());
            filePreviewResp.setFilePath(attachEntity.getAttachPath());
            filePreviewResp.setSaveTimeStr(DateUtils.formatDateTime(attachEntity.getCreateTime()));
            filePreviewResp.setChainAddress(attachEntity.getChainAddress());
            filePreviewRespList.add(filePreviewResp);
        });
        dataAndFilePreviewResp.setFilePreviewRespList(filePreviewRespList);
        return EndpointResponse.ok(dataAndFilePreviewResp);
    }

    private void handleDataFile(EndpointSummaryDataEntity dataMainEntity, DataFileReq dataFileReq) {
        String fileHash = dataFileReq.getFileHash();
        FileEntity attachEntity = fileService.getByFileNo(dataFileReq.getFileHash());
        String attachHash = fileService.generateFileHash(attachEntity.getAttachPath());
        if (StringUtils.isBlank(attachHash)) {
            return;
        }
        LocalDateTime nowTime = LocalDateTime.now();
        String saveTime = DateUtils.formatDateTime(nowTime);
        String hashCal = HashCalEnum.SHA256.type;
        attachEntity.setHashCal(hashCal);
        attachEntity.setDataId(dataMainEntity.getId());
        attachEntity.setAttachHash(attachHash);
        String dataAddress = contractService.uploadChain(attachHash, hashCal, saveTime);
        if (StringUtils.isBlank(dataAddress)) {
            attachEntity.setChainStatus(ChainStatusEnum.NO.code);
            log.error("附件上链失败,返回数据上链地址为空,附件编号={}", dataFileReq.getFileHash());
        } else {
            attachEntity.setChainStatus(ChainStatusEnum.YES.code);
            attachEntity.setChainAddress(dataAddress);
            dataMainEntity.setFileNo(fileHash);
            dataMainEntity.setChainStatus(ChainStatusEnum.YES.code);
            dataMainEntity.setChainAddress(dataAddress);
            dataMainEntity.setSaveTime(nowTime);
            this.updateById(dataMainEntity);
        }
        fileService.updateById(attachEntity);
    }

    /**存证数据分页转换*/
    private IPage<DataMainResp> convertDataPage(IPage<EndpointSummaryDataEntity> dataMainPage) {
        IPage<DataMainResp> dataMainRespPage = new Page<>(dataMainPage.getCurrent(), dataMainPage.getSize());
        List<EndpointSummaryDataEntity> dataMainEntityList = dataMainPage.getRecords();
        List<DataMainResp> dataMainRespList = Lists.newArrayListWithCapacity(dataMainEntityList.size());
        dataMainEntityList.forEach((dataMainEntity) -> {
            DataMainResp mainResp = new DataMainResp();
            BeanUtils.copyProperties(dataMainEntity, mainResp);
            EndpointInfoEntity endpointInfoEntity = endpointService.getEndpointById(dataMainEntity.getEndpointId());
            mainResp.setEndpointUrl(endpointInfoEntity.getEndpointUrl());
            mainResp.setEndpointName(endpointInfoEntity.getEndpointName());
            mainResp.setSaveTimeStr(DateUtils.formatDateTime(dataMainEntity.getSaveTime()));
            //获取链上地址和摘要文件路径
            FileEntity attachEntity = fileService.getByFileNo(dataMainEntity.getFileNo());
            mainResp.setFileNo(attachEntity.getFileNo());
            dataMainRespList.add(mainResp);
        });
        dataMainRespPage.setRecords(dataMainRespList);
        dataMainRespPage.setTotal(dataMainPage.getTotal());
        return dataMainRespPage;
    }

    /**解析上链返回结果*/
    private ChainQueryData parseChainData(String chainResponse) {
        JSONArray jsonArray = JSONUtil.parseArray(chainResponse);
        String dataHash = jsonArray.get(0).toString();
        String hashCal = jsonArray.get(1).toString();
        String saveTime = jsonArray.get(2).toString();
        return new ChainQueryData(dataHash, hashCal, saveTime);
    }
}
