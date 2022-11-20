package com.wust.endpoint.chain.bo;

/**
 * 区块链交易返回log
 * @author xujiao
 * @date 2022-02-21 12:37
 */
public class LogAddressBO {

    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "LogAddressBO{" +
                "address='" + address + '\'' +
                '}';
    }
}
