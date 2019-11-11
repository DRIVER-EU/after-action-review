import {store} from '../store';

class FetchService {
  static INSTANCE = new FetchService();

  static getInstance () {
    return FetchService.INSTANCE;
  }

  performPost (url, data) {
    // POST does not work correctly with withCredentials, needs to go via "request" instead, see https://github.com/axios/axios/issues/876
    return this.getAxios().request({url: url, method: 'post', withCredentials: true, data: data});
  }

  performGet (url) {
    return this.getAxios().get(url, {withCredentials: true});
  }

  performGetBase64 (url) {
    return this.getAxios().get(url, {
      responseType: 'arraybuffer',
      withCredentials: true
    }).then(response => Buffer.from(response.data, 'binary').toString('base64'));
  }

  performSimpleDownload (url) {
    const baseUrl = this.getAxios().defaults.baseURL;
    window.open(baseUrl + '/' + url, '_blank');
  }

  getAxios () {
    return store.axios;
  }
}

const fetchService = FetchService.getInstance();

export {FetchService, fetchService};
