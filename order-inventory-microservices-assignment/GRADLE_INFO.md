# Gradle Build Information

This project uses **Gradle 8.5** as the build tool. The Gradle Wrapper is included, so you don't need to install Gradle separately.

## Quick Start

Both services include the Gradle Wrapper scripts:
- **Unix/Linux/Mac:** `./gradlew`
- **Windows:** `gradlew.bat`

### First Time Setup

The first time you run any `./gradlew` command, it will automatically download Gradle 8.5. This is a one-time operation.

```bash
cd inventory-service
./gradlew build  # Downloads Gradle on first run
```

## Common Gradle Commands

### Building

```bash
# Clean and build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Build JAR file
./gradlew bootJar
```

### Running

```bash
# Run the application
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests InventoryServiceTest

# Run specific test method
./gradlew test --tests 'OrderServiceTest.shouldPlaceOrderSuccessfully'

# Run tests matching pattern
./gradlew test --tests '*IntegrationTest'

# Run tests with coverage
./gradlew test jacocoTestReport
```

### Other Useful Commands

```bash
# List all tasks
./gradlew tasks

# List dependencies
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates

# View project info
./gradlew properties

# Clean build directory
./gradlew clean
```

## Build File Structure

### build.gradle

The `build.gradle` file contains:
- **Plugins** - Spring Boot, Dependency Management, Java
- **Dependencies** - All required libraries
- **Repositories** - Maven Central
- **Configuration** - Java version, test settings, etc.

### settings.gradle

The `settings.gradle` file contains the project name.

### gradle/wrapper/

Contains the Gradle Wrapper configuration:
- `gradle-wrapper.properties` - Gradle version and download URL
- `gradle-wrapper.jar` - Wrapper executable (auto-downloaded)

## Gradle vs Maven Comparison

| Task | Maven | Gradle |
|------|-------|--------|
| Build | `mvn clean install` | `./gradlew build` |
| Run | `mvn spring-boot:run` | `./gradlew bootRun` |
| Test | `mvn test` | `./gradlew test` |
| Package | `mvn package` | `./gradlew bootJar` |
| Clean | `mvn clean` | `./gradlew clean` |
| Skip tests | `mvn install -DskipTests` | `./gradlew build -x test` |
| Specific test | `mvn test -Dtest=TestClass` | `./gradlew test --tests TestClass` |

## Build Output

Gradle creates a `build/` directory (instead of Maven's `target/`):

```
build/
├── classes/          # Compiled classes
├── libs/             # JAR files
├── reports/          # Test reports, coverage, etc.
├── resources/        # Processed resources
└── tmp/              # Temporary files
```

## Gradle Daemon

Gradle uses a daemon process for faster builds:

```bash
# Check daemon status
./gradlew --status

# Stop daemon
./gradlew --stop

# Run without daemon
./gradlew build --no-daemon
```

## Troubleshooting

### Permission Denied

```bash
# Make gradlew executable
chmod +x gradlew
```

### Gradle Wrapper JAR Missing

If you see "Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain":

```bash
# Re-download wrapper (requires Gradle installed globally)
gradle wrapper --gradle-version 8.5

# Or download manually from:
# https://services.gradle.org/distributions/gradle-8.5-bin.zip
```

### Build Cache Issues

```bash
# Clean Gradle cache
./gradlew clean --refresh-dependencies

# Delete cache directory
rm -rf ~/.gradle/caches/
```

### Port Already in Use

```bash
# Stop any running instances
./gradlew --stop
```

## IDE Integration

### IntelliJ IDEA
- IntelliJ automatically detects `build.gradle`
- Use "Reload Gradle Project" if needed
- Run configurations work out of the box

### VS Code
- Install "Gradle for Java" extension
- Gradle tasks appear in sidebar
- Full debugging support

### Eclipse
- Install Buildship Gradle plugin
- Import as "Existing Gradle Project"

## Continuous Integration

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

## Performance Tips

1. **Use Gradle Daemon** (enabled by default)
2. **Enable parallel builds** - add to `gradle.properties`:
   ```properties
   org.gradle.parallel=true
   org.gradle.caching=true
   ```
3. **Increase heap size** if needed:
   ```bash
   ./gradlew build -Xmx2g
   ```

## Additional Resources

- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/)
- [Gradle vs Maven](https://gradle.org/maven-vs-gradle/)

## Why Gradle?

**Advantages over Maven:**
- ✅ Faster builds (incremental compilation, build cache)
- ✅ More flexible and less verbose
- ✅ Better dependency management
- ✅ Groovy/Kotlin DSL (more powerful than XML)
- ✅ Built-in task optimization
- ✅ Better IDE integration

**When to use Maven:**
- Simpler projects
- Team more familiar with Maven
- Need for strict convention-over-configuration

---

For more information, see the main [README.md](README.md).

