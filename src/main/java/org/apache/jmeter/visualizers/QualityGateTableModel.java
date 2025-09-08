package org.apache.jmeter.visualizers;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for quality gate configuration
 */
public class QualityGateTableModel extends AbstractTableModel {
    
    private final List<QualityGateMetric> metrics;
    private final String[] columnNames = {
        "Enabled", "Metric", "Aggregate", "Operator", "Pass Value", "Warning Value", "Weightage"
    };
    
    public QualityGateTableModel() {
        this.metrics = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return metrics.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Boolean.class;  // Enabled
            case 1: return String.class;   // Metric
            case 2: return String.class;   // Aggregate
            case 3: return String.class;   // Operator
            case 4: return String.class;   // Pass Value
            case 5: return String.class;   // Warning Value
            case 6: return String.class;   // Weightage
            default: return Object.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= metrics.size()) {
            return null;
        }
        
        QualityGateMetric metric = metrics.get(rowIndex);
        switch (columnIndex) {
            case 0: return metric.isEnabled();
            case 1: return metric.getMetric();
            case 2: return metric.getAggregate();
            case 3: return metric.getOperator();
            case 4: return metric.getPassValue() != null ? metric.getPassValue().toString() : "";
            case 5: return metric.getWarningValue() != null ? metric.getWarningValue().toString() : "";
            case 6: return metric.getWeightage() != null ? metric.getWeightage().toString() : "1.0";
            default: return null;
        }
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (rowIndex >= metrics.size()) {
            return;
        }
        
        QualityGateMetric metric = metrics.get(rowIndex);
        switch (columnIndex) {
            case 0:
                metric.setEnabled((Boolean) value);
                break;
            case 1:
                metric.setMetric((String) value);
                break;
            case 2:
                metric.setAggregate((String) value);
                break;
            case 3:
                metric.setOperator((String) value);
                break;
            case 4:
                String passStr = (String) value;
                metric.setPassValue(passStr.isEmpty() ? null : Float.parseFloat(passStr));
                break;
            case 5:
                String warningStr = (String) value;
                metric.setWarningValue(warningStr.isEmpty() ? null : Float.parseFloat(warningStr));
                break;
            case 6:
                String weightageStr = (String) value;
                metric.setWeightage(weightageStr.isEmpty() ? 1.0f : Float.parseFloat(weightageStr));
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    /**
     * Adds a new row with default values
     */
    public void addRow() {
        QualityGateMetric metric = new QualityGateMetric();
        metric.setEnabled(true);
        metric.setMetric("Response Time");
        metric.setAggregate("Avg");
        metric.setOperator("<");
        metric.setPassValue(1000.0f);
        metric.setWarningValue(2000.0f);
        metric.setWeightage(1.0f);
        
        metrics.add(metric);
        fireTableRowsInserted(metrics.size() - 1, metrics.size() - 1);
    }
    
    /**
     * Adds a new row with specified values
     */
    public void addRow(Boolean enabled, String metric, String aggregate, String operator, 
                      Float passValue, Float warningValue, Float weightage) {
        QualityGateMetric qualityGateMetric = new QualityGateMetric();
        qualityGateMetric.setEnabled(enabled);
        qualityGateMetric.setMetric(metric);
        qualityGateMetric.setAggregate(aggregate);
        qualityGateMetric.setOperator(operator);
        qualityGateMetric.setPassValue(passValue);
        qualityGateMetric.setWarningValue(warningValue);
        qualityGateMetric.setWeightage(weightage);
        
        metrics.add(qualityGateMetric);
        fireTableRowsInserted(metrics.size() - 1, metrics.size() - 1);
    }
    
    /**
     * Removes a row at the specified index
     */
    public void removeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < metrics.size()) {
            metrics.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }
    
    /**
     * Clears all rows
     */
    public void clear() {
        int size = metrics.size();
        metrics.clear();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    /**
     * Gets the list of metrics
     */
    public List<QualityGateMetric> getMetrics() {
        return new ArrayList<>(metrics);
    }
}
