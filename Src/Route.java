import java.util.ArrayList;

public class Route {

	ArrayList<POI> shop = new ArrayList<POI>();
	ArrayList<Integer> items = new ArrayList<Integer>();
	ArrayList<IS_Pair> items_sh = new ArrayList<IS_Pair>();
	int idx=0;
	double cost=0;
	double distance=0;
	double ncost=0;
	double ndistance=0;


	public Route(Route r)
	{
		//this.shop = r.shop;
		for (int i=0;i<r.shop.size();i++)
		{
			this.shop.add(r.shop.get(i));
		}

		for (int i=0;i<r.items.size();i++)
		{
			this.items.add(r.items.get(i));
			//this.items_sh.add(r.items_sh.get(i));
		}

		for (int i=0;i<r.items_sh.size();i++)
		{
			this.items_sh.add(r.items_sh.get(i));
		}

		//this.items = r.items;
		this.cost = r.cost;
		this.distance = r.distance;
		this.idx = r.idx;
		this.ncost = r.ncost;
		this.ndistance = r.ndistance;


	}


	public Route() {
		// TODO Auto-generated constructor stub
	};
	public String print()
	{
//		System.out.println("Number of shops visited: " + shop.size());
		String visited_shops = "\"visited_shop\": [";
		for(int i=0; i< shop.size();i++)
		{
			visited_shops += "{\"id\": " + shop.get(i).ID + ",\"lat\": " + shop.get(i).latitude + ",\"lng\": " + shop.get(i).longitude + "},";
//			System.out.print("Shop ID: " + shop.get(i).ID + " lat: " + shop.get(i).latitude + ", lng: " + shop.get(i).longitude); // nahin added : lat and lng
//			System.out.println();
		}
		visited_shops = visited_shops.substring(0, visited_shops.length() - 1);
		visited_shops += "]";
//		System.out.println(visited_shops);
//		System.out.println();

//		System.out.println("Cost: " + cost);
//		System.out.println("Distance: " + distance);

		String cost_str = "\"cost\": " + cost + "";
		String dist_str = "\"distance\": " + distance + "";
//		System.out.println(items);

		String item_sh = "\"item_sh\":[";
		for(int i=0; i<items_sh.size();i++)
		{
//			System.out.println("[ item: "+ items_sh.get(i).item_id + "  shop: "+ items_sh.get(i).shop_id+ " Cost: " + items_sh.get(i).cost +" ]");
			item_sh += "{\"item\": "+ items_sh.get(i).item_id + ", \"shop\": "+ items_sh.get(i).shop_id+ ", \"cost\": " + items_sh.get(i).cost +"},";
		}
		item_sh = item_sh.substring(0, item_sh.length() - 1);
		item_sh += "]";
//		System.out.println(item_sh);
//		System.out.println();

		return cost_str + "," + dist_str + "," + visited_shops + "," + item_sh;
	}



}
