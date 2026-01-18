# Aegis 🛡️

**Secure Remote Linux Locking via Android**

Aegis is a cross-platform security utility that allows you to remotely trigger `hyprlock` on your Linux desktop from an Android device. It combines a **Rust-based** server with a **Material 3** Android app, connected securely over the internet via **Tailscale Funnel**.



---

## ✨ Features

* **Hyprland Aesthetic:** UI designed with the Hyprland tiling window manager in mind, featuring custom border gaps and a minimalist layout.
* **Heavy Haptic Feedback:** Uses advanced Android `VibrationEffect` to provide tactile confirmation of a successful lock.
* **First-Time Setup:** Securely set your PIN on the first run; no hardcoded credentials.
* **Zero-Trust Networking:** All requests are authenticated via a pre-shared `X-Api-Key` and tunneled through HTTPS.
* **Memory Safe:** Backend built with Rust for high performance and safety.

---

## 🛠️ Architecture

* **Mobile App:** Android application built with Kotlin, ViewBinding, and OkHttp.
* **Server:** Lightweight Axum server running on Rust to execute system commands.
* **Tunnel:** Tailscale Funnel to expose the local server securely to the public web.

---

## 🚀 Getting Started

### 1. Laptop Setup (Rust)
1. Navigate to the `laptop_lock` directory.
2. Create a `.env` file based on `.env.example`:
```env
API_KEY=your_secure_random_string
PORT=8080
```


3. Run the server:
```bash
cargo run --release

```


4. In a separate terminal, start the tunnel:
```bash
tailscale funnel 8080

```



### 2. Mobile Setup (Android)

1. Open the project in Android Studio.
2. Add your secrets to `local.properties`:
```properties
LOCK_URL=[https://your-tailnet-node.ts.net/lock](https://your-tailnet-node.ts.net/lock)
API_KEY=your_secure_random_string

```


3. Build and install the APK on your device.

---

## 🔒 Security Requirements

* **Tailscale** must be installed and authenticated on the host machine.
* **hyprlock** must be installed and accessible in the system PATH.
* Both apps must share the exact same `API_KEY`.

---

<p align="center">
<img src="https://media1.tenor.com/m/-EMFz5rTDkYAAAAC/charizard-pokemon-charizard.gif" alt="Alt text" width="100" height="50" />
</p>

<div align="center">

`Made with ❤️‍🔥 by Nish Deshmukh`

</div>

