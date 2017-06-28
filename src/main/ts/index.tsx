import 'babel-polyfill';
import * as injectTapEventPlugin from 'react-tap-event-plugin';

import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { MuiThemeProvider } from 'material-ui/styles';
import App from './App';
import './index.css';

injectTapEventPlugin();

ReactDOM.render(
    <MuiThemeProvider>
        <App />
    </MuiThemeProvider>,
    document.getElementById('root')
);
