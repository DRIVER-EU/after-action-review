<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Trial" :value="record.recordData.trialName"></DetailsRow>
    <DetailsRow title="Scenario" :value="record.recordData.scenarioName"></DetailsRow>
    <DetailsRow title="Session" :value="record.recordData.sessionName"></DetailsRow>
    <DetailsRow title="State" :value="record.recordData.sessionState"></DetailsRow>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from './DetailsRow';
  import DetailsJsonTree from './DetailsJsonTree';
  import {formatDate} from '../../helper';

  export default {
    name: 'SessionMgmtDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree},
  };
</script>

<style scoped>

</style>
