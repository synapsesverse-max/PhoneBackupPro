#!/bin/bash
# PhoneBackup Pro - Build Script
# This script builds all modules and generates the APK

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  PhoneBackup Pro - Build Pipeline   ${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed${NC}"
    exit 1
fi

# Check Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo -e "${YELLOW}Warning: ANDROID_HOME not set${NC}"
fi

# Clean
echo -e "${GREEN}[1/8] Cleaning project...${NC}"
./gradlew clean

# Build Core
echo -e "${GREEN}[2/8] Building Core Module...${NC}"
./gradlew :core:assembleDebug

# Build Features
echo -e "${GREEN}[3/8] Building Backup Module...${NC}"
./gradlew :features:backup:assembleDebug

echo -e "${GREEN}[4/8] Building Restore Module...${NC}"
./gradlew :features:restore:assembleDebug

echo -e "${GREEN}[5/8] Building Transfer Module...${NC}"
./gradlew :features:transfer:assembleDebug

echo -e "${GREEN}[6/8] Building WhatsApp Module...${NC}"
./gradlew :features:whatsapp:assembleDebug

# Build App
echo -e "${GREEN}[7/8] Building Main Application...${NC}"
./gradlew :app:assembleDebug

# Run Tests
echo -e "${GREEN}[8/8] Running Tests...${NC}"
./gradlew test

echo ""
echo -e "${BLUE}======================================${NC}"
echo -e "${GREEN}  Build Completed Successfully!      ${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo "APK Location: app/build/outputs/apk/debug/app-debug.apk"
