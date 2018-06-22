package com.lanmo.mq.netty.message;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc 消费类型
 * @date 2017/7/6
 */
public enum ConsumptionType {

    ALONE(0),//只有一个消费者消费
    BROADCAST(1);//广播消费


    private int value;

    private ConsumptionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
