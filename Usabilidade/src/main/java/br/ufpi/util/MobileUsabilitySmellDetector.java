package br.ufpi.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import br.ufpi.datamining.models.auxiliar.CountActionsAux;
import br.ufpi.datamining.models.auxiliar.FieldSearch;
import br.ufpi.datamining.models.auxiliar.FieldSearchComparatorEnum;*/

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;

import br.ufpi.datamining.models.ActionDataMining;
import br.ufpi.datamining.models.PageViewActionDataMining;
import br.ufpi.datamining.models.SmellMobileDetMining;
import br.ufpi.datamining.models.auxiliar.MobilePatternsAux;
import br.ufpi.datamining.models.auxiliar.SessionGraph;
import br.ufpi.datamining.models.auxiliar.SessionResultDataMining;
import br.ufpi.datamining.models.auxiliar.TaskMobileSmellAnalysisResult;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysis;
import br.ufpi.datamining.repositories.ActionDataMiningRepository;

/** 
 * Programa que realiza la Deteccion de Usability Smells y el Descubrimiento de Padrones
 * @author María Isabel Limaylla Lunarejo
*/

public class MobileUsabilitySmellDetector {
	
	private static final boolean debug = false;
	
	//tamano x defecto de los patrones
	private static final int MIN_SIZE = 4;
	private static final int MAX_SIZE = 10;

	//minimo de ocurrencias para ser considerado um patron
	private static final int MIN_OCURR = 3; 

	//eventos
	private static final String SINGLETAP = "a";
	private static final String DOUBLETAP = "b";
	private static final String PRESS = "c";
	private static final String SWIPEUP = "d";
	private static final String SWIPEDOWN = "e";
	private static final String SWIPELEFT = "f";
	private static final String SWIPERIGTH = "g";
	private static final String ORIENTATIONCHANGE = "h";
	private static final String PINCHIN = "i";
	private static final String PINCHOUT = "j";
	private static final String CLICK = "k";
	private static final String DOUBLECLICK = "l";
	private static final String MOUSEMOVE = "m";
	private static final String FOCUSOUT = "n";
	private static final String FORM_SUBMIT = "o";
	private static final String ONLOAD = "p";
	private static final String RELOAD = "q";
	private static final String CLOSE = "r";
	private static final String BACK = "s";
	private static final String HASHCHANGE = "t";
	private static final String MOUSEOVER = "U";
	private static final String FOCUSOUT_INPUT = "v";
	
	private List<Long> listIds = null;
	private static String[] eventosTotal = {"singletap", "doubletap", "taphold", "swipeup", "swipedown", "swipeleft", "swiperight", "orientationchange", "pinchin", "pinchout", "click", "dblclick"
			, "mousemove", "focusout", "focusinput", "form_submit", "onload", "reload", "close", "back", "haschange", "mouseover", "swipe", "pinch", "all"};
		
		
	/**
	 * Genera el análisis de descubrimiento de patrones 
	 * 
	 * @param	actions			uma lista de ações
	 * @param	cantRep			cantidad de ocurrencias para ser considerado um patron
	 * @param	tamMin			tamano minimo de un padron
	 * @param	tamMax			tamano maximo de un padron
	 * @return					Un listado de acciones con los padrones identificado
	 */	
	public  List<TaskMobileSmellAnalysisResult> analizePattern (List<ActionDataMining> actions, int cantRep, int tamMin, int tamMax) throws IOException, ParseException{
		
		List<TaskMobileSmellAnalysisResult> detections = new ArrayList<TaskMobileSmellAnalysisResult>();
				
		String entryURL = null;
		String entryEventos = null;
		String patron = null; 
		String patronSec = null;
		int tamano = 0;
		boolean val = false;
				
		List<ActionDataMining> actionsList = new ArrayList<ActionDataMining>();
		Map<String, List<ActionDataMining>> actionsList2 = null;
		Map<String, List<String>> actionsList3 = null;
		List<String> actionListDet = new ArrayList<String>();
		Map<String, List<ActionDataMining>> patronesGeneral = new LinkedHashMap<String, List<ActionDataMining>>();
						
		String key = null;
		String key2 = null;
		String url = null;
		int t = 1;
		int cant = 0;
		int min = 0;
		int max = 0;
		int index = 0;
		Pattern p = null; 
        Matcher m = null; 
		
		if (!(cantRep==0)) cant = cantRep;
		else cant = MIN_OCURR;
		
		if (!(tamMin==0)) min = tamMin;
		else min = MIN_SIZE;
		
		if (!(tamMax==0)) max = tamMax;
		else max = MAX_SIZE;
			
		//se ordenan las acciones por url, usuario y fecha
		Map<String, Map<String, List<ActionDataMining>>> sortedActions = getSortedActions (actions, 60000);
		
		//Caso LCS, FALTA MEJORAR
		MobilePatternDetector mdp = new MobilePatternDetector();
		Map<String, String> result = new HashMap<String, String>();
		try {
			result = mdp.LCS(sortedActions);			
			for (Map.Entry<String, String> entry : result.entrySet()) {
				detections.add(new TaskMobileSmellAnalysisResult(renameEventOrig(entry.getValue()) + "-"+ entry.getKey(), null, null, "1"));
			}			
		} catch (Exception e1) {
			if (debug) System.out.print("error");			
		}		

		//se identifican todos los patrones existentes en cada grupo, que cumplan con el rango de tamano indicado
		for (int l=min; l<max+1;l++) {						
					
			for (Map.Entry<String, Map<String, List<ActionDataMining>>> entry : sortedActions.entrySet()) {
				key2 = entry.getKey();
				index = key2.indexOf("=");
				entryURL = key2.substring(0,index);				
				for (Map.Entry<String, List<ActionDataMining>> entry2 : entry.getValue().entrySet()) {
					entryEventos = entry2.getKey();
					for (int j=0; j<entryEventos.length()-l+1;j++) {
				        patronSec = entryEventos.substring(j, j+l);
						val = false;
						for (Map.Entry<String, List<ActionDataMining>> e : patronesGeneral.entrySet()) {
							patron = e.getKey().substring(0, e.getKey().indexOf("-"));
							if (patron.equals(patronSec)) { //si ya existe el patron en otro grupo no se toma en cuenta
								val = true;
							}
						}
						if (val == false) {
							patronesGeneral.put(patronSec + "-"+entryURL, null);
						}
					}
				}
			}
		}	
		
		//por cada patron encontrado se analiza
		for (Map.Entry<String, List<ActionDataMining>> patronFor : patronesGeneral.entrySet()) {
			actionsList2 = new LinkedHashMap<String, List<ActionDataMining>>();
			actionsList3 = new LinkedHashMap<String, List<String>>();
			key = patronFor.getKey();
			patron = key.substring(0, key.indexOf("-"));
			url = key.substring(key.indexOf("-")+1, key.length());
			t = 1;
			
			//se realiza un loop de todo nuevamente
			for (Map.Entry<String, Map<String, List<ActionDataMining>>> entry : sortedActions.entrySet()) {
				key2 = entry.getKey();
				index = key2.indexOf("=");
				entryURL = key2.substring(0,index);
				
					for (Map.Entry<String, List<ActionDataMining>> entry2 : entry.getValue().entrySet()) {
							actionListDet = new ArrayList<String>(); 
				        	entryEventos = entry2.getKey();
							actionsList = entry2.getValue();
							val = false;
									
							//si el url es equal
							if (entryURL.equals(url)) {		        			
					        	p = Pattern.compile(patron); 
					        	m = p.matcher(entryEventos); 
					        			
								while(m.find()) { 
									val = true;
									actionListDet.add(String.valueOf(actionsList.get(m.start()).getId()));
								}   	
							}
							if (val) {										
								//se guardan el listado de eventos de esta sesion y el listado de eventos correspondientes al padron validado 
								actionsList2.put(String.valueOf(t), actionsList);
								actionsList3.put(String.valueOf(t), actionListDet);									
							}
							t += 1;
					}
			}
			//si la ocurrencia es mayor al minimo de ocurrencia entonces se considera un patron
			if (actionsList2.size() >= cant){							
				tamano = patron.length();
				detections.add(new TaskMobileSmellAnalysisResult(renameEventOrig(patron)+ "-"+url+ "-"+ tamano, actionsList2, actionsList3, null));
			}
		}
        return detections;    		
	}
	
	/**
	 * Identifica patrones de usability smells en base a un listado de sesiones (task) y un usability smell especificado
	 * 
	 * @param	tasks						uma lista de ações agrupadas por sesiones
	 * @param	detailSmell					detalle del patron del smell
	 * @param	actionDataMiningRepository	repositorio para obtener datos de action
	 * @return					Un listado de sesiones que coinciden con el patron del usability smell especificado
	 */	
	 public List<TaskMobileSmellAnalysisResult> analizePatternbySession (List<TaskSmellAnalysis> tasks, List<SmellMobileDetMining> detailSmell, ActionDataMiningRepository actionDataMiningRepository) throws IOException{
    	
    	List<TaskMobileSmellAnalysisResult> detections = new ArrayList<TaskMobileSmellAnalysisResult>();
		
		List<SessionGraph> detectedSessions = null;
        Map<String, String> sessionInfo = null;
		Map<String, String> mapEvento = null;
		List<Map<String, String>> listEvento = null;
		
		Boolean ind = false; 
		String patternFindFinal = "";
		String listAccions = "";
		String infoBattery = "";
		String infoContext = "";
		String pattern = null;
		String evento = "";
		List<Long> listIdsSession = null;
        Long totalTime = null;
        List<PageViewActionDataMining> list = null;
        List<PageViewActionDataMining> listRemove = null;
		Pattern p = null; 
        Matcher m = null; 
                
		int sessionActionCount = 0;	
                
        listIds = new ArrayList<Long>();
        
		//obtiene el listado de patrones para el smell seleccionado
		List<MobilePatternsAux> auxList = getPattern (detailSmell);
		
		for (MobilePatternsAux aux : auxList) {
			pattern = aux.getPattern();
			
			for (TaskSmellAnalysis task : tasks) {
				detectedSessions = new ArrayList<SessionGraph>();
				sessionInfo = new HashMap<String, String>();
				mapEvento = new HashMap<String, String>();
			    listEvento = new ArrayList<Map<String, String>>();
			    listRemove = new ArrayList<PageViewActionDataMining>();
			    
				for (SessionResultDataMining session : task.getSessions()) {
					ind = false;
					patternFindFinal = "";
					listAccions = "";
					infoBattery = "";
					infoContext = "";
					evento = "";
					listIdsSession = new ArrayList<Long>();						
					list = session.getActions();
					
					//total de tiempo entre la primera action y la última (de toda la sesion)
					totalTime = session.getTime();
									
					//se define si el usuario es nuevo o con experiencia (busca en historico del mismo usuario)
					/*allUrls = CollectionUtils.collect(list, TransformerUtils.invokerTransformer("getsUrl"));
					uniqueUrls = new HashSet<String>(allUrls);
					userExpert = getExpert(session.getUsername(),actionDataMiningRepository, null, uniqueUrls);*/
			        				
					//x sesion se obtiene la informacion de bateria y del contexto de usuario 				
					for(PageViewActionDataMining action : list){	
						evento = action.getAction();
						/*quitar la accion de la bateria*/
						if (evento.indexOf("battery") == -1){	
							listAccions += renameEvent(evento, false, action.getInfoTag());		
							listIdsSession.add(action.getId());
						} else {
							infoBattery = evento;
							listRemove.add(action);
						}
						infoContext = action.getsMobileConf();
					} 
					//remueve los eventos de tipo batery 
					list.removeAll(listRemove);
									
					p = Pattern.compile(pattern); 
				    m = p.matcher(listAccions); 
				    
				    while(m.find()) 
				    { 
				        //si ingresa al while es porque la sesion tiene una o mas coincidencias con el patron identificado, se devuelve la sesion
					    //en un timeline y con la indicacion del MUS encontrado	
					    patternFindFinal += m.start() + "," + (m.end()-1+ ", "); 
					    mapEvento.put(session.getId(), patternFindFinal);
					    listEvento.add(mapEvento);
					    listIds.addAll(listIdsSession);
					    ind = true;
				    }   
				    if (ind == true ) {
				    	sessionActionCount = session.getActions().size();
				    	sessionInfo.put("datamining.smells.testes.actioncount", String.valueOf(sessionActionCount));
				    	sessionInfo.put("datamining.smells.testes.mobile.contexto", infoContext);
				    	//sessionInfo.put("datamining.smells.testes.mobile.usuario", String.valueOf(userExpert));
				    	sessionInfo.put("datamining.smells.testes.mobile.tempo", String.valueOf(totalTime/1000) +" segundos");
				    	if (infoBattery != "") sessionInfo.put("Battery", infoBattery);
				    	detectedSessions.add(new SessionGraph(session.getId(), session, sessionGraph(session), sessionInfo));				    	
				    }					          
				}				 
				if (detectedSessions.size() != 0){							
					detections.add(new TaskMobileSmellAnalysisResult(task.getName(), detectedSessions, null, listEvento, getEventOrig(detailSmell, aux.getIdSmellPattern().toString()) + " Patron buscado: " + pattern));
				}
			}				
		}
        return detections;     		
	}
    
    /**
	 * Identifica patrones de usability smells en base a un listado de acciones y un usability smell especificado
	 * 
	 * @param	tasks						uma lista de ações 
	 * @param	detailSmell					detalle del patron del smell
	 * @param	actionDataMiningRepository	repositorio para obtener datos de action
	 * @return					Un listado de sesiones que coinciden con el patron del usability smell especificado
	 */	
    public List<TaskMobileSmellAnalysisResult> analizePatternbyActions (List<ActionDataMining> actions, List<SmellMobileDetMining> detailSmell, String smellType, ActionDataMiningRepository actionDataMiningRepository) throws IOException, ParseException{
    	
    	List<TaskMobileSmellAnalysisResult> detections = new ArrayList<TaskMobileSmellAnalysisResult>();
		
		Boolean ind = false; 
		Pattern p = null; 
        Matcher m = null; 
        List<SessionGraph> detectedSessions = null;
        Map<String, String> sessionInfo = null;
		Map<String, String> mapEvento = null;
		List<Map<String, String>> listEvento = null;
		String patternFindFinal = "";
		int sessionActionCount = 0;
		String infoBattery = "";
		String infoContext = "";	
		int t = 0;
        String entryURL = null;
        String entryUsername = null;
		String entryEventos = null;
		List<ActionDataMining> actionsList = new ArrayList<ActionDataMining>();
		String pattern = null;
		int index = 0;
		String key = null;
		SessionResultDataMining session = null;
		List<PageViewActionDataMining> actionsPage = null;
		
		int var = Integer.parseInt(smellType);		
		
		//obtiene el listado de patrones para el smell seleccionado
		List<MobilePatternsAux> auxList = getPattern (detailSmell);
			
		//obtiene las acciones ordenadas
		Map<String, Map<String, List<ActionDataMining>>> sortedActions = getSortedActions(actions, var);
		
		for (MobilePatternsAux aux : auxList) {
			pattern = aux.getPattern();	
		
			//se realiza un loop de las acciones ordenadas
			for (Map.Entry<String, Map<String, List<ActionDataMining>>> entry : sortedActions.entrySet()) {
				key = entry.getKey();
				index = key.indexOf("=");
				entryURL = key.substring(0,index);
				entryUsername = key.substring(index+1,key.indexOf("=",index+1));
				index = key.indexOf("=",index+1);
				
				for (Map.Entry<String, List<ActionDataMining>> entry2 : entry.getValue().entrySet()) {
					detectedSessions = new ArrayList<SessionGraph>();
					sessionInfo = new HashMap<String, String>();
					mapEvento = new HashMap<String, String>();
					listEvento = new ArrayList<Map<String, String>>();
					actionsPage = new ArrayList<PageViewActionDataMining>();
					ind = false;
					patternFindFinal = "";
					    
					entryEventos = entry2.getKey();
					actionsList = entry2.getValue();
							
					p = Pattern.compile(pattern); 
					m = p.matcher(entryEventos); 
											    
					while(m.find()) 
					{ 
						if (debug) System.out.println("Pattern found from " + m.start() + " to " + (m.end()-1)); 
						//Si tiene el marcador de tag se valida si el evento es el mismo para el mismo tag
						if ( validateTag(m, aux, null, actionsList)) {
							patternFindFinal += m.start() + "," + (m.end()-1+ ", "); 
							mapEvento.put(String.valueOf(t), patternFindFinal);
							listEvento.add(mapEvento);		
							ind = true;
						}		
					}
							
					if (ind == true ) {
						for (ActionDataMining entry5 : actionsList) {
							actionsPage.add(new PageViewActionDataMining(entry5));
							//buscamos informacion de la bateria y del contexto
							/*if (!(entry5.getsActionType().indexOf("battery") == -1)){	
								infoBattery = entry5.getsActionType();
							}*/
							infoContext = entry5.getsMobileConf();
						}
						session = new SessionResultDataMining(String.valueOf(t),String.valueOf(t),entryUsername,null,null,actionsPage,null, null, null); 
								
						sessionActionCount = actionsList.size();
						sessionInfo.put("datamining.smells.testes.actioncount", String.valueOf(sessionActionCount));
						sessionInfo.put("datamining.smells.testes.mobile.contexto", infoContext);
						//sessionInfo.put("datamining.smells.testes.mobile.usuario", String.valueOf(userExpert));
						if (infoBattery != "") sessionInfo.put("Battery", infoBattery);
						detectedSessions.add(new SessionGraph(entryUsername + "-" +String.valueOf(t), session, sessionGraph(session), sessionInfo));				    	
					}					          
				}						 
				if (detectedSessions.size() != 0){							
					detections.add(new TaskMobileSmellAnalysisResult(entryURL, detectedSessions, null, listEvento,getEventOrig(detailSmell, aux.getIdSmellPattern().toString())));
				}
				t += 1;
			}					
		}					
			
        return detections;        		
	}
    
    /**
	 * Identifica si el usuario es experto: si ha ingresado un numero de veces determinada a la aplicación en los últimos 6 meses
	 * PENDIENTE DE MEJORA
	 * 
	 * @param	username					uma lista de ações 
	 * @param	actionDataMiningRepository	uma lista de ações 
	 * @param	sClient						detalle del patron del smell
	 * @param	uniqueUrls					urls a los cuales debe ingresar
	 * @return					indica si es experto o no
	 */	/*
	private boolean getExpert(String username, ActionDataMiningRepository actionDataMiningRepository, String sClient, Set<String> uniqueUrls) {
		
		boolean ind = false;
		int min_acceso = 6;
		
		List<FieldSearch> fieldsSearch = new ArrayList<FieldSearch>();
		fieldsSearch.add(new FieldSearch("sUsername", "sUsername", username, FieldSearchComparatorEnum.EQUALS));
		if (sClient != null)
			fieldsSearch.add(new FieldSearch("sClient", "sClient", sClient, FieldSearchComparatorEnum.EQUALS));
		
		//los ultimos 6 meses
		Date today = new Date();
		Calendar cal = Calendar.getInstance(); 
        cal.setTime(today); 
        cal.add(Calendar.MONTH, -6);
        Date initDate = cal.getTime();
        
		fieldsSearch.add(new FieldSearch("sTime", "sTimeInit", initDate.getTime(), FieldSearchComparatorEnum.GREATER_EQUALS_THAN));
		fieldsSearch.add(new FieldSearch("sTime", "sTimeEnd", today.getTime(), FieldSearchComparatorEnum.LESS_EQUALS_THAN));
		
		List<CountActionsAux> counts = actionDataMiningRepository.getCountActionsByRestrictions(new FieldSearch("sUrl", "sUrl", null, null), fieldsSearch, null);
		
		for (CountActionsAux count : counts) {
			if (uniqueUrls.contains(count.getDescription())) {
				if (count.getCount()>min_acceso) {
					ind = true;
				}
			}			
		}		
		return ind;
	}*/

    	
	/**
	 * Valida si el evento es para el mismo tag
	 * 
	 * @param	Matcher							coincidencia de patron 
	 * @param	MobilePatternsAux				información de patron 
	 * @param	List<PageViewActionDataMining>	listado de acciones (basados en tareas)
	 * @param	List<ActionDataMining>			listado de acciones 
	 * @return									true si el tag es el mismo
	 */	
	public boolean validateTag (Matcher m, MobilePatternsAux aux, List<PageViewActionDataMining> list, List<ActionDataMining> actionsList) {
		
		int start = 0;
        boolean ind = true;
        String tag = null;
        String tagEvent = null;
        String tagEventAnt = null;
        String classEvent = null;
        String classEventAnt = null;
        String pathEvent = null;
        String pathEventAnt = null;
        PageViewActionDataMining action = null;
        ActionDataMining action2 = null;
        
        String matching = null;
        String pattern =  null;
		
		start = m.start();
		int tamano = 4; //obtener este valor del padron
		        
        ArrayList<String> listado = aux.getListado();
        
        for (int i = 0; i < listado.size(); i++) {
        
        	pattern =  listado.get(i).substring(0, listado.get(i).indexOf(";"));
        	tag = listado.get(i).substring(listado.get(i).indexOf(";")+1,listado.get(i).length());
        
        	if ((tag!=null) && (tag.equals("1"))) {
	       		 
	       		matching = m.group();
	       		
	       		for (int l=0; l<matching.length();l++) {
	    	       			
	       			if (pattern.equals(matching.substring(l,l+1))) {
	       				
	       				if (list!=null) {
	    	       			action =(list).get(start+l); 
	    	       			tagEvent = action.getInfoTag();	
	    	       			classEvent = action.getsClass();	
	    	       			pathEvent = action.getsXPath();
	    	       		 } else {
	    	       			action2 = actionsList.get(start+l); 
	    	       			tagEvent = action2.getsTag();	
	    	       			classEvent = action2.getsClass();	
	    	       			pathEvent = action2.getsXPath();
	    	       		 }
	       				
	       				if (l>0) {	       					
	       					if (tagEvent.equals(tagEventAnt)) {
	       						if (classEvent!=null) {
	       							if (classEvent.equals(classEventAnt)) {
	       								if (pathEvent!=null) {
	       									if (pathEvent.equals(pathEventAnt)) {
	       										ind = true;	
	       										if (l>=tamano) {
	       											break;
	       										}
	    	       							} else {
	    	       								ind = false;
	    	    		       					break;
	    	       							}	       	
	       								}else {
	    			       					ind = true;	       							
	    	       						}
	       							}else {
	    		       					ind = false;
	    		       					break;
	    		       				}
	       						} else {
			       					ind = true;	       							
	       						}
		       				} else {
		       					ind = false;
		       					break;
		       				}
		       			}
	       			}
	       			tagEventAnt = tagEvent;
	       			classEventAnt = classEvent;
	       			pathEventAnt = pathEvent;
	       		}	       		
	       	 }			        	 
        }		
		return ind;		
	}
	
	/**
	 * Retorna el equivalente del tipo de accion enviado
	 * 
	 * @param	actiontype		tipo de accion 
	 * @param	ind				true para indicar que es convertir el patron definido, false para indicar que es convertir los eventos del logs
	 * @param	tag				para comparar que el focusout solo sea para elementos de tipo INPUT
	 * @return					string que retorna el equivalente al tipo de action
	 */	
	public String renameEvent (String actiontype, boolean ind, String tag) {
		
		String replaceEvent = "";		
		String result = ""; 
		
		if (actiontype.startsWith("(")){
			replaceEvent = actiontype;		
			
			for (int i=0;i<eventosTotal.length;i++){
				replaceEvent = replaceEvent.replace(eventosTotal[i], renameEvent2(eventosTotal[i],ind, tag));	
			}
			result = replaceEvent;	
		}else {			
			 result = renameEvent2(actiontype,ind, tag);
		}	
		return result;
	}
	
	/**
	 * Retorna el equivalente del tipo de accion enviado
	 * 
	 * @param	actiontype		tipo de accion 
	 * @param	ind				true para indicar que es convertir el patron definido, false para indicar que es convertir los eventos del logs
	 * @param	tag				para comparar que el focusout solo sea para elementos de tipo INPUT
	 * @return					string que retorna el equivalente al tipo de action
	 */	
	public String renameEvent2 (String actiontype, boolean ind, String tag) {
		
		String result = "";		
		
		if (actiontype.equals("singletap")){
			result = SINGLETAP;
		}else if (actiontype.equals("doubletap")){
			result = DOUBLETAP;				
		}else if (actiontype.equals("taphold")){
			result = PRESS;				
		}else if (actiontype.equals("swipeup")){
			result = SWIPEUP;				
		}else if (actiontype.equals("swipedown")){
			result = SWIPEDOWN;				
		}else if (actiontype.equals("swipeleft")){
			result = SWIPELEFT;				
		}else if (actiontype.equals("swiperight")){
			result = SWIPERIGTH;				
		}else if (actiontype.equals("orientationchange")){
			result = ORIENTATIONCHANGE;				
		}else if (actiontype.equals("click")){
			result = CLICK;				
		}else if (actiontype.equals("dblclick")){
			result = DOUBLECLICK;				
		}else if (actiontype.equals("mousemove")){
			result = MOUSEMOVE;				
		}else if (actiontype.equals("focusinput")){
			result = FOCUSOUT_INPUT;	
		}else if (actiontype.equals("focusout")){
			if  (ind == true) {
				result = "(" + FOCUSOUT + "|" + FOCUSOUT_INPUT + ")";	
			} else {
				if (tag.equals("INPUT")) result = FOCUSOUT_INPUT;	
				else result = FOCUSOUT;		
			}					
		}else if (actiontype.equals("form_submit")){
			result = FORM_SUBMIT;				
		}else if (actiontype.equals("onload")){
			result = ONLOAD;				
		}else if (actiontype.equals("reload")){
			result = RELOAD;				
		}else if (actiontype.equals("close")){
			result = CLOSE;				
		}else if (actiontype.equals("back")){
			result = BACK;				
		}else if (actiontype.equals("haschange")){
			result = HASHCHANGE;				
		}else if (actiontype.equals("mouseover")){
			result = MOUSEOVER;				
		}else if (actiontype.equals("swipe")){
			result = "(" + SWIPEUP + "|" + SWIPEDOWN + "|" + SWIPELEFT + "|" + SWIPERIGTH + ")";				
		}else if (actiontype.equals("pinch")){
			result = "(" + PINCHIN + "|" + PINCHOUT + ")";				
		}else if (actiontype.equals("all")){
			result = "(" + SWIPEUP + "|" + SWIPEDOWN + "|" + SWIPELEFT + "|" + SWIPERIGTH + "|" + SINGLETAP + "|" + DOUBLETAP + "|" + PRESS + "|" + FOCUSOUT + "|" + ORIENTATIONCHANGE + "|" +  PINCHIN + "|" + PINCHOUT + ")";				
		}else if (actiontype.equals("pinchin")){
			result = PINCHIN;				
		}else if (actiontype.equals("pinchout")){
			result = PINCHOUT;			
		}else {
			result = "z";
		}			
		return result;
	}

	/**
	 * Retorna el equivalente del tipo de accion enviado (valor original)
	 * 
	 * @param	actiontype		tipo de accion 
	 * @return					string que retorna el equivalente al tipo de action
	 */	
	public String renameEventOrig (String cadena) {
		
		String result = "";	
		String actiontype = ""; 
		String actiontypeAnt = ""; 
		String aux  = "";
		int m=0;
		int t=1;
		
		for(int i=0;i<cadena.length();i++){
			
			actiontype = cadena.substring(m,i+1);
			
			if (actiontypeAnt.equals(actiontype)) {
				t += 1;
				m++;
				continue;
			} else {
				if (t>1) {
					aux = result.substring(0,result.length()-1);
					result = aux + "{"+t+"}";
				}
				t = 1;
			}
			m++;
	        actiontypeAnt = actiontype;
			
			if (actiontype.equals(SINGLETAP)){
				result += "singletap";
			}else if (actiontype.equals(DOUBLETAP)){
				result += "doubletap";				
			}else if (actiontype.equals(PRESS)){
				result += "taphold";				
			}else if (actiontype.equals(SWIPEUP)){
				result += "swipeup";				
			}else if (actiontype.equals(SWIPEDOWN)){
				result += "swipedown";				
			}else if (actiontype.equals(SWIPELEFT)){
				result += "swipeleft";				
			}else if (actiontype.equals(SWIPERIGTH)){
				result += "swiperight";				
			}else if (actiontype.equals(ORIENTATIONCHANGE)){
				result += "orientationchange";				
			}else if (actiontype.equals(PINCHIN)){
				result += "pinchin";				
			}else if (actiontype.equals(PINCHOUT)){
				result += "pinchout";				
			}else if (actiontype.equals(CLICK)){
				result += "click";				
			}else if (actiontype.equals(DOUBLECLICK)){
				result += "dblclick";				
			}else if (actiontype.equals(MOUSEMOVE)){
				result += "mousemove";				
			}else if (actiontype.equals(FOCUSOUT)){
				result += "focusout";				
			}else if (actiontype.equals(FOCUSOUT_INPUT)){
				result += "focusinput";				
			}else if (actiontype.equals(FORM_SUBMIT)){
				result += "form_submit";				
			}else if (actiontype.equals(ONLOAD)){
				result += "onload";				
			}else if (actiontype.equals(RELOAD)){
				result += "reload";				
			}else if (actiontype.equals(CLOSE)){
				result += "close";				
			}else if (actiontype.equals(BACK)){
				result += "back";				
			}else if (actiontype.equals(HASHCHANGE)){
				result += "haschange";				
			}else if (actiontype.equals(MOUSEOVER)){
				result += "mouseover";				
			}else {
				result += "z";
			}
			result += " ";           
        }
		if (t>1) {
			aux = result.substring(0,result.length()-1);
			result = aux + "{"+t+"} ";
		}					
		return result;
	}
	
	/**
	 * Retorna el equivalente del tipo de accion enviado (para mostrar en las estadísticas)
	 * 
	 * @param	actiontype		tipo de accion 
	 * @return					string que retorna el equivalente al tipo de action
	 */	
	public static String renameEvent3 (String actiontype) {
		
		String result = "";		
		
		if (actiontype.equals("singletap")){
			result = "SingleTap";
		}else if (actiontype.equals("doubletap")){
			result = "DoubleTap";				
		}else if (actiontype.equals("taphold")){
			result = "TapHold";				
		}else if (actiontype.equals("swipeup")){
			result = "SwipeUp";				
		}else if (actiontype.equals("swipedown")){
			result = "SwipeDown";				
		}else if (actiontype.equals("swipeleft")){
			result = "SwipeLeft";				
		}else if (actiontype.equals("swiperight")){
			result = "SwipeRight";				
		}else if (actiontype.equals("orientationchange")){
			result = "OrientChange";				
		}else if (actiontype.equals("focusout")){
			result = "FocusOut";	
		}else if (actiontype.equals("form_submit")){
			result = "Submit";				
		}else if (actiontype.equals("onload")){
			result = "OnLoad";				
		}else if (actiontype.equals("pinchin")){
			result = "PinchOut";				
		}else if (actiontype.equals("pinchout")){
			result = "PinchIn";								
		}else {
			result = "";
		}					
		return result;
	}


	/**
	 * Retorna el equivalente del tipo de accion enviado (valor original)
	 * 
	 * @param	detailSmell		detalle del patron
	 * @param	idSmellPattern	identificador del patron
	 * @return					string que retorna el equivalente al tipo de action
	 */	
	public String getEventOrig (List<SmellMobileDetMining> detailSmell, String idSmellPattern) {
		
		String result = "";	
		
		for(SmellMobileDetMining smellDet : detailSmell){
			if (idSmellPattern.equals(smellDet.getIdSmellPattern().toString())) {
				result += smellDet.getsEvent();
				result += smellDet.getsQuantity();	
			}
		}				
		return result;
	}
	
	/**
	 * Retorna el patron en un formato para ser usado
	 * 
	 * @param	detailSmell		detalle del patron 
	 * @return					string que retorna en formato para ser usado por pattern y matcher
	 */	
	public List<MobilePatternsAux> getPattern (List<SmellMobileDetMining> detailSmell) {
		
		String pattern = "";				
		String resultTag = "";				
		Long idSmellPattern = null;	
		Long idSmellPatternAux = null;	
		ArrayList<String> listado = new ArrayList<String>();
		
		List<MobilePatternsAux> auxList = new  ArrayList<MobilePatternsAux>();
		MobilePatternsAux aux = new MobilePatternsAux();
		
		for(SmellMobileDetMining smellDet : detailSmell){
			idSmellPattern = smellDet.getIdSmellPattern();			
			idSmellPatternAux = aux.getIdSmellPattern();
			
			if((idSmellPatternAux!=null) && (idSmellPatternAux.equals(idSmellPattern))) {
					
				pattern = aux.getPattern();
				pattern += renameEvent(smellDet.getsEvent(),true,null);
				pattern += smellDet.getsQuantity();	
				aux.setPattern(pattern);
				
				resultTag = smellDet.getsIndTag();								
				listado = aux.getListado();
				listado.add(renameEvent(smellDet.getsEvent(),true,null)+";"+resultTag);
			}else {
				
				aux = new MobilePatternsAux();
				aux.setIdSmellPattern(idSmellPattern);
				
				pattern = "";	
				pattern += renameEvent(smellDet.getsEvent(),true,null);
				pattern += smellDet.getsQuantity();		
				aux.setPattern(pattern);
				
				resultTag = smellDet.getsIndTag();
				
				listado = new ArrayList<String>();
				listado.add(renameEvent(smellDet.getsEvent(),true,null)+";"+resultTag);
				
				aux.setListado(listado);
				auxList.add(aux);
			}			
		}
		return auxList;
	}
	
	/**
	 * Retorna el patron en un formato para ser usado (original)
	 * 
	 * @param	detailSmell		detalle del patron 
	 * @return					string que retorna el patron en su forma original
	 */	
	public List<String> getPatternOrig (List<SmellMobileDetMining> detailSmell) {
				
		List<String> listPattern = null;
		Map<String, String> mapPattern = new LinkedHashMap<String, String>();;
		
		String result = "";				
		Long idSmellPatter = null;	
		
		for(SmellMobileDetMining smellDet : detailSmell){
			idSmellPatter = smellDet.getIdSmellPattern();
			
			if(mapPattern.containsKey(idSmellPatter.toString())) {
				result = mapPattern.get(idSmellPatter.toString());
				result += smellDet.getsEvent();
				result += smellDet.getsQuantity();	
				mapPattern.put(idSmellPatter.toString(), result);
								
			}else {
				result = "";	
				result += smellDet.getsEvent();
				result += smellDet.getsQuantity();	
				mapPattern.put(idSmellPatter.toString(), result);
			}			
		}
		listPattern = new ArrayList<String>(mapPattern.values());
		
		return listPattern;
	}
	
	/**
	 * Retorna un map de acciones ordenados por fecha, usuario y url
	 * 
	 * @param	actions		listado de acciones (ordenadas x usuario, url y fecha
	 * @return				retorna un map con las acciones ordenadas
	 */	
	public Map<String, Map<String, List<ActionDataMining>>> getSortedActions (List<ActionDataMining> actions, int timeBetweenActions) throws ParseException {
		
		Map<String, List<ActionDataMining>> sortedActionsByEventos = new LinkedHashMap<String, List<ActionDataMining>>();
		Map<String, Map<String, List<ActionDataMining>>> sortedActionsByURL = new LinkedHashMap<String, Map<String, List<ActionDataMining>>>();
		
		ActionDataMining actionAnt = null;		
		String fecha = null;
		String fechaAnt = null;		
		Long dif = null;
		String listEvento = null; 
		String key = null;
		int count = 1;
		
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");		
		List<ActionDataMining> actionsByEventos = new ArrayList<ActionDataMining>();
		
		getActionsMasking (actions);
		
		for(ActionDataMining action : actions){	
				
			if ((listIds!=null) && (listIds.contains(action.getId()))) {				    
				continue;
			}else {
				if (!((action.getsActionType()).indexOf("battery") == -1)){	
					continue;
				}
				
				//X UN PROBLEMA EN EL EVENTO DE TOUCHSWIPE				
				if (action.getsActionType().equals("pinchout")){	
					action.setsActionType("pinchin");
				}else if (action.getsActionType().equals("pinchin")){	
					action.setsActionType("pinchout");
				}				
				
				if (!(action.getsMobile()==null) && (action.getsMobile()==1)) {
					//agrupar por dia, usuario, url, definir sesiones
					fecha = String.valueOf(formatter.parse(formatter.format(action.getCreatedAt())));
					if (actionAnt==null) {
						
						key = action.getsUrl()+"="+action.getsUsername()+"="+fecha+"="+count;
						
						actionsByEventos = new ArrayList<ActionDataMining>();
		            	actionsByEventos.add(action);
		            	
		            	sortedActionsByEventos = new LinkedHashMap<String, List<ActionDataMining>>();
		            	sortedActionsByEventos.put(renameEvent(action.getsActionType(), false, action.getsTag()),actionsByEventos) ; 
		            	
		            	sortedActionsByURL = new LinkedHashMap<String, Map<String, List<ActionDataMining>>>();
						sortedActionsByURL.put(key,sortedActionsByEventos);    
		            	
					}else {
						
						dif = action.getsTime() - actionAnt.getsTime();
						if ((action.getsUsername().equals(actionAnt.getsUsername())) && (action.getsUrl().equals(actionAnt.getsUrl())) && (fecha.equals(fechaAnt)&&(dif<timeBetweenActions))) {
														
							key = action.getsUrl()+"="+action.getsUsername()+"="+fecha+"="+count;
							
							if ((dif==0) && (validaDuplicado(action,actionAnt))) {
								continue;
							}					
							
							sortedActionsByEventos = sortedActionsByURL.get(key);
							listEvento = (String) sortedActionsByEventos.keySet().toArray()[0];
							actionsByEventos = sortedActionsByEventos.get(listEvento);
							listEvento += renameEvent(action.getsActionType(), false, action.getsTag());
							actionsByEventos.add(action);
			            	sortedActionsByEventos = new LinkedHashMap<String, List<ActionDataMining>>();
			            	sortedActionsByEventos.put(listEvento,actionsByEventos) ; 
			            	
			            	sortedActionsByURL.put(key,sortedActionsByEventos) ;
							
						}else {
							count = count + 1;
							key = action.getsUrl()+"="+action.getsUsername()+"="+fecha+"="+count;
							
							actionsByEventos = new ArrayList<ActionDataMining>();
			            	actionsByEventos.add(action);
			            	
			            	sortedActionsByEventos = new LinkedHashMap<String, List<ActionDataMining>>();
			            	sortedActionsByEventos.put(renameEvent(action.getsActionType(), false,action.getsTag()),actionsByEventos) ; 
			            	
			            	sortedActionsByURL.put(key,sortedActionsByEventos) ; 
						}
					}						
				}
			}
			actionAnt = action;
			fechaAnt = fecha;
		}
		return sortedActionsByURL;
	}
	
	/**
	 * Retorna true si la accion tiene duplicado
	 * 
	 * @param	action		accion a comparar
	 * @param	actionAnt	accion anterior
	 * @return				true si las acciones son duplicadas
	 */		
	public boolean validaDuplicado (ActionDataMining action, ActionDataMining actionAnt) throws ParseException {
		
		boolean result = false;
		String tag = null;
		String tagAnt = null;
		String clase = null;
		String classAnt = null;
		String path = null;
		String pathAnt = null;
		
		/*validamos que no haya duplicados*/
		if (action.getsXPath()==null) path = "";
		else path = action.getsXPath();
		
		if (actionAnt.getsXPath()==null) pathAnt = "";
		else pathAnt = actionAnt.getsXPath();
		
		if (action.getsTag()==null) tag = "";
		else tag = action.getsTag();
		
		if (actionAnt.getsTag()==null) tagAnt = "";
		else tagAnt = actionAnt.getsTag();
		
		if (action.getsClass()==null) clase = "";
		else clase = action.getsClass();
		
		if (actionAnt.getsClass()==null) classAnt = "";
		else classAnt = actionAnt.getsClass();
		
		if ((action.getsActionType().equals(actionAnt.getsActionType())) && (path.equals(pathAnt))
				&& (tag.equals(tagAnt))&& (clase.equals(classAnt))  ) {
			result = true;
		}		
		
		return result;
	}
	
	/**
	 * Retorna el listado de acciones con los nombres de los usuarios datos ocultos
	 * 
	 * @param	actions		listado de acciones
	 */	
	public void getActionsMasking (List<ActionDataMining> actions) throws ParseException {
	
		Map<String, String> users = new LinkedHashMap<String, String>();
		String userName = null;
		int index = 0;
		
		for(ActionDataMining action : actions){				
			userName = action.getsUsername();			
			if (!(users.containsKey(userName))) {
				users.put(userName,null);
			}						
		}
		
		for (Map.Entry<String, String> entry : users.entrySet()) {
			
			index += 1;
			userName = "Usuario"+index;			
			
			users.put(entry.getKey(), userName);
			/*aqui tambien podría ir el validar si el usuario es especialista o no*/
		}		
		
		for(ActionDataMining action : actions){							
			userName = action.getsUsername();			
			if (users.containsKey(userName)) {								
				action.setsUsername(users.get(userName));
			}									
		}
	}
	
	/**
	 * Retorna um grafo representando uma sessão. Nesse grafo, os vértices representam as ações 
	 * realizadas e as arestas, a transição entre essas ações, sequencialmente, conforme sua 
	 * data/hora de execução.
	 *
	 * @param	session	uma sessão de usuário
	 * @return			um grafo representando a sessão de entrada
	 */
	private DirectedPseudograph<String, DefaultWeightedEdge> sessionGraph(SessionResultDataMining session){
		String lastAction = null;
		List<PageViewActionDataMining> sessionActions = new ArrayList<PageViewActionDataMining>(session.getActions());
		Collections.sort(sessionActions, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Long t1 = ((PageViewActionDataMining) o1).getTime();
				Long t2 = ((PageViewActionDataMining) o2).getTime();
				return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
			}
		});
		DirectedPseudograph<String, DefaultWeightedEdge> sessionGraph =
				new DirectedPseudograph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		lastAction = sessionActions.get(0).getPageViewActionUnique();
		sessionGraph.addVertex(lastAction);
		for (PageViewActionDataMining action : sessionActions.subList(1, sessionActions.size())) {
			sessionGraph.addVertex(action.getPageViewActionUnique());
			//TODO testar e finalizar
			if (sessionGraph.containsEdge(lastAction, action.getPageViewActionUnique())) {
				sessionGraph.setEdgeWeight(
						sessionGraph.getEdge(lastAction, action.getPageViewActionUnique()),
						sessionGraph.getEdgeWeight(
								sessionGraph.getEdge(lastAction, action.getPageViewActionUnique()))+1);
			} else
				sessionGraph.addEdge(lastAction, action.getPageViewActionUnique());
			lastAction = action.getPageViewActionUnique();
		}
		return sessionGraph;
	}
}
