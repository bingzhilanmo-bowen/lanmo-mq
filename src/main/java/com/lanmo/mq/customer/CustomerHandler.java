package com.lanmo.mq.customer;

import com.alibaba.fastjson.JSONObject;
import com.lanmo.mq.netty.message.ConsumerMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

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

    public CustomerHandler(ProcessMsgService processMsgService){
        this.processMsgService=processMsgService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("connect time："+new Date());
        log.info("CustomerHandler channelActive");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("close connect time："+new Date());
        System.out.println("CustomerHandler channelInactive");
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
