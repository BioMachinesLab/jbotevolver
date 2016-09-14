package taskexecutor;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.remoteApi;
import result.Result;
import taskexecutor.VRepTaskExecutor.VREPContainer;
import tasks.Task;

public class VRepTask extends Task{
	
	private float[] parameters;
        private remoteApi vrepApi;
	private VREPContainer container;
	
	public VRepTask(float[] parameters) {
		this.parameters = parameters;
	}
	
	public void setVREP(VREPContainer container, remoteApi vrepApi) {
		this.container = container;
                this.vrepApi = vrepApi;
	}

	@Override
	public void run() {
		sendDataToVREP(parameters);
	}

	@Override
	public Result getResult() {
		return new VRepResult(getId(),getDataFromVREP());
	}
	
	protected float[] getDataFromVREP() {
    	CharWA str=new CharWA(0);
    	
    	int signalVal = vrepApi.simxGetStringSignal(container.clientId,"toClient",str,remoteApi.simx_opmode_blocking);
    	
    	while(signalVal != remoteApi.simx_return_ok) {
    		
    		if(signalVal == 3 || signalVal == remoteApi.simx_return_initialize_error_flag)//error in the connection
    			return null;
    		
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		signalVal = vrepApi.simxGetStringSignal(container.clientId,"toClient",str,remoteApi.simx_opmode_blocking);
		}
    	
		vrepApi.simxClearStringSignal(container.clientId, "toClient", remoteApi.simx_opmode_blocking);
		 
		FloatWA f = new FloatWA(0);
		f.initArrayFromCharArray(str.getArray());
		return f.getArray();
    }
    
    protected void sendDataToVREP(float[] arr) {
    	FloatWA f = new FloatWA(arr.length);
    	f.setValue(arr);
    	char[] chars = f.getCharArrayFromArray();
    	String tempStr = new String(chars);
		CharWA str = new CharWA(tempStr);
		vrepApi.simxWriteStringStream(container.clientId,"fromClient",str,remoteApi.simx_opmode_blocking);
    }

}
