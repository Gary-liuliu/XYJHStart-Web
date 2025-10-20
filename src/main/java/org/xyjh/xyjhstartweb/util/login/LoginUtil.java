package org.xyjh.xyjhstartweb.util.login;

import org.json.JSONObject;

public class LoginUtil {

    /**
     * 执行登录操作并返回认证信息
     * @param useProxy 是否使用代理
     * @param proxyPort 代理端口号(不使用代理时可传0)
     * @param username 用户名
     * @param password 密码
     * @return 包含JWT和用户ID的字符串数组，[0]是JWT，[1]是用户ID
     * @throws RuntimeException 当验证码验证或登录失败时抛出
     */
    public static String[] doLogin(boolean useProxy, int proxyPort, String username, String password) {
        try {
            // 1. 验证码验证
            EroLabsCaptchaVerifier verifier = new EroLabsCaptchaVerifier(useProxy ? proxyPort : null, useProxy);
            int hashCode = verifier.verifyCaptcha();

            // 2. 执行登录
            EroLabsLogin login = new EroLabsLogin(username, password, useProxy ? proxyPort : null, hashCode, useProxy);
            String result = login.login();

            // 3. 解析结果
            JSONObject jsonObject = new JSONObject(result);
            String jwt = JwtParser.parseJwt(jsonObject);
            String userId = JwtParser.parseUserId(jsonObject);

            return new String[]{jwt, userId};

        } catch (EroLabsCaptchaVerifier.CaptchaVerificationException | EroLabsLogin.LoginException e) {
            throw new RuntimeException("登录失败: " + e.getMessage(), e);
        }
    }


    public static String[] interactiveLogin(boolean useProxy,int proxyPort,String username,String password) {
        return doLogin(useProxy, proxyPort, username, password);
    }
}