package com.wust.endpoint.chain.exception;

/**
 *端点异常类
 *@author xujiao
 *@date 2022-02-19
 */
public class EndpointException extends RuntimeException {

    public EndpointException(String message) {
        super(message);
    }

    public EndpointException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndpointException(Throwable cause) {
        super(cause);
    }
}
