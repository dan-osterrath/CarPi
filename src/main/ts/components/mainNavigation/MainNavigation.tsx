import * as React from 'react';
import * as styles from './MainNavigation.scss';

import {BottomNavigation, BottomNavigationItem} from 'material-ui';
import IconLocationOn from 'material-ui/svg-icons/communication/location-on';
const nearbyIcon = <IconLocationOn />;

interface MainNavigationState {
    selectedTab: number;
}

class MainNavigation extends React.Component<{}, MainNavigationState> {
    constructor() {
        super();
        this.state = {
            selectedTab: 0
        };
    }

    render() {
        return (
            <div className={styles.mainNavigation}>
                <div className={styles.contentContainer} />
                <BottomNavigation selectedIndex={this.state.selectedTab} >
                    <BottomNavigationItem label="Karte" icon={nearbyIcon} onClick={() => this.select(0)} />
                    <BottomNavigationItem label="GPS" icon={nearbyIcon} onClick={() => this.select(1)} />
                    <BottomNavigationItem label="Fahrzeug" icon={nearbyIcon} onClick={() => this.select(2)} />
                    <BottomNavigationItem label="CarPi" icon={nearbyIcon} onClick={() => this.select(3)} />
                </BottomNavigation>
            </div>
        );
    }

    private select(tab: number) {
        this.setState({
            ...this.state,
            selectedTab: tab,
        });
    }
}

export default MainNavigation;