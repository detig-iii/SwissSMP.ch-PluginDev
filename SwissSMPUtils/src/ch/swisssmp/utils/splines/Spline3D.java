package ch.swisssmp.utils.splines;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

public class Spline3D {
	public Spline3D(Vector... points) {
		count = points.length;

		float[] x = new float[count];
		float[] y = new float[count];
		float[] z = new float[count];

		for (int i = 0; i < count; i++) {
			x[i] = (float)points[i].getX();
			y[i] = (float)points[i].getY();
			z[i] = (float)points[i].getZ();
		}

		this.x = Curve.calcCurve(count - 1, x);
		this.y = Curve.calcCurve(count - 1, y);
		this.z = Curve.calcCurve(count - 1, z);
	}

	final int count;
	private final Cubic[] x, y, z;

	/**
	 * POINT COUNT
	 */

	public final int pointCount() {
		return count;
	}

	/**
	 * POSITION
	 */

	public final Vector getPositionAt(float param) {
		Vector v = new Vector();
		this.getPositionAt(param, v);
		return v;
	}

	public final void getPositionAt(float param, Vector result) {
		// clamp
		if (param < 0.0f)
			param = 0.0f;
		if (param >= count - 1)
			param = (count - 1) - Math.ulp(count - 1);

		// split
		int ti = (int) param;
		float tf = param - ti;

		// eval
		result.setX(x[ti].eval(tf));
		result.setY(y[ti].eval(tf));
		result.setZ(z[ti].eval(tf));
	}

	private List<CacheItem> travelCache;
	private float maxTravelStep;
	private float posStep;

	public void enabledTripCaching(float maxTravelStep, float posStep) {
		this.maxTravelStep = maxTravelStep;
		this.posStep = posStep;

		float x = this.x[0].eval(0.0f);
		float y = this.y[0].eval(0.0f);
		float z = this.z[0].eval(0.0f);

		this.travelCache = new ArrayList<CacheItem>();
		this.travelCache.add(new CacheItem(new Vector(x, y, z)));
	}

	public Vector getTripPosition(float totalTrip) {
		CacheItem last = this.travelCache.get(this.travelCache.size() - 1);

		// build cache
		while (last.travelled < totalTrip) {
			if (totalTrip == 0.0f) {
				// don't even bother
				break;
			}

			float travel = Math.min(totalTrip - last.travelled, maxTravelStep);

			CacheItem curr = this.getSteppingPosition(last.position, travel,
					posStep);

			if (curr.position >= this.count) {
				// reached end of spline
				break;
			}

			// only cache if we travelled far enough
			if (curr.travelled > this.maxTravelStep * 0.95f) {
				this.travelCache.add(curr);
			}

			curr.travelled += last.travelled;

			last = curr;
		}

		// figure out position

		int lo = 0;
		int hi = this.travelCache.size() - 1;

		while (true) {
			int mid = (lo + hi) / 2;

			last = this.travelCache.get(mid);

			if (last.travelled < totalTrip) {
				if (lo == mid)
					break;
				lo = mid;
			} else {
				if (hi == mid)
					break;
				hi = mid;
			}
		}

		for (int i = lo; i <= hi; i++) {
			CacheItem item = this.travelCache.get(i);

			if (item.travelled <= totalTrip) {
				last = item;
			} else {
				break;
			}
		}

		float travel = totalTrip - last.travelled;
		last = this.getSteppingPosition(last.position, travel, posStep);
		return last.pos;
	}

	private CacheItem getSteppingPosition(float posOffset, float travel,
			float segmentStep) {
		float pos = posOffset;
		Vector last = this.getPositionAt(pos);

		float travelled = 0.0f;

		while (travelled < travel && pos < this.count) {
			Vector curr = this.getPositionAt(pos += segmentStep);
			travelled += Spline3D.dist(last, curr);
			last = curr;
		}

		CacheItem item = new CacheItem(last);
		item.position = pos;
		item.travelled = travelled;
		return item;
	}

	private static float dist(Vector a, Vector b) {
		return (float)a.distance(b);
	}

	/**
	 * CURVE CLASS
	 */

	private static class Curve {
		static final Cubic[] calcCurve(int n, float[] axis) {
			float[] gamma = new float[n + 1];
			float[] delta = new float[n + 1];
			float[] d = new float[n + 1];
			Cubic[] c = new Cubic[n];

			// gamma
			gamma[0] = 0.5F;
			for (int i = 1; i < n; i++)
				gamma[i] = 1.0F / (4.0F - gamma[i - 1]);
			gamma[n] = 1.0F / (2.0F - gamma[n - 1]);

			// delta
			delta[0] = 3.0F * (axis[1] - axis[0]) * gamma[0];
			for (int i = 1; i < n; i++)
				delta[i] = (3.0F * (axis[i + 1] - axis[i - 1]) - delta[i - 1])
						* gamma[i];
			delta[n] = (3.0F * (axis[n] - axis[n - 1]) - delta[n - 1])
					* gamma[n];

			// d
			d[n] = delta[n];
			for (int i = n - 1; i >= 0; i--)
				d[i] = delta[i] - gamma[i] * d[i + 1];

			// c
			for (int i = 0; i < n; i++) {
				float x0 = axis[i];
				float x1 = axis[i + 1];
				float d0 = d[i];
				float d1 = d[i + 1];
				c[i] = new Cubic(x0, d0, 3.0F * (x1 - x0) - 2.0F * d0 - d1,
						2.0F * (x0 - x1) + d0 + d1);
			}
			return c;
		}
	}

	/**
	 * CUBIC CLASS
	 */

	static class Cubic {
		private final float a, b, c, d;

		Cubic(float a, float b, float c, float d) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}

		final float eval(float u) {
			return (((d * u) + c) * u + b) * u + a;
		}
	}
}