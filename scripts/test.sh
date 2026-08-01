#!/bin/bash
# PhoneBackup Pro - Test Script

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "Running Unit Tests..."
./gradlew test

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed${NC}"
else
    echo -e "${RED}✗ Tests failed${NC}"
    exit 1
fi

echo ""
echo "Running Lint Checks..."
./gradlew lint

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Lint checks passed${NC}"
else
    echo -e "${RED}✗ Lint checks failed${NC}"
    exit 1
fi

echo ""
echo "Generating Coverage Report..."
./gradlew testDebugUnitTestCoverage

echo ""
echo -e "${GREEN}✓ All checks completed${NC}"
echo "Coverage report: app/build/reports/jacoco/testDebugUnitTestCoverage/"
