<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Le Poutine House

Android native Kotlin Jetpack Compose app with a Dockerized Node.js Express API, MySQL database, and phpMyAdmin.

View your app in AI Studio: https://ai.studio/apps/ff6aac3e-33a6-4e2d-9380-8b28cd57b444

## Run the Android App Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

## Run the Docker Backend

**Prerequisites:** Docker Desktop

From the project root, run:

```bash
docker compose up -d --build
```

Services:

- Backend API: http://localhost:3000
- Products endpoint: http://localhost:3000/products
- Health endpoint: http://localhost:3000/health
- phpMyAdmin: http://localhost:8081
- MySQL: localhost:3307

Database credentials:

- Database: `le_poutine_house`
- User: `poutine_user`
- Password: `poutine123`
- Root password: `root`

phpMyAdmin login options:

- Server: `mysql`
- Username: `root`
- Password: `root`

or:

- Server: `mysql`
- Username: `poutine_user`
- Password: `poutine123`

## Android API URLs

Use this URL from the Android emulator:

```text
http://10.0.2.2:3000
```

Use your computer's local network IP for a physical phone on the same Wi-Fi network. Example:

```text
http://192.168.1.25:3000
```

## Test the API

After Docker starts, open:

```text
http://localhost:3000/products
```

You should see the sample Le Poutine House products. The backend automatically creates these tables if they do not exist:

- `products`
- `orders`
- `order_items`

Sample products are inserted only when the `products` table is empty.
