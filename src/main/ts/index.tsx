import 'babel-polyfill';
import * as injectTapEventPlugin from 'react-tap-event-plugin';

import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {Provider, Store} from 'react-redux';
import {createStore, applyMiddleware} from 'redux';
import {createLogger} from 'redux-logger';
import thunkMiddleware from 'redux-thunk';
import {MuiThemeProvider} from 'material-ui/styles';
import App from './App';
import {default as reducers, AppState} from './reducers/reducers';
import './index.css';
import websocketMiddleware from './api/WebsocketMiddleware';

injectTapEventPlugin();

const store: Store<AppState> = createStore(
    reducers,
    applyMiddleware(thunkMiddleware, websocketMiddleware, createLogger())
);

ReactDOM.render(
    <Provider store={store}>
        <MuiThemeProvider>
            <App />
        </MuiThemeProvider>
    </Provider>,
    document.getElementById('root')
);
