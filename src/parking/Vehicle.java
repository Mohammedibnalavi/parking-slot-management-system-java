package parking;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base class representing a vehicle.
 * Uses inheritance — TwoWheeler and FourWheeler extend this.
 */
public abstract class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String vehicleNumber;
    protected String ownerName;
    protected LocalDateTime entryTime;

    static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public Vehicle(String vehicleNumber, String ownerName) {
        this.vehicleNumber  = vehicleNumber.toUpperCase().trim();
        this.ownerName      = ownerName.trim();
        this.entryTime      = LocalDateTime.now();
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    public String       getVehicleNumber() { return vehicleNumber; }
    public String       getOwnerName()     { return ownerName;     }
    public LocalDateTime getEntryTime()    { return entryTime;     }

    // ── Abstract contract ────────────────────────────────────────────────────
    /** Returns "Two-Wheeler" or "Four-Wheeler". */
    public abstract String getVehicleType();

    /** Charge rate in ₹ per hour. */
    public abstract double getRatePerHour();

    /** Slots consumed by this vehicle (1 for 2W, 2 for 4W). */
    public abstract int    getSlotsRequired();

    // ── Charge calculation ───────────────────────────────────────────────────
    /**
     * Calculates parking charge.
     * First hour is charged at full rate; subsequent hours at rate/2 for
     * two-wheelers and full rate for four-wheelers.
     * A minimum of 1 hour is always billed.
     */
    public double calculateCharge(LocalDateTime exitTime) {
        long minutes = java.time.Duration.between(entryTime, exitTime).toMinutes();
        double hours = Math.max(1.0, Math.ceil(minutes / 60.0));   // minimum 1 hr
        return hours * getRatePerHour();
    }

    // ── Display ──────────────────────────────────────────────────────────────
    public String getSummary() {
        return String.format(
            "  Vehicle No : %s\n  Owner      : %s\n  Type       : %s\n  Entry Time : %s",
            vehicleNumber, ownerName, getVehicleType(),
            entryTime.format(FORMATTER)
        );
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | In: %s",
            getVehicleType(), vehicleNumber, ownerName,
            entryTime.format(FORMATTER));
    }
}
