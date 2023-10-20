package algo;

public class Calculus {

    /**
     * Sigmoid activation function: f(x) = 1 / (1 + e^(-x))
     * 
     * @param x
     * @return
     */
    static public double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * Derivative of sigmoid: f'(x) = f(x) * (1 - f(x))
     * 
     * @param x
     * @return
     */
    static public double derivSigmoid(double x) {
        double fx = sigmoid(x);
        return fx * (1 - fx);
    }

    static public double error(double expected, double output) {
        return .5 * Math.pow(expected - output, 2);
    }
}
