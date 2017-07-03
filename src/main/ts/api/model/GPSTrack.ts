interface Element {
    longitude: number;
    latitude: number;
    altitude: number;

}

interface GPSTrack {
    start: number;
    distance: number;
    path: Array<Element>;

}

export default GPSTrack;