# PetCare - CalfTeam

<div align="center">

![PetCare](https://img.shields.io/badge/PetCare-Pet%20Care%20Application-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple)
![Android](https://img.shields.io/badge/Android-24%2B-green)
![Compose](https://img.shields.io/badge/Jetpack-Compose-blue)

Aplikasi mobile untuk memudahkan pencarian dan perawatan hewan peliharaan kesayangan Anda.

[Fitur](#fitur-utama) • [Instalasi](#instalasi) • [Teknologi](#teknologi) • [Kontribusi](#kontribusi)

</div>

---

## 📱 Deskripsi

**PetCare** adalah aplikasi Android modern yang dirancang untuk membantu pengguna dalam mencari, berbagi, dan mengelola informasi tentang hewan peliharaan. Aplikasi ini memungkinkan pengguna untuk:

- Mencari hewan peliharaan yang hilang atau yang ingin diadopsi
- Membagikan postingan tentang hewan kesayangan mereka
- Mengelola profil dan koleksi hewan peliharaan mereka
- Berinteraksi dengan komunitas pecinta hewan lainnya

---

## ✨ Fitur Utama

### Autentikasi & Keamanan
- ✅ Login dan Sign-Up yang aman
- ✅ Manajemen akun pengguna
- ✅ Proteksi data pengguna

### Home & Jelajahi
- ✅ Feed beranda dengan postingan hewan
- ✅ Rekomendasi hewan peliharaan
- ✅ Konten yang dipersonalisasi

### Search & Discovery
- ✅ Pencarian hewan peliharaan berdasarkan kriteria
- ✅ Filter pencarian yang advanced
- ✅ Lokasi berbasis fitur

### Post & Sharing
- ✅ Buat postingan tentang hewan hilang/adopsi
- ✅ Upload foto hewan peliharaan
- ✅ Deskripsi lengkap untuk setiap postingan

### Profile
- ✅ Kelola profil pengguna
- ✅ Riwayat postingan yang telah dibuat
- ✅ Kelola koleksi hewan peliharaan Anda

### Detail & Interaksi
- ✅ Lihat detail lengkap setiap postingan
- ✅ Informasi pemilik dan hewan
- ✅ Hubungi pemilik hewan

---

## 🛠️ Teknologi

### Frontend
- **Kotlin** - Bahasa pemrograman utama
- **Jetpack Compose** - UI Framework modern
- **Material Design 3** - Desain UI terkini
- **Coil** - Image Loading Library

### Backend & Services
- **Appwrite** - Backend-as-a-Service (BaaS)
- **Firebase** - Authentication & Real-time Database (opsional)

### Architecture & Libraries
- **MVVM Architecture** - Model-View-ViewModel
- **ViewModel & LiveData** - State Management
- **Jetpack Navigation** - Navigation Component
- **Lifecycle** - Component lifecycle management

### Development
- **Gradle 8.x** - Build System
- **Android SDK 36** - Target SDK
- **Minimum SDK 24** - Support hingga Android 7.0+

---

## 📋 Requirements

Sebelum memulai, pastikan Anda memiliki:

- **Android Studio** 2024.1 atau lebih baru
- **JDK 11** atau lebih baru
- **Android SDK** dengan API Level 36 dan 24
- **Git** untuk version control
- **Appwrite Account** untuk backend services

---

## ⚙️ Instalasi & Setup

### 1. Clone Repository

```bash
git clone https://github.com/mhrf21/PetCare-CalfTeam.git
cd PetCare-CalfTeam
```

### 2. Buka dengan Android Studio

```bash
# Buka project di Android Studio
# File → Open → Pilih folder PetCare-CalfTeam
```

### 3. Sync Gradle & Dependencies

```bash
# Android Studio akan otomatis mendownload dependencies
# Atau jalankan manual:
./gradlew build
```

### 4. Konfigurasi Backend (Appwrite)

```bash
# Buat akun di https://appwrite.io (cloud atau self-hosted)
# Update konfigurasi Appwrite di project:
# - Endpoint: https://your-appwrite-server/v1
# - Project ID: your-project-id
# - API Key: your-api-key
```

### 5. Build & Run

```bash
# Option 1: Dari Android Studio
# Run → Run 'app'

# Option 2: Dari terminal
./gradlew installDebug
```

---

## 📁 Struktur Project

```
PetCare-CalfTeam/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/calfteam/petcare/
│   │       │   ├── MainActivity.kt           # Entry point utama
│   │       │   ├── data/                     # Data layer (models, repositories)
│   │       │   ├── ui/
│   │       │   │   ├── auth/                 # Login & Sign-Up screens
│   │       │   │   ├── screens/
│   │       │   │   │   ├── home/             # Home screen
│   │       │   │   │   ├── profile/          # Profile screen
│   │       │   │   │   ├── post/             # Post creation screen
│   │       │   │   │   ├── search/           # Search screen
│   │       │   │   │   └── detail/           # Detail screen
│   │       │   │   ├── components/           # Reusable components
│   │       │   │   └── theme/                # UI theme & styling
│   │       │   ├── viewmodel/                # ViewModels untuk state management
│   │       │   └── utils/                    # Utility functions
│   │       ├── res/                          # Resources (strings, colors, etc)
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                    # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔐 Permissions

Aplikasi memerlukan permissions berikut:

```xml
<!-- Internet untuk API calls -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Akses storage untuk upload foto -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Lokasi untuk fitur berbasis lokasi -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

---

## 🚀 Build Variants

### Debug Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

## 📱 Device Requirements

| Requirement | Spesifikasi |
|-------------|-----------|
| Minimum SDK | Android 7.0 (API 24) |
| Target SDK | Android 15 (API 36) |
| RAM | 2GB minimum |
| Storage | 100MB minimum |
| Network | Internet connection |

---

## 🙏 Terima Kasih

Terima kasih telah menggunakan PetCare! Jika aplikasi ini bermanfaat bagi Anda, silakan berikan ⭐ di GitHub kami.

<div align="center">

Made with ❤️ by CalfTeam

</div>
