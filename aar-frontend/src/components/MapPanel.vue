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
  import TileWMS from 'ol/source/TileWMS';
  import VectorSource from 'ol/source/Vector';
  import GeoJSON from 'ol/format/GeoJSON';
  import Circle from 'ol/geom/Circle';
  import Point from 'ol/geom/Point';
  import Polygon from 'ol/geom/Polygon';
  import Style from 'ol/style/Style';
  import Icon from 'ol/style/Icon';
  import Control from 'ol/control/Control';
  import {transformExtent} from 'ol/proj.js';
  import {mapStyling} from '../service/MapStylingService';
  import Urls from '../constants/Urls';

  export default {
    name: 'MapPanel',
    props: ['geojson', 'infoArray', 'wmsData'],
    watch: {
      geojson: function () {
        this.updateFeatures();
      },
      infoArray: function () {
        this.updateFeatures();
      },
      wmsData: function () {
        this.updateFeatures();
      }
    },
    methods: {
      transform (extent) {
        return transformExtent(extent, 'EPSG:4326', 'EPSG:3857');
      },
      updateFeatures () {
        this.vectorSource.clear();
        this.removeWmsLayer();
        this.updateFeaturesFromGeoJson();
        this.updateFeaturesFromInfoArray();
        this.updateWmsLayerFromWmsData();
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
            if (info.area) {
              const items = Array.isArray(info.area) ? info.area : [info.area];
              const resourceType = this.getResourceType(info.parameter);
              for (const area of items) {
                const feature = this.createFeatureFromInfoArea(area, resourceType);
                if (feature) {
                  features.push(feature);
                }
              }
            }
          }
          if (features.length > 0) {
            features.forEach(f => f.getGeometry().transform('EPSG:4326', 'EPSG:3857'));
            this.vectorSource.addFeatures(features);
          }
        }
      },
      updateWmsLayerFromWmsData() {
        const me = this;
        if (this.wmsData) {
          const update = this.wmsData;
          if (update && update.layerType === "WMS") {
            console.log('Displaying WMS layer', update);
            const ordinates = update.description.split(",");
            const extent = me.transform([parseFloat(ordinates[1]), parseFloat(ordinates[0]), parseFloat(ordinates[3]), parseFloat(ordinates[2])]);
            me.wmsLayer = new TileLayer({
              extent: extent,
              source: new TileWMS({ url: update.url, params: {'LAYERS': update.title, 'TILED': true}, transition: 0 })
            });
            me.map.addLayer(me.wmsLayer);
            me.map.getView().fit(extent, me.map.getSize());
            me.addCenterButton(extent);
          }
        }
      },
      getResourceType(parameter) {
        if (parameter) {
          const items = Array.isArray(parameter) ? parameter : [parameter];
          return items.filter(i => i.valueName === "ResourceType").map(i => i.value).find(i => true) || null;
        } else {
          return null;
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
      createFeatureFromInfoArea(area, resourceType) {
        const geometry = this.createGeometryFromInfoArea(area);
        if (geometry) {
          const feature = new Feature(geometry);
          const style = this.createStyleFromInfoArea(area, resourceType);
          feature.setStyle(style);
          return feature;
        } else {
          return null;
        }
      },
      createStyleFromInfoArea(area, resourceType) {
        const isCircle = !!area.circle;
        if (isCircle && resourceType) {
          return new Style({
            image: new Icon( ({ src: Urls.ICON_SERVICE + resourceType }))
          });
        } else {
          const styleValue = (area.geocode || []).filter(g => g.valueName === "style").map(g => g.value).find(a => true);
          if (styleValue) {
            return mapStyling.createStyleFromCssString(styleValue);
          } else {
            return null;
          }
        }
      },
      scaleToFeatures() {
        const features = this.vectorSource.getFeatures();
        if (features.length > 0) {
          const extent = this.vectorSource.getExtent();
          this.map.getView().fit(extent, {size: this.map.getSize(), maxZoom: 10});
        } else if (!this.wmsData) {
          const euExtent = this.transform([-27.68862, 33.59717, 43.90757, 71.97626]);
          this.map.getView().fit(euExtent, this.map.getSize());
        }
      },
      addCenterButton(extent) {
        const me = this;
        const button = document.createElement('button');
        button.innerHTML = '&middot;';
        const handleRotateNorth = () => me.map.getView().fit(extent, me.map.getSize());
        button.addEventListener('click', handleRotateNorth.bind(this), false);
        const element = document.createElement('div');
        element.className = 'rotate-north ol-unselectable ol-control';
        element.style.top = "65px";
        element.style.left = "0.5em";
        element.appendChild(button);
        me.centerButtonControl = new Control({ element: element });
        this.map.addControl(me.centerButtonControl);
      },
      removeWmsLayer() {
        if (this.wmsLayer) {
          this.map.removeLayer(this.wmsLayer);
        }
        if (this.centerButtonControl) {
          this.map.removeControl(this.centerButtonControl);
        }
      }
    },
    mounted () {
      const me = this;
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
          }),
          vectorLayer
        ],
        view: new View({
          center: [0, 0],
          zoom: 2
        })
      });

      this.updateFeatures();
    },
  };
</script>
