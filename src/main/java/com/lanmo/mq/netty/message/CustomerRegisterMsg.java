package com.lanmo.mq.netty.message;

import lombok.Data;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc 注册消费的topic的消息
 * @date 2017/7/12
 */
@Data
public class CustomerRegisterMsg extends BaseMsg {

    private long registerTime;

    private String topic;

}
