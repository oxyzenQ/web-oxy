#!/bin/bash

# Currency Converter Development Server Manager
# Usage: ./dev-server.sh [start|stop|restart|status]

# Resolve paths relative to this script, regardless of caller's CWD
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND_DIR="$SCRIPT_DIR/frontend"
BACKEND_PORT=8000
FRONTEND_PORT=3000

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to kill processes on specific ports
kill_port() {
    local port=$1
    local pids=$(lsof -ti:$port)
    if [ ! -z "$pids" ]; then
        echo "Killing processes on port $port: $pids"
        kill -9 $pids 2>/dev/null
        sleep 1
    fi
}

# Function to start backend
start_backend() {
    print_status "Starting FastAPI backend on port $BACKEND_PORT..."
    
    if check_port $BACKEND_PORT; then
        print_warning "Port $BACKEND_PORT is already in use"
        return 1
    fi
    
    if [ ! -d "$BACKEND_DIR" ]; then
        print_error "Backend directory '$BACKEND_DIR' not found"
        return 1
    fi
    
    # Store current directory
    local original_dir=$(pwd)
    
    cd $BACKEND_DIR
    if [ ! -d "venv" ]; then
        print_error "Virtual environment not found. Please run: python -m venv venv"
        cd "$original_dir"
        return 1
    fi
    
    source venv/bin/activate
    nohup uvicorn main:app --reload --host 0.0.0.0 --port $BACKEND_PORT > "$original_dir/backend.log" 2>&1 &
    echo $! > "$original_dir/backend.pid"
    cd "$original_dir"
    
    sleep 2
    if check_port $BACKEND_PORT; then
        print_success "Backend started successfully on http://localhost:$BACKEND_PORT"
        print_status "Backend logs: tail -f backend.log"
        return 0
    else
        print_error "Failed to start backend"
        return 1
    fi
}

# Function to start frontend (now only for backend-only mode)
start_frontend() {
    print_status "⚠️  Frontend auto-start disabled. Use 'npm run preview' in frontend/ directory instead."
    print_status "Backend-only mode: Frontend should be started separately with Vite."
    print_status ""
    print_status "To start frontend:"
    print_status "  cd frontend"
    print_status "  npm install  # if needed"
    print_status "  npm run preview"
    print_status ""
    return 0
}

# Function to stop servers
stop_servers() {
    print_status "Stopping all servers..."
    
    # Kill by PID files
    if [ -f "backend.pid" ]; then
        local backend_pid=$(cat backend.pid)
        if kill -0 $backend_pid 2>/dev/null; then
            kill $backend_pid
            print_success "Backend stopped (PID: $backend_pid)"
        fi
        rm -f backend.pid
    fi
    
    if [ -f "frontend.pid" ]; then
        local frontend_pid=$(cat frontend.pid)
        if kill -0 $frontend_pid 2>/dev/null; then
            kill $frontend_pid
            print_success "Frontend stopped (PID: $frontend_pid)"
        fi
        rm -f frontend.pid
    fi
    
    # Force kill by port
    kill_port $BACKEND_PORT
    kill_port $FRONTEND_PORT
    
    # Kill by process name
    pkill -f "uvicorn main:app" 2>/dev/null
    pkill -f "python -m http.server $FRONTEND_PORT" 2>/dev/null
    
    print_success "All servers stopped"
}

# Function to show status
show_status() {
    print_status "Server Status:"
    
    if check_port $BACKEND_PORT; then
        print_success "✅ Backend running on http://localhost:$BACKEND_PORT"
        print_status "   API Docs: http://localhost:$BACKEND_PORT/docs"
        print_status "   Health: http://localhost:$BACKEND_PORT/api/health"
    else
        print_warning "❌ Backend not running"
    fi
    
    if check_port $FRONTEND_PORT; then
        print_success "✅ Frontend running on http://localhost:$FRONTEND_PORT"
    else
        print_warning "❌ Frontend not running"
    fi
    
    echo ""
    print_status "Quick Commands:"
    echo "  ./dev-server.sh start   - Start both servers"
    echo "  ./dev-server.sh stop    - Stop all servers"
    echo "  ./dev-server.sh restart - Restart all servers"
    echo "  ./dev-server.sh status  - Show this status"
}

# Function to generate new JWT token and save to frontend/.env
generate_token() {
    print_status "Generating new JWT token..."
    
    # Store current directory
    local original_dir=$(pwd)
    
    if [ -d "$BACKEND_DIR" ]; then
        cd $BACKEND_DIR
        if [ -d "venv" ]; then
            source venv/bin/activate
            # Generate token and capture output (first JWT-looking line)
            local token_output=$(python generate_token.py 60 2>/dev/null | grep -m1 "eyJ")
            if [ ! -z "$token_output" ]; then
                # Normalize token (strip newlines/spaces)
                local token_clean=$(echo "$token_output" | tr -d '\r\n ')
                # Return to original directory and go to frontend
                cd "$original_dir"
                cd $FRONTEND_DIR
                # Write a clean .env file with only the required keys
                cat > .env.tmp <<EOF
# Frontend environment (auto-generated)
ENVIRONMENT=development
JWT_TOKEN=$token_clean
EOF
                mv .env.tmp .env
                print_success "JWT token updated in frontend/.env"
            else
                print_error "Failed to generate JWT token"
            fi
        else
            print_error "Virtual environment not found"
        fi
    else
        print_error "Backend directory not found"
    fi
}

# Main script logic
case "${1:-status}" in
    "start")
        stop_servers  # Stop any existing servers first
        generate_token  # Auto-generate JWT token before starting
        start_backend
        if [ $? -eq 0 ]; then
            start_frontend
            if [ $? -eq 0 ]; then
                echo ""
                show_status
                echo ""
                print_status "🚀 Development environment ready!"
                print_status "Press Ctrl+C to stop servers or run: ./dev-server.sh stop"
            fi
        fi
        ;;
    "stop")
        stop_servers
        ;;
    "restart")
        stop_servers
        sleep 2
        $0 start
        ;;
    "status")
        show_status
        ;;
    "token")
        generate_token
        ;;
    *)
        print_error "Usage: $0 {start|stop|restart|status|token}"
        echo ""
        echo "Commands:"
        echo "  start   - Start both backend and frontend servers"
        echo "  stop    - Stop all running servers"
        echo "  restart - Restart all servers"
        echo "  status  - Show server status"
        echo "  token   - Generate new JWT token"
        exit 1
        ;;
esac
