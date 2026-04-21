# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot starter library `grpc-nacos-discovery-spring-boot-starter` that enables gRPC service discovery using Nacos as the service registry. It mimics the implementation of Spring Cloud Alibaba Nacos Discovery to provide immediate gRPC service discovery capabilities.

## Build System

- **Build Tool**: Maven
- **Java Version**: 17
- **Spring Boot Version**: 2.7.18
- **Spring Cloud Version**: 2021.0.5.0
- **Dependencies**:
  - `spring-cloud-starter-alibaba-nacos-discovery`
  - `spring-boot-autoconfigure`

## Common Development Tasks

### Building the Project
```bash
mvn clean install
```

### Running Tests
```bash
mvn test
```

### Building with Release Profile (for publishing)
```bash
mvn clean install -Prelease
```

## Architecture

The project follows a Spring Boot auto-configuration pattern with the following key components:

### Core Classes
- `EnhancedNacosWatch`: Enhanced Nacos watcher that subscribes to service changes and manages lifecycle
- `EnhancedEventListener`: Event listener that handles Nacos naming events and triggers service refresh
- `EnhancedNacosDiscoveryClientConfiguration`: Main configuration class that enables auto-configuration
- `ConditionalOnGrpcNacosDiscoveryEnabled`: Conditional annotation for enabling the discovery functionality

### Auto-Configuration
The library uses Spring Boot's auto-configuration mechanism through `META-INF/spring.factories` to automatically configure the discovery client when the starter is on the classpath.

### Event Flow
1. Nacos server pushes `NamingEvent` when services change
2. `EnhancedEventListener` receives the event
3. `GatewayLocatorHeartBeatPublisher` converts it to a Spring Cloud `HeartbeatEvent`
4. `NameResolverProvider` listens to the event and refreshes gRPC server list

## Configuration

Key configuration properties:
- `io.github.grpc.nacos.discovery.immediate.enabled`: Enable gRPC Nacos immediate discovery (default: true)
- `spring.cloud.nacos.discovery.enhanced.watch.enabled`: Enable enhanced Nacos watcher (default: true)
- `spring.cloud.gateway.discovery.locator.enabled`: Enable gateway locator (default: true)

## Dependencies

The project depends on:
- `spring-cloud-starter-alibaba-nacos-discovery` for Nacos integration
- `spring-boot-autoconfigure` for auto-configuration support

## Publishing

For publishing to Maven Central:
1. Ensure you have the necessary GPG keys configured
2. Use the release profile: `mvn clean install -Prelease`
3. The project is configured to publish to both GitHub Packages and Maven Central

## Code Structure

- `src/main/java/io/github/grpc/nacos/discovery/`: Contains all Java source files
- `src/main/resources/META-INF/`: Contains Spring Boot auto-configuration files
- `pom.xml`: Maven build configuration
- `README.md`: Project documentation and usage instructions
- `.github/workflows/`: CI/CD configuration files

## Important Notes

- The project requires Java 17 runtime environment
- It's designed as a Spring Boot starter library, not a standalone application
- Service discovery works by converting Nacos service changes into Spring Cloud events that gRPC clients can consume
- The library enhances the standard Nacos discovery to work specifically with gRPC services