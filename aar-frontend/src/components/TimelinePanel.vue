<template>
  <v-flex>
    <v-card class="timelinePanel">
      <v-card-title class="justify-center primary--text">
        <span>Timeline</span>
        <div class="includeLogs">
          <v-checkbox
            v-model="isLogIncluded"
            :label="`Include Logs`"
          ></v-checkbox>
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

  export default {
    name: 'TimelinePanel',
    isInitialized: false,
    data: function() {
      return {
        isLogIncluded: false,
      };
    },
    watch: {
      isLogIncluded: function () {
        this.updateTimeline();
      }
    },
    computed: {},
    methods: {
      handleClick: function (data) {
        const recordId = data.item;
        if (recordId && timeline.isRecordGroup(data.group)) {
          eventBus.$emit(EventName.RECORD_SELECTED, recordId, null);
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
        this.selectedId = this.$store.state.record ? this.$store.state.record.id : null;
        const items = timeline.getItems(trial, records, this.isLogIncluded, this.selectedId);
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
        const newSelectedItem = this.timeline.itemsData.get(recordID);
        const updates = [];
        if (this.selectedId) {
          const previousSelectedItem = this.timeline.itemsData.get(this.selectedId);
          if (previousSelectedItem) {
            updates.push({id: previousSelectedItem.id, className: previousSelectedItem.baseClassName});
          }
        }
        if (newSelectedItem) {
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
