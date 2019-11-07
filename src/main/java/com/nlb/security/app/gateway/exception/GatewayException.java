package com.nlb.security.app.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GatewayException  {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String gatewayHandeler(HttpServletResponse response ,Exception e){
        response.setStatus(HttpStatus.BAD_GATEWAY.value());
        return "token已经失效";
    }
}
