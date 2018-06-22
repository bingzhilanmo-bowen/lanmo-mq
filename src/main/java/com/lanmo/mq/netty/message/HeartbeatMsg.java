package com.lanmo.mq.netty.message;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc 心跳消息
 * @date 2017/7/6
 */
@Data
public class HeartbeatMsg extends BaseMsg {

    private long heartTime;

    private String heartInfo;



    public static HeartbeatMsg createHeartMsg(String heartInfo){
        MsgHeader header=new MsgHeader(MsgType.HEARTBEAT.getValue());
        HeartbeatMsg heartbeatMsg=new HeartbeatMsg();
        heartbeatMsg.setHeader(header);
        heartbeatMsg.setHeartInfo(heartInfo);
        return heartbeatMsg;
    }


}
