function getHost() {
  const host = window.location.host;
  if (host === "localhost:8080") {
    return "localhost:8095";
  } else {
    return host;
  }
}

export default {
  HTTP_BASE: 'http://' + getHost() + '/AARService',
  WEBSOCKET: 'ws://'+ getHost() + '/AARServiceWSEndpoint',
  SOCKJS: 'http://'+ getHost() + '/AARServiceWSEndpoint',
  ICON_SERVICE: 'http://tb1.driver-testbed.eu:8910/TBIconService/getIcon?size=24x24&path='
}
