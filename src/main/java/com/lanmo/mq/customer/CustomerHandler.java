package com.lanmo.mq.customer;

import com.alibaba.fastjson.JSONObject;
import com.lanmo.mq.common.EtcdRoots;
import com.lanmo.mq.etcd.EtcdUtils;
import com.lanmo.mq.netty.message.ConsumerMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.HashedWheelTimer;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/11
 */
@ChannelHandler.Sharable
@Slf4j
public class CustomerHandler extends ChannelInboundHandlerAdapter {

    private ProcessMsgService processMsgService;

    private List<String> topics;

    private HashedWheelTimer timer;

    public CustomerHandler(ProcessMsgService processMsgService,List<String> topics,HashedWheelTimer timer){
        this.processMsgService=processMsgService;
        this.topics=topics;
        this.timer=timer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("connect time："+new Date());
        log.info("CustomerHandler channelActive");
        timer.newTimeout( timeout -> {
            for (String topic:topics){
                log.info("register topic is {},ip is {}",topic,ctx.channel().localAddress().toString());
                //默认存活180S
                EtcdUtils.put(EtcdRoots.registerDir(topic,ctx.channel().localAddress().toString()),ctx.channel().localAddress().toString(),180);
            }
        }  ,170l*1000,TimeUnit.MILLISECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("close connect time："+new Date());
        log.info("CustomerHandler channelInactive");
        for (String topic:topics){
            log.info("remove register topic is {},ip is {}",topic,ctx.channel().localAddress().toString());
            EtcdUtils.delete(EtcdRoots.registerDir(topic,ctx.channel().localAddress().toString()));
        }
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("receive producer msg trans by broker >>>>>>> {}",JSONObject.toJSONString(msg));
        if(msg instanceof ConsumerMsg){
            processMsgService.process((ConsumerMsg) msg);
            ReferenceCountUtil.release(msg);
        }else {
            ctx.fireChannelRead(msg);
        }
    }

}
