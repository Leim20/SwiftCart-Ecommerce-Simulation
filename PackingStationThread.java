public class PackingStationThread extends Thread {
    
    public PackingStationThread() {
        super("Packer-1");
    }
    
    @Override
    public void run() {
        SharedResources.logActivity("PackingStation: Packer started");
        
        while (SharedResources.simulationRunning) {
            try {
                SharedResources.packingStationSemaphore.acquire();
                
                Order order = SharedResources.packingQueue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                
                if (order != null) {
                    SharedResources.logActivity("PackingStation: Packing Order #" + order.getOrderId());
                    
                    // Original slower timing for 5m53s version
                    Thread.sleep(150 + (int)(Math.random() * 100));
                    
                    if (order.verifyPackingContents()) {
                        order.setStatus("PACKED");
                        SharedResources.labellingQueue.put(order);
                        SharedResources.logActivity("PackingStation: Packed Order #" + order.getOrderId());
                    } else {
                        order.setStatus("REJECTED");
                        order.setDefective(true);
                        Statistics.getInstance().incrementOrdersRejected();
                        SharedResources.logActivity("PackingStation: Order #" + order.getOrderId() + " rejected - packing error");
                    }
                } else if (SharedResources.orderGenerationComplete && 
                          SharedResources.packingQueue.isEmpty() && 
                          SharedResources.orderQueue.isEmpty()) {
                    SharedResources.packingStationSemaphore.release();
                    break;
                }
                
                SharedResources.packingStationSemaphore.release();
                
            } catch (InterruptedException e) {
                SharedResources.logActivity("PackingStation: Packer interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        SharedResources.logActivity("PackingStation: Packer finished");
    }
}