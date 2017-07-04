import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';
import {Map as LeafletMap} from 'leaflet';
import 'leaflet/dist/leaflet-src.js';
import '!style-loader!css-loader!resolve-url-loader!leaflet/dist/leaflet.css';
import LatLng = L.LatLng;
import Marker = L.Marker;
import CircleMarker = L.CircleMarker;
import MarkerOptions = L.MarkerOptions;
import CircleMarkerOptions = L.CircleMarkerOptions;
import MapOptions = L.MapOptions;
import Point = L.Point;

import * as styles from './Map.scss';

import MapConfiguration from '../../api/model/MapConfiguration';
import GPSPosition from '../../api/model/GPSPosition';

import {AppState, getMapConfig} from '../../reducers/reducers';

const MAX_ZOOM = 13;

interface MapComponentProps {
    disableZoom?: boolean;
    position?: GPSPosition;
}

interface ContainerDispatchProps {
}

interface ContainerStateProps {
    mapConfig?: MapConfiguration;
}

type ContainerOwnProps = MapComponentProps & ProviderProps;

type MapProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

class Map extends React.Component<MapProps, {}> {
    map: LeafletMap;
    marker: Marker;
    circle: CircleMarker;
    mapContainer: HTMLDivElement | null;

    constructor(props: MapProps) {
        super(props);
    }

    componentDidMount() {
        this.initializeMap(this.props);
    }

    componentWillReceiveProps(nextProps: MapProps) {
        this.initializeMap(nextProps);
        if (this.map) {
            if (nextProps.disableZoom !== this.props.disableZoom) {
                this.map.options.zoomControl = !nextProps.disableZoom;
                if (nextProps.disableZoom) {
                    this.map.dragging.disable();
                    this.map.touchZoom.disable();
                    this.map.doubleClickZoom.disable();
                } else {
                    this.map.dragging.enable();
                    this.map.touchZoom.enable();
                    this.map.doubleClickZoom.enable();
                }
            }
            const nextPosition = nextProps.position;
            if (nextProps.mapConfig && nextPosition && !this.positionEquals(nextPosition, this.props.position)) {
                const latLng = new LatLng(nextPosition.latitude, nextPosition.longitude, nextPosition.altitude);
                const radius = Math.max(nextPosition.latitudeError, nextPosition.longitudeError);
                const showCircle = this.showCircle(radius, latLng, this.map.getZoom());
                this.map.setView(latLng, Math.min(MAX_ZOOM, nextProps.mapConfig.maxZoom));
                this.marker.setLatLng(latLng);
                this.circle.options.opacity = showCircle ? 0.8 : 0;
                this.circle.options.fillOpacity = showCircle ? 0.1 : 0;
                this.circle.setLatLng(latLng);
                this.circle.setRadius(radius);
            }
        }
    }

    shouldComponentUpdate() {
        return false;
    }

    render() {
        return (
            <div ref={(div) => this.mapContainer = div} className={styles.map}/>
        );
    }

    private initializeMap(props: MapProps) {
        const position = props.position;
        if (!this.map && this.mapContainer && props.mapConfig && position) {
            const mapOptions: MapOptions = {
                zoomControl: !(props.disableZoom === true),
            };

            const latLng = new LatLng(position.latitude, position.longitude, position.altitude);
            const zoom = Math.min(MAX_ZOOM, props.mapConfig.maxZoom);
            this.map = L.map(this.mapContainer, mapOptions).setView(latLng, zoom);
            // L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            L.tileLayer('/api/map/{z}/{x}/{y}', {
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            }).addTo(this.map);

            const radius = Math.max(position.latitudeError, position.longitudeError);
            const showCircle = this.showCircle(radius, latLng, zoom);
            const circleOptions: CircleMarkerOptions = {
                color: '#00bcd4',
                interactive: false,
                weight: 1,
                radius: radius,
                opacity: showCircle ? 0.8 : 0,
                fillOpacity: showCircle ? 0.1 : 0,
            };
            this.circle = L.circle(latLng, circleOptions).addTo(this.map);

            const markerOptions: MarkerOptions = {
                keyboard: false,
                icon: L.divIcon({
                    iconAnchor: new Point(22, 43),
                }),
            };
            this.marker = L.marker(latLng, markerOptions).addTo(this.map);
            if (props.disableZoom) {
                this.map.dragging.disable();
                this.map.touchZoom.disable();
                this.map.doubleClickZoom.disable();
            }
        }
    }

    private positionEquals(pos1?: GPSPosition, pos2?: GPSPosition): boolean {
        if (pos1 === pos2) {
            return true;
        }
        if (pos1) {
            if (pos2) {
                if (pos1.latitude !== pos2.latitude) {
                    return false;
                } else if (pos1.longitude !== pos2.longitude) {
                    return false;
                } else if (pos1.altitude !== pos2.altitude) {
                    return false;
                } else if (pos1.longitudeError !== pos2.longitudeError) {
                    return false;
                } else if (pos1.latitudeError !== pos2.latitudeError) {
                    return false;
                } else if (pos1.altitudeError !== pos2.altitudeError) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        } else {
            return !!pos2;
        }
    }

    private getMetersInPixel(meters: number, latLng: LatLng, zoom: number): number {
        const metresPerPixel = 40075016.686 * Math.abs(Math.cos(latLng.lat * 180 / Math.PI)) / Math.pow(2, zoom + 8);
        return meters / metresPerPixel;
    }

    private showCircle(radius: number, latLng: LatLng, zoom: number): boolean {
        if (radius < 10) {
            return false;
        }
        const pixels = this.getMetersInPixel(radius, latLng, zoom);
        return pixels >= 20;
    }
}

const Map$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        mapConfig: getMapConfig(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({})
)(Map);

export default Map$$;
