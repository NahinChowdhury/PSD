import java.io.PrintWriter;

public class IS_Pair {
	int shop_id;
	int item_id;
	double cost;
	
	void print (PrintWriter writer)
	{
		writer.print("Shop: "+shop_id+ "  item_id: "+ item_id+ "  cost: "+ cost);
		writer.println();
	}

}
