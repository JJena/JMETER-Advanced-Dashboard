package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Live Dashboard Listener for JMeter
 * Provides real-time monitoring with system resource tracking and quality gate functionality
 */
public class LiveDashboardListener extends AbstractVisualizer implements TestStateListener {
    
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(LiveDashboardListener.class);
    
    // Constants
    private static final String JTL_FILENAME = "live-dashboard.jtl";
    private static final String HTML_FILENAME = "jmeter-dashboard.html";
    private static final String SYSTEM_METRICS_FILENAME = ".jmeter-system-metrics.csv";
    private static final int DEFAULT_PORT = 9090;
    
    // GUI Components
    private JTextField portField;
    private JButton openDashboardButton;
    private JLabel instructionsLabel;
    
    // File and server management
    private File jtlFile;
    private File htmlFile;
    private File systemMetricsFile;
    private BufferedWriter jtlWriter;
    private BufferedWriter systemMetricsWriter;
    private HttpServer httpServer;
    private boolean headerWritten = false;
    private static boolean serverStarted = false; // Static to prevent multiple servers
    
    // System metrics collector
    private SystemMetricsCollector systemMetricsCollector;
    
    // Quality gate manager
    private QualityGateManager qualityGateManager;
    private java.util.Timer systemMetricsTimer;
    
    public LiveDashboardListener() {
        super();
        init();
        initGui();
    }
    
    @Override
    public String getLabelResource() {
        return "Advanced JMeter Dashboard";
    }
    
    public void init() {
        
        // Get JMeter bin directory
        File jmeterBin = new File(JMeterUtils.getJMeterBinDir());
        
        // Initialize JTL file
        jtlFile = new File(jmeterBin, JTL_FILENAME);
        
        // Check if HTML file exists in bin directory
        htmlFile = new File(jmeterBin, HTML_FILENAME);
        
        // Initialize system metrics collector
        systemMetricsCollector = new SystemMetricsCollector(5000); // 5 seconds interval
        
        // Initialize quality gate manager
        qualityGateManager = new QualityGateManager();
    }
    
    public void initGui() {
        
        // Main panel with vertical layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Quality Gate Panel (at the top)
        JPanel qualityGatePanel = qualityGateManager.createQualityGatePanel();
        mainPanel.add(qualityGatePanel);
        
        // Live Dashboard Panel (at the bottom)
        JPanel dashboardPanel = createDashboardPanel();
        mainPanel.add(dashboardPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Live Dashboard Settings"));
        
        // Port and button row
        JPanel portRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portRow.add(new JLabel("Port:"));
        portField = new JTextField(String.valueOf(DEFAULT_PORT), 8);
        portRow.add(portField);
        
        openDashboardButton = new JButton("Open Live Dashboard");
        openDashboardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDashboard();
            }
        });
        portRow.add(openDashboardButton);
        
        panel.add(portRow);
        
        // Instructions
        JPanel instructionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        instructionsLabel = new JLabel("<html><body style='width: 600px'>" +
            "<b>Quick Setup:</b><br>" +
            "1. Configure quality gate above<br>" +
            "2. Start test<br>" +
            "3. Open Live Dashboard</body></html>");
        instructionsPanel.add(instructionsLabel);
        panel.add(instructionsPanel);
        
        return panel;
    }
    
    private void openDashboard() {
        try {
            int port = Integer.parseInt(portField.getText());
            
            // Start HTTP server if not already started
            if (!serverStarted) {
                startHttpServer(port);
            }
            
            // Open dashboard in browser
            String dashboardUrl = "http://localhost:" + port + "/jmeter-dashboard.html";
            Desktop.getDesktop().browse(new java.net.URI(dashboardUrl));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening dashboard: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            log.error("Error opening dashboard", e);
        }
    }
    
    private void startHttpServer(int port) throws IOException {
        httpServer = HttpServer.create(new java.net.InetSocketAddress(port), 0);
        
        // File handler for serving HTML and other files
        httpServer.createContext("/", new FileHandler());
        
        // System metrics handler
        httpServer.createContext("/system-metrics", new SystemMetricsHandler());
        
        // Quality gate handler
        httpServer.createContext("/quality-gate", new QualityGateHandler());
        
        // Target scores handler
        httpServer.createContext("/target-scores", new TargetScoresHandler());
        
        httpServer.setExecutor(null);
        httpServer.start();
        serverStarted = true;
    }
    
    @Override
    public void testStarted() {
        testStarted("");
    }
    
    @Override
    public void testStarted(String host) {
        // Test started
    }
    
    @Override
    public void testEnded() {
        testEnded("");
    }
    
    @Override
    public void testEnded(String host) {
        try {
            // Stop system metrics collection
            if (systemMetricsTimer != null) {
                systemMetricsTimer.cancel();
                systemMetricsTimer = null;
            }
            
            // Close writers
            if (jtlWriter != null) {
                jtlWriter.close();
                jtlWriter = null;
            }
            
            if (systemMetricsWriter != null) {
                systemMetricsWriter.close();
                systemMetricsWriter = null;
            }
            
        } catch (IOException e) {
            log.error("Error closing files after test end", e);
        }
    }
    
    
    /**
     * Initialize system metrics collection (called from sampleOccurred() since testStarted() might not be called)
     */
    private void initializeSystemMetricsCollection() {
        // Stop any existing timer first
        stopSystemMetricsTimer();
        
        // Reset system metrics collector to ensure fresh collection
        systemMetricsCollector = new SystemMetricsCollector();
        
        // Create fresh system metrics file
        createFreshSystemMetricsFile();
        
        // Start system metrics collection timer
        startSystemMetricsCollection();
    }
    
    private void createFreshSystemMetricsFile() {
        try {
            // Get JMeter bin directory (same as JTL file)
            File jmeterBin = new File(JMeterUtils.getJMeterBinDir());
            systemMetricsFile = new File(jmeterBin, SYSTEM_METRICS_FILENAME);
            
            // Delete existing file if it exists
            if (systemMetricsFile.exists()) {
                systemMetricsFile.delete();
            }
            
            // Create new file with header
            systemMetricsWriter = new BufferedWriter(new FileWriter(systemMetricsFile, false));
            systemMetricsWriter.write("timestamp,cpu_usage,memory_usage,used_memory_mb,available_memory_mb\n");
            systemMetricsWriter.flush();
            
        } catch (IOException e) {
            log.error("Error creating system metrics file", e);
        }
    }
    
    private void stopSystemMetricsTimer() {
        if (systemMetricsTimer != null) {
            systemMetricsTimer.cancel();
            systemMetricsTimer = null;
        }
    }
    
    private void startSystemMetricsCollection() {
        systemMetricsTimer = new java.util.Timer("SystemMetricsTimer", true);
        systemMetricsTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    SystemMetricsSnapshot metrics = systemMetricsCollector.collectMetricsIfNeeded();
                    if (metrics.isFresh()) {
                        writeSystemMetrics(metrics);
                    }
                } catch (Exception e) {
                    log.error("Error collecting system metrics", e);
                }
            }
        }, 0, 5000); // Every 5 seconds
    }
    
    private void writeSystemMetrics(SystemMetricsSnapshot metrics) {
        try {
            if (systemMetricsWriter != null) {
                systemMetricsWriter.write(String.format("%d,%.2f,%.2f,%d,%d\n",
                    metrics.getTimestamp(),
                    metrics.getCpuUsage(),
                    metrics.getMemoryUsage(),
                    metrics.getUsedMemoryMB(),
                    metrics.getAvailableMemoryMB()
                ));
                systemMetricsWriter.flush();
            }
        } catch (IOException e) {
            log.error("Error writing system metrics", e);
        }
    }
    
    @Override
    public void add(SampleResult result) {
        sampleOccurred(result);
    }
    
    public void sampleOccurred(SampleResult result) {
        // Initialize JTL writer if not already done
        if (jtlWriter == null) {
            try {
                jtlWriter = new BufferedWriter(new FileWriter(jtlFile));
                headerWritten = false;
            } catch (IOException e) {
                log.error("Error initializing JTL file", e);
            return;
            }
        }
        
        try {
            // Write JTL header if not written yet
            if (!headerWritten) {
                jtlWriter.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n");
                headerWritten = true;
                
                // Initialize system metrics collection on first sample (since testStarted() might not be called)
                initializeSystemMetricsCollection();
            }
            
            // Write sample result to JTL file
            jtlWriter.write(String.format("%d,%d,%s,%s,%s,%s,%s,%s,%s,%d,%d,%d,%d,%s,%d,%d,%d\n",
                result.getTimeStamp(),
                result.getTime(),
                result.getSampleLabel(),
                result.getResponseCode(),
                result.getResponseMessage(),
                result.getThreadName(),
                result.getDataType(),
                result.isSuccessful(),
                result.getResponseMessage(),
                result.getBytesAsLong(),
                result.getSentBytes(),
                result.getGroupThreads(),
                result.getAllThreads(),
                result.getURL(),
                result.getLatency(),
                result.getIdleTime(),
                result.getConnectTime()
            ));
            jtlWriter.flush();
            
        } catch (IOException e) {
            log.error("Error writing sample result to JTL file", e);
        }
    }
    
    @Override
    public void clearGui() {
        super.clearGui();
    }
    
    @Override
    public void clearData() {
        // Clear any cached data if needed
    }
    
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        
        // Initialize quality gate manager if not already done
        if (qualityGateManager == null) {
            qualityGateManager = new QualityGateManager();
        }
        
        // Load quality gate configuration
        qualityGateManager.loadQualityGateConfiguration(element);
        
        // Load port setting
        if (portField != null) {
            portField.setText(element.getPropertyAsString("PORT", String.valueOf(DEFAULT_PORT)));
        }
    }
    
    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);
        
        // Initialize quality gate manager if not already done
        if (qualityGateManager == null) {
            qualityGateManager = new QualityGateManager();
        }
        
        // Save quality gate configuration
        qualityGateManager.saveQualityGateConfiguration(element);
        
        // Save port setting
        if (portField != null) {
            element.setProperty("PORT", portField.getText());
        }
    }
    
    // HTTP Handlers
    private class FileHandler implements HttpHandler {
    @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/") || path.equals("/jmeter-dashboard.html")) {
                // Serve the HTML dashboard
                serveFile(exchange, htmlFile, "text/html");
            } else if (path.equals("/" + JTL_FILENAME)) {
                // Serve the JTL file
                serveFile(exchange, jtlFile, "text/plain");
            } else if (path.equals("/.jmeter-system-metrics.csv")) {
                // Serve the system metrics file
                serveFile(exchange, systemMetricsFile, "text/plain");
            } else {
                // 404 Not Found
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        private void serveFile(HttpExchange exchange, File file, String contentType) throws IOException {
            if (file != null && file.exists()) {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, fileBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }
            } else {
                String response = "File not found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
    
    private class SystemMetricsHandler implements HttpHandler {
    @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                SystemMetricsSnapshot metrics = systemMetricsCollector.collectMetricsIfNeeded();
                String response = String.format("{\"timestamp\":%d,\"cpuUsage\":%.2f,\"memoryUsage\":%.2f,\"usedMemoryMB\":%d,\"availableMemoryMB\":%d}",
                    metrics.getTimestamp(),
                    metrics.getCpuUsage(),
                    metrics.getMemoryUsage(),
                    metrics.getUsedMemoryMB(),
                    metrics.getAvailableMemoryMB()
                );
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                log.error("Error handling system metrics request", e);
                String response = "{\"error\":\"Failed to get system metrics\"}";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
    
    private class QualityGateHandler implements HttpHandler {
    @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<QualityGateMetric> metrics = qualityGateManager.getQualityGateMetrics();
                StringBuilder response = new StringBuilder();
                response.append("{\"metrics\":[");
                
                for (int i = 0; i < metrics.size(); i++) {
                    QualityGateMetric metric = metrics.get(i);
                    if (i > 0) response.append(",");
                    response.append(String.format(
                        "{\"enabled\":%s,\"metric\":\"%s\",\"aggregate\":\"%s\",\"operator\":\"%s\",\"passValue\":%s,\"warningValue\":%s,\"weightage\":%s}",
                        metric.isEnabled(),
                        metric.getMetric(),
                        metric.getAggregate(),
                        metric.getOperator(),
                        metric.getPassValue() != null ? metric.getPassValue() : "null",
                        metric.getWarningValue() != null ? metric.getWarningValue() : "null",
                        metric.getWeightage() != null ? metric.getWeightage() : "1.0"
                    ));
                }
                
                response.append("]}");
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.toString().getBytes());
                }
            } catch (Exception e) {
                log.error("Error handling quality gate request", e);
                String response = "{\"error\":\"Failed to get quality gate configuration\"}";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
    
    private class TargetScoresHandler implements HttpHandler {
    @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                float[] scores = qualityGateManager.getTargetScores();
                String response = String.format("{\"passScore\":%.2f,\"warningScore\":%.2f}",
                    scores[0], scores[1]);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                log.error("Error handling target scores request", e);
                String response = "{\"error\":\"Failed to get target scores\"}";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
}
