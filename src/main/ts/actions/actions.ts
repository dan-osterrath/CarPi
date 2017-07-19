import {Dispatch} from 'react-redux';
import { createAction } from 'redux-actions';

import {AppState} from '../reducers/reducers';
import MapEndpoint from '../api/MapEndpoint';
import GPSEndpoint from '../api/GPSEndpoint';
import MapConfiguration from '../api/model/MapConfiguration';
import EventMessage from '../api/model/EventMessage';
import GPSData from '../api/model/GPSData';
import GPSPosition from '../api/model/GPSPosition';

const mapEndpoint: MapEndpoint = new MapEndpoint();
const gpsEndpoint: GPSEndpoint = new GPSEndpoint();

const Actions = {
    REQUEST_INITIAL_DATA: 'app/INITIAL_DATA',
    REQUEST_MAP_CONFIG: 'map/REQUEST_CONFIG',
    RECEIVE_MAP_CONFIG: 'map/RECEIVE_CONFIG',
    REQUEST_MAP_GEO_JSON: 'map/REQUEST_GEO_JSON',
    RECEIVE_MAP_GEO_JSON: 'map/RECEIVE_GEO_JSON',
    REQUEST_GPS_DATA: 'gps/REQUEST_DATA',
    RECEIVE_GPS_DATA: 'gps/RECEIVE_DATA',
    REQUEST_GPS_POSITION: 'gps/REQUEST_POSITION',
    RECEIVE_GPS_POSITION: 'gps/RECEIVE_POSITION',
    WEBSOCKET_CONNECT: 'websocket/CONNECT',
    WEBSOCKET_CONNECTED: 'websocket/CONNECTED',
    WEBSOCKET_DISCONNECT: 'websocket/DISCONNECT',
    WEBSOCKET_DISCONNECTED: 'websocket/DISCONNECTED',
    WEBSOCKET_SEND: 'websocket/SEND',
    WEBSOCKET_RECEIVED: 'websocket/RECEIVED',
    WEBSOCKET_SUBSCRIBE: 'websocket/RECEIVED',
    WEBSOCKET_UNSUBCRIBE: 'websocket/RECEIVED',
};

const requestMapConfig = createAction(Actions.REQUEST_MAP_CONFIG);
const receiveMapConfig = createAction<MapConfiguration, MapConfiguration>(Actions.RECEIVE_MAP_CONFIG, (config: MapConfiguration) => config);
const loadMapConfig = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(requestMapConfig());
    mapEndpoint.getMapConfig().then(response => {
        dispatch(receiveMapConfig(response));
        if (response.withGeoJson) {
            dispatch(loadMapGeoJson());
        }
    });
    return Promise.resolve();
};

const requestMapGeoJson = createAction(Actions.REQUEST_MAP_GEO_JSON);
const receiveMapGeoJson = createAction<GeoJSONGeoJsonObject, GeoJSONGeoJsonObject>(Actions.RECEIVE_MAP_GEO_JSON, (geoJson: GeoJSONGeoJsonObject) => geoJson);
const loadMapGeoJson = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(requestMapGeoJson);
    mapEndpoint.getGeoJson().then(response => {
        dispatch(receiveMapGeoJson(response));
    });
    return Promise.resolve();
};

const requestGpsData = createAction(Actions.REQUEST_GPS_DATA);
const receiveGpsData = createAction<GPSData, GPSData>(Actions.RECEIVE_GPS_DATA, (data: GPSData) => data);
const loadGpsData = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(requestGpsData());
    gpsEndpoint.getCurrentData().then(response => {
        dispatch(receiveGpsData(response));
    });
    return Promise.resolve();
};

const requestGpsPosition = createAction(Actions.REQUEST_GPS_POSITION);
const receiveGpsPosition = createAction<GPSPosition, GPSPosition>(Actions.RECEIVE_GPS_POSITION, (position: GPSPosition) => position);
const loadGpsPosition = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(requestGpsPosition());
    gpsEndpoint.getCurrentPosition().then(response => {
        dispatch(receiveGpsPosition(response));
    });
    return Promise.resolve();
};

const connectWebsocket = createAction(Actions.WEBSOCKET_CONNECT);
const websocketConnected = createAction(Actions.WEBSOCKET_CONNECTED);
const disconnectWebsocket = createAction(Actions.WEBSOCKET_DISCONNECT);
const websocketDisconnected = createAction(Actions.WEBSOCKET_DISCONNECTED);
const sendWebsocket = createAction<{}, {}>(Actions.WEBSOCKET_SEND, (data: {}) => data);
const receivedWebsocket = createAction<EventMessage, EventMessage>(Actions.WEBSOCKET_RECEIVED, (data: EventMessage) => data);

const subscribeEvent = (event: string) => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(sendWebsocket({SUBSCRIBE: event}));
    return Promise.resolve();
};
const unsubscribeEvent = (event: string) => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(sendWebsocket({UNSUBSCRIBE: event}));
    return Promise.resolve();
};

const loadInitialData = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(loadMapConfig());
    dispatch(connectWebsocket());
    return Promise.resolve();
};

export {
    Actions,

    requestMapConfig,
    receiveMapConfig,
    loadMapConfig,

    requestMapGeoJson,
    receiveMapGeoJson,
    loadMapGeoJson,

    requestGpsData,
    receiveGpsData,
    loadGpsData,

    requestGpsPosition,
    receiveGpsPosition,
    loadGpsPosition,

    connectWebsocket,
    websocketConnected,
    disconnectWebsocket,
    websocketDisconnected,
    sendWebsocket,
    receivedWebsocket,

    subscribeEvent,
    unsubscribeEvent,

    loadInitialData,
};