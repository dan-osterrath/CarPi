import GPSMetaInfo from './GPSMetaInfo';

const EVENT_NAME = 'GPSMetaInfoChangeEvent';

interface GPSMetaInfoChangeEvent {
    metaInfo: GPSMetaInfo;
}

export {
    EVENT_NAME,
};

export default GPSMetaInfoChangeEvent;