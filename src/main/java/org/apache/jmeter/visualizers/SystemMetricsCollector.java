package org.apache.jmeter.visualizers;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * System metrics collector for monitoring CPU and memory usage using OSHI library
 * Provides cross-platform system information without requiring native libraries
 */
public class SystemMetricsCollector {
    
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hal;
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    
    private long lastCollectionTime = 0;
    private final long collectionInterval;
    
    // Cached values to avoid repeated system calls
    private double lastCpuUsage = 0.0;
    private double lastMemoryUsage = 0.0;
    private long lastUsedMemoryMB = 0;
    private long lastAvailableMemoryMB = 0;
    
    // CPU tick tracking for accurate CPU usage calculation
    private long[] prevTicks = null;
    private long prevTime = 0;
    
    /**
     * Default constructor with 5-second collection interval
     */
    public SystemMetricsCollector() {
        this(5000); // 5 seconds default
    }
    
    /**
     * Constructor with custom collection interval
     * @param collectionIntervalMs Collection interval in milliseconds
     */
    public SystemMetricsCollector(long collectionIntervalMs) {
        this.collectionInterval = collectionIntervalMs;
        this.systemInfo = new SystemInfo();
        this.hal = systemInfo.getHardware();
        this.processor = hal.getProcessor();
        this.memory = hal.getMemory();
    }
    
    /**
     * Collects system metrics if enough time has passed since last collection
     * @return SystemMetricsSnapshot with current or cached values
     */
    public SystemMetricsSnapshot collectMetrics() {
        long currentTime = System.currentTimeMillis();
        
        // Return cached values if not enough time has passed
        if (currentTime - lastCollectionTime < collectionInterval) {
            return new SystemMetricsSnapshot(
                lastCpuUsage,
                lastMemoryUsage,
                lastUsedMemoryMB,
                lastAvailableMemoryMB,
                false // cached data, not fresh
            );
        }
        
        // Collect fresh metrics
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();
        long usedMemoryMB = getUsedMemoryMB();
        long availableMemoryMB = getAvailableMemoryMB();
        
        // Update cached values
        lastCpuUsage = cpuUsage;
        lastMemoryUsage = memoryUsage;
        lastUsedMemoryMB = usedMemoryMB;
        lastAvailableMemoryMB = availableMemoryMB;
        lastCollectionTime = currentTime;
        
        return new SystemMetricsSnapshot(
            cpuUsage,
            memoryUsage,
            usedMemoryMB,
            availableMemoryMB,
            true // fresh data
        );
    }
    
    /**
     * Alias for collectMetrics() to maintain compatibility
     * @return SystemMetricsSnapshot with current or cached values
     */
    public SystemMetricsSnapshot collectMetricsIfNeeded() {
        return collectMetrics();
    }
    
    /**
     * Get CPU usage percentage using OSHI
     * @return CPU usage percentage (0-100)
     */
    private double getCpuUsage() {
        try {
            // Get current CPU ticks
            long[] ticks = processor.getSystemCpuLoadTicks();
            long currentTime = System.currentTimeMillis();
            
            if (prevTicks != null && prevTime > 0) {
                // Calculate CPU usage based on tick differences
                long[] tickDiff = new long[ticks.length];
                for (int i = 0; i < ticks.length; i++) {
                    tickDiff[i] = ticks[i] - prevTicks[i];
                }
                
                // Calculate total and idle time
                long totalTicks = 0;
                long idleTicks = 0;
                
                for (int i = 0; i < tickDiff.length; i++) {
                    totalTicks += tickDiff[i];
                    if (i == CentralProcessor.TickType.IDLE.getIndex()) {
                        idleTicks = tickDiff[i];
                    }
                }
                
                if (totalTicks > 0) {
                    double cpuUsage = (double) (totalTicks - idleTicks) / totalTicks * 100.0;
                    prevTicks = ticks.clone();
                    prevTime = currentTime;
                    return Math.max(0.0, Math.min(100.0, cpuUsage));
                }
            }
            
            // First run or calculation failed, initialize for next time
            prevTicks = ticks.clone();
            prevTime = currentTime;
            
            // Fallback: use OSHI's built-in CPU load calculation
            double systemCpuLoad = processor.getSystemCpuLoad(1000); // 1 second delay
            if (systemCpuLoad >= 0) {
                return systemCpuLoad * 100.0;
            }
            
        } catch (Exception e) {
            // If any error occurs, return cached value or 0
        }
        
        return lastCpuUsage > 0 ? lastCpuUsage : 0.0;
    }
    
    /**
     * Get memory usage percentage using OSHI
     * @return Memory usage percentage (0-100)
     */
    private double getMemoryUsage() {
        try {
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            
            if (totalMemory > 0) {
                long usedMemory = totalMemory - availableMemory;
                return (double) usedMemory / totalMemory * 100.0;
            }
        } catch (Exception e) {
            // If any error occurs, return cached value or 0
        }
        
        return lastMemoryUsage > 0 ? lastMemoryUsage : 0.0;
    }
    
    /**
     * Get used memory in MB using OSHI
     * @return Used memory in MB
     */
    private long getUsedMemoryMB() {
        try {
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            return usedMemory / (1024 * 1024); // Convert bytes to MB
        } catch (Exception e) {
            // If any error occurs, return cached value or 0
        }
        
        return lastUsedMemoryMB > 0 ? lastUsedMemoryMB : 0;
    }
    
    /**
     * Get available memory in MB using OSHI
     * @return Available memory in MB
     */
    private long getAvailableMemoryMB() {
        try {
            long availableMemory = memory.getAvailable();
            return availableMemory / (1024 * 1024); // Convert bytes to MB
        } catch (Exception e) {
            // If any error occurs, return cached value or 0
        }
        
        return lastAvailableMemoryMB > 0 ? lastAvailableMemoryMB : 0;
    }
    
    /**
     * Get system information string for debugging
     * @return System information string
     */
    public String getSystemInfo() {
        try {
            return String.format("OS: %s %s, CPU: %s, Memory: %d MB",
                systemInfo.getOperatingSystem().getFamily(),
                systemInfo.getOperatingSystem().getVersionInfo().getVersion(),
                processor.getProcessorIdentifier().getName(),
                memory.getTotal() / (1024 * 1024)
            );
        } catch (Exception e) {
            return "System info unavailable";
        }
    }
    
    /**
     * Check if the collector is properly initialized
     * @return true if initialized successfully
     */
    public boolean isInitialized() {
        return systemInfo != null && hal != null && processor != null && memory != null;
    }
}