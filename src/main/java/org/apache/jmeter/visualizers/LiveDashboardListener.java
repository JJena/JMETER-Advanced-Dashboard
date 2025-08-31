package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Live Dashboard Listener for JMeter
 * Creates a live JTL file in JMeter bin directory and provides link to HTML dashboard
 */
public class LiveDashboardListener extends AbstractVisualizer {
    
    private static final Logger log = LoggerFactory.getLogger(LiveDashboardListener.class);
    private static final String JTL_FILENAME = "live-dashboard.jtl";
    private static final String HTML_FILENAME = "jmeter-dashboard.html";
    
    private BufferedWriter jtlWriter;
    private File jtlFile;
    private File htmlFile;
    private JButton dashboardButton;
    
    private boolean headerWritten = false;
    
    public LiveDashboardListener() {
        super();
        init();
        initGui();
    }
    
    @Override
    public String getLabelResource() {
        return null; // Return null to use getStaticLabel() instead
    }
    
    @Override
    public String getStaticLabel() {
        return "Advanced JMeter Dashboard";
    }
    
    private void init() {
        // Get JMeter bin directory
        String jmeterBin = JMeterUtils.getJMeterBinDir();
        jtlFile = new File(jmeterBin, JTL_FILENAME);
        
        // Check if HTML file exists in bin directory
        htmlFile = new File(jmeterBin, HTML_FILENAME);
        
        log.info("Live Dashboard Listener initialized");
        log.info("JTL file will be written to: " + jtlFile.getAbsolutePath());
        log.info("HTML dashboard expected at: " + htmlFile.getAbsolutePath());
    }
    
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        initializeJTLFile();
    }
    
    private void initializeJTLFile() {
        try {
            // Create/recreate the JTL file
            if (jtlFile.exists()) {
                jtlFile.delete();
            }
            jtlFile.createNewFile();
            
            // Initialize writer
            jtlWriter = new BufferedWriter(new FileWriter(jtlFile, false));
            headerWritten = false;
            log.info("JTL file initialized: " + jtlFile.getName());
            
        } catch (IOException e) {
            log.error("Failed to initialize JTL file", e);
        }
    }
    
    private void initGui() {
        setLayout(new BorderLayout());
        
        // Main panel with centered content
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Top panel with title and button
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("📊 Advanced JMeter Dashboard");
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        titlePanel.add(titleLabel);
        topPanel.add(titlePanel);
        
        // Add some spacing
        topPanel.add(javax.swing.Box.createVerticalStrut(20));
        
        // Dashboard button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        dashboardButton = new JButton("🌐 Open Live Dashboard");
        dashboardButton.setFont(dashboardButton.getFont().deriveFont(14f));
        dashboardButton.setPreferredSize(new java.awt.Dimension(200, 40));
        dashboardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDashboard();
            }
        });
        
        buttonPanel.add(dashboardButton);
        topPanel.add(buttonPanel);
        
        // Add spacing
        topPanel.add(javax.swing.Box.createVerticalStrut(20));
        
        // Instructions panel
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Setup Instructions"));
        
        String[] instructions = {
            "1. Ensure jmeter-dashboard.html is in JMeter bin directory",
            "2. Start your test plan",
            "3. Click 'Open Live Dashboard' to view real-time results",
            "4. The dashboard auto-refreshes every 5 seconds"
        };
        
        for (String instruction : instructions) {
            JLabel instructionLabel = new JLabel(instruction);
            instructionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
            instructionsPanel.add(instructionLabel);
        }
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(instructionsPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    

    
    private void openDashboard() {
        try {
            if (htmlFile.exists()) {
                // Try to open via HTTP server if available, otherwise use file:// with warning
                String httpUrl = "http://localhost:8080/jmeter-dashboard.html";
                try {
                    // Test if local server is running
                    java.net.URL url = new java.net.URL(httpUrl);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("HEAD");
                    connection.setConnectTimeout(1000);
                    connection.connect();
                    
                    if (connection.getResponseCode() == 200) {
                        // Server is running, open HTTP URL
                        Desktop.getDesktop().browse(new java.net.URI(httpUrl));
                        log.info("Dashboard opened at: " + httpUrl);
                    } else {
                        throw new Exception("Server not responding");
                    }
                } catch (Exception serverEx) {
                    // Fallback to file:// with instructions
                    Desktop.getDesktop().browse(htmlFile.toURI());
                    JOptionPane.showMessageDialog(this, 
                        "Dashboard opened with file:// protocol.\n\n" +
                        "For live data functionality, please:\n" +
                        "1. Open terminal in JMeter bin directory\n" +
                        "2. Run: python3 -m http.server 8080\n" +
                        "3. Open: http://localhost:8080/jmeter-dashboard.html\n\n" +
                        "Live data requires HTTP protocol for security reasons.",
                        "Live Data Setup", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "HTML file not found!\n" +
                    "Please copy jmeter-dashboard.html to: " + htmlFile.getParent(),
                    "File Not Found", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            log.error("Failed to open dashboard", ex);
            JOptionPane.showMessageDialog(this, 
                "Failed to open dashboard: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    

    
    @Override
    public void add(SampleResult sample) {
        if (jtlWriter == null) {
            return;
        }
        
        try {
            // Write header if not written yet
            if (!headerWritten) {
                writeJTLHeader();
                headerWritten = true;
            }
            
            // Write sample data
            writeSampleToJTL(sample);
            
            // Sample processed successfully
            
        } catch (IOException e) {
            log.error("Failed to write sample to JTL file", e);
        }
    }
    
    private void writeJTLHeader() throws IOException {
        String header = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Filename,latency,encoding,SampleCount,ErrorCount,Hostname,IdleTime,Connect";
        jtlWriter.write(header);
        jtlWriter.newLine();
        jtlWriter.flush();
    }
    
    private void writeSampleToJTL(SampleResult sample) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        // timeStamp
        sb.append(sample.getTimeStamp()).append(",");
        // elapsed
        sb.append(sample.getTime()).append(",");
        // label
        sb.append(escapeCSV(sample.getSampleLabel())).append(",");
        // responseCode
        sb.append(sample.getResponseCode()).append(",");
        // responseMessage
        sb.append(escapeCSV(sample.getResponseMessage())).append(",");
        // threadName
        sb.append(escapeCSV(sample.getThreadName())).append(",");
        // dataType
        sb.append(sample.getDataType()).append(",");
        // success
        sb.append(sample.isSuccessful()).append(",");
        // failureMessage
        sb.append(escapeCSV(sample.getFirstAssertionFailureMessage())).append(",");
        // bytes
        sb.append(sample.getBytesAsLong()).append(",");
        // sentBytes
        sb.append(sample.getSentBytes()).append(",");
        // grpThreads
        sb.append(sample.getGroupThreads()).append(",");
        // allThreads
        sb.append(sample.getAllThreads()).append(",");
        // URL
        sb.append(escapeCSV(sample.getURL() != null ? sample.getURL().toString() : "")).append(",");
        // Filename
        sb.append(",");
        // latency
        sb.append(sample.getLatency()).append(",");
        // encoding
        sb.append("UTF-8").append(",");
        // SampleCount
        sb.append("1").append(",");
        // ErrorCount
        sb.append(sample.isSuccessful() ? "0" : "1").append(",");
        // Hostname
        sb.append("localhost").append(",");
        // IdleTime
        sb.append(sample.getIdleTime()).append(",");
        // Connect
        sb.append(sample.getConnectTime());
        
        jtlWriter.write(sb.toString());
        jtlWriter.newLine();
        jtlWriter.flush(); // Immediate flush for live viewing
    }
    
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Simple CSV escaping - replace commas and quotes
        return value.replace(",", ";").replace("\"", "'");
    }
    

    
    @Override
    public void clearData() {
        initializeJTLFile();
    }
    
    @Override
    public void finalize() throws Throwable {
        if (jtlWriter != null) {
            try {
                jtlWriter.close();
            } catch (IOException e) {
                log.error("Failed to close JTL writer", e);
            }
        }
        super.finalize();
    }
}
