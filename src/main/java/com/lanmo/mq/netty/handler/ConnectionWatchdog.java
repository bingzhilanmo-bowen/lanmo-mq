package com.lanmo.mq.netty.handler;

import com.lanmo.mq.producer.ProducerServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc 检查连接是否断开，断开了 就重试12次
 * @date 2017/7/10
 */
@Slf4j
@ChannelHandler.Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask,ChannelHandlerHolder {

    private final Bootstrap bootstrap;

    private final Timer timer;

    private final int port;

    private final String host;

    private volatile boolean reconnect=true;

    private int attempts;

    public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, int port,String host, boolean reconnect) {
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.port = port;
        this.host = host;
        this.reconnect = reconnect;
    }

    /**
     * channel链路每次active的时候，将其连接的次数重新☞ 0
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("connect ing ,reset attempts=0 ");
        attempts = 0;
        ctx.fireChannelActive();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      log.info("connect closed");
      if(reconnect){
          log.info("Try reconnecting ");
          if(attempts<12){
              attempts++;
          }
          int timeout=2<<attempts;//重试的间隔时间 会越来越长
          log.info("add new try connect task ,run time is {}",timeout);
          timer.newTimeout(this,timeout, TimeUnit.MILLISECONDS);//添加任务到 时间轮中
      }
      ctx.fireChannelInactive();
    }

    public void run(Timeout timeout) throws Exception {
        log.info("start connect server ");
        ChannelFuture future;
        //bootstrap已经初始化好了，只需要将handler填入就可以了
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>(){
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(handlers());
                }
            });
            future = bootstrap.connect(host,port);
        }
        //future对象
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                boolean succeed = f.isSuccess();
                //如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
                if (!succeed) {
                   log.info("Try reconnecting fail");
                    f.channel().pipeline().fireChannelInactive();
                }else{
                    log.info("Try reconnecting success");
                }
            }
        });
    }

}
