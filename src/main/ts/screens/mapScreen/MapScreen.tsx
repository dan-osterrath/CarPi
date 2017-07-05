import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';

import * as styles from './MapScreen.scss';
import Map from '../../components/map/Map';

import {AppState, getCurrentGPSPosition} from '../../reducers/reducers';
import GPSPosition from '../../api/model/GPSPosition';
import {subscribeEvent, unsubscribeEvent} from '../../actions/actions';
import {EVENT_NAME as GPSPositionChangeEventName} from '../../api/model/GPSPositionChangeEvent';

interface ContainerDispatchProps {
    subscribeGpsData: () => void;
    unsubscribeGpsData: () => void;
}

interface ContainerStateProps {
    gpsPosition?: GPSPosition;
}

type ContainerOwnProps = ProviderProps;

type MapScreenProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

class MapScreen extends React.Component<MapScreenProps, {}> {

    constructor(props: MapScreenProps) {
        super(props);
    }

    componentDidMount() {
        this.props.subscribeGpsData();
    }

    componentWillUnmount() {
        this.props.unsubscribeGpsData();
    }

    render() {
        return (
            <div className={styles.mapScreen}>
                <Map position={this.props.gpsPosition}/>
            </div>
        );
    }
}

const MapScreen$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        gpsPosition: getCurrentGPSPosition(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({
        subscribeGpsData: () => dispatch(subscribeEvent(GPSPositionChangeEventName)),
        unsubscribeGpsData: () => dispatch(unsubscribeEvent(GPSPositionChangeEventName)),
    })
)(MapScreen);

export default MapScreen$$;
