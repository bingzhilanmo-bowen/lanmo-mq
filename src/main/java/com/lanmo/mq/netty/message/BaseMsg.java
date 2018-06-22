package com.lanmo.mq.netty.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Data;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc
 * @date 2017/7/6
 */
@Data
public class BaseMsg {

    /**
     * 请求头
     */
    private MsgHeader header;

    /**
     * 消息体
     */
    @JsonIgnore
    private ByteBuf msgBody;

    @JsonIgnore
    private transient Channel channel;


    public long getMsgId() {
        return header.getMsgId();
    }

    public void setMsgId(long msgId) {
        this.getHeader().setMsgId(msgId);
    }

}
