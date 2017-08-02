package com.lanmo.mq.delay;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author bo5.wang@56qq.com
 * @version 1.0
 * @desc
 * @date 2017/8/1
 */
public class DelayPush {

    /**
     * 队列
     */
   private static final DelayQueue<DelayTask> queue = new DelayQueue<DelayTask>();

   private static final ExecutorService exec = Executors.newCachedThreadPool();

   private static volatile Boolean start=false;

    public static void add(int delayInMilliseconds, String topic, String tag, String content, Integer consumptionType){
        if(!start){
            run();
            start=true;
        }
       queue.offer(new DelayTask(delayInMilliseconds,topic,tag,content,consumptionType));
   }

   public static void run(){
       exec.execute(new DelayedTaskConsumer(queue));
   }

    //定义使用整个延迟队列的任务类
  public static class DelayedTaskConsumer implements Runnable{
        private DelayQueue<DelayTask> queue;

        public DelayedTaskConsumer(DelayQueue<DelayTask> queue){
            this.queue = queue;
           }
        public void run(){
            try{
               while (true){
                   queue.take().run();
               }
            }catch(InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }


}
