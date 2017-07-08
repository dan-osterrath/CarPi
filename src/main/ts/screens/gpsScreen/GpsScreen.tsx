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

interface ContainerDispatchProps {
    subscribeGpsPosition: () => void;
    unsubscribeGpsPosition: () => void;
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
        this.props.subscribeGpsPosition();
    }

    componentWillUnmount() {
        this.props.unsubscribeGpsPosition();
    }

    render() {
        const p = this.props.gpsData ? this.props.gpsData.position : undefined;
        const m = this.props.gpsData ? this.props.gpsData.meta : undefined;

        return (
            <div className={styles.gpsScreen}>
                <Paper zDepth={1} className={styles.infoPaper}>
                    <Card>
                        <CardMedia>
                            <div>
                                {p && m ?
                                    <List>
                                        <ListItem
                                            leftIcon={<IconLocationOn />}
                                            primaryText={positionDecimal2Degrees(p.latitude, p.longitude)}
                                            secondaryText={`Position (±${Math.round(p.latitudeError)}m, ±${Math.round(p.longitudeError)}m)`}
                                            disabled={true}
                                        />
                                        <ListItem
                                            leftIcon={<IconTerrain />}
                                            primaryText={`${Math.round(p.altitude)}m`}
                                            secondaryText={`Höhe (±${Math.round(p.altitudeError)}m)`}
                                            disabled={true}
                                        />
                                        <Divider inset={true}/>
                                        <ListItem
                                            leftIcon={SpeedIcon}
                                            primaryText={`${Math.round(p.speed * 3.6)}km/h`}
                                            secondaryText={`Geschwindigkeit (±${Math.round(p.speedError * 3.6)}km/h)`}
                                            disabled={true}
                                        />
                                        <ListItem
                                            leftIcon={ClimbRateIcon}
                                            primaryText={`${Math.round(p.climbRate)}m/s`}
                                            secondaryText={`Steigrate (±${Math.round(p.climbRateError)}m/s)`}
                                            disabled={true}
                                        />
                                        <Divider inset={true}/>
                                        <ListItem
                                            leftIcon={SatelliteIcon}
                                            primaryText={m.numSatellites.toFixed()}
                                            secondaryText="Satelliten"
                                            disabled={true}
                                        />
                                        <ListItem
                                            leftIcon={<IconSchedule />}
                                            primaryText={moment.unix(p.timestamp).format('DD.MM.YYYY HH:mm')}
                                            secondaryText="Zeit"
                                            disabled={true}
                                        />
                                    </List>
                                : null}
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
        subscribeGpsPosition: () => dispatch(subscribeEvent(GPSPositionChangeEventName)),
        unsubscribeGpsPosition: () => dispatch(unsubscribeEvent(GPSPositionChangeEventName)),
    })
)(GpsScreen);

export default GpsScreen$$;
