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
      topic: ["All"],
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
      let newRecord = new Record(record)
      newRecord.recordJson = JSON.parse(record.recordJson)
      let splitDateTime = record.createDate.split(" ")
      console.log(record.createDate, splitDateTime)
      newRecord.createDate = splitDateTime[0]
      newRecord.createTime = splitDateTime[1]
      if(newRecord.recordJson.hasOwnProperty('dateTimeSent')) {
        newRecord.recordJson.dateTimeSent = parseDate(newRecord.recordJson.dateTimeSent)
      }
      state.records.push(newRecord)
      createFilterOptions(newRecord, state.filterOptions)
    },
    GET_RECORDS(state, records) {
      records.forEach(function (record) {
        let newRecord = new Record(record)
        newRecord.recordJson = JSON.parse(record.recordJson)
        newRecord.createDate = parseDate(record.createDate, 'YYYY-MM-DD')
        newRecord.createTime = parseDate(record.createDate, 'HH:mm:ss.SSS')
        if(newRecord.recordJson.hasOwnProperty('dateTimeSent')) {
          newRecord.recordJson.dateTimeSent = parseDate(newRecord.recordJson.dateTimeSent, 'YYYY-MM-DD HH:mm:ss.SSS')
        }
        state.records.push(newRecord)
        createFilterOptions(newRecord, state.filterOptions)
      })
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
