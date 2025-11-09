# Render Deployment Fix - 502 Bad Gateway

## Issues Fixed

### 1. **Database Path Issue** ✅

- **Problem**: The `create_database.py` script was creating the database at `instance/ira.db`, but
  the app expected it at `/tmp/ira.db` on Render.
- **Fix**: Updated `create_database.py` to accept a `db_path` parameter and automatically use
  `/tmp/ira.db` when `RENDER` environment variable is set.

### 2. **Gunicorn Configuration** ✅

- **Problem**: Gunicorn wasn't properly binding to the port provided by Render.
- **Fix**: Updated both `render.yaml` and `Procfile` to use:
  ```
  gunicorn --bind 0.0.0.0:$PORT --workers 2 --timeout 120 app:app
  ```
    - `--bind 0.0.0.0:$PORT` ensures it listens on the correct port
    - `--workers 2` provides better performance with multiple workers
    - `--timeout 120` prevents worker timeouts during AI model loading

### 3. **Database Initialization** ✅

- **Problem**: App wasn't properly initializing the database with all required tables.
- **Fix**: Enhanced `app.py` to create all necessary tables if the database doesn't exist, with
  proper fallback handling.

## How to Redeploy

### Option 1: Auto-Deploy (Recommended)

If you have auto-deploy enabled on Render, simply push these changes:

```bash
git add .
git commit -m "Fix 502 error and database initialization"
git push
```

Render will automatically detect the changes and redeploy.

### Option 2: Manual Redeploy

1. Go to your Render dashboard: https://dashboard.render.com
2. Find your `ira-studentwellness` service
3. Click on "Manual Deploy" → "Deploy latest commit"

## Verify Deployment

After deployment completes (usually 2-5 minutes), check:

1. **Build Logs**: Should show:
   ```
   ✅ Database created successfully at /tmp/ira.db!
   📊 Sample data inserted
   ```

2. **Service Logs**: Should show:
   ```
   ✅ Database found at /tmp/ira.db
   Starting IRA - Intuitive Reflection and Alert
   [INFO] Starting gunicorn
   [INFO] Listening at: http://0.0.0.0:10000
   ```

3. **Health Check**: Visit `https://your-app.onrender.com/health`
    - Should return: `{"status": "healthy", "database": "connected"}`

4. **Landing Page**: Visit `https://your-app.onrender.com/`
    - Should load without errors

## Important Notes

### Database on Render

- Render's free tier uses ephemeral storage in `/tmp/`
- **Database will reset on every deployment** (this is expected for free tier)
- For persistent storage, you would need:
    - Render PostgreSQL database (paid add-on), OR
    - External database service, OR
    - Upgrade to paid Render plan with persistent disk

### Environment Variables

Make sure these are set in Render dashboard:

- `SECRET_KEY` - Auto-generated (already set)
- `RENDER=true` - Forces `/tmp/ira.db` path (already set)
- `GEMINI_API_KEY` - Optional, for AI chatbot (set manually if needed)

### Test Credentials

After deployment, you can login with:

**Student Account:**

- Email: `aarav@student.edu`
- Password: `student123`

**Counselor Account:**

- Email: `counselor@ira.edu`
- Password: `counselor123`

## Troubleshooting

If you still see 502 errors:

1. **Check Build Logs**:
    - Go to your service → "Logs" tab → Filter "Build"
    - Look for any errors during build

2. **Check Deploy Logs**:
    - Filter "Deploy" in logs
    - Ensure gunicorn starts successfully
    - Look for port binding messages

3. **Check Service Logs**:
    - Filter "Service" in logs
    - Check for Python errors or crashes

4. **Common Issues**:
    - **Still 502?** Wait 1-2 minutes after deployment completes (AI models loading)
    - **Port binding error?** Ensure `PORT` environment variable is set (Render sets this
      automatically)
    - **Import errors?** Check that all dependencies in `requirements.txt` installed successfully
    - **Database errors?** Check that `/tmp/ira.db` was created during build

## Additional Checks

### Manual Health Check

You can manually verify the health endpoint:

```bash
curl https://your-app.onrender.com/health
```

Should return:

```json
{
  "status": "healthy",
  "ai_models_loaded": false,
  "database": "connected"
}
```

Note: `ai_models_loaded` will be `false` initially (they load in background).

### Check Database Connection

The app now creates all necessary tables on startup. Check the deploy logs for:

```
✅ Database initialized at /tmp/ira.db
```

## Success Checklist

- ✅ Build completes without errors
- ✅ Database created at `/tmp/ira.db`
- ✅ Gunicorn starts and binds to correct port
- ✅ Health check returns 200 OK
- ✅ Landing page loads
- ✅ Can login with test credentials
- ✅ No 502 errors

## Need More Help?

If issues persist after these fixes:

1. Share the **build logs** (first 50 lines and last 50 lines)
2. Share the **deploy logs** (last 100 lines)
3. Share any **error messages** from service logs
4. Provide the **exact URL** showing the error

These logs will help diagnose any remaining issues.
