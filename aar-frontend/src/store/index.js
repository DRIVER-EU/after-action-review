import Vue from 'vue';
import Vuex from 'vuex';
import {Record} from '../objects/record';
import {heartbeatController} from '../heartbeatController';
import {createFilterOptions} from '../helper';
import {parseDate} from '../helper';

Vue.use(Vuex);

function createRecord(record) {
  let newRecord = new Record(record);
  newRecord.createDate = parseDate(record.createDate, 'YYYY-MM-DD');
  newRecord.createTime = parseDate(record.createDate, 'HH:mm:ss.SSS');
  newRecord.recordData = JSON.parse(record.recordJson);
  if (newRecord.recordJson && newRecord.recordJson.hasOwnProperty('dateTimeSent')) {
    newRecord.recordJson.dateTimeSent = parseDate(newRecord.recordJson.dateTimeSent);
  }
  return newRecord;
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
    timelineRecords: null,
    record: null,
    trial: null,
    filterOptions: {
      id: ['All'],
      clientId: ['All'],
      topic: ['All'],
      recordType: ['All']
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
      state.socket.messageAccepted = true;
    },
    RECORD_NOTIFICATION (state, record) {
      let newRecord = createRecord(record);
      state.records.push(newRecord);
      createFilterOptions(newRecord, state.filterOptions);
    },
    GET_RECORDS (state, records) {
      records.forEach(function (record) {
        const newRecord = createRecord(record);
        state.records.push(newRecord);
        createFilterOptions(newRecord, state.filterOptions);
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
    getRecord (context, payload) {
      this.axios.get('getRecord/' + payload.id).then(response => {
        context.commit('GET_RECORD', (response.data));
      }).catch(ex => console.log(ex));
    },
    getRecords (context) {
      this.axios.get('getAllRecords').then(response => {
        context.commit('GET_RECORDS', (response.data));
      }).catch(ex => console.log(ex));
    },
    getAllTimelineRecords (context) {
      this.axios.get('getAllTimelineRecords').then(response => {
        let list = response.data;
        // list = list.slice(0, Math.min(list.length - 1, 300)); // FIXME
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
