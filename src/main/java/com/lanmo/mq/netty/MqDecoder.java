package com.lanmo.mq.netty;

import com.alibaba.fastjson.JSONObject;
import com.lanmo.mq.netty.code.Codec;
import com.lanmo.mq.netty.code.impl.JsonCodec;
import com.lanmo.mq.netty.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/6
 */
@Slf4j
public class MqDecoder extends ByteToMessageDecoder {

    //默认解码
    private Codec DEFAULT_CODEC=new JsonCodec();

    //魔数的长度
    private int magicLength = 2;

    //消息体总长度的标识的长度
    private int contentAllLengthByteLength = 4;

    //消息header中header长度标识的长度
    private int contentHeaderLengthByteLength = 4;

    //非消息体的长度 2 + 4
    private int unBodySliceStartLength = 6;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //log.info("receive msg is {}",in.toString(CharsetUtil.UTF_8));
        //消息最少也要 大于6的长度
        if(in.readableBytes()>=unBodySliceStartLength){
             //防止socket字节流攻击
            // 防止，客户端传来的数据过大
            // 因为，太大的数据，是不合理的
            if (in.readableBytes() > 2048) {
                in.skipBytes(in.readableBytes());
            }
            // 记录包头开始的index
            int beginReader;


            while (true){
                // 获取包头开始的index
                beginReader = in.readerIndex();
                byte[] magicNumDst=new byte[2];
                in.readBytes(magicNumDst);
                //堵到了协议头 结束while循环
                if(magicNumDst[0]==MsgDefinition.MAGIC_CODE_BYTE[0]&&magicNumDst[1]==MsgDefinition.MAGIC_CODE_BYTE[1]){
                     break;
                }

                // 未读到包头，略过一个字节
                // 每次略过，一个字节，去读取，包头信息的开始标记
                in.resetReaderIndex();
                in.readByte();

                // 当略过，一个字节之后，
                // 数据包的长度，又变得不满足
                // 此时，应该结束。等待后面的数据到达
                if (in.readableBytes() < unBodySliceStartLength) {
                    return;
                }
            }
            //因为前两位是 魔数 所以标识 整个body长度的是 从readerIndex+magicLength位置 开始读取的
            int contentAllLength=(int) in.readUnsignedInt();
            //减去 contentAllLengthByteLength 所占用的长度 就是body的真实长度（这四个长度 是用来 记录后面真实body的长度的 结合encoder 就明白了）
            int contentLength = contentAllLength - contentAllLengthByteLength;

            // frameLength 整个消息应该的长度
            int frameLength = contentLength + contentAllLengthByteLength;

            log.info("contentAllLength {},contentLength {},frameLength {},read length {}",contentAllLength,contentLength,frameLength,in.readableBytes());
            if (frameLength > in.readableBytes()) {
                //数据还不够,返回
                in.readerIndex(beginReader); // 还原读指针
                return;
            }

            if (frameLength < unBodySliceStartLength) {
                in.skipBytes(frameLength);
                throw new Exception("error!!!,other thread read this msg");
            }
            BaseMsg msg = decodeMsg(in, contentLength);
            if (msg != null) {
                log.info("get message success");
                log.info("receive decoder msg is {}",JSONObject.toJSONString(msg));
                out.add(msg);
                /*if(in.readableBytes()==0){
                    in.release();
                }*/
            }
        }
    }

    private BaseMsg decodeMsg(ByteBuf in, int contentLength){
        //得到消息的真实buf
        ByteBuf realBuf=in.readBytes(contentLength);
        int headerLength=realBuf.readInt();
        log.info("header length is {}",headerLength);
        ByteBuf headerBuf=realBuf.slice(realBuf.readerIndex(),headerLength);
        byte[] header=new byte[headerLength];
        headerBuf.readBytes(header);
        MsgHeader msgHeader=DEFAULT_CODEC.decode(header,MsgHeader.class);
        log.info("header info is {}", JSONObject.toJSONString(msgHeader));

        BaseMsg baseMsg = ensureMsg(realBuf, msgHeader, headerLength + contentHeaderLengthByteLength);
        //计算器-1，-1后如果计算器和初始值1相同，则会释放内存
        return baseMsg;

    }

    private BaseMsg ensureMsg(ByteBuf in, MsgHeader msgHeader, int start) {

        if(msgHeader.getMsgType()== MsgType.PRODUCER.getValue()){
           //生产者的消息
            ProducerMsg producerMsg=new ProducerMsg();
            producerMsg.setSendTime(System.currentTimeMillis());
            producerMsg.setHeader(msgHeader);
            producerMsg.setMsgBody(in.slice(start,msgHeader.getLength()));
            int bodyLength=msgHeader.getLength();
            if(bodyLength>0){
                byte[] body=new byte[bodyLength];
                producerMsg.getMsgBody().readBytes(body);
                ProMsgInfo proMsgInfo=DEFAULT_CODEC.decode(body,ProMsgInfo.class);
                producerMsg.setProMsgInfo(proMsgInfo);
            }
            log.info("receive producer msg is {}",JSONObject.toJSONString(producerMsg));
            return producerMsg;
        }else if(msgHeader.getMsgType()== MsgType.CONSUMER.getValue()){
            //消费者的消息
            ConsumerMsg consumerMsg=new ConsumerMsg();
            consumerMsg.setReceiveTime(System.currentTimeMillis());
            consumerMsg.setHeader(msgHeader);
            int bodyLength=msgHeader.getLength();
            consumerMsg.setMsgBody(in.slice(start,bodyLength));
            if(bodyLength>0){
                byte[] body=new byte[bodyLength];
                consumerMsg.getMsgBody().readBytes(body);
                ConsMsgInfo consMsgInfo=DEFAULT_CODEC.decode(body,ConsMsgInfo.class);
                consumerMsg.setConsMsgInfo(consMsgInfo);
            }
            log.info("receive consumer msg is {}",JSONObject.toJSONString(consumerMsg));
           return consumerMsg;
        }else if(msgHeader.getMsgType()== MsgType.HEARTBEAT.getValue()){
            HeartbeatMsg heartbeatMsg=new HeartbeatMsg();
            heartbeatMsg.setHeartTime(System.currentTimeMillis());
            heartbeatMsg.setHeader(msgHeader);
            int bodyLength=msgHeader.getLength();
            heartbeatMsg.setMsgBody(in.slice(start,bodyLength));
            if(bodyLength>0){
                byte[] body=new byte[bodyLength];
                heartbeatMsg.getMsgBody().readBytes(body);
                String string=DEFAULT_CODEC.decode(body,String.class);
                heartbeatMsg.setHeartInfo(string);
            }
            log.info("receive heart msg is {}",JSONObject.toJSONString(heartbeatMsg));
            return heartbeatMsg;
        }else if(msgHeader.getMsgType()== MsgType.REGISTER.getValue()){
            //注册topic的消息
            CustomerRegisterMsg registerMsg=new CustomerRegisterMsg();
            registerMsg.setRegisterTime(System.currentTimeMillis());
            registerMsg.setHeader(msgHeader);
            int bodyLength=msgHeader.getLength();
            registerMsg.setMsgBody(in.slice(start,bodyLength));
            if(bodyLength>0){
                byte[] body=new byte[bodyLength];
                registerMsg.getMsgBody().readBytes(body);
                String topic=DEFAULT_CODEC.decode(body,String.class);
                registerMsg.setTopic(topic);
            }
            log.info("receive register msg is {}",JSONObject.toJSONString(registerMsg));
            return registerMsg;
        }else {
            throw new RuntimeException("not support msg type");
        }
    }

}
