package org.xyjh.xyjhstartweb.entity;

// 封装登录结果
public class AccountLoginResult {
    private String token;
    private String aid;

    public AccountLoginResult() {}
    public AccountLoginResult(String token, String aid) {
        this.token = token;
        this.aid = aid;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getAid() {
        return aid;
    }
    public void setAid(String aid) {
        this.aid = aid;
    }
}