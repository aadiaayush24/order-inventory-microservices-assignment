# Build System - Gradle

This project uses **Gradle 8.5** as its build system.

## Why Gradle?

✅ **Faster builds** - Incremental compilation and build caching  
✅ **No installation needed** - Gradle Wrapper included  
✅ **Modern and flexible** - Cleaner syntax than XML  
✅ **Better IDE support** - IntelliJ, VS Code, Eclipse  
✅ **Powerful features** - Task optimization and parallel builds  

## Quick Start

### First Time Setup

```bash
# Navigate to a service
cd inventory-service  # or order-service

# Make wrapper executable (Unix/Mac/Linux)
chmod +x gradlew

# Build (automatically downloads Gradle on first run)
./gradlew build
```

**Note:** The first run downloads Gradle (~100MB) and dependencies. Subsequent builds are much faster!

### Windows

```cmd
# Just run the batch file
gradlew.bat build
```

## Common Commands

| Task | Command | Description |
|------|---------|-------------|
| **Build** | `./gradlew build` | Compile, test, and package |
| **Clean Build** | `./gradlew clean build` | Clean and rebuild everything |
| **Run** | `./gradlew bootRun` | Start the application |
| **Test** | `./gradlew test` | Run all tests |
| **Package** | `./gradlew bootJar` | Create executable JAR |
| **Clean** | `./gradlew clean` | Delete build directory |
| **Tasks** | `./gradlew tasks` | List all available tasks |
| **Help** | `./gradlew help` | Show Gradle help |

## Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests InventoryServiceTest

# Specific test method
./gradlew test --tests 'OrderServiceTest.shouldPlaceOrderSuccessfully'

# Integration tests only
./gradlew test --tests '*IntegrationTest'

# With verbose output
./gradlew test --info

# With test report
./gradlew test
# Report at: build/reports/tests/test/index.html
```

## Build Output

Gradle creates a `build/` directory with:

```
build/
├── classes/          # Compiled .class files
├── libs/             # Generated JAR files
├── reports/          # Test reports, code coverage
├── resources/        # Processed resources
└── tmp/              # Temporary build files
```

**JAR Location:** `build/libs/[service-name]-1.0.0.jar`

## Project Structure

Each service has:

```
service-name/
├── build.gradle              # Build configuration
├── settings.gradle           # Project settings
├── gradlew                   # Wrapper script (Unix/Mac/Linux)
├── gradlew.bat              # Wrapper script (Windows)
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties  # Gradle version
└── src/                     # Source code
    ├── main/
    └── test/
```

## Gradle Wrapper

The Gradle Wrapper ensures everyone uses the same Gradle version:

- **No manual installation needed**
- **Version consistency** across team
- **Included in project** - just run `./gradlew`

### Wrapper Files
- `gradlew` - Unix/Mac/Linux script
- `gradlew.bat` - Windows batch file
- `gradle/wrapper/gradle-wrapper.properties` - Configuration

## Build Configuration

### build.gradle

Key sections:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
}

group = 'com.microservices'
version = '1.0.0'
sourceCompatibility = '17'

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## Gradle Daemon

Gradle runs a background daemon for faster builds:

```bash
# Check daemon status
./gradlew --status

# Stop daemon (if needed)
./gradlew --stop

# Build without daemon
./gradlew build --no-daemon
```

## Performance Tips

1. **Use the daemon** (enabled by default)
2. **Incremental builds** - Only changed files recompile
3. **Parallel execution** - Add to `gradle.properties`:
   ```properties
   org.gradle.parallel=true
   org.gradle.caching=true
   ```

## IDE Integration

### IntelliJ IDEA
- Automatically detects `build.gradle`
- "Reload Gradle Project" if needed
- Run configurations work out of the box

### VS Code
- Install "Gradle for Java" extension
- Gradle tasks appear in sidebar

### Eclipse
- Install Buildship Gradle plugin
- Import as "Existing Gradle Project"

## Troubleshooting

### Permission Denied
```bash
chmod +x gradlew
```

### Wrapper JAR Missing
```bash
# If you have Gradle installed:
gradle wrapper --gradle-version 8.5

# Or download manually (see SETUP_NOTE.md)
```

### Build Fails
```bash
# Clean and refresh
./gradlew clean build --refresh-dependencies

# With stack trace for debugging
./gradlew build --stacktrace
```

### Port Already in Use
```bash
# Stop Gradle daemon
./gradlew --stop

# Find and kill process
lsof -i :8081
kill -9 <PID>
```

### IDE Issues
- **IntelliJ:** File → Reload All Gradle Projects
- **VS Code:** Reload window (Cmd/Ctrl + Shift + P)
- **Eclipse:** Right-click project → Gradle → Refresh

## CI/CD

### GitHub Actions
```yaml
- name: Build with Gradle
  run: |
    chmod +x gradlew
    ./gradlew build
```

### Jenkins
```groovy
stage('Build') {
    steps {
        sh './gradlew clean build'
    }
}
```

## Advanced Usage

### Dependency Management
```bash
# List all dependencies
./gradlew dependencies

# Check for updates
./gradlew dependencyUpdates

# Refresh dependencies
./gradlew build --refresh-dependencies
```

### Build Profiles
```bash
# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Custom Tasks
```bash
# List all tasks
./gradlew tasks --all

# Run specific task
./gradlew compileJava
```

## Resources

- [Official Gradle Docs](https://docs.gradle.org/)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/)
- [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)

## Getting Help

1. **Check documentation:**
   - This file for build system info
   - [GRADLE_INFO.md](GRADLE_INFO.md) for detailed commands
   - [README.md](README.md) for project overview

2. **Run with diagnostics:**
   ```bash
   ./gradlew build --info      # Detailed output
   ./gradlew build --debug     # Debug output
   ./gradlew build --stacktrace  # With stack traces
   ```

3. **Check status:**
   ```bash
   ./gradlew --version
   ./gradlew --status
   ```

---

**Ready to build?**
```bash
./gradlew build && ./gradlew bootRun
```

