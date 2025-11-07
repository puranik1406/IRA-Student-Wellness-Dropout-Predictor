#!/usr/bin/env python3
"""
IRA Deployment Readiness Checker
Verifies all requirements and generates deployment secrets
"""

import os
import secrets
import subprocess
import sys

def print_header(text):
    """Print formatted header"""
    print("\n" + "="*60)
    print(f"  {text}")
    print("="*60 + "\n")

def print_success(text):
    """Print success message"""
    print(f"✅ {text}")

def print_warning(text):
    """Print warning message"""
    print(f"⚠️  {text}")

def print_error(text):
    """Print error message"""
    print(f"❌ {text}")

def check_git_repo():
    """Check if this is a git repository"""
    print_header("Checking Git Repository")
    
    try:
        result = subprocess.run(['git', 'rev-parse', '--git-dir'], 
                              capture_output=True, text=True)
        if result.returncode == 0:
            print_success("Git repository detected")
            
            # Check for remote
            result = subprocess.run(['git', 'remote', '-v'], 
                                  capture_output=True, text=True)
            if result.stdout.strip():
                print_success("Git remote configured")
                print(f"   Remote: {result.stdout.strip().split()[1]}")
            else:
                print_warning("No git remote configured")
                print("   Run: git remote add origin <your-repo-url>")
            return True
        else:
            print_error("Not a git repository")
            print("   Initialize with: git init")
            return False
    except FileNotFoundError:
        print_error("Git not installed")
        return False

def check_requirements():
    """Check if requirements.txt exists and is valid"""
    print_header("Checking Requirements")
    
    if not os.path.exists('requirements.txt'):
        print_error("requirements.txt not found")
        return False
    
    print_success("requirements.txt found")
    
    with open('requirements.txt', 'r') as f:
        lines = [line.strip() for line in f if line.strip() and not line.startswith('#')]
        print(f"   {len(lines)} packages listed")
    
    # Check for essential packages
    essential = ['Flask', 'gunicorn', 'transformers', 'torch']
    missing = []
    
    for package in essential:
        if not any(package.lower() in line.lower() for line in lines):
            missing.append(package)
    
    if missing:
        print_warning(f"Missing packages: {', '.join(missing)}")
    else:
        print_success("All essential packages present")
    
    return True

def check_deployment_files():
    """Check if deployment files exist"""
    print_header("Checking Deployment Files")
    
    files = {
        'Procfile': 'Heroku/Railway deployment',
        'render.yaml': 'Render deployment',
        'runtime.txt': 'Python version specification',
        '.env.example': 'Environment variables template'
    }
    
    all_present = True
    for file, desc in files.items():
        if os.path.exists(file):
            print_success(f"{file} - {desc}")
        else:
            print_warning(f"{file} missing - {desc}")
            all_present = False
    
    return all_present

def generate_secret_key():
    """Generate a secure secret key"""
    print_header("Generating Secret Key")
    
    secret = secrets.token_hex(32)
    print_success("Secret key generated!")
    print(f"\n   SECRET_KEY={secret}\n")
    print("   ⚠️  Save this securely and add it to your deployment environment variables!")
    return secret

def check_env_file():
    """Check .env file configuration"""
    print_header("Checking Environment Variables")
    
    if os.path.exists('.env'):
        print_success(".env file found")
        
        with open('.env', 'r') as f:
            content = f.read()
            
            checks = {
                'SECRET_KEY': 'Flask session secret',
                'GEMINI_API_KEY': 'AI chatbot API key (optional)'
            }
            
            for key, desc in checks.items():
                if key in content:
                    if 'your_' in content or 'change_' in content:
                        print_warning(f"{key} needs to be set - {desc}")
                    else:
                        print_success(f"{key} configured - {desc}")
                else:
                    print_warning(f"{key} not found - {desc}")
    else:
        print_warning(".env file not found")
        print("   Copy .env.example to .env and configure values")
        return False
    
    return True

def check_database():
    """Check database setup"""
    print_header("Checking Database Setup")
    
    if os.path.exists('create_database.py'):
        print_success("create_database.py found")
    else:
        print_error("create_database.py missing")
        return False
    
    if os.path.exists('instance/ira.db'):
        print_success("Local database exists")
    else:
        print_warning("Local database not found (will be created on deployment)")
    
    return True

def check_gitignore():
    """Check .gitignore file"""
    print_header("Checking .gitignore")
    
    if not os.path.exists('.gitignore'):
        print_error(".gitignore missing")
        return False
    
    print_success(".gitignore found")
    
    with open('.gitignore', 'r') as f:
        content = f.read()
        
        important = ['.env', '*.db', '__pycache__', 'instance/']
        missing = []
        
        for item in important:
            if item not in content:
                missing.append(item)
        
        if missing:
            print_warning(f"Missing entries: {', '.join(missing)}")
        else:
            print_success("All important entries present")
    
    return True

def print_deployment_instructions():
    """Print deployment instructions"""
    print_header("🚀 Ready to Deploy!")
    
    print("""
Choose your deployment platform:

1. 🟢 RENDER (Recommended - Free Tier)
   - Visit: https://render.com
   - Connect GitHub repository
   - Set build command: pip install -r requirements.txt && python create_database.py
   - Set start command: gunicorn app:app
   - Add environment variables (SECRET_KEY, GEMINI_API_KEY)

2. 🔵 RAILWAY (Fast & Simple - $5 Credit)
   - Visit: https://railway.app
   - Deploy from GitHub
   - Add environment variables
   - Automatic deployment

3. 🟣 HEROKU (Classic - Paid)
   - Install Heroku CLI
   - Run: heroku create ira-app
   - Run: git push heroku main

📚 Full instructions: See DEPLOYMENT.md

⚠️  Important:
   - Set SECRET_KEY in production environment
   - Add GEMINI_API_KEY for AI chatbot
   - Never commit .env file to Git
   - Database resets on free tier (use PostgreSQL for persistence)
""")

def main():
    """Main function"""
    print("""
╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║        🔆 IRA Deployment Readiness Checker 🔆                ║
║    Intuitive Reflection and Alert System                    ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
""")
    
    checks = [
        check_git_repo(),
        check_requirements(),
        check_deployment_files(),
        check_database(),
        check_gitignore(),
    ]
    
    # Generate secret key
    secret_key = generate_secret_key()
    
    # Check env (non-blocking)
    check_env_file()
    
    # Summary
    print_header("Summary")
    
    passed = sum(checks)
    total = len(checks)
    
    if passed == total:
        print_success(f"All checks passed ({passed}/{total})!")
        print_deployment_instructions()
    else:
        print_warning(f"{passed}/{total} checks passed")
        print("\n   Fix the issues above before deploying.")
    
    print("\n" + "="*60 + "\n")

if __name__ == '__main__':
    main()
