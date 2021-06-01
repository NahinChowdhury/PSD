import java.util.ArrayList;
import java.util.Arrays;

//import org.graphstream.graph.Node;

public class Cluster {
	
	ArrayList<POI> memberPois = new ArrayList<POI>();
	ArrayList<Cluster> memberClusters = new ArrayList<Cluster>();
	ArrayList< ArrayList<POI> > expendable = new ArrayList<ArrayList<POI>>();
	double[] avgcost;
	double[] mincost;
	double[][] clusterDist;
	
	int leaf =8;
	int [] product_count;
	int level=1;
	Cluster parentCluster;
	
	
	double interdistance (double x1, double y1, double x2, double y2)
	{
		x1 = x1 - x2;
		y1 = y1 - y2;
		x1 = x1*x1;
		y1 = y1*y1;
		return Math.sqrt(x1+y1);
	};
	
	
	void baseCluster(ArrayList <POI> pois, double [][] sp, int Csize)
	{
		/*for(int i=0;i<Csize;i++)
        {
        	//luster c = new Cluster();
        	//memberClusters.add(c);
        }*/
		level=0;
		
	}
	
	void createCluster(ArrayList <POI> pois, double [][] sp, int Csize )
	{
		POI mnlong, mnlat, mxlong, mxlat;
		mnlong=mxlong= pois.get(0);
		mnlat=mxlat= pois.get(0);
        for(int i=0;i<Csize;i++)
        {
        	Cluster c = new Cluster();
        	memberClusters.add(c);
        }
		for (int i=0;i<pois.size();i++)
		{
			
			if (pois.get(i).longitude>mxlong.longitude) mxlong=pois.get(i);
			else if (pois.get(i).longitude<mnlong.longitude) {
				mnlong = pois.get(i);
				
			}
			if (pois.get(i).latitude>mxlat.latitude) mxlat=pois.get(i);
			else if (pois.get(i).latitude<mnlat.latitude) {
				mnlat = pois.get(i);
				
			}
			
		}
		
		for(int i =0;i<pois.size();i++)
		{
			int idx1=0;
			int idx2=0;
			double min1, min2;
			if (sp[i][mxlong.ID] < sp[i][mnlong.ID])
			{
				idx1 = 0;
				min1 = sp[i][mxlong.ID];
			}
			else {
				idx1 = 1;
				min1 = sp[i][mnlong.ID];
			}
			
			if (sp[i][mxlat.ID] < sp[i][mnlat.ID])
			{
				idx2 = 2;
				min2 = sp[i][mxlat.ID];
			}
			else {
				idx2 = 3;
				min2 = sp[i][mnlat.ID];
			}
			if(min2<min1) idx1=idx2;
			
			memberClusters.get(idx1).memberPois.add(pois.get(i));
			
		}
		
		for(int i=0;i<Csize;i++)
        {
        	Cluster c = memberClusters.get(i);
        	c.parentCluster = this;
        	if(c.memberPois.size()>16)
        	{
        		c.createCluster(c.memberPois, sp, 4);
        		c.level=level+1;
        	}
        	else c.level=0;
        	
        }

	};
	
	
	
	
	
	/*void createCluster2(ArrayList <POI> pois, double [][] sp, int Csize )
	{
		POI mnlong, mnlat, mxlong, mxlat;
		mnlong=mxlong= pois.get(0);
		mnlat=mxlat= pois.get(0);
		product_count = new int[pois.get(0).items.length];
		double mnlg, mnlt, mxlg, mxlt, avglg, avglt;
		mnlg=mxlg = pois.get(0).longitude;
		mnlt=mxlt = pois.get(0).latitude;
        for(int i=0;i<Csize;i++)
        {
        	Cluster c = new Cluster();
        	c.product_count = new int[pois.get(0).items.length];
        	memberClusters.add(c);
        }
		for (int i=0;i<pois.size();i++)
		{
			
			if (pois.get(i).longitude>mxlg) mxlg=pois.get(i).longitude;
			else if (pois.get(i).longitude<mnlg) {
				mnlg = pois.get(i).longitude;
				
			}
			if (pois.get(i).latitude>mxlt) mxlt=pois.get(i).latitude;
			else if (pois.get(i).latitude<mnlt) {
				mnlt = pois.get(i).latitude;
				
			}
			
		}
		avglg = (mnlg+mxlg)/2;
		avglt = (mnlt+mxlt)/2;
		mnlg = (mnlg+avglg)/2;
		mxlg = (mxlg+avglg)/2;
		mnlt = (mnlt+avglt)/2;
		mxlt = (mxlt+avglt)/2;
		
		for (int i=0;i<pois.size();i++)
		{
			if(interdistance(mxlg,mxlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mxlg,mxlt, mnlong.longitude, mnlong.latitude))
					mnlong = pois.get(i);
			if(interdistance(mxlg,mnlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mxlg,mnlt, mnlat.longitude, mnlat.latitude))
					mnlat = pois.get(i);
			if(interdistance(mnlg,mnlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mnlg,mnlt, mxlong.longitude, mxlong.latitude))
					mxlong = pois.get(i);
			if(interdistance(mnlg,mxlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mnlg,mxlt, mxlat.longitude, mxlat.latitude))
					mxlat = pois.get(i);

		}
		
		
		for(int i =0;i<pois.size();i++)
		{
			int idx1=0;
			int idx2=0;
			double min1, min2;
			if (sp[i][mxlong.ID] < sp[i][mnlong.ID])
			{
				idx1 = 0;
				min1 = sp[i][mxlong.ID];
			}
			else {
				idx1 = 1;
				min1 = sp[i][mnlong.ID];
			}
			
			if (sp[i][mxlat.ID] < sp[i][mnlat.ID])
			{
				idx2 = 2;
				min2 = sp[i][mxlat.ID];
			}
			else {
				idx2 = 3;
				min2 = sp[i][mnlat.ID];
			}
			if(min2<min1) idx1=idx2;
			
			memberClusters.get(idx1).memberPois.add(pois.get(i));
			
		}
		
		for(int i=0;i<Csize;i++)
        {
        	Cluster c = memberClusters.get(i);
        	c.parentCluster = this;
        	if(c.memberPois.size()>4)
        	{
        		c.createCluster2(c.memberPois, sp, 4);
        		c.level=level+1;
        	}
        	else c.level=0;
        	
        }

	};

	*/
	
	
	
	
	
	void createCluster2(ArrayList <POI> pois, double [][] sp, int Csize )
	{
		POI mnlong, mnlat, mxlong, mxlat;
		mnlong=mxlong= pois.get(0);
		mnlat=mxlat= pois.get(0);
		product_count = new int[pois.get(0).items.length];
		
		double mnlg, mnlt, mxlg, mxlt, avglg, avglt;
		mnlg=mxlg = pois.get(0).longitude;
		mnlt=mxlt = pois.get(0).latitude;
		
		//initialize memeber clusters
        for(int i=0;i<Csize;i++)
        {
        	Cluster c = new Cluster();
        	c.product_count= new int[pois.get(0).items.length];
        	memberClusters.add(c);
        }
        
        //find the maximum and minimum longitute and latitude among the POIs
		for (int i=0;i<pois.size();i++)
		{
			
			if (pois.get(i).longitude>mxlg) mxlg=pois.get(i).longitude;
			else if (pois.get(i).longitude<mnlg) {
				mnlg = pois.get(i).longitude;
				
			}
			if (pois.get(i).latitude>mxlt) mxlt=pois.get(i).latitude;
			else if (pois.get(i).latitude<mnlt) {
				mnlt = pois.get(i).latitude;
				
			}
			
			
			//keeps tarck  of availability of each  item
			for( int j =0; j< pois.get(i).items.length;j++)
			{
				if (pois.get(i).items[j]>0) 
				{
					product_count[j]++;
				}
			}
			
		}
		
		// Finding the center of each quadrant. lg=longitude; lt= latitude;
		avglg = (mnlg+mxlg)/2;
		avglt = (mnlt+mxlt)/2;
		mnlg = (mnlg+avglg)/2;
		mxlg = (mxlg+avglg)/2;
		mnlt = (mnlt+avglt)/2;
		mxlt = (mxlt+avglt)/2;
		
		//finding the center POI  for each quadrant. The closest POI to the center of a quadrant is the center POI.
		for (int i=0;i<pois.size();i++)
		{
			if(interdistance(mxlg,mxlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mxlg,mxlt, mnlong.longitude, mnlong.latitude))
					mxlong = pois.get(i);
			if(interdistance(mxlg,mnlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mxlg,mnlt, mnlat.longitude, mnlat.latitude))
					mnlat = pois.get(i);
			if(interdistance(mnlg,mnlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mnlg,mnlt, mxlong.longitude, mxlong.latitude))
					mnlong = pois.get(i);
			if(interdistance(mnlg,mxlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mnlg,mxlt, mxlat.longitude, mxlat.latitude))
					mxlat = pois.get(i);

		}
		
		
		
		//Assigning POIs to the appropriate quadrant. A POI will be closer to the center POI of its assigned quadrant than to the other center POIs.
		for(int i =0;i<pois.size();i++)
		{
			int idx1=0;
			int idx2=0;
			double min1, min2;
			int idx = pois.get(i).ID;
			if (sp[idx][mxlong.ID] < sp[idx][mnlong.ID])
			{
				idx1 = 0;
				min1 = sp[idx][mxlong.ID];
			}
			else {
				idx1 = 1;
				min1 = sp[idx][mnlong.ID];
			}
			
			if (sp[idx][mxlat.ID] < sp[idx][mnlat.ID])
			{
				idx2 = 2;
				min2 = sp[idx][mxlat.ID];
			}
			else {
				idx2 = 3;
				min2 = sp[idx][mnlat.ID];
			}
			if(min2<min1) idx1=idx2;
			
			addproduct(memberClusters.get(idx1), pois.get(i));
			memberClusters.get(idx1).memberPois.add(pois.get(i));
			
		}
		
		
		//Repartition each quadrant.
		for(int i=0;i<Csize;i++)
        {
        	Cluster c = memberClusters.get(i);
        	c.parentCluster = this;
        	if(c.memberPois.size()>4)
        	{
        		c.createCluster2(c.memberPois, sp, 4);
        		c.level=level+1;
        	}
        	else c.level=0;
        	
        }

	};


	
	
	
	//Keeps track of each item in product_count.
	void addproduct(Cluster c, POI p)
	{
		for( int j =0; j< p.items.length;j++)
		{
			if (p.items[j]>0) 
			{
				c.product_count[j]++;
			}
		}
	};
	
	
	
	//Assigns the expendable POIs of 2nd cluster to the 1st cluster from the parameter
	void Exchange(Cluster c2, Cluster c1, int c2_idx, int c1_idx )
	{
		//System.out.println("ex");
		for(int i =0; i<c1.expendable.get(c2_idx).size();i++)
		{
			POI poi = c1.expendable.get(c2_idx).get(i);
			//System.out.println(poi.ID);
			c2.memberPois.add(poi);
			c1.memberPois.remove(c1.expendable.get(c2_idx).get(i));
		}
	}
	;
	
	
	void Calc_region (ArrayList<Cluster> memberClusters)
	{
		
		for(int i=0;i<memberClusters.size()-1;i++)
		{
			Cluster c1 = memberClusters.get(i);
			for(int j = i+1; j< memberClusters.size();j++)
			{
				Cluster c2 = memberClusters.get(j);
				int cnt1 = 0;
				int cnt2 = 0;
				if(!c1.expendable.get(j).isEmpty())
				{
					//System.out.println("y");
					for(int k =0;k<c1.expendable.get(j).size();k++)
					{
						POI p = c1.expendable.get(j).get(k);
						
						for(int l=0;l<p.items.length;l++)
						{
							if(p.items[l]>0 && c2.product_count[l]==0)
							{
								cnt2 = cnt2+1;
							}
						}
					}
				}
				
				
				if(!c2.expendable.get(i).isEmpty())
				{
					//System.out.println("n");
					for(int k =0;k<c2.expendable.get(i).size();k++)
					{
						POI p = c2.expendable.get(i).get(k);
						for(int l=0;l<p.items.length;l++)
						{
							if(p.items[l]>0 && c1.product_count[l]==0)
							{
								cnt1 = cnt1+1;
							}
						}
					}
				}
				
				
				if(cnt1>cnt2)  Exchange(c2,c1, j, i );
				else if(cnt2>cnt1) Exchange(c1, c2, i, j);
				
			}
		}
		
	}
	
	
	void swapPOI (ArrayList<Cluster> memberClusters, double mnlg, double mnlt, double mxlg, double mxlt, double avglg, double avglt)
	{
		
		for (int i =0 ; i< memberClusters.size();i++)
		{
			Cluster cluster = memberClusters.get(i);
			
			for  (int j=0;j<4; j++)
			{
				//System.out.println(i+ " "+ j);
				ArrayList <POI> pois = new ArrayList<POI>();
				cluster.expendable.add(pois);
			}
			
			if(i==0)
			{
				double tuner = .01;
				double prevlg = avglg;
				int cnt1 [] = cluster.product_count;
				int cnt2 [] = cluster.product_count;
				int flag1=0;
				int flag2=0;
				while(tuner < .2)
				{
					double lng = avglg + (mxlg - avglg)*tuner;
					
					
					for(int j=0; j< cluster.memberPois.size(); j++)
					{
						POI p = cluster.memberPois.get(j);
						
						if(p.longitude>=prevlg &&  p.longitude < lng)
						{
							//System.out.println(prevlg);
							//System.out.println(p.longitude);
							//System.out.println(lng);
							for(int k =0 ; k< p.items.length;k++)
							{
								if(p.items[k]>0)
								{
									cnt1[k] = cnt1[k]-1;
								}
								if(cnt1[k]<=0)
								{
									flag1=1;
									break;
								}
								else {
									//System.out.println(p.ID);
									//cluster.expendable.get(2).add(p);
									
								}
							}
							if(flag1==0)cluster.expendable.get(2).add(p);
						}
						if(flag1==1)break;
						
					}
					if (flag1==1) break;
					tuner = tuner + .05;
					prevlg = lng;
				}
				
				
				tuner = .01;
				double prevlt = avglt;
				while(tuner < .2)
				{
					double lt = avglt + (mxlt - avglt)*tuner;
					
					
					for(int j=0; j< cluster.memberPois.size(); j++)
					{
						POI p = cluster.memberPois.get(j);
						if(p.latitude>=prevlt &&  p.latitude < lt)
						{
							for(int k =0 ; k< p.items.length;k++)
							{
								if(p.items[k]>0)
								{
									cnt2[k] = cnt2[k]-1;
								}
								if(cnt2[k]<=0)
								{
									flag2=1;
									break;
								}
								else {
									//cluster.expendable.get(3).add(p);
								}
							}
							if(flag2==0)cluster.expendable.get(3).add(p);
						}
						if(flag2==1)break;
						
					}
					if (flag2==1) break;
					tuner = tuner + .01;
					prevlg = lt;
					
					
					tuner = tuner + .01;
				}
				
				
			}
			
			
			else if (i==1)
			{
				double tuner = .01;
				double prevlg = avglg;
				int cnt1 [] = cluster.product_count;
				int cnt2 [] = cluster.product_count;
				int flag1=0;
				int flag2=0;
				while(tuner < .2)
				{
					double lng = avglg - (avglg-mnlg)*tuner;
					
					
					for(int j=0; j< cluster.memberPois.size(); j++)
					{
						POI p = cluster.memberPois.get(j);
						if(p.longitude<=prevlg &&  p.longitude > lng)
						{
							for(int k =0 ; k< p.items.length;k++)
							{
								if(p.items[k]>0)
								{
									cnt1[k] = cnt1[k]-1;
								}
								if(cnt1[k]<=0)
								{
									flag1=1;
									break;
								}
								else {
									//cluster.expendable.get(3).add(p);
								}
							}
							if(flag1==0) cluster.expendable.get(3).add(p);
						}
						if(flag1==1)break;
						
					}
					if (flag1==1) break;
					tuner = tuner + .01;
					prevlg = lng;
				}
				
				
				tuner = .01;
				double prevlt = avglt;
				while(tuner < .2)
				{
					double lt = avglt - (avglt-mnlt)*tuner;
					
					
					for(int j=0; j< cluster.memberPois.size(); j++)
					{
						POI p = cluster.memberPois.get(j);
						if(p.latitude<=prevlt &&  p.latitude > lt)
						{
							for(int k =0 ; k< p.items.length;k++)
							{
								if(p.items[k]>0)
								{
									cnt2[k] = cnt2[k]-1;
								}
								if(cnt2[k]<=0)
								{
									flag2=1;
									break;
								}
								else {
									//cluster.expendable.get(2).add(p);
								}
							}
							if(flag2==0)cluster.expendable.get(2).add(p);
						}
						if(flag2==1)break;
						
					}
					if (flag2==1) break;
					tuner = tuner + .01;
					prevlg = lt;
					
					
					tuner = tuner + .01;
				}
				
				
				
			}
			
			else if (i==2)
			{
				double tuner = .01;
				double prevlg = avglg;
				int cnt1 [] = cluster.product_count;
				int cnt2 [] = cluster.product_count;
				int flag1=0;
				int flag2=0;
				while(tuner < .2)
				{
					double lng = avglg - (avglg- mnlg)*tuner;
					
					
					for(int j=0; j< cluster.memberPois.size(); j++)
					{
						POI p = cluster.memberPois.get(j);
						if(p.longitude<=prevlg &&  p.longitude > lng)
						{
							for(int k =0 ; k< p.items.length;k++)
							{
								if(p.items[k]>0)
								{
									cnt1[k] = cnt1[k]-1;
								}
								if(cnt1[k]<=0)
								{
									flag1=1;
									break;
								}
								else {
									//cluster.expendable.get(0).add(p);
								}
							}
							if(flag1==0) cluster.expendable.get(0).add(p);
						}
						if(flag1==1)break;
						
					}
					if (flag1==1) break;
					tuner = tuner + .01;
					prevlg = lng;
				}
				
				
				tuner = .01;
				double prevlt = avglt;
				while(tuner < .2)
				{
					double lt = avglt + (mxlt - avglt)*tuner;
					
					
					for(int j=0; j< cluster.memberPois.size(); j++)
					{
						POI p = cluster.memberPois.get(j);
						if(p.latitude>=prevlt &&  p.latitude < lt)
						{
							for(int k =0 ; k< p.items.length;k++)
							{
								if(p.items[k]>0)
								{
									cnt2[k] = cnt2[k]-1;
								}
								if(cnt2[k]<=0)
								{
									flag2=1;
									break;
								}
								else {
									//cluster.expendable.get(1).add(p);
								}
							}
							if(flag2==0)cluster.expendable.get(1).add(p);
						}
						if(flag2==1)break;
						
					}
					if (flag2==1) break;
					tuner = tuner + .01;
					prevlg = lt;
					
					
					tuner = tuner + .01;
				}
				
				
				
			}
			
			else if (i==3)
			{
				double tuner = .01;
				double prevlg = avglg;
				int cnt1 [] = cluster.product_count;
				int cnt2 [] = cluster.product_count;
				int flag1=0;
				int flag2=0;
				while(tuner < .2)
				{
					double lng = avglg + (mxlg - avglg)*tuner;
					
					
					for(int j=0; j< cluster.memberPois.size(); j++)
					{
						POI p = cluster.memberPois.get(j);
						if(p.longitude>=prevlg &&  p.longitude < lng)
						{
							for(int k =0 ; k< p.items.length;k++)
							{
								if(p.items[k]>0)
								{
									cnt1[k] = cnt1[k]-1;
								}
								if(cnt1[k]<=0)
								{
									flag1=1;
									break;
								}
								else {
									//cluster.expendable.get(1).add(p);
								}
							}
							if(flag1==0)cluster.expendable.get(1).add(p);
						}
						if(flag1==1)break;
						
					}
					if (flag1==1) break;
					tuner = tuner + .01;
					prevlg = lng;
				}
				
				
				tuner = .01;
				double prevlt = avglt;
				while(tuner < .2)
				{
					double lt = avglt - (avglt-mnlt)*tuner;
					
					
					for(int j=0; j< cluster.memberPois.size(); j++)
					{
						POI p = cluster.memberPois.get(j);
						if(p.latitude<=prevlt &&  p.latitude > lt)
						{
							for(int k =0 ; k< p.items.length;k++)
							{
								if(p.items[k]>0)
								{
									cnt2[k] = cnt2[k]-1;
								}
								if(cnt2[k]<=0)
								{
									flag2=1;
									break;
								}
								else {
									//cluster.expendable.get(0).add(p);
								}
							}
							if(flag2==0)cluster.expendable.get(0).add(p);
						}
						if(flag2==1)break;
						
					}
					if (flag2==1) break;
					tuner = tuner + .01;
					prevlg = lt;
					
					
					tuner = tuner + .01;
				}
				
				
				
			}
				
				
		}
		
		Calc_region (memberClusters);
		
		for (int i =0 ; i< memberClusters.size();i++)
		{
			Cluster cluster = memberClusters.get(i);
			cluster.expendable.clear();
			cluster.expendable.trimToSize();
		}
		
		
	};
	
	
	
	void createCluster3(ArrayList <POI> pois, double [][] sp, int Csize )
	{
		POI mnlong, mnlat, mxlong, mxlat;
		mnlong=mxlong= pois.get(0);
		mnlat=mxlat= pois.get(0);
		product_count = new int[pois.get(0).items.length];
		
		double mnlg, mnlt, mxlg, mxlt, avglg, avglt;
		mnlg=mxlg = pois.get(0).longitude;
		mnlt=mxlt = pois.get(0).latitude;
        for(int i=0;i<Csize;i++)
        {
        	Cluster c = new Cluster();
        	c.product_count = new int[pois.get(0).items.length];
        	memberClusters.add(c);
        }
		for (int i=0;i<pois.size();i++)
		{
			
			if (pois.get(i).longitude>mxlg) mxlg=pois.get(i).longitude;
			else if (pois.get(i).longitude<mnlg) {
				mnlg = pois.get(i).longitude;
				
			}
			if (pois.get(i).latitude>mxlt) mxlt=pois.get(i).latitude;
			else if (pois.get(i).latitude<mnlt) {
				mnlt = pois.get(i).latitude;
				
			}
			
			for( int j =0; j< pois.get(i).items.length;j++)
			{
				if (pois.get(i).items[j]>0) 
				{
					product_count[j]++;
				}
			}
			
		}
		avglg = (mnlg+mxlg)/2;
		avglt = (mnlt+mxlt)/2;
		mnlg = (mnlg+avglg)/2;
		mxlg = (mxlg+avglg)/2;
		mnlt = (mnlt+avglt)/2;
		mxlt = (mxlt+avglt)/2;
		
		for (int i=0;i<pois.size();i++)
		{
			if(interdistance(mxlg,mxlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mxlg,mxlt, mnlong.longitude, mnlong.latitude))
					mxlong = pois.get(i);
			if(interdistance(mxlg,mnlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mxlg,mnlt, mnlat.longitude, mnlat.latitude))
					mnlat = pois.get(i);
			if(interdistance(mnlg,mnlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mnlg,mnlt, mxlong.longitude, mxlong.latitude))
					mnlong = pois.get(i);
			if(interdistance(mnlg,mxlt, pois.get(i).longitude, pois.get(i).latitude) < interdistance(mnlg,mxlt, mxlat.longitude, mxlat.latitude))
					mxlat = pois.get(i);

		}
		
		
		for(int i =0;i<pois.size();i++)
		{
			int idx1=0;
			int idx2=0;
			double min1, min2;
			int idx = pois.get(i).ID;
			if(pois.get(i).inactive==0)
			{
				if (sp[idx][mxlong.ID] < sp[idx][mnlong.ID])
				{
					idx1 = 0;
					min1 = sp[idx][mxlong.ID];
				}
				else {
					idx1 = 1;
					min1 = sp[idx][mnlong.ID];
				}
				
				if (sp[idx][mxlat.ID] < sp[idx][mnlat.ID])
				{
					idx2 = 2;
					min2 = sp[idx][mxlat.ID];
				}
				else {
					idx2 = 3;
					min2 = sp[idx][mnlat.ID];
				}
				if(min2<min1) idx1=idx2;
				
				addproduct(memberClusters.get(idx1), pois.get(i));
				memberClusters.get(idx1).memberPois.add(pois.get(i));
			}
			
		}
		
		
		swapPOI(memberClusters, mnlg, mnlt, mxlg, mxlt, avglg, avglt);
		
		
		
		for(int i=0;i<Csize;i++)
        {
        	Cluster c = memberClusters.get(i);
        	c.parentCluster = this;
        	if(c.memberPois.size()>this.leaf)
        	{
        		c.createCluster3(c.memberPois, sp, 4);
        		c.level=level+1;
        	}
        	else c.level=0;
        	
        }

	};


	
	
	
	
	void printCluster()
	{
		for(int i=0;i<memberClusters.size();i++)
		{
			if(memberClusters.get(i).memberPois.size()>4)memberClusters.get(i).printCluster();
			for(int j=0;j<memberClusters.get(i).memberPois.size();j++)
			{
				System.out.print(memberClusters.get(i).memberPois.get(j).ID);
				System.out.print(" ");
			}
			System.out.println();
		}
	};
	
	
	
	
	void setAvg(int size) 
	{
		//int size = memberPois.get(0).items.length;
		avgcost = new double[size];
		int [] count = new int[size];
		for (int i = 0; i< memberPois.size();i++)
		{
			POI poi = memberPois.get(i);
			for (int j=0; j<size;j++)
			{
				avgcost[j] += poi.items[j];
				if(poi.items[j]>0)
					count[j]++;
			}
		}
		for (int j=0; j<size;j++)
		{
			if (count[j]>0)
				avgcost[j]/=count[j];
		}
		
		for (int j=0; j<memberClusters.size();j++)
		{
			memberClusters.get(j).setAvg(size);
		}
	}
	
	
	
	
	
	void setMin(int size) 
	{
		//int size = memberPois.get(0).items.length;
		mincost = new double[size];
		Arrays.fill(mincost, 0);
		for (int i = 0; i< memberPois.size();i++)
		{
			POI poi = memberPois.get(i);
			for (int j=0; j<size;j++)
			{

				if(poi.items[j]>0)
				{					
					if (mincost[j]==0)
						mincost[j]=poi.items[j];
					else if (mincost[j]>poi.items[j])
						mincost[j]=poi.items[j];		
				}
			}
		}
		
		for (int j=0; j<memberClusters.size();j++)
		{
			memberClusters.get(j).setMin(size);
		}
	}
	
	
	
	
	void setClusterDist(double [][] sp)
	{
		if (memberClusters.size()<=1) return;
		int size = memberClusters.size();
		clusterDist = new double[size][size];
		for (int i =0; i< memberClusters.size();i++)
		{			
			for(int j =0 ; j< memberClusters.size();j++)
				
				if(i!=j && clusterDist[i][j]==0 && memberClusters.get(i).memberPois.size()>0 && memberClusters.get(j).memberPois.size()>0)
				{
					int idx1=memberClusters.get(i).memberPois.get(0).ID;
					int idx2=memberClusters.get(j).memberPois.get(0).ID;
					double min = sp[idx1][idx2];
					for(int k=0;k<memberClusters.get(i).memberPois.size();k++)
					{
						for (int l=0;l<memberClusters.get(j).memberPois.size();l++)
						{
							idx1=memberClusters.get(i).memberPois.get(k).ID;
							idx2=memberClusters.get(j).memberPois.get(l).ID;
							if(sp[idx1][idx2]<min)
								min= sp[idx1][idx2];
						}
					}
					clusterDist[i][j]=min;
					clusterDist[j][i]=min;
					
				}
		}
		
		for (int i=0;i<memberClusters.size();i++)
		{
			memberClusters.get(i).setClusterDist(sp);
		}
	};
}
