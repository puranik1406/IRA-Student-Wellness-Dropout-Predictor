# ⚡ Quick Deploy Guide - Get Live in 10 Minutes!

## 🎯 Fastest Way to Deploy IRA

### Option 1: Render (Recommended) ⭐

**Already connected!** Your repo is
at: https://github.com/puranik1406/IRA-Student-Wellness-Dropout-Predictor.git

#### Steps:

1. **Go to Render Dashboard**
    - Visit: https://dashboard.render.com
    - Log in with GitHub

2. **Create New Web Service**
    - Click "New +" → "Web Service"
    - Connect repository: `puranik1406/IRA-Student-Wellness-Dropout-Predictor`

3. **Configure (Copy-Paste These)**
   ```
   Name: ira-studentwellness
   Region: Oregon (or closest to you)
   Branch: main
   Runtime: Python 3
   Build Command: pip install -r requirements.txt && python create_database.py
   Start Command: gunicorn app:app
   Instance Type: Free
   ```

4. **Add Environment Variables** (IMPORTANT!)

   Click "Advanced" → "Add Environment Variable":

   ```
   SECRET_KEY = 9f2104027a5a8335f8cf9f35696f92d41febdf529fd0f5b32c7a9bca9c4c6f12
   RENDER = true
   GEMINI_API_KEY = <your_api_key_or_leave_blank>
   PYTHON_VERSION = 3.11.0
   ```

   *(Use the SECRET_KEY generated above, or run `python deploy_check.py` to get a new one)*

5. **Deploy!**
    - Click "Create Web Service"
    - Wait 5-10 minutes (first deploy takes longer due to AI model downloads)
    - Your app will be live! 🎉

6. **Access Your App**
    - URL will be: `https://ira-studentwellness.onrender.com`
    - Or your custom name

---

### Option 2: Railway (Fast Alternative) 🚂

1. **Go to Railway**
    - Visit: https://railway.app
    - Sign in with GitHub

2. **New Project**
    - Click "New Project"
    - Select "Deploy from GitHub repo"
    - Choose: `puranik1406/IRA-Student-Wellness-Dropout-Predictor`

3. **Add Environment Variables**

   In Variables tab:
   ```
   SECRET_KEY = 9f2104027a5a8335f8cf9f35696f92d41febdf529fd0f5b32c7a9bca9c4c6f12
   GEMINI_API_KEY = <your_api_key>
   PORT = 10000
   ```

4. **Deploy**
    - Railway auto-detects and deploys
    - Takes 3-5 minutes
    - Click on deployment to get your URL

---

## 🔑 Get Gemini API Key (Optional but Recommended)

For the AI chatbot to work:

1. Visit: https://makersuite.google.com/app/apikey
2. Sign in with Google
3. Click "Create API Key"
4. Copy the key (starts with `AIza...`)
5. Add to environment variables as `GEMINI_API_KEY`

**It's FREE!** The chatbot will work in fallback mode without it, but full features need the key.

---

## ✅ Test Your Deployment

Once live, test with these credentials:

**Student Login:**

- Email: `aarav@student.edu`
- Password: `student123`

**Counselor Login:**

- Email: `counselor@ira.edu`
- Password: `counselor123`

Test these features:

- ✅ Student Dashboard
- ✅ Mood Logging
- ✅ Journal Entries
- ✅ AI Chatbot (voice + text)
- ✅ Schedule Meeting
- ✅ Counselor Dashboard

---

## 🔄 Update Your Deployment

After making changes:

```bash
git add .
git commit -m "Your changes"
git push origin main
```

**Render/Railway will auto-deploy** (if auto-deploy is enabled)

---

## 🐛 Quick Troubleshooting

### "Application Error" / 500 Error

- Check logs in dashboard
- Verify environment variables are set
- Wait for build to complete (10 min first time)

### "Database not found"

- Ensure build command includes: `python create_database.py`
- Check `RENDER=true` is set in environment

### AI Chatbot Not Working

- Add `GEMINI_API_KEY` to environment variables
- Restart the service
- Check key is valid at Google AI Studio

### Build Timeout

- First deployment takes 10-15 minutes (AI models download ~1GB)
- Be patient! It only happens once
- Check logs for progress

---

## 📊 Monitoring Your App

### Render:

- Logs: Dashboard → Your Service → Logs
- Metrics: Dashboard → Your Service → Metrics
- Restart: Dashboard → Your Service → Manual Deploy → Deploy Latest Commit

### Railway:

- Logs: Project → Deployments → View Logs
- Usage: Project → Usage

---

## 🎉 You're Live!

Share your app:

- 🔗 Live URL: `https://your-app.onrender.com`
- 📱 Works on mobile!
- 🌐 Accessible worldwide

**Pro Tip:** Star your GitHub repo and share it on social media! 🌟

---

## 📚 Need More Help?

- **Full Guide:** See `DEPLOYMENT.md`
- **Check Readiness:** Run `python deploy_check.py`
- **GitHub Issues:** Open an issue on your repo

---

**Built with ❤️ - Happy Deploying!** 🚀
