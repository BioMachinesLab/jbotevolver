package taskexecutor;

import result.Result;
import taskexecutor.VRepTaskExecutor.VRepContainer;
import tasks.Task;
import vrep.VRepUtils;

public class VRepTask extends Task {

    private final float[] parameters;
    private VRepContainer container;

    public VRepTask(float[] parameters) {
        this.parameters = parameters;
    }

    public void setVREP(VRepContainer container) {
        this.container = container;
    }

    @Override
    public void run() {
        VRepUtils.sendDataToVREP(container.clientId, parameters);
    }

    @Override
    public Result getResult() {
        return new VRepResult(getId(), VRepUtils.getDataFromVREP(container.clientId));
    }
}
