import java.util.ArrayList;
import java.util.List;

public class SwiftCartSimulation {
    public static void main(String[] args) {
        System.out.println("=== SwiftCart E-commerce Centre Simulation Starting ===");
        System.out.println("Simulating 600 orders with 500ms intervals...\n");
        
        Statistics stats = Statistics.getInstance();
        List<Thread> allThreads = new ArrayList<>();
        
        try {
            OrderIntakeThread orderIntake = new OrderIntakeThread();
            allThreads.add(orderIntake);
            orderIntake.start();
            
            PickingStationThread picker1 = new PickingStationThread(1);
            PickingStationThread picker2 = new PickingStationThread(2);
            PickingStationThread picker3 = new PickingStationThread(3);
            PickingStationThread picker4 = new PickingStationThread(4);
            allThreads.add(picker1);
            allThreads.add(picker2);
            allThreads.add(picker3);
            allThreads.add(picker4);
            picker1.start();
            picker2.start();
            picker3.start();
            picker4.start();
            
            PackingStationThread packer = new PackingStationThread();
            allThreads.add(packer);
            packer.start();
            
            LabellingStationThread labeller = new LabellingStationThread();
            allThreads.add(labeller);
            labeller.start();
            
            SortingAreaThread sorter = new SortingAreaThread();
            allThreads.add(sorter);
            sorter.start();
            
            LoadingBayManager loadingBayManager = new LoadingBayManager();
            allThreads.add(loadingBayManager);
            loadingBayManager.start();
            
            SharedResources.logActivity("SwiftCartSimulation: All systems initialized and running");
            
            for (Thread thread : allThreads) {
                thread.join(); // Wait indefinitely for each thread to complete
            }
            
            Thread.sleep(2000);
            
            SharedResources.simulationRunning = false;
            
            SharedResources.logActivity("SwiftCartSimulation: All operations completed");
            
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted: " + e.getMessage());
            SharedResources.simulationRunning = false;
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Simulation error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stats.printFinalStatistics();
        }
    }
}