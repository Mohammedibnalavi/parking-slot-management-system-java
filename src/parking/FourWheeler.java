package parking;

/**
 * Concrete subclass for four-wheelers (cars, SUVs, etc.).
 * Rate  : ₹50 / hour
 * Slots : 2
 */
public class FourWheeler extends Vehicle {

    private static final long serialVersionUID = 3L;

    private static final double RATE_PER_HOUR  = 50.0;
    private static final int    SLOTS_REQUIRED = 2;

    public FourWheeler(String vehicleNumber, String ownerName) {
        super(vehicleNumber, ownerName);
    }

    @Override public String getVehicleType()  { return "Four-Wheeler";  }
    @Override public double getRatePerHour()  { return RATE_PER_HOUR;   }
    @Override public int    getSlotsRequired(){ return SLOTS_REQUIRED;  }
}
