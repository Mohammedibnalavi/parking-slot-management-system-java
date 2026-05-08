# Parking Slot Management System (Java)

A menu-driven Java application for managing parking slots with charge calculation,
built using **Inheritance**, **File Handling**, and **OOP principles**.

---

## Project Structure

```
ParkingSystem/
│
├── src/parking/
│   ├── Vehicle.java        ← Abstract base class (Serializable)
│   ├── TwoWheeler.java     ← Extends Vehicle  (₹20/hr, 1 slot)
│   ├── FourWheeler.java    ← Extends Vehicle  (₹50/hr, 2 slots)
│   ├── ParkingSlot.java    ← Single physical slot
│   ├── ParkingLot.java     ← Core manager (slot array + registry)
│   ├── Receipt.java        ← Immutable parking receipt
│   ├── FileHandler.java    ← Serialisation + CSV log (File Handling)
│   └── Main.java           ← Menu-driven entry point
│
├── out/
│   ├── parking/            ← Compiled .class files
│   └── ParkingSystem.jar   ← Executable JAR
│
└── run.sh                  ← Build & run script
```

---

## How to Compile & Run

### Option A – Script
```bash
chmod +x run.sh
./run.sh
```

### Option B – Manual
```bash
mkdir -p out
javac -d out src/parking/*.java
cd out && java parking.Main
```

### Option C – Pre-built JAR
```bash
cd out
java -jar ParkingSystem.jar
```

---

## Features

| Feature                  | Detail                                          |
|--------------------------|-------------------------------------------------|
| **Inheritance**          | `TwoWheeler`, `FourWheeler` extend `Vehicle`    |
| **Abstraction**          | `Vehicle` is abstract with abstract methods     |
| **File Handling**        | `parking_state.dat` (serialised state) + `transactions.csv` (log) |
| **Session Restore**      | Previous parked vehicles reloaded on startup    |
| **Charge Calculation**   | Minimum 1 hr; ceiling on fractional hours       |
| **Menu-Driven UI**       | do-while + switch with 8 menu options           |
| **Consecutive Slots**    | 4-wheelers require 2 adjacent free slots        |
| **Duplicate Detection**  | Cannot park the same vehicle number twice       |
| **Search**               | By vehicle number or owner name (partial match) |
| **Transaction Log**      | Append-only CSV with all exit receipts          |
| **Statistics**           | Real-time count of vehicles served & revenue    |

---

## Rates

| Vehicle Type | Rate       | Slots Used |
|--------------|------------|------------|
| Two-Wheeler  | ₹20 / hour | 1          |
| Four-Wheeler | ₹50 / hour | 2          |

> Minimum billing: **1 hour**. Fractional hours are rounded **up**.

---

## OOP Concepts Used

```
Vehicle (abstract)            ← Base class
   └── TwoWheeler             ← Subclass 1 (Inheritance)
   └── FourWheeler            ← Subclass 2 (Inheritance)

Interfaces/Features:
   • Serializable             ← File I/O (object serialisation)
   • Method Overriding        ← getVehicleType(), getRatePerHour(), getSlotsRequired()
   • Encapsulation            ← private fields + getters
   • Polymorphism             ← Vehicle reference holds TwoWheeler/FourWheeler
```

---

## Files Generated at Runtime

| File                  | Purpose                          |
|-----------------------|----------------------------------|
| `parking_state.dat`   | Serialised active vehicle map    |
| `transactions.csv`    | Append-only receipt log (CSV)    |
