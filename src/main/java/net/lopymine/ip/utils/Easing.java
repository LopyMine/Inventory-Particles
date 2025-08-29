package net.lopymine.ip.utils;

// https://nicmulvaney.com/easing
@SuppressWarnings("unused")
public final class Easing {

	public static float easeInSine(float x) {
		return (float) (1 - Math.cos((x * Math.PI) / 2));
	}

	public static float easeOutSine(float x) {
		return (float) Math.sin((x * Math.PI) / 2);
	}

	public static float easeInOutSine(float x) {
		return (float) (-(Math.cos(Math.PI * x) - 1) / 2);
	}

	public static float easeInQuad(float x) {
		return x * x;
	}

	public static float easeOutQuad(float x) {
		return 1 - (1 - x) * (1 - x);
	}

	public static float easeInOutQuad(float x) {
		return x < 0.5 ? 2 * x * x : (float) (1 - Math.pow(-2 * x + 2, 2) / 2);
	}

	public static float easeInCubic(float x) {
		return x * x * x;
	}

	public static float easeOutCubic(float x) {
		return (float) (1 - Math.pow(1 - x, 3));
	}

	public static float easeInOutCubic(float x) {
		return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
	}

	public static float easeInQuart(float x) {
		return x * x * x * x;
	}

	public static float easeOutQuart(float x) {
		return (float) (1 - Math.pow(1 - x, 4));
	}

	public static float easeInOutQuart(float x) {
		return (float) (x < 0.5 ? 8 * Math.pow(x, 4) : 1 - Math.pow(-2 * x + 2, 4) / 2);
	}

	public static float easeInQuint(float x) {
		return x * x * x * x * x;
	}

	public static float easeOutQuint(float x) {
		return (float) (1 - Math.pow(1 - x, 5));
	}

	public static float easeInOutQuint(float x) {
		return (float) (x < 0.5 ? 16 * Math.pow(x, 5) : 1 - Math.pow(-2 * x + 2, 5) / 2);
	}

	public static float easeInExpo(float x) {
		return x == 0 ? 0 : (float) Math.pow(2, 10 * x - 10);
	}

	public static float easeOutExpo(float x) {
		return x == 1 ? 1 : (float) (1 - Math.pow(2, -10 * x));
	}

	public static float easeInOutExpo(float x) {
		if (x == 0) return 0;
		if (x == 1) return 1;
		return (float) (x < 0.5
				? Math.pow(2, 20 * x - 10) / 2
				: (2 - Math.pow(2, -20 * x + 10)) / 2);
	}

	public static float easeInCirc(float x) {
		return (float) (1 - Math.sqrt(1 - Math.pow(x, 2)));
	}

	public static float easeOutCirc(float x) {
		return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
	}

	public static float easeInOutCirc(float x) {
		return (float) (x < 0.5
				? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
				: (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2);
	}

	public static float easeInBack(float x) {
		final double c1 = 1.70158;
		final double c3 = c1 + 1;
		return (float) (c3 * x * x * x - c1 * x * x);
	}

	public static float easeOutBack(float x) {
		final double c1 = 1.70158;
		final double c3 = c1 + 1;
		return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
	}

	public static float easeInOutBack(float x) {
		final double c1 = 1.70158;
		final double c2 = c1 * 1.525;
		if (x < 0.5) {
			return (float) ((Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2);
		} else {
			return (float) ((Math.pow(2 * x - 2, 2) * ((c2 + 1) * (2 * x - 2) + c2) + 2) / 2);
		}
	}

	public static float easeInElastic(float x) {
		final double c4 = (2 * Math.PI) / 3;
		if (x == 0) return 0;
		if (x == 1) return 1;
		return (float) (-Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4));
	}

	public static float easeOutElastic(float x) {
		final double c4 = (2 * Math.PI) / 3;
		if (x == 0) return 0;
		if (x == 1) return 1;
		return (float) (Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
	}

	public static float easeInOutElastic(float x) {
		final double c5 = (2 * Math.PI) / 4.5;
		if (x == 0) return 0;
		if (x == 1) return 1;
		double sin = Math.sin((20 * x - 11.125) * c5);
		return (float) (x < 0.5
				? -(Math.pow(2, 20 * x - 10) * sin) / 2
				: (Math.pow(2, -20 * x + 10) * sin) / 2 + 1);
	}

	public static float easeInBounce(float x) {
		return 1 - easeOutBounce(1 - x);
	}

	public static float easeOutBounce(float x) {
		final double n1 = 7.5625;
		final double d1 = 2.75;
		double t = x;
		if (t < 1 / d1) {
			return (float) (n1 * t * t);
		} else if (t < 2 / d1) {
			t -= 1.5 / d1;
			return (float) (n1 * t * t + 0.75);
		} else if (t < 2.5 / d1) {
			t -= 2.25 / d1;
			return (float) (n1 * t * t + 0.9375);
		} else {
			t -= 2.625 / d1;
			return (float) (n1 * t * t + 0.984375);
		}
	}

	public static float easeInOutBounce(float x) {
		return x < 0.5
				? (1 - easeOutBounce(1 - 2 * x)) / 2
				: (1 + easeOutBounce(2 * x - 1)) / 2;
	}
}
