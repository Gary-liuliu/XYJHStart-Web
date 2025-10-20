package org.xyjh.xyjhstartweb.util.login;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class EroLabsCaptchaVerifier {
    private final Integer proxyPort;  // 改为Integer以支持null
    private final boolean useProxy;   // 新增字段控制是否使用代理
    private static final String PROXY_HOST = "localhost";
    private static final String TARGET_URL = "https://www.ero-labs.com/api/v2/captcha/isVerify";
    private static final String POST_DATA = "datas%5B%5D=-1&datas%5B%5D=-2&datas%5B%5D=-2&datas%5B%5D=-2&datas%5B%5D=-2&datas%5B%5D=-2&datas%5B%5D=-2&datas%5B%5D=-2&datas%5B%5D=-2&datas%5B%5D=-1&datas%5B%5D=-1&datas%5B%5D=-1&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=0&datas%5B%5D=1&datas%5B%5D=1";
    private static final String COOKIES = "_ga=GA1.1.987943544.1746005209; postRight=; reportRight=; forumRight=; erolabsOrderNum=2; addDesktop=true; erolabscoins=0; _clck=17qx5tw%7C2%7Cfvp%7C0%7C1946; erolabsAvatar=; erolabsAvatarFrame=; erolabsgrade=; erolabsgradeisplus=; erolabsjwt=; erolabsaccount=; erolabsnickname=; erolabsuserid=; prefer-types=MALE; DeviceToken=e61a9cf7-24ee-dd8e-d96e-030ef4387358; _clsk=1ecj852%7C1746586062728%7C10%7C0%7Cn.clarity.ms%2Fcollect; _ga_E75ZYNYYN7=GS2.1.s1746584405$o24$g1$t1746586066$j55$l0$h0";

    // 新增构造方法
    public EroLabsCaptchaVerifier() {
        this(null, false);
    }

    // 修改后的构造方法
    public EroLabsCaptchaVerifier(Integer proxyPort, boolean useProxy) {
        this.proxyPort = proxyPort;
        this.useProxy = useProxy;
    }

    public int verifyCaptcha() throws CaptchaVerificationException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(TARGET_URL);

            // 根据useProxy决定是否使用代理
            if (useProxy && proxyPort != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, proxyPort));
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            // Configure connection
            configureConnection(conn);

            // Send POST data
            sendPostData(conn);

            // Get and process response
            return processResponse(conn);
        } catch (MalformedURLException e) {
            throw new CaptchaVerificationException("无效的URL格式", e);
        } catch (IOException e) {
            handleErrorResponse(conn, e);
            throw new CaptchaVerificationException("网络错误", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void configureConnection(HttpURLConnection conn) throws ProtocolException {
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // Set request headers
        conn.setRequestProperty("Host", "www.ero-labs.com");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Length", String.valueOf(POST_DATA.length()));
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
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = POST_DATA.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private int processResponse(HttpURLConnection conn) throws IOException, CaptchaVerificationException {
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new CaptchaVerificationException("Unexpected response code: " + responseCode);
        }

        String response = readResponse(conn);
        return extractHashCode(response);
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

    private int extractHashCode(String response) throws CaptchaVerificationException {
        String hashCodePattern = "\"hashCode\":";
        int startIndex = response.indexOf(hashCodePattern);
        if (startIndex == -1) {
            throw new CaptchaVerificationException("无效的响应格式 - 未找到 hashCode 字段");
        }
        startIndex += hashCodePattern.length();
        int endIndex = response.indexOf("}", startIndex);
        if (endIndex == -1) {
            endIndex = response.length();
        }

        try {
            return Integer.parseInt(response.substring(startIndex, endIndex).trim());
        } catch (NumberFormatException e) {
            throw new CaptchaVerificationException("从响应中解析 hashCode 失败", e);
        }
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

    public static class CaptchaVerificationException extends Exception {
        public CaptchaVerificationException(String message) {
            super(message);
        }

        public CaptchaVerificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}