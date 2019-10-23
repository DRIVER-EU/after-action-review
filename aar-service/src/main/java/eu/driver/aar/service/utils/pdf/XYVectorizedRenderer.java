package eu.driver.aar.service.utils.pdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYDataset;

public class XYVectorizedRenderer extends XYLineAndShapeRenderer {
	public XYVectorizedRenderer() {
		super();
		setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));
	}
	
	
	public boolean getDrawSeriesLineAsPath() {
		return false;
	}
	
	
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
		
		// do nothing if item is not visible
		if(!getItemVisible(series, item)) {
			return;
		}
		
		// first pass draws the background (lines, for instance)
		if(isLinePass(pass)) {
			if(getItemLineVisible(series, item)) {
				drawPrimaryLine(state, g2, plot, dataset, pass, series,
						item, domainAxis, rangeAxis, dataArea);
			}
		}
		// second pass adds shapes where the items are ..
		else if(isItemPass(pass)) {
			// setup for collecting optional entity info...
			EntityCollection entities = null;
			if(info != null && info.getOwner() != null) {
				entities = info.getOwner().getEntityCollection();
			}
			
			drawSecondaryPass(g2, plot, dataset, pass, series, item,
					domainAxis, dataArea, rangeAxis, crosshairState, entities);
		}
	}
	
	
	protected void drawPrimaryLine(XYItemRendererState state, Graphics2D g2, XYPlot plot, XYDataset dataset,
			int pass, int series, int item, ValueAxis domainAxis, ValueAxis rangeAxis, Rectangle2D dataArea) {
		if(item == 0) {
			return;
		}
		
		// get the data points
		double x1 = dataset.getXValue(series, item);
		double y1 = dataset.getYValue(series, item);
		if(Double.isNaN(y1) || Double.isNaN(x1)) {
			return;
		}
		
		double x0 = dataset.getXValue(series, item - 1);
		double y0 = dataset.getYValue(series, item - 1);
		if(Double.isNaN(y0) || Double.isNaN(x0)) {
			return;
		}
		
		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		
		double pxx0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
		double pxy0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);
		
		double pxx1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
		double pxy1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
		
		// Draw the line between points
		Line2D line;
		PlotOrientation orientation = plot.getOrientation();
		if(orientation.equals(PlotOrientation.HORIZONTAL)) {
			line = new Line2D.Double(pxy0, pxx0, pxy1, pxx1);
		}
		else {
			line = new Line2D.Double(pxx0, pxy0, pxx1, pxy1);
		}
		g2.setPaint(getItemPaint(series, item));
		g2.setStroke(getItemStroke(series, item));
		g2.draw(line);
		
		// Calculate the arrow angle in radians
		double dxx = (pxx1 - pxx0);
		double dyy = (pxy1 - pxy0);
		
		double angle = 0.0;
		if (dxx != 0.0 && dxx < 0 /*&& dyy < 0*/) {
			angle = - Math.PI / 2.0 - Math.atan(dyy / dxx);
		} else if(dxx != 0.0) {
			angle = Math.PI / 2.0 - Math.atan(dyy / dxx);
		} 
		
		// V2 : Draw arrow shape with fixed size instead of line path
		int arrowHeight = 12;
		int arrowBase = 3;
		
		int px_x1 = (int) pxx1;
		int px_y1 = (int) pxy1;
		
		// Create the arrow shape
		int[] xpoints = new int[] {px_x1, px_x1+arrowBase, px_x1-arrowBase};
		int[] ypoints = new int[] {px_y1, px_y1+arrowHeight, px_y1+arrowHeight};
		Polygon arrow = new Polygon(xpoints, ypoints, 3);
		
		//g2.setFont(new Font("Consolas", Font.PLAIN, 12));
		//g2.drawString(String.format("%d °", (int)(angle*180/Math.PI)), px_x1+10, px_y1+10);
		
		// Convert shape to path
		Path2D arrowPath = new Path2D.Double(arrow);
		
		// Apply rotation with AffineTransform to the path
		Rectangle bounds = arrowPath.getBounds();
		Point center = new Point(bounds.x + bounds.width/2, bounds.y + bounds.height/2);
		arrowPath.transform(AffineTransform.getRotateInstance(-angle + Math.PI, px_x1, px_y1));
		
		// Draw the path
		g2.fill(arrowPath);
		if(g2.getPaint() instanceof Color) {
			g2.setPaint(((Color)g2.getPaint()).darker());
			g2.draw(arrowPath);
		}
	}
}
