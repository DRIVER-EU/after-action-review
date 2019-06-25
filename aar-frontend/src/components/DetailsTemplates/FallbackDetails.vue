<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Client ID" :value="record.recordData.id"></DetailsRow>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from './DetailsRow';
  import DetailsJsonTree from './DetailsJsonTree';

  export default {
    name: 'FallbackDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree},
  };
</script>

<style scoped>

</style>
