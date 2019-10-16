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

  constructor () {
    this.stylingFunction = this.stylingFunction.bind(this);
  }

  stylingFunction (feature) {
    return this.styles[feature.getGeometry().getType()];
  };

  /**
   * Parses CSS string into a map style.
   * Supported: stroke:#3366FF;stroke-width:3px;stroke-opacity:1.0;fill:#FF0000;fill-opacity:0.4
   *
   * @param cssString string to parse
   * @returns {Style} map style
   */
  createStyleFromCssString (cssString) {
    const styleMap = this.parseCssStringIntoMap(cssString);
    const strokeColor = this.createColor(styleMap['stroke'], styleMap['stroke-opacity']) || this.strokeColor;
    const strokeWidth = this.toInt(styleMap['stroke-width']) || this.strokeWidth;
    const areaFill = this.createColor(styleMap['fill'], styleMap['fill-opacity']);
    return new Style({
      stroke: new Stroke({
        color: strokeColor,
        width: strokeWidth
      }),
      fill: new Fill({
        color: areaFill
      })
    });
  };

  createColor (colorBaseString, colorOpacity) {
    const colorBase = this.parseColor(colorBaseString);
    if (colorBase) {
      const opacity = colorOpacity !== null && colorOpacity !== undefined ? parseFloat(colorOpacity) : 1.0;
      return 'rgba(' + colorBase[0] + ',' + colorBase[1] + ',' + colorBase[2] + ',' + opacity + ')';
    } else {
      return null;
    }
  }

  toInt (string) {
    if (string) {
      return parseInt(string);
    } else {
      return null;
    }
  }

  parseCssStringIntoMap (cssString) {
    const styleMap = {};
    for (const keyValueString of cssString.split(';')) {
      const keyValue = keyValueString.split(':');
      if (keyValue.length === 2) {
        styleMap[keyValue[0]] = keyValue[1];
      }
    }
    return styleMap;
  };

  parseColor (colorString) {
    if (colorString) {
      const body = document.getElementsByTagName('body')[0];
      const div = document.createElement('div');
      body.appendChild(div);
      div.style.color = colorString;
      const m = getComputedStyle(div).color.match(/^rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)$/i);
      body.removeChild(div);
      if (m) {
        return [parseInt(m[1]), parseInt(m[2]), parseInt(m[3])];
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}

const mapStyling = MapStylingService.getInstance();

export {MapStylingService, mapStyling};
