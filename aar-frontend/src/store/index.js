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
import {fetchService} from '../service/FetchService';

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
      runType: [FilterOption.ALL],
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
      console.log('Web socket connection opened.');
      state.socket.isConnected = true;
      heartbeatController()
    },
    SOCKET_ONCLOSE (state) {
      console.log('Web socket connection closed.');
      state.socket.isConnected = false;
      if (state.socket.pingTimer) {
        console.log("Stopped heartbeat timer", state.socket.pingTimer);
        clearInterval(state.socket.pingTimer);
        state.socket.pingTimer = null
      }
      if (state.socket.pingTimeOutTimer) {
        console.log("Stopped heartbeat timeout timer", state.socket.pingTimeOutTimer);
        clearInterval(state.socket.pingTimeOutTimer);
        state.socket.pingTimeOutTimer = null
      }
    },
    SOCKET_RECONNECT (state) {
      console.log('Web socket reconnected.');
      state.socket.isConnected = true;
      heartbeatController()
    },
    SOCKET_ONMESSAGE(state, msg) {
      console.log('Received unhandled message', msg)
    },
    HBRESPONSE (state) {
      // console.log("Received heartbeat response");
      state.socket.messageAccepted = true;
    },
    RECORD_NOTIFICATION (state, record) {
      console.log("Received record notification", record);
      const me = this;
      let newRecord = createRecord(record);
      const isSessionManagement = newRecord.recordType === "SessionMgmt";
      const handleReady = () => {
        state.timelineRecords.push(record);
        me.eventBus.$emit(EventName.RECORD_NOTIFICATION, newRecord);
        if (isErrorLog(newRecord)) {
          me.eventBus.$emit(EventName.LOG_ERROR_RECEIVED);
        }
      };
      if (isSessionManagement) {
        fetchService.performGet('getActualTrial').then(response => {
          me.commit('GET_ACTUAL_TRIAL', (response.data));
          handleReady();
        }).catch(ex => console.log(ex));
      } else {
        handleReady();
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
    GET_RUN_TYPES (state, runTypes) {
      runTypes.forEach(runType => {
        if (state.filterOptions.runType.indexOf(runType) === -1) {
          state.filterOptions.runType.push(runType)
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
      fetchService.performGet('getRecordTypes').then(response => {
        context.commit('GET_RECORD_TYPES', (response.data));
      }).catch(ex => console.log(ex));
      fetchService.performGet('getTopicNames').then(response => {
        context.commit('GET_TOPIC_NAMES', (response.data));
      }).catch(ex => console.log(ex));
      fetchService.performGet('getSenderClientIds').then(response => {
        context.commit('GET_CLIENT_IDS', (response.data));
      }).catch(ex => console.log(ex));
      fetchService.performGet('getReceiverClientIds').then(response => {
        context.commit('GET_CLIENT_IDS', (response.data));
      }).catch(ex => console.log(ex));
      fetchService.performGet('getRunTypes').then(response => {
        context.commit('GET_RUN_TYPES', (response.data));
      }).catch(ex => console.log(ex));
      fetchService.performGet('getMsgTypes').then(response => {
        context.commit('GET_MSG_TYPES', (response.data));
      }).catch(ex => console.log(ex));
      //context.commit('GET_MSG_TYPES', ["Info", "Warn", "Error", "Ack"]);
    },
    getRecord (context, payload) {
      fetchService.performGet('getRecord/' + payload.id).then(response => {
        context.commit('GET_RECORD', (response.data));
      }).catch(ex => console.log(ex));
    },
    getPageCount (context) {
      fetchService.performGet('getPageCount').then(response => {
        context.commit('GET_RECORDS_PAGE_COUNT', (response.data));
      }).catch(ex => console.log(ex));
    },
    getRecords (context, payload) {
      const page = payload ? payload.page : null;
      const url = page ? 'getAllRecords?size=' + Settings.PAGE_SIZE + "&page=" + page : 'getAllRecords';
      fetchService.performGet(url).then(response => {
        console.log('/getAllRecords returned count', response.data.length);
        context.commit('GET_RECORDS', (response.data));
      }).catch(ex => console.log(ex));
    },
    getAllTimelineRecords (context) {
      fetchService.performGet('getAllTimelineRecords').then(response => {
        let list = response.data;
        if (environment.isLocalDevelopment()) {
          list = list.slice(0, Math.min(list.length - 1, 300)); // FIXME: limits number of timeline items to 300
        }
        context.commit('GET_TIMELINE_RECORDS', list);
      }).catch(ex => console.log(ex));
    },
    getActualTrial (context) {
      fetchService.performGet('getActualTrial').then(response => {
        context.commit('GET_ACTUAL_TRIAL', (response.data));
      }).catch(ex => console.log(ex));
    }
  }
});
