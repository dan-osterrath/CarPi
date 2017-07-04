import GPSPosition from './GPSPosition';

const EVENT_NAME = 'GPSPositionChangeEvent';

interface GPSPositionChangeEvent {
    location: GPSPosition;
}

export {
    EVENT_NAME,
};

export default GPSPositionChangeEvent;