package org.apache.jmeter.visualizers;

/**
 * Snapshot of system metrics at a point in time
 */
public class SystemMetricsSnapshot {
    
    private final double cpuUsage;
    private final double memoryUsage;
    private final long usedMemoryMB;
    private final long availableMemoryMB;
    private final boolean isFresh;
    private final long timestamp;
    
    public SystemMetricsSnapshot(double cpuUsage, double memoryUsage, long usedMemoryMB, 
                                long availableMemoryMB, boolean isFresh) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.usedMemoryMB = usedMemoryMB;
        this.availableMemoryMB = availableMemoryMB;
        this.isFresh = isFresh;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get CPU usage percentage
     * @return CPU usage percentage, or -1 if not available
     */
    public double getCpuUsage() {
        return cpuUsage;
    }
    
    /**
     * Get memory usage percentage
     * @return Memory usage percentage, or -1 if not available
     */
    public double getMemoryUsage() {
        return memoryUsage;
    }
    
    /**
     * Get used memory in MB
     * @return Used memory in MB
     */
    public long getUsedMemoryMB() {
        return usedMemoryMB;
    }
    
    /**
     * Get available memory in MB
     * @return Available memory in MB
     */
    public long getAvailableMemoryMB() {
        return availableMemoryMB;
    }
    
    /**
     * Check if this snapshot contains fresh data
     * @return true if fresh data, false if cached
     */
    public boolean isFresh() {
        return isFresh;
    }
    
    /**
     * Get timestamp when this snapshot was created
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Check if CPU metrics are available
     * @return true if CPU metrics are available
     */
    public boolean isCpuAvailable() {
        return cpuUsage >= 0;
    }
    
    /**
     * Check if memory metrics are available
     * @return true if memory metrics are available
     */
    public boolean isMemoryAvailable() {
        return memoryUsage >= 0;
    }
    
    @Override
    public String toString() {
        return String.format("SystemMetricsSnapshot{cpu=%.2f%%, memory=%.2f%%, used=%dMB, available=%dMB, fresh=%s}", 
                           cpuUsage, memoryUsage, usedMemoryMB, availableMemoryMB, isFresh);
    }
}
