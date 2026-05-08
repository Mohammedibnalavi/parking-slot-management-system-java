package parking;

import java.io.Serializable;

/**
 * Represents a single physical parking slot.
 */
public class ParkingSlot implements Serializable {

    private static final long serialVersionUID = 4L;

    private final int     slotId;
    private boolean       occupied;
    private Vehicle       vehicle;       // null when free

    public ParkingSlot(int slotId) {
        this.slotId   = slotId;
        this.occupied = false;
        this.vehicle  = null;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int     getSlotId()   { return slotId;   }
    public boolean isOccupied()  { return occupied;  }
    public Vehicle getVehicle()  { return vehicle;   }

    // ── Slot operations ──────────────────────────────────────────────────────
    public void assign(Vehicle v) {
        this.vehicle  = v;
        this.occupied = true;
    }

    public void release() {
        this.vehicle  = null;
        this.occupied = false;
    }

    @Override
    public String toString() {
        if (!occupied) {
            return String.format("  Slot %3d  |  [ AVAILABLE ]", slotId);
        }
        return String.format("  Slot %3d  |  [ OCCUPIED  ]  %s  |  Owner: %s",
            slotId, vehicle.getVehicleNumber(), vehicle.getOwnerName());
    }
}
