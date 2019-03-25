<template>
  <v-flex>
    <v-card>
      <v-card-title class="justify-center primary--text">Timeline</v-card-title>
      <v-card-text v-if="!items" class="text-xs-center">Loading...</v-card-text>
      <v-card-text v-else>
        <timeline ref="timeline"
                  :items="items"
                  :groups="groups"
                  :options="options">
        </timeline>
      </v-card-text>
    </v-card>
  </v-flex>
</template>


<script>
  // for clustering see: https://codepen.io/anon/pen/OZYwQN (as well as https://github.com/almende/vis/issues/3859)

  import {eventBus} from '../main'

  export default {
    name: 'TimelinePanel',
    data: function () {
      return {
        recordID: '',
        recordData: null,
        groups: [{
          id: 1,
          content: 'Trials'
        }, {
          id: 2,
          content: 'Scenarios'
        }, {
          id: 3,
          content: 'Events'
        }],
      }
    },
    computed: {
      // for performance see http://visjs.org/examples/timeline/other/groupsPerformance.html?count=10000
      items: function () {
        const trial = this.$store.state.trial;
        const records = this.getRecords();
        console.log("itemsx", trial, records);
        if (trial != null && records != null) {
          const scenarios = trial.szenarioList || [];
          const items = [];
          items.push(this.createTrialItem(trial));
          for (let i = 0; i < scenarios.length; i++) {
            items.push(this.createScenarioItem(scenarios[i]));
          }
          for (let i = 0; i < records.length; i++) {
            items.push(this.createRecordItem(records[i]));
          }
          console.log("Rendering items", items);
          return items;
        }
      },
      options: function() {
        const records = this.getRecords();
        const options = {
          editable: false,
          stack: false,
        };
        if (records != null) {
          /*
          const itemsToShow = 10;
          const sortedRecords = records.sort((a, b) => new Date(a.createDate).getTime() - new Date(b.createDate).getTime());
          const firstDate = new Date(sortedRecords[records.length - itemsToShow].createDate);
          const lastDate = new Date(sortedRecords[records.length - 1].createDate);
          const duration = lastDate.getTime() - firstDate.getTime();
          options.start = new Date(firstDate.getTime() - duration/10);
          options.end = new Date(lastDate.getTime() + duration/10);
          // options.zoomMax = 1 * 60 * 1000;
          */
        }
        console.log("Using options", options);
        return options;
      }
    },
    methods: {
      getRecords: function() {
        const records = this.$store.state.timelineRecords;
        return records != null ? records : null; // .slice(0, Math.min(1000, records.length - 1))
      },
      createTrialItem: function(trial) {
        return {
          group: 1,
          start: new Date(trial.startDate),
          end: new Date(trial.endDate),
          content: trial.trialName,
        }
      },
      createScenarioItem: function(scenario) {
        return {
          group: 2,
          start: new Date(scenario.startDate),
          end: new Date(scenario.endDate),
          content: scenario.szenarioName,
        }
      },
      createRecordItem: function(record) {
        return {
          group: 3,
          start: new Date(record.createDate),
          content: "" + record.id,
        }
      },
    },
    created () {
      eventBus.$on('recordSelected', (recordID, recordData) => {
      })
    }
  }
</script>