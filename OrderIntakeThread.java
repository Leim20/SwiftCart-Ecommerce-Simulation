public class OrderIntakeThread extends Thread {
    private static final int TOTAL_ORDERS = 600;
    private static final int ORDER_INTERVAL_MS = 500;
    
    public OrderIntakeThread() {
        super("OrderThread-1");
    }
    
    @Override
    public void run() {
        SharedResources.logActivity("OrderIntake: Starting order generation");
        
        for (int i = 1; i <= TOTAL_ORDERS; i++) {
            try {
                Order order = new Order(i);
                
                if (order.verifyPayment() && order.checkInventory() && order.validateShippingAddress()) {
                    order.setStatus("VERIFIED");
                    SharedResources.orderQueue.put(order);
                    SharedResources.logActivity("OrderIntake: Order #" + i + " received");
                } else {
                    order.setStatus("REJECTED");
                    order.setDefective(true);
                    Statistics.getInstance().incrementOrdersRejected();
                    SharedResources.logActivity("OrderIntake: Order #" + i + " rejected - failed verification");
                }
                
                Thread.sleep(ORDER_INTERVAL_MS);
                
            } catch (InterruptedException e) {
                SharedResources.logActivity("OrderIntake: Interrupted during order generation");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        SharedResources.orderGenerationComplete = true;
        SharedResources.logActivity("OrderIntake: Order generation completed - " + TOTAL_ORDERS + " orders processed");
    }
}