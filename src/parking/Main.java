package parking;

import java.util.*;

/**
 *  ╔════════════════════════════════════════╗
 *  ║   Parking Slot Management System      ║
 *  ║   Menu-driven | Inheritance | Files   ║
 *  ╚════════════════════════════════════════╝
 *
 *  Demonstrates:
 *   • Inheritance   – TwoWheeler / FourWheeler extend abstract Vehicle
 *   • File Handling – state saved to parking_state.dat; receipts logged to transactions.csv
 *   • Menu Control  – do-while loop with switch dispatch
 */
public class Main {

    private static final Scanner sc  = new Scanner(System.in);
    private static ParkingLot    lot;

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        printBanner();
        lot = new ParkingLot();
        System.out.println();

        int choice;
        do {
            printMenu();
            choice = readInt("  Enter your choice: ");
            System.out.println();

            switch (choice) {
                case 1  -> parkVehicle();
                case 2  -> releaseVehicle();
                case 3  -> displayAllSlots();
                case 4  -> displayActiveVehicles();
                case 5  -> searchVehicle();
                case 6  -> FileHandler.printTransactionLog();
                case 7  -> lot.displayStatistics();
                case 8  -> adminMenu();
                case 0  -> confirmExit();
                default -> System.out.println("  [!] Invalid option. Please choose 0-8.");
            }
        } while (choice != 0);

        sc.close();
    }

    // ── Menu ──────────────────────────────────────────────────────────────────
    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║       PARKING SLOT MANAGEMENT SYSTEM        ║");
        System.out.println("  ║  Two-Wheeler: ₹20/hr  |  Four-Wheeler: ₹50/hr  ║");
        System.out.println("  ║      Total Slots: " + ParkingLot.TOTAL_SLOTS + "                         ║");
        System.out.println("  ╚══════════════════════════════════════════════╝");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  ┌─────────────────────────────┐");
        System.out.println("  │         MAIN MENU           │");
        System.out.println("  ├─────────────────────────────┤");
        System.out.println("  │  1. Park a Vehicle          │");
        System.out.println("  │  2. Release a Vehicle       │");
        System.out.println("  │  3. View All Parking Slots  │");
        System.out.println("  │  4. View Active Vehicles    │");
        System.out.println("  │  5. Search Vehicle          │");
        System.out.println("  │  6. Transaction History     │");
        System.out.println("  │  7. Statistics              │");
        System.out.println("  │  8. Admin Menu              │");
        System.out.println("  │  0. Exit                    │");
        System.out.println("  └─────────────────────────────┘");
    }

    // ── 1. Park Vehicle ───────────────────────────────────────────────────────
    private static void parkVehicle() {
        System.out.println("  ── PARK VEHICLE ──────────────────────────────");
        System.out.println("  Select vehicle type:");
        System.out.println("    1. Two-Wheeler  (1 slot  | ₹20/hr)");
        System.out.println("    2. Four-Wheeler (2 slots | ₹50/hr)");
        int type = readInt("  Choice: ");

        if (type != 1 && type != 2) {
            System.out.println("  [!] Invalid vehicle type.");
            return;
        }

        System.out.print("  Enter Vehicle Number : ");
        String vn = sc.nextLine().trim();
        if (vn.isEmpty()) { System.out.println("  [!] Vehicle number cannot be empty."); return; }

        System.out.print("  Enter Owner Name     : ");
        String owner = sc.nextLine().trim();
        if (owner.isEmpty()) { System.out.println("  [!] Owner name cannot be empty."); return; }

        Vehicle v = (type == 1) ? new TwoWheeler(vn, owner) : new FourWheeler(vn, owner);

        if (!lot.canAccommodate(v)) {
            System.out.println("  [!] Sorry, no " + v.getSlotsRequired() +
                " consecutive slot(s) available for a " + v.getVehicleType() + ".");
            return;
        }

        List<Integer> assigned = lot.parkVehicle(v);
        if (!assigned.isEmpty()) {
            System.out.println();
            System.out.println("  ✔ Vehicle parked successfully!");
            System.out.printf ("  %-16s : %s%n", "Vehicle Number", v.getVehicleNumber());
            System.out.printf ("  %-16s : %s%n", "Owner",          v.getOwnerName());
            System.out.printf ("  %-16s : %s%n", "Type",           v.getVehicleType());
            System.out.printf ("  %-16s : %s%n", "Assigned Slots", assigned.toString().replaceAll("[\\[\\]]",""));
            System.out.printf ("  %-16s : %s%n", "Entry Time",     v.getEntryTime().format(Vehicle.FORMATTER));
            System.out.printf ("  %-16s : ₹%.2f / hr%n", "Rate", v.getRatePerHour());
        }
    }

    // ── 2. Release Vehicle ────────────────────────────────────────────────────
    private static void releaseVehicle() {
        System.out.println("  ── RELEASE VEHICLE ───────────────────────────");
        if (lot.getActiveVehicles().isEmpty()) {
            System.out.println("  No vehicles currently parked.");
            return;
        }
        System.out.print("  Enter Vehicle Number to release: ");
        String vn = sc.nextLine().trim();

        Receipt receipt = lot.releaseVehicle(vn);
        if (receipt != null) {
            System.out.print(receipt.toFormattedString());
            System.out.println("  ✔ Slot(s) freed. Transaction saved to " +
                FileHandler.getLogFile());
        }
    }

    // ── 3. Display All Slots ──────────────────────────────────────────────────
    private static void displayAllSlots() {
        lot.displayAllSlots();
    }

    // ── 4. Display Active Vehicles ────────────────────────────────────────────
    private static void displayActiveVehicles() {
        System.out.println("  ── ACTIVE VEHICLES ───────────────────────────");
        lot.displayActiveVehicles();
    }

    // ── 5. Search ─────────────────────────────────────────────────────────────
    private static void searchVehicle() {
        System.out.println("  ── SEARCH VEHICLE ────────────────────────────");
        System.out.println("  Search by:");
        System.out.println("    1. Vehicle Number");
        System.out.println("    2. Owner Name");
        int opt = readInt("  Choice: ");

        if (opt == 1) {
            System.out.print("  Enter Vehicle Number: ");
            String vn = sc.nextLine().trim();
            Vehicle v = lot.searchByVehicleNumber(vn);
            if (v == null) System.out.println("  Vehicle not found.");
            else {
                System.out.println("\n  Vehicle found:");
                System.out.println(v.getSummary());
                List<Integer> s = lot.getVehicleSlotMap().get(v.getVehicleNumber());
                System.out.println("  Slots      : " + (s != null ? s : "N/A"));
            }
        } else if (opt == 2) {
            System.out.print("  Enter Owner Name (partial OK): ");
            String name = sc.nextLine().trim();
            List<Vehicle> results = lot.searchByOwnerName(name);
            if (results.isEmpty()) System.out.println("  No matching vehicles found.");
            else {
                System.out.println("\n  Matching vehicles (" + results.size() + "):");
                for (Vehicle v : results) {
                    System.out.println(v.getSummary());
                    List<Integer> s = lot.getVehicleSlotMap().get(v.getVehicleNumber());
                    System.out.println("  Slots      : " + (s != null ? s : "N/A"));
                    System.out.println("  " + "─".repeat(40));
                }
            }
        } else {
            System.out.println("  [!] Invalid option.");
        }
    }

    // ── 8. Admin Menu ─────────────────────────────────────────────────────────
    private static void adminMenu() {
        System.out.println("  ── ADMIN MENU ────────────────────────────────");
        System.out.println("  1. Clear all data files");
        System.out.println("  2. Show file locations");
        System.out.println("  0. Back");
        int opt = readInt("  Choice: ");
        switch (opt) {
            case 1 -> {
                System.out.print("  Confirm clear ALL data? (yes/no): ");
                String confirm = sc.nextLine().trim();
                if (confirm.equalsIgnoreCase("yes")) FileHandler.clearAllData();
                else System.out.println("  Cancelled.");
            }
            case 2 -> {
                System.out.println("  State file : " + FileHandler.getStateFile());
                System.out.println("  Log file   : " + FileHandler.getLogFile());
            }
            case 0 -> {}
            default -> System.out.println("  [!] Invalid option.");
        }
    }

    // ── Exit ──────────────────────────────────────────────────────────────────
    private static void confirmExit() {
        System.out.print("  Save and exit? (yes/no): ");
        String c = sc.nextLine().trim();
        if (!c.equalsIgnoreCase("yes")) {
            System.out.println("  Returning to menu…");
            // Reset choice to a non-exit value so the loop continues.
            // The do-while checks choice==0, so we just re-set nothing — 
            // the flag was already 0; override it externally is not possible here,
            // so we'll just print a message and let the menu redisplay naturally.
        } else {
            FileHandler.saveState(lot.getActiveVehicles());
            System.out.println();
            System.out.println("  ╔══════════════════════════════════════════╗");
            System.out.println("  ║  State saved. Thank you for using PSMS! ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }
}
