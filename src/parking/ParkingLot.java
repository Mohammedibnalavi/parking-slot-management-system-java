package parking;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core management class.
 * Maintains the slot array and active-vehicle registry,
 * delegates persistence to {@link FileHandler}.
 */
public class ParkingLot {

    // ── Configuration ─────────────────────────────────────────────────────────
    public static final int TOTAL_SLOTS = 20;

    // ── Internal state ────────────────────────────────────────────────────────
    private final ParkingSlot[]        slots;
    /** vehicleNumber → Vehicle (only parked vehicles) */
    private final Map<String, Vehicle> activeVehicles;
    /** vehicleNumber → list of slot IDs assigned to it */
    private final Map<String, List<Integer>> vehicleSlotMap;

    // ── Statistics ────────────────────────────────────────────────────────────
    private int    totalVehiclesServed = 0;
    private double totalRevenue        = 0.0;

    // ── Constructor ───────────────────────────────────────────────────────────
    public ParkingLot() {
        slots = new ParkingSlot[TOTAL_SLOTS + 1];   // 1-indexed
        for (int i = 1; i <= TOTAL_SLOTS; i++) {
            slots[i] = new ParkingSlot(i);
        }

        // Restore serialised state (file handling)
        activeVehicles = FileHandler.loadState();
        vehicleSlotMap = new HashMap<>();

        // Rebuild slot occupation from restored state
        rebuildFromState();
    }

    /** Reconstruct slot mappings after deserialisation. */
    private void rebuildFromState() {
        // We can't fully rebuild slot IDs without a persisted slot-map,
        // so we just re-assign sequentially (good enough for restore demo).
        int slotPtr = 1;
        for (Vehicle v : activeVehicles.values()) {
            List<Integer> assigned = new ArrayList<>();
            for (int k = 0; k < v.getSlotsRequired(); k++) {
                if (slotPtr <= TOTAL_SLOTS) {
                    slots[slotPtr].assign(v);
                    assigned.add(slotPtr);
                    slotPtr++;
                }
            }
            vehicleSlotMap.put(v.getVehicleNumber(), assigned);
        }
        if (!activeVehicles.isEmpty()) {
            System.out.printf("  [INFO] %d vehicle(s) restored into slots.%n",
                activeVehicles.size());
        }
    }

    // ── Core operations ───────────────────────────────────────────────────────

    /**
     * Allocates consecutive free slots for {@code vehicle}.
     * @return list of assigned slot IDs, or empty list if not enough free slots.
     */
    public List<Integer> parkVehicle(Vehicle vehicle) {
        String vn = vehicle.getVehicleNumber();

        if (activeVehicles.containsKey(vn)) {
            System.out.println("  [ERROR] Vehicle " + vn + " is already parked!");
            return Collections.emptyList();
        }

        int needed = vehicle.getSlotsRequired();
        List<Integer> assigned = findConsecutiveFreeSlots(needed);

        if (assigned.isEmpty()) {
            System.out.printf("  [ERROR] Not enough free slots! Need %d, but insufficient.%n", needed);
            return Collections.emptyList();
        }

        for (int id : assigned) slots[id].assign(vehicle);
        activeVehicles.put(vn, vehicle);
        vehicleSlotMap.put(vn, assigned);

        FileHandler.saveState(activeVehicles);   // persist
        return assigned;
    }

    /**
     * Releases all slots for the vehicle, calculates charge, logs receipt.
     * @return the generated {@link Receipt}, or null if vehicle not found.
     */
    public Receipt releaseVehicle(String vehicleNumber) {
        String vn = vehicleNumber.toUpperCase().trim();
        Vehicle v  = activeVehicles.get(vn);
        if (v == null) {
            System.out.println("  [ERROR] Vehicle " + vn + " not found in parking lot.");
            return null;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        double charge = v.calculateCharge(exitTime);

        List<Integer> slotList = vehicleSlotMap.getOrDefault(vn, Collections.emptyList());
        int[] slotArr = slotList.stream().mapToInt(Integer::intValue).toArray();

        for (int id : slotList) slots[id].release();
        activeVehicles.remove(vn);
        vehicleSlotMap.remove(vn);

        totalVehiclesServed++;
        totalRevenue += charge;

        Receipt receipt = new Receipt(v, exitTime, charge, slotArr);
        FileHandler.logReceipt(receipt);         // persist
        FileHandler.saveState(activeVehicles);   // persist updated state

        return receipt;
    }

    // ── Slot search ───────────────────────────────────────────────────────────

    /** Finds the first run of {@code count} consecutive free slots (1-indexed). */
    private List<Integer> findConsecutiveFreeSlots(int count) {
        List<Integer> run = new ArrayList<>();
        for (int i = 1; i <= TOTAL_SLOTS; i++) {
            if (!slots[i].isOccupied()) {
                run.add(i);
                if (run.size() == count) return run;
            } else {
                run.clear();
            }
        }
        return Collections.emptyList();
    }

    // ── Availability ──────────────────────────────────────────────────────────

    public int availableSlots() {
        int free = 0;
        for (int i = 1; i <= TOTAL_SLOTS; i++)
            if (!slots[i].isOccupied()) free++;
        return free;
    }

    public int occupiedSlots() { return TOTAL_SLOTS - availableSlots(); }

    /** Can a vehicle of this type fit right now? */
    public boolean canAccommodate(Vehicle v) {
        return !findConsecutiveFreeSlots(v.getSlotsRequired()).isEmpty();
    }

    // ── Search ────────────────────────────────────────────────────────────────

    public Vehicle searchByVehicleNumber(String vn) {
        return activeVehicles.get(vn.toUpperCase().trim());
    }

    public List<Vehicle> searchByOwnerName(String name) {
        String lower = name.toLowerCase().trim();
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : activeVehicles.values()) {
            if (v.getOwnerName().toLowerCase().contains(lower)) result.add(v);
        }
        return result;
    }

    // ── Display helpers ───────────────────────────────────────────────────────

    public void displayAllSlots() {
        System.out.println("\n  ┌─────────────────────────────────────────────────────────────┐");
        System.out.println("  │                   PARKING LOT STATUS                        │");
        System.out.printf("  │   Total Slots: %-4d  Occupied: %-4d  Available: %-4d        │%n",
            TOTAL_SLOTS, occupiedSlots(), availableSlots());
        System.out.println("  ├─────────────────────────────────────────────────────────────┤");
        for (int i = 1; i <= TOTAL_SLOTS; i++) {
            System.out.println("  │" + slots[i]);
            if (i % 5 == 0 && i < TOTAL_SLOTS)
                System.out.println("  ├─────────────────────────────────────────────────────────────┤");
        }
        System.out.println("  └─────────────────────────────────────────────────────────────┘");
    }

    public void displayActiveVehicles() {
        if (activeVehicles.isEmpty()) {
            System.out.println("  No vehicles currently parked.");
            return;
        }
        System.out.printf("  %-5s %-14s %-18s %-13s %-8s%n",
            "Slots", "Vehicle No", "Owner", "Type", "Entry");
        System.out.println("  " + "─".repeat(68));
        for (Map.Entry<String, Vehicle> e : activeVehicles.entrySet()) {
            Vehicle v = e.getValue();
            List<Integer> s = vehicleSlotMap.getOrDefault(e.getKey(), List.of());
            String slotStr = s.toString().replaceAll("[\\[\\]]", "");
            System.out.printf("  %-5s %-14s %-18s %-13s %s%n",
                slotStr, v.getVehicleNumber(), v.getOwnerName(),
                v.getVehicleType(),
                v.getEntryTime().format(Vehicle.FORMATTER));
        }
        System.out.println("  " + "─".repeat(68));
        System.out.println("  Active vehicles: " + activeVehicles.size());
    }

    public void displayStatistics() {
        System.out.println("\n  ┌───────────────────────────────────┐");
        System.out.println("  │         SESSION STATISTICS        │");
        System.out.println("  ├───────────────────────────────────┤");
        System.out.printf ("  │  Total Slots       : %-14d│%n", TOTAL_SLOTS);
        System.out.printf ("  │  Currently Occupied: %-14d│%n", occupiedSlots());
        System.out.printf ("  │  Currently Free    : %-14d│%n", availableSlots());
        System.out.printf ("  │  Vehicles Served   : %-14d│%n", totalVehiclesServed);
        System.out.printf ("  │  Session Revenue   : ₹%-13.2f│%n", totalRevenue);
        System.out.println("  └───────────────────────────────────┘");
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public Map<String, Vehicle>      getActiveVehicles() { return activeVehicles; }
    public Map<String, List<Integer>> getVehicleSlotMap(){ return vehicleSlotMap; }
}
