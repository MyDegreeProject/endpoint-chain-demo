package com.wust.endpoint.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wust.endpoint.chain.persist.entity.EndpointUserEntity;
import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.vo.req.LoginReq;
import com.wust.endpoint.chain.vo.req.RegisterReq;

/**
 * <p>
 * 管理用户表 服务类
 * </p>
 *
 * @author yibi
 * @since 2021-06-24
 */
public interface EndpointUserService extends IService<EndpointUserEntity> {
    /**
     * 用户注册处理
     * @author xujiao
     * @date 2022-03-16 10:36
     * @param registerReq 注册请求
     * @return com.wust.endpoint.chain.response.EndpointResponse
     */
    EndpointResponse register(RegisterReq registerReq);
    /**
     * 用户登录处理
     * @author xujiao
     * @date 2022-02-24 10:36
     * @param loginReq 登录请求
     * @return com.wust.endpoint.chain.response.EndpointResponse
     */
    EndpointResponse login(LoginReq loginReq);

    /**
     * 部署存证合约
     * @author xujiao
     * @date 2022-02-24 10:36
     * @param userId 登录用户id
     * @return com.wust.endpoint.chain.response.EndpointResponse
     */
    EndpointResponse deployContract(Integer userId);

    /**
     * 获取链节点信息
     * @author xujiao
     * @date 2022-02-24 10:36
     * @return com.wust.endpoint.chain.response.EndpointResponse
     */
    EndpointResponse chainInfo();

    /**
     * 获取用户和链信息
     * @author xujiao
     * @date 2022-02-24 10:36
     * @param userId 用户id
     * @return com.wust.endpoint.chain.response.EndpointResponse
     */
    EndpointResponse getInfoAndChain(Integer userId);
}
