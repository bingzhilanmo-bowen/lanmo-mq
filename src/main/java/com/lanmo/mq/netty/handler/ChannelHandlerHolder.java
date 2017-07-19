package com.lanmo.mq.netty.handler;

import io.netty.channel.ChannelHandler;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc 客户端的ChannelHandler集合，由子类实现，这样做的好处：
 * 继承这个接口的所有子类可以很方便地获取ChannelPipeline中的Handlers
 * 获取到handlers之后方便ChannelPipeline中的handler的初始化和在重连的时候也能很方便
 * 地获取所有的handlers
 * @date 2017/7/10
 */
public interface ChannelHandlerHolder {

    ChannelHandler[] handlers();

}