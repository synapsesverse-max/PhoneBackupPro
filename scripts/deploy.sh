#!/bin/bash
# PhoneBackup Pro - Deploy Script

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

VERSION=$1
BUILD_TYPE=${2:-release}

if [ -z "$VERSION" ]; then
    echo "Usage: ./deploy.sh <version> [build_type]"
    echo "Example: ./deploy.sh 1.0.0 release"
    exit 1
fi

echo -e "${BLUE}Deploying version $VERSION...${NC}"

# Update version
echo "Updating version to $VERSION..."
sed -i "s/versionName = ".*"/versionName = \"$VERSION\"/" app/build.gradle.kts

# Build
echo "Building $BUILD_TYPE..."
./gradlew :app:assemble${BUILD_TYPE^}

# Generate changelog
echo "Generating changelog..."
git log --oneline $(git describe --tags --abbrev=0 2>/dev/null || git rev-list --max-parents=0 HEAD)..HEAD > CHANGELOG.md

# Create tag
echo "Creating git tag v$VERSION..."
git tag -a "v$VERSION" -m "Release version $VERSION"
git push origin "v$VERSION"

echo -e "${GREEN}✓ Deployment completed${NC}"
echo "APK: app/build/outputs/apk/$BUILD_TYPE/app-$BUILD_TYPE.apk"
