import RecordType from '../constants/RecordType';

class TimelineService {
  static INSTANCE = new TimelineService();

  static getInstance () {
    return TimelineService.INSTANCE;
  }

  static GROUP_TRIALS = 1;

  static GROUP_SCENARIOS = 2;

  static GROUP_SESSIONS = 3;

  static GROUP_RECORDS = 4;

  getGroups () {
    return [{
      id: TimelineService.GROUP_TRIALS,
      content: 'Trials'
    }, {
      id: TimelineService.GROUP_SCENARIOS,
      content: 'Scenarios'
    }, {
      id: TimelineService.GROUP_SESSIONS,
      content: 'Sessions'
    }, {
      id: TimelineService.GROUP_RECORDS,
      content: 'Events'
    }];
  };

  getOptions () {
    const options = {
      editable: false,
      stack: false,
      selectable: false,
    };
    // const records = this.getRecords();
    // if (records != null) {
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
    // }
    console.log('Using options', options);
    return options;
  }

  // for clustering see: https://codepen.io/anon/pen/OZYwQN (as well as https://github.com/almende/vis/issues/3859)
  // for performance see http://visjs.org/examples/timeline/other/groupsPerformance.html?count=10000
  getItems (trial, records, isLogIncluded) {
    const items = [];
    if (trial != null && records != null) {
      items.push(this.createTrialItem(trial));
      const scenarios = trial.szenarioList || [];
      for (let i = 0; i < scenarios.length; i++) {
        const scenario = scenarios[i];
        items.push(this.createScenarioItem(scenario));
        const sessions = scenario.sessionList || [];
        for (let j = 0; j < sessions.length; j++) {
          const session = sessions[j];
          items.push(this.createSessionItem(session));
        }
      }
      for (let i = 0; i < records.length; i++) {
        const record = records[i];
        const isLogRecord = record.recordType === RecordType.LOG;
        if (!isLogRecord || isLogIncluded) {
          items.push(this.createRecordItem(record));
        }
      }
    }
    return items;
  }

  createTrialItem (trial) {
    return {
      group: TimelineService.GROUP_TRIALS,
      start: new Date(trial.startDate),
      end: new Date(trial.endDate),
      openEnd: trial.endDate === null,
      content: trial.trialName,
      className: 'trial'
    };
  }

  createScenarioItem (scenario) {
    return {
      group: TimelineService.GROUP_SCENARIOS,
      start: new Date(scenario.startDate),
      end: new Date(scenario.endDate),
      openEnd: scenario.endDate === null,
      content: scenario.szenarioName,
      className: 'scenario'
    };
  }

  createSessionItem (session) {
    return {
      group: TimelineService.GROUP_SESSIONS,
      start: new Date(session.startDate),
      end: new Date(session.endDate),
      openEnd: session.endDate === null,
      content: session.sessionName,
      className: 'session'
    };
  }

  createRecordItem (record) {
    return {
      id: record.id,
      group: TimelineService.GROUP_RECORDS,
      start: new Date(record.createDate),
      content: '' + record.id,
      className: record.recordType,
      record: record,
      title: record.recordType
    };
  }

  isRecordGroup (groupId) {
    return groupId === TimelineService.GROUP_RECORDS;
  }
}

const timeline = TimelineService.getInstance();

export {TimelineService, timeline};
