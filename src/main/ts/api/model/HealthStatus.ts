interface HealthStatus {
    gpuTemperature: number;
    cpuTemperature: number;
    cpuVoltage: number;
    cpuUsage: number;
    systemLoad: number;
    discFree: number;
    discTotal: number;
    memFree: number;
    memTotal: number;
}

export default HealthStatus;