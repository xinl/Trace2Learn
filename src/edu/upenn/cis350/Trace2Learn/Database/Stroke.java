package edu.upenn.cis350.Trace2Learn.Database;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;

public class Stroke {

	List<PointF> points = new ArrayList<PointF>();

	static private float MOVE_TO_BLOCK = -1F;

	public Stroke() {

	}

	/**
	 * creates a new strokes which starts at (startX, startY)
	 */
	public Stroke(float startX, float startY) {
		this(new PointF(startX, startY));
	}

	/**
	 * creates a new strokes which starts at startP
	 * 
	 * @param startP
	 */
	public Stroke(PointF startP) {
		this();
		addPoint(startP);
	}

	public synchronized int getNumberOfPoints() {
		return points.size();
	}

	/**
	 * @return a list of points sampled to represent the stroke
	 */
	public synchronized List<PointF> getAllPoints() {
		return new ArrayList<PointF>(points);
	}

	public synchronized void addPoint(float x, float y) {
		addPoint(new PointF(x, y));
	}

	public synchronized void addPoint(PointF p) {
		points.add(p);
	}

	/**
	 * @return A path representation of the stroke which can be drawn on-screen
	 */
	public synchronized Path toPath(float time) {
		if (time > 1)
			return toPath();
		Path path = new Path();
		if (points.size() <= 0) {
			return path;
		} else if (points.size() == 1) {
			PointF p = points.get(0);
			path.moveTo(p.x, p.y);
			return path;
		}
		float pTime = 1F / points.size();
		if (time < pTime)
			return path;
		Iterator<PointF> iter = points.iterator();
		PointF p1 = iter.next();
		PointF p2 = iter.next();
		path.moveTo(p1.x, p1.y);
		float covered = pTime;
		if (covered <= time && iter.hasNext()) {
			p1 = p2;
			p2 = iter.next();
			covered += pTime;
		}

		while (covered <= time && iter.hasNext()) {
			path.quadTo(p1.x, p1.y, (p2.x + p1.x) / 2, (p2.y + p1.y) / 2);
			p1 = p2;
			p2 = iter.next();
			covered += pTime;
		}

		path.lineTo(p2.x, p2.y);
		return new Path(path);
	}

	public Path toPath(Matrix transform) {
		Path p = toPath(1);
		p.transform(transform);
		return p;
	}

	public Path toPath(Matrix transform, float time) {
		Path p = toPath(time);
		p.transform(transform);
		return p;
	}

	public Path toPath() {
		return toPath(1);
	}

	/**
	 * Decode binary strokes data into a list of Stroke objects.
	 * 
	 * @param data
	 *            A byte array holding encoded binary strokes data
	 * @return A list of Stroke objects or null if data is empty
	 * @throws IllegalArgumentException
	 *             when input data format is incorrect.
	 */
	static public List<Stroke> decodeStrokesData(byte[] data) throws IllegalArgumentException {
		if (data == null || data.length == 0) {
			return null;
		}
		if (data.length % 4 != 0) {
			throw new IllegalArgumentException("Data not in 32-bit blocks.");
		}
		if (data.length <= 12) {
			throw new IllegalArgumentException("Too few blocks.");
		}

		ByteBuffer buf = ByteBuffer.wrap(data);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		List<Stroke> strokes = new ArrayList<Stroke>();

		while (buf.hasRemaining()) {
			strokes.add(parseStroke(buf));
		}
		return strokes;

	}

	static private Stroke parseStroke(ByteBuffer buf) {
		float block = buf.getFloat();
		if (block == MOVE_TO_BLOCK) {
			Stroke stroke = new Stroke();
			stroke.addPoint(parsePoint(buf));
			while (buf.hasRemaining()) {
				float nextFloat = peekNextFloat(buf);
				if (nextFloat >= 0 && nextFloat <= 1) {
					stroke.addPoint(parsePoint(buf));
				} else {
					break;
				}
			}
			return stroke;
		} else {
			throw new IllegalArgumentException("Expected MoveTo at byte " + buf.position());
		}
	}

	static private PointF parsePoint(ByteBuffer buf) {
		float x = buf.getFloat();
		if (x >= 0 && x <= 1) {
			float y = buf.getFloat();
			if (y >= 0 && y <= 0) {
				return new PointF(x, y);
			} else {
				throw new IllegalArgumentException("Expect Y coordinate at byte " + buf.position());
			}
		} else {
			throw new IllegalArgumentException("Expect X coordinate at byte " + buf.position());
		}
	}

	static private float peekNextFloat(ByteBuffer buf) {
		buf.mark();
		float f = buf.getFloat();
		buf.reset();
		return f;
	}

	/**
	 * Encode a list of Strokes into byte array format.
	 * 
	 * @param strokes
	 *            A list of Strokes.
	 * @return A byte array of encoded stroke data.
	 */
	static public byte[] encodeStrokesData(List<Stroke> strokes) {
		if (strokes == null || strokes.size() == 0) {
			return null;
		}
		int size = 0;
		for (Stroke stroke : strokes) {
			size += 1; // one block for MoveTo per stroke
			size += stroke.getNumberOfPoints() * 2; // two blocks for X, Y coordinates per point
		}
		if (size < 2) { // there's no point in it (pun intended)
			return null;
		}
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		for (Stroke stroke : strokes) {
			buf.putFloat(MOVE_TO_BLOCK);
			for (PointF point : stroke.points) {
				buf.putFloat(point.x);
				buf.putFloat(point.y);
			}
		}
		return buf.array();
	}

}
