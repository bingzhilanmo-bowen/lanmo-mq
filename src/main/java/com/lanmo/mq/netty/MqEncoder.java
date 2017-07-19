package com.lanmo.mq.netty;

import com.alibaba.fastjson.JSONObject;
import com.lanmo.mq.netty.code.Codec;
import com.lanmo.mq.netty.code.impl.JsonCodec;
import com.lanmo.mq.netty.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/6
 */
@Slf4j
public class MqEncoder extends MessageToByteEncoder {


    private Codec DEFAULT_CODEC=new JsonCodec();

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        ByteBuf byteBuf=null;
        if(out==null){
            out=ctx.alloc().buffer();
        }
        try{
            if(msg instanceof BaseMsg){
                byteBuf=encodeMsg(ctx,(BaseMsg) msg,out);
            }else {
                //不是我们需要支持的类型
                log.error("msg is not mq mg");
            }
        }finally {
            if(byteBuf!=null){
                byteBuf.release();//不为空 释放buf
            }
        }
    }

    /**
     * 编码数据
     * @param ctx
     * @param msg
     * @param out
     * @return
     */
    private ByteBuf encodeMsg(ChannelHandlerContext ctx, BaseMsg msg, ByteBuf out){
         ByteBuf byteBuf=readAll(ctx,msg);
         log.info("msg total read length {}",byteBuf.readableBytes());
         //获取总长度 预留4位在存储这个长度
         int totalLength=4+byteBuf.readableBytes();
         out.writeBytes(MsgDefinition.MAGIC_CODE_BYTE);
         if(out.capacity()<totalLength+2){
             //如果容量太小 就扩容
             out.capacity(totalLength+2);
         }
         //写入 可读消息的总长度
         out.writeInt(totalLength);
         //写入到out中
         out.writeBytes(byteBuf,byteBuf.readerIndex(),byteBuf.readableBytes());

         return byteBuf;

    }

    private ByteBuf readAll(ChannelHandlerContext ctx, BaseMsg msg){
        ByteBuf byteBuf=ctx.alloc().buffer();
        MsgHeader header=msg.getHeader();
        byte[] body=null;
        int bodyLength=0;
        if(msg instanceof ProducerMsg){
            ProducerMsg producerMsg= (ProducerMsg) msg;
            body=DEFAULT_CODEC.encode(producerMsg.getProMsgInfo());
            bodyLength=body.length;
        }else if(msg instanceof ConsumerMsg){
            ConsumerMsg consumerMsg= (ConsumerMsg) msg;
            body=DEFAULT_CODEC.encode(consumerMsg.getConsMsgInfo());
            bodyLength=body.length;
        }else if(msg instanceof HeartbeatMsg){
            HeartbeatMsg heartbeatMsg= (HeartbeatMsg) msg;
            body=DEFAULT_CODEC.encode(heartbeatMsg.getHeartInfo());
            bodyLength=body.length;
        }else if(msg instanceof CustomerRegisterMsg){
            CustomerRegisterMsg registerMsg= (CustomerRegisterMsg) msg;
            body=DEFAULT_CODEC.encode(registerMsg.getTopic());
            bodyLength=body.length;
        }else {
            throw  new RuntimeException("not support msg type ,msg is >>>"+ JSONObject.toJSONString(msg));
        }
        header.setLength(bodyLength);

        byte[] headerByte=DEFAULT_CODEC.encode(header);
        //记录header的长度 方便后面取出 header 的信息
        byteBuf.writeInt(headerByte.length);
        //写入header的信息
        byteBuf.writeBytes(headerByte);
        //写入body信息
        byteBuf.writeBytes(body);

        return byteBuf;
    }

}
