interface GPSPosition {
    latitude: number;
    longitude: number;
    altitude: number;
    latitudeError: number;
    longitudeError: number;
    altitudeError: number;
    speed: number;
    climbRate: number;
    speedError: number;
    climbRateError: number;
    timestamp: number;
}

export default GPSPosition;