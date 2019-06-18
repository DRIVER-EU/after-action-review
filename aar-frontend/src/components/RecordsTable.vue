<template>
  <v-flex>
    <v-card class="tablePanel" style="position:absolute;top:0px;left:0px;right:0px;bottom:60px;">
      <v-data-table :items=filteredRecords hide-actions class="recordsTable" style="height:100%">
        <template slot="headers" slot-scope="props">
          <tr>
            <th>
              <div class="primary--text" style="padding: 16px;">RecordID</div>
              <v-select
                :items="filterOptions.id"
                label="All"
                single-line
                v-model="currentlySelectedId"
              ></v-select>
            </th>
            <th>
              <div class="primary--text" style="padding: 16px;">ClientID</div>
              <v-select
                :items="filterOptions.clientId"
                label="All"
                single-line
                v-model="currentlySelectedClientId"
              ></v-select>
            </th>
            <th>
              <div class="primary--text" style="padding: 16px;">Topic</div>
              <v-select
                :items="filterOptions.topic"
                label="All"
                single-line
                v-model="currentlySelectedTopicId"
              ></v-select>
            </th>
            <th>
              <div class="primary--text" style="padding: 16px;">Type</div>
              <v-select
                :items="filterOptions.recordType"
                label="All"
                single-line
                v-model="currentlySelectedRecordType"
              ></v-select>
            </th>
            <th>
              <div class="primary--text" style="padding: 16px">Date</div>
              <table>
                <tr>
                  <td><v-datetime-picker label="From" v-model="currentlySelectedFromDate" style="min-width: 150px"></v-datetime-picker></td>
                  <td><v-datetime-picker label="To" v-model="currentlySelectedToDate" style="min-width: 150px"></v-datetime-picker></td>
                </tr>
              </table>
            </th>
          </tr>
        </template>
        <template slot="items" slot-scope="props" style="height: 93vh; overflow: auto;">
          <tr @click="recordSelected(props.item.id, props.item.recordType)" v-bind:class="getRowClass(props.item)">
            <td>{{props.item.id}}</td>
            <td>{{props.item.clientId}}</td>
            <td>{{props.item.topic}}</td>
            <td>{{props.item.recordType}}</td>
            <td class="text-xs-center">{{props.item.createDate}}&nbsp;{{props.item.createTime}}</td>
          </tr>
        </template>
      </v-data-table>
    </v-card>
    <v-card class="tablePanel" style="position:absolute;bottom:0px;left:0px;right:0px;height:60px;">
      <div class="text-xs-center" style="padding: 10px 0px">
        <v-pagination v-model="pagination.page" :length=recordsPageCount :total-visible="11" @input="switchPage"></v-pagination>
      </div>
    </v-card>
  </v-flex>
</template>

<script>
  import {eventBus} from '../main';
  import {recordFilter} from '../service/RecordFilterService';
  import EventName from '../constants/EventName';
  import Settings from '../constants/Settings';
  import RecordType from '../constants/RecordType';

  export default {
    name: 'RecordsTable',
    data: function () {
      return {
        currentlySelectedId: 'All',
        currentlySelectedClientId: 'All',
        currentlySelectedTopicId: 'All',
        currentlySelectedRecordType: 'All',
        currentlySelectedFromDate: null,
        currentlySelectedToDate: null,
        additionalRecords: [],
        pagination: {
          page: 1,
        }
      };
    },
    watch: {
      currentlySelectedId: function () {
        this.updateFilter();
      },
      currentlySelectedClientId: function () {
        this.updateFilter();
      },
      currentlySelectedTopicId: function () {
        this.updateFilter();
      },
      currentlySelectedRecordType: function () {
        this.updateFilter();
      },
      currentlySelectedFromDate: function () {
        this.updateFilter();
      },
      currentlySelectedToDate: function () {
        this.updateFilter();
      },
    },
    computed: {
      records: function () {
        return this.$store.getters.records;
      },
      recordsPageCount: function () {
        return this.$store.state.recordsPageCount;
      },
      filterOptions: function () {
        return this.$store.getters.filterOptions;
      },
      filteredRecords: function () {
        if (this.pagination.page === 1) {
          let result = [];
          result = result.concat(this.additionalRecords.slice(0, Settings.PAGE_SIZE));
          result = result.concat(this.records.slice(0, Settings.PAGE_SIZE - result.length));
          return result;
        } else {
          return this.records;
        }
      }
    },
    methods: {
      switchPage (page) {
        this.reloadData();
      },
      recordSelected: function (recordID, recordType) {
        eventBus.$emit(EventName.RECORD_SELECTED, recordID, recordType);
      },
      updateFilter: function () {
        recordFilter.updateFilter(this.currentlySelectedId, this.currentlySelectedClientId, this.currentlySelectedRecordType, this.currentlySelectedTopicId, this.currentlySelectedFromDate, this.currentlySelectedToDate);
      },
      getRowClass: function(item) {
        if (item && this.$store.state.record && item.id === this.$store.state.record.id) {
          return "selected";
        } else if (item.recordType === RecordType.LOG) {
          const level = item.recordData ? item.recordData.level : "unknown";
          return "log-" + level;
        } else {
          return null;
        }
      },
      reloadData() {
        this.additionalRecords = [];
        this.$store.dispatch('getPageCount');
        this.$store.dispatch('getRecords', {page: this.pagination.page});
      }
    },
    created () {
      const vm = this;
      this.$store.dispatch('getRecords', {page: vm.pagination.page});
      eventBus.$on(EventName.FILTER_CHANGED, this.reloadData);
      eventBus.$on(EventName.RECORD_NOTIFICATION, (newRecord) => {
        vm.additionalRecords.unshift(newRecord);
      });
    }
  };
</script>
