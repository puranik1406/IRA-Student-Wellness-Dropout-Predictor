# Quick Deploy Guide - Fix 502 Error

## What Was Fixed

✅ **Database path**: Now uses `/tmp/ira.db` on Render (ephemeral storage)
✅ **Gunicorn binding**: Properly binds to `$PORT` with `--bind 0.0.0.0:$PORT`
✅ **Database initialization**: All tables created automatically on startup
✅ **Worker configuration**: 2 workers with 120s timeout for AI model loading

## Deploy Now

Run these commands to deploy the fixes:

```bash
git add .
git commit -m "Fix 502 error: Update database path and gunicorn configuration"
git push origin main
```

If you have auto-deploy enabled on Render, it will automatically redeploy.

Otherwise, go to Render dashboard and click "Manual Deploy".

## After Deployment

1. Wait 2-5 minutes for deployment to complete
2. Visit your app URL: https://your-app-name.onrender.com
3. You should see the landing page load successfully!

## Test Login

**Student**: `aarav@student.edu` / `student123`
**Counselor**: `counselor@ira.edu` / `counselor123`

## Important Note

⚠️ On Render's free tier, the database uses `/tmp/` which is ephemeral storage.
This means **the database will reset on every deployment**. This is normal for free tier.

For persistent storage, you'll need:

- Render PostgreSQL database (paid), OR
- External database service, OR
- Paid Render plan with persistent disk

---

See `RENDER_FIX.md` for detailed troubleshooting guide.
