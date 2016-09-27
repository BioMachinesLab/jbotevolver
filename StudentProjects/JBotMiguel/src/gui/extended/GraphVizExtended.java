package gui.extended;

import gui.util.GraphViz;


public class GraphVizExtended extends GraphViz {
	/*	
   public GraphVizExtended() {
	   super();
   }
   
   public GraphVizExtended(NeuralNetwork network) {
	   this();
	   this.network = network;
	   setupNetwork();
   }
   
   public GraphVizExtended(int input, int hidden, int output) {
	   super(input,hidden,output);
   }
   
   @Override
	public void changeNeuralNetwork(NeuralNetwork n) {
		super.changeNeuralNetwork(n);
		setupNetwork();
	}
   
   protected void setupNetwork() {
	   this.graph = new StringBuilder();
	   
	   addln(start_graph());
	   
	   if(network != null) {
		   this.input = network.getNumberOfInputNeurons();
		   this.output = network.getNumberOfOutputNeurons();
		   
		   if(network instanceof CTRNNMultilayer) {
			  super.setupNetwork();
		   }
		   
		   if(network instanceof ERNEATNetwork) {
			   createNEATNetwork();
			   connectNEATNetwork();
		   }
		   
		   if(network instanceof LayeredNeuralNetwork) {
			   createLayeredNetwork();
			   connectLayeredNetwork();
		   }
	   }
	   addln(end_graph());
//	   System.out.println(graph);
   }
   
   protected void createLayeredNetwork() {
	   String result = "node [shape=circle,fixedsize=true,width=0.9];";
	   
	   LayeredANN net = ((LayeredNeuralNetwork)network).getNetwork();
	   
	   result+="size=\"18,18\"; ranksep=\"2.2 equally\"";
	   
	   for(int i = 0 ; i < net.getNumberOfLayers() ; i++) {
		   result+="{rank=same;";
		   
		   for(ANNNeuron n : net.getLayer(i).getNeurons()) {
			   double state = ((int)(n.getActivationValue()*100.0))/100.0;
			   result+=n.getId()+" [label=\""+n.getId()+"\n("+state+")\"] ";
		   }
		   
		   result+=";}";
	   }
	   
	   addln(result);
   }
   
   protected void connectLayeredNetwork() {
	   ArrayList<ANNSynapse> links = ((LayeredNeuralNetwork)network).getNetwork().getAllSynapses();
	   
	   for(ANNSynapse l : links) {
		   long from = l.getFromNeuron();
		   long to = l.getToNeuron();
		   double w = ((int)(l.getWeight()*100))/100.0;
		   addln(from+" -> "+to+" [label=\" "+w+"\", "
		   		+ (w < 0 ? "arrowhead = empty, color = \"red\", " : "color = \"green\", ")
		   		+ "penwidth = "+Math.max(Math.abs((int)Math.round(w)),1)
		   		+ "];");
	   }
   }
   
   protected void createNEATNetwork() {
	   String result = "node [shape=circle,fixedsize=true,width=0.9];";
	   
	   NEATNetwork net = ((ERNEATNetwork)network).getNetwork();
	   
	   result+="size=\"18,18\"; ranksep=\"2.2 equally\"";
	   
	   result+="{rank=same;";
	   for(int i = 0 ; i < net.getInputCount()+1 ; i++) {
		   double state = ((int)(net.getPostActivation()[i]*100))/100.0;
		   result+=i+" [label=\""+i+"\n("+state+")\"] ";
	   }
	   result+=";}";
	   
	   int hidden = net.getActivationFunctions().length-(net.getInputCount()+net.getOutputCount()+1);
	   if(hidden > 0) {
		   result+="{rank=same;";
		   for(int i = 0 ; i < hidden ; i++) {
			   String text = "";
			   
			   int id = (net.getOutputCount()+net.getOutputIndex()+i);
			   
			   if(net instanceof NEATContinuousNetwork) {
				   NEATContinuousNetwork cnet = (NEATContinuousNetwork)net;
				   if(cnet.getNeurons()[id].isDecayNeuron())
					   text=" c";
			   }
			   
			   double state = ((int)(net.getPostActivation()[id]*100))/100.0;
			   result+=id+" [label=\""+id+"\n("+state+text+")\"] ";
		   }
		   result+=";}";
	   }
	   
	   result+="{rank=same;";
	   for(int i = 0 ; i < net.getOutputCount() ; i++) {
		   int id = i+net.getOutputIndex();
		   double state = ((int)(net.getPostActivation()[id]*100))/100.0;
		   result+=id+" [label=\""+id+"\n("+state+")\"] ";
	   }
	   result+=";}";
	   
	   addln(result);
   }
   
   protected void connectNEATNetwork() {
	   
	   NEATLink[] links = ((ERNEATNetwork)network).getNetwork().getLinks();
	   
	   for(NEATLink l : links) {
		   int from = l.getFromNeuron();
		   int to = l.getToNeuron();
		   double w = ((int)(l.getWeight()*100))/100.0;
		   addln(from+" -> "+to+" [label=\" "+w+"\", "
		   		+ (w < 0 ? "arrowhead = empty, color = \"red\", " : "color = \"green\", ")
		   		+ "penwidth = "+Math.max(Math.abs((int)Math.round(w)),1)
		   		+ "];");
	   }
	   
   }*/
}