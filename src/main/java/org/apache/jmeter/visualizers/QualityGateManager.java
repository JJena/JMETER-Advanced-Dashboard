package org.apache.jmeter.visualizers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

/**
 * Manages quality gate configuration and validation
 */
public class QualityGateManager {
    
    // Quality gate table model
    private QualityGateTableModel qualityGateTableModel;
    private JTable qualityGateTable;
    
    // Target scores fields
    private JTextField passScoreField;
    private JTextField warningScoreField;
    
    // Constants for property names
    private static final String QUALITY_GATE_PROPERTY = "QUALITY_GATE_CONFIG";
    private static final String PASS_SCORE_PROPERTY = "PASS_SCORE";
    private static final String WARNING_SCORE_PROPERTY = "WARNING_SCORE";
    
    // Validation patterns
    
    /**
     * Creates the quality gate panel
     */
    public JPanel createQualityGatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Quality Gate Configuration"));
        
        // Target Scores Panel
        JPanel targetScoresPanel = createTargetScoresPanel();
        panel.add(targetScoresPanel, BorderLayout.NORTH);
        
        // Quality Gate Table Panel
        JPanel tablePanel = createQualityGateTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);
        
        // Buttons Panel
        JPanel buttonsPanel = createButtonsPanel();
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the target scores panel
     */
    private JPanel createTargetScoresPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Target Scores"));
        
        // Pass Score
        panel.add(new JLabel("Pass Score:"));
        passScoreField = new JTextField("80", 10);
        passScoreField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}
            
            @Override
            public void focusLost(FocusEvent e) {
                validatePassScore();
            }
        });
        panel.add(passScoreField);
        
        // Warning Score
        panel.add(new JLabel("Warning Score:"));
        warningScoreField = new JTextField("60", 10);
        warningScoreField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}
            
            @Override
            public void focusLost(FocusEvent e) {
                validateWarningScore();
            }
        });
        panel.add(warningScoreField);
        
        return panel;
    }
    
    /**
     * Creates the quality gate table panel
     */
    private JPanel createQualityGateTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table model and table
        qualityGateTableModel = new QualityGateTableModel();
        qualityGateTable = new JTable(qualityGateTableModel);
        
        // Setup table renderers and editors
        setupTableRenderersAndEditors();
        
        // Setup table selection mode
        qualityGateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        qualityGateTable.setRowSelectionAllowed(true);
        qualityGateTable.setColumnSelectionAllowed(false);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(qualityGateTable);
        scrollPane.setPreferredSize(new Dimension(800, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the buttons panel
     */
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Add Row button
        JButton addRowButton = new JButton("Add Row");
        addRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                qualityGateTableModel.addRow();
            }
        });
        panel.add(addRowButton);
        
        // Remove Row button
        JButton removeRowButton = new JButton("Remove Row");
        removeRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = qualityGateTable.getSelectedRow();
                if (selectedRow >= 0) {
                    qualityGateTableModel.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(panel, "Please select a row to remove.", 
                                                "No Row Selected", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        panel.add(removeRowButton);
        
        return panel;
    }
    
    /**
     * Sets up table renderers and editors
     */
    private void setupTableRenderersAndEditors() {
        // Metric column - dropdown
        JComboBox<String> metricCombo = new JComboBox<>(new String[]{
            "Response Time", "Throughput", "Error Rate", "CPU Usage", "Memory Usage"
        });
        qualityGateTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(metricCombo));
        
        // Aggregate column - dropdown
        JComboBox<String> aggregateCombo = new JComboBox<>(new String[]{
            "Avg", "Max", "Min", "99Pct", "95Pct", "90Pct"
        });
        qualityGateTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(aggregateCombo));
        
        // Operator column - dropdown
        JComboBox<String> operatorCombo = new JComboBox<>(new String[]{
            "<", ">", "<=", ">="
        });
        qualityGateTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(operatorCombo));
        
        // Pass Value and Warning Value columns - text fields with validation
        for (int i = 4; i < 6; i++) {
            JTextField textField = new JTextField();
            textField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {}
                
                @Override
                public void focusLost(FocusEvent e) {
                    validateThresholdValue((JTextField) e.getSource());
                }
            });
            qualityGateTable.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(textField));
        }
        
        // Weightage column - text field with validation
        JTextField weightageField = new JTextField();
        weightageField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}
            
            @Override
            public void focusLost(FocusEvent e) {
                validateWeightageValue((JTextField) e.getSource());
            }
        });
        qualityGateTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(weightageField));
    }
    
    /**
     * Validates pass score
     */
    private void validatePassScore() {
        try {
            float passScore = Float.parseFloat(passScoreField.getText());
            if (passScore < 0 || passScore > 100) {
                JOptionPane.showMessageDialog(passScoreField, "Pass score must be between 0 and 100", 
                                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                passScoreField.setText("80");
                return;
            }
            
            float warningScore = Float.parseFloat(warningScoreField.getText());
            if (passScore <= warningScore) {
                JOptionPane.showMessageDialog(passScoreField, "Pass score must be greater than warning score", 
                                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                passScoreField.setText("80");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(passScoreField, "Please enter a valid number", 
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            passScoreField.setText("80");
        }
    }
    
    /**
     * Validates warning score
     */
    private void validateWarningScore() {
        try {
            float warningScore = Float.parseFloat(warningScoreField.getText());
            if (warningScore < 0 || warningScore > 100) {
                JOptionPane.showMessageDialog(warningScoreField, "Warning score must be between 0 and 100", 
                                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                warningScoreField.setText("60");
                return;
            }
            
            float passScore = Float.parseFloat(passScoreField.getText());
            if (warningScore >= passScore) {
                JOptionPane.showMessageDialog(warningScoreField, "Warning score must be less than pass score", 
                                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                warningScoreField.setText("60");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(warningScoreField, "Please enter a valid number", 
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            warningScoreField.setText("60");
        }
    }
    
    /**
     * Validates threshold value format (now only validates float values since operator is separate)
     */
    private void validateThresholdValue(JTextField textField) {
        String value = textField.getText().trim();
        if (!value.isEmpty()) {
            try {
                Float.parseFloat(value);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(textField, 
                    "Invalid format. Please enter a valid number (e.g., 1000, 2.5)", 
                    "Invalid Format", JOptionPane.ERROR_MESSAGE);
                textField.setText("");
            }
        }
    }
    
    /**
     * Validates weightage value
     */
    private void validateWeightageValue(JTextField textField) {
        try {
            String value = textField.getText().trim();
            if (!value.isEmpty()) {
                float weightage = Float.parseFloat(value);
                if (weightage < 0.0f || weightage > 1.0f) {
                    JOptionPane.showMessageDialog(textField, "Weightage must be between 0.0 and 1.0", 
                                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    textField.setText("1.0");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(textField, "Please enter a valid number", 
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            textField.setText("1.0");
        }
    }
    
    /**
     * Saves quality gate configuration to test element
     */
    public void saveQualityGateConfiguration(org.apache.jmeter.testelement.TestElement element) {
        // Save target scores (only if GUI components are initialized)
        if (passScoreField != null) {
            element.setProperty(PASS_SCORE_PROPERTY, String.valueOf(passScoreField.getText()));
        }
        if (warningScoreField != null) {
            element.setProperty(WARNING_SCORE_PROPERTY, String.valueOf(warningScoreField.getText()));
        }
        
        // Save quality gate metrics
        if (qualityGateTableModel == null) {
            return; // GUI not initialized yet
        }
        List<QualityGateMetric> metrics = qualityGateTableModel.getMetrics();
        StringBuilder config = new StringBuilder();
        for (QualityGateMetric metric : metrics) {
            config.append(metric.isEnabled()).append("|")
                  .append(metric.getMetric()).append("|")
                  .append(metric.getAggregate()).append("|")
                  .append(metric.getOperator()).append("|")
                  .append(metric.getPassValue() != null ? metric.getPassValue() : "null").append("|")
                  .append(metric.getWarningValue() != null ? metric.getWarningValue() : "null").append("|")
                  .append(metric.getWeightage() != null ? metric.getWeightage() : "1.0").append("\n");
        }
        element.setProperty(QUALITY_GATE_PROPERTY, config.toString());
    }
    
    /**
     * Loads quality gate configuration from test element
     */
    public void loadQualityGateConfiguration(org.apache.jmeter.testelement.TestElement element) {
        // Load target scores (only if GUI components are initialized)
        if (passScoreField != null) {
            passScoreField.setText(element.getPropertyAsString(PASS_SCORE_PROPERTY, "80"));
        }
        if (warningScoreField != null) {
            warningScoreField.setText(element.getPropertyAsString(WARNING_SCORE_PROPERTY, "60"));
        }
        
        // Load quality gate metrics
        String config = element.getPropertyAsString(QUALITY_GATE_PROPERTY, "");
        if (!config.isEmpty() && qualityGateTableModel != null) {
            qualityGateTableModel.clear();
            String[] lines = config.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 7) {
                        Boolean enabled = Boolean.parseBoolean(parts[0]);
                        String metric = parts[1];
                        String aggregate = parts[2];
                        String operator = parts[3];
                        Float passValue = "null".equals(parts[4]) ? null : Float.parseFloat(parts[4]);
                        Float warningValue = "null".equals(parts[5]) ? null : Float.parseFloat(parts[5]);
                        Float weightage = Float.parseFloat(parts[6]);
                        
                        qualityGateTableModel.addRow(enabled, metric, aggregate, operator, passValue, warningValue, weightage);
                    }
                }
            }
        }
    }
    
    /**
     * Clears the quality gate configuration
     */
    public void clearQualityGateConfiguration() {
        qualityGateTableModel.clear();
        passScoreField.setText("80");
        warningScoreField.setText("60");
    }
    
    /**
     * Gets the quality gate metrics
     */
    public List<QualityGateMetric> getQualityGateMetrics() {
        return qualityGateTableModel.getMetrics();
    }
    
    /**
     * Gets the target scores
     */
    public float[] getTargetScores() {
        float passScore = Float.parseFloat(passScoreField.getText());
        float warningScore = Float.parseFloat(warningScoreField.getText());
        return new float[]{passScore, warningScore};
    }
}
