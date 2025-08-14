# Xero Invoice Calculator

A modern Android application built with Jetpack Compose for calculating and displaying invoice data with a clean, user-friendly interface.

## 📱 Screenshots

| Invoice List | Error State | Empty State |
|--------------|---------------|-------------|
| ![Invoice List](https://github.com/madhu2b4u/Invoice/blob/master/Screenshot_20250814_222045.png) | ![Loading State](https://github.com/madhu2b4u/Invoice/blob/master/Screenshot_20250814_222219.png) | ![Empty State](https://github.com/madhu2b4u/Invoice/blob/master/Screenshot_20250814_222151.png) |

## Architecture

### Clean Architecture + MVVM
The project follows **Clean Architecture** principles with **MVVM pattern** to ensure separation of concerns, testability, and maintainability:

```
app/
├── presentation/        # UI Layer (Compose + ViewModels)
├── domain/             # Business Logic (Use Cases)
├── data/              # Data Layer (Repository + Data Sources)
│   ├── repository/    # Repository Implementation
│   ├── source/        # Remote/Local Data Sources
│   └── models/        # Data Models
└── core/              # Shared utilities, Result wrapper, etc.
```

### Why This Architecture?

- **Separation of Concerns**: Each layer has a single responsibility
- **Testability**: Pure functions and dependency injection enable comprehensive testing
- **Scalability**: Easy to add new features without affecting existing code
- **Maintainability**: Clear boundaries make code easier to understand and modify
- **Android Best Practices**: Follows Google's recommended architecture guidelines

### Key Components

#### 1. **Presentation Layer**
- **Jetpack Compose** for modern, declarative UI
- **ViewModels** with **StateFlow** for reactive state management
- **Hilt** for dependency injection
- Separate composables for better reusability and testing

#### 2. **Domain Layer**
- **Use Cases** encapsulate business logic
- **Repository Interfaces** define data contracts
- Pure Kotlin with no Android dependencies

#### 3. **Data Layer**
- **Repository Pattern** for data abstraction
- **Remote Data Source** for API calls
- **Result Wrapper** for consistent error handling

## Data Flow

```
UI → ViewModel → Use Case → Repository → Remote Data Source → API
```

1. **UI** triggers actions via ViewModel
2. **ViewModel** calls appropriate Use Cases
3. **Use Cases** orchestrate business logic and call Repository
4. **Repository** fetches data from Remote Data Source
5. **Data flows back** through the same layers with proper state management

## Trade-offs & Decisions

### Architecture Trade-offs

| Decision | Pros | Cons | Why Chosen |
|----------|------|------|------------|
| **Clean Architecture** | High testability, maintainability, scalability | More boilerplate, initial complexity | Long-term benefits outweigh setup cost |
| **Single Activity + Compose** | Modern UI, better performance, less fragments | Learning curve, newer technology | Future-proof approach, better UX |
| **StateFlow over LiveData** | Coroutine integration, better testing | Slightly more complex | Better async handling |
| **Repository Pattern** | Data source abstraction, easier testing | Additional layer | Enables offline support later |

### Technical Trade-offs

**Chosen: Jetpack Compose**
- ✅ Modern, declarative UI
- ✅ Better performance
- ✅ Excellent preview support
- ❌ Learning curve for teams familiar with Views

**Chosen: Coroutines + Flow**
- ✅ Excellent async handling
- ✅ Reactive programming
- ✅ Cancellation support
- ❌ Complexity for simple operations

**Chosen: Hilt over Manual DI**
- ✅ Compile-time safety
- ✅ Android integration
- ✅ Reduced boilerplate
- ❌ Additional dependency, build time impact

## How to Run

### Prerequisites
- **Android Studio** Koala (2024.1.1) or later
- **JDK 11** or higher
- **Android SDK** with minimum API level 24
- **Kotlin** 2.0.0+

### Setup Instructions

1. **Clone the repository:**
```bash
git clone https://github.com/madhu2b4u/Invoice.git
cd xero-invoice-calculator
```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Sync the project:**
   - Android Studio will automatically prompt to sync
   - Wait for Gradle sync to complete

4. **Run the app:**
   - Connect an Android device or start an emulator
   - Click the "Run" button or use `Shift + F10`


```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Testing

### Testing Strategy
The project includes comprehensive testing at all layers:

- **Unit Tests**: ViewModels, Use Cases, Repositories

### Run Tests
```bash
# Run all unit tests
./gradlew test

# Run tests with coverage
./gradlew testDebugUnitTestCoverageReport

# Run specific test class
./gradlew test --tests "*InvoiceViewModelTest*"
```

## Third-Party Libraries

### Core Dependencies
| Library | Version | Purpose | Why Chosen |
|---------|---------|---------|------------|
| **Jetpack Compose** | `2025.07.00` | UI Framework | Modern, declarative UI |
| **Hilt** | `2.49` | Dependency Injection | Android-first DI solution |
| **Retrofit** | `2.9.0` | HTTP Client | Industry standard for API calls |
| **Kotlin Coroutines** | `1.6.4` | Async Programming | First-class Kotlin async support |
| **OkHttp** | `5.0.0-alpha.2` | HTTP Client | Robust networking with interceptors |
| **Core KTX** | `1.16.0` | Android extensions | Enhanced Kotlin APIs |
| **Lifecycle Runtime** | `2.9.1` | Lifecycle management | Reactive lifecycle handling |

### Testing Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| **MockK** | `1.12.0` | Mocking framework |
| **Coroutines Test** | `1.6.4` | Coroutine testing utilities |
| **JUnit** | `4.13.2` | Testing framework |
| **Truth** | `1.1.3` | Fluent assertions |
| **Core Testing** | `2.2.0` | Architecture components testing |
| **Robolectric** | `4.7.3` | Android unit testing |

### Development Tools
- **Kotlin** `2.0.0` - Primary language
- **Android Gradle Plugin** `8.7.3` - Build system
- **Material 3** - Design system

## Key Features

- **Modern UI**: Clean Material 3 design with Jetpack Compose
- **Invoice Calculation**: Automatic total calculation with currency formatting
- **Data Display**: Organized invoice list with expandable details
- **State Management**: Reactive UI with proper loading/error states
- **Well Tested**: Comprehensive unit tests for business logic
- **Scalable Architecture**: Ready for future feature additions

## Future Enhancements

### Planned Features
- **Offline Support**: Room database integration
- **Search & Filter**: Invoice filtering and search functionality
- **Export Options**: PDF generation and sharing
- **Dark Theme**: Complete dark mode support
- **Accessibility**: Enhanced accessibility features

### Technical Improvements
- **CI/CD Pipeline**: Automated testing and deployment
- **UI Testing**: Comprehensive Compose UI tests
- **Performance**: Lazy loading for large datasets
- **Analytics**: User behavior tracking
- **Crash Reporting**: Production crash monitoring
