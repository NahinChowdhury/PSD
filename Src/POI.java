import java.util.ArrayList;

import org.graphstream.graph.Node;

public class POI {
	int ID;
	int inactive;
	double longitude;
	double latitude;
	String eId;
	double distance;
	double distance2;
	Node n1;
	Node n2;
	int  near;
	double[]items;
	ArrayList<Neighbor> neighbors =  new ArrayList<Neighbor>();
	void setvalues(int id, double lon, double lat, String e, double d )
	{
		ID= id;
		longitude = lon;
		latitude = lat;
		eId = e;
		distance = d;
		inactive=0;
	};

}
