import java.util.concurrent.*;

public class SharedResources {
    public static final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Order> pickingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Order> packingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Order> labellingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Order> sortingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Container> containerQueue = new LinkedBlockingQueue<>(10);
    public static final BlockingQueue<Container> loadingQueue = new LinkedBlockingQueue<>();
    
    public static final Semaphore pickingStationSemaphore = new Semaphore(4);
    public static final Semaphore packingStationSemaphore = new Semaphore(1);
    public static final Semaphore labellingStationSemaphore = new Semaphore(1);
    public static final Semaphore loaderSemaphore = new Semaphore(3);
    public static final Semaphore loadingBaySemaphore = new Semaphore(2);
    
    public static volatile boolean simulationRunning = true;
    public static volatile boolean orderGenerationComplete = false;
    
    public static final Object batchLock = new Object();
    public static int currentBatchSize = 0;
    public static int nextContainerId = 1;
    public static int nextTruckId = 1;
    
    public static synchronized int getNextContainerId() {
        return nextContainerId++;
    }
    
    public static synchronized int getNextTruckId() {
        return nextTruckId++;
    }
    
    public static void logActivity(String activity) {
        String threadName = Thread.currentThread().getName();
        long timestamp = System.currentTimeMillis();
        System.out.println("[" + timestamp + "] " + activity + " (Thread: " + threadName + ")");
    }
}