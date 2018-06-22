package com.lanmo.mq.customer;

import com.lanmo.mq.common.EtcdRoots;
import com.lanmo.mq.etcd.EtcdUtils;
import com.lanmo.mq.netty.MqDecoder;
import com.lanmo.mq.netty.MqEncoder;
import com.lanmo.mq.netty.handler.ConnectionWatchdog;
import com.lanmo.mq.netty.handler.ConnectorIdleStateTrigger;
import com.lanmo.mq.netty.message.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc
 * @date 2017/7/11
 */
@Slf4j
public class CustomerServer {

    protected final HashedWheelTimer timer=new HashedWheelTimer();

    private Bootstrap bootstrap;

    private Channel channel;

    private volatile AtomicBoolean started=new AtomicBoolean(false);

    private final ConnectorIdleStateTrigger idleStateTrigger=new ConnectorIdleStateTrigger();

    public void connect(int port, String host, ProcessMsgService processMsgService, List<String> topics) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO));

        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer, port,host, true) {

            public ChannelHandler[] handlers() {
                return new ChannelHandler[] {
                        this,
                        new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
                        idleStateTrigger,
                        new MqDecoder(),
                        new MqEncoder(),
                        new CustomerHandler(processMsgService,topics)
                };
            }
        };

        ChannelFuture future;
        //进行连接
        try {
            synchronized (bootstrap) {
                bootstrap.handler(new ChannelInitializer() {

                    //初始化channel
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(watchdog.handlers());
                    }
                });

                future = bootstrap.connect(host,port);

                future.addListeners(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        started.set(true);
                        channel=future.channel();
                    }
                });
            }
            // 以下代码在synchronized同步块外面是安全的
            future.sync();
        } catch (Throwable t) {
            throw new Exception("connects to  fails", t);
        }
    }

    public boolean isStarted() {
        return started.get();
    }

    public ChannelFuture  writeAndFlush(CustomerRegisterMsg msg) {
        if(!isStarted()){
            throw new IllegalStateException("client not started !");
        }
        return channel.writeAndFlush(msg);
    }

    public void registerTopic(String topic){
        log.info("register topic is {},ip is {}",topic,channel.localAddress().toString());
        EtcdUtils.put(EtcdRoots.registerDir(topic,channel.localAddress().toString()),channel.localAddress().toString());
    }



    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        CustomerServer customerServer=  new CustomerServer();
        customerServer.connect(port, "127.0.0.1",(msg)->{
            String topic=msg.getConsMsgInfo().getTopic();
            String tag=msg.getConsMsgInfo().getContent();
            log.info("receive topic is {},and tag is {}",topic,tag);
            //@TODO 自己的业务逻辑
        }, Arrays.asList("test_topic"));



    }

}
