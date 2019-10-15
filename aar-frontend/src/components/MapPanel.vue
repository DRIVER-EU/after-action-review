<template>
  <div ref="container"></div>
</template>

<script>
  import 'ol/ol.css';
  import Map from 'ol/Map';
  import View from 'ol/View';
  import Feature from 'ol/Feature';
  import TileLayer from 'ol/layer/Tile';
  import VectorLayer from 'ol/layer/Vector';
  import OSM from 'ol/source/OSM';
  import VectorSource from 'ol/source/Vector';
  import GeoJSON from 'ol/format/GeoJSON';
  import Circle from 'ol/geom/Circle';
  import Point from 'ol/geom/Point';
  import Polygon from 'ol/geom/Polygon';
  import Style from 'ol/style/Style';
  import Stroke from 'ol/style/Stroke';
  import Fill from 'ol/style/Fill';
  import {transformExtent} from 'ol/proj.js';
  import {mapStyling} from '../service/MapStylingService';

  export default {
    name: 'MapPanel',
    props: ['geojson', 'infoArray'],
    watch: {
      geojson: function () {
        this.updateFeatures();
      },
      infoArray: function () {
        this.updateFeatures();
      }
    },
    methods: {
      transform (extent) {
        return transformExtent(extent, 'EPSG:4326', 'EPSG:3857');
      },
      updateFeatures () {
        this.vectorSource.clear();
        this.updateFeaturesFromGeoJson();
        this.updateFeaturesFromInfoArray();
        this.scaleToFeatures();
      },
      updateFeaturesFromGeoJson () {
        if (this.geojson) {
          const features = new GeoJSON().readFeatures(this.geojson);
          console.log('Displaying GeoJSON features', features);
          if (features.length > 0) {
            features.forEach(f => f.getGeometry().transform('EPSG:4326', 'EPSG:3857'));
            this.vectorSource.addFeatures(features);
          }
        }
      },
      updateFeaturesFromInfoArray () {
        if (this.infoArray) {
          const features = [];
          console.log('Displaying info array features');
          for (const info of this.infoArray) {
            for (const area of info.area) {
              const feature = this.createFeatureFromInfoArea(area);
              if (feature) {
                features.push(feature);
              }
            }
          }
          if (features.length > 0) {
            features.forEach(f => f.getGeometry().transform('EPSG:4326', 'EPSG:3857'));
            this.vectorSource.addFeatures(features);
          }
        }
      },
      createGeometryFromInfoArea(area) {
        if (area.circle) {
          const pointAndRadius = area.circle.split(" ");
          const point = this.createPoint(pointAndRadius[0]);
          const radius = parseFloat(pointAndRadius[1]);
          if (radius > 0) {
            return new Circle(point, radius );
          } else {
            return new Point(point);
          }
        } else if (area.polygon) {
          const points = area.polygon.split(" ").map(this.createPoint);
          return new Polygon([points]);
        }
        return null;
      },
      createPoint(coordinates) {
        const latAndLon = coordinates.split(",");
        const lat = parseFloat(latAndLon[0]);
        const lon = parseFloat(latAndLon[1]);
        return [lon, lat];
      },
      createFeatureFromInfoArea(area) {
        // style: var camelCased = myString.replace(/-([a-z])/g, function (g) { return g[1].toUpperCase(); });
        // new OpenLayers.Style({strokeColor: "blue",          strokeWidth: 2,          strokeOpacity: 0.8      });
        // feature.setStyle()
        const geometry = this.createGeometryFromInfoArea(area);
        if (geometry) {
          const feature = new Feature(geometry);
          const style = this.createStyleFromInfoArea(area);
          feature.setStyle(style);
          return feature;
        } else {
          return null;
        }
      },
      createStyleFromInfoArea(area) {
        const styleValue = (area.geocode || []).filter(g => g.valueName === "style").map(g => g.value).find(a => true);
        console.log(area, styleValue);
        return new Style({stroke: new Stroke({color: '#FF8C00', width: 2})});
      },
      scaleToFeatures() {
        const features = this.vectorSource.getFeatures();
        if (features.length > 0) {
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
