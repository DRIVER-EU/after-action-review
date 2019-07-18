<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Client ID" :value="record.recordData.id"></DetailsRow>
    <DetailsRow title="Date/Time" :value="record.createDate + ' ' + record.createTime"></DetailsRow>
    <DetailsRow title="Level" :value="record.recordData.level"></DetailsRow>
    <DetailsBlock title="Message" :value="record.recordData.log"></DetailsBlock>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import DetailsBlock from '../items/DetailsBlock';
  import DetailsAttachments from '../items/DetailsAttachments';

  export default {
    name: 'LogDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsBlock, DetailsAttachments},
  };
</script>

<style scoped>

</style>
