package com.lanmo.mq.netty.message;

import lombok.Data;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc 生产者消息的内容
 * @date 2017/7/6
 */
@Data
public class ProMsgInfo {

    /**
     * 传递的消息内容
     */
    private String content;


    /**
     * 消费类型 广播 还是 单独消费
     */
    private Integer consumptionType;

    /**
     * 延迟投递 0 延迟  delaySend>0 表示 延迟多少秒 投递
     */
    private Integer delaySend;

    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息分组
     */
    private String tag;

}
