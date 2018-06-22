package com.lanmo.mq.delay;

import com.lanmo.mq.broker.TopicContainer;
import com.lanmo.mq.common.EtcdRoots;
import com.lanmo.mq.etcd.EtcdUtils;
import com.lanmo.mq.netty.message.*;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc
 * @date 2017/8/1
 */
@Slf4j
public class DelayTask implements Runnable,Delayed {


    private final int delta;//差值信号：用于记录任务线程的延迟时间；

    private final long trigger; //触发信号：任务线程在加入队列，延迟期满，即将要执行时的系统时间；

    private String topic;

    private String tag;

    private String content;

    private Long id;

    private Integer consumptionType;


    public DelayTask(int delayInMilliseconds, String topic, String tag, String content, Integer consumptionType){
        delta = delayInMilliseconds;
        trigger = System.currentTimeMillis() + (long)delta;
        this.topic=topic;
        this.tag=tag;
        this.content = content;
        this.id=System.currentTimeMillis();
        this.consumptionType=consumptionType;
    }

    public DelayTask(int delayInMilliseconds, String topic, String tag, String content, Long id, Integer consumptionType){
        delta = delayInMilliseconds;
        trigger = System.currentTimeMillis() + (long)delta;
        this.topic=topic;
        this.tag=tag;
        this.content = content;
        this.id=id;
        this.consumptionType=consumptionType;
    }

    @Override
    public long getDelay(TimeUnit unit){   //提供TimeUnit的时间单位的转换；
        return unit.convert(trigger - System.currentTimeMillis(),TimeUnit.MILLISECONDS);
    }


    @Override
    public int compareTo(Delayed delayed) {
        DelayTask task = (DelayTask)delayed;
        return this.id>task.id?1:(this.id)<task.id?-1:0;
    }

    @Override
    public void run() {
        List<String> subRegister= EtcdUtils.getDir(EtcdRoots.topicDir(topic));
        if(!subRegister.isEmpty()){
            List<Channel> consList=getChannels(subRegister);
            log.info("topic {},msg {}, send channel is {}",topic, consList.size());

            ConsumerMsg consumerMsg=new ConsumerMsg();
            MsgHeader msgHeader=new MsgHeader();
            msgHeader.setMsgType(MsgType.CONSUMER.getValue());
            consumerMsg.setHeader(msgHeader);

            ConsMsgInfo consMsgInfo=new ConsMsgInfo();
            consMsgInfo.setTopic(topic);
            consMsgInfo.setTag(tag);
            consMsgInfo.setContent(content);
            consumerMsg.setConsMsgInfo(consMsgInfo);

            for (Channel ch:consList)
                ch.writeAndFlush(consumerMsg);
        }
    }

    /**
     * 如果是只需要一个消费者的情况下
     * @return
     */
    private List<Channel> getChannels(List<String> subRegister){
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
