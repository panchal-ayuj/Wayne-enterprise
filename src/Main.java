import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final PC pc = new PC();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pc.produce(new Integer(3));
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pc.consume();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

    public static class PC<T> {
        LinkedList<T> list = new LinkedList<>();
        int capacity = 2;

        public void produce(T obj) throws InterruptedException {
//            T obj = (T) new Object();
            while (true) { // Because we need to produce till the time the code is running
                synchronized (this) {
                    while (list.size() == capacity) {
                        wait();
                    }
                    System.out.println("Produced: " + obj);
                    list.add(obj);
                    notify();
                    Thread.sleep(1000);
                }
            }
        }

        public void consume() throws InterruptedException {
            while (true) {
                synchronized (this) {
                    while (list.size() == 0) {
                        wait();
                    }
                    T obj = list.removeFirst();
                    System.out.println("Consumed: " + obj);
                    notify();
                    Thread.sleep(1000);
                }
            }
        }
    }
}