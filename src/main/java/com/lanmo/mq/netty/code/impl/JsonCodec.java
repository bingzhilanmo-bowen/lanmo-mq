package com.lanmo.mq.netty.code.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lanmo.mq.netty.code.Codec;

import java.io.IOException;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc json类型编码和解码
 * @date 2017/7/6
 */
public class JsonCodec implements Codec {


    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    /**
     * 解码---byte到某个具体的实体类
     *
     * @param bytes
     * @param clazz
     * @return
     */
    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, 0, bytes.length, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对象转bytes
     *
     * @param data
     * @return
     */
    @Override
    public <T> byte[] encode(T data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
