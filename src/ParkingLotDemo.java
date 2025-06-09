import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// Enum for Vehicle Types
enum VehicleType {
    CAR, MOTORCYCLE, TRUCK
}

// Abstract Vehicle class
abstract class Vehicle {
    protected String plate;
    protected VehicleType type;

    public Vehicle(String plate, VehicleType type) {
        this.plate = plate;
        this.type = type;
    }

    public VehicleType getType() { return type; }
    public String getPlate() { return plate; }
}

// Concrete Vehicle classes
class Car extends Vehicle {
    public Car(String plate) { super(plate, VehicleType.CAR); }
}

class Motorcycle extends Vehicle {
    public Motorcycle(String plate) { super(plate, VehicleType.MOTORCYCLE); }
}

class Truck extends Vehicle {
    public Truck(String plate) { super(plate, VehicleType.TRUCK); }
}

// ParkingSpot
class ParkingSpot {
    private int spotNumber;
    private VehicleType type;
    private Vehicle vehicle;

    public ParkingSpot(int spotNumber, VehicleType type) {
        this.spotNumber = spotNumber;
        this.type = type;
    }

    public boolean isAvailable() {
        return vehicle == null;
    }

    public boolean canFitVehicle(Vehicle v) {
        return v.getType() == type;
    }

    public boolean park(Vehicle v) {
        if (isAvailable() && canFitVehicle(v)) {
            vehicle = v;
            return true;
        }
        return false;
    }

    public void unpark() {
        vehicle = null;
    }

    public int getSpotNumber() { return spotNumber; }
    public Vehicle getVehicle() { return vehicle; }
}

// Level
class Level {
    private int levelNumber;
    private List<ParkingSpot> spots;
    private final ReentrantLock lock = new ReentrantLock();

    public Level(int levelNumber, int numSpots, VehicleType type) {
        this.levelNumber = levelNumber;
        spots = new ArrayList<>();
        for (int i = 0; i < numSpots; i++) {
            spots.add(new ParkingSpot(i, type));
        }
    }

    public boolean parkVehicle(Vehicle v) {
        lock.lock();
        try {
            for (ParkingSpot spot : spots) {
                if (spot.isAvailable() && spot.canFitVehicle(v)) {
                    return spot.park(v);
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean unparkVehicle(Vehicle v) {
        lock.lock();
        try {
            for (ParkingSpot spot : spots) {
                if (spot.getVehicle() == v) {
                    spot.unpark();
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void displayAvailableSpots() {
        System.out.print("Level " + levelNumber + " available spots: ");
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable()) {
                System.out.print(spot.getSpotNumber() + " ");
            }
        }
        System.out.println();
    }
}

// Singleton ParkingLot
class ParkingLot {
    private static ParkingLot instance;
    private static final Object lock = new Object();

    private List<Level> levels;

    private ParkingLot() {
        levels = new ArrayList<>();
    }

    public static ParkingLot getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new ParkingLot();
            }
        }
        return instance;
    }

    public void addLevel(Level level) {
        levels.add(level);
    }

    public boolean parkVehicle(Vehicle v) {
        for (Level level : levels) {
            if (level.parkVehicle(v)) {
                return true;
            }
        }
        return false;
    }

    public boolean unparkVehicle(Vehicle v) {
        for (Level level : levels) {
            if (level.unparkVehicle(v)) {
                return true;
            }
        }
        return false;
    }

    public void displayAvailableSpots() {
        for (Level level : levels) {
            level.displayAvailableSpots();
        }
    }
}

// Main class to demonstrate usage
public class ParkingLotDemo {
    public static void main(String[] args) {
        ParkingLot parkingLot = ParkingLot.getInstance();
        parkingLot.addLevel(new Level(0, 3, VehicleType.CAR));
        parkingLot.addLevel(new Level(1, 2, VehicleType.MOTORCYCLE));

        Vehicle car1 = new Car("KA-01-1234");
        Vehicle car2 = new Car("KA-02-5678");
        Vehicle bike1 = new Motorcycle("KA-03-9999");

        parkingLot.parkVehicle(car1);
        parkingLot.parkVehicle(car2);
        parkingLot.parkVehicle(bike1);

        parkingLot.displayAvailableSpots();

        parkingLot.unparkVehicle(car1);
        parkingLot.displayAvailableSpots();
    }
}
