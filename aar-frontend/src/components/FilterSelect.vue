<template>
  <v-select
    :items="options"
    id="id"
    label="All"
    single-line
    v-model="currentlySelected"
  ></v-select>
</template>

<script>
  export default {
    name: "FilterSelect",
    props: ['id','options'],
    data() {
      return {
        currentlySelected: "All"
      }
    }
    ,
    methods: {},
    watch:
      {
        currentlySelected: function () {
          this.$emit('selectedValueChanged', this.id, this.currentlySelected)
        }
      }
  }
</script>

<style scoped>

</style>
