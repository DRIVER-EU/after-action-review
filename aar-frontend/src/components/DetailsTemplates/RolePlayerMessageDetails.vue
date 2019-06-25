<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Type" :value="record.recordData.type"></DetailsRow>
    <DetailsRow title="Title" :value="record.recordData.title"></DetailsRow>
    <DetailsRow title="Headline" :value="record.recordData.headline"></DetailsRow>
    <DetailsRow title="Description" :value="record.recordData.description"></DetailsRow>
    <DetailsRow title="Role player" :value="record.recordData.rolePlayerName"></DetailsRow>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from './DetailsRow';
  import DetailsJsonTree from './DetailsJsonTree';

  export default {
    name: 'RolePlayerMessageDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree},
  };
</script>

<style scoped>

</style>
