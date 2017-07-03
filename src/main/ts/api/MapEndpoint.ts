import MapConfiguration from './model/MapConfiguration';

class MapEndpoint {
    public getMapConfig(): Promise<MapConfiguration> {
        return fetch('/api/map/config').then(response => response.json());
    }
}

export default MapEndpoint;