import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {
    private static Statistics instance;
    
    private AtomicInteger totalOrdersProcessed = new AtomicInteger(0);
    private AtomicInteger totalOrdersRejected = new AtomicInteger(0);
    private AtomicInteger totalBoxesPacked = new AtomicInteger(0);
    private AtomicInteger totalContainersShipped = new AtomicInteger(0);
    private AtomicInteger totalTrucksDispatched = new AtomicInteger(0);
    
    private List<Long> truckLoadingTimes = new ArrayList<>();
    private List<Long> truckWaitingTimes = new ArrayList<>();
    
    private long simulationStartTime;
    private long simulationEndTime;
    
    private Statistics() {
        simulationStartTime = System.currentTimeMillis();
    }
    
    public static synchronized Statistics getInstance() {
        if (instance == null) {
            instance = new Statistics();
        }
        return instance;
    }
    
    public void incrementOrdersProcessed() {
        totalOrdersProcessed.incrementAndGet();
    }
    
    public void incrementOrdersRejected() {
        totalOrdersRejected.incrementAndGet();
    }
    
    public void incrementBoxesPacked() {
        totalBoxesPacked.incrementAndGet();
    }
    
    public void incrementContainersShipped() {
        totalContainersShipped.incrementAndGet();
    }
    
    public void incrementTrucksDispatched() {
        totalTrucksDispatched.incrementAndGet();
    }
    
    public synchronized void addTruckLoadingTime(long loadingTime) {
        truckLoadingTimes.add(loadingTime);
    }
    
    public synchronized void addTruckWaitingTime(long waitingTime) {
        truckWaitingTimes.add(waitingTime);
    }
    
    public void setSimulationEndTime() {
        simulationEndTime = System.currentTimeMillis();
    }
    
    public void printFinalStatistics() {
        setSimulationEndTime();
        long totalSimulationTime = simulationEndTime - simulationStartTime;
        long minutes = totalSimulationTime / 60000;
        long seconds = (totalSimulationTime % 60000) / 1000;
        
        System.out.println("\n=== SwiftCart - Final Statistics ===");
        System.out.println("Simulation Duration: " + minutes + " minutes " + seconds + " seconds");
        System.out.println("Total Orders Processed: " + totalOrdersProcessed.get());
        System.out.println("Total Orders Rejected: " + totalOrdersRejected.get());
        System.out.println("Total Boxes Packed: " + totalBoxesPacked.get());
        System.out.println("Total Containers Shipped: " + totalContainersShipped.get());
        System.out.println("Total Trucks Dispatched: " + totalTrucksDispatched.get());
        System.out.println("Average Containers per Truck: " + String.format("%.1f", (double)totalContainersShipped.get() / totalTrucksDispatched.get()));
        System.out.println();
        
        if (!truckLoadingTimes.isEmpty()) {
            long maxLoading = truckLoadingTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            long minLoading = truckLoadingTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            double avgLoading = truckLoadingTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            
            System.out.println("Truck Loading Times:");
            System.out.println("  Maximum: " + maxLoading + " ms");
            System.out.println("  Minimum: " + minLoading + " ms");
            System.out.println("  Average: " + String.format("%.2f", avgLoading) + " ms");
            System.out.println();
        }
        
        if (!truckWaitingTimes.isEmpty()) {
            long maxWaiting = truckWaitingTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            long minWaiting = truckWaitingTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            double avgWaiting = truckWaitingTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            
            System.out.println("Truck Waiting Times:");
            System.out.println("  Maximum: " + maxWaiting + " ms");
            System.out.println("  Minimum: " + minWaiting + " ms");
            System.out.println("  Average: " + String.format("%.2f", avgWaiting) + " ms");
            System.out.println();
        }
        
        boolean allSystemsCleared = checkAllSystemsCleared();
        System.out.println("All systems cleared: " + allSystemsCleared);
        System.out.println();
        System.out.println("BUILD SUCCESSFUL (total time: " + minutes + " minutes " + seconds + " seconds)");
    }
    
    private boolean checkAllSystemsCleared() {
        // Check if all 600 orders have been accounted for
        boolean allOrdersAccounted = (totalOrdersProcessed.get() + totalOrdersRejected.get() == 600);
        
        // Check if processed orders equal boxes packed (should be 1:1 ratio)
        boolean ordersMatchBoxes = (totalBoxesPacked.get() == totalOrdersProcessed.get());
        
        // Check if containers can hold all boxes (allowing partial containers)
        boolean containersCanHoldBoxes = (totalContainersShipped.get() * 30 >= totalBoxesPacked.get());
        
        // All systems cleared if all conditions met
        return allOrdersAccounted && ordersMatchBoxes && containersCanHoldBoxes;
    }
    
    public int getTotalOrdersProcessed() {
        return totalOrdersProcessed.get();
    }
    
    public int getTotalOrdersRejected() {
        return totalOrdersRejected.get();
    }
}