package vrep;

public class VRepDummyController extends VRepController {

    protected float[] locomotionParameters;

    public VRepDummyController(float[] parameters) {
        super(parameters);
        // just forward the inputs, excluding the controller type (first param)
        locomotionParameters = ControllerFactory.doubleToFloat(ControllerFactory.extractWeights(parameters));
    }

    @Override
    public float[] controlStep(float[] inputs) {
        return locomotionParameters;
    }
}
