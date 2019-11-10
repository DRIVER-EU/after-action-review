import SockJS from 'sockjs-client'
import Urls from '../constants/Urls'
import {store} from '../store'

class WebSocketConnection {
  static INSTANCE = new WebSocketConnection();

  static RECONNECT_DELAY_MILLISECONDS = 2000;

  reconnectInterval = null;

  socket = null;

  firstConnection = true;

  static getInstance () {
    return WebSocketConnection.INSTANCE
  }

  connect () {
    const me = this;
    console.log('Connecting to web socket...');

    me.socket = new SockJS(Urls.SOCKJS);

    me.socket.onopen = function () {
      if (me.firstConnection) {
        store.commit('SOCKET_ONOPEN');
        me.firstConnection = false;
      } else {
        store.commit('SOCKET_RECONNECT')
      }
      if (me.reconnectInterval) {
        window.clearInterval(me.reconnectInterval);
        me.reconnectInterval = null
      }
    };

    me.socket.onmessage = function (e) {
      // console.log('Received message: ', e.data)
      const msg = JSON.parse(e.data);
      me.handleMessage(msg)
    };

    me.socket.onclose = function () {
      store.commit('SOCKET_ONCLOSE');
      if (!me.reconnectInterval) {
        me.reconnectInterval = window.setInterval(me.connect.bind(me), WebSocketConnection.RECONNECT_DELAY_MILLISECONDS);
        console.log("Started reconnect timer", me.reconnectInterval);
      }
    }
  }

  send (message) {
    const me = this;
    if (me.socket) {
      // console.log('Sending', message)
      me.socket.send(message)
    } else {
      console.log('Could not send message as socket not open', message)
    }
  }

  close() {
    const me = this;
    if (me.socket) {
      console.log('Closing socket');
      me.socket.close()
    } else {
      console.log('Could not close socket as socket not open')
    }
  }

  handleMessage (msg) {
    let method = 'commit';
    let target = 'SOCKET_ONMESSAGE';
    if (msg.mutation) {
      target = [msg.namespace || '', msg.mutation].filter((e) => !!e).join('/')
    } else if (msg.action) {
      method = 'dispatch';
      target = [msg.namespace || '', msg.action].filter((e) => !!e).join('/')
    }
    // console.log('Store', method, target, msg)
    store[method](target, msg)
  }
}

const webSocketConnection = WebSocketConnection.getInstance();

export {webSocketConnection}
