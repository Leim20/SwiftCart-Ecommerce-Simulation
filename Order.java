public class Order {
    private int orderId;
    private String status;
    private long creationTime;
    private long processingStartTime;
    private long processingEndTime;
    private String trackingId;
    private boolean isDefective;
    
    public Order(int orderId) {
        this.orderId = orderId;
        this.status = "RECEIVED";
        this.creationTime = System.currentTimeMillis();
        this.isDefective = false;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public void setProcessingStartTime(long time) {
        this.processingStartTime = time;
    }
    
    public void setProcessingEndTime(long time) {
        this.processingEndTime = time;
    }
    
    public long getProcessingTime() {
        return processingEndTime - processingStartTime;
    }
    
    public String getTrackingId() {
        return trackingId;
    }
    
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }
    
    public boolean isDefective() {
        return isDefective;
    }
    
    public void setDefective(boolean defective) {
        this.isDefective = defective;
    }
    
    public boolean verifyPayment() {
        return Math.random() > 0.05;
    }
    
    public boolean checkInventory() {
        return Math.random() > 0.03;
    }
    
    public boolean validateShippingAddress() {
        return Math.random() > 0.02;
    }
    
    public boolean checkForMissingItems() {
        return Math.random() > 0.04;
    }
    
    public boolean verifyPackingContents() {
        return Math.random() > 0.03;
    }
    
    public boolean passQualityCheck() {
        return Math.random() > 0.02;
    }
    
    @Override
    public String toString() {
        return "Order #" + orderId + " [" + status + "]";
    }
}