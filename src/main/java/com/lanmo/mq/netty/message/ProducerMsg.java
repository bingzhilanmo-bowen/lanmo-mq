package com.lanmo.mq.netty.message;

import lombok.Data;

/**
 * @author bingzhilanmo@gmail.com
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
