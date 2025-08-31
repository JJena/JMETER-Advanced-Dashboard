# JMeter Live Dashboard Listener Setup Guide

This guide will help you build and install the JMeter Live Dashboard Listener using Maven.

## Prerequisites

Before you begin, ensure you have:

1. **Java Development Kit (JDK) 8 or higher**
   - Check with: `java -version` and `javac -version`
   - Download from: https://adoptium.net/ or https://www.oracle.com/java/

2. **Apache Maven 3.6 or higher**
   - Check with: `mvn -version`
   - Download from: https://maven.apache.org/download.cgi

3. **Apache JMeter**
   - Download from: https://jmeter.apache.org/download_jmeter.cgi
   - Extract to a directory (e.g., `/opt/jmeter` or `C:\jmeter`)

## Step 1: Build the Plugin

1. **Navigate to the project directory** where `pom.xml` is located.

2. **Build the project:**
   ```bash
   mvn clean package
   ```

   This will:
   - Download all required dependencies
   - Compile the Java code
   - Create the JAR file in `target/live-dashboard-listener-1.0.0.jar`

## Step 2: Install the Plugin

1. **Copy the JAR file** to JMeter's lib/ext directory:
   ```bash
   cp target/live-dashboard-listener-1.0.0.jar $JMETER_HOME/lib/ext/
   ```

2. **Copy the HTML dashboard** to JMeter's bin directory:
   ```bash
   cp jmeter-dashboard.html $JMETER_HOME/bin/
   ```

3. **Restart JMeter** to load the new plugin.

## Step 3: Use the Live Dashboard

1. **Start JMeter GUI:**
   ```bash
   $JMETER_HOME/bin/jmeter
   ```

2. **Create or open a test plan.**

3. **Add the Advanced JMeter Dashboard:**
   - Right-click on your test plan or thread group
   - Go to **Add → Listeners → Advanced JMeter Dashboard**

4. **Configure your test** as usual with samplers, thread groups, etc.

5. **Run your test:**
   - The listener will create a file called `live-dashboard.jtl` in the JMeter bin directory
   - Click the **"🌐 Open Live Dashboard"** button in the listener panel
   - This opens the HTML dashboard in your default browser

6. **View live data:**
   - In the dashboard, click **"📡 Load Live Data"**
   - The dashboard will refresh every 5 seconds with live test data

## Maven Build Options

### Development Build
```bash
mvn clean package
```

### Production Build
```bash
mvn clean package -Pprod
```

### Build without ZIP (Recommended)
```bash
mvn clean package
```
This creates only the JAR file for cleaner, faster builds. ZIP distribution has been removed.

## Troubleshooting

### Build Issues

**Error: "Maven not found"**
- Install Maven and add it to your PATH
- Verify with `mvn -version`

**Error: "JAVA_HOME not set"**
- Set JAVA_HOME environment variable to your JDK installation
- Ensure it points to JDK, not JRE

### Plugin Not Appearing in JMeter

**Check these common issues:**
1. JAR file is in the correct location (`$JMETER_HOME/lib/ext/`)
2. JMeter was restarted after copying the JAR
3. Build completed successfully without errors
4. Correct permissions on the JAR file
5. Look for "Advanced JMeter Dashboard" (not "Live Dashboard Listener")

### Live Dashboard Issues

**Dashboard not loading:**
- Ensure `jmeter-dashboard.html` is in JMeter's bin directory
- Check that your browser allows local file access
- Verify the HTML file hasn't been corrupted

**No live data appearing:**
- Confirm the listener is added to your test plan
- Check that test is actually running
- Verify `live-dashboard.jtl` file is being created in bin directory
- **For live data functionality**: Start HTTP server in JMeter bin directory:
  ```bash
  cd $JMETER_HOME/bin
  python3 -m http.server 8080
  ```
- Open dashboard via `http://localhost:8080/jmeter-dashboard.html`
- Browser security restrictions prevent `file://` URLs from fetching live data

### File Permissions (macOS/Linux)

If you encounter permission issues:
```bash
chmod 644 $JMETER_HOME/lib/ext/live-dashboard-listener-1.0.0.jar
chmod 644 $JMETER_HOME/bin/jmeter-dashboard.html
```

## Advanced Configuration

### Custom JTL File Location

You can modify the listener code to write to a different location by changing this line in the Java source:

```java
private static final String JTL_FILE_PATH = System.getProperty("jmeter.home") + "/bin/live-dashboard.jtl";
```

### Dashboard Refresh Rate

To change the 5-second refresh rate, modify this line in `jmeter-dashboard.html`:

```javascript
liveDataInterval = setInterval(loadLiveJTLFile, 5000); // Change 5000 to desired milliseconds
```

## File Structure After Installation

Your JMeter installation should look like this:

```
jmeter/
├── bin/
│   ├── jmeter (or jmeter.bat on Windows)
│   ├── jmeter-dashboard.html          # ← Dashboard HTML file
│   └── live-dashboard.jtl             # ← Created during test runs
├── lib/
│   └── ext/
│       └── live-dashboard-listener-1.0.0.jar  # ← Plugin JAR file
└── ... (other JMeter files)
```

## Verification

To verify everything is working:

1. Start JMeter GUI
2. Look for "Advanced JMeter Dashboard" in Add → Listeners menu
3. Add it to a test plan
4. Run a simple test
5. Check if `live-dashboard.jtl` appears in bin directory
6. Click "🌐 Open Live Dashboard" button
7. Use "📡 Load Live Data" in the dashboard (via HTTP server for live functionality)

## Live Data Setup (Important!)

For live data functionality, the plugin tries to:
1. **First**: Open dashboard via `http://localhost:8080/jmeter-dashboard.html` (if HTTP server is running)
2. **Fallback**: Open via `file://` with instructions to set up HTTP server

**Recommended workflow:**
1. Open terminal in JMeter bin directory
2. Start HTTP server: `python3 -m http.server 8080`
3. Run your JMeter test with the listener
4. Click "Open Live Dashboard" - it will automatically use HTTP protocol
5. Dashboard will have full live data functionality

If all steps work, your installation is successful!