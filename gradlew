#!/usr/bin/env bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# If local portable JDK-17 exists, use it
if [ -d "$DIR/.gradle-bin/jdk-17/Contents/Home" ]; then
    export JAVA_HOME="$DIR/.gradle-bin/jdk-17/Contents/Home"
fi

# If local portable Gradle exists, use it directly
if [ -x "$DIR/.gradle-bin/gradle-8.10.2/bin/gradle" ]; then
    exec "$DIR/.gradle-bin/gradle-8.10.2/bin/gradle" "$@"
fi

# Fallback to standard Gradle wrapper (used on GitHub Actions & Googlebook)
JAVACMD="java"
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
fi

exec "$JAVACMD" "-Dorg.gradle.appname=gradlew" -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
