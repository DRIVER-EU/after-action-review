<template>
  <v-app>
    <toolbar>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>all_inbox</v-icon>
          Reports
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile @click="downloadReport('FIRST_IMPRESSION_OV')">
              <v-icon left>inbox</v-icon>
              FIE Overview
            </v-list-tile>
            <v-list-tile @click="downloadReport('FIRST_IMPRESSION')">
              <v-icon left>inbox</v-icon>
              First Impression Evaluation
            </v-list-tile>
            <v-list-tile @click="downloadReport('BASELINE')">
              <v-icon left>inbox</v-icon>
              Baseline Observation
            </v-list-tile>
            <v-list-tile @click="downloadReport('INNOVATIONLINE')">
              <v-icon left>inbox</v-icon>
              Innovationline Observation
            </v-list-tile>
            <v-list-tile @click="downloadReport('OBSERVATIONS')">
              <v-icon left>inbox</v-icon>
              Observations
            </v-list-tile>
            <v-list-tile @click="downloadReport('TRIAL_DIMENSION')">
              <v-icon left>inbox</v-icon>
              Trial dimension
            </v-list-tile>
            <v-list-tile @click="downloadReport('SOLUTION_DIMENSION')">
              <v-icon left>inbox</v-icon>
              Solution dimension
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>bar_chart</v-icon> <!-- insert_chart_outlined -->
          Sequence diagram
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile @click="openOverviewSequenceDiagramPage()">
              <v-icon left>insert_chart_outlined</v-icon>
              Overview
            </v-list-tile>
            <v-list-tile @click="openSequenceDiagramPage()">
              <v-icon left>bar_chart</v-icon>
              Details
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <fetch-button-group className="diagramButton" icon="rotate_right" :buttons="[{title:'Analyse records', icon: 'rotate_right', url:'/analyseRecords'}, {title:'Finish trial', icon: 'rotate_right', url:'/finishUpTheTrial'}]">
        Post-Process
      </fetch-button-group>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>save_alt</v-icon>
          Export Data
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile v-for="(item, index) in exportDataItems" :key="index" @click="exportData(item.exportType)">
              <v-icon left>save_alt</v-icon> <!-- arrow_downward -->
              <v-list-tile-title v-text="item.title"/>
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
    </toolbar>
    <main style="height: 100%">
      <v-layout column justify-space-between fill-height>
        <div style="position:absolute;top:64px;bottom:300px;left:0px;right:0px;">
          <v-layout row wrap fill-height>
            <div ref="mainFrame" style="position:absolute;top:0px;bottom:0px;left:0px;right:400px;">
              <records-table style="height: 100%; overflow: auto;"/>
            </div>
            <div ref="detailsFrame" style="position:absolute;top:0px;bottom:0px;right:0px;width:400px;">
              <details-panel style="height: 100%; overflow: auto;" :onWidthChange="setDetailsWidth"/>
            </div>
          </v-layout>
        </div>
        <div style="position:absolute;bottom:0px;height:300px;left:0px;right:0px;">
          <timeline-panel style="overflow: auto;"/>
        </div>
      </v-layout>
      <v-snackbar v-model="snackbar.visible" :top="true" :timeout="0" color="error">
        {{snackbar.text}}
        <v-btn flat @click="snackbar.visible = false">
          Close
        </v-btn>
      </v-snackbar>
    </main>
    <diagram-popup/>
    <attachment-popup/>
  </v-app>
</template>
<script>
  import {recordFilter} from '../service/RecordFilterService';
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';
  import {fetchService} from '../service/FetchService';
  import Urls from '../constants/Urls';
  import FetchButton from '../components/FetchButton';
  import FetchButtonGroup from '../components/FetchButtonGroup';

  export default {
    name: 'MainPage',
    components: {FetchButton, FetchButtonGroup},
    data: () => ({
      exportDataItems: [
        {exportType: 'CSV', title: 'CSV'},
        {exportType: 'SQL', title: 'SQL'},
      ],
      snackbar: {
        visible: false,
        text: ""
      }
    }),
    methods: {
      analyseRecords() {
        fetchService.performGet(Urls.HTTP_BASE + "/analyseRecords");
      },
      openDiagramPopup () {
        eventBus.$emit(EventName.DIAGRAM_POPUP, true);
      },
      openSequenceDiagramPage () {
        let routeData = this.$router.resolve({name: 'sequenceDiagram'});
        window.open(routeData.href, '_blank');
      },
      openOverviewSequenceDiagramPage () {
        let routeData = this.$router.resolve({name: 'overviewSequenceDiagram'});
        window.open(routeData.href, '_blank');
      },
      exportData (exportType) {
        fetchService.performSimpleDownload('exportData?exportType=' + exportType);
      },
      downloadReport (reportType) {
        const path = this.getReportPath(reportType);
        console.log("Downloading report from " + path);
        fetchService.performSimpleDownload(path);
      },
      getReportPath(reportType) {
        switch (reportType) {
          case "FIRST_IMPRESSION_OV":
            return "createOverviewFIEPDFReport";
          case "FIRST_IMPRESSION":
            return "createFIEPDFReport";
          case "BASELINE":
            return "createPDFStatisticReport?runType=Baseline";
          case "INNOVATIONLINE":
            return "createPDFStatisticReport?runType=Innovationline";
          case "OBSERVATIONS":
            return "createPDFStatisticReport";
          case "TRIAL_DIMENSION":
            return "createPDFStatisticReport?runType=TrialDimention";
          case "SOLUTION_DIMENSION":
            return "createPDFStatisticReport?runType=SolutionDimention";
          default:
            throw "Unsupported report type " + reportType;
        }
      },
      setDetailsWidth(width) {
        const mainFrame = this.$refs.mainFrame;
        const detailsFrame = this.$refs.detailsFrame;
        mainFrame.style.right = width + "px";
        detailsFrame.style.width = width + "px";
      }
    },
    created () {
      const vm = this;
      recordFilter.resetFilter();
      this.$store.dispatch('getPageCount');
      this.$store.dispatch('getFilterOptions');
      this.$store.dispatch('getActualTrial');
      eventBus.$on(EventName.LOG_ERROR_RECEIVED, () => {
        vm.snackbar.text = "A record of type Log on level ERROR has been received.";
        vm.snackbar.visible = true;
      });
    }
  };
</script>
