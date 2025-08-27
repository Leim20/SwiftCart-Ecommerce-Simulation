import java.util.ArrayList;
import java.util.List;

public class SortingAreaThread extends Thread {
    private List<Order> currentBatch;
    private List<List<Order>> completedBatches;
    private static final int BATCH_SIZE = 6;
    private static final int BATCHES_PER_CONTAINER = 5;
    private static final int CONTAINER_CAPACITY = 30;
    private int batchNumber = 1;
    
    public SortingAreaThread() {
        super("Sorter-1");
        this.currentBatch = new ArrayList<>();
        this.completedBatches = new ArrayList<>();
    }
    
    @Override
    public void run() {
        SharedResources.logActivity("Sorter: Sorting area started");
        
        while (SharedResources.simulationRunning) {
            try {
                Order order = SharedResources.sortingQueue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                
                if (order != null) {
                    synchronized (SharedResources.batchLock) {
                        currentBatch.add(order);
                        SharedResources.logActivity("Sorter: Added Order #" + order.getOrderId() + " to Batch #" + batchNumber);
                        
                        if (currentBatch.size() >= BATCH_SIZE) {
                            completeBatch();
                        }
                    }
                } else if (SharedResources.orderGenerationComplete && 
                          SharedResources.sortingQueue.isEmpty() && 
                          SharedResources.labellingQueue.isEmpty() && 
                          SharedResources.packingQueue.isEmpty() && 
                          SharedResources.orderQueue.isEmpty()) {
                    
                    synchronized (SharedResources.batchLock) {
                        if (!currentBatch.isEmpty()) {
                            completeBatch();
                        }
                        if (!completedBatches.isEmpty()) {
                            createContainer();
                        }
                    }
                    break;
                }
                
            } catch (InterruptedException e) {
                SharedResources.logActivity("Sorter: Interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        SharedResources.logActivity("Sorter: Sorting area finished");
    }
    
    private void completeBatch() {
        // Complete current batch and add to completed batches
        List<Order> batchToComplete = new ArrayList<>(currentBatch);
        completedBatches.add(batchToComplete);
        SharedResources.logActivity("Sorter: Completed Batch #" + batchNumber + " with " + batchToComplete.size() + " boxes");
        
        currentBatch.clear();
        batchNumber++;
        
        // Check if we have enough batches to create a container
        if (completedBatches.size() >= BATCHES_PER_CONTAINER) {
            createContainer();
        }
    }
    
    private void createContainer() {
        try {
            int containerId = SharedResources.getNextContainerId();
            Container container = new Container(containerId);
            
            // Use up to 5 batches (or all available) for this container
            int batchesUsed = Math.min(completedBatches.size(), BATCHES_PER_CONTAINER);
            int totalBoxes = 0;
            
            for (int i = 0; i < batchesUsed; i++) {
                List<Order> batch = completedBatches.remove(0);
                for (Order order : batch) {
                    if (container.addBox(order)) {
                        order.setStatus("CONTAINERIZED");
                        totalBoxes++;
                    } else {
                        // Container is full - shouldn't happen with proper batch sizing
                        break;
                    }
                }
            }
            
            SharedResources.containerQueue.put(container);
            Statistics.getInstance().incrementContainersShipped();
            
            SharedResources.logActivity("Sorter: Created Container #" + containerId + " with " + 
                                      totalBoxes + " boxes from " + batchesUsed + " batches");
            
        } catch (InterruptedException e) {
            SharedResources.logActivity("Sorter: Interrupted while creating container");
            Thread.currentThread().interrupt();
        }
    }
}