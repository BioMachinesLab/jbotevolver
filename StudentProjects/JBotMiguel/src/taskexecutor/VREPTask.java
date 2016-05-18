package taskexecutor;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.remoteApi;
import result.Result;
import taskexecutor.VREPTaskExecutor.VREPContainer;
import tasks.Task;

public class VREPTask extends Task{
	
	private float[] parameters;
	private VREPContainer container;
	
	public VREPTask(float[] parameters) {
		this.parameters = parameters;
	}
	
	public void setVREP(VREPContainer container) {
		this.container = container;
	}

	@Override
	public void run() {
		sendDataToVREP(parameters);
	}

	@Override
	public Result getResult() {
		return new VREPResult(getDataFromVREP());
	}
	
	protected float[] getDataFromVREP() {
    	CharWA str=new CharWA(0);
    	
    	int signalVal = container.vrep.simxGetStringSignal(container.clientId,"toClient",str,remoteApi.simx_opmode_oneshot_wait);
    	
    	while(signalVal != remoteApi.simx_return_ok) {
    		
    		if(signalVal == 3 || signalVal == remoteApi.simx_return_initialize_error_flag)//error in the connection
    			return null;
    		
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		signalVal = container.vrep.simxGetStringSignal(container.clientId,"toClient",str,remoteApi.simx_opmode_oneshot_wait);
		}
    	
		container.vrep.simxClearStringSignal(container.clientId, "toClient", remoteApi.simx_opmode_oneshot);
		 
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
		container.vrep.simxWriteStringStream(container.clientId,"fromClient",str,remoteApi.simx_opmode_oneshot);
    }

}
