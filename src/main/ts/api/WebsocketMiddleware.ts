import {Action, Dispatch, Middleware, MiddlewareAPI} from 'redux';
import {
    Actions, receiveGPSMetaInfo, receiveGPSPosition, websocketConnected,
    websocketDisconnected
} from '../actions/actions';
import EventMessage from './model/EventMessage';
import GPSPositionChangeEvent from './model/GPSPositionChangeEvent';
import GPSMetaInfoChangeEvent from './model/GPSMetaInfoChangeEvent';

const createWebsocketMiddleware = ((url: string): Middleware => {
    let socket: WebSocket|null = null;

    const onOpen = (ws: WebSocket, store: MiddlewareAPI<{}>) => (evt: Event) => store.dispatch(websocketConnected());
    const onClose = (ws: WebSocket, store: MiddlewareAPI<{}>) => (evt: Event) => store.dispatch(websocketDisconnected());
    const onMessage = (ws: WebSocket, store: MiddlewareAPI<{}>) => (evt: MessageEvent) => {
        try {
            const msg: EventMessage = JSON.parse(evt.data);
            switch (msg.type) {
                case 'GPSPositionChangeEvent':
                    const e1: GPSPositionChangeEvent = msg.event as GPSPositionChangeEvent;
                    store.dispatch(receiveGPSPosition(e1.location));
                    break;
                case 'GPSMetaInfoChangeEvent':
                    const e2: GPSMetaInfoChangeEvent = msg.event as GPSMetaInfoChangeEvent;
                    store.dispatch(receiveGPSMetaInfo(e2.metaInfo));
                    break;
                default:
                    console.log('Received unknown message type ' + msg.type);
            }
        } catch (e) {
            console.warn('Could not decode websocket message', e);
        }
    };
    const onError = (ws: WebSocket, store: MiddlewareAPI<{}>) => (evt: Event) => {
        console.log('Websocket error: ', evt);
    };

    return ((store: MiddlewareAPI<{}>) => (next: Dispatch<{}>) => (action: Action) => {
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
            default:
                return next(action);
        }
        return undefined;
    }) as Middleware;
});

export default createWebsocketMiddleware('ws://localhost:3000/events');