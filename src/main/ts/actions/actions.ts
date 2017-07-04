import {Dispatch} from 'react-redux';
import { createAction } from 'redux-actions';

import {AppState} from '../reducers/reducers';
import MapEndpoint from '../api/MapEndpoint';
import MapConfiguration from '../api/model/MapConfiguration';
import EventMessage from '../api/model/EventMessage';

const mapEndpoint: MapEndpoint = new MapEndpoint();

const Actions = {
    REQUEST_INITIAL_DATA: 'app/INITIAL_DATA',
    REQUEST_MAP_CONFIG: 'map/REQUEST_CONFIG',
    RECEIVE_MAP_CONFIG: 'map/RECEIVE_CONFIG',
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