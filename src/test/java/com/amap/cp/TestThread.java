package com.amap.cp;

/**
 * Created by yang.hua on 14-1-24.
 */
public class TestThread {
    private ThreadSon t = new ThreadSon();
    class ThreadSon extends Thread {
        @Override
        public void run() {
            while (true) {
                System.out.println("ggggggg");
                try {
                    Thread.sleep(1000*10);
                    System.out.println("wake ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) {
        new TestThread().t.start();
    }
}
