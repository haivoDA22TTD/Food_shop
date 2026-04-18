#!/bin/bash

echo "🧪 Testing Eureka Server..."
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

EUREKA_URL="http://localhost:8761"

# Test 1: Health Check
echo "1️⃣ Testing Health Check..."
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" $EUREKA_URL/actuator/health)
if [ $HEALTH -eq 200 ]; then
    echo -e "${GREEN}✅ Health check passed${NC}"
else
    echo -e "${RED}❌ Health check failed (HTTP $HEALTH)${NC}"
fi
echo ""

# Test 2: Eureka Dashboard
echo "2️⃣ Testing Eureka Dashboard..."
DASHBOARD=$(curl -s -o /dev/null -w "%{http_code}" $EUREKA_URL)
if [ $DASHBOARD -eq 200 ]; then
    echo -e "${GREEN}✅ Dashboard accessible${NC}"
    echo "   URL: $EUREKA_URL"
else
    echo -e "${RED}❌ Dashboard not accessible (HTTP $DASHBOARD)${NC}"
fi
echo ""

# Test 3: Registered Services
echo "3️⃣ Checking Registered Services..."
SERVICES=$(curl -s $EUREKA_URL/eureka/apps)
if echo "$SERVICES" | grep -q "applications"; then
    echo -e "${GREEN}✅ Eureka API working${NC}"
    
    # Count registered services
    COUNT=$(echo "$SERVICES" | grep -o "<application>" | wc -l)
    echo "   Registered services: $COUNT"
    
    if [ $COUNT -eq 0 ]; then
        echo -e "${YELLOW}   ⚠️  No services registered yet${NC}"
    fi
else
    echo -e "${RED}❌ Eureka API not responding${NC}"
fi
echo ""

# Test 4: Metrics
echo "4️⃣ Testing Metrics Endpoint..."
METRICS=$(curl -s -o /dev/null -w "%{http_code}" $EUREKA_URL/actuator/metrics)
if [ $METRICS -eq 200 ]; then
    echo -e "${GREEN}✅ Metrics endpoint working${NC}"
else
    echo -e "${RED}❌ Metrics endpoint failed (HTTP $METRICS)${NC}"
fi
echo ""

# Summary
echo "📊 Summary:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Eureka Server: $EUREKA_URL"
echo "Dashboard: $EUREKA_URL"
echo "Health: $EUREKA_URL/actuator/health"
echo "Services: $EUREKA_URL/eureka/apps"
echo ""
echo "🎯 Next steps:"
echo "1. Open dashboard: $EUREKA_URL"
echo "2. Start client services (Identity, Product, Order, Payment)"
echo "3. Watch services register in dashboard"
