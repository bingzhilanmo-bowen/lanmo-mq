package com.lanmo.mq.customer;

import com.lanmo.mq.netty.message.ConsumerMsg;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc 处理消息的逻辑接口
 * @date 2017/7/20
 */
public interface ProcessMsgService {
    /**
     * 处理接受到的消息
     */
    public void process(ConsumerMsg consumerMsg);

}
