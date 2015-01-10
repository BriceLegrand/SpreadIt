package com.spreadit.radar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.spreadit.radar.CanvasAnimationView.CanvasAnimation;

public class CirclesCanvasAnimation extends CanvasAnimation {
	private final List<Circle> mCircles = new ArrayList<Circle>();

	public CirclesCanvasAnimation(long duration, boolean fillAfter) {
		super(duration, fillAfter);
	}




	@Override
	protected void handle(Canvas canvas, float normalizedTime) {
		if (mCircles.size() == 0) {
			return;
		}

		final Paint paint = new Paint();
		final int alpha = (int) (255 * normalizedTime);

		for (Circle circle: mCircles) {
			final float radius = circle.minRadius + (circle.maxRadius - circle.minRadius) * normalizedTime;

			paint.setColor(circle.color);
			paint.setAlpha(alpha);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(6);

			final RectF rect = new RectF(circle.x - radius, circle.y - radius, circle.x + radius, circle.y + radius);
			canvas.drawOval(rect, paint);
		}
	}

	public void setCircles(Collection<Circle> circles) {
		mCircles.clear();
		mCircles.addAll(circles);
	}

	public static class Circle {
		public final int x;
		public final int y;
		public final int minRadius;
		public final int maxRadius;
		public final int color;

		public Circle(int x, int y, int minRadius, int maxRadius, int color) {
			this.x = x;
			this.y = y;
			this.minRadius = minRadius;
			this.maxRadius = maxRadius;
			this.color = color;
		}
	}
}