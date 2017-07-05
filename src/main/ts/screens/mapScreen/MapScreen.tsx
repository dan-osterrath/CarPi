import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';

import * as styles from './MapScreen.scss';
import Map from '../../components/map/Map';

import {AppState, getCurrentGPSData} from '../../reducers/reducers';
import GPSData from '../../api/model/GPSData';
import {subscribeEvent, unsubscribeEvent} from '../../actions/actions';
import {EVENT_NAME as GPSPositionChangeEventName} from '../../api/model/GPSPositionChangeEvent';
import {EVENT_NAME as GPSTrackChangeEventName} from '../../api/model/GPSTrackChangeEvent';

interface ContainerDispatchProps {
    subscribeGpsPosition: () => void;
    unsubscribeGpsPosition: () => void;
    subscribeGpsTrack: () => void;
    unsubscribeGpsTrack: () => void;
}

interface ContainerStateProps {
    gpsData?: GPSData;
}

type ContainerOwnProps = ProviderProps;

type MapScreenProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

class MapScreen extends React.Component<MapScreenProps, {}> {

    constructor(props: MapScreenProps) {
        super(props);
    }

    componentDidMount() {
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
                <Map
                    position={this.props.gpsData ? this.props.gpsData.position : undefined}
                    track={this.props.gpsData ? this.props.gpsData.track : undefined}
                />
            </div>
        );
    }
}

const MapScreen$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        gpsData: getCurrentGPSData(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({
        subscribeGpsPosition: () => dispatch(subscribeEvent(GPSPositionChangeEventName)),
        unsubscribeGpsPosition: () => dispatch(unsubscribeEvent(GPSPositionChangeEventName)),
        subscribeGpsTrack: () => dispatch(subscribeEvent(GPSTrackChangeEventName)),
        unsubscribeGpsTrack: () => dispatch(unsubscribeEvent(GPSTrackChangeEventName)),
    })
)(MapScreen);

export default MapScreen$$;
