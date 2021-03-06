import {Action, handleActions} from 'redux-actions';
import {Actions} from '../actions/actions';

import MapConfiguration from '../api/model/MapConfiguration';
import GPSData from '../api/model/GPSData';
import GPSPosition from '../api/model/GPSPosition';
import EventMessage from '../api/model/EventMessage';
import GPSPositionChangeEvent, {EVENT_NAME as GPSPositionChangeEventName} from '../api/model/GPSPositionChangeEvent';
import GPSMetaInfoChangeEvent, {EVENT_NAME as GPSMetaInfoChangeEventName} from '../api/model/GPSMetaInfoChangeEvent';
import GPSTrackChangeEvent, {EVENT_NAME as GPSTrackChangeEventName} from '../api/model/GPSTrackChangeEvent';
import HealthStatusChangeEvent, {EVENT_NAME as HealthStatusChangeEventName} from '../api/model/HealthStatusChangeEvent';
import GPSMetaInfo from '../api/model/GPSMetaInfo';
import HealthStatus from '../api/model/HealthStatus';
import * as HealthStatusUtils from '../helpers/HeathStatusUtils';

interface AppState extends Readonly<{}> {
    mapConfig?: MapConfiguration;
    gpsData?: GPSData;
    geoJson?: GeoJSONGeoJsonObject;
    healthStatus?: HealthStatus;
    websocketConnected: boolean;
    healthIsOk: boolean;
}

const initialState: AppState = {
    websocketConnected: false,
    healthIsOk: true,
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

        [Actions.RECEIVE_MAP_GEO_JSON]: (state: AppState, action: Action<GeoJSONGeoJsonObject>) => {
            if (action.error || !action.payload) {
                return state;
            }
            return {
                ...state,
                geoJson: action.payload,
            };
        },

        [Actions.REQUEST_GPS_DATA]: (state: AppState, action: Action<GPSData>) => {
            if (action.error || !action.payload) {
                return state;
            }
            return {
                ...state,
                gpsData: action.payload,
            };
        },

        [Actions.REQUEST_GPS_POSITION]: (state: AppState, action: Action<GPSPosition>) => {
            if (action.error || !action.payload) {
                return state;
            }
            return {
                ...state,
                gpsData: {
                    ...state.gpsData,
                    position: action.payload,
                }
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
                case HealthStatusChangeEventName:
                    const e4: HealthStatusChangeEvent = action.payload.event as HealthStatusChangeEvent;
                    const healthStatus = e4.status;
                    const healthIsOk = //
                        HealthStatusUtils.cpuTemperatureIsOk(healthStatus.cpuTemperature) && //
                        HealthStatusUtils.gpuTemperatureIsOk(healthStatus.gpuTemperature) && //
                        HealthStatusUtils.cpuUsageIsOk(healthStatus.cpuUsage) && //
                        HealthStatusUtils.cpuVoltageIsOk(healthStatus.cpuVoltage) && //
                        HealthStatusUtils.discUsageIsOk(healthStatus.discTotal, healthStatus.discFree) && //
                        HealthStatusUtils.memUsageIsOk(healthStatus.memTotal, healthStatus.memFree) && //
                        HealthStatusUtils.systemLoadIsOk(healthStatus.systemLoad) && //
                        HealthStatusUtils.batteryVoltageIsOk(healthStatus.batteryVoltage) && //
                        HealthStatusUtils.inputVoltageIsOk(healthStatus.inputVoltage);
                    return {
                        ...state,
                        healthStatus,
                        healthIsOk,
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

function isHealthStatusOk(state: AppState): boolean {
    return state.healthIsOk;
}

function getMapConfig(state: AppState): MapConfiguration|undefined {
    return state.mapConfig;
}

function getGeoJson(state: AppState): GeoJSONGeoJsonObject|undefined {
    return state.geoJson;
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

function getCurrentHealthStatus(state: AppState): HealthStatus|undefined {
    return state.healthStatus;
}

export {
    AppState,

    isWebSocketConnected,
    isHealthStatusOk,
    getMapConfig,
    getGeoJson,
    getCurrentGPSData,
    getCurrentGPSPosition,
    getCurrentGPSMetaInfo,
    getCurrentHealthStatus,
};

export default reducers;