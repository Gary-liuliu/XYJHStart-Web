package org.xyjh.xyjhstartweb.util;

public class Result<T> {

    /**
     * 状态码：0 表示成功，非 0 表示失败
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ✅ 静态方法快速生成成功结果
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    // ✅ 静态方法快速生成失败结果
    public static <T> Result<T> fail(String message) {
        return new Result<>(1, message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    // getter & setter
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}