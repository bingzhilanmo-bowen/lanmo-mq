package com.lanmo.mq.netty.code;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc 序列化接口
 * @date 2017/7/6
 */
public interface Codec {

    /**
     * 解码---byte到某个具体的实体类
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T decode(byte[] bytes, Class<T> clazz);


    /**
     * 对象转bytes
     * @param data
     * @return
     */
    <T> byte[] encode(T data);

}
