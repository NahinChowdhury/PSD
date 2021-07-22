import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

import javax.swing.text.ChangedCharSetException;

import java.util.List;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceGPX.GPXConstants.WPTAttribute;
//mport org.omg.CORBA.PUBLIC_MEMAMS;
import org.graphstream.algorithm.AStar.DistanceCosts;
import org.graphstream.algorithm.Dijkstra;
import net.sf.geographiclib.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import static java.lang.Integer.parseInt;


public class Main {



//// Reads the Graph from a input file	

	public static int readGraph(Graph graph, int lid)
	{


		//Reads Vertices

		try (BufferedReader br = new BufferedReader(new FileReader("./datasets/Amsterdam/roadnetwork/RoadVerticesAMS.txt"))) {
			String line;

			while ((line = br.readLine()) != null) {

				String[] splited = line.split("\\s+"); 							// splits based on white spaces
				graph.addNode(splited[0]);										// add node
				Node n = graph.getNode(splited[0]);								// get the node as object
				n.setAttribute("long", Double.parseDouble(splited[1]));			// set longitude attribute
				n.setAttribute("lat", Double.parseDouble(splited[2]));			// set latitude attribute
				n.setAttribute("poi", 0);										// set the node as a normal node
				lid = parseInt(splited[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (BufferedReader br = new BufferedReader(new FileReader("./datasets/Amsterdam/roadnetwork/RoadEdgesAMS.txt"))) {
			String line;

			while ((line = br.readLine()) != null) {

				String[] splited = line.split("\\s+");							// split based on white spaces
				try
				{
					graph.addEdge(splited[0], splited[1], splited[2]);    	// adds the edge

					Node n1 = graph.getNode(splited[1]);					// gets the nodes from both ends of the edge
					Node n2 = graph.getNode(splited[2]);

					// gets the road network distance between the edge
					GeodesicData g = Geodesic.WGS84.Inverse(n1.getAttribute("lat"), n1.getAttribute("long"), n2.getAttribute("lat"), n2.getAttribute("long"));
					//System.out.println(g.s12);
					Edge e1 = graph.getEdge(splited[0]);					// get the edge as object
					e1.setAttribute("weight", g.s12);						// set the road network distance as the weight of the edge
					//System.out.println(e1.getId());
				}
				catch (EdgeRejectedException e) {

				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}




		return lid;

	}



	public static void ShortestPath(Graph graph, double[][] sp, int Lid, ArrayList<POI> P, String name)
	{

		for (int i=0;i<P.size();i++)
		{
			POI p = P.get(i);
			Edge e = graph.getEdge(p.eId);
			p.n1 = e.getNode0();
			p.n2 = e.getNode1();
			Node n = p.n1;
			double s1 = n.getAttribute("long") ;
			s1 = s1 - p.longitude;
			s1 = s1*s1;

			double s2 = n.getAttribute("lat") ;
			s2 = s2 - p.latitude;
			s2 = s2*s2;
			double S1 = Math.sqrt(s1+s2);

			n = p.n2;
			s1 = n.getAttribute("long") ;
			s1 = s1 - p.longitude;
			s1 = s1*s1;

			s2 = n.getAttribute("lat") ;
			s2 = s2 - p.latitude;
			s2 = s2*s2;
			double S2 = Math.sqrt(s1+s2);
			if (S1<S2) p.near=1;
			else p.near=2;
			p.distance = S1;
			p.distance2=S2;

//			p.distance2 = graph.getEdge(p.eId).getAttribute("weight");
//			p.distance2 = p.distance2 - p.distance;
		}

		PrintWriter writer;
		try {
			writer = new PrintWriter(name, "UTF-8");

			for (int i=0;i<Lid;i++)
			{
				//System.out.println(i);
				for(int k=0;k<2;k++)
				{
					Node node;
					POI p = P.get(i);
					if(k==0)node=p.n1;
					else node = p.n2;

					Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE ,"result", "weight");
					dijkstra.init(graph);
					dijkstra.setSource(node);
					//System.out.println(node.getId());
					dijkstra.compute();

					for(int j=0;j<Lid;j++)
					{
						POI p1 = P.get(j);
						double d1 = dijkstra.getPathLength(p1.n1);
						double d2 = dijkstra.getPathLength(p1.n2);
						if(p1.near==1)
						{
							d1  = d1 + p1.distance;
							d2  = d2 + p1.distance2;
						}
						else {
							d1  = d1 + p1.distance2;
							d2  = d2 + p1.distance;
						}
						if (d1<d2) sp[i][j]=d1;
						else sp[i][j]=d2;
					}
				}
			}
			for (int i=0;i<Lid;i++)
			{
				for(int j=0;j<Lid;j++)
				{
					writer.print(sp[i][j]);
					writer.print(" ");
				}
				writer.println();
			}
			writer.close();
		}
		catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();}


	}


	public static double randomizeItem (ArrayList<POI> P, ArrayList<Item> Itm, double [] maxCost)
	{
		double mcost = 0;
		for(int i = 0; i< Itm.size();i++)
		{
			Random rand = new Random();
			double base = rand.nextDouble()*10.0;
			int iid = Itm.get(i).Id;
			for (int j=0;j<Itm.get(i).p.size()-1;j++)
			{
				int pid = Itm.get(i).p.get(j);
				if(P.get(pid).items[iid]==0) continue;
				else {
					P.get(pid).items[iid]+=base;
					maxCost[iid]+=base;
					if(maxCost[iid]>mcost)mcost=maxCost[iid];
				}

			}
		}
		return mcost;

	}

	public static void normalizeItem (ArrayList<POI> P, ArrayList<Item> Itm, double [] maxCost)
	{
		for(int i = 0; i< Itm.size();i++)
		{

			int iid = Itm.get(i).Id;
			for (int j=0;j<Itm.get(i).p.size()-1;j++)
			{
				int pid = Itm.get(i).p.get(j);
				if(P.get(pid).items[iid]==0) continue;
				else {
					P.get(pid).items[iid]= P.get(pid).items[iid]/maxCost[iid];
				}

			}
		}
	}

	public static void normalizeItem2(ArrayList<POI> P, ArrayList<Item> Itm, double [] maxCost, ArrayList<Integer>list)
	{

		double mcost = 0;

		for(int i = 0; i< list.size();i++)
		{
			if(maxCost[list.get(i)]>mcost)mcost=maxCost[list.get(i)];
		}
		for(int i = 0; i< Itm.size();i++)
		{

			int iid = Itm.get(i).Id;
			for (int j=0;j<Itm.get(i).p.size()-1;j++)
			{
				int pid = Itm.get(i).p.get(j);
				if(P.get(pid).items[iid]==0) continue;
				else {
					P.get(pid).items[iid]= P.get(pid).items[iid]/mcost;
				}

			}
		}
		System.out.println("mAXiMUM: "+ mcost);
	}

	public static double getnormcost (double [] maxCost, ArrayList<Integer>list)
	{
		double mcost = 0;

		for(int i = 0; i< list.size();i++)
		{
			if(maxCost[list.get(i)]>mcost)mcost=maxCost[list.get(i)];
		}
		return mcost;
	}


	public static void setnewItem (ArrayList<POI> P, ArrayList<Item> Itm)
	{
		double minc = 20;
		for(int i = 0; i< Itm.size();i++)
		{
			Random rand = new Random();
			double base = rand.nextDouble()*10.0 + 5;
			//if(base<5)System.out.println("vuaa");
			int iid = Itm.get(i).Id; // get the ith itemID
			for (int j=Itm.get(i).p.size()-1;j>=0;j--) // j is the number of stores this item can be found in
			{
				int pid = Itm.get(i).p.get(j); // first poi id where this item is found?
				if(P.get(pid).items[iid]==0)
				{
					Itm.get(i).p.remove(j); // remove the POI from item's list if it isn't in the POI's item list(has a cost of 0)?
					continue;
				}
				else {
					Random rand1 = new Random();
					double dev = rand1.nextGaussian()%2;
					P.get(pid).items[iid]= base + dev;
					if(P.get(pid).items[iid]<minc)minc=P.get(pid).items[iid];
				}

			}
		}
		//System.out.println("MINIMUM: "+minc);
	}






	public static void costdit (ArrayList<POI> P, ArrayList<Item> Itm, double [] maxCost)
	{
		for(int i = 0; i< Itm.size();i++)
		{

			int iid = Itm.get(i).Id;

			for (int j=0;j<Itm.get(i).p.size();j++)
			{
				int pid = Itm.get(i).p.get(j);
				if(P.get(pid).items[iid]==0) continue;
				else {
					P.get(pid).items[iid]= P.get(pid).items[iid]/maxCost[iid];
				}

			}
		}
	}




	public static void sortItems(ArrayList<POI> P, ArrayList<Item> Itm, String name, double m, double [] maxCost)

	{
		for(int i = 0; i< Itm.size();i++)
		{
			int iid = Itm.get(i).Id;
			for (int j=0;j<Itm.get(i).p.size()-1;j++)
			{
				double min;
				int pid = Itm.get(i).p.get(j);
				if(P.get(pid).items[iid]==0) min=m;
				else {min = P.get(pid).items[iid];}
				int idx = j;
				for(int k = j+1; k< Itm.get(i).p.size(); k++)
				{
					pid = Itm.get(i).p.get(k);
					if ((P.get(pid).items[iid]!=0) && (P.get(pid).items[iid]<min))
					{
						//if(iid==232)System.out.println(k);
						min = P.get(pid).items[iid];
						idx = k;
					}
					if ((P.get(pid).items[iid]!=0) && (P.get(pid).items[iid]>maxCost[iid]))
					{
						//if(iid==232)System.out.println(k);
						maxCost[iid]  = P.get(pid).items[iid];
					}
				}
				//if(iid==232)System.out.println(Itm.get(i).p.get(j)+"  Before: "+ j);
				int temp = Itm.get(i).p.get(j);
				Itm.get(i).p.set(j, Itm.get(i).p.get(idx));
				Itm.get(i).p.set(idx, temp);
				//if(iid==232)System.out.println(Itm.get(i).p.get(j) + "  After: "+ idx);


			}
		}

		PrintWriter writer;
		try {
			writer = new PrintWriter(name, "UTF-8");

			for(int i = 0; i< Itm.size();i++)
			{
				int iid = Itm.get(i).Id;
				writer.print(iid);
				writer.print(" ");
				for (int j=0;j<Itm.get(i).p.size();j++)
				{
					int pid = Itm.get(i).p.get(j);
					if(P.get(pid).items[iid]>0)
					{
						writer.print(Itm.get(i).p.get(j));
						writer.print(" ");
					}

				}
				writer.println();

			}
			writer.close();


		}
		catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();}




	}


	public static void readPoi(ArrayList<POI> P, int total, Graph graph, String name )
	{
		// Reads the POIs  "./datasets/Amsterdam/poi/originals/GasStationsAMS.txt"

		try (BufferedReader br = new BufferedReader(new FileReader(name))) {

			String line;
			while ((line = br.readLine()) != null) {

				String[] splited = line.split("\\s+");							// split based on white spaces
				POI nPoi = new POI();
				nPoi.items = new double[total];
				nPoi.setvalues(parseInt(splited[0]), Double.parseDouble(splited[1]), Double.parseDouble(splited[2]), splited[3], Double.parseDouble(splited[4]));
				//System.out.println(splited[3]);

				Edge e = graph.getEdge(nPoi.eId);
				//System.out.println(splited[3]);
				//System.out.println(e.getId());
				//System.out.println(e.getNode1().getId());
				nPoi.n1 = e.getNode0();
				nPoi.n2 = e.getNode1();
				P.add(nPoi);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}





	public static void readShoppingList(ArrayList<Integer> ItemList, String iList)
	{
		try (BufferedReader br = new BufferedReader(new FileReader(iList))) {

			String line;

			while ((line = br.readLine()) != null) {
				int item = parseInt(line);
				//System.out.println(item);
				ItemList.add(item);


			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}






	public static double readSP(double [][] sp, int len, String name, double maxd )
	{
		//"./datasets/Amsterdam/poi/originals/ShortestPathPoi.txt"
		// = 0;
		int i=0;
//		int i=50;
		try (BufferedReader br = new BufferedReader(new FileReader(name))) {

			String line;
			while ((line = br.readLine()) != null) {
//				line = line.replaceAll("\r", "");
				String[] splited = line.split("\\s+"); // split based on white spaces
//				System.out.println(splited[49]);

				for (int j=0;j<len;j++)
				{
					sp[i][j]= Double.parseDouble(splited[j]);
					if (sp[i][j]>maxd) maxd=sp[i][j];

				}
				i++;

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//Normalization
		for (int k=0;k<i;k++)

		{
			for (int j=0; j<i;j++)
				//sp[k][j] = Math.round((sp[k][j]/maxd) * 100.0) / 100.0;;
//				sp[k][j] = precision(sp[k][j]/maxd);
				sp[k][j] = precision(sp[k][j]);
		}
		return maxd;
	}




	public static void ReadPermutes(ArrayList<ArrayList<int []>> permutes, int len)
	{

		ArrayList<int []> firstArrayList = new ArrayList<int []>();
		permutes.add(firstArrayList);
		for(int i = 2; i<=len; i++)
		{
			String nameString = "./datasets/permute_" + i + ".txt";
			ArrayList<int []> plist = new ArrayList<int []>();
			try (BufferedReader br = new BufferedReader(new FileReader(nameString))) {

				String line;
				while ((line = br.readLine()) != null) {

					//System.out.println(i +": " +line);
					int [] prmt = new int[i];

					String[] splited = line.split("\\s+");							// split based on white spaces

					for(int j = 0; j<splited.length;j++)
					{
						int shp = parseInt(splited[j]);
						prmt[j] =shp;
					}
					plist.add(prmt);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			permutes.add(plist);
		}
	}



	public static void readItems(ArrayList<Item>items, String name)
	{
		//"./datasets/Amsterdam/poi/originals/StoresPerItemAMS.txt"

		try (BufferedReader br = new BufferedReader(new FileReader(name))) {

			String line;
			while ((line = br.readLine()) != null) {

				String[] splited = line.split("\\s+");							// split based on white spaces
				Item itm = new Item();
				itm.Id = parseInt(splited[0]);
				//System.out.println(line);
				for(int i = 1; i<splited.length;i++)
				{
					int shp = parseInt(splited[i]);
					if(!itm.p.contains(shp))
					{itm.p.add(shp);}
				}
				items.add(itm);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public static void readItemNames(ArrayList<Item>items, String name)
	{
		//"./datasets/Amsterdam/poi/originals/StoresPerItemAMS.txt"

		try (BufferedReader br = new BufferedReader(new FileReader(name))) {

			String line;
			int counter = 0;
//			while ((line = br.readLine()) != null) {
			// counter < 1000 because we only have 1000 items
			while (counter < 1000) {
				line = br.readLine();
//				String[] splited = line.split("\\s+");							// split based on white spaces
//				items.get(counter).name = splited[0];
				items.get(counter).name = line;

//				System.out.println("Item id: " + items.get(counter).Id + ", Item name: " + items.get(counter).name);
				counter++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void readStoreItems(ArrayList<POI> pois, String name)
	{
		//"./datasets/Amsterdam/poi/originals/ItemsPerStoreAMS.txt"
		double maxcost =0;
		try (BufferedReader br = new BufferedReader(new FileReader(name))) {

			String line;
			while ((line = br.readLine()) != null) {

				String[] splited = line.split("\\s+");							// split based on white spaces

				int poi = parseInt(splited[0]);
				int  itm = parseInt(splited[1]);
				//System.out.println(line);
				if(itm>1000)continue;
				//System.out.println(splited[2] + ": "+ splited[2].length());
				pois.get(poi).items[itm] = Double.parseDouble(splited[2]);
				if(pois.get(poi).items[itm]>maxcost) maxcost = pois.get(poi).items[itm];

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i=0;i<pois.size();i++)
		{
			POI p = pois.get(i);
			for(int j=0;j<p.items.length;j++)
			{
				double val = p.items[j]/maxcost;
				p.items[j] = Math.round(val * 100.0) / 100.0;
			}
		}

	}





	public static void GetWareDist(double [] wareDist, Graph graph, ArrayList <POI> P, Node n1, double maxd)
	{



		for (int i=0;i<P.size();i++)
		{
			POI p = P.get(i);
			Edge e = graph.getEdge(p.eId);
			p.n1 = e.getNode0();
			p.n2 = e.getNode1();
			Node n = p.n1;
			double s1 = n.getAttribute("long") ;
			s1 = s1 - p.longitude;
			s1 = s1*s1;

			double s2 = n.getAttribute("lat") ;
			s2 = s2 - p.latitude;
			s2 = s2*s2;
			double S1 = Math.sqrt(s1+s2);

			n = p.n2;
			s1 = n.getAttribute("long") ;
			s1 = s1 - p.longitude;
			s1 = s1*s1;

			s2 = n.getAttribute("lat") ;
			s2 = s2 - p.latitude;
			s2 = s2*s2;
			double S2 = Math.sqrt(s1+s2);
			if (S1<S2) p.near=1;
			else p.near=2;
			p.distance = S1;
			p.distance2=S2;
//			p.distance2 = graph.getEdge(p.eId).getAttribute("weight");
//			p.distance2 = p.distance2 - p.distance;
		}

		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE ,"result", "weight");
		dijkstra.init(graph);
		dijkstra.setSource(n1);
		dijkstra.compute();
		for(int j=0;j<P.size();j++)
		{
			POI p1 = P.get(j);
			//System.out.println(p1.n1.getId());
			double d1 = dijkstra.getPathLength(p1.n1)+p1.distance;
			double d2 = dijkstra.getPathLength(p1.n2)+p1.distance2;
//			if(p1.near==1)
//			{
//				d1  = d1 + p1.distance;
//				d2  = d2 + p1.distance2;
//			}
//			else {
//				d1  = d1 + p1.distance2;
//				d2  = d2 + p1.distance;
//			}
//			if (d1<d2) wareDist[j]=	Math.round((d1/maxd) * 100.0) / 100.0;
//			else wareDist[j]=Math.round((d2/maxd) * 100.0) / 100.0;
//			if (d1<d2) wareDist[j]=	precision(d1/maxd);
			if (d1<d2) wareDist[j]=	precision(d1);
//			else wareDist[j]= precision(d2/maxd);
			else wareDist[j]= precision(d2);

		}

	}





	public static double [] wareToCluster(Cluster cluster, double [] wareDist)
	{
		double [] D = new double [cluster.memberClusters.size()];

		for (int i = 0; i<cluster.memberClusters.size();i++)
		{
			//double min = wareDist[cluster.memberClusters.get(i).memberPois.get(0).ID];
			double min = 10000;
			for (int j = 0; j< cluster.memberClusters.get(i).memberPois.size(); j++ )
			{
				if (wareDist[cluster.memberClusters.get(i).memberPois.get(j).ID]<min)
				{
					min = wareDist[cluster.memberClusters.get(i).memberPois.get(j).ID];
				}
			}
			D[i] = min;
		}

		return D;

	}





	public static Comparator<Route> idComparator = new Comparator <Route>(){

		@Override
		public int compare(Route c1, Route c2) {
			return (int) (c1.cost - c2.cost);
		}
	};



	public static boolean check_Avl(Route route, ArrayList<Integer> items)
	{

		int [] flags = new int [items.size()];
		for (int i = 0; i < route.shop.size(); i++)
		{
			for (int j = 0; j < flags.length; j++) {
				if(route.shop.get(i).items[items.get(j)]>0)
				{
					flags[j]=1;
				}
			}
		}
		for (int i = 0; i < flags.length; i++) {
			if(flags[i]!=1)return false;
		}
		return true;
	}





	public static void check_sequence (ArrayList<Route> bruteR)
	{
		for (int i =1; i < bruteR.size(); i++) {
			if(bruteR.get(i-1).distance<bruteR.get(i).distance)
			{
				if(bruteR.get(i-1).cost<=bruteR.get(i).cost)
				{
					bruteR.remove(i);
					i = i-1;
				}
			}
			else if (bruteR.get(i-1).distance==bruteR.get(i).distance) {
				if(bruteR.get(i-1).cost<bruteR.get(i).cost)
				{
					bruteR.remove(i);
					i = i-1;
				}
				else
				{
					bruteR.remove(i-1);
					i = i-1;
				}
			}

		}

	}




	public static ArrayList<Route> Brute(ArrayList<POI> poi, double [][] sp, double [] wp, ArrayList<Integer> ItemList, ArrayList<Item> Items)
	{
		ArrayList<Route> firstArrayList = new ArrayList<Route>();
		ArrayList<Route> Result = new ArrayList<Route>();

		for(int i=0; i< poi.size()-2;i++)
		{
			for(int j=i+1; j< poi.size()-1;j++)
			{
				for(int k = j+1; k<poi.size();k++)
				{
					Route route = new Route();
					route.shop.add(poi.get(i));
					route.shop.add(poi.get(j));
					route.shop.add(poi.get(k));
					firstArrayList.add(route);
				}
			}
		}

		for(int i=firstArrayList.size()-1; i>=0 ;i--)
		{
			if(!check_Avl(firstArrayList.get(i), ItemList))
			{
				firstArrayList.remove(i);
			}
			else {
				firstArrayList.get(i).items.addAll(ItemList);
				firstArrayList.get(i).cost = get_min_cost(firstArrayList.get(i), Items, poi);
				firstArrayList.get(i).distance = 0;
				if(firstArrayList.get(i).cost>3.5 && firstArrayList.get(i).cost<3.55)
				{
					firstArrayList.get(i).print();
				}
			}
		}
		System.out.println(firstArrayList.size());
		return Result;
	}








	public static void helper (ArrayList < Route > combinations, int data[], int start, int end, int index, ArrayList<POI> pois, ArrayList<Integer> ItemList, ArrayList <Item > items)
	{
		if (index == data.length)
		{
			//int[] combination = data.clone ();
			Route route = new Route();
			for (int i = 0; i < data.length; i++) {
				route.shop.add(pois.get(data[i]));
			}
			if(check_Avl(route, ItemList))
			{
				for (int i = 0; i < ItemList.size(); i++) {
					route.items.add(ItemList.get(i));
				}
				route.cost = get_min_cost(route, items, pois);
				combinations.add(route);
			}
		}
		else if (start <= end)
		{
			//int[] combination = data.clone ();
			//if(!combinations.contains(combination)){combinations.add (combination);}
			data[index] = start;
			helper (combinations, data, start + 1, end, index + 1, pois, ItemList, items);
			helper (combinations, data, start + 1, end, index, pois, ItemList, items);
		}
	}


	public static ArrayList < Route > generate (int n, int r, ArrayList<POI> pois, ArrayList<Integer> ItemList, ArrayList <Item > items)
	{
		ArrayList < Route > combinations = new ArrayList <> ();
		for(int i=1; i<=r ; i++)
		{
			helper (combinations, new int[i], 0, n - 1, 0, pois, ItemList, items);
		}
		return combinations;
	}








	public static ArrayList<Route> LinearSkyline(ArrayList<Route> R)
	{

		ArrayList<Route> result = new ArrayList<Route>();
		for(int i=0;i<R.size();i++)
		{
			result.add(R.get(i));
			check_Lin_Skyline(result);

		}
		return result;
	}




	public static void Neighbor_Gen(ArrayList<POI> P, double [][]sp, double [] cp) {
		for (int i = 0; i< P.size(); i++)
		{
			POI poi = P.get(i);
			if(poi.inactive==1)continue;
			for (int j=0; j< P.size();j++)
			{
				if(P.get(j).inactive==1) continue;
				if (i!=j)
				{
					Neighbor neigh = new Neighbor();
					neigh.Id = P.get(j).ID;
					neigh.distance = sp [i][j] + cp[j];
					poi.neighbors.add(neigh);
				}
			}
			Collections.sort(P.get(i).neighbors,new Nei_Comp());
		}
	}



	public static double get_min_cost(Route route, ArrayList<Item> items, ArrayList<POI> pois)
	{
		double cost=0;
		int [] flags = new int [route.shop.size()];
		route.items_sh.clear();
		for(int i = 0; i< route.items.size();i++)
		{
			Item itm = items.get(route.items.get(i));
			for (int j=0;j< itm.p.size();j++)
			{
				POI poi = pois.get(itm.p.get(j));
				if(route.shop.contains(poi) && poi.items[itm.Id]>0)
				{
					cost = cost + poi.items[itm.Id];
					int idx = route.shop.indexOf(poi);
					flags[idx]=1;
					IS_Pair iPair = new IS_Pair();    // nahin uncommented from here
					iPair.item_id = itm.Id;
					iPair.shop_id = poi.ID;
					iPair.cost = poi.items[itm.Id];
					route.items_sh.add(i, iPair);     // nahin uncommented till here
					break;
				}
			}
		}

//		for (int i = 0; i<flags.length-1 ; i++)
//		{
//			if (flags[i]!=1)
//				{cost=-1;}
//		}
		cost = Math.round(cost * 100.0) / 100.0;


		return cost;

	}







	public static double get_min_cost2(Route route, ArrayList<Item> items, ArrayList<POI> pois)
	{
		double cost=0;
		int [] flags = new int [route.shop.size()];
		for(int i = 0; i< route.items.size();i++)
		{
			Item itm = items.get(route.items.get(i));
			for (int j=0;j< itm.p.size();j++)
			{
				POI poi = pois.get(itm.p.get(j));
				if(route.shop.contains(poi) && poi.items[itm.Id]>0)
				{
					cost = cost + poi.items[itm.Id];
					int idx = route.shop.indexOf(poi);
					flags[idx]=1;
					break;
				}
			}
		}

		//if(cost==0)System.out.println("zerxxxx: "+ route.items.size());

		//cost = Math.round(cost * 100.0) / 100.0;

		//if(cost==0)System.out.println("zerxxxx11111: "+ route.items.size());



		return cost;

	}






	public static double Linear(Route firstRoute, Route midRoute, Route lastRoute)
	{
		double cost =0;

		//distance = ((midRoute.cost - firstRoute.cost) * (firstRoute.distance-lastRoute.distance))/(firstRoute.cost-lastRoute.cost) + firstRoute.distance;
		cost = ((midRoute.distance - firstRoute.distance) * (firstRoute.cost-lastRoute.cost))/(firstRoute.distance-lastRoute.distance) + firstRoute.cost;


		return cost;
	}

	public static void check_Lin_Skyline( ArrayList<Route>result)
	{
		if(result.size()>2)
		{
			boolean iteration = true;
			while(iteration)
			{
				Route lastRoute = result.get(result.size()-1);
				Route midRoute = result.get(result.size()-2);
				Route firstRoute = result.get(result.size()-3);
				//double distance = Linear(firstRoute, midRoute, lastRoute);
				double cost = Linear(firstRoute, midRoute, lastRoute);

				if (midRoute.cost>cost)
				{
					result.remove(midRoute);
				}
				else {
					iteration=false;
				}
				if(result.size()<3) break;


			}
		}
	}





	public static double Linear_cost(Route firstRoute, Route midRoute, Route lastRoute)
	{
		double distance =0;

		distance = ((midRoute.cost - firstRoute.cost) * (firstRoute.distance-lastRoute.distance))/(firstRoute.cost-lastRoute.cost) + firstRoute.distance;


		return distance;
	}

	public static void check_Lin_Skyline_cost( ArrayList<Route>result)
	{
		if(result.size()>2)
		{
			boolean iteration = true;
			while(iteration)
			{
				Route lastRoute = result.get(result.size()-1);
				Route midRoute = result.get(result.size()-2);
				Route firstRoute = result.get(result.size()-3);
				double distance = Linear_cost(firstRoute, midRoute, lastRoute);


				if (midRoute.distance>distance)
				{
					result.remove(midRoute);
				}
				else {
					iteration=false;
				}
				if(result.size()<3) break;


			}
		}
	}






	public static double get_BruteDist(int [] permute, Route route, double [] wareDist, double [] c_Dist, double [] [] sp)
	{
		double dist = wareDist[route.shop.get(permute[0]).ID];
		for (int i=0; i< permute.length-1;i++)
		{
			dist = dist + sp[route.shop.get(permute[i]).ID][route.shop.get(permute[i+1]).ID];
		}
		dist = dist + c_Dist[route.shop.get(permute[permute.length-1]).ID];

		return dist;
	}


	public static Route createRoute(Route route, int [] permute, double dist )
	{
		//if(permute.length!=route.shop.size()) {System.out.println("error");}
		Route Rrt = new Route(route);
		Rrt.shop.clear();
		for(int i=0; i<permute.length;i++)
		{
			Rrt.shop.add(route.shop.get(permute[i]));
		}
		Rrt.distance = dist;
		return Rrt;
	}

	public static ArrayList<Route> gen_Brute_Route(ArrayList<Route> combinations, double [] wareDist,double [] C_Dist, double [][] sp, ArrayList<ArrayList<int []>> permutes)
	{
		ArrayList<Route> Result = new ArrayList<Route>();

		for (int i=0; i< combinations.size(); i++)
		{
			Route route = combinations.get(i);
			int length = route.shop.size();
			if(length==1)
			{
				Result.add(route);
			}
			else {
				ArrayList<int []> prmts = permutes.get(length-1);
				int index = 0;
				double min = 10000;
				for(int j =0 ; j<prmts.size();j++)
				{
					double dist = get_BruteDist(prmts.get(j), route, wareDist, C_Dist, sp);
					if(dist<min) {
						min = dist;
						index = j;
					}
				}

				Route route2 = createRoute(route, prmts.get(index), min );
				Result.add(route2);
			}
		}


		return  Result;
	}


	public static ArrayList<Route> Baseline_cluster(Route pRoute, ArrayList <POI> pois, int idx, ArrayList<Integer> ItemList, double [][]sp )
	{
		ArrayList<Route> result = new ArrayList<Route>();
		Comparator<Route> C = new Comp();
		PriorityQueue<Route> Q = new PriorityQueue<Route>(10,C);
		//PriorityQueue<Route> Q = new PriorityQueue<Route>(idComparator);
		Q.add(pRoute);

		while(!Q.isEmpty())
		{

			Route route = Q.poll();
			Route droute = new Route(route);
			//route.print();
			//System.out.println("\nNew");


			int sz = route.items.size()-1;
			if(route.items.size()==ItemList.size())
			{
				result.add(route);
				//System.out.println(route.items.size());

			}
			else {
				route.ndistance=0;
				route.ncost=0;
				for(int i=0; i<pois.size();i++)
				{
					int flag =0;
					if(!route.shop.contains(pois.get(i)))
					{
						for(int j=0;j<ItemList.size();j++)
						{
							if(!route.items.contains(ItemList.get(j)) && pois.get(i).items[ItemList.get(j)]>0)
							{
								flag =1;
								//route.cost += pois.get(i).items[ItemList.get(j)];
								//route.distance += sp[route.shop.get(route.shop.size()-1).ID][pois.get(i).ID];
								route.ncost += pois.get(i).items[ItemList.get(j)];
								route.ndistance = sp[route.shop.get(route.shop.size()-1).ID][pois.get(i).ID];
								route.items.add(ItemList.get(j));

							}

						}
					}
					if(flag==1)
					{
						route.shop.add(pois.get(i));
						route.idx = sz;
						route.distance += route.ndistance;

						route.cost = route.cost + route.ncost;

						//route.print();
						//droute.print();
						Q.add(route);
						break;

					}



				}
				if(droute.shop.size()>1)
				{

					int index = pois.indexOf(droute.shop.get(droute.shop.size()-1));
					if(index<(pois.size()-1))
					{
						//POI poi = droute.shop.get(droute.shop.size()-1);
						droute.shop.remove(droute.shop.size()-1);
						//System.out.println("before " + droute.cost);
						droute.cost -= droute.ncost;
						//System.out.println("after " + droute.cost);
						droute.distance -= droute.ndistance;
						//droute.items.subList(droute.idx, droute.items.size()-1).clear();
						//route.print();
						//droute.print();
						for (int i = droute.items.size()-1; i >= droute.idx; i--)
						{
							droute.items.remove(i);
						}
						//route.print();
						//droute.print();

						for(int i=index+1; i<pois.size();i++)
						{
							int flag1 =0;
							if(!droute.shop.contains(pois.get(i)))// && pois.get(i)!=poi)
							{
								for(int j=0;j<ItemList.size();j++)
								{
									if(!droute.items.contains(ItemList.get(j)) && pois.get(i).items[ItemList.get(j)]>0)
									{
										flag1 =1;
										//droute.cost += pois.get(i).items[ItemList.get(j)];
										//droute.distance = sp[droute.shop.get(droute.shop.size()-1).ID][pois.get(i).ID];
										droute.ncost += pois.get(i).items[ItemList.get(j)];
										droute.ndistance = sp[droute.shop.get(droute.shop.size()-1).ID][pois.get(i).ID];
										droute.items.add(ItemList.get(j));

									}

								}
							}
							if(flag1==1)
							{
								droute.shop.add(pois.get(i));
								droute.distance += droute.ndistance;

								droute.cost = droute.cost + droute.ncost;

								//route.print();
								//droute.print();
								Q.add(droute);
								break;

							}



						}

					}
					//else {System.out.println(index);break;}
				}
			}

		}



		return result;
	}











	//FOR BASELINE ONLY

	public static ArrayList<Route> Baseline(Route pRoute, ArrayList <POI> pois, int idx, ArrayList<Integer> ItemList, double [][]sp )
	{
		ArrayList<Route> result = new ArrayList<Route>();
		Comparator<Route> C = new Comp();
		PriorityQueue<Route> Q = new PriorityQueue<Route>(10,C);
		//PriorityQueue<Route> Q = new PriorityQueue<Route>(idComparator);
		Q.add(pRoute);
		//System.out.println(pois.size());

		while(!Q.isEmpty())
		{

			Route route = Q.poll();
			Route droute = new Route(route);

			int sz = route.items.size()-1;
			if(route.items.size()==ItemList.size())
			{
				result.add(route);
				//System.out.println(route.items.size());

			}
			else {
				route.ndistance=0;
				route.ncost=0;
				route.idx =-1;
				for(int i=0; i<pois.size();i++)
				{
					//int flag =0;
					if(!route.shop.contains(pois.get(i)))
					{
						for(int j=0;j<ItemList.size();j++)
						{
							if(!route.items.contains(ItemList.get(j)) && pois.get(i).items[ItemList.get(j)]>0)
							{
								//flag =1;
								//route.cost += pois.get(i).items[ItemList.get(j)];
								//route.distance += sp[route.shop.get(route.shop.size()-1).ID][pois.get(i).ID];
								route.ncost += pois.get(i).items[ItemList.get(j)];
								route.ndistance = sp[route.shop.get(route.shop.size()-1).ID][pois.get(i).ID];
								route.items.add(ItemList.get(j));

								Route nroute = new Route(route);
								nroute.shop.add(pois.get(i));
								nroute.idx = sz;
								nroute.distance += nroute.ndistance;

								nroute.cost = nroute.cost + nroute.ncost;

								//route.print();
								//droute.print();
								Q.add(nroute);

							}

						}
					}
					/*if(flag==1)
					{
						route.shop.add(pois.get(i));
						route.idx = sz;
						route.distance += route.ndistance;

						route.cost = route.cost + route.ncost;

						//route.print();
						//droute.print();
						Q.add(route);
						break;

					}*/



				}
				if(droute.shop.size()>1 && droute.cost>0)
				{

					int index = pois.indexOf(droute.shop.get(droute.shop.size()-1));
					if(index<(pois.size()-1))
					{
						//POI poi = droute.shop.get(droute.shop.size()-1);
						//System.out.println("before: " + droute.shop.size());
						droute.shop.remove(droute.shop.size()-1);
						//System.out.println("after: " + droute.shop.size());
						//System.out.println("before " + droute.cost);
						droute.cost -= droute.ncost;
						//System.out.println("after " + droute.cost);
						droute.distance = droute.distance - droute.ndistance;
						//droute.items.subList(droute.idx, droute.items.size()-1).clear();
						//route.print();
						//droute.print();
						//System.out.println("beforeI: " + droute.items.size());
						for (int i = droute.items.size()-1; i>= droute.idx; i--)
						{
							droute.items.remove(i);
						}
						//System.out.println("afterI: " + droute.items.size());
						//route.print();
						//droute.print();

						for(int i=index+1; i<pois.size();i++)
						{
							//int flag1 =0;
							if(!droute.shop.contains(pois.get(i)))// && pois.get(i)!=poi)
							{
								for(int j=0;j<ItemList.size();j++)
								{
									if(!droute.items.contains(ItemList.get(j)) && pois.get(i).items[ItemList.get(j)]>0)
									{
										//flag1 =1;
										//droute.cost += pois.get(i).items[ItemList.get(j)];
										//droute.distance += sp[droute.shop.get(droute.shop.size()-1).ID][pois.get(i).ID];
										droute.ncost += pois.get(i).items[ItemList.get(j)];
										droute.ndistance = sp[droute.shop.get(droute.shop.size()-1).ID][pois.get(i).ID];
										droute.items.add(ItemList.get(j));

										Route nroute = new Route(droute);
										nroute.shop.add(pois.get(i));
										nroute.idx = sz;
										nroute.distance += nroute.ndistance;

										nroute.cost = nroute.cost + nroute.ncost;

										//route.print();
										//droute.print();
										Q.add(nroute);

									}

								}
							}

						}

					}
					//else {System.out.println(index);break;}
				}
			}

		}


		//System.out.println(result.size());
		return result;
	}








	public static double precision(double val)
	{
		DecimalFormat df = new DecimalFormat("#.000");
		return Double.valueOf(df.format(val));
	}

	public static double precision2(double val)
	{
		DecimalFormat df = new DecimalFormat("#.00");
		return Double.valueOf(df.format(val));
	}



	public static void change (double [] c, double [][]sp, double v)
	{

		int flag =1;
		int len = c.length;
		while(flag==1)
		{
			flag =0;

			for(int i=0;i<len;i++)
			{
				c[i]=v;
//				for(int j=0;j<len;j++)
//				{
//					if(i!=j)
//					{
//						double d = sp[i][j] + c[j];
//						if(d<c[i])
//						{
//							c[j]+=sp[i][j];
//							flag=1;
//						}
//					}
//				}
			}

		}

	}


	public static ArrayList<Route> Baseline_distance(ArrayList<Route> pRoute, ArrayList <POI> pois, ArrayList<Integer> ItemList, double [][]sp, Route min_r, ArrayList<Item> items, double [] wp, double [] cp )
	{
		ArrayList<Route> result = new ArrayList<Route>();
		PriorityQueue<Route> Q = new PriorityQueue<Route>(10, new Comp_Dist());



		Q.add(pRoute.get(0));
		int next_R = 1;
		min_r.cost = precision2(min_r.cost);

		//System.out.println("\n\nDD: "+min_r.distance);

		while(!Q.isEmpty())
		{

			Route nroute = new Route();
			nroute	= Q.poll();
			Route droute = new Route(nroute);
			Route route = new Route(nroute);

			int sz = nroute.items.size()-1;


			nroute.cost = get_min_cost(route, items, pois);




			if( (nroute.distance) >min_r.distance)
			{
				//System.out.println("broke: "+nroute.distance);
				break;  ///////////////// need function

			}






			if(nroute.items.size()==ItemList.size() && (nroute.distance) <min_r.distance)
			{
				if(result.size()>0)
				{
					Route route2 = result.get(result.size()-1);
					if (route2.distance<nroute.distance)
					{
						if(nroute.cost<route2.cost)
						{
							result.add(nroute);
							check_Lin_Skyline(result);

						}
					}
				}
				else {
					result.add(nroute);  ///////////////// need function
					check_Lin_Skyline(result);
				}

				if(nroute.cost==min_r.cost)   ////////////////////  CHANGED/////////
				{
					//System.out.println("term");
					break;
				}

			}

			///// need termination
//			if(nroute.items.size()==ItemList.size() && nroute.cost== min_r.cost)
//			{
//				System.out.println("terminate");
//				break;
//			}

			POI last_Poi = route.shop.get(route.shop.size()-1);
			//System.out.println(last_Poi.neighbors);
			for(int i=0; i<last_Poi.neighbors.size();i++)
			{
				int flag = 0;
				int ng_id = last_Poi.neighbors.get(i).Id;
				route.ndistance = sp[last_Poi.ID][ng_id];
				if(!route.shop.contains(pois.get(ng_id)))
				{
					for(int j=0;j<ItemList.size();j++)
					{
						if(pois.get(ng_id).items[ItemList.get(j)]>0)
						{
							flag =1;
							if(!route.items.contains(ItemList.get(j)))
							{route.items.add(ItemList.get(j));}

						}

					}
				}


				if(flag == 1)
				{

					route.shop.add(pois.get(ng_id));
					route.idx = sz;
					route.distance = get_Dist(route.shop, wp, sp);
					route.distance+= cp[ng_id] ;
					double v = route.distance;
					route.distance = precision(v);



					route.cost  =  get_min_cost(route, items, pois);
					if((route.distance)< min_r.distance && route.shop.size()<= ItemList.size())// && route.cost>-1)////////////     CHANGED////////////////
					{

						Q.add(route);
					}
					break;
				}


			}

			if(droute.shop.size()>1)
			{

				POI scnd_last_Poi = droute.shop.get(droute.shop.size()-2);
				int index = scnd_last_Poi.neighbors.size();

				for(int i=0; i< scnd_last_Poi.neighbors.size(); i++)
				{
					if(scnd_last_Poi.neighbors.get(i).Id == last_Poi.ID)
					{
						index = i;
					}
				}


				if(index<(scnd_last_Poi.neighbors.size()-1))
				{
					droute.distance = droute.distance - sp[scnd_last_Poi.ID][droute.shop.get(droute.shop.size()-1).ID];

					droute.shop.remove(droute.shop.size()-1);

					for (int i = droute.items.size()-1; i> droute.idx; i--) //   track item
					{
						droute.items.remove(i);
					}

					sz = droute.items.size()-1;


					for(int i=index+1; i<scnd_last_Poi.neighbors.size();i++)
					{
						int flag1 =0;
						POI temp_Poi = pois.get(scnd_last_Poi.neighbors.get(i).Id);
						if(!droute.shop.contains(temp_Poi))
						{
							for(int j=0;j<ItemList.size();j++)
							{
								if( temp_Poi.items[ItemList.get(j)]>0)
								{
									flag1 =1;
									droute.ndistance = sp[scnd_last_Poi.ID][temp_Poi.ID];
									if(!droute.items.contains(ItemList.get(j)))
									{droute.items.add(ItemList.get(j));}


								}

							}
						}

						if(flag1==1)
						{

							droute.shop.add(temp_Poi);
							droute.idx = sz;
							droute.distance = get_Dist(droute.shop, wp, sp);
							droute.distance+= cp[temp_Poi.ID];
							double v = droute.distance ;
							droute.distance = precision(v);

							droute.cost  =  get_min_cost(droute, items, pois);


							if((droute.distance)< min_r.distance)// && droute.cost>-1)
							{
								Q.add(droute);
							}


							break;

						}



					}

				}
			}
			else if (droute.shop.size()==1) {
				if(next_R<pRoute.size())
				{
					Q.add(pRoute.get(next_R));
					next_R++;
				}

			}


		}
		//result.add(min_r);
		check_Lin_Skyline(result);


		return result;
	}






	public static double get_Dist(ArrayList <POI> shop, double [] wp, double [][] sp)
	{
		double Dist= wp[shop.get(0).ID];

		for(int i=0; i<shop.size()-1;i++)
		{
			Dist = Dist + sp[shop.get(i).ID][shop.get(i+1).ID];
		}
//		return Math.round(Dist*1000)/1000;
		return precision(Dist);

	}








	public static ArrayList<Route> Baseline_cost(ArrayList<Route> pRoute, ArrayList <POI> pois, ArrayList<Integer> ItemList, double [][]sp, Route min_r, ArrayList<Item> items, ArrayList<IS_Pair> is_Pairs, double [] wp, double [] cp )
	{
		ArrayList<Route> result = new ArrayList<Route>();
		Comp C = new Comp();
		PriorityQueue<Route> Q = new PriorityQueue<Route>(10,C);
		double upperBound = min_r.distance;
		//PriorityQueue<Route> Q = new PriorityQueue<Route>(10, new Comp());
//		double check_min = pRoute.get(0).cost;

		for(int i=0; i<pRoute.size();i++)
		{
			Q.add(pRoute.get(i));
		}


		while(!Q.isEmpty())
		{

			Route nroute = new Route();
			nroute	= Q.poll();

			nroute.distance = get_Dist(nroute.shop, wp, sp);

			Route droute = new Route(nroute);
			Route route; //= new Route(nroute);
			int sz = nroute.items.size()-1;
//			if(nroute.cost< check_min)
//			{
//				System.out.println("\n\n\n Mismatch\n\n\n");
//				break;
//			}
//			else {
//				check_min = nroute.cost;
//			}

			int route_flag = 1;
			int droute_flag = 1;


			if(nroute.distance>upperBound)
			{
				route_flag = 0 ;  ///     stop_expanding
			}




			if(nroute.items.size() ==ItemList.size() && nroute.distance<upperBound)
			{
				route_flag = 0 ;
				Route rout = new Route(nroute);
				rout.distance = get_Dist(rout.shop, wp, sp) + cp[rout.shop.get(rout.shop.size()-1).ID];

				if(rout.distance<upperBound)
				{
					if(result.size()>0)
					{
						Route route2 = result.get(result.size()-1);
						if(route2.cost==rout.cost && rout.distance<route2.distance)
						{
							result.remove(route2);
							result.add(rout);
							check_Lin_Skyline_cost(result);
						}
						else if (route2.distance>rout.distance)
						{
							if(rout.cost>route2.cost)
							{
								result.add(rout);
								check_Lin_Skyline_cost(result);   /////////////  _CHECK_

							}
						}
					}
					else {
						result.add(rout);
						check_Lin_Skyline_cost(result);   /////////////  _CHECK_
					}
					upperBound = rout.distance;
				}
			}





			POI last_Poi = nroute.shop.get(nroute.shop.size()-1);

			if(route_flag==1)
			{	route = new Route(nroute);
				for(int i=0; i< is_Pairs.size();i++)
				{
					IS_Pair ip = is_Pairs.get(i);
					if(!route.items.contains(ip.item_id))
					{
						route.cost = route.cost + ip.cost;
						route.idx = i;
						route.items.add(ip.item_id);
						route.items_sh.add(ip);

						POI new_Poi = pois.get(ip.shop_id);
						if(new_Poi.ID == ip.shop_id && !route.shop.contains(new_Poi))
						{
							route.shop.add(new_Poi);
						}
						route.distance = get_Dist(route.shop, wp, sp);
						route.cost = Math.round(route.cost * 100.0) / 100.0;
						Q.add(route);
						break;
					}
				}
			}






			IS_Pair last_Pair = droute.items_sh.get(sz);

			last_Poi = droute.shop.get(droute.shop.size()-1);
			for(int i = 0 ; i<sz; i++)
			{
				if(droute.items_sh.get(i).shop_id == last_Pair.shop_id)
				{
					droute_flag=0;
					break;
				}
			}


			droute.items.remove(sz);
			droute.items_sh.remove(sz);
			droute.cost = droute.cost - last_Pair.cost;

			if(droute_flag==1 && last_Pair.shop_id == last_Poi.ID)
			{
				droute.shop.remove(last_Poi);

			}

			for(int i=droute.idx+1 ; i< is_Pairs.size();i++)
			{
				IS_Pair ip = is_Pairs.get(i);
				if(!droute.items.contains(ip.item_id))
				{
					droute.cost = droute.cost + ip.cost;
					droute.idx = i;
					droute.items.add(ip.item_id);
					droute.items_sh.add(ip);

					POI new_Poi = pois.get(ip.shop_id);
					if(new_Poi.ID == ip.shop_id && !droute.shop.contains(new_Poi))
					{
						droute.shop.add(new_Poi);
						droute.distance = get_Dist(droute.shop, wp, sp);
					}

					droute.distance = get_Dist(droute.shop, wp, sp);
					droute.cost = Math.round(droute.cost * 100.0) / 100.0;

					Q.add(droute);



					break;
				}
			}


		}
		check_Lin_Skyline_cost(result);


		return result;
	}








	/// FOR BASELINE






	public static ArrayList<Route> GetBaseRoutes_cost(Cluster cluster, ArrayList<Integer> ItemList, ArrayList <POI> pois, double[] wareTC, double [][]sp, double [] wp, double [] cp, ArrayList<Item> items )
	{

		ArrayList<Route> Result = new ArrayList<Route>();
		//int it =0;
		//int prev =-1;

		ArrayList<Route> pRoute = new ArrayList<Route>();
		PrintWriter writer;


		if(cluster.level==0)
		{
			ArrayList<IS_Pair> is_Pairs = new ArrayList<IS_Pair>();
			for(int i=0; i<cluster.memberPois.size();i++)
			{
				POI p = cluster.memberPois.get(i);
				for(int j = 0; j< ItemList.size();j++)
				{
					if(p.items[ItemList.get(j)]>0)
					{
						IS_Pair pair  = new IS_Pair();
						pair.item_id = ItemList.get(j);
						pair.shop_id = p.ID;
						pair.cost = p.items[ItemList.get(j)];
						is_Pairs.add(pair);
					}
				}
			}





			Collections.sort(is_Pairs, new Pair_Comp());


			try {
				writer = new PrintWriter("list_check.txt", "UTF-8");
				for(int i=0; i<is_Pairs.size();i++)
				{
					is_Pairs.get(i).print(writer);
				}
				writer.close();
			}
			catch (Exception e) {
				// TODO: handle exception
			}


			IS_Pair first_Pair = is_Pairs.get(0);
			Route first_Route = new Route();
			first_Route.cost = first_Pair.cost;
			first_Route.distance = wp[first_Pair.shop_id];
			first_Route.idx = 0;
			first_Route.items.add(first_Pair.item_id);
			first_Route.items_sh.add(first_Pair);
			first_Route.shop.add(pois.get(first_Pair.shop_id));
			first_Route.ndistance =  wp[first_Pair.shop_id];
			pRoute.add(first_Route);
			Route min_r = GetminR(ItemList, items, sp, wp, cp, pois);
			min_r.print();
			//System.out.println(wp[min_r.shop.get(0).ID]);
//			for(int j=0; j<min_r.shop.size()-1;j++)
//			{
//				System.out.println(sp[min_r.shop.get(j).ID][min_r.shop.get(j+1).ID]);
//			}


			ArrayList<Route> pr= Baseline_cost(pRoute,cluster.memberPois, ItemList, sp, min_r, items, is_Pairs, wp, cp);

			Result.addAll(pr);


		}
		return Result;
	}






	public static ArrayList<Route> GetBaseRoutes(Cluster cluster, ArrayList<Integer> ItemList, ArrayList <POI> pois, double[] wareTC, double [][]sp, double [] wp, double [] cp, ArrayList<Item> items )
	{

		ArrayList<Route> Result = new ArrayList<Route>();
		ArrayList<Route> pRoute = new ArrayList<Route>();

		if(cluster.level==0)
		{

			for(int i=0; i<cluster.memberPois.size();i++)
			{
				int shopFlag=0;
				Route nRoute = new Route();
				nRoute.shop.add(cluster.memberPois.get(i));
				nRoute.distance = wp[cluster.memberPois.get(i).ID] + cp[cluster.memberPois.get(i).ID];
				for(int j =0; j< ItemList.size(); j++)
				{
					if(cluster.memberPois.get(i).items[ItemList.get(j)]>0)
					{
						nRoute.items.add(ItemList.get(j));
						shopFlag=1;

					}
				}

				if(shopFlag==1) {
					//pRoute.shop.add(cluster.memberPois.get(i));
					//pRoute.distance = wp[cluster.memberPois.get(i).ID];
					//ArrayList<Route> pr= Baseline_cluster(pRoute,cluster.memberPois, i, ItemList, sp);
					nRoute.cost = get_min_cost(nRoute, items, pois);
					nRoute.idx = 0;
					pRoute.add(nRoute);

				}
			}

			//System.out.println("......................................=============");
			Collections.sort(pRoute, new Comp_Dist() );





			Route min_r = GetminR(ItemList, items, sp, wp, cp, pois);
			//min_r.print();

			/*for (int j=0;j<pRoute.size();j++)
			{
				ArrayList<Route> pr= Baseline(pRoute.get(j),cluster.memberPois, i, ItemList, sp);
				Result.addAll(pr);

			}*/

			ArrayList<Route> pr= Baseline_distance(pRoute,cluster.memberPois, ItemList, sp, min_r, items, wp, cp);
			//ArrayList<Route> pr= Baseline_cost(pRoute,cluster.memberPois, ItemList, sp, min_r, items);

			Result.addAll(pr);
			Result.add(min_r);
			check_sequence(Result);
			check_Lin_Skyline(Result);

			//System.out.println(Result.size());
			return Result;
		}
		return Result;
	}









	public static ArrayList<Route> GetRoutes(Cluster cluster, ArrayList<Integer> ItemList, ArrayList <POI> pois, double[] wareTC, double[] C_TC, double [][]sp, double [] wp, double [] cp, double rdist, double normC)
	{

		ArrayList<Route> Result = new ArrayList<Route>();
		int it = 0;
		int prev = -1;

		if(cluster.level==0)
		{
			for(int i=0; i<cluster.memberPois.size();i++)
			{
				int shopFlag=0;
				//ArrayList<Route> result = new ArrayList<Route>();
				Route pRoute = new Route();
				//	pRoute.shop.add(cluster.memberPois.get(i));
				for(int j =0; j< ItemList.size(); j++)
				{
					if(cluster.memberPois.get(i).items[ItemList.get(j)]>0)
					{
						//System.out.println("SHOP: " + cluster.memberPois.get(i).ID);
						pRoute.items.add(ItemList.get(j));
						pRoute.cost+=cluster.memberPois.get(i).items[ItemList.get(j)];
						//System.out.println(pRoute.cost);
						shopFlag=1;

					}
				}

				if(shopFlag==1) {
					pRoute.shop.add(cluster.memberPois.get(i));
					pRoute.distance = wp[cluster.memberPois.get(i).ID];
					//ArrayList<Route> pr= Baseline_cluster(pRoute,cluster.memberPois, i, ItemList, sp);
					ArrayList<Route> pr= Baseline_cluster(pRoute,cluster.memberPois, i, ItemList, sp);
					Result.addAll(pr);
				}
			}
			//System.out.println(Result.size());
//			for(int i=0; i<Result.size();i++)
//			{
//				int id = Result.get(i).shop.get(Result.get(i).shop.size()-1).ID;
//				Result.get(i).distance+=cp[id];
//			}
			return Result;
		}

		while (!ItemList.isEmpty()) {
			double [] cost = new double[cluster.memberClusters.size()];
			int [] count = new int [cluster.memberClusters.size()];
			double [] distance = new double[cluster.memberClusters.size()];

			for (int i=0; i<cluster.memberClusters.size();i++)
			{
				if(cluster.memberClusters.get(i).memberPois.size()>0) {

					for(int j=0;j<ItemList.size();j++)
					{
						cost[i]+=cluster.memberClusters.get(i).avgcost[ItemList.get(j)]; // if an item is found in itemList, add that item to cost
						if(cluster.memberClusters.get(i).avgcost[ItemList.get(j)]!=0)count[i]++;
						//cost[i]+=cluster.memberClusters.get(i).mincost[ItemList.get(j)];
						//if(cluster.memberClusters.get(i).mincost[ItemList.get(j)]!=0)count[i]++;

					}
					if (cluster.level==1)
//						distance[i]=(wareTC[i] + C_TC[i])/rdist;
						distance[i]=(wareTC[i] + C_TC[i]);
					else if(it != 0)
//						distance[i]=(cluster.clusterDist[prev][i] + C_TC[i])/rdist;
						distance[i]=(cluster.clusterDist[prev][i] + C_TC[i]);
					//System.out.println(distance[i]);
				}
			}
			prev=-1;
			for (int i=0; i<cluster.memberClusters.size();i++)
			{
				if(count[i]>0) { // if the items are found in each cluster
					if (prev==-1 && cost[i]>0)
						prev =i;
					else {
						//if((cost[i]+distance[i])/count[i]<(cost[prev]+distance[prev])/count[prev])
						//	{prev=i;}
						if((( (cost[i]/count[i])/normC + distance[i]) < ( (cost[prev]/count[prev])/normC + distance[prev])) && distance[i]<1.0)
						{prev=i;}

					}
				}
			}
			ArrayList<Integer>itm = new ArrayList<Integer>();
			if(prev>-1) {

				for (int i = ItemList.size()-1;i>=0;i--)
				{
					//if (cluster.memberClusters.get(prev).mincost[ItemList.get(i)]>0)
					if (cluster.memberClusters.get(prev).avgcost[ItemList.get(i)]>0)
					{
						itm.add(ItemList.get(i));
						ItemList.remove(i);
					}
				}
				ArrayList<Route> Result1 = GetRoutes(cluster.memberClusters.get(prev), itm, pois, wareTC, C_TC, sp, wp, cp, rdist, normC);
				//System.out.println(Result1.get(0).shop.size());
				if (Result.size()==0)
				{
					Result=Result1;
				}
				else {
					ArrayList<Route> result2 = new ArrayList<Route>();
					for (int i=0; i< Result.size();i++)
					{
						//System.out.println(Result.get(0).shop.get(0).ID);
						for(int j=0; j<Result1.size();j++)
						{
							//System.out.println(Result1.get(j).distance);
							//	System.out.println(Result.get(i).shop.get(0).ID);

							//	System.out.println(sp[Result1.get(j).shop.get(0).ID][Result.get(i).shop.get(0).ID]);

							Route route = new Route(Result.get(i));
							route.cost += Result1.get(j).cost;
							route.distance += Result1.get(j).distance;
							route.items.addAll(Result1.get(j).items);
							route.shop.addAll(Result1.get(j).shop);
							route.distance += sp[Result.get(i).shop.get(Result.get(i).shop.size()-1).ID][Result1.get(j).shop.get(0).ID];
							result2.add(route);
						}
					}
					Result=result2;
				}

			}

			it++;
		}


		return Result;
	}






	public static Route GetminR(ArrayList<Integer> ItemList, ArrayList <Item > items, double [][]sp, double [] wareDist, double [] C_Dist,  ArrayList <POI> pois)
	{
		Route r = new Route();
		r.cost=0;
		r.distance=0;
		ArrayList <POI> temp = new ArrayList<POI>();

		int dist=0;
		//System.out.println(ItemList.size());
		for(int i=0;i<ItemList.size();i++)
		{
			int it = ItemList.get(i);
			int p = items.get(it).p.get(0);
			//System.out.println("p: "+p+" ID:"+pois.get(p).ID);
			r.cost += pois.get(p).items[it];
			if(!temp.contains(pois.get(p)))
			{
				temp.add(pois.get(p));
				if (temp.size()==1)dist= 0;
				else if(wareDist[p]<wareDist[temp.get(dist).ID]) dist=temp.size()-1;
			}

		}
		//System.out.println(ItemList.size());
		r.shop.add(temp.get(dist));
		r.distance+=wareDist[temp.get(dist).ID];

		temp.remove(dist);
		while(!temp.isEmpty()) {
			int d=0;
			int idx = r.shop.get(r.shop.size()-1).ID;
			for (int i=0;i<temp.size();i++)
			{
				if (sp[idx][temp.get(i).ID]<sp[idx][temp.get(d).ID])d=i;

			}
			r.shop.add(temp.get(d));
			r.distance+=sp[idx][temp.get(d).ID];
			temp.remove(d);
		}
		int id = r.shop.get(r.shop.size()-1).ID;
		r.distance += C_Dist[id];
		r.items = ItemList;

		return r;
	}

	public static void addDistance(ArrayList<Route> Result, double [] cp)
	{
		for(int i=0; i<Result.size();i++)
		{
			int id = Result.get(i).shop.get(Result.get(i).shop.size()-1).ID;
			Result.get(i).distance+=cp[id];
		}

	}


	public static double Trapizoid(ArrayList<Route> R)
	{
		double area = 0;
		if(R.size()==0)return area;

		area = R.get(0).cost*R.get(0).distance;
		for(int i=1;i<R.size();i++)
		{
			area+= (R.get(i).distance-R.get(i-1).distance)*(R.get(i).cost+R.get(i-1).cost)/2;
		}


		return area;

	}



	public static double Trapizoid2(ArrayList<Route> R)
	{
		double area = 0;
		if(R.size()==0)return area;

		area = R.get(0).cost*R.get(0).distance;
		for(int i=1;i<R.size();i++)
		{
			area+= (R.get(i).cost-R.get(i-1).cost)*(R.get(i).distance+R.get(i-1).distance)/2;
		}


		return area;

	}




	public static double checkArea(ArrayList<Route> Q, ArrayList<Route> B)
	{
		double area=0;
		Collections.sort(Q, new Comp_Dist());
		Collections.sort(B, new Comp_Dist());

		ArrayList<Route> B2 = new ArrayList<Route>();

		for(int i=0; i<Q.size();i++)
		{
			Route r1 = Q.get(i);
			for(int j=0;j<B.size();j++)
			{
				Route b1 = B.get(j);
				if(b1.distance<=r1.distance && b1.cost<=r1.cost)
				{
					if(!B2.contains(b1)) B2.add(b1);
				}
			}

		}

		//double A = Trapizoid(Q);
		//area = (A - Trapizoid(B2))/A;
		double A = Trapizoid(B2);
		if(A==0)
		{
//			for (int i = 0; i < Q.size(); i++) {
//				Q.get(i).print();
//			}
//
//			System.out.println("\n\n");
//
//			for (int i = 0; i < B.size(); i++) {
//				B.get(i).print();
//			}
			return 1;
		}

		area = Math.abs((Trapizoid(Q)-A)/A);
		//System.out.println("AR1: "+ area);

		return area;
	}


	public static double checkArea2(ArrayList<Route> Q, ArrayList<Route> B)
	{
		double area=0;



		Collections.sort(Q, new Comp_Dist());
		Collections.sort(B, new Comp_Dist());


		for(int i=0; i<Q.size();i++)
		{
			ArrayList<Route> B1 = new ArrayList<Route>();
			ArrayList<Route> B2 = new ArrayList<Route>();
			Route r1 = Q.get(i);
			B1.add(r1);
			for(int j=0;j<B.size();j++)
			{
				Route b1 = B.get(j);
				if(b1.distance<=r1.distance && b1.cost<=r1.cost)
				{
					if(!B2.contains(b1)) B2.add(b1);
				}
			}
			double A = Trapizoid(B2);
//			if(A==0)
//			{
//				for (int j = 0; j < B.size(); j++) {
//				B.get(j).print();
//				}
//
//				System.out.println("\n\n");
//				r1.print();
//				System.out.println("still");
//			}

			area+= (Trapizoid(B1)-A)/A;

		}

		//System.out.println("AR2: "+ area);

		return area/Q.size();
	}


	public static double checkArea3(ArrayList<Route> Q, ArrayList<Route> B, double [] cover)
	{
		double area=0;

		Route lastRoute = Q.get(Q.size()-1);
		Route newrRoute = new Route();
		int idx = -1;

		for(int i=0; i< B.size();i++)
		{
			if(B.get(i).cost>=lastRoute.cost)
			{
				idx = i;
				break;
			}
		}

		if(idx==-1)
		{
			double t= Trapizoid2(B);
			double q = Trapizoid2(Q);
			area = Math.abs((q-t)/q);
			return area;

		}
		else if(idx==0)
		{
			double t= Trapizoid2(B);
			area = (B.get(idx).cost*(lastRoute.distance-B.get(idx).distance))/(B.get(idx).cost*lastRoute.distance);
			cover[0]+= (t- B.get(idx).distance*B.get(idx).cost)/t;
			return area;

		}
		else if(B.get(idx).cost==lastRoute.cost)
			newrRoute = B.get(idx);
		else {
			newrRoute.cost = lastRoute.cost;
			newrRoute.distance = Linear_cost(B.get(idx-1), lastRoute, B.get(idx));
		}

		ArrayList<Route> B1 = new ArrayList<Route>();


		for(int i=0; i<idx;i++)
		{
			B1.add(B.get(i));

		}

		B1.add(newrRoute);

		double A = Trapizoid2(B1);
		double t= Trapizoid2(B);
		double q = Trapizoid2(Q);
		//System.out.println("A: "+A+" t: "+t);
		area = Math.abs((q-A)/q);
		double val = (t-A)/t;
		cover[0]+= val;
//		if(val<0)
//		{
//			for (int i = 0; i < Q.size(); i++) {
//				Q.get(i).print();
//			}
//
//			System.out.println("\n\n");
//
//			for (int i = 0; i < B1.size(); i++) {
//				B1.get(i).print();
//			}
//
//			System.out.println("\n\n");
//
//			for (int i = 0; i < B.size(); i++) {
//				B.get(i).print();
//			}
//		}
//
		//System.out.println("AR3: "+ area);

		return area;
	}



	public static void pickPoi(ArrayList<POI> pois, int N)
	{
//		int count =0;
		ArrayList<POI> nPois = new ArrayList<POI>();
//		while(count<N)
		// N is 25
		for(int i = 0; i < 51; i++)
		{
//			Random rand = new Random();
//			int rand_int1 = rand.nextInt(pois.size());
//			POI poi = pois.get(rand_int1);
			POI poi = pois.get(i);
//			System.out.println(poi.ID + " " + poi.latitude + " " + poi.longitude);

			if(i % 2 == 1){

				if(!nPois.contains(poi))
				{
					nPois.add(poi);
					//System.out.println("Picked: "+ poi.ID);
//				count++;
				}
			}

		}

		for(int i=0;i<pois.size();i++)
		{
			POI p = pois.get(i);
			if(!nPois.contains(p))
			{
				Arrays.fill(p.items,0.0);
				p.inactive=1;
			}
		}

	}


	public static void 	getItemList(ArrayList<Integer> ItemList, ArrayList<Item>items, JSONArray item_array, int len)
	{
		int count = 0;
		while(count<item_array.length())
		{
			//Random rand = new Random();
			//int rand_int1 = rand.nextInt(items.size());
			int itm_id;
			int total_item_q = 0;
			try {
				itm_id = item_array.getJSONObject(count).getInt("id");
				total_item_q += item_array.getJSONObject(count).getInt("quantity");
//				System.out.println(itm_id +": " + total_item_q);
			}catch(JSONException e){
				System.out.println(e);
				System.out.println("One item skipped cause could not extract item id");
				continue;
			}
			Item itm = items.get(itm_id);
			if(itm.p.size()>0)
			{
				for(int i = 0;i < total_item_q; i++){
					ItemList.add(itm_id);// nahin commented this
				}
//		    	ItemList.add(1); // nahin modified it
//		    	ItemList.add(132); // nahin modified it
			}
			count++;
		}
	}




	public static void main (String[] arg) throws FileNotFoundException, UnsupportedEncodingException {

		int Lid =0;																// number of nodes
		double maxd =0;
		ArrayList <POI> pois = new ArrayList<POI>();							// List of all the POIs
		ArrayList <Item > items = new ArrayList<Item>();						// List of all the items

		Scanner myObj = new Scanner(System.in);  // Create a Scanner object
		//System.out.println("Length: ");

		// String to output
		String shopper_loc = "";
		String customer_loc = "";
		String route_str = "";
		String selected_poi_info = "";
		String item_poi_info = "";

		ArrayList<Integer> ItemListFinal = new ArrayList<Integer>();
//		for(int i = 0; i < arg.length; i++){
//			System.out.println(arg[i]);
//		}

//		int len = myObj.nextInt();
		int len = parseInt(arg[0]);
		//System.out.println("Experiments: ");

//		int len = 10;
		long startTime1 = System.currentTimeMillis();
		Graph graph = new SingleGraph("Amsterdam");	  							// initializes the graph
		Lid=readGraph(graph, Lid);												// generates the graph by reading the input file

		//readItems(items,"./datasets/Amsterdam/poi/originals/StoresPerItemAMSX.txt");

		//graph.display();
		readItems(items,"./datasets/Amsterdam/poi/originals/Item_Cost/StoresPerItemAMS_Cost_INward.txt");														// Reads all the item information from input file

		//readItems(items,"./datasets/Amsterdam/poi/originals/Store_Cardinality/StoresPerItemAMS_SC50.txt");

		readItemNames(items, "./datasets/itemNames/item_dataset.txt");

		readPoi(pois, items.size(), graph, "./datasets/Amsterdam/poi/originals/PoiAMS50.txt");										// Reads all the POI information from input file
		//readPoi(pois, items.size(), graph, "./datasets/Amsterdam/poi/originals/PoiAMSX.txt");										// Reads all the POI information from input file
		//System.out.println("Hello");

		//readStoreItems(pois, "./datasets/Amsterdam/poi/originals/Store_Cardinality/ItemsPerStoreAMS_SC50.txt"); 													// Sets item available per store with unit price
		readStoreItems(pois, "./datasets/Amsterdam/poi/originals/Item_Cost/ItemsPerStoreAMS_Cost_INward.txt"); 													// Sets item available per store with unit price

		pickPoi(pois, 25);

		double [] maxCost = new double [items.size()];

		setnewItem(pois, items);

		sortItems(pois, items, "./datasets/Amsterdam/poi/originals/Item_Cost/StoresPerItemAMS_Cost_INward_Sorted.txt", 50, maxCost);

		//randomizeItem(pois, items, maxCost);
		//normalizeItem(pois, items, maxCost);

		double [][] sp = new double [pois.size()][pois.size()] ;				// 2d array to store shortest path length among the POIs

		//ShortestPath(graph, sp, pois.size(), pois, "./datasets/Amsterdam/poi/originals/ShortestPathPoi50.txt");  //offline

		long endTime1   = System.currentTimeMillis();
		long totalTime1 = endTime1 - startTime1;
		//System.out.println("Time: "+totalTime1);


//		maxd = readSP (sp, pois.size(), "./datasets/Amsterdam/poi/originals/ShortestPathPoi50.txt", maxd);												// Calculated the shortest paths
		maxd = readSP (sp, pois.size(), "./generateSP/test.txt", maxd);												// Calculated the shortest paths
//		System.out.println(maxd);
		//Neighbor_Gen(pois, sp, );
//		int cn=0;

//		for(int i =0;i<pois.size();i++)
//		{
//			if(pois.get(i).inactive==0)cn++;
//		}
//		System.out.println("active: "+cn+" in: "+ (pois.size()-cn));

		Cluster cluster = new Cluster();										// Parent cluster
		cluster.memberPois = pois;												// All POIs under parent cluster
		cluster.createCluster2(pois, sp, 4);										// Creates the full hierarchical cluster
		//cluster.baseCluster(pois, sp, 4);
		cluster.setAvg(items.size());											// Sets average costs per product in the clusters
		cluster.setMin(items.size());											// Sets minimum costs per product in the clusters
		cluster.setClusterDist(sp);												// Calculated minimum distances among clusters


//		for(int i=0;i<cluster.memberClusters.size();i++)
//		{
//			System.out.println("total: "+ cluster.memberClusters.get(i).memberPois.size());
//		}



//		Cluster cluster2 = new Cluster();										// Parent cluster
//		cluster2.memberPois = pois;												// All POIs under parent cluster
//		//cluster.createCluster3(pois, sp, 4);										// Creates the full hierarchical cluster
//		cluster2.baseCluster(pois, sp, 4);
//		cluster2.setAvg(items.size());											// Sets average costs per product in the clusters
//		cluster2.setMin(items.size());											// Sets minimum costs per product in the clusters
//		cluster2.setClusterDist(sp);												// Calculated minimum distances among clusters

		endTime1   = System.currentTimeMillis();
		totalTime1 = endTime1 - startTime1;
		//System.out.println("Time: "+totalTime1);
		int iteration =0;
		//long startTime = System.currentTimeMillis();							// Start timer


		long totaltime=0;
		long totaltimeB=0;
		//long totaltimeP=0;
		double area=0;
		double [] cover = {0};

//		int N =1;
//		int N = myObj.nextInt();  // Read user input
		int N = parseInt(arg[1]);  // Read user input
		double maxA=0;
		/////////////////////////////// 100 Experiments /////////////////////////////
		while(iteration <N) {



			// set warehouse location

			Random rand = new Random();
			int rand_int1 = rand.nextInt(100000);
			String shopper = Integer.toString(rand_int1);

			Node node = graph.getNode(shopper);
//			System.out.println("\nShopper's location: lat: " + node.getAttribute("lat") + " lng: " + node.getAttribute("long"));
			shopper_loc = "{\"shopper_location\":{ \"lat\": " + node.getAttribute("lat") + ", \"lng\": " + node.getAttribute("long") + "}},";
//			System.out.println("{\"shopper_location\":{ \"lat\": " + node.getAttribute("lat") + ", \"lng\": " + node.getAttribute("long") + "}}");

			rand_int1 = rand.nextInt(100000);
			shopper = Integer.toString(rand_int1);
			Node node2 = graph.getNode(shopper);

			customer_loc = "{\"customer_location\": {\"lat\": \"" + node2.getAttribute("lat") + "\", \"lng\": \"" + node2.getAttribute("long") + "\"}},";
//			System.out.println("{\"customer_location\": {\"lat\": \"" + node2.getAttribute("lat") + "\", \"lng\": \"" + node2.getAttribute("long") + "\"}}");


			// Node node = graph.getNode("6900"); // shopper's location
			// Node node2 = graph.getNode("15900"); // customer's location

			long startTP = System.currentTimeMillis();	// Start timer

			double[] wareDist = new double [pois.size()];							// Distance from warehouse
			GetWareDist(wareDist, graph, pois, node, maxd);								// to POIs

			double[] C_Dist = new double [pois.size()];							// Distance from customer location
			GetWareDist(C_Dist, graph, pois, node2, maxd);								// to POIs


			//change(C_Dist, sp, 0);
			//System.out.println("here");

			double [] wareTC = wareToCluster(cluster, wareDist);					// Distance from warehouse to clusters
			double [] CusTC = wareToCluster(cluster, C_Dist);					// Distance from customer to clusters





			ArrayList<Integer> ItemList = new ArrayList<Integer>();					// The shopping list
//			ArrayList<Integer> itemList = new ArrayList<Integer>();
			//PriorityQueue<Route> Q = new PriorityQueue<Route>(new Comp());			// The routes
			ArrayList<Route> Q = new ArrayList<Route>();			// The routes


			//readShoppingList(itemList, "./datasets/Amsterdam/poi/originals/ShoppingList_20.txt");
//			String item_input = "[{\"id\": 20, \"name\": \"TropicanaS\", \"catgeory\": \"Juice\", \"quantity\": 5},{\"id\": 1, \"name\": \"Tropicana\", \"catgeory\": \"Juice\", \"quantity\": 1},{\"id\": 1, \"name\": \"Tropicana\", \"catgeory\": \"Juice\", \"quantity\": 1}, {\"id\": 2, \"name\": \"Eggs\", \"catgeory\": \"Dairy\", \"quantity\": 12}]";
			String item_input = arg[2];
			// need to change item_input to arg[2] and pass it to getItemList function
			JSONArray item_array = new JSONArray();
			try{
				item_array = new JSONArray(item_input);
//				System.out.println(item_arrays.getJSONObject(1));
			}catch(JSONException e){
				System.out.println(e);
			}
//			System.out.println(item_array.length());

//			try {
//				System.out.println(item_array.getJSONObject(1).getString("name"));
//			}catch(JSONException e){
//				System.out.println(e);
//			}

			// counting total items ordered
			int total_item_q = 0;
			for(int i = 0; i < item_array.length(); i++){
				try {
					total_item_q += item_array.getJSONObject(i).getInt("quantity");
				}catch(JSONException e){
					System.out.println(e);
					System.out.println("One item skipped cause could not extract item id");
					continue;
				}
			}

//			System.out.println(total_item_q);


			getItemList(ItemList, items, item_array, len);
			//ItemList = new ArrayList<Integer>(itemList.subList(0, len));
			ArrayList<Integer> itemList = new ArrayList<Integer>(ItemList);
			// ItemListFinal is the array we will get the items whose pois will be outputted
			ItemListFinal = new ArrayList<Integer>(ItemList);

			//itemList = ItemList.;
			//System.out.println(ItemList.size());
			//normalizeItem2(pois, items, maxCost, ItemList);

			double normCost = getnormcost(maxCost, ItemList);


			long startTime = System.currentTimeMillis();	// Start timer

			Route r = GetminR(ItemList, items, sp, wareDist, C_Dist, pois);
			//r.print();
			Q = GetRoutes(cluster, itemList, pois, wareTC,CusTC,  sp, wareDist, C_Dist, r.distance, normCost);
			//Q = GetBaseRoutes(cluster, ItemList, pois, wareTC, sp, wareDist, C_Dist, items);
			//Q = GetBaseRoutes_cost(cluster, ItemList, pois, wareTC, sp, wareDist, C_Dist, items);

			//ItemList = new ArrayList<Integer>(itemList.subList(0, len));
			ItemList = itemList;
			//Route r = GetminR(ItemList, items, sp, wareDist, C_Dist, pois);
			//r.cost = get_min_cost(r, items, pois);        // nahin commented this
			Q.add(r);

			// nahin code starts
			for(int i = 0; i < Q.size(); i++){
				Q.get(i).cost = get_min_cost(Q.get(i), items, pois);
				continue;
			}
			// nahin code ends

			//System.out.println("cost: "+r.cost);
			addDistance(Q, C_Dist);
			Collections.sort(Q, new Comp_Dist());
			check_sequence(Q);
			//check_Lin_Skyline(Q);
			Q=LinearSkyline(Q);

			// nahin code starts
			route_str = "{\"route\": [";
//			System.out.println("Iteration " + iteration);
			for(int i = 0; i < Q.size(); i++){
//				System.out.println("\n\nRoute " + i);
				String route_specific_str = "{\"round\": " + i + ",";
				route_specific_str += Q.get(i).print();
				route_str += route_specific_str + "},";
//				System.out.println(route_specific_str);
				continue;
			}
			route_str = route_str.substring(0, route_str.length() - 1);
			route_str += "]}";
//			System.out.println(route_str);
			// nahin code ends


			iteration = iteration+1;



			long endTime   = System.currentTimeMillis();
			totaltime+= endTime - startTime+(-startTP+startTime);

			//		System.out.println("\n\n\n\n\n\n\nPRINTING\n\n\n\n\n\n\n");

			//if(iteration==1) {


			//		for (int i =0; i < Q.size(); i++) {
			//			System.out.print(Q.get(i).distance+ ", ");
			//		}
			//		System.out.println();
			//
			//		for (int i =0; i < Q.size(); i++) {
			//			System.out.print(Q.get(i).cost+ ", ");
			//		}
			//		System.out.println();









			long comT = -startTP+startTime;

			startTime = System.currentTimeMillis();

			Neighbor_Gen(pois, sp, C_Dist);


			ArrayList<Route> B = new ArrayList<Route>();			// The routes
			//B = GetBaseRoutes(cluster2, ItemList, pois, wareTC, sp, wareDist, C_Dist, items);

			endTime   = System.currentTimeMillis();
			totaltimeB+= endTime - startTime+comT;


//			System.out.println("\n\n");

			//check_sequence(B);
			//Collections.sort(B, new Comp());
			Collections.sort(Q, new Comp());


//		double AR = checkArea3(Q,B, cover);
//			//if(AR>maxA)maxA
//
//			area+= AR;
//			System.out.println("Itr: "+ iteration+" Area: "+ area);
//			System.out.println("Cover: "+ cover[0]);





////////////////************************************************************//////////////////////////////////

//			ArrayList < Route > combinations = generate (pois.size(), ItemList.size(), pois, ItemList, items);
//			ArrayList<ArrayList<int []>> permutes = new ArrayList<ArrayList<int []>>();
//			ReadPermutes(permutes, len);
////		    System.out.println(combinations.size());
//
//
//
//			ArrayList < Route > bruteR = gen_Brute_Route(combinations, wareDist,C_Dist, sp, permutes);
//			Collections.sort(bruteR, new Comp_Dist());
//
//			//bruteR.get(1).print();
//
//			check_sequence(bruteR);
//
////			System.out.println(bruteR.size());
//			ArrayList < Route > bruteR1 = new ArrayList<Route>();
//			for(int i=0;i<bruteR.size();i++)
//			{
//				//bruteR.get(i).print();
//				if(bruteR.get(i).distance!=0 && bruteR.get(i).cost!=0)
//				{
//					//bruteR.get(i).print();
//					bruteR1.add(bruteR.get(i));
//					check_Lin_Skyline(bruteR1);
//				}
//			}
//
//
//			for(int i=0;i<B.size();i++)
//				{
//					B.get(i).print();
//
//				}
//
//			System.out.println("\n\n\n");
//
//			for(int i=0;i<bruteR1.size();i++)
//			{
//				bruteR1.get(i).print();
//
//			}
//


////////////////************************************************************//////////////////////////////////


//			System.out.println(Q);

		}


//		System.out.println(ItemListFinal);

		item_poi_info += "{\"item_poi\":[";

		for(int j = 0; j < items.toArray().length; j++){
			Item item = items.get(j);
			if(ItemListFinal.contains(item.Id)) {

	//			System.out.println("\n\nItem ID: " + item.Id);
				String item_info = "{\"id\":\"" + item.Id + "\", \"name\": \"" + item.name + "\", \"pois\": [";
				for (int k = 0; k < item.p.toArray().length; k++) {
					int shopID = item.p.get(k);
					item_info += "\"" + shopID + "\"";
					if (k != item.p.toArray().length - 1) {
						item_info += ",";
					}
	//			System.out.println("	Shop ID: " + shopID );
				}
				item_info += "]},";
				item_poi_info += item_info;
			}
		}
		item_poi_info = item_poi_info.substring(0, item_poi_info.length() - 1);
		item_poi_info += "]}";
//		System.out.println(item_poi_info);

		int c = 1;
		selected_poi_info = "{\"selected_poi\":[";
		for(int i = 0; i < pois.toArray().length; i++){
			POI poi = pois.get(i);
			if(poi.inactive == 0){
				String poi_str = "{\"id\": \"" + poi.ID + "\", \"eID\": \"" + poi.eId + "\", \"lat\": \"" + poi.latitude + "\", \"lng\": \"" + poi.longitude + "\"},";
//				System.out.println(c + ": " +poi.ID);
//				c++;
//				System.out.println(poi.ID + ": " + poi.items.length );
//				for(int j = 0; j < poi.items.length; i++){
////					Item item =
//					System.out.println(poi.items[j].Id);
//
//				}
//				System.out.println("\n\n");
				selected_poi_info += poi_str;
			}
		}
		selected_poi_info = selected_poi_info.substring(0, selected_poi_info.length() - 1);
		selected_poi_info += "]}";
//		System.out.println(selected_poi_info);

		String final_output = "{[" +shopper_loc + customer_loc + route_str + "]}";
		System.out.println(shopper_loc);
		System.out.println(customer_loc);
		System.out.println(route_str);
		System.out.println(selected_poi_info);
		System.out.println(item_poi_info);


		//System.out.println(final_output);

//		System.out.println("APXT: "+totaltime/N);
		//System.out.println("BASET: "+totaltimeB/N);
		//System.out.println("\n\n\n");
//		System.out.println("AREA: "+ area/N);
		//System.out.println("COVER: "+ cover[0]/N);

		//System.out.println(pois.size());


		//System.out.println(sp[18][35]);






		//startTime = System.currentTimeMillis();

//		ArrayList<Integer> ItemList = new ArrayList<Integer>();					// The shopping list
//		ArrayList<Integer> itemList = new ArrayList<Integer>();	
//
//		
//		
//	    Node node = graph.getNode("6900");
//		
//	    Node node2 = graph.getNode("15900");
//	    
//		double[] wareDist = new double [pois.size()];							// Distance from warehouse 
//		GetWareDist(wareDist, graph, pois, node, maxd);								// to POIs
//
//		
//		double[] C_Dist = new double [pois.size()];							// Distance from customer location
//		GetWareDist(C_Dist, graph, pois, node2, maxd);								// to POIs
//
//		
//		
//		
//		readShoppingList(itemList, "./datasets/Amsterdam/poi/originals/ShoppingList_10.txt");
//		ItemList = new ArrayList<Integer>(itemList.subList(0, len));
//
//		ArrayList < Route > combinations = generate (pois.size(), ItemList.size(), pois, ItemList, items);
//		ArrayList<ArrayList<int []>> permutes = new ArrayList<ArrayList<int []>>();
//		ReadPermutes(permutes, len);
////	    System.out.println(combinations.size());
//
//		
//		
//		ArrayList < Route > bruteR = gen_Brute_Route(combinations, wareDist,C_Dist, sp, permutes);
//		Collections.sort(bruteR, new Comp_Dist());
//		
//		//bruteR.get(1).print();
//		
//		check_sequence(bruteR);
//			
////		System.out.println(bruteR.size());
//		ArrayList < Route > bruteR1 = new ArrayList<Route>();
//		for(int i=0;i<bruteR.size();i++)
//		{
//			//bruteR.get(i).print();
//			if(bruteR.get(i).distance!=0 && bruteR.get(i).cost!=0)
//			{
//				//bruteR.get(i).print();
//				bruteR1.add(bruteR.get(i));
//				check_Lin_Skyline(bruteR1);
//			}
//		}
//		
//		
//		System.out.println("\n\n\n\n\n\n");
//		
//		
//		for (int i =0; i < bruteR1.size(); i++) {
//			bruteR1.get(i).print();
//			//System.out.println(wareDist[bruteR1.get(i).shop.get(0).ID]);
//			for(int j=0; j<bruteR1.get(i).shop.size()-1;j++)
//			{
//				//System.out.println(sp[bruteR1.get(i).shop.get(j).ID][bruteR1.get(i).shop.get(j+1).ID]);
//			}
//		}
//		
//		
//		for (int i =0; i < bruteR1.size(); i++) {
//			System.out.print(bruteR1.get(i).distance+ ", ");
//		}
//		System.out.println();
//		
//		for (int i =0; i < bruteR1.size(); i++) {
//			System.out.print(bruteR1.get(i).cost+ ", ");
//		}
//		System.out.println();
//		
//		endTime   = System.currentTimeMillis();
//		
//		System.out.println("Time: "+ (endTime-startTime));
	}

}












