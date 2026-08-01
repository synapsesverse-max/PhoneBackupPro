# PhoneBackup Pro

A comprehensive Android backup and restore solution with cross-platform support and WhatsApp recovery from third-party tools.

## Features

### 📱 Complete Phone Backup

- Full device backup (contacts, messages, call logs, media, apps, settings)
- Incremental backups
- Selective backup options
- Encrypted backups (AES-256-GCM)
- Compressed storage

### 💬 WhatsApp Recovery

- Recover from local WhatsApp backups
- Recover from Google Drive backups
- Recover from third-party tools:
  - Dr.Fone (Wondershare)
  - AnyDroid (iMobie)
  - MobileTrans (Wondershare)
  - iMyFone
- Merge multiple backups with deduplication
- Media file recovery

### 🔄 Cross-Platform Transfer

- WiFi Direct transfer
- Bluetooth transfer
- Web server transfer (works with any browser)
- QR code chunked transfer
- Cloud storage sync

### 🔒 Security

- AES-256-GCM encryption
- PBKDF2 key derivation
- Secure key storage
- Integrity verification

### 🎨 Modern UI

- Material Design 3
- Jetpack Compose
- Dark mode support
- Dynamic colors

## Architecture

- Clean Architecture with MVVM
- Jetpack Compose UI
- Hilt Dependency Injection
- Room Database
- Kotlin Coroutines & Flow
- WorkManager for background tasks

## Quick Start

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Gradle 8.4

### Installation

1. Clone the repository:

```bash
git clone https://github.com/yourusername/PhoneBackupPro.git
cd PhoneBackupPro
```

2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator:

```bash
./gradlew :app:installDebug
```

### Build

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK
./gradlew :app:assembleRelease

# Run tests
./gradlew test

# Build all modules
./gradlew build
```

## Project Structure

```text
PhoneBackupPro/
├── app/                          # Main application
│   └── src/main/java/.../pro/   # App source
│       ├── ui/                   # UI screens & components
│       │   ├── navigation/       # Navigation setup
│       │   ├── screens/          # App screens
│       │   ├── components/       # Reusable components
│       │   └── theme/            # Theme & colors
│       └── di/                   # Dependency injection
├── core/                         # Core modules
│   ├── encryption/               # Encryption services
│   ├── compression/              # Compression services
│   ├── database/                 # Room database & DAOs
│   └── network/                  # Network & transfer
├── features/                     # Feature modules
│   ├── backup/                   # Backup engine
│   ├── restore/                  # Restore engine
│   ├── transfer/                 # Cross-platform transfer
│   ├── whatsapp/                 # WhatsApp recovery
│   └── cloud/                    # Cloud storage
├── scripts/                      # Build & utility scripts
├── docs/                         # Documentation
└── .github/                      # GitHub Actions workflows
```

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests with coverage
./gradlew testDebugUnitTestCoverage
```

## CI/CD

GitHub Actions workflows:

- `build.yml`: Builds all modules on push/PR
- `test.yml`: Runs tests daily
- `release.yml`: Creates releases and uploads to Play Store

## Permissions

The app requires these permissions:

- Storage: For backup/restore operations
- Contacts: For contact backup
- SMS: For message backup
- Call Log: For call log backup
- Calendar: For calendar backup
- WiFi/Bluetooth: For device transfer
- Internet: For web server transfer

## Contributing

See CONTRIBUTING.md for contribution guidelines.

## Documentation

- [Architecture](ARCHITECTURE.md)
- [API Documentation](API.md)
- [Contributing Guide](CONTRIBUTING.md)

## License

MIT License - see LICENSE file for details.

## Support

- GitHub Issues: Report bugs or request features
- Discussions: Ask questions and share ideas

## Acknowledgments

- Jetpack Compose for modern UI
- Hilt for dependency injection
- Room for database
- Apache Commons Compress for compression
- OkHttp for networking

Built with ❤️ using Kotlin
