import Vue from 'vue';
import Vuex from 'vuex';
import {Record} from '../objects/record';
import {heartbeatController} from '../heartbeatController';
import {formatDate} from '../helper';
import {environment} from '../service/EnvironmentService';
import EventName from '../constants/EventName';
import FilterOption from '../constants/FilterOption';
import Settings from '../constants/Settings';
import RecordType from '../constants/RecordType';
import LogLevel from '../constants/LogLevel';

Vue.use(Vuex);

function createRecord(record) {
  let newRecord = new Record(record);
  newRecord.createDate = formatDate(record.createDate, 'YYYY-MM-DD');
  newRecord.createTime = formatDate(record.createDate, 'HH:mm:ss.SSS');
  newRecord.recordData = JSON.parse(record.recordJson);
  if (newRecord.recordJson && newRecord.recordJson.hasOwnProperty('dateTimeSent')) {
    newRecord.recordJson.dateTimeSent = formatDate(newRecord.recordJson.dateTimeSent);
  }
  return newRecord;
}

function isErrorLog(record) {
  if (record.recordType === RecordType.LOG) {
    return record.recordData && record.recordData.level === LogLevel.ERROR;
  } else {
    return false;
  }
}

export const store = new Vuex.Store({
  state: {
    socket: {
      isConnected: false,
      message: '',
      reconnectError: false,
      pingTimer: null,
      pingTimeOutTimer: null,
      messageAccepted: false
    },
    records: [],
    recordsPageCount: 1,
    timelineRecords: [],
    record: null,
    trial: null,
    filterOptions: {
      id: [FilterOption.ALL],
      clientId: [FilterOption.ALL],
      topic: [FilterOption.ALL],
      msgType: [FilterOption.ALL],
      recordType: [FilterOption.ALL]
    }
  },
  getters: {
    records (state) {
      return state.records.sort((a, b) => {
        return b.id - a.id;
      });
    },
    filterOptions (state) {
      return state.filterOptions;
    }
  }
  ,
  mutations: {
    SOCKET_ONOPEN (state) {
      console.log('Connection opened');
      state.socket.isConnected = true;
      heartbeatController();
    },
    SOCKET_ONCLOSE (state) {
      console.log('Connection closed');
      state.socket.isConnected = false;
      clearInterval(state.socket.pingTimer);
      clearInterval(state.socket.pingTimeOutTimer);
    },
    SOCKET_RECONNECT () {
      console.log('Reconnect');
    },
    SOCKET_ONERROR () {
    },
    HBRESPONSE (state) {
      // console.log("Received heartbeat response");
      state.socket.messageAccepted = true;
    },
    RECORD_NOTIFICATION (state, record) {
      console.log("Received record notification", record);
      let newRecord = createRecord(record);
      state.timelineRecords.push(record);
      this.eventBus.$emit(EventName.RECORD_NOTIFICATION, newRecord);
      if (isErrorLog(newRecord)) {
        this.eventBus.$emit(EventName.LOG_ERROR_RECEIVED);
      }
    },
    GET_RECORDS (state, records) {
      state.records = [];
      records.forEach(function (record) {
        const newRecord = createRecord(record);
        state.records.push(newRecord);
      });
    },
    GET_RECORDS_PAGE_COUNT (state, count) {
      // console.log("Received records page count", count);
      state.recordsPageCount = count;
    },
    GET_RECORD_TYPES (state, recordTypes) {
      recordTypes.forEach(recordType => {
        if (state.filterOptions.recordType.indexOf(recordType) === -1) {
          state.filterOptions.recordType.push(recordType)
        }
      });
    },
    GET_MSG_TYPES (state, msgTypes) {
      msgTypes.forEach(msgType => {
        if (state.filterOptions.msgType.indexOf(msgType) === -1) {
          state.filterOptions.msgType.push(msgType)
        }
      });
    },
    GET_TOPIC_NAMES (state, topics) {
      topics.forEach(topic => {
        if (state.filterOptions.topic.indexOf(topic) === -1) {
          state.filterOptions.topic.push(topic)
        }
      });
    },
    GET_CLIENT_IDS (state, clientIds) {
      clientIds.forEach(clientId => {
        if (state.filterOptions.clientId.indexOf(clientId) === -1) {
          state.filterOptions.clientId.push(clientId)
        }
      });
    },
    GET_RECORD (state, record) {
      state.record = createRecord(record);
    },
    GET_TIMELINE_RECORDS (state, records) {
      state.timelineRecords = records;
    },
    GET_ACTUAL_TRIAL (state, trial) {
      state.trial = trial;
    }
  },
  actions: {
    getFilterOptions(context) {
      this.axios.get('getRecordTypes').then(response => {
        context.commit('GET_RECORD_TYPES', (response.data));
      }).catch(ex => console.log(ex));
      this.axios.get('getTopicNames').then(response => {
        context.commit('GET_TOPIC_NAMES', (response.data));
      }).catch(ex => console.log(ex));
      this.axios.get('getSenderClientIds').then(response => {
        context.commit('GET_CLIENT_IDS', (response.data));
      }).catch(ex => console.log(ex));
      this.axios.get('getReceiverClientIds').then(response => {
        context.commit('GET_CLIENT_IDS', (response.data));
      }).catch(ex => console.log(ex));
      context.commit('GET_MSG_TYPES', ["Info", "Warn", "Error", "Ack"]);
    },
    getRecord (context, payload) {
      this.axios.get('getRecord/' + payload.id).then(response => {
        context.commit('GET_RECORD', (response.data));
      }).catch(ex => console.log(ex));
    },
    getPageCount (context) {
      this.axios.get('getPageCount').then(response => {
        context.commit('GET_RECORDS_PAGE_COUNT', (response.data));
      }).catch(ex => console.log(ex));
    },
    getRecords (context, payload) {
      const page = payload ? payload.page : null;
      const url = page ? 'getAllRecords?size=' + Settings.PAGE_SIZE + "&page=" + page : 'getAllRecords';
      this.axios.get(url).then(response => {
        console.log('/getAllRecords returned count', response.data.length);
        context.commit('GET_RECORDS', (response.data));
      }).catch(ex => console.log(ex));
    },
    getAllTimelineRecords (context) {
      this.axios.get('getAllTimelineRecords').then(response => {
        let list = response.data;
        if (environment.isLocalDevelopment()) {
          list = list.slice(0, Math.min(list.length - 1, 300)); // FIXME: limits number of timeline items to 300
        }
        context.commit('GET_TIMELINE_RECORDS', list);
      }).catch(ex => console.log(ex));
    },
    getActualTrial (context) {
      this.axios.get('getActualTrial').then(response => {
        context.commit('GET_ACTUAL_TRIAL', (response.data));
      }).catch(ex => console.log(ex));
    }
  }
});
