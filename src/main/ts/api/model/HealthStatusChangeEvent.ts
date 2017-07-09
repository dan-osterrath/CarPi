import HealthStatus from './HealthStatus';

const EVENT_NAME = 'HealthStatusChangeEvent';

interface HealthStatusChangeEvent {
    status: HealthStatus;
}

export {
    EVENT_NAME,
};

export default HealthStatusChangeEvent;