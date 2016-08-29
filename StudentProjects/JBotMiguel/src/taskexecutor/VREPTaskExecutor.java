package taskexecutor;

import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import result.Result;
import simulation.util.Arguments;
import tasks.Task;
import coppelia.remoteApi;
import evolutionaryrobotics.JBotEvolver;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VREPTaskExecutor extends TaskExecutor {

    private static int DEFAULT_PORT = 19996;

    private Stack<VREPContainer> availableClients = new Stack<VREPContainer>();
    private ExecutorService executor;
    private LinkedList<Future<Result>> resultsList = new LinkedList<Future<Result>>();
    private boolean setup = false;

    private String remoteIps[];
    private int remoteInstances[];
    private static int ALLOWED_FAULTS = 3;

    private remoteApi vrepApi;

    public VREPTaskExecutor(JBotEvolver jBotEvolver, Arguments args) {
        super(jBotEvolver, args);

        int instances = 0;
        if (args.getArgumentIsDefined("remote")) {

            String arg = args.getArgumentAsString("remote");
            String[] split = arg.split(",");

            remoteIps = new String[split.length / 2];
            remoteInstances = new int[split.length / 2];

            int index = 0;

            for (int i = 0; i < split.length;) {
                remoteIps[index] = split[i++];
                remoteInstances[index] = Integer.parseInt(split[i++]);
                instances += remoteInstances[index];
                index++;
            }

        }

        executor = Executors.newFixedThreadPool(instances);
    }

    public int getInstances() {
        if(!setup) {
            try {
                synchronized(availableClients){
                    availableClients.wait();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return availableClients.size();
    }

    @Override
    public void run() {
        try {
            vrepApi = new remoteApi();
            for (int r = 0; r < remoteIps.length; r++) {
                for (int i = 0; i < remoteInstances[r]; i++) {
                    initContainer(remoteIps[r], DEFAULT_PORT + i);
                }
            }
            setup = true;
            synchronized(availableClients){
                availableClients.notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected VREPContainer initContainer(String ip, int port) {
        VREPContainer c = new VREPContainer();

        c.ip = ip;
        c.port = port;
        c.clientId = vrepApi.simxStart(c.ip, c.port, true, false, 5000, 5);
        if (c.clientId == -1) {
            return null;
        }
        vrepApi.simxClearStringSignal(c.clientId, "toClient", remoteApi.simx_opmode_blocking);
        vrepApi.simxClearStringSignal(c.clientId, "fromClient", remoteApi.simx_opmode_blocking);
        availableClients.push(c);

        return c;
    }

    @Override
    public void stopTasks() {
        // TODO
    }

    @Override
    public void addTask(Task t) {
        while (!setup) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized (resultsList) {
            Future<Result> submit = executor.submit(new VREPJBotCallable(t));
            resultsList.add(submit);
        }
    }

    @Override
    public Result getResult() {
        Result obj = null;

        while (obj == null) {

            Future<Result> callable = null;

            synchronized (resultsList) {
                while (callable == null) {
                    callable = resultsList.pop();
                }
            }
            try {
                obj = callable.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return obj;
    }

    public class VREPContainer {

        int clientId;
        String ip;
        int port;
        int faults = 0;
    }

    private class VREPJBotCallable implements Callable<Result> {

        private Task t;

        public VREPJBotCallable(Task t) {
            this.t = t;
        }

        @Override
        public Result call() throws Exception {

            VREPContainer container = null;

            synchronized (availableClients) {

                while (availableClients.size() == 0) {
                    availableClients.wait();
                }

                //get the VREP conn
                container = availableClients.pop();
            }

            VREPTask vt = (VREPTask) t;
            vt.setVREP(container, vrepApi);

            t.run();

            Result r = t.getResult();

            VREPResult vrepR = (VREPResult) r;

            synchronized (availableClients) {
                //return the VREP conn
                if (vrepR.getValues() != null) {
                    container.faults = 0;
                    availableClients.push(container);
                    availableClients.notify();
                } else {
                    //we lost a worker, resubmit task and notify others
                    addTask(this.t);

                    if (container.faults < ALLOWED_FAULTS) {
                        vrepApi.simxFinish(container.clientId);
                        VREPContainer inited = initContainer(container.ip, container.port);
                        if (inited != null) {
                            inited.faults = container.faults + 1;
                        }
                    } 
                    availableClients.notify();
                    return null;
                }
            }
            return r;
        }
    }
}
