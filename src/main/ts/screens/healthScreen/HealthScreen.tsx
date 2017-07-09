import * as React from 'react';
import {connect, ProviderProps} from 'react-redux';
import {Paper, Card, CardMedia, List, ListItem, LinearProgress} from 'material-ui';
import IconPower from 'material-ui/svg-icons/notification/power';
import IconStorage from 'material-ui/svg-icons/device/sd-storage';

import * as styles from './HealthScreen.scss';
import HealthStatus from '../../api/model/HealthStatus';
import * as HealthStatusUtils from '../../helpers/HeathStatusUtils';
import {AppState, getCurrentHealthStatus} from '../../reducers/reducers';
import TemperatureIcon from '../../icons/TemperatureIcon';
import CPUIcon from '../../icons/CPUIcon';
import RaspPiIcon from '../../icons/RaspPiIcon';
import MemoryIcon from '../../icons/MemoryIcon';

interface ContainerDispatchProps {
}

interface ContainerStateProps {
    healthStatus?: HealthStatus;
}

type ContainerOwnProps = ProviderProps;

type HealthScreenProps = ContainerOwnProps & ContainerStateProps & ContainerDispatchProps;

class HealthScreen extends React.Component<HealthScreenProps, {}> {

    render() {
        const {
            cpuTemperature = 0,
            gpuTemperature = 0,
            cpuUsage = 0,
            cpuVoltage = 0,
            discFree = 0,
            discTotal = 0,
            memFree = 0,
            memTotal = 0,
            systemLoad = 0,
        } = {...this.props.healthStatus};

        return (
            <div className={styles.healthScreen}>
                <Paper zDepth={1} className={styles.healthPaper}>
                    <Card>
                        <CardMedia>
                            <div>
                                <List>
                                    <ListItem
                                        leftIcon={TemperatureIcon}
                                        secondaryText={`CPU-Temperatur: ${Math.round(cpuTemperature * 10) / 10}°C`}
                                        disabled={true}
                                        className={styles.healthElement}
                                    >
                                        <LinearProgress
                                            mode="determinate"
                                            min={0}
                                            max={80}
                                            value={cpuTemperature}
                                            color={HealthStatusUtils.cpuTemperatureIsOk(cpuTemperature) ? undefined : '#d32f2f'}
                                        />
                                    </ListItem>
                                    <ListItem
                                        leftIcon={TemperatureIcon}
                                        secondaryText={`GPU-Temperatur: ${Math.round(gpuTemperature * 10) / 10}°C`}
                                        disabled={true}
                                        className={styles.healthElement}
                                    >
                                        <LinearProgress
                                            mode="determinate"
                                            min={0}
                                            max={80}
                                            value={gpuTemperature}
                                            color={HealthStatusUtils.gpuTemperatureIsOk(gpuTemperature) ? undefined : '#d32f2f'}
                                        />
                                    </ListItem>
                                    <ListItem
                                        leftIcon={CPUIcon}
                                        secondaryText={`CPU-Auslastung: ${Math.round(cpuUsage)}%`}
                                        disabled={true}
                                        className={styles.healthElement}
                                    >
                                        <LinearProgress
                                            mode="determinate"
                                            min={0}
                                            max={100}
                                            value={cpuUsage}
                                            color={HealthStatusUtils.cpuUsageIsOk(cpuUsage) ? undefined : '#d32f2f'}
                                        />
                                    </ListItem>
                                    <ListItem
                                        leftIcon={RaspPiIcon}
                                        secondaryText={`System-Auslastung: ${Math.round(systemLoad * 10) / 10}`}
                                        disabled={true}
                                        className={styles.healthElement}
                                    >
                                        <LinearProgress
                                            mode="determinate"
                                            min={0}
                                            max={4}
                                            value={systemLoad}
                                            color={HealthStatusUtils.systemLoadIsOk(systemLoad) ? undefined : '#d32f2f'}
                                        />
                                    </ListItem>
                                    <ListItem
                                        leftIcon={MemoryIcon}
                                        secondaryText={`Speicher-Auslastung: ${Math.round((memTotal - memFree) / 1048576)}MB / ${Math.round(memTotal / 1048576)}MB`}
                                        disabled={true}
                                        className={styles.healthElement}
                                    >
                                        <LinearProgress
                                            mode="determinate"
                                            min={0}
                                            max={1}
                                            value={memTotal > 0 ? 1 - (memFree / memTotal) : 0}
                                            color={HealthStatusUtils.memUsageIsOk(memTotal, memFree) ? undefined : '#d32f2f'}
                                        />
                                    </ListItem>
                                    <ListItem
                                        leftIcon={<IconStorage />}
                                        secondaryText={`SD-Karten-Auslastung: ${Math.round((discTotal - discFree) / 1073741824)}GB / ${Math.round(discTotal / 1073741824)}GB`}
                                        disabled={true}
                                        className={styles.healthElement}
                                    >
                                        <LinearProgress
                                            mode="determinate"
                                            min={0}
                                            max={1}
                                            value={discTotal > 0 ? 1 - (discFree / discTotal) : 0}
                                            color={HealthStatusUtils.discUsageIsOk(discTotal, discFree) ? undefined : '#d32f2f'}
                                        />
                                    </ListItem>
                                    <ListItem
                                        leftIcon={<IconPower />}
                                        secondaryText={`CPU-Spannung: ${Math.round(cpuVoltage * 100) / 100}V`}
                                        disabled={true}
                                        className={styles.healthElement}
                                    >
                                        <LinearProgress
                                            mode="determinate"
                                            min={1}
                                            max={2}
                                            value={cpuVoltage}
                                            color={HealthStatusUtils.cpuVoltageIsOk(cpuVoltage) ? undefined : '#d32f2f'}
                                        />
                                    </ListItem>
                                </List>
                            </div>
                        </CardMedia>
                    </Card>
                </Paper>
            </div>
        );
    }
}

const HealthScreen$$ = connect<ContainerStateProps, ContainerDispatchProps, ContainerOwnProps>(
    (state: AppState, ownProps: ContainerOwnProps): ContainerStateProps => ({
        healthStatus: getCurrentHealthStatus(state),
    }),
    (dispatch, ownProps: ContainerOwnProps): ContainerDispatchProps => ({})
)(HealthScreen);

export default HealthScreen$$;
