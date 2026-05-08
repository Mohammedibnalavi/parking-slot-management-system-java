package parking;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Immutable receipt generated on vehicle exit.
 * Stored in the transaction log via file handling.
 */
public class Receipt implements Serializable {

    private static final long   serialVersionUID = 5L;
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final String        receiptId;
    private final String        vehicleNumber;
    private final String        ownerName;
    private final String        vehicleType;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final long          durationMinutes;
    private final double        totalCharge;
    private final int[]         slotIds;

    public Receipt(Vehicle v, LocalDateTime exitTime, double charge, int[] slotIds) {
        this.receiptId       = "RCP-" + System.currentTimeMillis();
        this.vehicleNumber   = v.getVehicleNumber();
        this.ownerName       = v.getOwnerName();
        this.vehicleType     = v.getVehicleType();
        this.entryTime       = v.getEntryTime();
        this.exitTime        = exitTime;
        this.durationMinutes = Duration.between(entryTime, exitTime).toMinutes();
        this.totalCharge     = charge;
        this.slotIds         = slotIds;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String        getReceiptId()      { return receiptId;       }
    public String        getVehicleNumber()  { return vehicleNumber;   }
    public double        getTotalCharge()    { return totalCharge;     }

    // ── Pretty-print ─────────────────────────────────────────────────────────
    public String toFormattedString() {
        long hrs = durationMinutes / 60;
        long min = durationMinutes % 60;
        StringBuilder sb = new StringBuilder();
        sb.append("\n  ╔══════════════════════════════════════╗\n");
        sb.append(  "  ║        PARKING RECEIPT               ║\n");
        sb.append(  "  ╠══════════════════════════════════════╣\n");
        sb.append(String.format("  ║  Receipt ID  : %-22s║\n", receiptId));
        sb.append(String.format("  ║  Vehicle No  : %-22s║\n", vehicleNumber));
        sb.append(String.format("  ║  Owner       : %-22s║\n", ownerName));
        sb.append(String.format("  ║  Type        : %-22s║\n", vehicleType));
        sb.append(String.format("  ║  Entry Time  : %-22s║\n", entryTime.format(FMT)));
        sb.append(String.format("  ║  Exit Time   : %-22s║\n", exitTime.format(FMT)));
        sb.append(String.format("  ║  Duration    : %02dh %02dm              ║\n", hrs, min));

        // slot list
        StringBuilder slots = new StringBuilder();
        for (int i = 0; i < slotIds.length; i++) {
            slots.append(slotIds[i]);
            if (i < slotIds.length - 1) slots.append(", ");
        }
        sb.append(String.format("  ║  Slot(s)     : %-22s║\n", slots));
        sb.append(  "  ╠══════════════════════════════════════╣\n");
        sb.append(String.format("  ║  TOTAL CHARGE: ₹%-21.2f║\n", totalCharge));
        sb.append(  "  ╚══════════════════════════════════════╝\n");
        return sb.toString();
    }

    /** One-line CSV representation for the log file. */
    public String toCsvLine() {
        StringBuilder slots = new StringBuilder();
        for (int i = 0; i < slotIds.length; i++) {
            slots.append(slotIds[i]);
            if (i < slotIds.length - 1) slots.append("|");
        }
        return String.join(",",
            receiptId, vehicleNumber, ownerName, vehicleType,
            entryTime.format(FMT), exitTime.format(FMT),
            String.valueOf(durationMinutes),
            String.format("%.2f", totalCharge),
            slots.toString()
        );
    }
}
