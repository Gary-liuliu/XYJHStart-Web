package org.xyjh.xyjhstartweb.util.login;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public class EroLabsLogin {
    private final String rawAccount;
    private final String rawPassword;
    private final Integer proxyPort;  // 改为Integer以支持null
    private final int hashCode;
    private final boolean useProxy;   // 新增字段控制是否使用代理

    // 硬编码的常量
    private static final String PROXY_HOST = "localhost";
    private static final String TARGET_URL = "https://www.ero-labs.com/api/v2/login";
    private static final String COOKIES = "_ga=GA1.1.987943544.1746005209; postRight=; reportRight=; forumRight=; erolabsOrderNum=2; addDesktop=true; erolabscoins=0; _clck=17qx5tw%7C2%7Cfvp%7C0%7C1946; erolabsAvatar=; erolabsAvatarFrame=; erolabsgrade=; erolabsgradeisplus=; erolabsjwt=; erolabsaccount=; erolabsnickname=; erolabsuserid=; prefer-types=MALE; DeviceToken=e61a9cf7-24ee-dd8e-d96e-030ef4387358; _clsk=1ecj852%7C1746586533984%7C12%7C0%7Cn.clarity.ms%2Fcollect; _ga_E75ZYNYYN7=GS2.1.s1746584405$o24$g1$t1746586538$j54$l0$h0";

    // 新增构造方法
    public EroLabsLogin(String rawAccount, String rawPassword, int hashCode) {
        this(rawAccount, rawPassword, null, hashCode, false);
    }

    // 修改后的构造方法
    public EroLabsLogin(String rawAccount, String rawPassword, Integer proxyPort, int hashCode, boolean useProxy) {
        this.rawAccount = rawAccount;
        this.rawPassword = rawPassword;
        this.proxyPort = proxyPort;
        this.hashCode = hashCode;
        this.useProxy = useProxy;
    }

    public String login() throws LoginException {
        HttpURLConnection conn = null;
        try {
            // 创建连接 - 根据useProxy决定是否使用代理
            URL url = new URL(TARGET_URL);
            if (useProxy && proxyPort != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, proxyPort));
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            // 配置连接
            configureConnection(conn);

            // 发送POST数据
            sendPostData(conn);

            // 处理响应
            return processResponse(conn);
        } catch (MalformedURLException e) {
            throw new LoginException("Invalid URL format", e);
        } catch (IOException e) {
            handleErrorResponse(conn, e);
            throw new LoginException("IO error during login", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void configureConnection(HttpURLConnection conn) throws ProtocolException {
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // 设置请求头
        conn.setRequestProperty("Host", "www.ero-labs.com");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
        conn.setRequestProperty("DeviceToken", "e61a9cf7-24ee-dd8e-d96e-030ef4387358");
        conn.setRequestProperty("lang", "cn");
        conn.setRequestProperty("sec-ch-ua", "\"Chromium\";v=\"136\", \"Microsoft Edge\";v=\"136\", \"Not.A/Brand\";v=\"99\"");
        conn.setRequestProperty("sec-ch-ua-mobile", "?0");
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setRequestProperty("Origin", "https://www.ero-labs.com");
        conn.setRequestProperty("Sec-Fetch-Site", "same-origin");
        conn.setRequestProperty("Sec-Fetch-Mode", "cors");
        conn.setRequestProperty("Sec-Fetch-Dest", "empty");
        conn.setRequestProperty("Referer", "https://www.ero-labs.com/cn/login.html");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br, zstd");
        conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        conn.setRequestProperty("Cookie", COOKIES);
    }

    private void sendPostData(HttpURLConnection conn) throws IOException {
        // 1. 对原始密码进行Base64编码
        String base64Password = Base64.getEncoder()
                .encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));

        // 2. 对账号和Base64密码进行URL编码
        String encodedAccount = URLEncoder.encode(rawAccount, StandardCharsets.UTF_8.name());
        String encodedPassword = URLEncoder.encode(base64Password, StandardCharsets.UTF_8.name());

        // 构建POST数据
        String postData = String.format("account=%s&password=%s&hashCode=%d",
                encodedAccount,
                encodedPassword, // 这里是双重编码后的密码
                hashCode);

        // 设置Content-Length
        conn.setRequestProperty("Content-Length", String.valueOf(postData.length()));

        // 发送数据
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = postData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private String processResponse(HttpURLConnection conn) throws IOException, LoginException {
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            // 添加更多错误信息
            String errorDetails = readErrorResponse(conn);
            throw new LoginException(String.format(
                    "Unexpected response code: %d\nError details: %s",
                    responseCode,
                    errorDetails
            ));
        }

        return readResponse(conn);
    }

    private String readErrorResponse(HttpURLConnection conn) {
        try {
            InputStream es = conn.getErrorStream();
            if (es == null) return "No error details available";

            // 处理可能的gzip压缩
            String contentEncoding = conn.getContentEncoding();
            InputStream errorStream = contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")
                    ? new GZIPInputStream(es)
                    : es;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (IOException e) {
            return "Failed to read error stream: " + e.getMessage();
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        String contentEncoding = conn.getContentEncoding();
        InputStream inputStream;

        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            inputStream = new GZIPInputStream(conn.getInputStream());
        } else {
            inputStream = conn.getInputStream();
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }

    private void handleErrorResponse(HttpURLConnection conn, IOException originalException) {
        if (conn != null) {
            try {
                InputStream es = conn.getErrorStream();
                if (es != null) {
                    String contentEncoding = conn.getContentEncoding();
                    InputStream errorStream;

                    if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
                        errorStream = new GZIPInputStream(es);
                    } else {
                        errorStream = es;
                    }

                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                        String responseLine;
                        System.err.println("Error response:");
                        while ((responseLine = br.readLine()) != null) {
                            System.err.println(responseLine);
                        }
                    }
                }
            } catch (IOException ex) {
                System.err.println("Failed to read error stream: " + ex.getMessage());
            }
        }
    }

    public static class LoginException extends Exception {
        public LoginException(String message) {
            super(message);
        }

        public LoginException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}