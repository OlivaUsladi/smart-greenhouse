package ru.greenhouse.server.processing;

public class EmaFilter {

    private final double alpha;
    private Double previous;

    public EmaFilter(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("N должно быть не меньше 1");
        }
        this.alpha = 2.0 / (n + 1);
    }

    public double apply(double value) {
        previous = previous == null ? value : value * alpha + previous * (1 - alpha);
        return previous;
    }

    public void reset() {
        previous = null;
    }

    public double alpha() {
        return alpha;
    }
}
