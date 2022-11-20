package com.wust.endpoint.chain.vo.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 注册实体
 * @author xujiao
 * @date 2022-02-24 10:28
 */
@Data
public class RegisterReq implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1297341721910577374L;

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 组织描述
     */
    private String orgDescription;
}
