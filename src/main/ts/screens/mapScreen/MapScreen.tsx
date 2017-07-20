import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';

import * as styles from './MapScreen.scss';
import Map, {PointDetails} from '../../components/map/Map';
import MapPointDetails from '../../components/mapPointDetails/MapPointDetails';

import {AppState, getCurrentGPSData, getGeoJson} from '../../reducers/reducers';
import GPSData from '../../api/model/GPSData';
import {loadGpsData, subscribeEvent, unsubscribeEvent} from '../../actions/actions';
import {EVENT_NAME as GPSPositionChangeEventName} from '../../api/model/GPSPositionChangeEvent';
import {EVENT_NAME as GPSTrackChangeEventName} from '../../api/model/GPSTrackChangeEvent';

interface ContainerDispatchProps {
    loadGpsData: () => void;
    subscribeGpsPosition: () => void;
    unsubscribeGpsPosition: () => void;
    subscribeGpsTrack: () => void;
    unsubscribeGpsTrack: () => void;
}

interface ContainerStateProps {
    gpsData?: GPSData;
    geoJson?: GeoJSONGeoJsonObject;
}

type ContainerOwnProps = ProviderProps;

type MapScreenProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

interface MapScreenState {
    mapPointDetails?: PointDetails;
}

class MapScreen extends React.Component<MapScreenProps, MapScreenState> {

    constructor(props: MapScreenProps) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        this.props.loadGpsData();
        this.props.subscribeGpsPosition();
        this.props.subscribeGpsTrack();
    }

    componentWillUnmount() {
        this.props.unsubscribeGpsPosition();
        this.props.unsubscribeGpsTrack();
    }

    render() {
        return (
            <div className={styles.mapScreen}>
                <MapPointDetails details={this.state.mapPointDetails} onClose={this.closeMapPointDetails}/>

                <Map
                    position={this.props.gpsData ? this.props.gpsData.position : undefined}
                    track={this.props.gpsData ? this.props.gpsData.track : undefined}
                    geoJson={this.props.geoJson}
                    showScale={true}
                    onShowDetails={this.showMapPointDetails}
                />
            </div>
        );
    }

    private showMapPointDetails = (details: PointDetails): void => {
        this.setState({
            ...this.state,
            mapPointDetails: details,
        });
    };

    private closeMapPointDetails = (btnClicked: boolean): void => {
        this.setState({
            ...this.state,
            mapPointDetails: undefined,
        });
    };
}

const MapScreen$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        gpsData: getCurrentGPSData(state),
        geoJson: getGeoJson(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({
        loadGpsData: () => dispatch(loadGpsData()),
        subscribeGpsPosition: () => dispatch(subscribeEvent(GPSPositionChangeEventName)),
        unsubscribeGpsPosition: () => dispatch(unsubscribeEvent(GPSPositionChangeEventName)),
        subscribeGpsTrack: () => dispatch(subscribeEvent(GPSTrackChangeEventName)),
        unsubscribeGpsTrack: () => dispatch(unsubscribeEvent(GPSTrackChangeEventName)),
    })
)(MapScreen);

export default MapScreen$$;
