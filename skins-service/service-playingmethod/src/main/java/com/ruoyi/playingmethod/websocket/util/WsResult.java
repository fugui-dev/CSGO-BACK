package com.ruoyi.playingmethod.websocket.util;

import com.ruoyi.common.constant.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author ruoyi
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsResult<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 成功 */
    public static final int SUCCESS = HttpStatus.SUCCESS;

    /** 失败 */
    public static final int FAIL = HttpStatus.ERROR;

    private String key;

    private T data;

    private int code;

    private String msg;

    public static <T> WsResult<T> ok()
    {
        return new WsResult(null,null, SUCCESS, "操作成功");
    }

    public static <T> WsResult<T> ok(String key, T data)
    {
        return new WsResult(key,data, SUCCESS, "操作成功");
    }

    public static <T> WsResult<T> ok(String key,T data,String msg)
    {
        return new WsResult(key,data, SUCCESS, msg);
    }

    public static <T> WsResult<T> ok(String msg)
    {
        return new WsResult(null,null, SUCCESS, msg);
    }

    public static <T> WsResult<T> fail()
    {
        return new WsResult(null,null, FAIL, "操作失败");
    }

    public static <T> WsResult<T> fail(String key,T data,String msg)
    {
        return new WsResult(key,data, FAIL, msg);
    }

    public static <T> WsResult<T> fail(String msg)
    {
        return new WsResult(null,null, FAIL, msg);
    }

    public static <T> Boolean isError(WsResult<T> ret)
    {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(WsResult<T> ret)
    {
        return WsResult.SUCCESS == ret.getCode();
    }
}
