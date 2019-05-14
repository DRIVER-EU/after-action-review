import FilterOption from '../constants/FilterOption';
import {store} from '../store';

class RecordFilterService {
  static INSTANCE = new RecordFilterService();

  static getInstance () {
    return RecordFilterService.INSTANCE;
  }

  resetFilter() {
    this.updateFilter(null, null, null, null);
  }

  updateFilter(currentlySelectedId, currentlySelectedClientId, currentlySelectedRecordType, currentlySelectedTopicId) {
    const filter = {
      filterEnabled: true,
      fromDate: null,
      toDate: null,
      receiverClientId: null,
      id: currentlySelectedId === FilterOption.ALL ? null : currentlySelectedId,
      senderClientId: currentlySelectedClientId === FilterOption.ALL ? null : currentlySelectedClientId,
      recordType: currentlySelectedRecordType === FilterOption.ALL ? null : currentlySelectedRecordType,
      topicName: currentlySelectedTopicId === FilterOption.ALL ? null : currentlySelectedTopicId,
    };
    console.log('/setActualFilter invoked with', filter);
    store.axios.post('setActualFilter', filter).then(response => {
      // console.log('Filter has been set', response.data);
      store.dispatch('getRecords');
      store.dispatch('getAllTimelineRecords');
      /**
      store.axios.get('getActualFilter').then(response => {
        console.log('/getActualFilter returned', response.data);
      }).catch(ex => console.log(ex));
       **/
    }).catch(ex => console.log(ex));
  }

}

const recordFilter = RecordFilterService.getInstance();

export {RecordFilterService, recordFilter};
