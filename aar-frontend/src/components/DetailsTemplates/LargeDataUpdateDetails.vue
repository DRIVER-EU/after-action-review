<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="URL" :value="record.recordData.url"></DetailsRow>
    <DetailsRow title="Data Type" :value="record.recordData.dataType"></DetailsRow>
    <DetailsRow title="Title" :value="record.recordData.title"></DetailsRow>
    <DetailsRow title="Description" :value="record.recordData.description"></DetailsRow>
  </ul>
</template>

<script>
  import DetailsRow from './DetailsRow';
  import DetailsJsonTree from './DetailsJsonTree';

  export default {
    name: 'LargeDataUpdateDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree},
  };
</script>

<style scoped>

</style>
