# Contributing to FitMate

Thank you for your interest in contributing!

FitMate welcomes bug fixes, new features, documentation improvements, UI enhancements, and performance improvements.

## Getting Started

1. Fork the repository on GitHub.
2. Clone your fork:
   `git clone https://github.com/YOUR_USERNAME/FitMate.git`
3. Enter the project directory:
   `cd FitMate`
4. Create a branch:
   `git checkout -b feature/your-feature-name`

## Development Guidelines

- Follow the existing project structure and coding conventions.
- Keep code readable and maintainable.
- Do not commit API keys, credentials, local databases, generated files, or machine-specific configuration.
- Avoid unnecessary dependencies.
- Update documentation when changing setup instructions or application behavior.

## Testing

Run relevant checks for the part of the project you modify.

Android:
`cd android && ./gradlew testDebugUnitTest assembleDebug`

Backend:
`cd backend && python -m pytest`

Desktop: Build and run the Gradle project in the desktop directory.

iOS: Open `ios/FitMate.xcodeproj` in Xcode, build, and test using a simulator or device.

Mention any tests you could not run in your pull request.

## Submitting a Pull Request

1. Commit your changes with a clear commit message.
2. Push your branch to your fork.
3. Open a pull request against the original repository's `main` branch.
4. Explain the problem and describe your changes.
5. Include screenshots for UI changes where appropriate.
6. Mention tests performed and known limitations.

## Reporting Bugs and Suggesting Features

Use GitHub Issues. Include reproduction steps, expected and actual behavior, and relevant screenshots or logs.

## Code of Conduct

Contributors are expected to follow [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
