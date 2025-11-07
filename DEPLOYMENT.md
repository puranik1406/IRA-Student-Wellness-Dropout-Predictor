# 🚀 IRA Deployment Guide

This guide will walk you through deploying the IRA application to various cloud platforms.

## 📋 Pre-Deployment Checklist

Before deploying, ensure you have:

- ✅ All code committed to a Git repository (GitHub, GitLab, or Bitbucket)
- ✅ A valid `requirements.txt` file
- ✅ Environment variables configured
- ✅ Database initialization script (`create_database.py`)
- ✅ (Optional) Gemini API key for AI chatbot

---

## 🎯 Deployment Options

### Option 1: Render (Recommended - Free Tier Available)

**Why Render?**

- ✅ Free tier available
- ✅ Automatic deployments from Git
- ✅ Simple configuration
- ✅ Good for Python/Flask apps
- ✅ You're already using it!

#### Steps:

1. **Sign up at [Render.com](https://render.com)**
    - Use your GitHub/GitLab account

2. **Create a New Web Service**
    - Click "New +" → "Web Service"
    - Connect your Git repository
    - Select your IRA repository

3. **Configure the Service**
   ```
   Name: ira-studentwellness (or your choice)
   Region: Oregon (or closest to you)
   Branch: main (or your default branch)
   Runtime: Python 3
   Build Command: pip install -r requirements.txt && python create_database.py
   Start Command: gunicorn app:app
   ```

4. **Set Environment Variables**

   Go to "Environment" tab and add:

   | Key | Value | Notes |
      |-----|-------|-------|
   | `SECRET_KEY` | `<random_string>` | Generate using: `python -c "import secrets; print(secrets.token_hex(32))"` |
   | `RENDER` | `true` | Tells app to use `/tmp` for database |
   | `GEMINI_API_KEY` | `<your_api_key>` | Get from [Google AI Studio](https://makersuite.google.com/app/apikey) |
   | `PYTHON_VERSION` | `3.11.0` | Python version |

5. **Deploy**
    - Click "Create Web Service"
    - Wait 5-10 minutes for initial build
    - Your app will be live at: `https://ira-studentwellness.onrender.com`

6. **Auto-Deploy Setup**
    - Enable "Auto-Deploy" in settings
    - Every git push will trigger a new deployment

#### Important Notes:

- ⚠️ Free tier sleeps after 15 minutes of inactivity (first request will be slow)
- ⚠️ Database resets on each deployment (uses `/tmp`)
- 💡 For persistent data, upgrade to paid plan and use PostgreSQL

---

### Option 2: Railway (Great Alternative - Free $5 Credit)

**Why Railway?**

- ✅ $5 free credit monthly
- ✅ Very fast deployments
- ✅ Simple configuration
- ✅ No sleep time on free tier

#### Steps:

1. **Sign up at [Railway.app](https://railway.app)**

2. **Create New Project**
    - Click "New Project"
    - Select "Deploy from GitHub repo"
    - Choose your IRA repository

3. **Configure Environment Variables**

   Add in the "Variables" tab:
   ```
   SECRET_KEY=<generate_random_string>
   GEMINI_API_KEY=<your_api_key>
   PORT=10000
   ```

4. **Deploy**
    - Railway auto-detects Python and uses `Procfile`
    - Deployment takes 2-5 minutes
    - Access your app via the generated URL

---

### Option 3: Heroku (Classic Option - Paid Only Now)

**Note:** Heroku removed free tier in November 2022, but it's still a great option if you have a
paid plan.

#### Steps:

1. **Install Heroku CLI**
   ```bash
   # Windows
   winget install Heroku.HerokuCLI
   
   # Mac
   brew tap heroku/brew && brew install heroku
   
   # Linux
   curl https://cli-assets.heroku.com/install.sh | sh
   ```

2. **Login to Heroku**
   ```bash
   heroku login
   ```

3. **Create Heroku App**
   ```bash
   heroku create ira-studentwellness
   ```

4. **Add Buildpack**
   ```bash
   heroku buildpacks:set heroku/python
   ```

5. **Set Environment Variables**
   ```bash
   heroku config:set SECRET_KEY=<your_secret_key>
   heroku config:set GEMINI_API_KEY=<your_gemini_api_key>
   ```

6. **Deploy**
   ```bash
   git push heroku main
   ```

7. **Open App**
   ```bash
   heroku open
   ```

---

### Option 4: PythonAnywhere (Beginner-Friendly)

**Why PythonAnywhere?**

- ✅ Free tier available
- ✅ Great for beginners
- ✅ Web-based file editor
- ✅ No command line required

#### Steps:

1. **Sign up at [PythonAnywhere.com](https://www.pythonanywhere.com)**

2. **Upload Your Code**
    - Use "Files" tab to upload your project
    - Or clone from GitHub: `git clone <your_repo_url>`

3. **Create Virtual Environment**
   ```bash
   mkvirtualenv --python=/usr/bin/python3.10 ira-env
   pip install -r requirements.txt
   ```

4. **Create Web App**
    - Go to "Web" tab
    - Click "Add a new web app"
    - Choose "Manual configuration"
    - Select Python 3.10

5. **Configure WSGI File**
   Edit `/var/www/yourusername_pythonanywhere_com_wsgi.py`:
   ```python
   import sys
   path = '/home/yourusername/IRA'
   if path not in sys.path:
       sys.path.append(path)
   
   from app import app as application
   ```

6. **Set Environment Variables**
   In web app configuration, add to WSGI file:
   ```python
   import os
   os.environ['SECRET_KEY'] = 'your_secret_key'
   os.environ['GEMINI_API_KEY'] = 'your_gemini_api_key'
   ```

7. **Reload Web App**
    - Click "Reload" button
    - Your app will be live at: `https://yourusername.pythonanywhere.com`

---

## 🔑 Generating Secret Keys

Generate a secure secret key:

**Option 1: Python Command**

```bash
python -c "import secrets; print(secrets.token_hex(32))"
```

**Option 2: Online Generator**
Use: https://randomkeygen.com/

**Option 3: PowerShell (Windows)**

```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | % {[char]$_})
```

---

## 🤖 Getting Gemini API Key

1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the key (starts with `AIza...`)
5. Add it to your environment variables

**Note:** Gemini API has a generous free tier!

---

## 🗄️ Database Considerations

### Development (SQLite)

- Default: `instance/ira.db`
- Good for testing and development

### Production (Render/Railway - Free Tier)

- Uses `/tmp/ira.db` (ephemeral storage)
- ⚠️ Database resets on each deployment
- ✅ Fine for demos and testing

### Production (Persistent Database)

For production with data persistence, upgrade to:

**PostgreSQL on Render:**

```bash
# Add PostgreSQL database in Render dashboard
# Update DATABASE_URI environment variable
DATABASE_URI=postgresql://user:pass@host:port/dbname
```

**Install PostgreSQL adapter:**
Add to `requirements.txt`:

```
psycopg2-binary
```

Update `app.py` to use PostgreSQL instead of SQLite.

---

## 🐛 Troubleshooting

### Build Fails on Render/Railway

**Issue:** PyTorch/transformers installation timeout

**Solution:**

1. Increase build timeout in dashboard settings
2. Or use smaller models in `ai_models/`

### Database Not Found

**Issue:** `instance/ira.db not found`

**Solution:**

- Ensure `create_database.py` runs in build command
- Check environment variable `RENDER=true` is set

### Port Already in Use (Local)

**Solution:**

```bash
# Change port in app.py
app.run(host='0.0.0.0', port=5001)
```

### AI Models Not Loading

**Issue:** Models not downloading during build

**Solution:**

- Increase timeout
- Models download on first run (500MB-1GB)
- Check Render logs for download progress

### Gemini API Not Working

**Issue:** "API key not configured"

**Solution:**

1. Verify `GEMINI_API_KEY` is set in environment variables
2. Check key is valid at Google AI Studio
3. Restart application after setting variable

---

## 📊 Monitoring Your Deployment

### Render

- View logs: Dashboard → Logs tab
- Monitor metrics: Dashboard → Metrics tab

### Railway

- View logs: Project → Deployments → View Logs
- Check usage: Project → Usage tab

### Heroku

```bash
heroku logs --tail
heroku ps
```

---

## 🔄 Updating Your Deployment

### Automatic (Render/Railway with Auto-Deploy)

```bash
git add .
git commit -m "Update features"
git push origin main
# Deployment triggers automatically
```

### Manual (Heroku)

```bash
git push heroku main
```

---

## 🎉 Post-Deployment

After successful deployment:

1. ✅ Test all features (login, dashboard, AI chatbot)
2. ✅ Create demo accounts
3. ✅ Share your deployed URL!
4. ✅ Monitor logs for errors
5. ✅ Set up custom domain (optional)

---

## 📧 Need Help?

- **Render Docs:** https://render.com/docs
- **Railway Docs:** https://docs.railway.app
- **Heroku Docs:** https://devcenter.heroku.com

---

## 🌟 Pro Tips

1. **Environment Variables**: Never commit `.env` file to Git
2. **Database**: Use PostgreSQL for production
3. **Monitoring**: Set up error tracking (e.g., Sentry)
4. **Performance**: Enable caching for AI models
5. **Security**: Always use HTTPS in production (automatic on Render/Railway)

---

**Built with ❤️ - Good luck with your deployment!** 🚀
