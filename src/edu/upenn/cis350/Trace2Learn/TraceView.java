package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis350.Trace2Learn.Database.Stroke;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TraceView extends View {
	
	private float moveTolerance;

	private List<StrokeDrawable> modelStrokes = new ArrayList<StrokeDrawable>();
	private List<StrokeDrawable> userStrokes = new ArrayList<StrokeDrawable>();

	private StrokeDrawable currentStroke;

	private String modelGlyph;

	private Bitmap backgroundBitmap;

	private Canvas backgroundCanvas;

	private boolean readOnly = false;
	private boolean useMask = false;

	private int size;
	private Rect clipBounds = new Rect();
	private RectF sizeRect = new RectF();

	private Paint borderPaint = new Paint();
	private Paint gridPaint = new Paint();
	private Paint maskPaint = new Paint();
	private Paint modelTracePaint = new Paint();
	private Paint userTracePaint = new Paint();
	
	private float lastSampledX = -1;
	private float lastSampledY = -1;

	public TraceView(Context context) {
		super(context);
	}

	private void setupPaints() {
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setColor(Color.GRAY);
		borderPaint.setStrokeWidth(0); //hair-line
		
		gridPaint.set(borderPaint);
		gridPaint.setPathEffect(new DashPathEffect(new float[] {size / 20F, size / 20F}, 0));		
		
		modelTracePaint.setAntiAlias(true);
		modelTracePaint.setStyle(Paint.Style.STROKE);
		modelTracePaint.setColor(Color.RED);
		modelTracePaint.setStrokeWidth(size / 40F);
		modelTracePaint.setStrokeJoin(Paint.Join.ROUND);
		modelTracePaint.setStrokeCap(Paint.Cap.ROUND);
		
		userTracePaint.setColor(Color.BLACK);
		
		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
	}

	public TraceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TraceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.getClipBounds(clipBounds);
		canvas.drawColor(Color.GRAY); //TODO: make customizable
		if(useMask && modelGlyph != null) {
			drawStrokes(canvas, modelStrokes);
			drawStrokes(canvas, userStrokes);
			canvas.drawBitmap(backgroundBitmap, clipBounds, clipBounds, null);
		} else {
			canvas.drawBitmap(backgroundBitmap, clipBounds, clipBounds, null);
			drawStrokes(canvas, modelStrokes);
			drawStrokes(canvas, userStrokes);
		}
	}
	
	private void drawStrokes(Canvas canvas, List<StrokeDrawable> strokes) {
		for(StrokeDrawable stroke : strokes) {
			stroke.draw(canvas);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int wSize = MeasureSpec.getSize(widthMeasureSpec);
		int hSize = MeasureSpec.getSize(heightMeasureSpec);
		int wMode = MeasureSpec.getMode(widthMeasureSpec);
		int hMode = MeasureSpec.getMode(heightMeasureSpec);
		int resultSpec = MeasureSpec.UNSPECIFIED;
		if (wMode == MeasureSpec.UNSPECIFIED && hMode == MeasureSpec.UNSPECIFIED) {
			resultSpec = MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY); // default size
		} else if ((wMode == MeasureSpec.UNSPECIFIED || hMode == MeasureSpec.UNSPECIFIED)) {
			// only one of the dimensions has been specified so we choose one that is specified
			resultSpec = (wMode != MeasureSpec.UNSPECIFIED) ? widthMeasureSpec : heightMeasureSpec;
		} else if (wMode == hMode) {
			// both dimensions have been specified using the same mode
			resultSpec = (wSize > hSize) ? widthMeasureSpec : heightMeasureSpec;
		} else {
			// we got one EXACTLY and one AT_MOST mode, EXACT prevails
			resultSpec = (wMode == MeasureSpec.EXACTLY) ? widthMeasureSpec : heightMeasureSpec;
		}
		super.onMeasure(resultSpec, resultSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// onMeasure ensured w == h
		this.size = w;
		moveTolerance = w / 120F;
		sizeRect.set(0, 0, w, h);
		
		setupCanvases(); 
		setupPaints();
		
		refreshBackgroundBitmap();
		
		for(StrokeDrawable stroke : modelStrokes) {
			stroke.setBounds(sizeRect);
		}
		for(StrokeDrawable stroke : userStrokes) {
			stroke.setBounds(sizeRect);
		}
	}

	private void setupCanvases() {
		backgroundBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

		backgroundCanvas = new Canvas(backgroundBitmap);
	}
	
	private void refreshBackgroundBitmap() {
		backgroundCanvas.drawColor(Color.WHITE); // TODO: make customizable
//		backgroundCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		backgroundCanvas.drawRect(0, 0, size, size, borderPaint);
		// draw cross grid
		backgroundCanvas.drawLine(size / 2F, 0, size / 2F, size, gridPaint);
		backgroundCanvas.drawLine(0, size / 2F, size, size / 2F, gridPaint);
		
		if (modelGlyph != null) {
			Rect bounds = new Rect();
			maskPaint.getTextBounds(modelGlyph, 0, modelGlyph.length(), bounds);
			backgroundCanvas.drawText(modelGlyph, (size - bounds.width()) / 2F, (size - bounds.height()) / 2F, maskPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!readOnly) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				beginRecordingStroke(x, y);
				break;
			case MotionEvent.ACTION_MOVE:
				updateRecordingStroke(x, y);
				break;
			case MotionEvent.ACTION_UP:
				endRecordingStroke(x, y);
				break;
			}
		}
		return super.onTouchEvent(event);
	}
	
	public void beginRecordingStroke(float x, float y) {
		currentStroke = new StrokeDrawable(sizeRect, userTracePaint);
		currentStroke.addPoint(x, y);
		userStrokes.add(currentStroke);
		lastSampledX = x;
		lastSampledY = y;
	}
	
	public void updateRecordingStroke(float x, float y) {
		if(currentStroke == null) {
			beginRecordingStroke(x, y);
			return;
		}
		if(x > size || y > size || x < 0 || y < 0) {
			endRecordingStroke(x, y);
			return;
		}
		
		float dx = Math.abs(x - lastSampledX);
		float dy = Math.abs(y - lastSampledY);

		// Don't sample if the movement is negligible in length
		if (dx >= moveTolerance || dy >= moveTolerance) {
			currentStroke.addPoint(x, y); // normalize to [0, 1]
			lastSampledX = x;
			lastSampledY = y;
			invalidate();
		}
	}
	
	public void endRecordingStroke(float x, float y) {
		if (currentStroke.getStroke().getNumberOfPoints() < 2) {
			userStrokes.remove(currentStroke);
		}
		currentStroke = null;
	}
	
	public List<Stroke> getUserStrokes() {
		List<Stroke> result = new ArrayList<Stroke>();
		for(StrokeDrawable stroke: userStrokes) {
			result.add(stroke.getStroke());
		}
		return result;
	}
	
	public void setModelStrokes(List<Stroke> strokes) {
		modelStrokes.clear();
		for(Stroke stroke : strokes) {
			modelStrokes.add(new StrokeDrawable(sizeRect, modelTracePaint, stroke));
		}
	}
	
	public void clearUserStrokes() {
		userStrokes.clear();
		invalidate();
	}
	
	public String getModelGlyph() {
		return modelGlyph;
	}

	public void setModelGlyph(String modelGlyph) {
		if(modelGlyph == null || modelGlyph.trim().length() == 0) {
			this.modelGlyph = null;
		} else {
			this.modelGlyph = modelGlyph;
			// recalculate text size
			maskPaint.setTextSize(size);
			Rect bounds = new Rect();
			maskPaint.getTextBounds(modelGlyph, 0, modelGlyph.length(), bounds);
			float longEdgeLength = Math.max(bounds.width(), bounds.height());
			float targetLength = size * 0.9F;
			float scaledSize = (targetLength / longEdgeLength ) * size;
			maskPaint.setTextSize(scaledSize);
		}
		refreshBackgroundBitmap();
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isUseMask() {
		return useMask;
	}

	public void setUseMask(boolean useMask) {
		this.useMask = useMask;
	}

}
