# Universal Java Studio — Standalone Offline Web Application

This folder (`/web-app`) is a complete, standalone, zero-dependency **Offline Progressive Web Application (PWA)** for **Universal Java Studio (JDK 1.0 – JDK 25)**.

## How to Use in a Separate New GitHub Repository
1. Push this repository to GitHub (or copy the contents of `/web-app` into the root of a new GitHub repository).
2. The included GitHub Actions workflow (`.github/workflows/blank.yml`) automatically deploys `/web-app` to **GitHub Pages**.
3. In your GitHub repository, go to **Settings → Pages → Build and deployment → Source** and select **GitHub Actions**.
4. Once you open your `https://<your-username>.github.io/<your-repo>/` link once, the Service Worker (`sw.js`) caches `index.html`, `styles.css`, `app.js`, and `manifest.json` so the link **always works 100% offline**.
