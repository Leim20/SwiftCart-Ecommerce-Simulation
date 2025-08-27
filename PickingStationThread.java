public class PickingStationThread extends Thread {
    private int pickerId;
    
    public PickingStationThread(int pickerId) {
        super("Picker-" + pickerId);
        this.pickerId = pickerId;
    }
    
    @Override
    public void run() {
        SharedResources.logActivity("PickingStation: Picker-" + pickerId + " started");
        
        while (SharedResources.simulationRunning) {
            try {
                SharedResources.pickingStationSemaphore.acquire();
                
                Order order = SharedResources.orderQueue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                
                if (order != null) {
                    SharedResources.logActivity("PickingStation: Picking Order #" + order.getOrderId());
                    
                    // Original slower timing for 5m53s version
                    Thread.sleep(100 + (int)(Math.random() * 100));
                    
                    if (order.checkForMissingItems()) {
                        order.setStatus("PICKED");
                        SharedResources.packingQueue.put(order);
                        SharedResources.logActivity("PickingStation: Order #" + order.getOrderId() + " picked successfully");
                    } else {
                        order.setStatus("REJECTED");
                        order.setDefective(true);
                        Statistics.getInstance().incrementOrdersRejected();
                        SharedResources.logActivity("PickingStation: Order #" + order.getOrderId() + " rejected - missing items");
                    }
                } else if (SharedResources.orderGenerationComplete && SharedResources.orderQueue.isEmpty()) {
                    SharedResources.pickingStationSemaphore.release();
                    break;
                }
                
                SharedResources.pickingStationSemaphore.release();
                
            } catch (InterruptedException e) {
                SharedResources.logActivity("PickingStation: Picker-" + pickerId + " interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        SharedResources.logActivity("PickingStation: Picker-" + pickerId + " finished");
    }
}