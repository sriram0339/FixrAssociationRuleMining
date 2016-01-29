package associationRuleMiner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.algorithms.cluster.BicomponentClusterer;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;

public class AssociationRuleGraph {
	public int n;
	
	Map<Integer, List<Integer> > adjList;
	List<AssociationRule> allRules;
	UndirectedGraph<Integer,Integer> assocG;
	int numEdges;
	
	protected Set< Set<Integer> > clusterComponents(){
		System.out.println("Clustering");
		BicomponentClusterer<Integer,Integer> eClu = new BicomponentClusterer<Integer, Integer>(); 
		int count = 0;
		int totMethodCount = 0;
		Set<Set<Integer>> clusters =  eClu.transform(assocG); 
		for (Set<Integer> sI: clusters){
			Set<Integer> clusterMethods  = new TreeSet<Integer>();
			for (int j: sI){
				AssociationRule a = allRules.get(j);
				a.clusterID = count;
				clusterMethods.addAll(a.antecedentIDs);
				clusterMethods.add(a.consequenceID);
			}
			System.out.println("Cluster ID: "+ count);
			System.out.println("\t Number of rules: "+ sI.size());
			System.out.println("\t Number of methods: "+ clusterMethods.size());
			totMethodCount = totMethodCount + clusterMethods.size();
			
			count++;
		}
		System.out.println("Total number of methods involved in the rules = "+totMethodCount);
		return clusters;
	}
	
	void visualizeGraphAndDumpToFile(String fileName){
		Layout<Integer,Integer> l = new KKLayout<Integer,Integer>( assocG);
		VisualizationImageServer<Integer, Integer> vis =
			    new VisualizationImageServer<Integer, Integer>(l, new Dimension(1200,1200));
		vis.setBackground(Color.WHITE);
		
		// Create the buffered image
		BufferedImage image = (BufferedImage) vis.getImage (
		    new Point2D.Double(l.getSize().getWidth() / 2,
		    l.getSize().getHeight() / 2),
		    new Dimension(l.getSize()));

		// Write image to a png file
		File outputfile = new File(fileName);

		try {
		    ImageIO.write(image, "png", outputfile);
		
		} catch (IOException e) {
		    // Exception handling
			e.printStackTrace();
			System.exit(1);
		}
	
	}
	protected void addEdge(int i, int j){
		List<Integer> aI, aJ;
		assocG.addEdge(numEdges, i,j,EdgeType.UNDIRECTED);
		numEdges++;
		if (adjList.containsKey(i)){
			aI = adjList.get(i);
		} else{
			aI = new ArrayList<Integer>();
			adjList.put(i, aI);
		}
		aI.add(j);
		if (adjList.containsKey(j)){
			aJ = adjList.get(j);
		} else {
			aJ = new ArrayList<Integer>();
			adjList.put(j, aJ);
		}
		aJ.add(i);
		return;
	}
	
	
	protected void constructAdjacencyList(){
		int i, j;
		for (i=0; i < n; ++i){
			assocG.addVertex((Integer) i);
		}
		
		for (i = 0; i < n-1; ++i){
			AssociationRule a1 = allRules.get(i);
			for (j = i+1; j < n; ++j){
				AssociationRule a2 = allRules.get(j);
				if (a1.intersectsFrequentItemSet(a2)){
					addEdge(i,j);
				}
			}
		}
	}
	public AssociationRuleGraph(List<AssociationRule> aRules){
		allRules = aRules;
		n = aRules.size();
		adjList = new TreeMap<Integer, List<Integer>> ();
		assocG = new UndirectedSparseGraph<Integer,Integer>();
		numEdges = 0;
		constructAdjacencyList();
	}
	
	public void dotPrintAssociationRuleGraph(String fileName){
		PrintWriter fstream;
		try {
			fstream = new PrintWriter(fileName, "UTF-8");
			fstream.println("graph G{ ");
			for (int k = 0; k < n; ++k){
				AssociationRule aK = allRules.get(k);
				fstream.format("n%d [shape=circle, label=\"%d\"];\n",k,k); //aK.toString());
			}
			for (Map.Entry<Integer, List<Integer>> e: adjList.entrySet()){
				int id = e.getKey();
				List<Integer> aI = e.getValue();
				for (int j: aI){
					if (id < j)
						fstream.format("n%d -- n%d; \n", id, j);
				}
			}
			fstream.println("}");
			fstream.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
	}
	
}
