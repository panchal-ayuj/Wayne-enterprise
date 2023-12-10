import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


class Order {
    static Random random = new Random();
    int cargoWeight;
    String destination;
    long creationTime;

    Order(){
        this.creationTime = System.currentTimeMillis();
        this.cargoWeight = random.nextInt(41) + 10;
        this.destination = random.nextBoolean() ? "Gotham" : "Atlanta";
    }
}

class Ship {
    static int LowerCargoLimit = 50;
    static int UpperCargoLimit = 300;
    static int TripCost = 1000;
    static int CancelCost = 250;
    static long MaxWaitTime = 60000;

    int cargo;
    int trips;

    Ship(){
        this.cargo = 0;
        this.trips = 0;
    }

    public void makeTrip(Order order) {
        if (Main.totalCost >= Main.RevenueRequired){
            return;
        }

        if (shouldCancelOrder(order.creationTime)) {
            Main.orderBlockingDeque.remove(order);
            System.out.println("Order canceled");
            synchronized (this) {
                Main.totalCost += CancelCost;
                Main.totalOrdersCanceled++;
            }
            return;
        }

        cargo += order.cargoWeight;
        synchronized (this) {
            Main.totalCost += TripCost;
            Main.totalOrdersDelivered++;
        }

        if (cargo < LowerCargoLimit) {
            System.out.println("Waiting for cargo.");
            return;
        }

        cargo = 0;
        String destination = order.destination;

        System.out.println("Shipment to " + destination);
        System.out.println("Total cost: " + Main.totalCost);
        trips++;

        if(trips%5 == 0){
            System.out.println("Ship in maintenance for 1 minute");
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void pickupCargo(int cargoWeight) {
        cargo += cargoWeight;
    }

    public int getTrips(){
        return trips;
    }

    public boolean shouldCancelOrder(long orderCreationTime) {
        return System.currentTimeMillis() - orderCreationTime > MaxWaitTime;
    }
}

public class Main {
    static int RevenueRequired = 400000;
    static BlockingDeque<Order> orderBlockingDeque = new LinkedBlockingDeque<>();
    static Ship[] ships = new Ship[5];

    static int totalCost = 0;
    static int totalOrdersDelivered = 0;
    static int totalOrdersCanceled = 0;
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i<5; i++){
            ships[i] = new Ship();
            new Thread(new ShippingThread(ships[i])).start();
        }

        for (int i=0; i<7; i++){
            new Thread(new ConsumerThread()).start();
        }
    }

    static class ConsumerThread implements Runnable {
        @Override
        public void run() {
            while (totalCost < RevenueRequired){
                try {
                    Thread.sleep(4000);
                    orderBlockingDeque.put(new Order());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ShippingThread implements  Runnable {
        Ship ship;

        public ShippingThread(Ship ship) {
            this.ship = ship;
        }

        @Override
        public void run() {
            while (totalCost < RevenueRequired) {
                try {
                    Order order = orderBlockingDeque.take();
                    ship.makeTrip(order);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Total orders delivered " + totalOrdersDelivered);
            System.out.println("Total orders canceled " + totalOrdersCanceled);
        }
    }
}