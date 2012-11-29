/**
 * 
 */
package edu.upenn.cis350.Trace2Learn;

import edu.upenn.cis350.Trace2Learn.Database.Character;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.View;

/**
 * @author Ryan
 *
 */
public class CharacterTracePane extends CharacterCreationPane {

	protected Character _template;
	
	protected Paint _style;
	private static final float _heightToStroke = 12F/400F;
	
	private float _templateTime = 0;
	private float _timeLimit = 0;
	
	private float _strokeTime = 4000;
	
	private long _lastTick;
	private int _curStroke;
	
	private OnTraceCompleteListener _onTraceCompleteListener = null;
	
	Thread _refreshTimer;
	
	protected Handler _handler;
	
	public CharacterTracePane(Context context) {
		super(context);
		_style = new Paint();
		_style.setColor(Color.GRAY);
		_style.setAntiAlias(true);
		_style.setDither(true);
		_style.setStyle(Paint.Style.STROKE);
		_style.setStrokeJoin(Paint.Join.ROUND);
		_style.setStrokeCap(Paint.Cap.ROUND);
		_handler = new Handler();
	}

	public void setTemplate(Character template)
	{
		_template = template;
		setCurrentTraceStroke(0);
	}
	
	@Override
	public void clearPane()
	{
		super.clearPane();
		setCurrentTraceStroke(0);
	}
	
	/**
	 * Builds and starts the animation timer
	 */
	public void startTimer()
	{
		if(_refreshTimer == null)
		{
			_refreshTimer = new Thread()
			{
				Runnable _update = new Runnable() {
					public void run() {
						invalidate();
					}
				};
				
				public void run()
				{
					while(_templateTime < _timeLimit)
					{
						_handler.post(_update);
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							break;
						}
					}
					_refreshTimer = null;
				}
			};
			_refreshTimer.start();
		}
	}
	
	protected void animateTick()
	{
		if(_template != null)
		{
			long time = System.currentTimeMillis();
			long dif = time - _lastTick;
			float strokeTime = dif/(_strokeTime*_template.getNumberOfStrokes());
			_templateTime = Math.min(_templateTime + strokeTime, _timeLimit);
		}
	}
	
	protected void setCurrentTraceStroke(int stroke)
	{
		if(_template != null)
		{
			int numStrokes = _template.getNumberOfStrokes();
			if(stroke < 0) stroke = 0;
			if(stroke >= numStrokes) {
				stroke = numStrokes;
				if (_onTraceCompleteListener != null) {
					try {
						Thread.sleep(500); // wait a little so user can appreciate their work
					} catch (InterruptedException e) {
						// do nothing
					}
					_onTraceCompleteListener.onTraceComplete(getRootView());
				}
			}
			float strokeLen = 1F/numStrokes;
			_templateTime = stroke*strokeLen;
			_timeLimit = (stroke+1)*strokeLen;
			_lastTick = System.currentTimeMillis();
			startTimer();
		}
		_curStroke = stroke;
	}
	
	@Override
	protected void beginStroke(float newX, float newY)
	{
		super.beginStroke(newX, newY);
		completeStrokeAnimation();
	}

	@Override
	protected void completeStroke(float newX, float newY)
	{
		super.completeStroke(newX, newY);
		animateNextStroke();
	}
	
	protected void animateNextStroke() {
		setCurrentTraceStroke(_curStroke+1);
	}

	
	protected void completeStrokeAnimation() {
		_templateTime = _timeLimit;
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{	
		animateTick();
		Log.i("DRAW", "TIME: " + _strokeTime);
		_style.setStrokeWidth(canvas.getHeight()*_heightToStroke);
		canvas.drawColor(_backgroundColor);
		if(_template != null) _template.draw(canvas, _style, _templateTime);
		// Consider using a bitmap buffer so only new strokes are drawn.
		drawCharacter(canvas, _character);
	}
	
	public interface OnTraceCompleteListener {
		public abstract void onTraceComplete(View v);
	}
	
	public void setOnTraceCompleteListener(OnTraceCompleteListener listener) {
		this._onTraceCompleteListener = listener;
	}
	
	public OnTraceCompleteListener getOnTraceCompleteListener() {
		return this._onTraceCompleteListener;
	}
	
}
