@echo off
echo Starting IRA - Intuitive Reflection and Alert
echo.

REM Activate virtual environment
call venv\Scripts\activate.bat

REM Install requirements
echo Installing/checking dependencies...
pip install -r requirements.txt --quiet

REM Check if database exists
if not exist "instance\ira.db" (
    echo.
    echo Database not found! Creating database...
    python create_database.py
    echo Database created successfully
)

REM Run the app
echo.
echo Starting Flask application...
echo Access the application at: http://127.0.0.1:5000
echo.
echo Press Ctrl+C to stop the server
echo.

python app.py

pause
