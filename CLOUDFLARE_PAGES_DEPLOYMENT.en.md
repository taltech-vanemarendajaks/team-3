# Cloudflare Pages Deployment Guide

Complete guide for deploying Next.js application to Cloudflare Pages.

## 📋 Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Project Preparation](#project-preparation)
- [Cloudflare Pages Configuration](#cloudflare-pages-configuration)
- [OAuth Configuration](#oauth-configuration)
- [Code Changes](#code-changes)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

This project uses Next.js 15 with App Router, configured for deployment on Cloudflare Pages via `@cloudflare/next-on-pages`. All API routes and dynamic pages are configured to work in Edge Runtime.

### Key Components:

- ✅ **App Router** (Next.js 15)
- ✅ **Middleware** for authentication
- ✅ **SSR** via Server Components
- ✅ **Edge Runtime** for all API routes
- ✅ **OAuth2** authentication via Google

---

## 📦 Prerequisites

- GitHub repository with your project
- Cloudflare account
- Google OAuth credentials (for OAuth)
- Backend API deployed and publicly accessible

---

## 🛠 Project Preparation

### 1. Install Dependencies

```bash
cd frontend
npm install -D @cloudflare/next-on-pages --legacy-peer-deps
```

> **Note**: `--legacy-peer-deps` is required due to version incompatibility between Next.js 15.5.7 and `@cloudflare/next-on-pages@1.13.16`. The package is marked as deprecated but works.

### 2. Add Build Script

In `frontend/package.json` add:

```json
{
  "scripts": {
    "pages:build": "npx @cloudflare/next-on-pages"
  }
}
```

### 3. Create `.npmrc`

Create file `frontend/.npmrc`:

```
legacy-peer-deps=true
```

This ensures `legacy-peer-deps` is used in all npm operations.

### 4. Update `next.config.ts`

Ensure `next.config.ts` does **NOT** contain `output: 'standalone'`:

```typescript
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  // DO NOT add output: 'standalone' - not needed for Cloudflare Pages
};

export default nextConfig;
```

---

## ⚙️ Cloudflare Pages Configuration

### Step 1: Create Project

1. Open [Cloudflare Dashboard](https://dash.cloudflare.com/)
2. Go to **Workers & Pages** → **Pages**
3. Click **"Create application"** → **"Connect to Git"**
4. Select your GitHub repository

### Step 2: Build Configuration

When configuring the project, specify:

| Parameter | Value |
|----------|-------|
| **Project name** | `vanemarendaja-borsibaar` (or your choice) |
| **Production branch** | `main` (or `dev`) |
| **Root directory** | `frontend` |
| **Framework preset** | `None` (or `Next.js` if available) |
| **Build command** | `npm install && npm run pages:build` |
| **Build output directory** | `.vercel/output/static` |

### Step 3: Compatibility Flags

**Critical!** Add compatibility flag:

1. Open project in Cloudflare Pages
2. Go to **Settings** → **Functions** → **Compatibility Flags**
3. Add flag: **`nodejs_compat`**
4. Ensure flag is added for **Production** and **Preview** environments

> ⚠️ **Without this flag** you will get error: `no nodejs_compat compatibility flag set`

### Step 4: Environment Variables

In **Environment variables** section add:

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_BACKEND_URL` | Public URL of your backend API | `https://api.yourdomain.com` |

---

## 🔐 OAuth Configuration

### 1. Google OAuth Setup

1. Open [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project
3. Go to **APIs & Services** → **Credentials**
4. Open your **OAuth 2.0 Client ID**
5. In **"Authorized redirect URIs"** section add:

```
https://your-backend-url/login/oauth2/code/google
```

Example:
```
https://api.yourdomain.com/login/oauth2/code/google
```

### 2. Backend Configuration

Ensure the following environment variables are correctly configured in backend:

```env
# Frontend application URL (WITHOUT trailing slash!)
app.frontend.url=https://vanemarendaja-borsibaar.pages.dev

# Allowed origins for CORS
APP_CORS_ALLOWED_ORIGINS=https://vanemarendaja-borsibaar.pages.dev,https://www.yourdomain.com
```

> ⚠️ **Important**: `app.frontend.url` must be **without trailing slash**, otherwise there will be issues with redirect URLs.

---

## 📝 Code Changes

### Changes for Cloudflare Pages

#### 1. `frontend/package.json`

Added build script:

```json
{
  "scripts": {
    "pages:build": "npx @cloudflare/next-on-pages"
  },
  "devDependencies": {
    "@cloudflare/next-on-pages": "^1.13.16"
  }
}
```

#### 2. `frontend/.npmrc` (new file)

```
legacy-peer-deps=true
```

#### 3. `frontend/next.config.ts`

Removed `output: 'standalone'`:

```typescript
const nextConfig: NextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  // output: 'standalone' removed - not needed for Cloudflare Pages
};
```

#### 4. Added `export const runtime = 'edge'` to all API routes

All 22 API routes in `frontend/app/api/**/route.ts` now contain:

```typescript
export const runtime = 'edge';
```

Example:
```typescript
import { NextRequest, NextResponse } from "next/server";
import { backendUrl } from "@/utils/constants";

export const runtime = 'edge'; // ✅ Added

export async function GET(request: NextRequest) {
  // ... code
}
```

#### 5. Added `export const runtime = 'edge'` to dynamic pages

- `frontend/app/(public)/login/page.tsx`
- `frontend/app/(protected)/(sidebar)/pos/[stationId]/page.tsx`

Example:
```typescript
export const runtime = 'edge'; // ✅ Added
export const dynamic = "force-dynamic";

export default function LoginPage() {
  // ... code
}
```

### Summary of Changed Files

- ✅ **1 new file**: `frontend/.npmrc`
- ✅ **2 config files**: `package.json`, `next.config.ts`
- ✅ **22 API routes**: all `app/api/**/route.ts`
- ✅ **2 pages**: `login/page.tsx`, `pos/[stationId]/page.tsx`

**Total**: 27 files changed/created

---

## 🐛 Troubleshooting

### Problem: `cd: can't cd to frontend`

**Symptoms**: Build fails with error `can't cd to frontend`

**Solution**: 
- Ensure **Root directory** is set to `frontend`
- **Build command** should be: `npm install && npm run pages:build` (without `cd frontend`)

---

### Problem: `no nodejs_compat compatibility flag set`

**Symptoms**: When opening page you see error about nodejs_compat

**Solution**:
1. Open project in Cloudflare Dashboard
2. **Settings** → **Functions** → **Compatibility Flags**
3. Add flag: `nodejs_compat`
4. Save and restart build

---

### Problem: `redirect_uri_mismatch` in OAuth

**Symptoms**: Google OAuth shows error `Error 400: redirect_uri_mismatch`

**Solution**:
1. Ensure correct redirect URI is added in Google OAuth Console:
   ```
   https://your-backend-url/login/oauth2/code/google
   ```
2. Check that `app.frontend.url` in backend is **without trailing slash**
3. Wait a few minutes after changing Google OAuth settings

---

### Problem: Double slash in URL (`//api/auth/callback`)

**Symptoms**: 404 error on `/api/auth/callback`, logs show `//api`

**Solution**:
- Ensure `app.frontend.url` in backend is **without trailing slash**:
  ```
  ✅ Correct: https://vanemarendaja-borsibaar.pages.dev
  ❌ Incorrect: https://vanemarendaja-borsibaar.pages.dev/
  ```

---

### Problem: `@cloudflare/next-on-pages` deprecated warning

**Symptoms**: See warning that package is deprecated

**Solution**:
- This is normal. Package is marked as deprecated but continues to work with Next.js 15.5.7
- Can migrate to `@opennextjs/cloudflare` in the future, but requires additional setup

---

### Problem: Build succeeds but shows "Hello world"

**Symptoms**: After successful build you only see "Hello world"

**Solution**:
- Ensure you are using **Cloudflare Pages**, not **Workers**
- URL should be `*.pages.dev`, not `*.workers.dev`
- Ensure **Deploy command** is empty or contains `true`, not `npx wrangler deploy`

---

### Problem: Routes not configured for Edge Runtime

**Symptoms**: Build fails with error that routes are not configured for Edge Runtime

**Solution**:
- Ensure all API routes contain `export const runtime = 'edge'`
- Ensure dynamic pages (`/login`, `/pos/[stationId]`) contain `export const runtime = 'edge'`

---

## ✅ Deployment Checklist

Before deployment ensure:

- [ ] `@cloudflare/next-on-pages` installed in `devDependencies`
- [ ] `.npmrc` created with `legacy-peer-deps=true`
- [ ] `pages:build` script added to `package.json`
- [ ] `output: 'standalone'` removed from `next.config.ts`
- [ ] All API routes contain `export const runtime = 'edge'`
- [ ] Dynamic pages contain `export const runtime = 'edge'`
- [ ] Cloudflare Pages project created with correct Root directory
- [ ] Compatibility flag `nodejs_compat` added
- [ ] Environment variables configured (`NEXT_PUBLIC_BACKEND_URL`)
- [ ] Google OAuth redirect URI configured in Google Console
- [ ] Backend `app.frontend.url` configured without trailing slash
- [ ] CORS configured in backend for Pages domain

---

## 📚 Useful Links

- [Cloudflare Pages Documentation](https://developers.cloudflare.com/pages/)
- [@cloudflare/next-on-pages GitHub](https://github.com/cloudflare/next-on-pages)
- [Next.js Edge Runtime](https://nextjs.org/docs/app/building-your-application/rendering/edge-and-nodejs-runtimes)
- [Google OAuth Setup](https://console.cloud.google.com/apis/credentials)

---

## 🔄 Deployment Process

1. **Git Push**: Changes are committed and pushed to repository
2. **Automatic Build**: Cloudflare Pages automatically triggers build on push to production branch
3. **Build Process**:
   - Clones repository
   - Installs dependencies
   - Runs `npm run pages:build`
   - Creates `.vercel/output/static` with Edge Functions and static files
4. **Automatic Deploy**: Cloudflare Pages automatically deploys the result

---

## 📊 Build Output Structure

After successful build creates:

```
.vercel/output/static/
├── _worker.js/           # Edge Functions and Middleware
│   ├── index.js
│   └── nop-build-log.json
├── *.html               # Prerendered pages
├── _next/               # Next.js assets
└── ...
```

---

## 🎉 Done!

After completing all steps your Next.js application will be deployed on Cloudflare Pages and accessible at:

```
https://your-project.pages.dev
```

All API routes and dynamic pages will work via Edge Runtime, providing fast and global availability.

---

**Last updated**: January 2026  
**Next.js version**: 15.5.7  
**@cloudflare/next-on-pages version**: 1.13.16
