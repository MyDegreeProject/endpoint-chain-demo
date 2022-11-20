package com.wust.endpoint.chain.response;

import cn.hutool.http.HttpStatus;
import com.wust.endpoint.chain.enums.RespEnum;

import java.util.HashMap;

/**
 * 接口响应数据封装实体
 * @author xujiao
 * @date 2022-02-19 12:39
 */
public class EndpointResponse extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public EndpointResponse() {
        put("code", RespEnum.SUCCESS.getCode());
        put("msg", RespEnum.SUCCESS.getMsg());
    }

    public static EndpointResponse error() {
        return error(HttpStatus.HTTP_INTERNAL_ERROR, "未知异常，请联系管理员");
    }

    public static EndpointResponse error(String msg) {
        return error(HttpStatus.HTTP_INTERNAL_ERROR, msg);
    }

    public static EndpointResponse error(int code, String msg) {
        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.put("code", code);
        endpointResponse.put("msg", msg);
        return endpointResponse;
    }

    public static EndpointResponse error(RespEnum respEnum) {
        EndpointResponse eviResponse = new EndpointResponse();
        eviResponse.put("code", respEnum.getCode());
        eviResponse.put("msg", respEnum.getMsg());
        return eviResponse;
    }

    public static EndpointResponse ok() {
        return new EndpointResponse();
    }

    public EndpointResponse put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static EndpointResponse ok(Object obj) {
        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.put("code", RespEnum.SUCCESS.getCode());
        endpointResponse.put("msg", RespEnum.SUCCESS.getMsg());
        endpointResponse.put("data", obj);
        return endpointResponse;
    }

    public static Boolean isOk(EndpointResponse endpointResponse) {
        Integer code = endpointResponse.get("code") == null ? null : Integer.valueOf(endpointResponse.get("code").toString());
        return code == RespEnum.SUCCESS.getCode();
    }
}
