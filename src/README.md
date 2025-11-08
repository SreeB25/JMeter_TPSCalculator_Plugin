# JMeter TPS & ThinkTime Calculator — README

## Overview

**TPS & ThinkTime Calculator** is a small JMeter Config Element plugin that calculates the required number of virtual users (threads) and/or think time based on target TPS (transactions per second), expected response time, and transactions-per-iteration. The plugin publishes results as both JMeter variables and JVM properties so they can be used in Thread Group fields and Timers.

This repository contains:
- `src/main/java/.../TPSCalculatorConfig.java` — calculation and JMeter lifecycle logic
- `src/main/java/.../TPSCalculatorGui.java` — GUI implementation (extends `AbstractConfigGui`)
- `pom.xml` — Maven build file
- `src/main/resources/META-INF/services/org.apache.jmeter.gui.ConfigElement` — service file to expose the GUI

---

## Quick features
- Compute **Users (N)** from `Target TPS`, `Expected Response (S)`, `Think time (Z)`, and `transactionsPerIteration (k)`:

  ```text
  N = T * (S + Z) / k
  ```

- Or compute **Think time (Z)** when `Fixed users` is provided:

  ```text
  Z = ( (N * k) / T ) - S
  ```

- Publishes outputs as:
  - System properties: `calculated.users`, `calculated.think` (ms)
  - JMeter variables with same names (available during test runtime)

---

## Build (local)

Requirements:
- Java JDK (8+)
- Maven

Commands:

```bash
# from repo root
mvn -DskipTests clean package
# artifact created at target/jmeter-tps-calculator-1.0.0.jar
```

Install into JMeter:

```bash
cp target/jmeter-tps-calculator-1.0.0.jar /path/to/apache-jmeter-<version>/lib/ext/
# Restart JMeter
```

---

## Usage in JMeter
1. Add the config element: **Add → Config Element → TPS Calculator**
2. Configure `Target TPS`, `Expected response (ms)`, and optionally `Desired think time (ms)` or `Fixed users (threads)` and `Transactions per iteration`.
3. In **Thread Group → Number of Threads** use:
   ```text
   ${__P(calculated.users,1)}
   ```
4. For timer (ms) use:
   ```text
   ${__P(calculated.think,1000)}
   ```
5. Optional: Add a **Debug Sampler** or a **JSR223 Sampler** to log `System.getProperty("calculated.users")` and `System.getProperty("calculated.think")`.

---

## Recommended project files

**.gitignore** (example):

```
# Maven
/target/
/.idea/
*.iml
*.class
.settings/

# OS
.DS_Store
Thumbs.db

# Eclipse
.project
.classpath

# VS Code
.vscode/
```

**LICENSE**
- Consider `MIT` for an open-source, permissive license. Create a `LICENSE` file with the license text.

---

## Publish code to Git (GitHub) — step-by-step

Below are step-by-step commands and guidance to publish your local project to GitHub (or any Git remote). Replace placeholders with your values.

### 1) Create a new repository on GitHub
- Go to https://github.com → New repository → name it `jmeter-tps-calculator` → create as **Public** or **Private** as needed.

Alternatively, use the GitHub CLI (`gh`) if installed:

```bash
gh repo create my-org/jmeter-tps-calculator --public --source=. --remote=origin
```

### 2) Initialize local Git, add files, commit

```bash
cd /path/to/jmeter-tps-calculator
git init
git add .
git commit -m "Initial commit: JMeter TPS & ThinkTime Calculator plugin"
```

### 3) Add remote & push

```bash
# if you created repo on GitHub via web UI, run (use your repo URL):
git remote add origin https://github.com/<your-username>/jmeter-tps-calculator.git
git branch -M main
git push -u origin main
```

If you used the `gh` command in step 1 the `--remote` option already added the remote and pushed.

---

## Create a release (publish the JAR)

You can publish the built JAR as a GitHub Release so consumers can download the plugin.

### Using the web UI:
1. Go to your GitHub repo → Releases → Draft a new release.  
2. Choose a tag (e.g. `v1.0.0`), set the title and notes, and attach the `target/jmeter-tps-calculator-1.0.0.jar` file.  
3. Publish the release.

### Using GitHub CLI:

```bash
# create tag + push
git tag v1.0.0
git push origin v1.0.0

# create release and attach artifact
gh release create v1.0.0 target/jmeter-tps-calculator-1.0.0.jar -t "v1.0.0" -n "Release: JMeter TPS Calculator"
```

---

## Continuous Integration (optional) — GitHub Actions

Add `.github/workflows/maven.yml` to build and publish artifacts on push or on release.

Example workflow:

```yaml
name: Java CI
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  release:
    types: [created]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 1.8
        uses: actions/setup-java@v4
        with:
          java-version: '1.8'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn -DskipTests clean package
      - name: Upload built jar
        if: success()
        uses: actions/upload-artifact@v4
        with:
          name: jmeter-tps-calculator-jar
          path: target/jmeter-tps-calculator-*.jar

  release:
    needs: build
    runs-on: ubuntu-latest
    if: startsWith(github.ref, 'refs/tags/')
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '1.8'
      - name: Build
        run: mvn -DskipTests clean package
      - name: Create GitHub Release (attach jar)
        uses: softprops/action-gh-release@v1
        with:
          files: target/jmeter-tps-calculator-*.jar
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## Publishing to other plugin channels

- **GitHub Releases** is the simplest and recommended place to host the jar for download.  
- If you want to publish to the official JMeter Plugins site (JMeter Plugins Manager), follow their contribution/publish guidelines at https://jmeter-plugins.org/ (you may need to prepare extra metadata and follow their submission process).

---

## Suggested next small tasks (I can do these for you)
- Add the `src/main/resources/META-INF/services/org.apache.jmeter.gui.ConfigElement` file to guarantee discovery (I can add it now).  
- Add `messages.properties` for localized plugin name/description.  
- Add CI workflow to the canvas for automatic builds and releasing.  
- Prepare a `LICENSE` (MIT) and `README.md` in the repo root (this file is ready to paste as README).

---

If you want, I will:
1. Add `.gitignore`, `LICENSE` (MIT), and the `META-INF/services` file into the canvas project now.  
2. Provide the exact `git` commands customized with your GitHub username and repo name so you can copy-paste to publish.  

Which of those should I do next? (I can update the canvas files immediately.)
