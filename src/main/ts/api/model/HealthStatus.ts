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
    batteryVoltage: number;
    inputVoltage: number;
}

export default HealthStatus;