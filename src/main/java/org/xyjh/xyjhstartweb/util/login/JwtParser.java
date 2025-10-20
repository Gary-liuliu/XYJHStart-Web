package org.xyjh.xyjhstartweb.util.login;

import org.json.JSONObject;

public class JwtParser {

    /**
     * 从 JSON 对象中解析出 JWT
     * @param jsonObject 输入的 JSON 对象
     * @return 返回 JWT 字符串，如果不存在则返回 null
     */
    public static String parseJwt(JSONObject jsonObject) {
        try {
            JSONObject data = jsonObject.getJSONObject("data");
            return data.getString("jwt");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 JSON 对象中解析出 userId
     * @param jsonObject 输入的 JSON 对象
     * @return 返回 userId 字符串，如果不存在则返回 null
     */
    public static String parseUserId(JSONObject jsonObject) {
        try {
            JSONObject data = jsonObject.getJSONObject("data");
            return data.getString("userId");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 JSON 字符串中解析出 JWT 和 userId
     * @param jsonString 输入的 JSON 字符串
     * @return 包含 jwt 和 userId 的 JSONObject，如果解析失败则返回 null
     */
    public static JSONObject parseFromString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject result = new JSONObject();
            result.put("jwt", parseJwt(jsonObject));
            result.put("userId", parseUserId(jsonObject));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}