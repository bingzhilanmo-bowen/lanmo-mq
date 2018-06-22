package com.lanmo.mq.etcd;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import mousio.client.ConnectionState;
import mousio.client.promises.ResponsePromise;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author bingzhilanmo@gmail.com
 * @version 1.0
 * @desc
 * @date 2017/7/5
 */
@Slf4j
public class EtcdUtils {



    private static String ETCD_URIS;

    private static EtcdClient etcdClient;

    static {
         //去读取配置文件--简单点写死在这里
        ETCD_URIS="http://172.16.92.162:2379";
        String[] servers=ETCD_URIS.split(",");
        log.info("etcd servers is {}", JSONObject.toJSONString(servers));
        URI[] uris=new URI[servers.length];

        for (Integer i=0;i<servers.length;i++)
            uris[i]=URI.create(servers[i]);

        etcdClient=new EtcdClient(uris);
        log.info("etcd version is {}",etcdClient.version().getServer());
    }

    /**
     * 普通的添加
     * @param key
     * @param value
     */
    public static void put(String key,String value){
       if(StringUtils.isBlank(key)||StringUtils.isBlank(value)){
           log.error(" key or value is not null");
           return;
       }
        try {
            etcdClient.put(key,value).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置存活时间的
     * @param key
     * @param value
     * @param ttl 存活时间  seconds
     */
    public static void put(String key,String value,Integer ttl){
        if(StringUtils.isBlank(key)||StringUtils.isBlank(value)){
            log.error(" key or value is not null");
            return;
        }
        try {
            etcdClient.put(key,value).ttl(ttl).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void putDir(String dir){
        try {
            etcdClient.putDir(dir).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key){

        try {
            EtcdResponsePromise<EtcdKeysResponse>  promise= etcdClient.get(key).send();
            return promise.get().node.getValue();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (EtcdException e) {
            e.printStackTrace();
            return null;
        } catch (EtcdAuthenticationException e) {
            e.printStackTrace();
            return null;
        } catch (TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 只支持一级的目录 不支持多级目录递归
     * @param dir
     * @return
     */
    public static List<String> getDir(String dir){
        List<String> childValues=new ArrayList<String>();
        try {
            EtcdResponsePromise<EtcdKeysResponse>  recursive=  etcdClient.getDir(dir).recursive().send();
               //获得目录
                EtcdKeysResponse.EtcdNode etcdNode= recursive.get().node;
                if(etcdNode.dir){
                    //获得子目录
                     List<EtcdKeysResponse.EtcdNode> childNodes= etcdNode.nodes;
                     for (EtcdKeysResponse.EtcdNode node:childNodes){
                        if(!node.dir){
                            childValues.add(node.getValue());
                        }
                     }
                }else {
                    log.error("this dir {},is not dir",dir);
                }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (EtcdException e) {
            e.printStackTrace();
        } catch (EtcdAuthenticationException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return childValues;
    }

    /**
     * 因为etcd4j的监听只能，监听一次所以需要，在一次监听完成后，再新加一次监听，以达到次序监听的目的
     * @param key
     * @param handler
     */
   public static void addListenner(String key,ListennerHandler handler){
       EtcdResponsePromise promise = null;
       try {
           promise = etcdClient.get(key).waitForChange().send();
           EtcdResponsePromise finalPromise = promise;
           promise.addListener((response)->{
               handler.action((EtcdKeysResponse) response.getNow());
              //监听完成后添加新的监听
               addListenner(key,handler);
           });

       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    /**
     * 删除熟悉
     * @param key
     */
   public static void delete(String key){
       try {
           etcdClient.delete(key).send();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }


    /**
     * 删除目录，并删除目录下的所有子节点
     * @param dir
     */
    public static void deleteDir(String dir){
        try {
            etcdClient.deleteDir(dir).recursive().send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
       // put("test","bobo test etcd",30);
       // System.out.print(get("test"));
          // System.out.print(JSONObject.toJSONString(getDir("test_dir")));
        /*putDir("test_dir");

        put("test_dir/node1","test1");
        put("test_dir/node2","test2");
        put("test_dir/node3","test3");*/
           /* addListenner("foo",(response)->{
               System.out.print( response.node.getValue());
            });*/


    }
}
