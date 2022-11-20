package com.wust.endpoint.chain.config;

import cn.hutool.core.lang.Pair;
import cn.hutool.json.JSONUtil;
import com.webank.webase.app.sdk.client.AppClient;
import com.webank.webase.app.sdk.config.HttpConfig;
import com.webank.webase.app.sdk.dto.req.ReqAppRegister;
import com.webank.webase.app.sdk.dto.req.ReqContractAddressSave;
import com.webank.webase.app.sdk.dto.req.ReqContractSourceSave;
import com.webank.webase.app.sdk.dto.req.ReqNewUser;
import com.webank.webase.app.sdk.dto.rsp.RspBasicInfo;
import com.webank.webase.app.sdk.dto.rsp.RspUserInfo;
import com.wust.endpoint.chain.constant.WebaseConstant;
import com.wust.endpoint.chain.contract.Evidence;
import com.wust.endpoint.chain.contract.EvidenceSignersData;
import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.util.DateUtils;
import com.wust.endpoint.chain.util.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * webase客户端
 *@author xujiao
 *@date 2022-02-19
 */
@Slf4j
@Component
public class WebaseClient implements ApplicationListener<ApplicationReadyEvent> {
    /**默认超时时间为30s*/
    private static final Integer DEFAULT_TIMEOUT = 30;

    private final WebaseConfig webaseConfig;
    private final AppClient appClient;

    public WebaseClient(WebaseConfig webaseConfig) {
        this.webaseConfig = webaseConfig;
        HttpConfig httpConfig = new HttpConfig(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
        this.appClient = new AppClient(webaseConfig.getUrl(), webaseConfig.getAppKey(), webaseConfig.getAppSecret(),
                webaseConfig.isTransferEncrypt(), httpConfig);
    }

    /**
     * 查询是否为国密
     * @author xujiao
     * @date 2022-02-19
     * @return boolean
     */
    public boolean isSmMode() {
        RspBasicInfo basicInfo = appClient.basicInfo();
        if (null == basicInfo || WebaseConstant.SM_ENCRYPT_TYPE.equals(basicInfo.getEncryptType())) {
            return true;
        }
        return false;
    }

    /**
     * 节点基本信息
     * @author xujiao
     * @date 2022-02-20 14:23
     * @return boolean
     */
    public RspBasicInfo basicInfo() {
        return appClient.basicInfo();
    }

    /**
     * 应用注册
     * @author xujiao
     * @date 2022-02-20 14:23
     */
    public EndpointResponse appRegister() {
        ReqAppRegister req = new ReqAppRegister();
        String linkUrl = this.webaseConfig.getLinkUrl();
        Pair<String, Integer> hostAndPort = UrlUtils.getHostAndPort(linkUrl);
        if (null == hostAndPort) {
            return EndpointResponse.error("配置应用注册链接不正确，format example:[http://{ip}:{port}/index.html]");
        }
        req.setAppIp(hostAndPort.getKey());
        req.setAppPort(hostAndPort.getValue());
        req.setAppLink(linkUrl);
        this.appClient.appRegister(req);
        return EndpointResponse.ok();
    }

    /**
     * 新增私钥用户
     * @author xujiao
     * @date 2022-02-20 14:23
     * @param userName 用户名称
     * @return com.webank.webase.app.sdk.dto.rsp.RspUserInfo
     */
    public RspUserInfo newUser(String userName) {
        ReqNewUser reqNewUser = new ReqNewUser();
        reqNewUser.setUserName(userName);
        reqNewUser.setAccount(WebaseConstant.ACCOUNT);
        reqNewUser.setGroupId(webaseConfig.getGroupId());
        reqNewUser.setDescription("区块链端点管理人员");
        RspUserInfo userInfo = appClient.newUser(reqNewUser);
        log.info("新增私钥用户={}返回结果为={}", userName, userInfo);
        return userInfo;
    }

    /**
     * 返回群组id
     * @author xujiao
     * @date 2022-02-20 14:23
     * @return java.lang.Integer
     */
    public Integer getGroupId() {
        return webaseConfig.getGroupId();
    }

    /**
     * 合约同步
     * @author xujiao
     * @date 2022-02-20 14:23
     */
    public void contractSourceSave() {
        ReqContractSourceSave reqContractSourceSave = new ReqContractSourceSave();
        reqContractSourceSave.setAccount(WebaseConstant.ACCOUNT);
        reqContractSourceSave.setContractVersion(WebaseConstant.CONTRACT_VERSION);
        List<ReqContractSourceSave.ContractSource> contractList = new ArrayList<>();

        // add EvidenceSignersData contract
        ReqContractSourceSave.ContractSource evidenceFactoryContractSource = new ReqContractSourceSave.ContractSource();
        evidenceFactoryContractSource.setContractName(WebaseConstant.EVIDENCE_CONTRACT_DATA_NAME);
        evidenceFactoryContractSource.setContractSource(EvidenceSignersData.SOURCE_BASE64);
        evidenceFactoryContractSource.setContractAbi(EvidenceSignersData.ABI);
        evidenceFactoryContractSource.setBytecodeBin(EvidenceSignersData.SM_BINARY);

        // add Evidence contract
        ReqContractSourceSave.ContractSource evidenceContractSource = new ReqContractSourceSave.ContractSource();
        evidenceContractSource.setContractName(WebaseConstant.EVIDENCE_CONTRACT_NAME);
        evidenceContractSource.setContractSource(Evidence.SOURCE_BASE64);
        evidenceContractSource.setContractAbi(Evidence.ABI);
        evidenceContractSource.setBytecodeBin(Evidence.SM_BINARY);

        contractList.add(evidenceContractSource);
        contractList.add(evidenceFactoryContractSource);
        reqContractSourceSave.setContractList(contractList);
        log.info("调用WebaseSdk ContractSourceSave接口,请求参数:>>{}", JSONUtil.toJsonStr(reqContractSourceSave));
        appClient.contractSourceSave(reqContractSourceSave);
    }

    /**
     * 合约地址绑定
     * @author xujiao
     * @date 2022-02-20 14:23
     * @param contractAddr 合约地址
     */
    public void contractAddressSave(String contractAddr) {
        ReqContractAddressSave reqContractAddressSave = new ReqContractAddressSave();
        reqContractAddressSave.setGroupId(webaseConfig.getGroupId());
        reqContractAddressSave.setContractName(WebaseConstant.EVIDENCE_CONTRACT_DATA_NAME);
        reqContractAddressSave.setContractPath(WebaseConstant.EVIDENCE_CONTRACT_DATA_NAME + "_" + DateUtils.dateTimeNow());
        reqContractAddressSave.setContractVersion(WebaseConstant.CONTRACT_VERSION);
        reqContractAddressSave.setContractAddress(contractAddr);
        log.info("调用WebaseSdk AddressSave接口,请求参数:>>{}", JSONUtil.toJsonStr(reqContractAddressSave));
        appClient.contractAddressSave(reqContractAddressSave);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // register
        this.appRegister();
    }
}
