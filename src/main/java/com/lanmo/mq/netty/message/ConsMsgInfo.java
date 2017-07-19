package com.lanmo.mq.netty.message;

import lombok.Data;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc 消费者接收到的消息的内容
 * @date 2017/7/6
 */
@Data
public class ConsMsgInfo {

    /**
     * 传递的消息内容
     */
    private String content;
    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息分组
     */
    private String tag;

}
