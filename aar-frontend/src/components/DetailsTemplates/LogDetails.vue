<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Client ID" :value="record.recordData.id"></DetailsRow>
    <DetailsRow title="Date/Time" :value="record.createDate + ' ' + record.createTime"></DetailsRow>
    <DetailsRow title="Level" :value="record.recordData.level"></DetailsRow>
    <DetailsBlock title="Message" :value="record.recordData.log"></DetailsBlock>
  </ul>
</template>

<script>
  import DetailsRow from './DetailsRow';
  import DetailsJsonTree from './DetailsJsonTree';
  import DetailsBlock from './DetailsBlock';

  export default {
    name: 'LogDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsBlock},
  };
</script>

<style scoped>

</style>
