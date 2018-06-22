package com.lanmo.mq.etcd;

import mousio.etcd4j.responses.EtcdKeysResponse;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc
 * @date 2017/7/6
 */
public interface ListennerHandler {

    /**
     * 完成自己的处理逻辑
     * @param etcdKeysResponse
     */
    public void action(EtcdKeysResponse etcdKeysResponse);

}
