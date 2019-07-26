<template>
  <div ref="container"></div>
</template>

<script>
  import JSONTreeView from 'json-tree-view';
  import "json-tree-view/devtools.css";

  export default {
    name: 'JsonTree',
    props: ['json'],
    watch: {
      json: function() {
        console.log("Updating view", this.json);
        if (this.json && this.json.length > 0) {
          const data = JSON.parse(this.json);
          this.view.value = data;
          this.view.refresh();
        }
      }
    },
    mounted () {
      console.log('Mounted, starting JSON view', this.$refs.container, this.json);
      if (this.json && this.json.length > 0) {
        const data = JSON.parse(this.json);
        this.view = new JSONTreeView('', data);
        this.view.expand(true);
        this.view.readonly = true;
        this.$refs.container.append(this.view.dom);
      }
    }
  };
</script>
