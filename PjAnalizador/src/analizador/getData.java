package analizador;



import java.io.IOException;
import java.util.ArrayList;
public class getData {
	private static  ArrayList<String> integers;
	
	public getData(String test) throws IOException
    {	
		
		integers = new ArrayList<String >();
		String[] integerStrings = test.split(" "); 
		// Splits each spaced integer into a String array.
		int[] integer= new int[integerStrings.length]; 
		// Creates the integer array.
		for (int i = 0; i < integer.length; i++){
		    integers.add(integerStrings[i]); 
		  
		//Parses the integer for each string.
		}
		for (int i = 0; i < integer.length; i++){
			 System.out.println(integers.get(i));
		}
		
		
    }
    public ArrayList<String> getLineas()
    {	
    	return integers;
    }
	
	

}
