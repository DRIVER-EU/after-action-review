import Vue from 'vue'
import Vuex from 'vuex'
import {Record} from '../objects/record'
import {heartbeatController} from '../heartbeatController'
import {createFilterOptions} from '../helper'
import {parseDate} from '../helper'

Vue.use(Vuex)

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
    filterOptions: {
      id: ["All"],
      clientId: ["All"],
      recordType: ["All"]
    }
  },
  getters: {
    records(state) {
      return state.records.sort((a,b) => {return b.id - a.id})
    },
    filterOptions(state) {
      return state.filterOptions
    }
  }
  ,
  mutations: {
    SOCKET_ONOPEN(state) {
      console.log('connection open')
      state.socket.isConnected = true
      heartbeatController()
    },
    SOCKET_ONCLOSE(state) {
      console.log('connection closed')
      state.socket.isConnected = false
      clearInterval(state.socket.pingTimer);
      clearInterval(state.socket.pingTimeOutTimer);
    },
    SOCKET_RECONNECT() {
      console.log('reconnect')
    },
    SOCKET_ONERROR() {
    },
    HBRESPONSE(state) {
      state.socket.messageAccepted = true
    },
    RECORD_NOTIFICATION(state, record) {
      record.recordJson = JSON.parse(record.recordJson)
      if(record.recordJson.hasOwnProperty('dateTimeSent')) {
        record.recordJson.dateTimeSent = parseDate(record.recordJson.dateTimeSent)
      }
      state.records.push(new Record(record))
      var keys = Object.keys(record)
      keys.forEach(function(key){
        if(state.filterOptions[key] && state.filterOptions[key].indexOf(record[key]) === -1 )  state.filterOptions[key].push(record[key])
      })
    },
    GET_RECORDS(state, records) {
      records.forEach(function (record) {
        record.recordJson = JSON.parse(record.recordJson)
        record.createDate = parseDate(record.createDate)
        if(record.recordJson.hasOwnProperty('dateTimeSent')) {
          record.recordJson.dateTimeSent = parseDate(record.recordJson.dateTimeSent)
        }
        createFilterOptions(record, state.filterOptions)
      })
      state.records = records
    }
  }
  ,
  actions: {
    getRecords(context) {
      this.axios.get('getAllRecords').then(response => {
        context.commit('GET_RECORDS', (response.data));

      }).catch(function () {
      });
    }
  }
})
