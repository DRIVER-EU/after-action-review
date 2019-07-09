import CircleStyle from 'ol/style/Circle';
import Fill from 'ol/style/Fill';
import Stroke from 'ol/style/Stroke';
import Style from 'ol/style/Style';

class MapStylingService {
  static INSTANCE = new MapStylingService();

  circleRadius = 5;

  strokeColor = 'red';

  strokeWidth = 1;

  areaFill = 'rgba(255, 255, 255, 0.3)';

  image = new CircleStyle({
    radius: this.circleRadius,
    fill: null,
    stroke: new Stroke({
      color: this.strokeColor,
      width: this.strokeWidth
    })
  });

  styles = {
    'Point': new Style({
      image: this.image
    }),
    'LineString': new Style({
      stroke: new Stroke({
        color: this.strokeColor,
        width: this.strokeWidth
      })
    }),
    'MultiLineString': new Style({
      stroke: new Stroke({
        color: this.strokeColor,
        width: this.strokeWidth
      })
    }),
    'MultiPoint': new Style({
      image: this.image
    }),
    'MultiPolygon': new Style({
      stroke: new Stroke({
        color: this.strokeColor,
        width: this.strokeWidth
      }),
      fill: new Fill({
        color: this.areaFill
      })
    }),
    'Polygon': new Style({
      stroke: new Stroke({
        color: this.strokeColor,
        width: this.strokeWidth
      }),
      fill: new Fill({
        color: this.areaFill
      })
    }),
    'GeometryCollection': new Style({
      stroke: new Stroke({
        color: this.strokeColor,
        width: this.strokeWidth
      }),
      fill: new Fill({
        color: this.areaFill
      }),
      image: this.image
    }),
    'Circle': new Style({
      stroke: new Stroke({
        color: this.strokeColor,
        width: this.strokeWidth
      }),
      fill: new Fill({
        color: this.areaFill
      }),
    })
  };

  static getInstance () {
    return MapStylingService.INSTANCE;
  }

  constructor() {
    this.stylingFunction = this.stylingFunction.bind(this);
  }

  stylingFunction(feature) {
    return this.styles[feature.getGeometry().getType()];
  };
}

const mapStyling = MapStylingService.getInstance();

export {MapStylingService, mapStyling};
