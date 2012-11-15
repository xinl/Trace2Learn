package edu.upenn.cis350.Trace2Learn;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;

public class StrokeDrawable {
	private Stroke stroke;
	private Path path = new Path();
	private Matrix matrixFromOriginal = new Matrix();
	private Matrix matrixToOriginal = new Matrix();
	static private RectF originalBounds = new RectF(0F, 0F, 1F, 1F);
	private PathMeasure pathMeasure = new PathMeasure();
	private float[] drawnInterval = new float[] {0, 0}; // {on, off}
	private Paint paint = new Paint();

	public StrokeDrawable() {
		
	}

	public StrokeDrawable(RectF bounds) {
		this();
		setBounds(bounds);
	}
	
	public StrokeDrawable(RectF bounds, Paint paint) {
		this(bounds);
		setPaint(paint);
		
	}
	
	public StrokeDrawable(RectF bounds, Paint paint, Stroke stroke) {
		this(bounds, paint);
		setStroke(stroke);
	}

	public void setBounds(RectF bounds) {
		matrixFromOriginal.setRectToRect(originalBounds, bounds, Matrix.ScaleToFit.CENTER);
		matrixToOriginal.setRectToRect(bounds, originalBounds, Matrix.ScaleToFit.CENTER);
		refreshPath();
	}

	public void addPoint(float x, float y) {
		if(stroke.getNumberOfPoints() == 0) {
			path.moveTo(x, y);
		} else {
			path.lineTo(x, y);
		}
		float[] point = new float[] {x, y};
		matrixToOriginal.mapPoints(point);
		stroke.addPoint(point[0], point[1]);
		updatePathMeasure();
	}
	
	public void draw(Canvas canvas) {
		canvas.drawPath(path, paint);
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
		refreshPath();
	}
	
	public float getPathLength() {
		return pathMeasure.getLength();
	}
	
	public void setPathDrawnLength(float length) {
		length = Math.min(length, getPathLength());
		drawnInterval[0] = length;
		drawnInterval[1] = getPathLength() - length;
		paint.setPathEffect(new DashPathEffect(drawnInterval, 0f));
	}
	
	public float getPathDrawnLength() {
		return drawnInterval[0];
	}
	
	public void setPaint(Paint paint) {
		this.paint.set(paint);
		this.paint.setPathEffect(new DashPathEffect(drawnInterval, 0f));
	}
	
	public Paint getPaint() {
		return paint;
	}
	
	private void refreshPath() {
		path.rewind();
		if (stroke == null || stroke.getNumberOfPoints() < 2)
			return;
		List<PointF> points = stroke.getAllPoints();
		path.moveTo(points.get(0).x, points.get(0).y);
		for (int i = 1; i < points.size(); i++) {
			path.lineTo(points.get(i).x, points.get(i).y);
		}
		path.transform(matrixFromOriginal);
		updatePathMeasure();
	}
	
	private void updatePathMeasure() {
		pathMeasure.setPath(path, false);
		drawnInterval[1] = pathMeasure.getLength();
		setPathDrawnLength(getPathLength()); // reveal the entire path on update
	}

}
