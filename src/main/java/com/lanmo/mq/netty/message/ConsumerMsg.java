package com.lanmo.mq.netty.message;

import lombok.Data;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/6
 */
@Data
public class ConsumerMsg extends BaseMsg {

    /**
     * 接受时间
     */
    private long receiveTime;

    protected ConsMsgInfo consMsgInfo;
}
