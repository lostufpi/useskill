package br.ufpi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufpi.datamining.models.ActionDataMining;

/** 
 * Clase para el descubrimiento de padrones
 * @author María Isabel Limaylla Lunarejo
*/
public class MobilePatternDetector {
	
	/**
	 * Identifica el longest common substring (LCS) de un listado 
	 * 
	 * @param	sortedActionsURL listado de acciones por URL			
	 * @return	listado do URL y el LCS para cada uno
	 */	
	public Map<String, String> LCS(Map<String, Map<String, List<ActionDataMining>>> sortedActionsURL) throws Exception{
				
		String entryURL = null;
		String entryEventos = null;
		Map<String, List<String>> listUrls = new HashMap<String, List<String>>();
		Map<String, String> result = new HashMap<String, String>();
		List<String> listado = null;						
		String key2 = null;		
		int index = 0;
		String res = ""; 
        
		//se realiza un loop de las sesiones - si estuviera ordenado x url
		for (Map.Entry<String, Map<String, List<ActionDataMining>>> entry : sortedActionsURL.entrySet()) {
			key2 = entry.getKey();
			index = key2.indexOf("=");
			entryURL = key2.substring(0,index);
			
			for (Map.Entry<String, List<ActionDataMining>> entry2 : entry.getValue().entrySet()) {
				entryEventos = entry2.getKey();
				//actionsList = entry2.getValue();
				
				if (listUrls.containsKey(entryURL)) {					
					listado = listUrls.get(entryURL);
					listado.add(entryEventos);
					listUrls.put(entryURL,listado);
					
				} else {
					listado = new ArrayList<String>();
					listado.add(entryEventos);
					listUrls.put(entryURL,listado);
				}						
			}				
		}
		
		for (Map.Entry<String, List<String>> entry : listUrls.entrySet()) {
			entryURL = entry.getKey();
			listado = entry.getValue();
			
			// Determine size of the array 
	        int n = listado.size(); 
	        
	        //solamente si es mayor que 1
	        if (n>1) {
	        	// Take first word from array as reference 
		        String s = listado.get(0); 
		        int len = s.length(); 
		  
		        res = ""; 
		  
		        for (int i = 0; i < len; i++) { 
		            for (int j = i + 1; j <= len; j++) { 
		  
		                // generating all possible substrings 
		                // of our reference string arr[0] i.e s 
		                String stem = s.substring(i, j); 
		                int k = 1; 
		                for (k = 1; k < n; k++)  
		  
		                    // Check if the generated stem is 
		                    // common to all words 
		                    //if (!arr[k].contains(stem)) 
		                    if (!listado.get(k).contains(stem)) 
		    	                break; 
		                  
		                // If current substring is present in 
		                // all strings and its length is greater   
		                // than current result 
		                if (k == n && res.length() < stem.length()) 
		                    res = stem; 
		            } 
		        } 
		        if (res!="") {
		        	result.put(entryURL+ "-"+ n, res);
		        }		        
	        }	  
		}		
	return result; 	
    }
	
}
