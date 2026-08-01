# Architecture Document

## Overview

PhoneBackup Pro follows Clean Architecture principles with MVVM pattern for the presentation layer.

## Architecture Layers

### 1. Presentation Layer
- **Jetpack Compose UI**: Modern declarative UI framework
- **ViewModels**: State management with StateFlow
- **Navigation**: Compose Navigation component
- **Theme**: Material Design 3 with dynamic colors

### 2. Domain Layer
- **Use Cases**: Business logic implementation
- **Entities**: Core business models
- **Repository Interfaces**: Data access contracts

### 3. Data Layer
- **Room Database**: Local persistence
- **Repositories**: Data operation implementations
- **Data Sources**: Local and remote data access

### 4. Infrastructure Layer
- **Encryption**: AES-256-GCM encryption
- **Compression**: ZIP/GZIP compression
- **Network**: HTTP/WebSocket communication
- **Transfer**: WiFi Direct, Bluetooth, Web Server

## Module Structure

```text
app/ # Main application module
core/ # Core shared functionality
  encryption/ # Encryption services
  compression/ # Compression services
  database/ # Local database
  network/ # Network services
features/
  backup/ # Backup engine
  restore/ # Restore engine
  transfer/ # Cross-platform transfer
  whatsapp/ # WhatsApp recovery
  cloud/ # Cloud storage integration
```

## Dependency Injection

Hilt is used for dependency injection with:
- `@Singleton` scoped services
- `@ViewModelScoped` for ViewModels
- `@ActivityScoped` for activity-level dependencies

## Data Flow

1. UI triggers action via ViewModel
2. ViewModel calls Repository/UseCase
3. Repository accesses Data Sources
4. Data flows back through Flow/StateFlow
5. UI updates automatically via Compose

## Testing Strategy

- **Unit Tests**: JUnit + Mockito for business logic
- **Integration Tests**: Room + DAO tests
- **UI Tests**: Compose testing framework
- **Instrumented Tests**: Android device tests
