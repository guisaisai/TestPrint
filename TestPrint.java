import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author owell
 * @date 2019/3/7 09:43
 */
public class TestPrint {
    int max = 10000;
    long start = 0;
    CountDownLatch countDownLatch = null;

    boolean printResult = true;
    public void printResult(Object obj){
        if(printResult){
            System.out.print(",");
            System.out.print(Thread.currentThread().getName() + ":" + obj);
        }
    }

    //@Before
    public void before() {
        countDownLatch = new CountDownLatch(2);
        start = System.currentTimeMillis();
    }

    //@After
    public void after() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println();
        System.out.println("总耗时:" + (end - start) + "毫秒");
        System.out.println();
    }

    volatile boolean isAPrint = true;

    //@Test
    public void testUnLock() throws Exception {
        System.out.println();
        System.out.print("unlock:");
        final int[] ii = {1};
        Thread a = new Thread("T1") {
            @Override
            public void run() {
                while (ii[0] <= max) {
                    if (isAPrint) {
                        if (ii[0] % 2 != 0) {
                            printResult(ii[0]);
                            ii[0]++;
                            isAPrint = false;
                        }
                    }
                }

                countDownLatch.countDown();
            }
        };

        Thread b = new Thread("T2") {
            @Override
            public void run() {
                while (ii[0] <= max) {
                    if (!isAPrint) {
                        if (ii[0] % 2 == 0) {
                            printResult(ii[0]);
                            ii[0]++;
                            isAPrint = true;
                        }
                    }
                }

                countDownLatch.countDown();
            }
        };

        a.start();
        b.start();
    }

    //@Test
    public void testLock() {
        System.out.print("ReentrantLock:");
        //可重入锁
        ReentrantLock lock = new ReentrantLock();
        //Condition信号1
        Condition c1 = lock.newCondition();
        //Condition信号2
        Condition c2 = lock.newCondition();
        AtomicInteger ii = new AtomicInteger(1);
        Thread t1 = new Thread("T1") {
            public void run() {
                while (ii.get() <= max) {
                    lock.lock();
                    try {
                        printResult(ii.getAndAdd(1));
                        c2.signal();//c2将线程2从阻塞等待->唤醒状态
                        c1.await();//c1将线程1从运行状态->阻塞等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }

                countDownLatch.countDown();

                lock.lock();
                try{
                    c2.signal();//c1将线程1从阻塞等待->唤醒状态
                } finally {
                    lock.unlock();
                }
            }
        };
        Thread t2 = new Thread("T2") {
            public void run() {
                while (ii.get() <= max) {
                    lock.lock();
                    try {
                        printResult(ii.getAndAdd(1));
                        c1.signal();//c1将线程1从阻塞等待->唤醒状态
                        c2.await();//c2将线程2从运行状态->阻塞等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }

                countDownLatch.countDown();
                lock.lock();
                try{
                    c1.signal();//c1将线程1从阻塞等待->唤醒状态
                } finally {
                    lock.unlock();
                }
            }
        };

        //线程t1启动
        t1.start();
        while (ii.get() == 0) {//保证t1先执行
            ;
        }
        //线程t2启动
        t2.start();
    }

    public void testSync() {
        System.out.print("sync:");
        final int[] i = {1};
        // 使用匿名内部类的形式，没创建runnable对象
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (i[0] <= max) {
                    // 上锁当前对象
                    synchronized (this) {
                        // 唤醒另一个线程
                        notify();
                        printResult(i[0]++);
                        try {
                            // 释放掉锁
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                countDownLatch.countDown();

                // 上锁当前对象
                synchronized (this) {
                    notify();
                }

            }
        };

        Thread t1 = new Thread(runnable);
        t1.setName("T1");
        Thread t2 = new Thread(runnable);
        t2.setName("T2");
        t1.start();
        t2.start();
    }

    public static void main(String[] args) throws Exception {
        int cycle = 10;

        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println("测试打印一次start");
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();

        TestPrint t1 = new TestPrint();
        t1.before();
        t1.testUnLock();
        t1.after();

        TestPrint t2 = new TestPrint();
        t2.before();
        t2.testSync();
        t2.after();

        TestPrint t3 = new TestPrint();
        t3.before();
        t3.testLock();
        t3.after();

        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println("测试打印一次end");
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();


        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println("正式打印start...");
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();

        int i = 0;

        i = 0;
        while(i<cycle){
            TestPrint p = new TestPrint();
            p.before();
            p.testUnLock();
            p.after();
            i++;
        }

        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println("换人...");
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();

        i = 0;
        while(i<cycle){
            TestPrint p = new TestPrint();
            p.before();
            p.testSync();
            p.after();
            i++;
        }


        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println("再换人...");
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();

        i = 0;
        while(i<cycle){
            TestPrint p = new TestPrint();
            p.before();
            p.testLock();
            p.after();
            i++;
        }


        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println("正式打印end...");
        System.err.println();
        System.err.println();
        System.err.println();
        System.err.println();
    }
}
