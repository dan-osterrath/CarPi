import GPSData from './model/GPSData';
import GPSPosition from './model/GPSPosition';
import GPSMetaInfo from './model/GPSMetaInfo';
import GPSTrack from './model/GPSTrack';

class GPSEndpoint {
    public getCurrentData(): Promise<GPSData> {
        return fetch('/api/gps').then(response => response.json());
    }

    public getCurrentPosition(): Promise<GPSPosition> {
        return fetch('/api/gps/position').then(response => response.json());
    }

    public getCurrentMetaInfo(): Promise<GPSMetaInfo> {
        return fetch('/api/gps/meta').then(response => response.json());
    }

    public getCurrentTrack(): Promise<GPSTrack> {
        return fetch('/api/gps/track').then(response => response.json());
    }
}

export default GPSEndpoint;