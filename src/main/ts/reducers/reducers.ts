import {Action, handleActions} from 'redux-actions';
import {Actions} from '../actions/actions';

import MapConfiguration from '../api/model/MapConfiguration';
import GPSData from '../api/model/GPSData';
import GPSPosition from '../api/model/GPSPosition';
import GPSMetaInfo from '../api/model/GPSMetaInfo';

interface AppState extends Readonly<{}> {
    mapConfig?: MapConfiguration;
    gpsData?: GPSData;
}

const initialState: AppState = {};

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
        [Actions.RECEIVE_GPS_DATA]: (state: AppState, action: Action<GPSData>) => {
            if (action.error || !action.payload) {
                return state;
            }
            return {
                ...state,
                gpsData: action.payload,
            };
        },
        [Actions.RECEIVE_GPS_POSITION]: (state: AppState, action: Action<GPSPosition>) => {
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
        [Actions.RECEIVE_GPS_META]: (state: AppState, action: Action<GPSMetaInfo>) => {
            if (action.error || !action.payload) {
                return state;
            }
            return {
                ...state,
                gpsData: {
                    ...state.gpsData,
                    meta: action.payload,
                }
            };
        },
    },
    initialState
);

function getMapConfig(state: AppState): MapConfiguration|undefined {
    return state.mapConfig;
}

function getCurrentGPSData(state: AppState): GPSData|undefined {
    return state.gpsData;
}

export {
    AppState,

    getMapConfig,
    getCurrentGPSData,
};

export default reducers;