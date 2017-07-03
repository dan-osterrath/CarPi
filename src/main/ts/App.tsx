import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';

import * as styles from './App.scss';
import {loadInitialData} from './actions/actions';
import MainNavigation from './components/mainNavigation/MainNavigation';
import Dashboard from './screens/dashboard/Dashboard';
import MapScreen from './screens/mapScreen/MapScreen';
import GpsScreen from './screens/gpsScreen/GpsScreen';
import Obd2Screen from './screens/obd2Screen/Obd2Screen';
import HealthScreen from './screens/healthScreen/HealthScreen';

interface ContainerDispatchProps {
    loadInitialData: () => void;
}

interface ContainerStateProps {
}

type ContainerOwnProps = ProviderProps;

type AppProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

interface AppState {
    selectedMainTab: number;
}

class App extends React.Component<AppProps, AppState> {
    constructor(props: AppProps) {
        super(props);
        this.state = {
            selectedMainTab: 0
        };
    }

    componentDidMount() {
        this.props.loadInitialData();
    }

    render() {
        let screen;
        switch (this.state.selectedMainTab) {
            case 0:
                screen = <Dashboard />;
                screen = <GpsScreen />;
                break;
            case 1:
                screen = <MapScreen />;
                break;
            case 2:
                screen = <GpsScreen />;
                break;
            case 3:
                screen = <Obd2Screen />;
                break;
            case 4:
                screen = <HealthScreen />;
                break;
            default:
                screen = null;
        }

        return (
            <div className={styles.app}>
                <div className={styles.contentContainer}>
                    {screen}
                </div>
                <MainNavigation selectedTab={this.state.selectedMainTab} onTabChanged={(tab) => this.onMainNavigationTagChanged(tab)}/>
            </div>
        );
    }

    private onMainNavigationTagChanged(tab: number) {
        this.setState({
            ...this.state,
            selectedMainTab: tab,
        });
    }
}

const App$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({

    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({
        loadInitialData: () => dispatch(loadInitialData())
    })
)(App);

export default App$$;
