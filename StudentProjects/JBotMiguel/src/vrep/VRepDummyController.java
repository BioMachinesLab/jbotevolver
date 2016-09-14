package vrep;

public class VRepDummyController extends VRepController {

    protected float[] locomotionParameters;

    public VRepDummyController(float[] parameters) {
        super(parameters);
        // just forward the inputs, excluding the controller type (first param)
        locomotionParameters = new float[parameters.length - 1];
        for (int i = 0; i < locomotionParameters.length; i++) {
            locomotionParameters[i] = parameters[i + 1];
        }
    }

    @Override
    public float[] controlStep(float[] inputs) {
        return locomotionParameters;
    }
}
