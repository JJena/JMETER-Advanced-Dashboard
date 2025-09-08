package org.apache.jmeter.visualizers;

/**
 * Represents a quality gate metric configuration
 */
public class QualityGateMetric {
    
    private boolean enabled;
    private String metric;
    private String aggregate;
    private String operator;
    private Float passValue;
    private Float warningValue;
    private Float weightage;
    
    public QualityGateMetric() {
        this.enabled = true;
        this.metric = "Response Time";
        this.aggregate = "Avg";
        this.operator = "<";
        this.passValue = 1000.0f;
        this.warningValue = 2000.0f;
        this.weightage = 1.0f;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getMetric() {
        return metric;
    }
    
    public void setMetric(String metric) {
        this.metric = metric;
    }
    
    public String getAggregate() {
        return aggregate;
    }
    
    public void setAggregate(String aggregate) {
        this.aggregate = aggregate;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public Float getPassValue() {
        return passValue;
    }
    
    public void setPassValue(Float passValue) {
        this.passValue = passValue;
    }
    
    public Float getWarningValue() {
        return warningValue;
    }
    
    public void setWarningValue(Float warningValue) {
        this.warningValue = warningValue;
    }
    
    public Float getWeightage() {
        return weightage;
    }
    
    public void setWeightage(Float weightage) {
        this.weightage = weightage;
    }
    
    @Override
    public String toString() {
        return String.format("QualityGateMetric{enabled=%s, metric='%s', aggregate='%s', operator='%s', passValue=%s, warningValue=%s, weightage=%s}",
                           enabled, metric, aggregate, operator, passValue, warningValue, weightage);
    }
}
