package com.lanmo.mq.netty.message;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/6
 */
public enum MsgType {

    PRODUCER(1), // 生产者
    CONSUMER(2),//消费者
    HEARTBEAT(3),//心跳
    REGISTER(4);//注册消费信息


    private int value;

    private MsgType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


}
