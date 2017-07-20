import * as React from 'react';
import {Dialog} from 'material-ui';

import * as styles from './MapPointDetails.scss';
import {PointDetails} from '../map/Map';

interface MapPointDetailsProps {
    details?: PointDetails;
    onClose: (buttonClicked: boolean) => void;
}

const MapPointDetails: React.SFC<MapPointDetailsProps> = (props: MapPointDetailsProps) => {
    const content = props.details ? (
            <div>
                <h1>{props.details.name}</h1>
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