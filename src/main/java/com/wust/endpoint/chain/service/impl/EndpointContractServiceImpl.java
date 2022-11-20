package com.wust.endpoint.chain.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wust.endpoint.chain.bo.TransDataRespBO;
import com.wust.endpoint.chain.bo.TransHandleReqBO;
import com.wust.endpoint.chain.bo.TransQueryReqBO;
import com.wust.endpoint.chain.config.WebaseClient;
import com.wust.endpoint.chain.constant.WebaseConstant;
import com.wust.endpoint.chain.contract.Evidence;
import com.wust.endpoint.chain.contract.EvidenceSignersData;
import com.wust.endpoint.chain.persist.entity.EndpointContractEntity;
import com.wust.endpoint.chain.persist.entity.EndpointUserEntity;
import com.wust.endpoint.chain.persist.mapper.EndpointContractMapper;
import com.wust.endpoint.chain.service.EndpointContractService;
import com.wust.endpoint.chain.util.HttpUtils;
import com.wust.endpoint.chain.util.WebaseUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.crypto.Sign;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 存证合约表 服务实现类
 * @author xujiao
 * @since 2022-02-20
 */
@Slf4j
@Service
public class EndpointContractServiceImpl extends ServiceImpl<EndpointContractMapper, EndpointContractEntity> implements EndpointContractService {

    @Value("${webase-front.trans.handle.url}")
    private String webaseFrontTransHandleUrl;
    @Value("${webase-front.trans.query.url}")
    private String webaseFrontTransQueryUrl;
    @Value("${webase-front.trans.encodeFunction.url}")
    private String webaseFrontEncodeFunctionUrl;
    @Resource
    private WebaseClient webaseClient;

    @Override
    public void insertContract(String contractName, EndpointUserEntity userEntity, String contractAddress) {
        EndpointContractEntity contractEntity = new EndpointContractEntity();
        contractEntity.setContractName(contractName);
        contractEntity.setContractAddress(contractAddress);
        contractEntity.setSignUserId(userEntity.getSignUserId());
        contractEntity.setPrivateKey(userEntity.getPrivateKey());
        contractEntity.setPublicKey(userEntity.getPublicKey());
        this.save(contractEntity);
    }

    @Override
    public String uploadChain(String dataHash, String hashCal, String saveTime) {
        EndpointContractEntity contractEntity = getEndpointContract();
        if (ObjectUtil.isNull(contractEntity)) {
            log.error("还未部署端点摘要存证合约,无法进行数据上链");
            return null;
        }
        TransHandleReqBO transHandleReqBO = new TransHandleReqBO();
        JSONArray parseArray = JSONUtil.parseArray(EvidenceSignersData.ABI);
        List<Object> abiList = JSONUtil.toList(parseArray, Object.class);
        transHandleReqBO.setContractAbi(abiList);
        transHandleReqBO.setContractAddress(contractEntity.getContractAddress());
        transHandleReqBO.setContractName(contractEntity.getContractName());
        transHandleReqBO.setFuncName(EvidenceSignersData.FUNC_NEWEVIDENCE);
        List<Object> params = getList(dataHash, hashCal, saveTime, contractEntity);
        if (params == null) {
            return null;
        }
        transHandleReqBO.setFuncParam(params);
        transHandleReqBO.setGroupId(webaseClient.getGroupId());
        transHandleReqBO.setUseCns(false);
        transHandleReqBO.setSignUserId(contractEntity.getSignUserId());
        log.info("调用webase-front接口,url>>{},请求参数:>>{}", webaseFrontTransHandleUrl, JSONUtil.toJsonStr(transHandleReqBO));
        String resp = HttpUtils.httpPostByJson(webaseFrontTransHandleUrl, JSONUtil.toJsonStr(transHandleReqBO));
        log.info("调用webase-front接口,响应结果reslut:>>{}", resp);
        TransDataRespBO resBO = JSONUtil.toBean(resp, TransDataRespBO.class);
        if (ObjectUtil.isNull(resBO) || !resBO.isStatusOK()) {
            log.error("端点数据上链返回失败");
            return null;
        }
        return resBO.getLogs().get(0).getAddress();
    }

    @Override
    public String searchChain(String dataAddress) {
        EndpointContractEntity contractEntity = getEndpointContract();
        if (ObjectUtil.isNull(contractEntity)) {
            log.error("还未部署存证合约,无法进行上链数据查询");
            return null;
        }
        TransQueryReqBO transQueryReqBO = new TransQueryReqBO();
        transQueryReqBO.setUserAddress(contractEntity.getPublicKey());
        transQueryReqBO.setEncodeStr(getEncodeStr());
        transQueryReqBO.setContractAbi(Evidence.ABI);
        transQueryReqBO.setContractAddress(dataAddress);
        transQueryReqBO.setFuncName(Evidence.FUNC_GETEVIDENCE);
        transQueryReqBO.setGroupId(webaseClient.getGroupId());
        log.info("调用webase-front接口,url>>{},请求参数:>>{}", webaseFrontTransQueryUrl, JSONUtil.toJsonStr(transQueryReqBO));
        String resp = HttpUtils.httpPostByJson(webaseFrontTransQueryUrl, JSONUtil.toJsonStr(transQueryReqBO));
        log.info("调用webase-front接口,响应结果reslut:>>{}", resp);
        return resp;
    }

    /**上链合约参数组装*/
    @Nullable
    private List<Object> getList(String dataHash, String hashCal, String saveTime, EndpointContractEntity contractEntity) {
        Sign.SignatureData signatureData = WebaseUtils.signMessage(dataHash, contractEntity.getPrivateKey());
        if (ObjectUtil.isNull(signatureData)) {
            log.error("数据hash签名失败,无法进行数据上链");
            return null;
        }
        List<Object> params = new ArrayList<>();
        params.add(dataHash);
        params.add(hashCal);
        params.add(saveTime);
        params.add(signatureData.getV());
        params.add(WebaseUtils.bytesToHexString(signatureData.getR()));
        params.add(WebaseUtils.bytesToHexString(signatureData.getS()));
        return params;
    }

    /*获取合约函数编码值encodeStr*/

    /**
     * 获取合约函数编码值
     *  @author xujiao
     *  @date 2022-02-20 18:08
     *  @return String 已编码函数值
     */
    private String getEncodeStr(){
        TransHandleReqBO encodeFunctionBo=new TransHandleReqBO();
        JSONArray parseArray = JSONUtil.parseArray(Evidence.ABI);
        List abiList = JSONUtil.toList(parseArray, Object.class);
        encodeFunctionBo.setContractAbi(abiList);
        encodeFunctionBo.setFuncName(Evidence.FUNC_GETEVIDENCE);
        List params =new ArrayList<>();
        encodeFunctionBo.setFuncParam(params);
        log.info("调用webase-front接口,url>>{},请求参数:>>{}", webaseFrontEncodeFunctionUrl, JSONUtil.toJsonStr(encodeFunctionBo));
        String encodeStr = HttpUtils.httpPostByJson(webaseFrontEncodeFunctionUrl, JSONUtil.toJsonStr(encodeFunctionBo));
        log.info("调用webase-front接口,响应结果reslut:>>{}", encodeStr);
        return encodeStr;
    }

    /**
     * 获取端点合约
     * @author xujiao
     * @date 2022-02-20 18:08
     * @return EndpointContractEntity
     */
    @Override
    public EndpointContractEntity getEndpointContract() {
        List<EndpointContractEntity> contractEntities = this.list();
        if (CollectionUtils.isEmpty(contractEntities)) {
            return null;
        }
        return contractEntities.get(0);
    }
}
