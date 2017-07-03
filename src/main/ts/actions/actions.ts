import {Dispatch} from 'react-redux';
import { createAction } from 'redux-actions';

import {AppState} from '../reducers/reducers';
import MapEndpoint from '../api/MapEndpoint';
import GPSEndpoint from '../api/GPSEndpoint';
import MapConfiguration from '../api/model/MapConfiguration';
import GPSData from '../api/model/GPSData';
import GPSPosition from '../api/model/GPSPosition';
import GPSMetaInfo from '../api/model/GPSMetaInfo';

const mapEndpoint: MapEndpoint = new MapEndpoint();
const gpsEndpoint: GPSEndpoint = new GPSEndpoint();

const Actions = {
    REQUEST_INITIAL_DATA: 'app/INITIAL_DATA',
    REQUEST_MAP_CONFIG: 'map/REQUEST_CONFIG',
    RECEIVE_MAP_CONFIG: 'map/RECEIVE_CONFIG',
    REQUEST_GPS_DATA: 'gps/REQUEST_DATA',
    RECEIVE_GPS_DATA: 'gps/RECEIVE_DATA',
    REQUEST_GPS_POSITION: 'gps/RECEIVE_POSITION',
    RECEIVE_GPS_POSITION: 'gps/RECEIVE_POSITION',
    REQUEST_GPS_META: 'gps/RECEIVE_META',
    RECEIVE_GPS_META: 'gps/RECEIVE_META',
    WEBSOCKET_CONNECT: 'websocket/CONNECT',
    WEBSOCKET_CONNECTED: 'websocket/CONNECTED',
    WEBSOCKET_DISCONNECT: 'websocket/DISCONNECT',
    WEBSOCKET_DISCONNECTED: 'websocket/DISCONNECTED',
    WEBSOCKET_SEND: 'websocket/SEND',
};

const requestMapConfig = createAction(Actions.REQUEST_MAP_CONFIG);
const receiveMapConfig = createAction<MapConfiguration, MapConfiguration>(Actions.RECEIVE_MAP_CONFIG, (config: MapConfiguration) => config);
const loadMapConfig = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(requestMapConfig());
    mapEndpoint.getMapConfig().then(response => {
        dispatch(receiveMapConfig(response));
    });
    return Promise.resolve();
};

const requestGPSData = createAction(Actions.REQUEST_GPS_DATA);
const receiveGPSData = createAction<GPSData, GPSData>(Actions.RECEIVE_GPS_DATA, (data: GPSData) => data);
const loadGPSData = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(requestGPSData());
    gpsEndpoint.getCurrentData().then(response => {
        dispatch(receiveGPSData(response));
    });
    return Promise.resolve();
};

const requestGPSPosition = createAction(Actions.REQUEST_GPS_POSITION);
const receiveGPSPosition = createAction<GPSPosition, GPSPosition>(Actions.RECEIVE_GPS_POSITION, (data: GPSPosition) => data);
const loadGPSPosition = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(requestGPSPosition());
    gpsEndpoint.getCurrentPosition().then(response => {
        dispatch(receiveGPSPosition(response));
    });
    return Promise.resolve();
};

const requestGPSMetaInfo = createAction(Actions.REQUEST_GPS_META);
const receiveGPSMetaInfo = createAction<GPSMetaInfo, GPSMetaInfo>(Actions.RECEIVE_GPS_META, (data: GPSMetaInfo) => data);
const loadGPSMetaInfo = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(requestGPSMetaInfo());
    gpsEndpoint.getCurrentMetaInfo().then(response => {
        dispatch(receiveGPSMetaInfo(response));
    });
    return Promise.resolve();
};

const connectWebsocket = createAction(Actions.WEBSOCKET_CONNECT);
const websocketConnected = createAction(Actions.WEBSOCKET_CONNECTED);
const disconnectWebsocket = createAction(Actions.WEBSOCKET_DISCONNECT);
const websocketDisconnected = createAction(Actions.WEBSOCKET_DISCONNECTED);
const sendWebsocket = createAction<{}, {}>(Actions.WEBSOCKET_CONNECT, (data: {}) => data);

const loadInitialData = () => (dispatch: Dispatch<AppState>): Promise<void> => {
    dispatch(loadMapConfig());
    dispatch(loadGPSData());
    dispatch(connectWebsocket());
    return Promise.resolve();
};

export {
    Actions,

    requestMapConfig,
    receiveMapConfig,
    loadMapConfig,

    requestGPSData,
    receiveGPSData,
    loadGPSData,

    requestGPSPosition,
    receiveGPSPosition,
    loadGPSPosition,

    requestGPSMetaInfo,
    receiveGPSMetaInfo,
    loadGPSMetaInfo,

    connectWebsocket,
    websocketConnected,
    disconnectWebsocket,
    websocketDisconnected,
    sendWebsocket,

    loadInitialData,
};