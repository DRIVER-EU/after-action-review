<template>
  <v-flex>
    <v-card class="timelinePanel" style="border-top: 1px solid #999999;">
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
          eventBus.$emit('recordSelected', recordId, null);
        }
      },
      getRecords: function () {
        const records = this.$store.state.timelineRecords;
        return records != null ? records : null; // .slice(0, Math.min(1000, records.length - 1))
      },
      updateTimeline: function () {
        console.log('Updating timeline');
        const trial = this.$store.state.trial;
        const records = this.getRecords();
        const items = timeline.getItems(trial, records, this.isLogIncluded);
        if (items.length > 0) {
          console.log('Rendering items', items, this.isInitialized);
          const data = new DataSet(items);
          if (!this.isInitialized) {
            const timeStart = new Date().getTime();
            this.timeline = new Timeline(this.$refs.container, data, timeline.getGroups(), timeline.getOptions());
            this.timeline.on('click', this.handleClick);
            console.log('Rendered in ' + (new Date().getTime() - timeStart) + ' ms');
            this.isInitialized = true;
          } else {
            this.timeline.setItems(data);
          }
        }
      },
    },
    created () {
      eventBus.$on('recordSelected', (recordID, recordData) => {
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
