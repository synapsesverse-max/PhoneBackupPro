# Contributing to PhoneBackup Pro

## Getting Started

1. Fork the repository
2. Clone your fork:

```bash
git clone https://github.com/YOUR_USERNAME/PhoneBackupPro.git
```

3. Create a feature branch:

```bash
git checkout -b feature/your-feature-name
```

## Development Setup

- Install Android Studio Hedgehog or later
- Install JDK 17
- Set up Android SDK 34

Run the project configuration:

```bash
./gradlew build
```

## Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Use extension functions where appropriate

## Commit Guidelines

Use conventional commits:

- `feat:` for new features
- `fix:` for bug fixes
- `docs:` for documentation
- `test:` for tests
- `refactor:` for code refactoring

## Pull Request Process

1. Update documentation if needed
2. Add/update tests
3. Ensure all tests pass: `./gradlew test`
4. Update CHANGELOG.md
5. Create PR with clear description

## Testing

- Write unit tests for business logic
- Write instrumented tests for Android-specific code

Run tests before submitting PR:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Building

```bash
# Debug build
./gradlew :app:assembleDebug

# Release build
./gradlew :app:assembleRelease

# Run tests
./gradlew test

# Lint check
./gradlew lint
```

## Project Structure

See ARCHITECTURE.md for detailed project structure.

## Questions?

Open an issue or discussion on GitHub.
