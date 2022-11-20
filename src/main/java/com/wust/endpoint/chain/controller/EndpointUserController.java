package com.wust.endpoint.chain.controller;

import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.vo.req.LoginReq;
import com.wust.endpoint.chain.vo.req.RegisterReq;
import com.wust.endpoint.chain.vo.req.UserContractReq;
import com.wust.endpoint.chain.service.EndpointUserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * @author xujiao
 * @date 2022-02-19 10:25
 */
@RestController
@RequestMapping("endpointChain/user")
public class EndpointUserController {

    private final EndpointUserService userService;

    public EndpointUserController(EndpointUserService userService) {
        this.userService = userService;
    }

    /**
     * 注册请求
     * @author xujiao
     * @date 2022-03-16 10:27
     * @param registerReq 注册请求
     * @return com.yibi.endpoint.chain.common.EndpointResponse
     */
    @PostMapping("/regist")
    public EndpointResponse regist(@RequestBody RegisterReq registerReq) {
        return userService.register(registerReq);
    }

    /**
     * 用户登录
     * @author xujiao
     * @date 2022-02-24 10:27
     * @param loginReq 登录请求
     * @return com.yibi.endpoint.chain.common.EndpointResponse
     */
    @PostMapping("/login")
    public EndpointResponse login(@RequestBody LoginReq loginReq) {
        return userService.login(loginReq);
    }

    /**
     * 获取链信息
     * @author xujiao
     * @date 2022-02-24 10:27
     * @return com.yibi.endpoint.chain.common.EndpointResponse
     */
    @PostMapping("/chainInfo")
    public EndpointResponse chainInfo() {
        return userService.chainInfo();
    }

    /**
     * 部署存证合约接口
     * @author xujiao
     * @date 2022-02-24 10:27
     * @param contractReq 合约部署请求
     * @return EndpointResponse
     */
    @PostMapping("/deployContract")
    public EndpointResponse deployContract(@RequestBody UserContractReq contractReq) {
        return userService.deployContract(contractReq.getUserId());
    }

    /**
     * 获取用户信息
     * @author xujiao
     * @date 2022-02-24 10:27
     * @param userId 用户id
     * @return com.yibi.endpoint.chain.common.EndpointResponse
     */
    @PostMapping("/{userId}")
    public EndpointResponse userInfo(@PathVariable("userId") Integer userId) {
        return userService.getInfoAndChain(userId);
    }
}
