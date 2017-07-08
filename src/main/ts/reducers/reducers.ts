import {Action, handleActions} from 'redux-actions';
import {Actions} from '../actions/actions';

import MapConfiguration from '../api/model/MapConfiguration';
import GPSData from '../api/model/GPSData';
import GPSPosition from '../api/model/GPSPosition';
import EventMessage from '../api/model/EventMessage';
import GPSPositionChangeEvent, {EVENT_NAME as GPSPositionChangeEventName} from '../api/model/GPSPositionChangeEvent';
import GPSMetaInfoChangeEvent, {EVENT_NAME as GPSMetaInfoChangeEventName} from '../api/model/GPSMetaInfoChangeEvent';
import GPSTrackChangeEvent, {EVENT_NAME as GPSTrackChangeEventName} from '../api/model/GPSTrackChangeEvent';
import GPSMetaInfo from '../api/model/GPSMetaInfo';

interface AppState extends Readonly<{}> {
    mapConfig?: MapConfiguration;
    gpsData?: GPSData;
    websocketConnected: boolean;
}

const initialState: AppState = {
    websocketConnected: false,
};

const reducers = handleActions<AppState, {}>(
    {
        [Actions.RECEIVE_MAP_CONFIG]: (state: AppState, action: Action<MapConfiguration>) => {
            if (action.error || !action.payload) {
                return state;
            }
            return {
                ...state,
                mapConfig: action.payload,
            };
        },

        [Actions.WEBSOCKET_CONNECTED]: (state: AppState, action: Action<{}>) => {
            return {
                ...state,
                websocketConnected: true,
            };
        },
        [Actions.WEBSOCKET_DISCONNECTED]: (state: AppState, action: Action<{}>) => {
            return {
                ...state,
                websocketConnected: false,
            };
        },
        [Actions.WEBSOCKET_RECEIVED]: (state: AppState, action: Action<EventMessage>) => {
            if (action.error || !action.payload) {
                return state;
            }

            switch (action.payload.type) {
                case GPSPositionChangeEventName:
                    const e1: GPSPositionChangeEvent = action.payload.event as GPSPositionChangeEvent;
                    return {
                        ...state,
                        gpsData: {
                            ...state.gpsData,
                            position: e1.location,
                        }
                    };
                case GPSMetaInfoChangeEventName:
                    const e2: GPSMetaInfoChangeEvent = action.payload.event as GPSMetaInfoChangeEvent;
                    return {
                        ...state,
                        gpsData: {
                            ...state.gpsData,
                            meta: e2.metaInfo,
                        }
                    };
                case GPSTrackChangeEventName:
                    const e3: GPSTrackChangeEvent = action.payload.event as GPSTrackChangeEvent;
                    return {
                        ...state,
                        gpsData: {
                            ...state.gpsData,
                            track: e3.track,
                        }
                    };
                default:
                    console.log('Received unknown message type ' + action.payload.type);
                    return state;
            }
        }
    },
    initialState
);

function isWebSocketConnected(state: AppState): boolean {
    return state.websocketConnected;
}

function getMapConfig(state: AppState): MapConfiguration|undefined {
    return state.mapConfig;
}

function getCurrentGPSData(state: AppState): GPSData|undefined {
    return state.gpsData;
}

function getCurrentGPSPosition(state: AppState): GPSPosition|undefined {
    return state.gpsData ? state.gpsData.position : undefined;
}

function getCurrentGPSMetaInfo(state: AppState): GPSMetaInfo|undefined {
    return state.gpsData ? state.gpsData.meta : undefined;
}

export {
    AppState,

    isWebSocketConnected,
    getMapConfig,
    getCurrentGPSData,
    getCurrentGPSPosition,
    getCurrentGPSMetaInfo,
};

export default reducers;