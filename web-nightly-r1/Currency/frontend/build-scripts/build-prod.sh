#!/bin/bash
# Production Build Script for Kconvert Currency Converter
# LTS-optimized build process with comprehensive checks

set -e  # Exit on any error

echo "🚀 Starting Kconvert LTS Production Build..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Build information
BUILD_START=$(date +%s)
BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
BUILD_VERSION="2.0.0"

echo -e "${BLUE}📦 Build Version: ${BUILD_VERSION}${NC}"
echo -e "${BLUE}📅 Build Date: ${BUILD_DATE}${NC}"

# Pre-build checks
echo -e "\n${YELLOW}🔍 Running pre-build checks...${NC}"

# Check Node.js version (LTS requirement)
NODE_VERSION=$(node --version | cut -d'v' -f2)
REQUIRED_NODE="18.18.0"

if [ "$(printf '%s\n' "$REQUIRED_NODE" "$NODE_VERSION" | sort -V | head -n1)" != "$REQUIRED_NODE" ]; then
    echo -e "${RED}❌ Node.js version $NODE_VERSION is below required $REQUIRED_NODE${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Node.js version: $NODE_VERSION${NC}"

# Check npm version
NPM_VERSION=$(npm --version)
echo -e "${GREEN}✅ npm version: $NPM_VERSION${NC}"

# Clean previous builds
echo -e "\n${YELLOW}🧹 Cleaning previous builds...${NC}"
rm -rf dist
rm -rf node_modules/.vite
echo -e "${GREEN}✅ Clean completed${NC}"

# Install dependencies with exact versions for LTS stability
echo -e "\n${YELLOW}📦 Installing dependencies...${NC}"
npm ci --production=false --audit=false
echo -e "${GREEN}✅ Dependencies installed${NC}"

# Security audit
echo -e "\n${YELLOW}🔒 Running security audit...${NC}"
npm audit --audit-level moderate || {
    echo -e "${YELLOW}⚠️  Security vulnerabilities found, but continuing build...${NC}"
}

# Run production build
echo -e "\n${YELLOW}🏗️  Building for production...${NC}"
export NODE_ENV=production
export VITE_BUILD_TIME="$BUILD_DATE"
export VITE_BUILD_VERSION="$BUILD_VERSION"

npm run build

# Verify build output
echo -e "\n${YELLOW}🔍 Verifying build output...${NC}"

if [ ! -d "dist" ]; then
    echo -e "${RED}❌ Build failed: dist directory not found${NC}"
    exit 1
fi

if [ ! -f "dist/index.html" ]; then
    echo -e "${RED}❌ Build failed: index.html not found${NC}"
    exit 1
fi

# Calculate build size
DIST_SIZE=$(du -sh dist | cut -f1)
echo -e "${GREEN}✅ Build completed successfully${NC}"
echo -e "${BLUE}📊 Build size: ${DIST_SIZE}${NC}"

# Generate build report
echo -e "\n${YELLOW}📋 Generating build report...${NC}"

cat > dist/build-info.json << EOF
{
  "version": "$BUILD_VERSION",
  "buildDate": "$BUILD_DATE",
  "nodeVersion": "$NODE_VERSION",
  "npmVersion": "$NPM_VERSION",
  "buildSize": "$DIST_SIZE",
  "buildDuration": "$(($(date +%s) - BUILD_START))s",
  "environment": "production",
  "ltsOptimized": true
}
EOF

# List generated files
echo -e "\n${BLUE}📁 Generated files:${NC}"
find dist -type f -name "*.js" -o -name "*.css" -o -name "*.html" | head -10

# Performance recommendations
echo -e "\n${GREEN}🎯 LTS Build Complete!${NC}"
echo -e "${BLUE}📈 Performance optimizations applied:${NC}"
echo -e "   • Legacy browser support enabled"
echo -e "   • Code splitting optimized for caching"
echo -e "   • Assets compressed and minified"
echo -e "   • Source maps disabled for production"
echo -e "   • Console logs removed"

BUILD_END=$(date +%s)
BUILD_DURATION=$((BUILD_END - BUILD_START))
echo -e "\n${GREEN}⏱️  Total build time: ${BUILD_DURATION}s${NC}"
echo -e "${GREEN}🎉 Ready for deployment!${NC}"
