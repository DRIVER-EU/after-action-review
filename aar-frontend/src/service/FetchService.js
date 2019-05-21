import {store} from '../store';

class FetchService {
  static INSTANCE = new FetchService();

  static getInstance () {
    return FetchService.INSTANCE;
  }

  performPost(url, data) {
    return this.getAxios().post(url, data);
  }

  performGet(url) {
    return this.getAxios().get(url);
  }

  performGetBase64(url) {
    return this.getAxios().get(url, {responseType: 'arraybuffer'}).then(response => Buffer.from(response.data, 'binary').toString('base64'))
  }

  performSimpleDownload(url) {
    const baseUrl = this.getAxios().defaults.baseURL;
    window.open(baseUrl + "/" + url, '_blank');
  }

  getAxios() {
    return store.axios;
  }
}

const fetchService = FetchService.getInstance();

export {FetchService, fetchService};
