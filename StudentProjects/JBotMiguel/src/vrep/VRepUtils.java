package vrep;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.remoteApi;
import taskexecutor.VRepResult;
import taskexecutor.VRepTask;
import taskexecutor.VRepTaskExecutor;

public class VRepUtils {

    public static final String DEFAULT_IP = "127.0.0.1";
    public static final int DEFAULT_PORT = 19996;
    public static remoteApi defaultVrep = null;
    public static int defaultClientId = -1;

    public static float[][] createDataPacket(evolutionaryrobotics.neuralnetworks.Chromosome[] chromosomes, float[] controllerParams) {
        float[][] totalPackets = new float[chromosomes.length][];
        for (int i = 0; i < totalPackets.length; i++) {

            evolutionaryrobotics.neuralnetworks.Chromosome c = chromosomes[i];
            float[] params = new float[2 + controllerParams.length + c.getAlleles().length];

            params[0] = c.getID(); //id of the chromosome
            params[1] = controllerParams.length + c.getAlleles().length;
            System.arraycopy(controllerParams, 0, params, 2, controllerParams.length);
            for (int j = 0; j < c.getAlleles().length; j++) {
                params[2 + controllerParams.length + j] = (float) c.getAlleles()[j];
            }
            totalPackets[i] = params;
        }
        return totalPackets;
    }
    
    public static int sendTasks(VRepTaskExecutor te, float[] fixedParameters, float[][] chromosomes) {

        int instances = te.getInstances();
        int chromosomesPerInstance = chromosomes.length / instances;

        int remainder = chromosomes.length % instances;

        int totalIndex = 0;

        for (int i = 0; i < instances; i++) {

            int currentTasks = chromosomesPerInstance;

            if (remainder > 0) {
                currentTasks++;
                remainder--;
            }

            float[][] toSend = new float[currentTasks + 1][];

            int index = 0;
            int toSendIndex = 0;

            float[] parameters = new float[fixedParameters.length + 1];

            for (int fp = 0; fp < fixedParameters.length; fp++) {
                parameters[index++] = fixedParameters[fp];
            }

            parameters[index++] = currentTasks; //nInds
            toSend[toSendIndex++] = parameters;

            for (int ci = 0; ci < currentTasks; ci++) {
                index = 0;

                float[] chromosome = chromosomes[totalIndex++];
                float[] params = new float[chromosome.length];

                for (float f : chromosome) {
                    params[index++] = f;
                }

                toSend[toSendIndex++] = params;

                System.out.print(".");
            }

            int count = 0;
            for (int c = 0; c < toSend.length; c++) {
                count += toSend[c].length;
            }

            float[] result = new float[count];
            int resultIndex = 0;
            for (int c = 0; c < toSend.length; c++) {
                for (int d = 0; d < toSend[c].length; d++) {
                    result[resultIndex++] = toSend[c][d];
                }
            }

            VRepTask task = new VRepTask(result);
            te.addTask(task);
            System.out.print("\n");
        }

        return instances;
    }

    public static float[][] receiveTasks(VRepTaskExecutor te, int nTasks) {

        float[][] results = new float[nTasks][];

        for (int i = 0; i < nTasks; i++) {
            VRepResult res = (VRepResult) te.getResult();

            float[] vals = res.getValues();

            results[i] = vals;

            float nResults = vals[0];

            for (int n = 0; n < nResults; n++) {
                System.out.print("!");
            }

            System.out.print("\n");
        }
        return results;
    }

    private static synchronized void init(boolean force) {
        if (defaultVrep == null || force) {
            defaultVrep = new remoteApi();
            defaultVrep.simxFinish(-1); // just in case, close all opened connections
            defaultClientId = defaultVrep.simxStart(DEFAULT_IP, DEFAULT_PORT, true, false, 5000, 5);
            System.out.println("Tried connection to " + DEFAULT_IP + "@" + DEFAULT_PORT + ": " + defaultClientId);
        }
    }

    public static synchronized float[] getDataFromVREP() {
        init(false);
        CharWA str = new CharWA(0);
        try {
            while (defaultVrep.simxGetStringSignal(defaultClientId, "toClient", str, remoteApi.simx_opmode_oneshot_wait) != remoteApi.simx_return_ok) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new float[]{0, 0, 0, 0};
        }

        defaultVrep.simxClearStringSignal(defaultClientId, "toClient", remoteApi.simx_opmode_oneshot_wait);
        FloatWA f = new FloatWA(0);
        f.initArrayFromCharArray(str.getArray());
        return f.getArray();
    }

    public static synchronized void sendDataToVREP(float[] arr) {
        init(false);
        FloatWA f = new FloatWA(arr.length);
        f.setValue(arr);
        char[] chars = f.getCharArrayFromArray();
        String tempStr = new String(chars);
        CharWA str = new CharWA(tempStr);
        int opStatus = defaultVrep.simxWriteStringStream(defaultClientId, "fromClient", str, remoteApi.simx_opmode_oneshot_wait);
        if(opStatus != remoteApi.simx_return_ok) {
            System.out.println("Error sending data: " + opStatus);
            init(true);
            sendDataToVREP(arr);
        }
    }
}
