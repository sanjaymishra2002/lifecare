#!/bin/bash

# Example Deployment Script for LifeCare Android App
# This script should be placed on your server and triggered by webhooks

set -e  # Exit on error

# Configuration
APP_DIR="/var/www/lifecare"
GIT_BRANCH="main"
LOG_FILE="/var/log/lifecare-deployment.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$LOG_FILE"
}

# Main deployment function
deploy() {
    log "Starting deployment..."
    
    # Navigate to app directory
    if [ ! -d "$APP_DIR" ]; then
        error "Application directory $APP_DIR does not exist"
        exit 1
    fi
    
    cd "$APP_DIR" || exit 1
    log "Changed to directory: $APP_DIR"
    
    # Fetch latest changes
    log "Fetching latest changes from Git..."
    git fetch origin || {
        error "Failed to fetch from origin"
        exit 1
    }
    
    # Check current branch
    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
    if [ "$CURRENT_BRANCH" != "$GIT_BRANCH" ]; then
        warning "Current branch ($CURRENT_BRANCH) differs from target branch ($GIT_BRANCH)"
        log "Checking out $GIT_BRANCH..."
        git checkout "$GIT_BRANCH" || {
            error "Failed to checkout branch $GIT_BRANCH"
            exit 1
        }
    fi
    
    # Get current commit hash before pulling
    OLD_COMMIT=$(git rev-parse HEAD)
    log "Current commit: $OLD_COMMIT"
    
    # Pull latest changes
    log "Pulling latest changes..."
    git pull origin "$GIT_BRANCH" || {
        error "Failed to pull changes"
        exit 1
    }
    
    # Get new commit hash after pulling
    NEW_COMMIT=$(git rev-parse HEAD)
    log "New commit: $NEW_COMMIT"
    
    # Check if there were any changes
    if [ "$OLD_COMMIT" = "$NEW_COMMIT" ]; then
        log "No changes detected. Deployment complete."
        exit 0
    fi
    
    # Show what changed
    log "Changes in this deployment:"
    git log --oneline "$OLD_COMMIT..$NEW_COMMIT" | tee -a "$LOG_FILE"
    
    # Optional: Build the application
    # Uncomment the following lines if you want to build on the server
    # log "Building application..."
    # chmod +x gradlew
    # ./gradlew assembleRelease --no-daemon || {
    #     error "Build failed"
    #     exit 1
    # }
    # log "Build completed successfully"
    
    # Optional: Run tests
    # Uncomment if you want to run tests before deployment
    # log "Running tests..."
    # ./gradlew test --no-daemon || {
    #     error "Tests failed"
    #     exit 1
    # }
    # log "Tests passed"
    
    # Optional: Copy APK to distribution directory
    # Uncomment and modify as needed
    # if [ -d "app/build/outputs/apk/release" ]; then
    #     log "Copying APK files..."
    #     cp -r app/build/outputs/apk/release/* /var/www/downloads/
    #     log "APK files copied to distribution directory"
    # fi
    
    # Send notification (optional)
    # Uncomment and configure your notification method
    # log "Sending deployment notification..."
    # curl -X POST "https://your-notification-service.com/notify" \
    #     -H "Content-Type: application/json" \
    #     -d "{\"message\":\"LifeCare deployed successfully\",\"commit\":\"$NEW_COMMIT\"}"
    
    log "Deployment completed successfully!"
}

# Error handler
trap 'error "Deployment failed at line $LINENO"' ERR

# Run deployment
deploy

exit 0
