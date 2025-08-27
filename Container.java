import java.util.ArrayList;
import java.util.List;

public class Container {
    private int containerId;
    private List<Order> boxes;
    private static final int MAX_BOXES = 30;
    private long creationTime;
    private String destination;
    
    public Container(int containerId) {
        this.containerId = containerId;
        this.boxes = new ArrayList<>();
        this.creationTime = System.currentTimeMillis();
        this.destination = generateDestination();
    }
    
    public synchronized boolean addBox(Order order) {
        if (boxes.size() < MAX_BOXES) {
            boxes.add(order);
            return true;
        }
        return false;
    }
    
    public synchronized boolean isFull() {
        return boxes.size() >= MAX_BOXES;
    }
    
    public synchronized int getBoxCount() {
        return boxes.size();
    }
    
    public int getContainerId() {
        return containerId;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public List<Order> getBoxes() {
        return new ArrayList<>(boxes);
    }
    
    private String generateDestination() {
        String[] zones = {"North Zone", "South Zone", "East Zone", "West Zone", "Central Zone"};
        return zones[(int) (Math.random() * zones.length)];
    }
    
    @Override
    public String toString() {
        return "Container #" + containerId + " (" + boxes.size() + "/" + MAX_BOXES + " boxes) -> " + destination;
    }
}