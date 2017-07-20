import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';
import {Paper, Card, CardMedia, List, ListItem, Divider} from 'material-ui';
import IconTerrain from 'material-ui/svg-icons/maps/terrain';
import IconSchedule from 'material-ui/svg-icons/action/schedule';
import * as moment from 'moment';

import * as styles from './Dashboard.scss';
import SpeedIcon from '../../icons/SpeedIcon';
import DistanceIcon from '../../icons/DistanceIcon';
import GasIcon from '../../icons/GasIcon';
import Map, {PointDetails} from '../../components/map/Map';
import MapPointDetails from '../../components/mapPointDetails/MapPointDetails';

import GPSData from '../../api/model/GPSData';
import {AppState, getCurrentGPSData, getGeoJson} from '../../reducers/reducers';
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

type DashboardProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

interface DashboardState {
    mapPointDetails?: PointDetails;
}

class Dashboard extends React.Component<DashboardProps, DashboardState> {

    constructor(props: DashboardProps) {
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
        let speed = 0;
        let distance = 0;
        let altitude = 0;
        let time = moment();
        let gas = 0;
        let reach = 0;
        if (this.props.gpsData) {
            const p = this.props.gpsData.position;
            if (p) {
                speed = p.speed;
                altitude = p.altitude;
                time = moment.unix(p.timestamp);
            }
            const t = this.props.gpsData.track;
            if (t) {
                distance = t.distance;
            }
        }

        return (
            <div className={styles.dashboard}>
                <MapPointDetails details={this.state.mapPointDetails} onClose={this.closeMapPointDetails}/>

                <Paper zDepth={1} className={styles.infoPaper}>
                    <Card>
                        <CardMedia>
                            <div>
                                <List>
                                    <ListItem
                                        leftIcon={SpeedIcon}
                                        primaryText={`${Math.round(speed * 3.6)}km/h`}
                                        disabled={true}
                                        className={styles.speed}
                                    />
                                    <Divider/>
                                    <ListItem
                                        leftIcon={DistanceIcon}
                                        primaryText={`${Math.round(distance / 100) / 10}km`}
                                        secondaryText="Strecke"
                                        disabled={true}
                                    />
                                    <ListItem
                                        leftIcon={GasIcon}
                                        primaryText={`${Math.round(gas)}l`}
                                        secondaryText="Tankinhalt"
                                        disabled={true}
                                    />
                                    <ListItem
                                        insetChildren={true}
                                        primaryText={`${Math.round(reach)}km`}
                                        secondaryText="Reichweite"
                                        disabled={true}
                                    />
                                    <Divider/>
                                    <ListItem
                                        leftIcon={<IconTerrain />}
                                        primaryText={`${Math.round(altitude)}m`}
                                        secondaryText={`Höhe`}
                                        disabled={true}
                                    />
                                    <ListItem
                                        leftIcon={<IconSchedule />}
                                        primaryText={time.format('DD.MM.YYYY HH:mm')}
                                        secondaryText="Zeit"
                                        disabled={true}
                                    />
                                </List>
                            </div>
                        </CardMedia>
                    </Card>
                </Paper>

                <Paper zDepth={1} className={styles.mapPaper}>
                    <Map
                        position={this.props.gpsData ? this.props.gpsData.position : undefined}
                        track={this.props.gpsData ? this.props.gpsData.track : undefined}
                        geoJson={this.props.geoJson}
                        showScale={true}
                        onShowDetails={this.showMapPointDetails}
                    />
                </Paper>
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

const Dashboard$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
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
)(Dashboard);

export default Dashboard$$;
