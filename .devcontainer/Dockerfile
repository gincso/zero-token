FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# Install essential tools
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk-headless \
    curl \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Find Java home dynamically
RUN JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java)))) && \
    echo "JAVA_HOME=$JAVA_HOME" && \
    echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile.d/java.sh

# Android SDK
ENV ANDROID_SDK_ROOT=/usr/lib/android-sdk
ENV ANDROID_HOME=$ANDROID_SDK_ROOT
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

# Install Android cmdline-tools
RUN mkdir -p $ANDROID_SDK_ROOT && \
    curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/tools.zip && \
    unzip -q /tmp/tools.zip -d /tmp/tools && \
    mkdir -p $ANDROID_SDK_ROOT/cmdline-tools && \
    mv /tmp/tools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest && \
    rm -rf /tmp/tools.zip /tmp/tools && \
    yes | $ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager --sdk_root=$ANDROID_SDK_ROOT --licenses > /dev/null 2>&1 && \
    $ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager --sdk_root=$ANDROID_SDK_ROOT \
        "platforms;android-36" \
        "build-tools;36.0.0" \
        "platform-tools" \
    && chmod -R a+r $ANDROID_SDK_ROOT

# Create vscode user (Codespaces convention)
RUN useradd -m -s /bin/bash vscode && \
    echo 'vscode ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

USER vscode
WORKDIR /workspaces/openclaw-zero-token
