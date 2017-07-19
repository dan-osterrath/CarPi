import MapConfiguration from './model/MapConfiguration';

class MapEndpoint {
    public getMapConfig(): Promise<MapConfiguration> {
        return fetch('/api/map/config').then(response => response.json());
    }

    public getGeoJson(): Promise<GeoJSONGeoJsonObject> {
        return fetch('/api/map/geojson').then(response => response.json());
    }
}

export default MapEndpoint;