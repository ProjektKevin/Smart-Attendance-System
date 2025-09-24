#!/bin/sh
MAVEN_WRAPPER_VERSION="3.2.0"
WRAPPER_JAR=".mvn/wrapper/maven-wrapper-${MAVEN_WRAPPER_VERSION}.jar"
WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/${MAVEN_WRAPPER_VERSION}/maven-wrapper-${MAVEN_WRAPPER_VERSION}.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
  mkdir -p .mvn/wrapper
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "Please install curl or wget."
    exit 1
  fi
fi
exec "$JAVA_HOME/bin/java" -jar "$WRAPPER_JAR" "$@"
