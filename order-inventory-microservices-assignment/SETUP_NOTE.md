# Initial Setup Note

## Gradle Wrapper Initialization

The project includes Gradle wrapper scripts (`gradlew` and `gradlew.bat`), but you may need to initialize the wrapper JAR on first use.

### Quick Setup (Recommended)

The Gradle wrapper will automatically download itself on first use:

```bash
cd inventory-service
./gradlew build  # Will download Gradle on first run

cd ../order-service
./gradlew build  # Will download Gradle on first run
```

### If Wrapper JAR is Missing

If you encounter an error about missing `gradle-wrapper.jar`, you have two options:

#### Option 1: Install Gradle Globally (Temporary)

```bash
# macOS
brew install gradle

# Or download from https://gradle.org/releases/

# Then regenerate wrapper
cd inventory-service
gradle wrapper --gradle-version 8.5

cd ../order-service
gradle wrapper --gradle-version 8.5
```

#### Option 2: Manual Download

Download the gradle-wrapper.jar manually:

```bash
# For Inventory Service
mkdir -p inventory-service/gradle/wrapper
curl -L -o inventory-service/gradle/wrapper/gradle-wrapper.jar \
  https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar

# For Order Service
mkdir -p order-service/gradle/wrapper
curl -L -o order-service/gradle/wrapper/gradle-wrapper.jar \
  https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar

# Make gradlew executable
chmod +x inventory-service/gradlew order-service/gradlew
```

### Verify Setup

After initialization, verify everything works:

```bash
# Check Gradle version
cd inventory-service
./gradlew --version

# Build the project
./gradlew build

# If successful, you should see:
# BUILD SUCCESSFUL in Xs
```

### Common Issues

**Permission Denied:**
```bash
chmod +x gradlew
```

**Gradle Daemon Issues:**
```bash
./gradlew --stop
./gradlew build
```

**Clean Start:**
```bash
./gradlew clean build --refresh-dependencies
```

## Why Gradle?

This project uses **Gradle** for:
- ✅ Faster build times (incremental compilation, build cache)
- ✅ Better dependency management
- ✅ Modern build features
- ✅ Included wrapper (no installation needed)
- ✅ Cleaner, more readable build files

## First Time Build Checklist

- [ ] Java 17+ installed (`java -version`)
- [ ] gradlew scripts are executable (`chmod +x gradlew`)
- [ ] First build will download dependencies (may take 2-3 minutes)
- [ ] Subsequent builds will be faster (incremental)

## Getting Help

- See [GRADLE_INFO.md](GRADLE_INFO.md) for detailed Gradle commands
- See [README.md](README.md) for general project information
- See [QUICKSTART.md](QUICKSTART.md) for a 5-minute guide

---

**Ready to start?** Run:
```bash
cd inventory-service && ./gradlew build && cd ../order-service && ./gradlew build
```

