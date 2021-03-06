package com.lanmo.mq.broker;

import com.alibaba.fastjson.JSONObject;
import com.lanmo.mq.common.EtcdRoots;
import com.lanmo.mq.delay.DelayPush;
import com.lanmo.mq.etcd.EtcdUtils;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc
 * @date 2017/7/7
 */
@ChannelHandler.Sharable
@Slf4j
public class BrokerHandler extends ChannelInboundHandlerAdapter {
    //记录register 的次数
    AtomicInteger runNum=new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("connect time ：{}",new Date());
        log.info("connect address {}",ctx.channel().remoteAddress().toString());
        TopicContainer.registerChannel(ctx);
       // ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("in active connect time ：{}",new Date());
        log.info("in active address is {}",ctx.channel().remoteAddress().toString());
        TopicContainer.removeChannel(ctx);
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
        }

    }


    private void sendCustomerMsg2Customer(ProducerMsg msg){
        ProMsgInfo msgInfo=msg.getProMsgInfo();
        log.info("receive need send to customer msg is {}",JSONObject.toJSONString(msgInfo));
        if(msgInfo.getDelaySend()>0){
            //延迟队列
            DelayPush.add(msgInfo.getDelaySend(),msgInfo.getTopic(),msgInfo.getTag(),msgInfo.getContent(),msgInfo.getConsumptionType());
        }else {
            String topic=msgInfo.getTopic();
            List<String> subRegister= EtcdUtils.getDir(EtcdRoots.topicDir(topic));
            log.info("not delay topic {},sub ips {}",topic,JSONObject.toJSONString(subRegister));
            //@TODO 负载均衡
            if(!subRegister.isEmpty()){
                ConsumerMsg consumerMsg=new ConsumerMsg();
                MsgHeader msgHeader=new MsgHeader();
                msgHeader.setMsgType(MsgType.CONSUMER.getValue());
                consumerMsg.setHeader(msgHeader);
                ConsMsgInfo consMsgInfo=new ConsMsgInfo();
                BeanUtils.copyProperties(msg.getProMsgInfo(),consMsgInfo);
                consumerMsg.setConsMsgInfo(consMsgInfo);
                for (Channel ch:getChannels(subRegister,msgInfo.getConsumptionType()))
                    ch.writeAndFlush(consumerMsg);
            }
        }
    }

    private List<Channel> getChannels(List<String> subRegister,Integer consumptionType){
        if(consumptionType== ConsumptionType.ALONE.getValue()){
            String subIp=subRegister.get(0);
            Channel sendChannel= TopicContainer.getSendChannel(subIp);
            return Arrays.asList(sendChannel);
        }else {
            return subRegister.stream().map((sub)->{
                return TopicContainer.getSendChannel(sub);
            }).collect(Collectors.toList());
        }
    }

}
