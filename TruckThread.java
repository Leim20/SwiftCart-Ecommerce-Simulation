import java.util.List;

public class TruckThread extends Thread {
    private int truckId;
    private List<Container> containers;
    private int bayNumber;
    private long arrivalTime;
    private long loadingStartTime;
    private long departureTime;
    
    public TruckThread(int truckId, List<Container> containers, int bayNumber) {
        super("Truck-" + truckId);
        this.truckId = truckId;
        this.containers = containers;
        this.bayNumber = bayNumber;
        this.arrivalTime = System.currentTimeMillis();
    }
    
    @Override
    public void run() {
        try {
            // Simulate realistic truck arrival patterns and congestion
            // Trucks often arrive in clusters causing congestion
            if (Math.random() < 0.6) { // 60% chance of waiting (increased)
                long extraWaitTime = 1000 + (long)(Math.random() * 3000); // 1-4s wait time
                SharedResources.logActivity("Truck-" + truckId + ": Waiting for loading bay availability...");
                Thread.sleep(extraWaitTime);
            }
            
            // Additional congestion simulation - some trucks wait longer
            if (truckId > 2 && Math.random() < 0.4) { // Later trucks more likely to wait
                long congestionDelay = 2000 + (long)(Math.random() * 4000); // 2-6s additional wait
                SharedResources.logActivity("Truck-" + truckId + ": Traffic congestion detected - extended wait period");
                Thread.sleep(congestionDelay);
            }
            
            long waitingTime = System.currentTimeMillis() - arrivalTime;
            Statistics.getInstance().addTruckWaitingTime(waitingTime);
            if (waitingTime > 100) {
                SharedResources.logActivity("Truck-" + truckId + ": Total waiting time: " + waitingTime + "ms");
            }
            
            loadingStartTime = System.currentTimeMillis();
            SharedResources.logActivity("Truck-" + truckId + ": Started loading at Bay-" + bayNumber);
            
            // More realistic loading times with variable delays
            int baseLoadingTime = 200 + (int)(Math.random() * 300); // 200-500ms per container
            int totalLoadingTime = containers.size() * baseLoadingTime;
            
            // Add random delays during loading (equipment issues, safety checks, etc.)
            if (Math.random() < 0.4) {
                int additionalDelay = 1000 + (int)(Math.random() * 2000); // 1-3s additional delay
                totalLoadingTime += additionalDelay;
                SharedResources.logActivity("Truck-" + truckId + ": Loading delay due to safety inspection");
            }
            
            Thread.sleep(totalLoadingTime);
            
            departureTime = System.currentTimeMillis();
            long actualLoadingTime = departureTime - loadingStartTime;
            
            Statistics.getInstance().addTruckLoadingTime(actualLoadingTime);
            Statistics.getInstance().incrementTrucksDispatched();
            
            if (containers.size() >= 12) {
                SharedResources.logActivity("Truck-" + truckId + ": Fully loaded with " + containers.size() + " containers. Departing to Distribution Centre");
            } else {
                SharedResources.logActivity("Truck-" + truckId + ": Loaded with " + containers.size() + " containers. Departing to Distribution Centre");
            }
            
        } catch (InterruptedException e) {
            SharedResources.logActivity("Truck-" + truckId + ": Loading interrupted");
            Thread.currentThread().interrupt();
        }
    }
    
    public long getLoadingTime() {
        if (departureTime > 0 && loadingStartTime > 0) {
            return departureTime - loadingStartTime;
        }
        return 0;
    }
    
    public long getWaitingTime() {
        if (loadingStartTime > 0) {
            return loadingStartTime - arrivalTime;
        }
        return 0;
    }
}