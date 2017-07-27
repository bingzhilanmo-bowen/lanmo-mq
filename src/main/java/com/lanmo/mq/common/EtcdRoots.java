package com.lanmo.mq.common;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/7/27
 */
public class EtcdRoots {


    /**
     * 获得topic的根目录
     * @param topic
     * @return
     */
    public static String topicDir(String topic){
        return "/etcd/lanmo/mq/"+topic;
    }


    /**
     * 获得注册某个topic消费信息的 目录
     * @param topic
     * @param ip
     * @return
     */
    public static String registerDir(String topic,String ip){
        return "/etcd/lanmo/mq/"+topic+"/"+ip;
    }

}
