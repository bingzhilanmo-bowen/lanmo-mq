package com.lanmo.mq.broker;

import com.lanmo.mq.netty.message.HeartbeatMsg;
import com.lanmo.mq.netty.message.MsgHeader;
import com.lanmo.mq.netty.message.MsgType;
import com.lanmo.mq.netty.message.ProMsgInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc broker 的基础处理类
 * @date 2017/7/7
 */
@Slf4j
public class AbstractBrokerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 回复心跳消息
     * @param channel
     * @param msg
     */
    protected void sendHeartbeatMsg(Channel channel,String msg){
        MsgHeader msgHeader=new MsgHeader(MsgType.HEARTBEAT.getValue());
        HeartbeatMsg heartbeatMsg=new HeartbeatMsg();
        if(StringUtils.isNotBlank(msg)){
            heartbeatMsg.setHeartInfo(msg);
        }
        heartbeatMsg.setHeartTime(System.currentTimeMillis());
        heartbeatMsg.setHeader(msgHeader);
        channel.write(heartbeatMsg,channel.voidPromise());
    }


    /**
     * 接收到 生产的消息，处理生产者的消息（并不一定 就是及时发送到消费者）
     * @param channel
     * @param proMsgInfo
     */
    protected void sendConsumerMsg(Channel channel, ProMsgInfo proMsgInfo){
        //@TODO

    }



}
