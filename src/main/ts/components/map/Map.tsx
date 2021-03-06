import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';
import {Map as LeafletMap} from 'leaflet';
import 'leaflet/dist/leaflet-src.js';
import '!style-loader!css-loader!resolve-url-loader!leaflet/dist/leaflet.css';

import * as styles from './Map.scss';

import MapConfiguration from '../../api/model/MapConfiguration';
import GPSPosition from '../../api/model/GPSPosition';
import GPSTrack, {Element as TrackElement} from '../../api/model/GPSTrack';

import {AppState, getMapConfig} from '../../reducers/reducers';

const MAX_ZOOM = 17;
const CLASS_DISABLED = 'leaflet-disabled';

interface PointDetails {
    name: string;
    description: string;
    position?: L.LatLng;
}

interface MapComponentProps {
    disableZoom?: boolean;
    position?: GPSPosition;
    track?: GPSTrack;
    geoJson?: GeoJSONGeoJsonObject;
    showScale?: boolean;
    onShowDetails?: (details: PointDetails) => void;
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
    tiles: L.TileLayer;
    path?: L.Polyline;
    geoJson?: L.GeoJSON;
    marker: L.Marker;
    circle: L.CircleMarker;
    zoomButtons: L.Control;
    followButton: L.Control;
    scale?: L.Control.Scale;
    mapContainer: HTMLDivElement | null;
    zoomInBtn: HTMLAnchorElement;
    zoomOutBtn: HTMLAnchorElement;
    followModeActive: boolean = true;
    positionBeforeDragging?: L.LatLng = undefined;
    dragging: boolean = false;
    zooming: boolean = false;

    constructor(props: MapProps) {
        super(props);
    }

    componentDidMount() {
        this.initializeMap(this.props);
    }

    componentWillUnmount() {
        if (this.map) {
            if (this.path) {
                this.map.removeLayer(this.path);
            }
            this.map.removeLayer(this.marker);
            this.map.removeLayer(this.circle);
            this.map.removeLayer(this.tiles);
            if (this.followButton) {
                this.map.removeControl(this.followButton);
            }
            if (this.zoomButtons) {
                this.map.removeControl(this.zoomButtons);
            }
            if (this.scale) {
                this.map.removeControl(this.scale);
            }
            if (this.geoJson) {
                this.map.removeLayer(this.geoJson);
            }
            this.map.remove();
        }
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

            if (nextProps.showScale !== this.props.showScale) {
                if (nextProps.showScale) {
                    this.scale = L.control.scale().addTo(this.map);
                } else if (this.scale) {
                    this.map.removeControl(this.scale);
                    this.scale = undefined;
                }
            }

            const nextPosition = nextProps.position;
            if (nextProps.mapConfig && nextPosition && !this.positionEquals(nextPosition, this.props.position)) {
                const latLng = new L.LatLng(nextPosition.latitude, nextPosition.longitude, nextPosition.altitude);
                const radius = Math.max(nextPosition.latitudeError, nextPosition.longitudeError);
                const showCircle = this.showCircle(radius, latLng, this.map.getZoom());
                if (this.followPosition()) {
                    this.map.setView(latLng, this.map.getZoom(), {
                        animate: false,
                        noMoveStart: true
                    });
                }
                this.marker.setLatLng(latLng);
                this.circle.setLatLng(latLng);
                this.circle.setRadius(radius);
                if (showCircle) {
                    this.circle.addTo(this.map);
                    this.marker.addTo(this.map);
                } else {
                    this.circle.remove();
                }
            }

            const nextTrack = nextProps.track;
            if (this.props.track === undefined && nextTrack !== undefined) {
                this.createPath();
                nextTrack.path.map(this.addToPath);
            } else if (this.props.track !== undefined && nextTrack === undefined) {
                if (this.path) {
                    this.map.removeLayer(this.path);
                    this.path = undefined;
                }
            } else if (this.props.track !== undefined && nextTrack !== undefined) {
                if (this.props.track.path.length !== nextTrack.path.length) {
                    if (!this.path || this.props.track.start !== nextTrack.start) {
                        this.createPath();
                        nextTrack.path.map(this.addToPath);
                    } else {
                        nextTrack.path.slice(this.props.track.path.length).map(this.addToPath);
                    }
                } else if (!this.path) {
                    this.createPath();
                    nextTrack.path.map(this.addToPath);
                }
            }

            const nextGeoJson = nextProps.geoJson;
            if (this.props.geoJson === undefined && nextGeoJson !== undefined) {
                this.geoJson = this.createGeoJsonLayer();
            } else if (this.props.geoJson !== undefined && nextGeoJson === undefined) {
                if (this.geoJson) {
                    this.map.removeLayer(this.geoJson);
                    this.geoJson = undefined;
                }
            } else if (this.props.geoJson !== undefined && nextGeoJson !== undefined && this.props.geoJson !== nextGeoJson) {
                if (this.geoJson) {
                    this.map.removeLayer(this.geoJson);
                }
                this.geoJson = this.createGeoJsonLayer();
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
            const mapOptions: L.MapOptions = {
                zoomControl: false,
                maxZoom: Math.min(MAX_ZOOM, props.mapConfig.maxZoom) + 1,
                minZoom: props.mapConfig.minZoom,
            };

            const latLng = new L.LatLng(position.latitude, position.longitude, position.altitude);
            const zoom = Math.min(MAX_ZOOM, props.mapConfig.maxZoom);
            this.map = L.map(this.mapContainer, mapOptions).setView(latLng, zoom);
            this.map.on('dragstart', this.onDragStart);
            this.map.on('dragend', this.onDragEnd);
            this.map.on('zoomstart', this.onZoomStart);
            this.map.on('zoomend', this.onZoomEnd);

            this.tiles = L.tileLayer('/api/map/{z}/{x}/{y}', {
                // attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
                maxNativeZoom: Math.min(MAX_ZOOM, props.mapConfig.maxZoom),
                minNativeZoom: props.mapConfig.minZoom,
            }).addTo(this.map);

            if (this.props.showScale) {
                this.scale = L.control.scale().addTo(this.map);
            }

            if (this.props.geoJson) {
                this.geoJson = this.createGeoJsonLayer();
            }

            if (!this.props.disableZoom) {
                // add zoom buttons
                this.zoomInBtn = L.DomUtil.create('a', 'leaflet-control-zoom-in') as HTMLAnchorElement;
                this.zoomInBtn.innerText = '+';
                this.zoomInBtn.href = '#';
                this.zoomInBtn.onclick = this.onZoomIn;

                this.zoomOutBtn = L.DomUtil.create('a', 'leaflet-control-zoom-out') as HTMLAnchorElement;
                this.zoomOutBtn.innerText = '-';
                this.zoomOutBtn.href = '#';
                this.zoomOutBtn.onclick = this.onZoomOut;

                const zoomContainer = L.DomUtil.create('div', 'leaflet-control-zoom leaflet-bar leaflet-control');
                L.DomEvent.disableClickPropagation(zoomContainer);
                zoomContainer.appendChild(this.zoomInBtn);
                zoomContainer.appendChild(this.zoomOutBtn);

                const zoomButtonOptions: L.Control.ZoomOptions = {
                    position: 'topright',
                };
                const ZoomButtons = L.Control.extend({
                    options: zoomButtonOptions,
                    onAdd: (map: LeafletMap) => zoomContainer,
                });
                this.zoomButtons = new ZoomButtons();
                this.map.addControl(this.zoomButtons);

                // add follow button
                const followBtn: HTMLAnchorElement = L.DomUtil.create('a', 'leaflet-control-follow-btn') as HTMLAnchorElement;
                followBtn.href = '#';
                followBtn.onclick = this.onFollowPosition;

                const followContainer = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-follow');
                followContainer.style.display = 'none';
                L.DomEvent.disableClickPropagation(followContainer);
                followContainer.appendChild(followBtn);

                const followButtonOptions: L.ControlOptions = {
                    position: 'bottomright',
                };
                const FollowButton = L.Control.extend({
                    options: followButtonOptions,
                    onAdd: (map: LeafletMap) => followContainer,
                });
                this.followButton = new FollowButton();
                this.map.addControl(this.followButton);
            }

            const radius = Math.max(position.latitudeError, position.longitudeError);
            const showCircle = this.showCircle(radius, latLng, zoom);
            const circleOptions: L.CircleMarkerOptions = {
                color: '#00bcd4',
                interactive: false,
                weight: 1,
                radius: radius,
                opacity: 0.8,
                fillOpacity: 0.1,
            };
            this.circle = L.circle(latLng, circleOptions);
            if (showCircle) {
                this.circle.addTo(this.map);
            }

            const markerOptions: L.MarkerOptions = {
                keyboard: false,
                icon: L.divIcon({
                    iconAnchor: new L.Point(22, 43),
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

    private getMetersInPixel(meters: number, latLng: L.LatLng, zoom: number): number {
        const metresPerPixel = 40075016.686 * Math.abs(Math.cos(latLng.lat * 180 / Math.PI)) / Math.pow(2, zoom + 8);
        return meters / metresPerPixel;
    }

    private showCircle(radius: number, latLng: L.LatLng, zoom: number): boolean {
        if (radius < 10) {
            return false;
        }
        const pixels = this.getMetersInPixel(radius, latLng, zoom);
        return pixels >= 20;
    }

    private followPosition(): boolean {
        return (!!this.props.disableZoom || this.followModeActive) && !this.dragging;
    }

    private onDragStart = (e: Event) => {
        this.dragging = true;
    };

    private onDragEnd = (e: Event) => {
        this.dragging = false;
        this.followModeActive = this.followModeActive && !!this.positionBeforeDragging && this.map.getCenter().equals(this.positionBeforeDragging);
        this.updateControls();
    };

    private onZoomStart = (e: Event) => {
        this.positionBeforeDragging = this.map.getCenter();
        this.dragging = true;
    };

    private onZoomEnd = (e: Event) => {
        if (!this.zooming) {
            // zoom by manual zoom
            this.followModeActive = this.followModeActive && !!this.positionBeforeDragging && this.map.getCenter().equals(this.positionBeforeDragging);
        }
        this.dragging = false;
        this.zooming = false;
        this.updateControls();
    };

    private updateControls() {
        if (this.followButton) {
            const c = this.followButton.getContainer();
            if (c) {
                c.style.display = this.followModeActive ? 'none' : 'block';
            }
        }
        if (this.map.options.maxZoom) {
            const ziClasses = this.zoomInBtn.classList;
            if (this.map.getZoom() < this.map.options.maxZoom) {
                if (ziClasses.contains(CLASS_DISABLED)) {
                    ziClasses.remove(CLASS_DISABLED);
                }
            } else {
                if (!ziClasses.contains(CLASS_DISABLED)) {
                    ziClasses.add(CLASS_DISABLED);
                }
            }
        }
        if (this.map.options.minZoom) {
            const zoClasses = this.zoomOutBtn.classList;
            if (this.map.getZoom() > this.map.options.minZoom) {
                if (zoClasses.contains(CLASS_DISABLED)) {
                    zoClasses.remove(CLASS_DISABLED);
                }
            } else {
                if (!zoClasses.contains(CLASS_DISABLED)) {
                    zoClasses.add(CLASS_DISABLED);
                }
            }
        }
    }

    private onFollowPosition = (e: MouseEvent): void => {
        this.map.setView(this.marker.getLatLng(), this.map.getZoom(), {
            animate: true,
            duration: 1,
            easeLinearity: 2,
            noMoveStart: true
        });
        window.setTimeout(
            () => {
                this.followModeActive = true;
                this.updateControls();
            },
            1000
        );
    };

    private onZoomIn = (e: MouseEvent): void => {
        this.zooming = true;
        this.map.setZoom(this.map.getZoom() + 1, {animate: true, duration: 1});
    };

    private onZoomOut = (e: MouseEvent): void => {
        this.zooming = true;
        this.map.setZoom(this.map.getZoom() - 1, {animate: true, duration: 1});
    };

    private createPath() {
        if (this.path) {
            this.map.removeLayer(this.path);
        }

        const pathOptions: L.PolylineOptions = {
            interactive: false,
            color: '#00bcd4',
            opacity: 0.8,
        };
        this.path = L.polyline([], pathOptions).addTo(this.map);
    }

    private addToPath = (e: TrackElement): void => {
        const latLng = new L.LatLng(e.latitude, e.longitude, e.altitude);
        if (this.path) {
            this.path.addLatLng(latLng);
        }
    };

    private createGeoJsonLayer(): L.GeoJSON {
        return L.geoJSON(
            this.props.geoJson,
            {
                style: this.getGeoJsonStyle,
                pointToLayer: this.getGeoJsonPointLayer,
                onEachFeature: this.handleGeoJsonFeature,
            }
        ).addTo(this.map);
    }

    private getGeoJsonStyle = (feature?: GeoJSONFeature<GeoJSONGeometryObject>): L.PathOptions => {
        return {
            color: '#757575',
            opacity: 0.8,
            interactive: (feature && feature.geometry.type === 'Point'),
        };
    };

    private getGeoJsonPointLayer = (geoJsonPoint: GeoJSONFeature<GeoJSONPoint>, latlng: L.LatLng): L.Layer => {
        /* tslint:disable-next-line:no-string-literal */
        const isClickable = geoJsonPoint.properties && (geoJsonPoint.properties['name'] || geoJsonPoint.properties['Name']) && geoJsonPoint.properties['description'];
        return L.circleMarker(latlng, {
            radius: isClickable ? 20 : 7,
            fillColor: '#757575',
            color: '#000',
            weight: isClickable ? 1 : 0,
            opacity: isClickable ? 0.8 : 0.3,
            fillOpacity: isClickable ? 0.5 : 0.3,
            interactive: isClickable,
        });
    };

    private handleGeoJsonFeature = (feature: GeoJSONFeature<GeoJSONGeometryObject>, layer: L.Layer): void => {
        if (feature.geometry.type === 'Point' && feature.properties) {
            let name: string | undefined = undefined;
            let description: string | undefined = undefined;
            let position: L.LatLng | undefined = undefined;
            /* tslint:disable:no-string-literal */
            if (feature.properties.hasOwnProperty('name')) {
                name = feature.properties['name'];
            } else if (feature.properties.hasOwnProperty('Name')) {
                name = feature.properties['Name'];
            }
            if (feature.properties.hasOwnProperty('description')) {
                description = feature.properties['description'];
            }
            if (feature.geometry.hasOwnProperty('coordinates')) {
                const coordinates: number[] = feature.geometry['coordinates'];
                position = new L.LatLng(coordinates[1], coordinates[0]);
            }
            /* tslint:enable:no-string-literal */

            if (name !== null && description !== null) {
                layer.on('click', (e) => this.showGeoJsonInfo({name: name || '', description: description || '', position}));
            }
        }
    };

    private showGeoJsonInfo(details: PointDetails): void {
        if (this.props.onShowDetails) {
            this.props.onShowDetails(details);
        }
    }
}

const Map$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        mapConfig: getMapConfig(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({})
)(Map);

export {
    PointDetails,
};

export default Map$$;
