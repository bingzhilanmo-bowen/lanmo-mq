package com.lanmo.mq.netty.message;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc 消息定义
 * @date 2017/7/6
 */
public class MsgDefinition {

    //魔数 --就是一个标识 标识文件类型的意思
    public static byte[] MAGIC_CODE_BYTE=new byte[]{(byte) 0xAD, (byte) 0xBF};

}
