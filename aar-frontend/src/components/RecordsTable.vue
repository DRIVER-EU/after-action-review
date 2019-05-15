<template>
  <v-flex>
    <v-card>
      <v-data-table :items=filteredRecords hide-actions class="recordsTable">
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
            <th style="padding-bottom: 68px">
              <div class="primary--text" style="padding: 16px">Date</div>
            </th>
            <th style="padding-bottom: 68px">
              <div class="primary--text" style="padding: 16px">Time</div>
            </th>
          </tr>
        </template>
        <template slot="items" slot-scope="props" style="height: 93vh; overflow: auto;">
          <tr @click="recordSelected(props.item.id, props.item.recordType)">
            <td>{{props.item.id}}</td>
            <td>{{props.item.clientId}}</td>
            <td>{{props.item.topic}}</td>
            <td>{{props.item.recordType}}</td>
            <td class="text-xs-center">{{props.item.createDate}}</td>
            <td class="text-xs-center">{{props.item.createTime}}</td>
          </tr>
        </template>
      </v-data-table>
    </v-card>
  </v-flex>
</template>

<script>
  import {eventBus} from '../main';
  import {recordFilter} from '../service/RecordFilterService';

  export default {
    name: 'RecordsTable',
    data: function () {
      return {
        currentlySelectedId: 'All',
        currentlySelectedClientId: 'All',
        currentlySelectedTopicId: 'All',
        currentlySelectedRecordType: 'All'
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
    },
    computed: {
      records: function () {
        return this.$store.getters.records;
      },
      filterOptions: function () {
        return this.$store.getters.filterOptions;
      },
      filteredRecords: function () {
        return this.records;
      }
    },
    methods: {
      recordSelected: function (recordID, recordType) {
        eventBus.$emit('recordSelected', recordID, recordType);
      },
      updateFilter: function() {
        recordFilter.updateFilter(this.currentlySelectedId, this.currentlySelectedClientId, this.currentlySelectedRecordType, this.currentlySelectedTopicId);
      }
    },
  };
</script>
