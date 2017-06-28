import * as React from 'react';
import * as styles from './App.css';
import MainNavigation from './components/mainNavigation/MainNavigation';

class App extends React.Component<{}, {}> {
    render() {
        return (
            <div className={styles.app}>
                <MainNavigation />
            </div>
        );
    }
}

export default App;
