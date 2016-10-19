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
import evolutionaryrobotics.JBotEvolver;
import vrep.VRepUtils;

public class VRepTaskExecutor extends TaskExecutor {

    private static final int BASE_PORT = 19996;
    private static final int ALLOWED_FAULTS = 3;

    private final Stack<VRepContainer> availableClients = new Stack<>();
    private final ExecutorService executor;
    private final LinkedList<Future<Result>> resultsList = new LinkedList<>();
    private boolean setup = false;

    private String remoteIps[];
    private int remoteInstances[];

    public VRepTaskExecutor(JBotEvolver jBotEvolver, Arguments args) {
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
            for (int r = 0; r < remoteIps.length; r++) {
                for (int i = 0; i < remoteInstances[r]; i++) {
                    initContainer(remoteIps[r], BASE_PORT + i);
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

    protected VRepTaskExecutor.VRepContainer initContainer(String ip, int port) {
        VRepTaskExecutor.VRepContainer c = new VRepTaskExecutor.VRepContainer();
        c.ip = ip;
        c.port = port;
        c.clientId = VRepUtils.initClient(ip, port);        
        if (c.clientId == -1) {
            return null;
        }
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

    public class VRepContainer {
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
            // get available container
            VRepContainer container = null;
            synchronized (availableClients) {
                while (availableClients.isEmpty()) {
                    availableClients.wait();
                }
                //get the VREP conn
                container = availableClients.pop();
            }

            // run task
            VRepTask vt = (VRepTask) t;
            vt.setVREP(container);
            t.run();
            Result r = t.getResult();
            VRepResult vrepR = (VRepResult) r;

            // parse result
            synchronized (availableClients) {
                if (vrepR.getValues() != null) {
                    container.faults = 0;
                    availableClients.push(container);
                    availableClients.notify();
                } else {
                    //we lost a worker, resubmit task and notify others
                    addTask(this.t);

                    if (container.faults < ALLOWED_FAULTS) {
                        VRepUtils.terminateClient(container.clientId);
                        try {
                            Thread.sleep(10000); // wait 10s before trying to init the client again
                        } catch(Exception e) {}
                        VRepContainer inited = initContainer(container.ip, container.port);
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
