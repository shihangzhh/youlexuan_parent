package com.offcn.entity;

import java.io.Serializable;
//返回结果封装
public class Result implements Serializable {
    //处理结果状态 true成功 false 失败
    public boolean success;
    //返回消息
    public String message;

    public Result() {
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
