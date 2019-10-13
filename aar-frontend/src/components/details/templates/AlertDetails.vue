<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsMap title="Map" :info-array="record.recordData.info"></DetailsMap>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Sender" :value="record.recordData.sender"></DetailsRow>
    <DetailsRow title="Date/Time" :value="record.createDate + ' ' + record.createTime"></DetailsRow>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import DetailsAttachments from '../items/DetailsAttachments';
  import DetailsMap from '../items/DetailsMap';

  export default {
    name: 'AlertDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsMap, DetailsAttachments}
  };
</script>

<style scoped>

</style>
