import * as React from 'react';
import * as styles from './MainNavigation.scss';

import {BottomNavigation, BottomNavigationItem, Paper} from 'material-ui';
import IconDashboard from 'material-ui/svg-icons/action/dashboard';
import IconMap from 'material-ui/svg-icons/maps/map';
import IconLocationOn from 'material-ui/svg-icons/communication/location-on';
import IconTablet from 'material-ui/svg-icons/hardware/tablet';
import IconCar from 'material-ui/svg-icons/maps/directions-car';

interface MainNavigationProps {
    selectedTab: number;
    onTabChanged: (tab: number) => void;
}

class MainNavigation extends React.Component<MainNavigationProps, {}> {
    constructor() {
        super();
    }

    render() {
        return (
            <Paper zDepth={1} className={styles.mainNavigation}>
                <BottomNavigation selectedIndex={this.props.selectedTab} >
                    <BottomNavigationItem label="Dashboard" icon={<IconDashboard />} onClick={() => this.select(0)} />
                    <BottomNavigationItem label="Karte" icon={<IconMap />} onClick={() => this.select(1)} />
                    <BottomNavigationItem label="GPS" icon={<IconLocationOn />} onClick={() => this.select(2)} />
                    <BottomNavigationItem label="Fahrzeug" icon={<IconCar />} onClick={() => this.select(3)} />
                    <BottomNavigationItem label="CarPi" icon={<IconTablet />} onClick={() => this.select(4)} />
                </BottomNavigation>
            </Paper>
        );
    }

    private select(tab: number) {
        this.props.onTabChanged(tab);
    }
}

export default MainNavigation;