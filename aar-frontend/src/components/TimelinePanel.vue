<template>
  <v-flex>
    <v-card class="timelinePanel">
      <v-card-title class="justify-center primary--text">
        <span>Timeline</span>
        <div class="includeLogs">
          <v-checkbox v-model="isLogIncluded" :label="`Include Logs`" style="display: inline-flex"></v-checkbox>
          <v-btn @click.prevent="clearSessionAndScenarioFilters()" :disabled="!clearSessinAndScenarioFiltersEnabled" style="margin-top:-10px;">
            <v-icon left>clear</v-icon>
            Clear filter
          </v-btn>
        </div>
      </v-card-title>
      <div ref="container"></div>
    </v-card>
  </v-flex>
</template>

<script>
  import {eventBus} from '../main';
  import {Timeline, DataSet} from 'vis';
  import {timeline} from '../service/TimelineService';
  import EventName from '../constants/EventName';
  import {recordFilter} from '../service/RecordFilterService';

  export default {
    name: 'TimelinePanel',
    isInitialized: false,
    data: function() {
      return {
        isLogIncluded: false,
        clearSessinAndScenarioFiltersEnabled: false,
      };
    },
    watch: {
      isLogIncluded: function () {
        this.updateTimeline();
      }
    },
    computed: {},
    methods: {
      setClearSessinAndScenarioFiltersEnabled(enabled) {
          this.clearSessinAndScenarioFiltersEnabled = enabled;
      },
      clearSessionAndScenarioFilters() {
        eventBus.$emit(EventName.CLEAR_SESSION_SCENARIO_FILTER);
        this.setClearSessinAndScenarioFiltersEnabled(false);
      },
      handleClick: function (data) {
        const itemId = data.item;
        if (itemId) {
          if (timeline.isRecordGroup(data.group)) {
            eventBus.$emit(EventName.RECORD_SELECTED, itemId, null);
          } else if (timeline.isSessionGroup(data.group)) {
            const sessionId = this.timeline.itemsData.get(itemId).dataId;
            eventBus.$emit(EventName.SESSION_SELECTED, sessionId, null);
            this.setClearSessinAndScenarioFiltersEnabled(true);
          } else if (timeline.isScenarioGroup(data.group)) {
            const scenarioId = this.timeline.itemsData.get(itemId).dataId;
            eventBus.$emit(EventName.SCENARIO_SELECTED, scenarioId, null);
            this.setClearSessinAndScenarioFiltersEnabled(true);
          }
        }
      },
      getRecords: function () {
        const records = this.$store.state.timelineRecords;
        return records != null ? records : null; // .slice(0, Math.min(1000, records.length - 1))
      },
      extendOpenEnd: function() {
        const updates = [];
        for (let i = 0; i < this.openItems.length; i++) {
          const item = this.openItems[i];
          updates.push({id: item.id, end: new Date()});
        }
        this.timeline.itemsData.update(updates);
      },
      updateTimeline: function () {
        console.log('Updating timeline');
        const trial = this.$store.state.trial;
        const records = this.getRecords();
        const filter = recordFilter.getCurrentFilter();
        this.selectedId = this.$store.state.record ? this.$store.state.record.id : null;
        const items = timeline.getItems(trial, records, this.isLogIncluded, this.selectedId, filter);
        if (items.length > 0) {
          console.log('Rendering items', items, this.isInitialized);
          const data = new DataSet(items);
          this.openItems = data.get({
            filter: function (item) {
              return item.openEnd === true;
            }
          });
          if (!this.isInitialized) {
            const timeStart = new Date().getTime();
            this.timeline = new Timeline(this.$refs.container, data, timeline.getGroups(), timeline.getOptions());
            this.timeline.on('click', this.handleClick);
            this.timeline.on('currentTimeTick', this.extendOpenEnd);
            console.log('Rendered in ' + (new Date().getTime() - timeStart) + ' ms');
            this.isInitialized = true;
          } else {
            this.timeline.setItems(data);
          }
        }
      },
    },
    created () {
      eventBus.$on(EventName.RECORD_SELECTED, (recordID, recordData) => {
        const updates = [];
        if (this.selectedId) {
          const previousSelectedItem = this.timeline.itemsData.get(this.selectedId);
          if (previousSelectedItem) {
            updates.push({id: previousSelectedItem.id, className: previousSelectedItem.baseClassName});
          }
        }
        const newSelectedItem = this.timeline.itemsData.get(recordID);
        if (newSelectedItem) {
          this.timeline.focus(recordID);
          updates.push({id: newSelectedItem.id, className: newSelectedItem.baseClassName + " selected"});
        }
        this.timeline.itemsData.update(updates);
        this.selectedId = recordID;
      });
    },
    mounted () {
      console.log('Mounted, starting vis.js timeline', this.$refs.container);
      this.updateTimeline();
      this.$store.watch(state => state.trial, this.updateTimeline);
      this.$store.watch(state => state.timelineRecords, this.updateTimeline);
    }
  };
</script>
