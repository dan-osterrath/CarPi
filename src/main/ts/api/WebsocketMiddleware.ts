import {Dispatch, Middleware, MiddlewareAPI} from 'redux';
import {Action} from 'redux-actions';
import {
    Actions, receivedWebsocket, websocketConnected,
    websocketDisconnected
} from '../actions/actions';
import EventMessage from './model/EventMessage';

const createWebsocketMiddleware = ((url: string): Middleware => {
    let socket: WebSocket|null = null;

    const onOpen = (ws: WebSocket, store: MiddlewareAPI<{}>) => (evt: Event) => store.dispatch(websocketConnected());
    const onClose = (ws: WebSocket, store: MiddlewareAPI<{}>) => (evt: Event) => {
        socket = null;
        store.dispatch(websocketDisconnected());
    };
    const onMessage = (ws: WebSocket, store: MiddlewareAPI<{}>) => (evt: MessageEvent) => {
        try {
            const msg: EventMessage = JSON.parse(evt.data);
            store.dispatch(receivedWebsocket(msg));
        } catch (e) {
            console.warn('Could not decode websocket message', e);
        }
    };
    const onError = (ws: WebSocket, store: MiddlewareAPI<{}>) => (evt: Event) => {
        console.log('Websocket error: ', evt);
    };

    return ((store: MiddlewareAPI<{}>) => (next: Dispatch<{}>) => (action: Action<{}>) => {
        switch (action.type) {
            case Actions.WEBSOCKET_CONNECT:
                if (socket != null) {
                    socket.close();
                    store.dispatch(websocketDisconnected());
                }

                socket = new WebSocket(url);
                socket.onopen = onOpen(socket, store);
                socket.onclose = onClose(socket, store);
                socket.onmessage = onMessage(socket, store);
                socket.onerror = onError(socket, store);

                break;
            case Actions.WEBSOCKET_DISCONNECT:
                if (socket == null) {
                    return;
                }

                socket.close();
                socket = null;

                store.dispatch(websocketDisconnected());
                break;
            case Actions.WEBSOCKET_SEND:
                if (socket == null) {
                    return;
                }

                const message = action.payload;
                if (message) {
                    const json = JSON.stringify(message);
                    socket.send(json);
                }
                break;
            default:
                return next(action);
        }
        return undefined;
    }) as Middleware;
});
const websocketEndpoint = `ws://${window.location.hostname}${window.location.port ? ':' + window.location.port : ''}/events`;

export default createWebsocketMiddleware(websocketEndpoint);