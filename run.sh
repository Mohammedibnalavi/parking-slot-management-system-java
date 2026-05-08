#!/bin/bash
# ─────────────────────────────────────────────────────────────
#  Parking Slot Management System – Build & Run Script
#  Usage:  chmod +x run.sh && ./run.sh
# ─────────────────────────────────────────────────────────────

echo "=== Compiling Parking Slot Management System ==="
mkdir -p out
javac -d out src/parking/*.java

if [ $? -ne 0 ]; then
  echo "[ERROR] Compilation failed."
  exit 1
fi

echo "=== Compilation successful. Starting application... ==="
echo ""
cd out
java parking.Main
