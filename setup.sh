#!/usr/bin/env bash

# Device Lens - Development Environment Setup Script
# Supports: Linux (Ubuntu/Debian/Pop!_OS) and macOS

# Text formatting
BOLD="\033[1m"
GREEN="\033[32m"
BLUE="\033[34m"
RED="\033[31m"
YELLOW="\033[33m"
RESET="\033[0m"

echo -e "${BOLD}${BLUE}╔════════════════════════════════════════════════╗${RESET}"
echo -e "${BOLD}${BLUE}║        DEVICE LENS - ENVIRONMENT SETUP         ║${RESET}"
echo -e "${BOLD}${BLUE}╚════════════════════════════════════════════════╝${RESET}"
echo ""

# Detect OS
OS_NAME=$(uname -s)
echo -e "Detected Operating System: ${BOLD}${OS_NAME}${RESET}"

# Variables
ANDROID_API_LEVEL="34"
BUILD_TOOLS_VERSION="34.0.0"

# -----------------------------------------------------------------------------
# 1. Install System Dependencies (Java 17)
# -----------------------------------------------------------------------------
echo -e "\n${BOLD}${YELLOW}[1/5] Checking System Dependencies...${RESET}"

if [ "$OS_NAME" = "Linux" ]; then
    if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q '17\.\|17-'; then
        echo -e "Installing OpenJDK 17..."
        if command -v apt-get >/dev/null; then
            sudo apt-get update
            sudo apt-get install -y openjdk-17-jdk unzip wget curl
        else
            echo -e "${RED}Unsupported Linux package manager. Please install OpenJDK 17 manually.${RESET}"
            exit 1
        fi
    else
        echo -e "${GREEN}✓ Java 17 is already installed.${RESET}"
    fi
elif [ "$OS_NAME" = "Darwin" ]; then
    if ! command -v brew >/dev/null; then
        echo -e "${RED}Homebrew is required on macOS. Please install it first: https://brew.sh/${RESET}"
        exit 1
    fi
    
    if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q '17\.\|17-'; then
        echo -e "Installing OpenJDK 17 via Homebrew..."
        brew install openjdk@17
        sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    else
        echo -e "${GREEN}✓ Java 17 is already installed.${RESET}"
    fi
else
    echo -e "${RED}Unsupported OS: $OS_NAME. Please install dependencies manually.${RESET}"
    exit 1
fi

# -----------------------------------------------------------------------------
# 2. Android SDK Setup
# -----------------------------------------------------------------------------
echo -e "\n${BOLD}${YELLOW}[2/5] Checking Android SDK...${RESET}"

if [ -z "$ANDROID_HOME" ]; then
    if [ "$OS_NAME" = "Linux" ]; then
        export ANDROID_HOME="$HOME/android-sdk"
    elif [ "$OS_NAME" = "Darwin" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    fi
    echo -e "ANDROID_HOME not set. Using default: ${BOLD}$ANDROID_HOME${RESET}"
else
    echo -e "Using existing ANDROID_HOME: ${BOLD}$ANDROID_HOME${RESET}"
fi

CMDLINE_TOOLS_DIR="$ANDROID_HOME/cmdline-tools/latest"
SDKMANAGER="$CMDLINE_TOOLS_DIR/bin/sdkmanager"

# Install SDK if missing
if [ ! -f "$SDKMANAGER" ]; then
    echo -e "Downloading Android Command Line Tools..."
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    cd "$ANDROID_HOME/cmdline-tools"
    
    # URL for late 2023/2024 cmdline tools (check developer.android.com/studio for latest if this breaks)
    if [ "$OS_NAME" = "Linux" ]; then
        wget -qO cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
    elif [ "$OS_NAME" = "Darwin" ]; then
        curl -L -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip
    fi
    
    unzip -q cmdline-tools.zip
    mv cmdline-tools latest
    rm cmdline-tools.zip
    echo -e "${GREEN}✓ Command Line Tools installed.${RESET}"
else
    echo -e "${GREEN}✓ Command Line Tools already installed.${RESET}"
fi

# Install SDK Components
echo -e "Accepting SDK licenses..."
yes | "$SDKMANAGER" --licenses > /dev/null 2>&1

echo -e "Installing SDK Platforms, Build Tools, and Platform Tools..."
"$SDKMANAGER" "platforms;android-${ANDROID_API_LEVEL}" "build-tools;${BUILD_TOOLS_VERSION}" "platform-tools" > /dev/null

echo -e "${GREEN}✓ Android SDK components installed.${RESET}"

# Prompt to add to PATH
BASHRC_FILE="$HOME/.bashrc"
ZSHRC_FILE="$HOME/.zshrc"

add_to_path() {
    local shell_rc="$1"
    if [ -f "$shell_rc" ]; then
        if ! grep -q "export ANDROID_HOME=" "$shell_rc"; then
            echo -e "\n# Android SDK" >> "$shell_rc"
            echo "export ANDROID_HOME=$ANDROID_HOME" >> "$shell_rc"
            echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> "$shell_rc"
            echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> "$shell_rc"
            echo 'export PATH=$PATH:$ANDROID_HOME/emulator' >> "$shell_rc"
            echo -e "Added ANDROID_HOME to ${BOLD}$shell_rc${RESET}"
        fi
    fi
}

add_to_path "$BASHRC_FILE"
add_to_path "$ZSHRC_FILE"

# Make adb accessible for the rest of this script
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator

# -----------------------------------------------------------------------------
# 3. VS Code Extensions (Optional but recommended)
# -----------------------------------------------------------------------------
echo -e "\n${BOLD}${YELLOW}[3/5] IDE Setup...${RESET}"

if command -v code >/dev/null 2>&1; then
    echo -e "VS Code detected. Installing recommended extensions..."
    code --install-extension fwcd.kotlin --force >/dev/null 2>&1
    code --install-extension vscjava.vscode-gradle --force >/dev/null 2>&1
    code --install-extension nicepay.vscode-android-emulator --force >/dev/null 2>&1
    code --install-extension redhat.vscode-xml --force >/dev/null 2>&1
    echo -e "${GREEN}✓ VS Code extensions installed.${RESET}"
else
    echo -e "VS Code not found in PATH. Skipping extension installation."
fi

# -----------------------------------------------------------------------------
# 4. iOS Setup (macOS only)
# -----------------------------------------------------------------------------
echo -e "\n${BOLD}${YELLOW}[4/5] iOS App Setup...${RESET}"

if [ "$OS_NAME" = "Darwin" ]; then
    if ! xcode-select -p >/dev/null 2>&1; then
        echo -e "${RED}Xcode Command Line Tools not found. Installing...${RESET}"
        xcode-select --install
        echo -e "Please complete the Xcode installation prompt and run this script again."
        exit 1
    else
        echo -e "${GREEN}✓ Xcode Command Line Tools are installed.${RESET}"
    fi
    
    # Check if Xcode app is installed (needed for simulators and building)
    if [ ! -d "/Applications/Xcode.app" ]; then
        echo -e "${YELLOW}Warning: Full Xcode.app not found. You will need it to compile the iOS app.${RESET}"
        echo -e "${YELLOW}Install from the Mac App Store.${RESET}"
    else
        echo -e "${GREEN}✓ Full Xcode app detected.${RESET}"
    fi
else
    echo -e "Linux detected. Skipping iOS setup."
fi

# -----------------------------------------------------------------------------
# 5. Build Initial Android Project (Debug APK)
# -----------------------------------------------------------------------------
echo -e "\n${BOLD}${YELLOW}[5/5] Building Android Project...${RESET}"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ANDROID_DIR="$SCRIPT_DIR/android"

if [ -d "$ANDROID_DIR" ]; then
    cd "$ANDROID_DIR"
    
    # Generate Gradle wrapper if missing
    if [ ! -f "gradlew" ]; then
        echo -e "Downloading Gradle 8.6 to generate wrapper..."
        
        # Download gradle binary to temp
        GRADLE_ZIP="/tmp/gradle-8.6-bin.zip"
        GRADLE_DIR="/tmp/gradle-8.6"
        
        if [ ! -d "$GRADLE_DIR" ]; then
            wget -q https://services.gradle.org/distributions/gradle-8.6-bin.zip -O "$GRADLE_ZIP"
            unzip -q "$GRADLE_ZIP" -d /tmp/
        fi
        
        # Use downloaded gradle to generate wrapper
        "$GRADLE_DIR/bin/gradle" wrapper --gradle-version 8.6
        
        # Clean up zip but keep the dir so the gradle daemon can use it
        rm -rf "$GRADLE_ZIP"
    fi
    
    chmod +x gradlew
    
    echo -e "Building App Debug APK (this may take a few minutes)..."
    if ./gradlew assembleDebug; then
        echo -e "${GREEN}✓ Initial Android build successful!${RESET}"
        APK_PATH="$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"
        
        if [ -f "$APK_PATH" ]; then
            echo -e "\n${BOLD}${BLUE}╔════════════════════════════════════════════════╗${RESET}"
            echo -e "${BOLD}${BLUE}║                  SETUP COMPLETE                ║${RESET}"
            echo -e "${BOLD}${BLUE}╚════════════════════════════════════════════════╝${RESET}"
            
            echo -e "\n${BOLD}Android APK Location:${RESET}"
            echo -e "$APK_PATH"
            
            echo -e "\n${BOLD}Next Steps (Android Device Tests):${RESET}"
            echo -e "1. Please restart your terminal to apply PATH changes."
            echo -e "2. Connect your Android phone via USB (with USB Debugging enabled)"
            echo -e "3. Run: ${BOLD}cd android && ./gradlew installDebug${RESET}"
            echo -e "4. Check the logs with: ${BOLD}adb logcat -s DeviceLens:* *:E${RESET}"
            echo -e "   or simply use the generated APK directly: ${BOLD}adb install -r $APK_PATH${RESET}"
            
            # Print explicit APK location path at the end
            echo -e "\n${BOLD}${GREEN}✅ Successfully built APK. You can find it here:${RESET}"
            echo -e "${BOLD}${BLUE}$APK_PATH${RESET}"
            
            if [ "$OS_NAME" = "Darwin" ]; then
                echo -e "\n${BOLD}Next Steps (iOS Device Tests):${RESET}"
                echo -e "1. Open ${BOLD}ios/DeviceLens.xcodeproj${RESET} in Xcode"
                echo -e "2. Select your connected iPhone from the scheme menu"
                echo -e "3. Configure code signing in the project settings"
                echo -e "4. Hit CMD+R to Run"
            fi
            
            echo -e "\n${BOLD}For a comprehensive list of capabilities, see README.md${RESET}\n"
        fi
    else
        echo -e "${RED}✗ Android build failed. See output above.${RESET}"
        exit 1
    fi
else
    echo -e "${RED}✗ Could not find 'android' directory in $SCRIPT_DIR${RESET}"
    exit 1
fi
