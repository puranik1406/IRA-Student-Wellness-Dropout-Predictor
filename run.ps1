# Beacon App Launcher
# This script activates the virtual environment and runs the Flask application

Write-Host "🔆 Starting Beacon - Student Dropout Prevention System" -ForegroundColor Cyan
Write-Host ""

# Check if virtual environment exists
if (-Not (Test-Path "venv\Scripts\Activate.ps1")) {
    Write-Host "❌ Virtual environment not found!" -ForegroundColor Red
    Write-Host "Creating virtual environment..." -ForegroundColor Yellow
    python -m venv venv
    Write-Host "✅ Virtual environment created" -ForegroundColor Green
}

# Activate virtual environment
Write-Host "Activating virtual environment..." -ForegroundColor Yellow
& ".\venv\Scripts\Activate.ps1"

# Install requirements if needed
Write-Host "Checking dependencies..." -ForegroundColor Yellow
pip install -r requirements.txt --quiet

# Check if database exists
if (-Not (Test-Path "instance\beacon.db")) {
    Write-Host ""
    Write-Host "⚠️  Database not found!" -ForegroundColor Yellow
    Write-Host "Creating database..." -ForegroundColor Yellow
    python create_database.py
    Write-Host "✅ Database created successfully" -ForegroundColor Green
}

# Run the app
Write-Host ""
Write-Host "🚀 Starting Flask application..." -ForegroundColor Green
Write-Host "📍 Access the application at: http://127.0.0.1:5000" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Gray
Write-Host ""

python app.py
