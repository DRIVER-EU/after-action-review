import FilterOption from '../constants/FilterOption';
import {store} from '../store';
import {eventBus} from '../main';
import {fetchService} from './FetchService';
import EventName from '../constants/EventName';

class RecordFilterService {
  static INSTANCE = new RecordFilterService();

  static getInstance () {
    return RecordFilterService.INSTANCE;
  }

  constructor() {
    this.currentFilter = {};
  }

  getCurrentFilter() {
    return this.currentFilter;
  }

  resetFilter() {
    this.updateFilter(null, null, null, null);
  }

  updateFilter(currentlySelectedId, currentlySelectedClientId, currentlySelectedRecordType, currentlySelectedTopicId,
               currentlySelectedMsgType, currentlySelectedRunType, currentlySelectedFromDate, currentlySelectedToDate, currentlySelectedSessionId, currentlySelectedScenarioId) {
    const filter = {
      filterEnabled: true,
      fromDate: currentlySelectedFromDate ? currentlySelectedFromDate.getTime() : null,
      toDate: currentlySelectedToDate ? currentlySelectedToDate.getTime() : null,
      receiverClientId: null,
      id: currentlySelectedId === FilterOption.ALL ? null : currentlySelectedId,
      senderClientId: currentlySelectedClientId === FilterOption.ALL ? null : currentlySelectedClientId,
      recordType: currentlySelectedRecordType === FilterOption.ALL ? null : currentlySelectedRecordType,
      msgType: currentlySelectedMsgType === FilterOption.ALL ? null : currentlySelectedMsgType,
      runType: currentlySelectedRunType === FilterOption.ALL ? null : currentlySelectedRunType,
      topicName: currentlySelectedTopicId === FilterOption.ALL ? null : currentlySelectedTopicId,
      scenarioId: currentlySelectedScenarioId,
      sessionId: currentlySelectedSessionId
    };
    console.log('/setActualFilter invoked with', filter);
    fetchService.performPost('setActualFilter', filter).then(() => {
      this.currentFilter = filter;
      eventBus.$emit(EventName.FILTER_CHANGED);
      store.dispatch('getAllTimelineRecords');
      /**
       fetchService.performGet('getActualFilter').then(response => {
        console.log('/getActualFilter returned', response.data);
      }).catch(ex => console.log(ex));
       **/
    }).catch(ex => console.log(ex));
  }

}

const recordFilter = RecordFilterService.getInstance();

export {RecordFilterService, recordFilter};
