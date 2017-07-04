import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';
import IconLocationOn from 'material-ui/svg-icons/communication/location-on';
import IconTerrain from 'material-ui/svg-icons/maps/terrain';
import IconSchedule from 'material-ui/svg-icons/action/schedule';
import {Paper, Card, CardMedia, List, ListItem, Divider} from 'material-ui';
import * as moment from 'moment';

import * as styles from './GpsScreen.scss';
import SpeedIcon from '../../icons/SpeedIcon';
import ClimbRateIcon from '../../icons/ClimbRateIcon';
import SatelliteIcon from '../../icons/SatelliteIcon';
import Map from '../../components/map/Map';

import {AppState, getCurrentGPSData} from '../../reducers/reducers';
import GPSData from '../../api/model/GPSData';
import {positionDecimal2Degrees} from '../../helpers/GPSUtils';
import {subscribeEvent, unsubscribeEvent} from '../../actions/actions';
import {EVENT_NAME as GPSPositionChangeEventName} from '../../api/model/GPSPositionChangeEvent';
import {EVENT_NAME as GPSMetaInfoChangeEventName} from '../../api/model/GPSMetaInfoChangeEvent';

interface ContainerDispatchProps {
    subscribeGpsData: () => void;
    unsubscribeGpsData: () => void;
    subscribeGpsMeta: () => void;
    unsubscribeGpsMeta: () => void;
}

interface ContainerStateProps {
    gpsData?: GPSData;
}

type ContainerOwnProps = ProviderProps;

type GpsScreenProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

class GpsScreen extends React.Component<GpsScreenProps, {}> {

    constructor(props: GpsScreenProps) {
        super(props);
    }

    componentDidMount() {
        this.props.subscribeGpsData();
        this.props.subscribeGpsMeta();
    }

    componentWillUnmount() {
        this.props.unsubscribeGpsData();
        this.props.unsubscribeGpsMeta();
    }

    render() {
        let position = '';
        let positionError = '';
        let altitude = '';
        let altitudeError = '';
        let speed = '';
        let speedError = '';
        let climbRate = '';
        let climbRateError = '';
        let time = '';
        let satellites = '';
        if (this.props.gpsData) {
            const p = this.props.gpsData.position;
            if (p) {
                position = positionDecimal2Degrees(p.latitude, p.longitude);
                positionError = `(±${Math.round(p.latitudeError)}m, ±${Math.round(p.longitudeError)}m)`;
                altitude = `${Math.round(p.altitude)}m`;
                altitudeError = `(±${Math.round(p.altitudeError)}m)`;
                speed = `${Math.round(p.speed * 3.6)}km/h`;
                speedError = `(±${Math.round(p.speedError * 3.6)}km/h)`;
                climbRate = `${Math.round(p.climbRate)}m/s`;
                climbRateError = `(±${Math.round(p.climbRateError)}m/s)`;
                time = moment.unix(p.timestamp).format('DD.MM.YYYY HH:mm');
            }
            const m = this.props.gpsData.meta;
            if (m) {
                satellites = Math.round(m.numSatellites).toString(10);
            }
        }
        return (
            <div className={styles.gpsScreen}>
                <Paper zDepth={1} className={styles.infoPaper}>
                    <Card>
                        <CardMedia>
                            <div>
                                <List>
                                    <ListItem
                                        leftIcon={<IconLocationOn />}
                                        primaryText={position}
                                        secondaryText={`Position ${positionError}`}
                                        disabled={true}
                                    />
                                    <ListItem
                                        leftIcon={<IconTerrain />}
                                        primaryText={altitude}
                                        secondaryText={`Höhe ${altitudeError}`}
                                        disabled={true}
                                    />
                                    <Divider inset={true}/>
                                    <ListItem
                                        leftIcon={SpeedIcon}
                                        primaryText={speed}
                                        secondaryText={`Geschwindigkeit ${speedError}`}
                                        disabled={true}
                                    />
                                    <ListItem
                                        leftIcon={ClimbRateIcon}
                                        primaryText={climbRate}
                                        secondaryText={`Steigrate ${climbRateError}`}
                                        disabled={true}
                                    />
                                    <Divider inset={true}/>
                                    <ListItem
                                        leftIcon={SatelliteIcon}
                                        primaryText={satellites}
                                        secondaryText="Satelliten"
                                        disabled={true}
                                    />
                                    <ListItem
                                        leftIcon={<IconSchedule />}
                                        primaryText={time}
                                        secondaryText="Zeit"
                                        disabled={true}
                                    />
                                </List>
                            </div>
                        </CardMedia>
                    </Card>
                </Paper>

                <Paper zDepth={1} className={styles.mapPaper}>
                    <Map disableZoom={true} position={this.props.gpsData ? this.props.gpsData.position : undefined}/>
                </Paper>
            </div>
        );
    }
}

const GpsScreen$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        gpsData: getCurrentGPSData(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({
        subscribeGpsData: () => dispatch(subscribeEvent(GPSPositionChangeEventName)),
        unsubscribeGpsData: () => dispatch(unsubscribeEvent(GPSPositionChangeEventName)),
        subscribeGpsMeta: () => dispatch(subscribeEvent(GPSMetaInfoChangeEventName)),
        unsubscribeGpsMeta: () => dispatch(unsubscribeEvent(GPSMetaInfoChangeEventName)),
    })
)(GpsScreen);

export default GpsScreen$$;
