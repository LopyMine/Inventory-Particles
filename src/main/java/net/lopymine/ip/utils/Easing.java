package net.lopymine.ip.utils;

// https://nicmulvaney.com/easing
@SuppressWarnings("unused")
public final class Easing {

	public static double easeInSine(double x) {
		return 1 - Math.cos((x * Math.PI) / 2);
	}

	public static double easeOutSine(double x) {
		return Math.sin((x * Math.PI) / 2);
	}

	public static double easeInOutSine(double x) {
		return -(Math.cos(Math.PI * x) - 1) / 2;
	}

	public static double easeInQuad(double x) {
		return x * x;
	}

	public static double easeOutQuad(double x) {
		return 1 - (1 - x) * (1 - x);
	}

	public static double easeInOutQuad(double x) {
		return x < 0.5 ? 2 * x * x : (1 - Math.pow(-2 * x + 2, 2) / 2);
	}

	public static double easeInCubic(double x) {
		return x * x * x;
	}

	public static double easeOutCubic(double x) {
		return 1 - Math.pow(1 - x, 3);
	}

	public static double easeInOutCubic(double x) {
		return x < 0.5 ? 4 * x * x * x : (1 - Math.pow(-2 * x + 2, 3) / 2);
	}

	public static double easeInQuart(double x) {
		return x * x * x * x;
	}

	public static double easeOutQuart(double x) {
		return 1 - Math.pow(1 - x, 4);
	}

	public static double easeInOutQuart(double x) {
		return x < 0.5 ? 8 * Math.pow(x, 4) : 1 - Math.pow(-2 * x + 2, 4) / 2;
	}

	public static double easeInQuint(double x) {
		return x * x * x * x * x;
	}

	public static double easeOutQuint(double x) {
		return 1 - Math.pow(1 - x, 5);
	}

	public static double easeInOutQuint(double x) {
		return x < 0.5 ? 16 * Math.pow(x, 5) : 1 - Math.pow(-2 * x + 2, 5) / 2;
	}

	public static double easeInExpo(double x) {
		return x == 0 ? 0 : Math.pow(2, 10 * x - 10);
	}

	public static double easeOutExpo(double x) {
		return x == 1 ? 1 : (1 - Math.pow(2, -10 * x));
	}

	public static double easeInOutExpo(double x) {
		if (x == 0) return 0;
		if (x == 1) return 1;
		return x < 0.5
				? Math.pow(2, 20 * x - 10) / 2
				: (2 - Math.pow(2, -20 * x + 10)) / 2;
	}

	public static double easeInCirc(double x) {
		return 1 - Math.sqrt(1 - Math.pow(x, 2));
	}

	public static double easeOutCirc(double x) {
		return Math.sqrt(1 - Math.pow(x - 1, 2));
	}

	public static double easeInOutCirc(double x) {
		return x < 0.5
				? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
				: (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;
	}

	public static double easeInBack(double x) {
		final double c1 = 1.70158;
		final double c3 = c1 + 1;
		return c3 * x * x * x - c1 * x * x;
	}

	public static double easeOutBack(double x) {
		final double c1 = 1.70158;
		final double c3 = c1 + 1;
		return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
	}

	public static double easeInOutBack(double x) {
		final double c1 = 1.70158;
		final double c2 = c1 * 1.525;
		if (x < 0.5) {
			return (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2;
		} else {
			return (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (2 * x - 2) + c2) + 2) / 2;
		}
	}

	public static double easeInElastic(double x) {
		final double c4 = (2 * Math.PI) / 3;
		if (x == 0) return 0;
		if (x == 1) return 1;
		return -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4);
	}

	public static double easeOutElastic(double x) {
		final double c4 = (2 * Math.PI) / 3;
		if (x == 0) return 0;
		if (x == 1) return 1;
		return Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1;
	}

	public static double easeInOutElastic(double x) {
		final double c5 = (2 * Math.PI) / 4.5;
		if (x == 0) return 0;
		if (x == 1) return 1;
		double sin = Math.sin((20 * x - 11.125) * c5);
		return x < 0.5
				? -(Math.pow(2, 20 * x - 10) * sin) / 2
				: (Math.pow(2, -20 * x + 10) * sin) / 2 + 1;
	}

	public static double easeInBounce(double x) {
		return 1 - easeOutBounce(1 - x);
	}

	public static double easeOutBounce(double x) {
		final double n1 = 7.5625;
		final double d1 = 2.75;
		double t = x;
		if (t < 1 / d1) {
			return n1 * t * t;
		} else if (t < 2 / d1) {
			t -= 1.5 / d1;
			return n1 * t * t + 0.75;
		} else if (t < 2.5 / d1) {
			t -= 2.25 / d1;
			return n1 * t * t + 0.9375;
		} else {
			t -= 2.625 / d1;
			return n1 * t * t + 0.984375;
		}
	}

	public static double easeInOutBounce(double x) {
		return x < 0.5
				? (1 - easeOutBounce(1 - 2 * x)) / 2
				: (1 + easeOutBounce(2 * x - 1)) / 2;
	}
}
