package com.lanmo.mq.producer;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc
 * @date 2017/7/11
 */
@ChannelHandler.Sharable
@Slf4j
public class ProducerHandler extends ChannelInboundHandlerAdapter {

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

        log.info(JSONObject.toJSONString(msg));
        ctx.write("has read message from server");
        ctx.flush();
        ReferenceCountUtil.release(msg);
    }

}
