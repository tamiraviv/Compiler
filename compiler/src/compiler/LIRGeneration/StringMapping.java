package compiler.LIRGeneration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StringMapping {
	private Map<String, String> map;
	private int count;
	
	public StringMapping()
	{
		this.map = new HashMap<>();
		this.count = 0;
	}
	
	public String addString(String quote)
	{
		String strName;
		
		if(!map.containsKey(quote))
		{
			strName = "str" + count;
			count++;
			map.put(quote, strName);
		}
		else
			strName = map.get(quote);
		
		return strName;
	}
	
	public Set<Map.Entry<String, String>> getStringMapping()
	{
		return map.entrySet();
	}
}
