package com.lanmo.mq.broker;

import com.lanmo.mq.netty.message.CustomerRegisterMsg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc 存储topic的消费信息
 * @date 2017/7/13
 */
@Slf4j
public class TopicContainer {

    private static ConcurrentHashMap<String,Channel> connectChannel=new ConcurrentHashMap<>();


    @Deprecated
    private static ConcurrentHashMap<String,Set<Channel>> topicSubscrie=new ConcurrentHashMap<>();


    /**
     * 注册topic
     * @param ctx
     * @param msg
     */
    @Deprecated
    public static void addTopicSub(ChannelHandlerContext ctx, CustomerRegisterMsg msg){
        String topic=msg.getTopic();
        log.info("add topic {},remote address {} channel",topic,ctx.channel().remoteAddress().toString());
        Set<Channel> topicSubChannel=  topicSubscrie.get(topic);
        if(topicSubChannel==null){
            topicSubChannel=new ConcurrentSet<Channel>();
        }
        topicSubChannel.add(ctx.channel());
        topicSubscrie.put(topic,topicSubChannel);
    }

    /**
     * 获取消费这个topic的 channel
     * @param topic
     * @return
     */
    @Deprecated
    public static Set<Channel> getTopicSubs(String topic){
        return topicSubscrie.get(topic);
    }

    /**
     * 记录建立链接的channel
     * @param ctx
     */
    public static void registerChannel(ChannelHandlerContext ctx){
        connectChannel.put(ctx.channel().remoteAddress().toString(),ctx.channel());
    }

    /**
     * 移除断开链接的channel
     * @param ctx
     */
    public static void removeChannel(ChannelHandlerContext ctx){
        connectChannel.remove(ctx.channel().remoteAddress().toString());
    }

    public static Channel getSendChannel(String ip){
        return connectChannel.get(ip);
    }
}
