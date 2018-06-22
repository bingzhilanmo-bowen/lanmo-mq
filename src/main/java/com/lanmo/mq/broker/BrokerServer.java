package com.lanmo.mq.broker;

import com.lanmo.mq.netty.MqDecoder;
import com.lanmo.mq.netty.MqEncoder;
import com.lanmo.mq.netty.handler.AcceptorIdleStateTrigger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc
 * @date 2017/7/7
 */

@Slf4j
public class BrokerServer {
    private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();
    private int port;

    public BrokerServer(int port) {
        this.port = port;
    }

    public void start(){

        EventLoopGroup bossGroup=new NioEventLoopGroup(1);
        EventLoopGroup workerGroup=new NioEventLoopGroup();


        try {
            ServerBootstrap sb=new ServerBootstrap().group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
                    .localAddress(new InetSocketAddress(port)).childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //设置读的超时时间 --就是5s中没有数据到server ---坚持心跳 没有心跳 就关闭连接
                            ch.pipeline().addLast(new IdleStateHandler(60,0,0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(idleStateTrigger);
                            ch.pipeline().addLast(new MqDecoder());
                            ch.pipeline().addLast(new MqEncoder());
                            ch.pipeline().addLast(new BrokerHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG,128).childOption(ChannelOption.SO_KEEPALIVE,true);

            ChannelFuture future=sb.bind(port).sync();

            future.channel().closeFuture().sync();

        }catch (Exception e){
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new BrokerServer(port).start();
    }

}
