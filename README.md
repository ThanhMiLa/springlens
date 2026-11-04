<div align="center">

# <img src="./docs/images/logo.svg" width="38" height="38" valign="middle" alt="SpringLens Logo" /> SpringLens
### Spring Boot RESTful API Tester for IntelliJ IDEA

[![Build](https://github.com/ThanhMiLa/SpringLens/actions/workflows/build.yml/badge.svg)](https://github.com/ThanhMiLa/SpringLens/actions)
[![Version](https://img.shields.io/badge/version-1.2.7-blue.svg)](https://github.com/ThanhMiLa/SpringLens/releases)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x%20%7C%203.x-6DB33F.svg)](https://spring.io/projects/spring-boot)


![SpringLens Live Demo](./docs/images/demo.gif)

</div>


## Overview

**SpringLens** is a powerful, lightweight RESTful API testing client integrated directly into IntelliJ IDEA. Designed specifically for Spring Boot and Spring Cloud developers, it allows you to test, debug, and manage your APIs instantly without leaving your editor.

SpringLens automatically scans your project to organize all endpoints into an intuitive tree, generates mock JSON request bodies from your DTOs, effortlessly handles Spring Cloud Gateway routing, and provides one-click authentication sharing — delivering a complete, Postman-grade testing experience right where you code.


## Key Features

| Feature | Description |
|---|---|
| **Discover Spring APIs from Source** | Find controller endpoints across project modules without importing an API specification. Browse APIs by service and controller, filter by source file, and exclude test controllers automatically. |
| **Generate & Sync Request JSON** | Generate sample JSON from Java DTOs, then use **Sync Schema** to merge DTO changes into the request body while preserving values you have entered. |
| **Resolve Spring Service URLs** | Detect module-specific ports, context paths, and profiles from Spring configuration and IntelliJ run configurations, reducing manual URL setup for multi-service projects. |
| **Test Directly or Through a Gateway** | Switch between direct service calls and Spring Cloud Gateway URLs for routable endpoints. Resolve supported `StripPrefix`, `PrefixPath`, and `RewritePath` rules while keeping internal endpoints direct-only. |
| **Reuse Authentication Across APIs** | Configure Bearer tokens, Basic Auth, or API keys and apply the configuration to all project APIs with **Apply to All APIs**. |
| **Send JSON, Forms & Files** | Edit path and query parameters, headers, and cookies; send JSON bodies or multipart form data with file uploads directly from IntelliJ. |
| **Organize & Resume API Testing** | Add manual requests and collections alongside discovered APIs. Restore request bodies, response history, and each API's last request tab across IDE restarts. |
| **Export Requests for Your Terminal** | Copy requests as cURL for Bash or Windows CMD, or as PowerShell commands, to reproduce API calls outside the IDE. |

## Screenshots

### 1. IDE Integration & Workspace Overview
![IDE Integration & Workspace Overview](./docs/images/fullscreen.png)

### 2. Request Body & Response Viewer
![Request Body & Response Viewer](./docs/images/request_body.png)

### 3. Query & Path Parameters
![Query & Path Parameters](./docs/images/request_param.png)

### 4. Endpoint Explorer & Security Status
![Endpoint Explorer & Security Status](./docs/images/endpoints.png)


## Installation

### Option A: JetBrains Marketplace (Recommended)
1. In IntelliJ IDEA, go to **Settings/Preferences** (`Cmd + ,` or `Ctrl + Alt + S`) > **Plugins**.
2. Select the **Marketplace** tab and search for **`SpringLens`**.
3. Click **Install** and restart the IDE.

### Option B: Install from Disk (.zip)
1. Download `SpringLens-1.2.7.zip` from [GitHub Releases](https://github.com/ThanhMiLa/SpringLens/releases).
2. Go to **Settings** > **Plugins** > click the Settings icon > **Install Plugin from Disk...**.
3. Select the downloaded `.zip` file and restart IntelliJ IDEA.

### Option C: Build from Source
```bash
git clone https://github.com/ThanhMiLa/SpringLens.git
cd SpringLens
./gradlew buildPlugin
```
The packaged plugin will be available at `build/distributions/SpringLens-1.2.7.zip`.

## License

SpringLens is under the Apache 2.0 license. See the [Apache License 2.0](./LICENSE) file for details.


*Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)*
