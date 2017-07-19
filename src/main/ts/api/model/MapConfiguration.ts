interface MapConfiguration {
    minZoom: number;
    maxZoom: number;
    type?: 'JPEG'|'PNG'|'VECTOR';
    withGeoJson: boolean;
}

export default MapConfiguration;