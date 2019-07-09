<template>
  <div ref="container"></div>
</template>

<script>
  import 'ol/ol.css';
  import Map from 'ol/Map';
  import View from 'ol/View';
  import TileLayer from 'ol/layer/Tile';
  import VectorLayer from 'ol/layer/Vector';
  import OSM from 'ol/source/OSM';
  import VectorSource from 'ol/source/Vector';
  import GeoJSON from 'ol/format/GeoJSON';
  import {transformExtent} from 'ol/proj.js';
  import {mapStyling} from '../service/MapStylingService';

  export default {
    name: 'MapPanel',
    props: ['geojson'],
    watch: {
      geojson: function () {
        this.updateFeatures();
      }
    },
    methods: {
      transform (extent) {
        return transformExtent(extent, 'EPSG:4326', 'EPSG:3857');
      },
      updateFeatures () {
        const features = new GeoJSON().readFeatures(this.geojson);
        console.log('Displaying features', features);
        this.vectorSource.clear();
        if (features.length > 0) {
          features.forEach(f => f.getGeometry().transform('EPSG:4326', 'EPSG:3857'));
          this.vectorSource.addFeatures(features);
          const extent = this.vectorSource.getExtent();
          this.map.getView().fit(extent, {size: this.map.getSize(), maxZoom: 10});
        } else {
          const euExtent = this.transform([-27.68862, 33.59717, 43.90757, 71.97626]);
          this.map.getView().fit(euExtent, this.map.getSize());
        }
      }
    },
    mounted () {
      console.log('Mounted, starting map', this.$refs.container);

      this.vectorSource = new VectorSource({
        features: []
      });

      const vectorLayer = new VectorLayer({
        source: this.vectorSource,
        style: mapStyling.stylingFunction
      });

      this.map = new Map({
        target: this.$refs.container,
        layers: [
          new TileLayer({
            source: new OSM()
            /*
            source: new XYZ({
              url: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
            })
            */
          }),
          vectorLayer
        ],
        view: new View({
          center: [0, 0],
          zoom: 2
        })
      });

      this.updateFeatures();
    }
  };
</script>
