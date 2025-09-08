# Advanced JMeter Dashboard with Quality Gates & System Monitoring

A JMeter plugin providing real-time dashboard visualization with **Quality Gate Management** and **System Resource Monitoring**. Features an embedded HTTP server for live monitoring and post-test analysis.

## 🎉 What's New

- ✅ **Quality Gate Management**: Configure and monitor test quality thresholds
- ✅ **Enhanced Charts**: System metrics with quality gate thresholds
- ✅ **System Resource Monitoring**: Real-time CPU and memory tracking
- ✅ **Embedded HTTP Server**: Pure Java implementation (no Python)
- ✅ **Weighted Scoring**: Importance-based quality assessment
- ✅ **Cross-platform Support**: Works on Windows, macOS, Linux


## 🚀 Key Features

- **Real-time Dashboard**: Live visualization with auto-refresh every 5 seconds
- **Quality Gate Management**: Configurable thresholds with weighted scoring
- **System Resource Monitoring**: CPU/memory tracking using OSHI library
- **Embedded HTTP Server**: Pure Java implementation (no Python required)

## 🛠️ Quick Start

### Prerequisites
- Java 17 configured as JAVA_HOME
- JMeter 5.6.2+
- Maven 3.6+ (for building)

### Installation
```bash
# Build the plugin
mvn clean package

# Install to JMeter
cp target/live-dashboard-listener-1.0.0.jar /path/to/jmeter/lib/ext/
cp jmeter-dashboard.html /path/to/jmeter/bin/
```

### Usage
1. Add **Advanced JMeter Dashboard** to your test plan
2. Configure quality gates (optional)
3. Run your test
4. Click **"🌐 Open Live Dashboard"** → **"📡 Load Live Data"**

## 🎯 Quality Gate System

### Available Metrics
- **Response Time**: Average, Max, Min, 90th, 95th, 99th percentiles
- **Throughput**: Requests per second
- **Error Rate**: Percentage of failed requests
- **CPU/Memory Usage**: System resource utilization

### Configuration
- **Operators**: <, >, <=, >=
- **Pass/Warning Values**: Set thresholds for each metric
- **Weightage**: Importance weight (0.0 to 1.0)

### Scoring Formula
```
Overall Score = Σ(Base Score × Weightage) / Σ(Weightage)
```
- **PASS**: 100 points (Overall Score ≥ 80)
- **WARNING**: 50 points (Overall Score ≥ 60)
- **FAIL**: 0 points (Overall Score < 60)

## 📊 System Resource Monitoring

- **Metrics**: CPU usage, memory usage, used/available memory
- **Collection**: Every 5 seconds during test execution
- **Technology**: OSHI (cross-platform, no dependencies)
- **Visualization**: Real-time charts with quality gate thresholds

## 🌐 Dashboard Features

### Charts
- Response time over time and by sampler
- Throughput trends and active threads
- HTTP status codes and response time percentiles
- CPU/memory usage with quality gate thresholds

### Data Tables
- Detailed statistics by request
- Quality gate results and overall scores
- System metrics summary

### Advanced Features
- Time period filtering (5s to 15min intervals)
- Chart maximization and image export
- Live data mode with auto-refresh

## 🔧 Architecture

1. **Embedded HTTP Server**: Java-based server (port 9090)
2. **Live Data Generation**: Real-time JTL file writing
3. **System Metrics Collection**: OSHI-based monitoring
4. **Quality Gate Evaluation**: Real-time threshold checking
5. **Dashboard Integration**: HTML dashboard with live updates

## 🚨 Troubleshooting

### Common Issues
- **Dashboard not loading**: Check HTTP server is running, verify port 9090
- **Quality gates not working**: Verify configuration and check browser console
- **System metrics missing**: Check OSHI library and file permissions
- **Plugin not visible**: Confirm JAR in `lib/ext/`, restart JMeter

### File Locations
- JAR: `lib/ext/live-dashboard-listener-1.0.0.jar`
- HTML: `bin/jmeter-dashboard.html`
- Generated files: `bin/live-dashboard.jtl`, `bin/.jmeter-system-metrics.csv`

## 🔄 Migration Notes

- **HTTP Server**: Now Java-based (no Python)
- **Port Change**: Default port 9090 (was 8080)
- **New Features**: Quality gates and system metrics

## 📋 Requirements

- Java 17+ (recommended)
- JMeter 5.6.2+
- Modern browser (Chrome, Firefox, Safari, Edge)
- Maven 3.6+ (for building)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes and test
4. Submit a pull request

## 📄 License

Open source - use and modify as needed.