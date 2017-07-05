import GPSTrack from './GPSTrack';

const EVENT_NAME = 'GPSTrackChangeEvent';

interface GPSTrackChangeEvent {
    track: GPSTrack;
}

export {
    EVENT_NAME,
};

export default GPSTrackChangeEvent;