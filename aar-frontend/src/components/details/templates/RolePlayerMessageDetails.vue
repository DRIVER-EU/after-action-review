<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Type" :value="record.recordData.type"></DetailsRow>
    <DetailsRow title="Title" :value="record.recordData.title"></DetailsRow>
    <DetailsRow title="Headline" :value="record.recordData.headline"></DetailsRow>
    <DetailsRow title="Description" :value="record.recordData.description"></DetailsRow>
    <DetailsRow title="Role player" :value="record.recordData.rolePlayerName"></DetailsRow>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import DetailsAttachments from '../items/DetailsAttachments';

  export default {
    name: 'RolePlayerMessageDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsAttachments},
  };
</script>

<style scoped>

</style>
