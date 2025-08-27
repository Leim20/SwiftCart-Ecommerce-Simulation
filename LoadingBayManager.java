import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoadingBayManager extends Thread {
    private static final int MAX_CONTAINERS_AT_BAY = 6; // Reduced for more congestion
    private static final int MAX_CONTAINERS_PER_TRUCK = 12; // Reduced for more frequent truck dispatches
    
    private BlockingQueue<Container> bay1 = new LinkedBlockingQueue<>();
    private BlockingQueue<Container> bay2 = new LinkedBlockingQueue<>();
    private List<Container> waitingContainers = new ArrayList<>();
    
    public LoadingBayManager() {
        super("LoadingBayManager");
    }
    
    @Override
    public void run() {
        SharedResources.logActivity("LoadingBayManager: Loading bay operations started");
        
        Thread loaderThread1 = new Thread(new LoaderWorker(1, bay1));
        Thread loaderThread2 = new Thread(new LoaderWorker(2, bay1));
        Thread loaderThread3 = new Thread(new LoaderWorker(3, bay2));
        
        SharedResources.logActivity("LoadingBayManager: Assigning Loader-1 and Loader-2 to Bay-1");
        SharedResources.logActivity("LoadingBayManager: Assigning Loader-3 to Bay-2");
        
        loaderThread1.start();
        loaderThread2.start();
        loaderThread3.start();
        
        while (SharedResources.simulationRunning) {
            try {
                Container container = SharedResources.containerQueue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                
                if (container != null) {
                    synchronized (this) {
                        int totalContainers = bay1.size() + bay2.size() + waitingContainers.size();
                        
                        if (totalContainers >= MAX_CONTAINERS_AT_BAY) {
                            SharedResources.logActivity("Supervisor: Dispatch paused. " + totalContainers + " containers at bay – waiting for truck");
                            waitingContainers.add(container);
                        } else {
                            // Enhanced load balancing between bays - Round robin approach
                            // Use container ID to alternate between bays for better distribution
                            if (container.getContainerId() % 2 == 1) {
                                bay1.offer(container);
                                SharedResources.logActivity("LoadingBayManager: Container #" + container.getContainerId() + " assigned to Bay-1 (" + (bay1.size()) + " containers)");
                            } else {
                                bay2.offer(container);
                                SharedResources.logActivity("LoadingBayManager: Container #" + container.getContainerId() + " assigned to Bay-2 (" + (bay2.size()) + " containers)");
                            }
                        }
                    }
                } else if (SharedResources.orderGenerationComplete && 
                          SharedResources.containerQueue.isEmpty() && 
                          SharedResources.sortingQueue.isEmpty() && 
                          SharedResources.labellingQueue.isEmpty() && 
                          SharedResources.packingQueue.isEmpty() && 
                          SharedResources.orderQueue.isEmpty()) {
                    // Wait longer for final containers to be processed (5m53s version)
                    Thread.sleep(3000);
                    if (bay1.isEmpty() && bay2.isEmpty() && waitingContainers.isEmpty()) {
                        SharedResources.simulationRunning = false;
                        break;
                    }
                }
                
                redistributeWaitingContainers();
                
            } catch (InterruptedException e) {
                SharedResources.logActivity("LoadingBayManager: Interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        try {
            loaderThread1.join();
            loaderThread2.join();
            loaderThread3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        SharedResources.logActivity("LoadingBayManager: Loading bay operations finished");
    }
    
    private synchronized void redistributeWaitingContainers() {
        while (!waitingContainers.isEmpty()) {
            int totalContainers = bay1.size() + bay2.size();
            if (totalContainers < MAX_CONTAINERS_AT_BAY) {
                Container container = waitingContainers.remove(0);
                // Use same round-robin logic for waiting containers
                if (container.getContainerId() % 2 == 1) {
                    bay1.offer(container);
                    SharedResources.logActivity("LoadingBayManager: Waiting Container #" + container.getContainerId() + " moved to Bay-1 (" + (bay1.size()) + " containers)");
                } else {
                    bay2.offer(container);
                    SharedResources.logActivity("LoadingBayManager: Waiting Container #" + container.getContainerId() + " moved to Bay-2 (" + (bay2.size()) + " containers)");
                }
            } else {
                break;
            }
        }
    }
    
    private class LoaderWorker implements Runnable {
        private int loaderId;
        private BlockingQueue<Container> assignedBay;
        
        public LoaderWorker(int loaderId, BlockingQueue<Container> bay) {
            this.loaderId = loaderId;
            this.assignedBay = bay;
        }
        
        @Override
        public void run() {
            Thread.currentThread().setName("Loader-" + loaderId);
            SharedResources.logActivity("Loader-" + loaderId + ": Started operations");
            
            while (SharedResources.simulationRunning) {
                try {
                    // Random loader breakdown simulation (high probability for demonstration)
                    if (Math.random() < 0.25) { // Increased to 25% for guaranteed breakdowns
                        long repairTime = 2000 + (long)(Math.random() * 4000); // 2-6s repair time
                        SharedResources.logActivity("Loader-" + loaderId + ": *** BREAKDOWN DETECTED *** - undergoing repairs for " + (repairTime/1000) + " seconds");
                        Thread.sleep(repairTime);
                        SharedResources.logActivity("Loader-" + loaderId + ": Repairs completed - resuming operations");
                        continue;
                    }
                    
                    SharedResources.loaderSemaphore.acquire();
                    
                    List<Container> truckLoad = new ArrayList<>();
                    int bayNumber = (assignedBay == bay1) ? 1 : 2;
                    
                    // More patient loading for better truck efficiency but slower operations
                    int emptyPolls = 0;
                    int maxWaitPolls = (SharedResources.orderGenerationComplete) ? 4 : 12; // More patient to create congestion
                    
                    // Simulate slower loading operations during peak times
                    if (truckLoad.isEmpty() && Math.random() < 0.4) {
                        SharedResources.logActivity("Loader-" + loaderId + ": Slow loading conditions - adjusting operations");
                        Thread.sleep(1000 + (int)(Math.random() * 2000)); // 1-3s delay
                    }
                    
                    while (truckLoad.size() < MAX_CONTAINERS_PER_TRUCK && emptyPolls < maxWaitPolls) {
                        Container container = assignedBay.poll(5, java.util.concurrent.TimeUnit.SECONDS);
                        if (container != null) {
                            truckLoad.add(container);
                            SharedResources.logActivity("Loader-" + loaderId + ": Moving Container #" + container.getContainerId() + " to Loading Bay-" + bayNumber);
                            emptyPolls = 0; // Reset counter when container found
                        } else {
                            emptyPolls++;
                            // If order generation is complete and we have some containers, dispatch truck
                            if (SharedResources.orderGenerationComplete && !truckLoad.isEmpty()) {
                                break;
                            }
                            // If we have some containers but not full, try a bit longer
                            if (truckLoad.size() > 0 && truckLoad.size() < 3) {
                                emptyPolls = Math.max(0, emptyPolls - 1); // Be more patient with partial loads
                            }
                        }
                    }
                    
                    if (!truckLoad.isEmpty()) {
                        SharedResources.loadingBaySemaphore.acquire();
                        
                        TruckThread truck = new TruckThread(SharedResources.getNextTruckId(), truckLoad, bayNumber);
                        truck.start();
                        
                        try {
                            truck.join();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        
                        SharedResources.loadingBaySemaphore.release();
                    } else if (SharedResources.orderGenerationComplete && assignedBay.isEmpty()) {
                        // No more containers to process
                        SharedResources.loaderSemaphore.release();
                        break;
                    }
                    
                    SharedResources.loaderSemaphore.release();
                    
                } catch (InterruptedException e) {
                    SharedResources.logActivity("Loader-" + loaderId + ": Interrupted");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            SharedResources.logActivity("Loader-" + loaderId + ": Finished operations");
        }
    }
}