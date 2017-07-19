import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';
import * as styles from './MainNavigation.scss';

import {BottomNavigation, BottomNavigationItem, Paper} from 'material-ui';
import IconDashboard from 'material-ui/svg-icons/action/dashboard';
import IconMap from 'material-ui/svg-icons/maps/map';
import IconLocationOn from 'material-ui/svg-icons/communication/location-on';
import IconTablet from 'material-ui/svg-icons/hardware/tablet';
import IconCar from 'material-ui/svg-icons/maps/directions-car';

import GPSMetaInfo from '../../api/model/GPSMetaInfo';
import {AppState, getCurrentGPSMetaInfo, isHealthStatusOk} from '../../reducers/reducers';

interface MainNavigationComponentProps {
    selectedTab: number;
    onTabChanged: (tab: number) => void;
}

interface ContainerDispatchProps {
}

interface ContainerStateProps {
    gpsMetaInfo?: GPSMetaInfo;
    healthStatusOk: boolean;
}

type ContainerOwnProps = MainNavigationComponentProps & ProviderProps;

type MainNavigationProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

class MainNavigation extends React.Component<MainNavigationProps, {}> {
    constructor(props: MainNavigationProps) {
        super(props);
    }

    render() {
        return (
            <Paper zDepth={1} className={styles.mainNavigation}>
                <BottomNavigation selectedIndex={this.props.selectedTab}>
                    <BottomNavigationItem label="Dashboard" icon={<IconDashboard />} onClick={() => this.select(0)}/>
                    <BottomNavigationItem label="Karte" icon={<IconMap />} onClick={() => this.select(1)}/>
                    <BottomNavigationItem label="GPS" icon={<IconLocationOn />} onClick={() => this.select(2)} className={(!this.props.gpsMetaInfo || !this.props.gpsMetaInfo.numSatellites) ? styles.noGPS : ''} />
                    <BottomNavigationItem label="Fahrzeug" icon={<IconCar />} onClick={() => null} />
                    <BottomNavigationItem label="CarPi" icon={<IconTablet />} onClick={() => this.select(4)} className={this.props.healthStatusOk ? '' : styles.healthWarning} />
                </BottomNavigation>
            </Paper>
        );
    }

    private select(tab: number) {
        this.props.onTabChanged(tab);
    }
}

const MainNavigation$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        gpsMetaInfo: getCurrentGPSMetaInfo(state),
        healthStatusOk: isHealthStatusOk(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({})
)(MainNavigation);

export default MainNavigation$$;
