package com.lanmo.mq.netty.message;

import lombok.Data;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/6
 */
@Data
public class ProducerMsg extends BaseMsg {
    /**
     * 发送时间
     */
    private long sendTime;

    private ProMsgInfo proMsgInfo;
}
