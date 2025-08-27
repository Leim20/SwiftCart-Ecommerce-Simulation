public class LabellingStationThread extends Thread {
    private static int trackingCounter = 1000;
    
    public LabellingStationThread() {
        super("Labeller-1");
    }
    
    @Override
    public void run() {
        SharedResources.logActivity("LabellingStation: Labeller started");
        
        while (SharedResources.simulationRunning) {
            try {
                SharedResources.labellingStationSemaphore.acquire();
                
                Order order = SharedResources.labellingQueue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                
                if (order != null) {
                    String trackingId = "#A" + (trackingCounter++);
                    order.setTrackingId(trackingId);
                    
                    SharedResources.logActivity("LabellingStation: Labelling Order #" + order.getOrderId());
                    
                    // Original slower timing for 5m53s version
                    Thread.sleep(80 + (int)(Math.random() * 70));
                    
                    if (order.passQualityCheck()) {
                        order.setStatus("LABELLED");
                        SharedResources.sortingQueue.put(order);
                        Statistics.getInstance().incrementOrdersProcessed();
                        Statistics.getInstance().incrementBoxesPacked();
                        SharedResources.logActivity("LabellingStation: Labelled Order #" + order.getOrderId() + " with Tracking ID " + trackingId);
                    } else {
                        order.setStatus("REJECTED");
                        order.setDefective(true);
                        Statistics.getInstance().incrementOrdersRejected();
                        SharedResources.logActivity("LabellingStation: Order #" + order.getOrderId() + " rejected - quality check failed");
                    }
                } else if (SharedResources.orderGenerationComplete && 
                          SharedResources.labellingQueue.isEmpty() && 
                          SharedResources.packingQueue.isEmpty() && 
                          SharedResources.orderQueue.isEmpty()) {
                    SharedResources.labellingStationSemaphore.release();
                    break;
                }
                
                SharedResources.labellingStationSemaphore.release();
                
            } catch (InterruptedException e) {
                SharedResources.logActivity("LabellingStation: Labeller interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        SharedResources.logActivity("LabellingStation: Labeller finished");
    }
}