const cpuTemperatureIsOk = (cpuTemperature: number) => cpuTemperature < 80;
const gpuTemperatureIsOk = (gpuTemperature: number) => gpuTemperature < 80;
const systemLoadIsOk = (systemLoad: number) => systemLoad < 3.5;
const memUsageIsOk = (memTotal: number, memFree: number) => memTotal > 0 && memFree / memTotal > 0.1;
const discUsageIsOk = (discTotal: number, discFree: number) => discTotal > 0 && discFree / discTotal > 0.1;
const cpuVoltageIsOk = (cpuVoltage: number) => cpuVoltage <= 0 || (cpuVoltage > 1.15 && cpuVoltage < 1.3);
const cpuUsageIsOk = (cpuUsage: number) => cpuUsage < 95;

export {
    cpuTemperatureIsOk,
    gpuTemperatureIsOk,
    systemLoadIsOk,
    memUsageIsOk,
    discUsageIsOk,
    cpuVoltageIsOk,
    cpuUsageIsOk,
};