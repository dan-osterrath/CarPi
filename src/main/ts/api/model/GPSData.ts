import GPSPosition from './GPSPosition';
import GPSMetaInfo from './GPSMetaInfo';
import GPSTrack from './GPSTrack';

interface GPSData {
    position?: GPSPosition;
    meta?: GPSMetaInfo;
    track?: GPSTrack;

}

export default GPSData;