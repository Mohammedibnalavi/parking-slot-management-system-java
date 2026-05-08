package parking;

/**
 * Concrete subclass for two-wheelers (bikes, scooters, etc.).
 * Rate  : ₹20 / hour
 * Slots : 1
 */
public class TwoWheeler extends Vehicle {

    private static final long serialVersionUID = 2L;

    private static final double RATE_PER_HOUR = 20.0;
    private static final int    SLOTS_REQUIRED = 1;

    public TwoWheeler(String vehicleNumber, String ownerName) {
        super(vehicleNumber, ownerName);
    }

    @Override public String getVehicleType()  { return "Two-Wheeler";   }
    @Override public double getRatePerHour()  { return RATE_PER_HOUR;   }
    @Override public int    getSlotsRequired(){ return SLOTS_REQUIRED;  }
}
