package com.lanmo.mq.broker;

import com.alibaba.fastjson.JSONObject;
import com.lanmo.mq.netty.message.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/7
 */
@ChannelHandler.Sharable
@Slf4j
public class BrokerHandler extends ChannelInboundHandlerAdapter {

    AtomicInteger runNum=new AtomicInteger(0);

    /**
     * 存储topic和消费节点的对应关系
     */

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("connect time ：{}",new Date());
        log.info("connect address {}",ctx.channel().remoteAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("in active connect time ：{}",new Date());
        log.info("in active address is {}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("channel  Read Data  Complete ....");
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HeartbeatMsg){
            log.info("receive heartbeat msg {}",JSONObject.toJSONString(msg));
            ReferenceCountUtil.release(msg);
        }else if (msg instanceof ProducerMsg){
            log.info("receive producer msg {}",JSONObject.toJSONString(msg));
            sendCustomerMsg2Customer((ProducerMsg) msg);
            ReferenceCountUtil.release(msg);
        }else if(msg instanceof CustomerRegisterMsg){
            log.info("register num is {}",runNum.addAndGet(1));
            log.info("receive customer register msg {}",JSONObject.toJSONString(msg));
            TopicContainer.addTopicSub(ctx, (CustomerRegisterMsg) msg);
            ReferenceCountUtil.release(msg);
        }

    }


    private void sendCustomerMsg2Customer(ProducerMsg msg){
        ProMsgInfo msgInfo=msg.getProMsgInfo();
        log.info("receive need send to customer msg is {}",JSONObject.toJSONString(msgInfo));
        String topic=msgInfo.getTopic();
        Set<Channel> topicSubChannel=TopicContainer.getTopicSubs(topic);

        //@TODO 负载均衡

        if(!topicSubChannel.isEmpty()){
            Channel sendChannel=topicSubChannel.iterator().next();
            log.info("topic {},msg {}, send channel is {}",topic,JSONObject.toJSONString(msgInfo),sendChannel.remoteAddress().toString());

            ConsumerMsg consumerMsg=new ConsumerMsg();
            MsgHeader msgHeader=new MsgHeader();
            msgHeader.setMsgType(MsgType.CONSUMER.getValue());
            consumerMsg.setHeader(msgHeader);

            ConsMsgInfo consMsgInfo=new ConsMsgInfo();
            BeanUtils.copyProperties(msg.getProMsgInfo(),consMsgInfo);
            consumerMsg.setConsMsgInfo(consMsgInfo);

            sendChannel.writeAndFlush(consumerMsg);
        }
    }

}
