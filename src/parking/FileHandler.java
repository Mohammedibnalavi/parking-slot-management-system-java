package parking;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Handles all file I/O for the parking system.
 *
 *  parking_state.dat  – serialised Map<String, Vehicle> (active vehicles)
 *  transactions.csv   – append-only receipt log
 */
public class FileHandler {

    private static final String STATE_FILE = "parking_state.dat";
    private static final String LOG_FILE   = "transactions.csv";

    private static final String CSV_HEADER =
        "ReceiptID,VehicleNo,OwnerName,Type,EntryTime,ExitTime,DurationMin,Charge,Slots";

    // ── Active-vehicle persistence ────────────────────────────────────────────

    /** Serialise the active-vehicle map to disk. */
    @SuppressWarnings("unchecked")
    public static void saveState(Map<String, Vehicle> activeVehicles) {
        try (ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(STATE_FILE))) {
            oos.writeObject(activeVehicles);
        } catch (IOException e) {
            System.err.println("  [WARN] Could not save parking state: " + e.getMessage());
        }
    }

    /**
     * Deserialise the active-vehicle map from disk.
     * Returns an empty map if the file doesn't exist or is corrupted.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Vehicle> loadState() {
        File f = new File(STATE_FILE);
        if (!f.exists()) return new HashMap<>();
        try (ObjectInputStream ois =
                new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                System.out.println("  [INFO] Previous session restored from " + STATE_FILE);
                return (Map<String, Vehicle>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("  [WARN] Could not restore state: " + e.getMessage());
        }
        return new HashMap<>();
    }

    // ── Transaction log ───────────────────────────────────────────────────────

    /** Append a receipt to the CSV transaction log. */
    public static void logReceipt(Receipt receipt) {
        boolean newFile = !new File(LOG_FILE).exists();
        try (PrintWriter pw =
                new PrintWriter(new FileWriter(LOG_FILE, true))) {
            if (newFile) pw.println(CSV_HEADER);
            pw.println(receipt.toCsvLine());
        } catch (IOException e) {
            System.err.println("  [WARN] Could not write receipt log: " + e.getMessage());
        }
    }

    /** Read and pretty-print all past transactions. */
    public static void printTransactionLog() {
        File f = new File(LOG_FILE);
        if (!f.exists()) {
            System.out.println("  No transaction records found.");
            return;
        }
        try {
            List<String> lines = Files.readAllLines(f.toPath());
            if (lines.size() <= 1) {
                System.out.println("  No transactions recorded yet.");
                return;
            }
            System.out.printf("  %-16s %-12s %-15s %-13s %-8s %s%n",
                "Receipt ID", "Vehicle No", "Owner", "Type", "Minutes", "Charge (₹)");
            System.out.println("  " + "─".repeat(78));
            for (int i = 1; i < lines.size(); i++) {      // skip header
                String[] p = lines.get(i).split(",");
                if (p.length >= 8) {
                    System.out.printf("  %-16s %-12s %-15s %-13s %-8s ₹%s%n",
                        p[0], p[1], p[2], p[3], p[6], p[7]);
                }
            }
            System.out.println("  " + "─".repeat(78));
            System.out.println("  Total records: " + (lines.size() - 1));
        } catch (IOException e) {
            System.err.println("  [ERROR] Cannot read log: " + e.getMessage());
        }
    }

    /** Delete both data files (admin reset). */
    public static void clearAllData() {
        boolean s = new File(STATE_FILE).delete();
        boolean l = new File(LOG_FILE).delete();
        if (s || l) System.out.println("  Data files cleared.");
        else        System.out.println("  Nothing to clear.");
    }

    public static String getStateFile() { return STATE_FILE; }
    public static String getLogFile()   { return LOG_FILE;   }
}
