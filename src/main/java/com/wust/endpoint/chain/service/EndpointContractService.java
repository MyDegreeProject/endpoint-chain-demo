package com.wust.endpoint.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wust.endpoint.chain.persist.entity.EndpointContractEntity;
import com.wust.endpoint.chain.persist.entity.EndpointUserEntity;

/**
 * <p>
 * 存证合约表 服务类
 * </p>
 *
 * @author yibi
 * @since 2021-06-24
 */
public interface EndpointContractService extends IService<EndpointContractEntity> {
    /**
     * 写入存证合约记录
     * @author xujiao
     * @date 2022-02-20 17:49
     * @param contractName 合约名称
     * @param userEntity wesign用户
     * @param contractAddress 合约地址
     */
    void insertContract(String contractName, EndpointUserEntity userEntity, String contractAddress);

    /**
     * 数据上链
     * @author xujiao
     * @date 2022-02-20 17:55
     * @param dataHash 上链数据hash
     * @param hashCal  上链数据hash算法
     * @param saveTime 上链时间
     * @return java.lang.String
     */
    String uploadChain(String dataHash, String hashCal, String saveTime);

    /**
     * 查询上链信息
     * @author xujiao
     * @date 2022-02-20 9:23
     * @param dataAddress 数据上链地址
     * @return java.lang.String
     */
    String searchChain(String dataAddress);

    /**
     * 获取合约信息
     * @author xujiao
     * @date 2022-02-20 10:50
     * @return EndpointContractEntity
     */
    EndpointContractEntity getEndpointContract();
}
