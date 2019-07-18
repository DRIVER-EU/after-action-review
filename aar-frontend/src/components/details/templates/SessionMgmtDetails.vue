<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Trial" :value="record.recordData.trialName"></DetailsRow>
    <DetailsRow title="Scenario" :value="record.recordData.scenarioName"></DetailsRow>
    <DetailsRow title="Session" :value="record.recordData.sessionName"></DetailsRow>
    <DetailsRow title="State" :value="record.recordData.sessionState"></DetailsRow>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import {formatDate} from '../../../helper';
  import DetailsAttachments from '../items/DetailsAttachments';

  export default {
    name: 'SessionMgmtDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsAttachments},
  };
</script>

<style scoped>

</style>
