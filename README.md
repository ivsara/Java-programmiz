# Universal Java Studio — All-Version Offline Java IDE (JDK 1.0 – JDK 25)

**Universal Java Studio** is a complete offline Java programming IDE, multi-version compiler/interpreter (`JDK 1.0` through `JDK 25`), API documentation browser, Java Version Explorer, and 25-category practice laboratory.

---

## How to Deploy to GitHub & Make Your Link Always Work Offline

1. **Push to GitHub from AI Studio:**
   - Click the **GitHub / Export** icon in the top-right toolbar of Google AI Studio and select **Push to GitHub** to create or update your GitHub repository.
2. **Automatic GitHub Actions Build & Offline GitHub Pages Deployment:**
   - This repository includes `.github/workflows/deploy-offline.yml`.
   - On every push to `main` or `master`, GitHub Actions automatically:
     1. Compiles the native **Offline Android APK** (`UniversalJavaStudio-Offline.apk`) and bundles it into the site (`docs/downloads/UniversalJavaStudio-Offline.apk`) as well as workflow artifacts.
     2. Deploys the **Offline-First Progressive Web App (`docs/`)** to **GitHub Pages** (`https://<your-github-username>.github.io/<your-repo-name>/`).
3. **One-Time GitHub Pages Settings Check:**
   - In your GitHub repository, go to **Settings → Pages** and set **Build and deployment → Source** to **GitHub Actions** (or **Deploy from a branch → `main` / `/docs`**).
4. **Works 100% Offline When Clicked:**
   - Once you open your `https://<your-github-username>.github.io/<your-repo-name>/` link once, the built-in **Service Worker (`docs/sw.js`)** caches the entire IDE, compiler, tutorials, and version explorer on your device—and you can also click **"Install Offline App"** or **"Download Android APK"** to use it anytime without internet access.
