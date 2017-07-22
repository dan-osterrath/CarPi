import * as React from 'react';
import {Dialog} from 'material-ui';

import * as styles from './MapPointDetails.scss';
import {PointDetails} from '../map/Map';
import {positionDecimal2Degrees} from '../../helpers/GPSUtils';

interface MapPointDetailsProps {
    details?: PointDetails;
    onClose: (buttonClicked: boolean) => void;
}

const MapPointDetails: React.SFC<MapPointDetailsProps> = (props: MapPointDetailsProps) => {
    const content = props.details ? (
            <div>
                <h1>{props.details.name}</h1>
                <h3>{props.details.position ? positionDecimal2Degrees(props.details.position.lat, props.details.position.lng) : null}</h3>
                <p>{props.details.description}</p>
            </div>
        ) : null;
    return (
        <Dialog open={!!props.details} contentClassName={styles.dialog} onRequestClose={props.onClose}>
            {content}
        </Dialog>

    );
};

export default MapPointDetails;