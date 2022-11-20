package com.wust.endpoint.chain.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.webase.app.sdk.dto.rsp.RspBasicInfo;
import com.webank.webase.app.sdk.dto.rsp.RspUserInfo;
import com.wust.endpoint.chain.bo.ContractDeployReqBO;
import com.wust.endpoint.chain.config.WebaseClient;
import com.wust.endpoint.chain.constant.WebaseConstant;
import com.wust.endpoint.chain.contract.EvidenceSignersData;
import com.wust.endpoint.chain.persist.entity.EndpointContractEntity;
import com.wust.endpoint.chain.persist.entity.EndpointUserEntity;
import com.wust.endpoint.chain.persist.mapper.EndpointUserMapper;
import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.service.EndpointContractService;
import com.wust.endpoint.chain.service.EndpointUserService;
import com.wust.endpoint.chain.util.DateUtils;
import com.wust.endpoint.chain.util.HttpUtils;
import com.wust.endpoint.chain.vo.req.LoginReq;
import com.wust.endpoint.chain.vo.req.RegisterReq;
import com.wust.endpoint.chain.vo.resp.ChainInfoResp;
import com.wust.endpoint.chain.vo.resp.LoginResp;
import com.wust.endpoint.chain.vo.resp.UserAndChainResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 管理用户表 服务实现类
 * </p>
 *
 * @author yibi
 * @since 2021-06-24
 */
@Slf4j
@Service
public class EndpointUserServiceImpl extends ServiceImpl<EndpointUserMapper, EndpointUserEntity> implements EndpointUserService {

    @Resource
    private WebaseClient webaseClient;
    @Resource
    private EndpointContractService contractService;

    @Value("${webase-front.contract.deploy.url}")
    private String webaseFrontContractDeployUrl;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public EndpointResponse register(RegisterReq registerReq){
        // 1.先落地user
        EndpointUserEntity userEntity = new EndpointUserEntity();
        userEntity.setCreateTime(LocalDateTime.now());
        userEntity.setUname(registerReq.getUsername());
        userEntity.setOrgName(registerReq.getOrgName());
        userEntity.setOrgDescription(registerReq.getOrgDescription());
        // 密码加密
        userEntity.setPwd(DigestUtils.sha256Hex(registerReq.getPassword()));
        this.getBaseMapper().insert(userEntity);

        // 调用WeBASE-APP-SDK，进行webase用户注册
        this.registerWb(userEntity.getId());
        return EndpointResponse.ok();
    }

    @Override
    public EndpointResponse login(LoginReq loginReq) {
        LambdaQueryWrapper<EndpointUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EndpointUserEntity::getUname, loginReq.getUsername());
        EndpointUserEntity userEntity = getOne(wrapper);
        if (ObjectUtil.isNull(userEntity)) {
            return EndpointResponse.error("用户名或密码不正确");
        }
        String encryptedPassword = DigestUtils.sha256Hex(loginReq.getEncryptedPassword());
        if (!userEntity.getPwd().equals(encryptedPassword)) {
            return EndpointResponse.error("用户名或密码不正确");
        }
        LoginResp loginResp = new LoginResp();
        loginResp.setUserId(userEntity.getId());
        return EndpointResponse.ok(loginResp);
    }


    @Override
    @Transactional
    public EndpointResponse deployContract(Integer userId) {
        EndpointUserEntity userEntity = registerWb(userId);
        if (ObjectUtil.isNull(userEntity)) {
            return EndpointResponse.error("端点存证合约部署失败");
        }
        ContractDeployReqBO contractDeployReqBO = new ContractDeployReqBO();
        String contractName = WebaseConstant.EVIDENCE_CONTRACT_NAME + "_" + DateUtils.dateTimeNow();
        JSONArray parseArray = JSONUtil.parseArray(EvidenceSignersData.ABI);
        List<Object> abiList = JSONUtil.toList(parseArray, Object.class);
        contractDeployReqBO.setGroupId(webaseClient.getGroupId());
        contractDeployReqBO.setAbiInfo(abiList);
        if (webaseClient.isSmMode()) {
            contractDeployReqBO.setBytecodeBin(EvidenceSignersData.SM_BINARY);
        } else {
            contractDeployReqBO.setBytecodeBin(EvidenceSignersData.BINARY);
        }
        contractDeployReqBO.setContractName(contractName);
        contractDeployReqBO.setVersion(WebaseConstant.CONTRACT_VERSION);

        List<Object> list = new ArrayList<>();
        List<String> signerAddrs = new ArrayList<>();
        signerAddrs.add(userEntity.getPublicKey());
//        signerAddrs.add(WebaseConstant.SIGN_PUBLIC_KEY1);
//        signerAddrs.add(WebaseConstant.SIGN_PUBLIC_KEY2);
        list.add(signerAddrs);

        contractDeployReqBO.setFuncParam(list);
        contractDeployReqBO.setSignUserId(userEntity.getSignUserId());
        String response = HttpUtils.httpPostByJson(webaseFrontContractDeployUrl, JSONUtil.toJsonStr(contractDeployReqBO));
        log.info("进行存证合约部署返回结果为={}", response);
        webaseClient.contractSourceSave();
        webaseClient.contractAddressSave(response);
        contractService.insertContract(contractName, userEntity, response);
        return EndpointResponse.ok(response);
    }

    @Override
    public EndpointResponse chainInfo() {
        ChainInfoResp infoResp = new ChainInfoResp();
        EndpointContractEntity contractEntity = contractService.getEndpointContract();
        if (ObjectUtil.isNotNull(contractEntity)) {
            infoResp.setInitStatus(true);
            return EndpointResponse.ok(infoResp);
        }
        RspBasicInfo basicInfo = webaseClient.basicInfo();
        if (null == basicInfo) {
            return EndpointResponse.error("区块链节点信息查询失败");
        }
        infoResp.setInitStatus(false);
        infoResp.setEncryptType(basicInfo.getEncryptType());
        infoResp.setFiscoBcosVersion(basicInfo.getFiscoBcosVersion());
        infoResp.setWebaseVersion(basicInfo.getWebaseVersion());
        return EndpointResponse.ok(infoResp);
    }

    @Override
    public EndpointResponse getInfoAndChain(Integer userId) {
        EndpointUserEntity userEntity = this.getById(userId);
        EndpointContractEntity contractEntity = contractService.getEndpointContract();
        UserAndChainResp chainResp = new UserAndChainResp();
        BeanUtils.copyProperties(userEntity, chainResp);
        chainResp.setContractName(contractEntity.getContractName());
        chainResp.setContractAddress(contractEntity.getContractAddress());
        chainResp.setCreateTimeStr(DateUtils.formatDateTime(contractEntity.getCreateTime()));
        return EndpointResponse.ok(chainResp);
    }

    /**
     * webase应用注册
     * @author yibi
     * @date 2021-06-28 11:19
     * @param userId 用户id
     * @return EndpointUserEntity
     */
    private EndpointUserEntity registerWb(Integer userId) {
        EndpointUserEntity userEntity = this.getById(userId);
        if (StringUtils.isNotBlank(userEntity.getPublicKey())) {
            return userEntity;
        }
        EndpointResponse response = webaseClient.appRegister();
        if (!EndpointResponse.isOk(response)) {
            return null;
        }
        String webaseUserName = userEntity.getUname() + System.currentTimeMillis();
        RspUserInfo userInfo = webaseClient.newUser(webaseUserName);
        if (ObjectUtil.isNull(userInfo)) {
            log.warn("新增用户私钥失败,userId={}", userId);
            return null;
        }
        userEntity.setSignUserId(userInfo.getSignUserId());
        userEntity.setPublicKey(userInfo.getAddress());
        userEntity.setPrivateKey(userInfo.getPrivateKey());
        this.updateById(userEntity);
        return userEntity;
    }
}
