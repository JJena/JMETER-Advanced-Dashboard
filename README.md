# JMeter Live Dashboard Listener

A JMeter plugin that provides real-time dashboard visualization for your performance tests. This plugin generates a live JTL file that can be consumed by an HTML dashboard for real-time monitoring.

## Features

- 📊 **Real-time Dashboard**: Live visualization of test metrics while JMeter is running
- 📈 **Comprehensive Charts**: Response times, throughput, active threads, HTTP codes, and more
- 📋 **Detailed Statistics**: Per-sampler statistics with error tracking
- 🎯 **Easy Integration**: Simple JMeter listener that works with any test plan
- 🌐 **Web-based UI**: Modern HTML dashboard with interactive charts
- 📱 **Responsive Design**: Works on desktop and mobile devices
- 🔄 **Auto-refresh**: Dashboard updates automatically during test execution
- 📸 **Export Options**: Export dashboard as PNG image

## Quick Start

### 1. Build the Plugin

```bash
mvn clean package
```

### 2. Install to JMeter

1. Copy the generated JAR file from `target/live-dashboard-listener-1.0.0.jar` to your JMeter `lib/ext/` directory
2. Copy `jmeter-dashboard.html` to your JMeter `bin/` directory
3. Restart JMeter

### 3. Use in Your Test Plan

1. Add the **Advanced JMeter Dashboard** to your test plan (Add → Listeners → Advanced JMeter Dashboard)
2. Run your test
3. Click the **"🌐 Open Live Dashboard"** button in the listener
4. In the opened dashboard, click **"📡 Load Live Data"** to start real-time monitoring

## Dashboard Features

### Metrics Overview
- Test timeline (start/end/duration)
- Total samples and error rate
- Average response time and throughput

### Interactive Charts
- **Response Time Over Time**: Average response times across test duration
- **Avg Response Time by Sampler**: Time series showing each sampler's performance
- **Throughput Over Time**: Requests per second trends
- **Active Threads**: Thread count progression
- **HTTP Codes Over Time**: Response code distribution over time
- **Response Time Percentiles**: 50th, 90th, 95th, and 99th percentiles
- **Response Codes Distribution**: Overall response code breakdown

### Data Tables
- **Detailed Statistics by Request**: Per-sampler metrics with percentiles
- **Detailed Errors**: Error breakdown with counts and percentages

### Advanced Features
- **Time Period Filter**: Adjust chart granularity (5s to 15min intervals, defaults to 5s)
- **Chart Maximization**: Full-screen view for any chart
- **Live Data Mode**: Auto-refresh every 5 seconds during test execution
- **Image Export**: Export entire dashboard as PNG
- **Smooth Chart Lines**: All time-series charts feature smooth curves
- **Real-time Statistics**: Complete percentile calculations in live mode

## How It Works

1. **Live Data Generation**: The JMeter listener writes test results to `live-dashboard.jtl` in real-time
2. **Dashboard Integration**: The HTML dashboard fetches this file periodically when in live mode
3. **Real-time Visualization**: Charts and tables update automatically as new data arrives

## Project Structure

```
├── src/main/java/
│   └── org/apache/jmeter/visualizers/
│       └── LiveDashboardListener.java     # JMeter listener plugin
├── jmeter-dashboard.html                  # HTML dashboard
├── sample-webapp-test.jtl                 # Sample test data
├── sample-webapp-30min.jtl               # Larger sample dataset
└── pom.xml                               # Maven configuration
```

## Maven Build Profiles

- **Development** (default): `mvn clean package`
- **Production**: `mvn clean package -Pprod`

Note: Distribution ZIP generation has been removed for cleaner builds.

## Requirements

- Java 8 or higher
- Apache JMeter 5.6.2 or compatible
- Maven 3.6+ (for building)
- Modern web browser (for dashboard)

## Browser Compatibility

The dashboard works with modern browsers that support:
- ES6 JavaScript features
- Canvas API (for chart rendering)
- Fetch API (for live data loading)

Tested on: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+

## Troubleshooting

### Dashboard Not Loading Live Data
- Ensure the JMeter listener is added to your test plan
- Check that `live-dashboard.jtl` exists in the JMeter `bin/` directory
- For live data functionality, use HTTP server: `python3 -m http.server 8080` in JMeter bin directory
- Open dashboard via `http://localhost:8080/jmeter-dashboard.html` for live data
- Verify the dashboard HTML file can access the JTL file (same directory)

### Charts Not Displaying
- Check browser console for JavaScript errors
- Ensure Chart.js library loads correctly
- Verify JTL file format is valid

### Plugin Not Visible in JMeter
- Confirm JAR is in `lib/ext/` directory
- Restart JMeter completely
- Check JMeter logs for plugin loading errors

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with different JMeter versions
5. Submit a pull request

## License

This project is open source. Feel free to use and modify according to your needs.

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review JMeter logs for errors
3. Ensure all files are in correct locations
4. Verify JMeter and Java versions are compatible
