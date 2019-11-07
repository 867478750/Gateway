package com.nlb.security.app.gateway.exception;

public class GatewayNewException extends Exception {
    private String data;

    public GatewayNewException(String data){
        this.data = data;
    }
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
