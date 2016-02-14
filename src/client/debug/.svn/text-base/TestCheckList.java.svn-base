package client.debug;

import java.util.LinkedList;

public class TestCheckList {
	
	public static boolean errorList( LinkedList<Long> list) {
		
		for( int i = 0 ; i < list.size() - 1 ; i++)
			for( int a = i+1 ; a < list.size() ; a++)
				if(list.get(i).compareTo(list.get(a)) == 0)
					return true;
		
		return false;
		
	}
	
}
