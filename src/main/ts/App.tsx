import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';
import {Dialog} from 'material-ui';
import IconError from 'material-ui/svg-icons/alert/error';

import * as styles from './App.scss';
import {loadInitialData, subscribeEvent, unsubscribeEvent} from './actions/actions';
import {AppState as GlobalAppState, getMapConfig, isWebSocketConnected} from './reducers/reducers';
import MapConfiguration from './api/model/MapConfiguration';
import MainNavigation from './components/mainNavigation/MainNavigation';
import Dashboard from './screens/dashboard/Dashboard';
import MapScreen from './screens/mapScreen/MapScreen';
import GpsScreen from './screens/gpsScreen/GpsScreen';
import Obd2Screen from './screens/obd2Screen/Obd2Screen';
import HealthScreen from './screens/healthScreen/HealthScreen';
import {EVENT_NAME as GPSMetaInfoChangeEventName} from './api/model/GPSMetaInfoChangeEvent';
import {EVENT_NAME as HealthStatusChangeEventName} from './api/model/HealthStatusChangeEvent';

interface ContainerDispatchProps {
    loadInitialData: () => void;
    subscribeGpsMeta: () => void;
    unsubscribeGpsMeta: () => void;
    subscribeHealthStatus: () => void;
    unsubscribeHealthStatus: () => void;
}

interface ContainerStateProps {
    websocketConnected: boolean;
    mapConfig?: MapConfiguration;
}

type ContainerOwnProps = ProviderProps;

type AppProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

interface AppState {
    selectedMainTab: number;
}

class App extends React.Component<AppProps, AppState> {
    connectInterval?: number;

    constructor(props: AppProps) {
        super(props);
        this.state = {
            selectedMainTab: 0,
        };
    }

    componentDidMount() {
        this.props.loadInitialData();
        this.connectInterval = setInterval(this.props.loadInitialData, 2000);
    }

    componentWillUnmount() {
        this.props.unsubscribeGpsMeta();
        this.props.unsubscribeHealthStatus();
    }

    componentWillReceiveProps(newProps: AppProps) {
        if (newProps.websocketConnected && !this.props.websocketConnected) {
            if (this.connectInterval !== undefined) {
                clearInterval(this.connectInterval);
                this.connectInterval = undefined;
            }
            this.props.subscribeGpsMeta();
            this.props.subscribeHealthStatus();
        } else if (!newProps.websocketConnected && this.props.websocketConnected) {
            if (this.connectInterval !== undefined) {
                clearInterval(this.connectInterval);
            }
            this.connectInterval = setInterval(this.props.loadInitialData, 2000);
        }
    }

    render() {
        let screen;
        if (this.props.websocketConnected && this.props.mapConfig) {
            switch (this.state.selectedMainTab) {
                case 0:
                    screen = <Dashboard />;
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
        } else {
            screen = null;
        }

        return (
            <div className={styles.app}>
                <Dialog open={!this.props.websocketConnected || this.props.mapConfig === undefined} contentClassName={styles.noConnection}>
                    <IconError className={styles.errorIcon}/>
                    <div className={styles.errorMessage}>
                        <h3>Keine Verbindung!</h3>
                    </div>
                </Dialog>

                <div className={styles.contentContainer}>
                    {screen}
                </div>
                <MainNavigation
                    selectedTab={this.state.selectedMainTab}
                    onTabChanged={(tab) => this.onMainNavigationTagChanged(tab)}
                />
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
    (state: GlobalAppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        websocketConnected: isWebSocketConnected(state),
        mapConfig: getMapConfig(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({
        loadInitialData: () => dispatch(loadInitialData()),
        subscribeGpsMeta: () => dispatch(subscribeEvent(GPSMetaInfoChangeEventName)),
        unsubscribeGpsMeta: () => dispatch(unsubscribeEvent(GPSMetaInfoChangeEventName)),
        subscribeHealthStatus: () => dispatch(subscribeEvent(HealthStatusChangeEventName)),
        unsubscribeHealthStatus: () => dispatch(unsubscribeEvent(HealthStatusChangeEventName)),
    })
)(App);

export default App$$;
