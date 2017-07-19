package com.lanmo.mq.netty.message;

import lombok.Data;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/6
 */
@Data
public class MsgHeader {

    /**
     * 消息id
     */
    private long msgId;

    /**
     * 消息类型
     */
    private int msgType;

    /**
     * body长度
     */
    private Integer length;

    public MsgHeader (){

    }

    public MsgHeader (int msgType){
        this.msgType=msgType;
    }
}
