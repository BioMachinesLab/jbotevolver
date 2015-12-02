/*
 * Created on Sep 30, 2004
 *
 */
package evolutionaryrobotics.evolution.neat.nn.core.functions;

import net.jafama.FastMath;
import evolutionaryrobotics.evolution.neat.nn.core.ActivationFunction;

/**
 * @author MSimmerson
 *
 */
public class TanhFunction implements ActivationFunction {

	/**
	 * @see org.neat4j.ailibrary.nn.core.ActivationFunction#activate(double)
	 */
	public double activate(double neuronIp) {
		double op;
		if (neuronIp < -20) {
			op = -1;
		} else if (neuronIp > 20) {
			op = 1;
		} else {
			op = (1 - FastMath.expQuick(-2 * neuronIp)) / (1 + FastMath.expQuick(-2 * neuronIp)); // TODO: expQuick
		}
		return (op);
	}

	public double derivative(double neuronIp) {
		double deriv = 0;
		deriv = (1 - FastMath.pow2(neuronIp));
		return (deriv);
	}
}
