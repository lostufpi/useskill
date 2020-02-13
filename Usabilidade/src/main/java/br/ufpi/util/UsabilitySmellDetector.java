package br.ufpi.util;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
//MILL import java.nio.file.Files;
//MILL import java.nio.file.Path;
//MILL import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;
//MILL import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYCoordinate;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jgrapht.Graph;
import org.jgrapht.alg.CycleDetector;
//MILL import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import br.ufpi.datamining.models.ActionDataMining;
import br.ufpi.datamining.models.PageViewActionDataMining;
import br.ufpi.datamining.models.auxiliar.BarChart;
import br.ufpi.datamining.models.auxiliar.SessionGraph;
import br.ufpi.datamining.models.auxiliar.SessionResultDataMining;
import br.ufpi.datamining.models.auxiliar.StackedAreaChart;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysis;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysisGroupedResult;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysisResult;
import br.ufpi.datamining.models.auxiliar.XYSerie;
import br.ufpi.datamining.models.enums.SessionClassificationDataMiningEnum;
import br.ufpi.datamining.repositories.ActionDataMiningRepository;


public class UsabilitySmellDetector {
	
	private static final boolean debug = true;
	
	private static final Logger log = Logger.getLogger(UsabilitySmellDetector.class.getName());
	
	public static final int DEFAULT_VALUE = -1;
	
	public static final int NUMBER_DEFAULT = -1;
	public static final long TIME_DEFAULT = -1;
	public static final double RATE_DEFAULT = -1.0;
	
	public static final double FENCE_TYPE_INNER = 1.5;
	public static final double FENCE_TYPE_OUTER = 3.0;
	
	
	//*********************************************************************************************
	//************* MÉTODOS DE GERAÇÃO DE GRÁFICOS A PARTIR DAS MÉTRICAS DOS SMELLS ***************
	//*********************************************************************************************
	
	/**
	 * Gera o gráfico de quantidade de ações das sessões completas de cada tarefa. Todos os tipos 
	 * de ações são contabilizadas, inclusive aquelas não realizadas diretamente pelo usuário 
	 * (como o carregamento de páginas, por exemplo).
	 *
	 * @param	tasks		uma lista de tarefas a serem analisadas
	 * @param	useLiteral	define que o gráfico deve ser baseado na quantidade literal de sessões
	 * @return				o gráfico de quantidade de ações correspondente às tarefas analisadas
	 */
	public StackedAreaChart generateTaskActionCountChart (List<TaskSmellAnalysis> tasks, boolean useLiteral) throws IOException{
		List<XYSerie> series = new ArrayList<XYSerie>();
		for (TaskSmellAnalysis task : tasks) {
			List<Double> taskActionCountDataset = new ArrayList<Double>();
			for (SessionResultDataMining session : task.getSessions()) {
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
					taskActionCountDataset.add((double)session.getActions().size());
				}
			}
			if (useLiteral)
				series.add(datasetDistributionLiteralSerie(task.getName(), taskActionCountDataset));
			else
				series.add(datasetDistributionSerie(task.getName(), taskActionCountDataset));
		}
		String xLabel;
		if (useLiteral)
			xLabel = "datamining.smells.testes.statistics.sessioncount";
		else
			xLabel = "datamining.smells.testes.statistics.sessionproportion";
		return new StackedAreaChart("datamining.smells.testes.statistics.actioncountchart", xLabel, "datamining.smells.testes.actioncount", series);
	}
	
	/**
	 * Gera o gráfico de duração das sessões completas de cada tarefa. A duração é apresentada em 
	 * minutos no gráfico retornado como saída para facilitar a visualização.
	 *
	 * @param	tasks		uma lista de tarefas a serem analisadas
	 * @param	useLiteral	define que o gráfico deve ser baseado na quantidade literal de sessões
	 * @return				o gráfico de duração correspondente às tarefas analisadas
	 */
	public StackedAreaChart generateTaskTimeChart (List<TaskSmellAnalysis> tasks, boolean useLiteral) throws IOException{
		List<XYSerie> series = new ArrayList<XYSerie>();
		for (TaskSmellAnalysis task : tasks) {
			List<Double> taskTimeDataset = new ArrayList<Double>();
			for (SessionResultDataMining session : task.getSessions()) {
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
					taskTimeDataset.add((double)session.getTime());
				}
			}
			if (taskTimeDataset.size() > 0) {
				XYSerie taskTimeSerie = new XYSerie(task.getName());
				XYSerie datasetDistributionSerie;
				if (useLiteral)
					datasetDistributionSerie = datasetDistributionLiteralSerie(task.getName(), taskTimeDataset);
				else
					datasetDistributionSerie = datasetDistributionSerie(task.getName(), taskTimeDataset);
				for (XYCoordinate coordinate : datasetDistributionSerie.getCoordinates()) {
					taskTimeSerie.addCoordinate(new XYCoordinate(coordinate.getX(), TimeUnit.MILLISECONDS.toMinutes(Math.round(coordinate.getY()))));
				}
				series.add(taskTimeSerie);
			}
		}
		String xLabel;
		if (useLiteral)
			xLabel = "datamining.smells.testes.statistics.sessioncount";
		else
			xLabel = "datamining.smells.testes.statistics.sessionproportion";
		return new StackedAreaChart("datamining.smells.testes.statistics.timechart", xLabel, "datamining.smells.testes.time", series);
	}
	
	/**
	 * Gera o gráfico de taxa de ciclos das sessões completas de cada tarefa. A taxa de ciclos 
	 * representa a proporção de ações da sessão que estão contidas em atividades cíclicas, ou 
	 * seja, em sequências de ações que envolvem a repetição de ações anteriormente executadas.
	 *
	 * @param	tasks		uma lista de tarefas a serem analisadas
	 * @param	useLiteral	define que o gráfico deve ser baseado na quantidade literal de sessões
	 * @return				o gráfico de taxa de ciclos correspondente às tarefas analisadas
	 */
	public StackedAreaChart generateTaskCycleRateChart (List<TaskSmellAnalysis> tasks, boolean useLiteral) throws IOException {
		List<XYSerie> series = new ArrayList<XYSerie>();
		for (TaskSmellAnalysis task : tasks) {
			List<Double> taskCycleRateDataset = new ArrayList<Double>();
			for (SessionResultDataMining session : task.getSessions()) {			
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS))
					taskCycleRateDataset.add(sessionCycleRate(session)*100);
			}
			if (useLiteral)
				series.add(datasetDistributionLiteralSerie(task.getName(), taskCycleRateDataset));
			else
				series.add(datasetDistributionSerie(task.getName(), taskCycleRateDataset));
		}
		String xLabel;
		if (useLiteral)
			xLabel = "datamining.smells.testes.statistics.sessioncount";
		else
			xLabel = "datamining.smells.testes.statistics.sessionproportion";
		return new StackedAreaChart("datamining.smells.testes.statistics.cycleratechart", xLabel, "datamining.smells.testes.cyclerate", series);
	}
	
	/**
	 * Gera o gráfico de taxa de ocorrência das ações mais frequentes de cada url. Como a 
	 * quantidade de ações e urls possíveis em uma determinada aplicação pode ser muito grande, 
	 * esse gráfico deve ser limitado por um número máximo de resultados, representando as ações 
	 * com maior taxa de ocorrência ordenadas em ordem crescente. As ações do tipo "mouseover" e 
	 * "onload" são desconsideradas, pois nem sempre são realizadas intencionalmente pelo usuário. 
	 * Internamente também é estabelecido um número mínimo de ocorrências de ações para que uma 
	 * url seja analisada (evitando que urls contendo apenas ações esporádicas gerem taxas de 
	 * ocorrência muito elevadas), dado pela mediana do número de ações que ocorrem em cada url.
	 * 
	 * @param	actions			uma lista de ações
	 * @param 	maxResultCount	a quantidade máxima de ações no gráfico de saída
	 * @return					o gráfico de taxa de ocorrência referente às ações de entrada
	 */
	@SuppressWarnings("unchecked")
	public BarChart generateActionOccurrenceRateChart (List<ActionDataMining> actions, int maxResultCount) {
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		Collection<String> allUrls = CollectionUtils.collect(sessionFreeActions, TransformerUtils.invokerTransformer("getsUrl"));
		Set<String> uniqueUrls = new HashSet<String>(allUrls);
		List<Integer> urlsActionCount = new ArrayList<Integer>();
		for (String url : uniqueUrls) {
			urlsActionCount.add(filteredActionsIds(sessionFreeActions, url).size());
		}
		int minOccurrenceCount = (int) median(urlsActionCount.toArray(new Integer[0]));
		Map<String, Double> mostFrequentActions = new LinkedHashMap<String, Double>();
		for (int i = 0; i < maxResultCount; i++) {
			mostFrequentActions.put(String.valueOf(i), 0.0);
		}
		for (String url : uniqueUrls) {
			List<String> actionsIds = filteredActionsIds(sessionFreeActions, url);				
			if (actionsIds.size() < minOccurrenceCount)
				continue;
			Multiset<String> idsFrequencyMapping = HashMultiset.create();
			idsFrequencyMapping.addAll(actionsIds);
			for (String id : idsFrequencyMapping.elementSet()) {
				double rate = (double)idsFrequencyMapping.count(id)/actionsIds.size()*100;
				String minKey = minKey(mostFrequentActions);	
				if (rate > mostFrequentActions.get(minKey)){
					mostFrequentActions.remove(minKey);
					mostFrequentActions.put(id, rate);
				}
			}
		}
		Map<String, Map<String, String>> actionsAttributes = new HashMap<String, Map<String,String>>();
		for (Map.Entry<String, Double> entry : mostFrequentActions.entrySet()) {
			actionsAttributes.put(entry.getKey(), actionCommonAttributes(entry.getKey(), sessionFreeActions));
		} 
		return new BarChart("datamining.smells.testes.statistics.actionfrequencychart", "datamining.smells.testes.statistics.actions", "datamining.smells.testes.occurrencerate", sortedMap(mostFrequentActions), actionsAttributes);
	}
	
	/**
	 * Gera o gráfico de quantidade de camadas das sessões completas de cada tarefa. A quantidade de camadas é determinada 
	 * pelo número de urls diferentes na sessão, portanto, páginas diferentes com urls iguais são contabilizadas como sendo 
	 * uma única página.
	 *
	 * @param	tasks		uma lista de tarefas a serem analisadas
	 * @param	useLiteral	define que o gráfico deve ser baseado na quantidade literal de sessões
	 * @return				o gráfico de quantidade de camadas correspondente às tarefas analisadas
	 */
	public StackedAreaChart generateTaskLayerCountChart (List<TaskSmellAnalysis> tasks, boolean useLiteral) {
		List<XYSerie> series = new ArrayList<XYSerie>();
		for (TaskSmellAnalysis task : tasks) {
			List<Double> taskLayerCountDataset = new ArrayList<Double>();
			for (SessionResultDataMining session : task.getSessions()) {			
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
					Set<String> uniqueUrls = new HashSet<String>();
					for (PageViewActionDataMining action : session.getActions()) {
						uniqueUrls.add(action.getLocal());
					}
					taskLayerCountDataset.add((double)uniqueUrls.size());
				}
			}
			if (useLiteral)
				series.add(datasetDistributionLiteralSerie(task.getName(), taskLayerCountDataset));
			else
				series.add(datasetDistributionSerie(task.getName(), taskLayerCountDataset));
		}
		String xLabel;
		if (useLiteral)
			xLabel = "datamining.smells.testes.statistics.sessioncount";
		else
			xLabel = "datamining.smells.testes.statistics.sessionproportion";
		return new StackedAreaChart("datamining.smells.testes.statistics.layercountchart", xLabel, "datamining.smells.testes.layercount", series);
	}
	
	/**
	 * Gera o gráfico dos elementos que possuem mais tentativas de tooltip, onde esses elementos 
	 * são determinados através da busca por ações do tipo "mouseover". Como a quantidade de ações 
	 * possíveis em uma determinada aplicação pode ser muito grande, esse gráfico deve ser limitado
	 *  por um número máximo de resultados, representando os elementos com maior quantidade de 
	 * tentativas de tooltip ordenados em ordem crescente.
	 * 
	 * @param	actions			uma lista de ações
	 * @param	maxResultCount	a quantidade máxima de ações no gráfico de saída
	 * @return					o gráfico de tentativas de tooltip referente às ações de entrada
	 */
	public BarChart generateActionTooltipCountChart (List<ActionDataMining> actions, int maxResultCount) {
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		List<String> actionsIds = new ArrayList<String>();
		for (ActionDataMining action : sessionFreeActions) {
			if (action.getsActionType().equals("mouseover"))
				actionsIds.add(actionId(action));
		}
		Map<String, Double> mostRepeatedActions = new HashMap<String, Double>();
		for (int i = 0; i < maxResultCount; i++) {
			mostRepeatedActions.put(String.valueOf(i), 0.0);
		}
		Multiset<String> actionsFrequencyMapping = HashMultiset.create();
		actionsFrequencyMapping.addAll(actionsIds);
		for (String id : actionsFrequencyMapping.elementSet()) {
			int actionCount = actionsFrequencyMapping.count(id);
			String minKey = minKey(mostRepeatedActions);
			if (actionCount > mostRepeatedActions.get(minKey)) {
				mostRepeatedActions.remove(minKey);
				mostRepeatedActions.put(id, (double)actionCount);
			}
		}
		Map<String, Map<String, String>> actionsAttributes = new HashMap<String, Map<String,String>>();
		for (Map.Entry<String, Double> entry : mostRepeatedActions.entrySet()) {
			actionsAttributes.put(entry.getKey(), actionCommonAttributes(entry.getKey(), sessionFreeActions));
		}
		return new BarChart("datamining.smells.testes.statistics.actiontooltipchart", "datamining.smells.testes.statistics.actions", "datamining.smells.testes.attemptcount", sortedMap(mostRepeatedActions), actionsAttributes);
	}
	
	/**
	 * Gera o gráfico de quantidade de repetições das ações mais repetidas. Como a quantidade de 
	 * ações possíveis em uma determinada aplicação pode ser muito grande, esse gráfico deve ser 
	 * limitado por um número máximo de resultados, representando as ações com maior quantidade 
	 * média de repetições ordenadas em ordem crescente. As ações do tipo "mouseover" são 
	 * desconsideradas, pois nem sempre são realizadas intencionalmente pelo usuário. Internamente 
	 * também é estabelecido um número mínimo de ocorrências não-sequenciais para que uma ação seja
	 *  analisada (evitando que ações esporádicas gerem uma quantidade de repetições muito 
	 * elevadas), dado pela mediana do número de instâncias de cada ação.
	 * 
	 * @param	actions			uma lista de ações
	 * @param	maxResultCount	a quantidade máxima de ações no gráfico de saída
	 * @return					o gráfico de quantidade de repetições referente às ações de entrada
	 */
	public BarChart generateActionRepetitionCountChart (List<ActionDataMining> actions, int maxResultCount) {
		Map<String, List<ActionDataMining>> usersActions = new HashMap<String, List<ActionDataMining>>();
		for (ActionDataMining action : actions) {
			if (action.getsActionType().equals("mouseover"))
				continue;
			if (usersActions.get(action.getsUsername()) == null) {
				usersActions.put(action.getsUsername(), new ArrayList<ActionDataMining>());
				usersActions.get(action.getsUsername()).add(action);
			} else {
				usersActions.get(action.getsUsername()).add(action);
			}
		}
		for (List<ActionDataMining> userActions : usersActions.values()) {
			Collections.sort(userActions, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Long t1 = ((ActionDataMining) o1).getsTime();
					Long t2 = ((ActionDataMining) o2).getsTime();
					return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
				}
			});
		}
		Map<String, List<Integer>> actionsRepetitions = new HashMap<String, List<Integer>>();
		for (List<ActionDataMining> userActions : usersActions.values()) {
			int repetitionCount = 1;
			String currentAction = "";
			String previousAction = "";
			for (int i = 1; i < userActions.size(); i++) {
				currentAction = actionId(userActions.get(i));
				previousAction = actionId(userActions.get(i-1));
				if (currentAction.equals(previousAction)) {
					repetitionCount++;
				} else {
					if (actionsRepetitions.get(previousAction) == null) {
						actionsRepetitions.put(previousAction, new ArrayList<Integer>());
						actionsRepetitions.get(previousAction).add(repetitionCount);
					} else {
						actionsRepetitions.get(previousAction).add(repetitionCount);
					}
					repetitionCount = 1;
				}
			}
			if (actionsRepetitions.get(currentAction) == null) {
				actionsRepetitions.put(currentAction, new ArrayList<Integer>());
				actionsRepetitions.get(currentAction).add(repetitionCount);
			} else {
				actionsRepetitions.get(currentAction).add(repetitionCount);
			}
		}
		List<Integer> actionInstancesCount = new ArrayList<Integer>();
		for (List<Integer> actionInstances : actionsRepetitions.values())
			actionInstancesCount.add(actionInstances.size());
		int minOccurrenceCount = (int)median(actionInstancesCount.toArray(new Integer[actionInstancesCount.size()]));
		Map<String, Double> mostRepeatedActions = new HashMap<String, Double>();
		for (int i = 0; i < maxResultCount; i++) {
			mostRepeatedActions.put(String.valueOf(i), 0.0);
		}
		for (String key : actionsRepetitions.keySet()) {
			if (actionsRepetitions.get(key).size() >= minOccurrenceCount) {
				double median = median(actionsRepetitions.get(key).toArray(new Integer[actionsRepetitions.get(key).size()]));
				String minKey = minKey(mostRepeatedActions);
				if (median > mostRepeatedActions.get(minKey)) {
					mostRepeatedActions.remove(minKey);
					mostRepeatedActions.put(key, median);
				}
			}
		}
		Map<String, Map<String, String>> actionsAttributes = new HashMap<String, Map<String,String>>();
		for (Map.Entry<String, Double> entry : mostRepeatedActions.entrySet()) {
			actionsAttributes.put(entry.getKey(), actionCommonAttributes(entry.getKey(), actions));
		}
		return new BarChart("datamining.smells.testes.statistics.actionrepetitionchart", "datamining.smells.testes.statistics.actions", "datamining.smells.testes.repetitioncount", sortedMap(mostRepeatedActions), actionsAttributes);
	}
	
	/**
	 * Gera o gráfico de quantidade de elementos interagíveis diferentes de cada url. Como a
	 * quantidade de urls possíveis em uma determinada aplicação pode ser muito grande, esse
	 * gráfico deve ser limitado por um número máximo de resultados, representando as urls em que
	 * contem mais elementos distintos, ordenadas em ordem crescente. Os elementos sobre os quais
	 * ocorrem apenas ações do tipo "mouseover" e "onload" são desconsiderados, pois nem sempre
	 * essas ações são realizadas intencionalmente pelo usuário. 
	 * 
	 * @param	actions			uma lista de ações
	 * @param 	maxResultCount	a quantidade máxima de urls no gráfico de saída
	 * @return					o gráfico de quantidade de elementos diferentes referente à entrada
	 */
	@SuppressWarnings("unchecked")
	public BarChart generateUrlElementCountChart (List<ActionDataMining> actions, int maxResultCount) {
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		Collection<String> allUrls = CollectionUtils.collect(sessionFreeActions, TransformerUtils.invokerTransformer("getsUrl"));
		Set<String> uniqueUrls = new HashSet<String>(allUrls);
		Map<String, Double> highestElementCountUrls = new LinkedHashMap<String, Double>();
		for (int i = 0; i < maxResultCount; i++) {
			highestElementCountUrls.put(String.valueOf(i), 0.0);
		}
		for (String url : uniqueUrls) {
			int urlElementCount = new HashSet<String>(elementsIds(filteredActionsIds(sessionFreeActions, url))).size();
			String minKey = minKey(highestElementCountUrls);
			if (urlElementCount > highestElementCountUrls.get(minKey)) {
				highestElementCountUrls.remove(minKey);
				highestElementCountUrls.put(url + " |  | ", (double)urlElementCount);
			}
		}
		Map<String, Map<String, String>> elementsAttributes = new HashMap<String, Map<String,String>>();
		for (Map.Entry<String, Double> entry : highestElementCountUrls.entrySet()) {
			elementsAttributes.put(entry.getKey(), new LinkedHashMap<String, String>());
		}
		return new BarChart("datamining.smells.testes.statistics.urlelementcountchart", "datamining.smells.testes.statistics.urls", "datamining.smells.testes.diffelementcount", sortedMap(highestElementCountUrls), elementsAttributes);
	}
	
	//MILL
	/**
	 * Genera el gráfico de acciones
	 * 
	 * @param	actions	 		listado de acciones			
	 * @param	maxResultCount	cantidad maxima de resultados			
	 * @return	listado de eventos con su cantidad de acciones
	 */	
	public BarChart generateCountChart (List<ActionDataMining> actions, int maxResultCount) {
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		HashMap<String, Double> highestElementCount = new LinkedHashMap<String, Double>();
		HashMap<String, Double> highestElementCountFinal = new LinkedHashMap<String, Double>();
		String label = null;
		
		for (ActionDataMining action : sessionFreeActions) {			
			if (!(action.getsMobile()==null) && (action.getsMobile()==1)) {
				if (action.getsActionType().indexOf("battery") == -1) {
					label = MobileUsabilitySmellDetector.renameEvent3(action.getsActionType()); 
					if( highestElementCount.containsKey( label ) ) {
						highestElementCount.put(label, (double)highestElementCount.get(label)+1);
			        }
					else{ highestElementCount.put(label, (double)1);
					}	
				}
			}					
		}	
		
		ValueComparator bvc =  new ValueComparator(highestElementCount);
        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
        sorted_map.putAll(highestElementCount);
				
		Map<String, Map<String, String>> elementsAttributes = new HashMap<String, Map<String,String>>();
	 		if (highestElementCount.size()<maxResultCount) {
			for (Map.Entry<String, Double> entry : highestElementCount.entrySet()) {
				elementsAttributes.put(entry.getKey(), new LinkedHashMap<String, String>());
				highestElementCountFinal.put(entry.getKey(), entry.getValue());
			}
		}
		else {
			for(int i = 1; i <= maxResultCount; i ++){
				Entry<String, Double> entry = sorted_map.pollFirstEntry();
	            highestElementCountFinal.put(entry.getKey(), entry.getValue());
				elementsAttributes.put(entry.getKey(), new LinkedHashMap<String, String>());			
	        }
		}
		return new BarChart("datamining.smells.testes.actioncountmobile", "datamining.smells.testes.mobileevents", "datamining.smells.testes.actioncount", sortedMap(highestElementCountFinal), elementsAttributes);
	}	
	
	/**
	 * Genera el gráfico de contexto
	 * 
	 * @param	actions	 		listado de acciones			
	 * @param	maxResultCount	cantidad maxima de resultados			
	 * @return	listado de contextos con su cantidad de acciones
	 */	
	public BarChart generateContextChart (List<ActionDataMining> actions, int maxResultCount) {
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		HashMap<String, Double> highestElementCount = new LinkedHashMap<String, Double>();
		HashMap<String, Double> highestElementCountFinal = new LinkedHashMap<String, Double>();
		String[] parts = null;
		String res = null;
		String browser = null;
		String os = null;
		/*String grade = null;
		String version = null;*/
		String tipo = null;
		String total = null;
		
		for (ActionDataMining action : sessionFreeActions) {			
			if (!(action.getsMobile()==null) && (action.getsMobile()==1)) {
				if (action.getsActionType().indexOf("battery") == -1) {	
					browser = action.getsUserAgent();
					parts = action.getsMobileConf().split(",");
					tipo = parts[0].substring(parts[0].indexOf(":") + 1);
					os = parts[1].substring(parts[1].indexOf(":") + 1);
					/*grade = parts[2];
					version = parts[3];*/
					res = parts[5].substring(15) +"x"+ parts[6].substring(16);
					if ((tipo==null) && (os.indexOf("Android")>=1)) {
						tipo = "android";
					}				
					//total = tipo +" | "+ os +" | "+ grade +" | "+ version +" | "+ browser +" | "+ res;
					total = tipo +" | "+ os +" | "+ browser +" | "+ res;
					
					if( highestElementCount.containsKey( total ) ) {
						highestElementCount.put(total, (double)highestElementCount.get(total)+1);
			        }
					else{ highestElementCount.put(total, (double)1);
					}	
				}
			}					
		}
		
		ValueComparator bvc =  new ValueComparator(highestElementCount);
        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
        sorted_map.putAll(highestElementCount);
				
		Map<String, Map<String, String>> elementsAttributes = new HashMap<String, Map<String,String>>();
		if (highestElementCount.size()<maxResultCount) {
			for (Map.Entry<String, Double> entry : highestElementCount.entrySet()) {
				elementsAttributes.put(entry.getKey(), new LinkedHashMap<String, String>());
				highestElementCountFinal.put(entry.getKey(), entry.getValue());
			}
		}
		else {
			for(int i = 1; i <= maxResultCount; i ++){
				Entry<String, Double> entry = sorted_map.pollFirstEntry();
	            highestElementCountFinal.put(entry.getKey(), entry.getValue());
				elementsAttributes.put(entry.getKey(), new LinkedHashMap<String, String>());			
	        }
		}
		return new BarChart("datamining.smells.testes.mobilecontext", "datamining.smells.testes.mobileevents", "datamining.smells.testes.actioncount", sortedMap(highestElementCountFinal), elementsAttributes);
	}	
	
	/**
	 * Implementa la clase comparator
	 */
	class ValueComparator implements Comparator<String> {
		 
        Map<String, Double> base;
        public ValueComparator(HashMap<String, Double> map) {
            this.base = map;
        }
        /**
    	 * Compara dos string
    	 * 
    	 * @param	a	string 1			
    	 * @param	b	string 2			
    	 * @return	retorna 1 si son iguales
    	 */
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }	
	//FIN MILL
	
	//*********************************************************************************************
	//************************** MÉTODOS DE DETECÇÃO DE USABILITY SMELLS **************************
	//*********************************************************************************************
	
	/**
	 * Retorna a lista de grafos das sessões onde o smell foi detectado. A detecção é determinada 
	 * pelos atributos maxActionCount e maxTime, onde a sessão é considerada como contendo o smell 
	 * caso ultrapasse ambos os valores máximos estabelecidos. Apenas as ações explicitamente 
	 * executadas pelo usuário são contadas, ou seja, desconsideram-se as ações do tipo "mouseover"
	 *  e "onload". Além disso, apenas as sessões completas são analisadas.
	 * 
	 * @param	tasks			uma lista de tarefas
	 * @param	maxActionCount	o número máximo de ações que uma sessão sem o smell pode conter
	 * @param	maxTime			o tempo máximo (em milis) que uma sessão sem o smell pode conter
	 * @param	minSessionCount	o número mínmo de sessões que uma tarefa deve conter para a análise
	 * @return					a lista de grafos das sessões detectadas com o smell
	 * @throws IOException
	 */
	public List<TaskSmellAnalysisResult> detectLaboriousTask (List<TaskSmellAnalysis> tasks, int maxActionCount, long maxTime, int minSessionCount) throws IOException{
		List<TaskSmellAnalysisResult> detections = new ArrayList<TaskSmellAnalysisResult>();
		if (maxActionCount == DEFAULT_VALUE) {
			if (debug) System.out.println("Calculating action count threshold...");
			List<Long> sessionsActionsCounts = new ArrayList<Long>();
			for (TaskSmellAnalysis task : tasks) {
				for (SessionResultDataMining session : task.getSessions())
					sessionsActionsCounts.add((long) explicitActionCount(session));
			}
			maxActionCount = (int) upperOuterFence(sessionsActionsCounts);
			if (debug) System.out.println("Max action count has been set to " + maxActionCount);
//			maxActionCount = 50;
		}
		if (maxTime == DEFAULT_VALUE) {
			if (debug) System.out.println("Calculating time threshold...");
			List<Long> sessionsTimes = new ArrayList<Long>();
			for (TaskSmellAnalysis task : tasks) {
				for (SessionResultDataMining session : task.getSessions())
					sessionsTimes.add(session.getTime());
			}
			maxTime = upperOuterFence(sessionsTimes);
			if (debug) System.out.println("Max time has been set to " + TimeUnit.MILLISECONDS.toMinutes(maxTime) + " min");
//			maxTime = TimeUnit.MINUTES.toMillis(20);
		}
		if (minSessionCount == DEFAULT_VALUE)
			minSessionCount = 10;
		for (TaskSmellAnalysis task : tasks) {
			List<SessionGraph> detectedSessions = new ArrayList<SessionGraph>();
			if (task.getSessions().size() >= minSessionCount) {				
				for (SessionResultDataMining session : task.getSessions()) {
					int sessionActionCount = explicitActionCount(session);
					if (sessionActionCount > maxActionCount && session.getTime() > maxTime
							&& session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
						Map<String, String> sessionInfo = new LinkedHashMap<String, String>();
						sessionInfo.put("datamining.smells.testes.actioncount", String.valueOf(sessionActionCount));
						sessionInfo.put("datamining.smells.testes.time", TimeUnit.MILLISECONDS.toMinutes(session.getTime()) + " min");
						detectedSessions.add(new SessionGraph(session.getId(), session, sessionGraph(session), sessionInfo));
					}
				}
				if (detectedSessions.size() > 0) {
					Collections.sort(detectedSessions, new Comparator<Object>() {
						@Override
						public int compare(Object o1, Object o2) {
							Long t1 = ((SessionGraph) o1).getSession().getTime() + ((SessionGraph) o1).getSession().getActions().size();
							Long t2 = ((SessionGraph) o2).getSession().getTime() + ((SessionGraph) o2).getSession().getActions().size();
							return t2 < t1 ? -1 : (t2 > t1 ? +1 : 0);
						}
					});
					detections.add(new TaskSmellAnalysisResult(task.getName(), detectedSessions, (double)detectedSessions.size()/task.getSessions().size()));
				}
			}
		}
		return detections;
	}
	
	/**
	 * Retorna a lista de grafos das sessões onde o smell foi detectado. A detecção é determinada 
	 * pelo atributo maxCycleRate, onde a sessão é considerada como contendo o smell caso 
	 * ultrapasse o valor máximo estabelecido. A taxa de ciclos representa a proporção da sessão 
	 * que é constituída de atividades repetitivas (cíclicas). Apenas as sessões completas são 
	 * analisadas.
	 * 
	 * @param	tasks			uma lista de tarefas
	 * @param	maxCycleRate	a taxa de ciclos máxima que uma sessão sem o smell pode conter
	 * @param	minActionCount	o número mínimo de ações que uma sessão deve ter para a análise
	 * @return					a lista de grafos das sessões detectadas com o smell
	 * @throws IOException
	 */
	public List<TaskSmellAnalysisResult> detectCyclicTask (List<TaskSmellAnalysis> tasks, double maxCycleRate, int minActionCount) throws IOException{
		List<TaskSmellAnalysisResult> detections = new ArrayList<TaskSmellAnalysisResult>();
		if (maxCycleRate == DEFAULT_VALUE)
			maxCycleRate = 0.7;
		if (minActionCount == DEFAULT_VALUE)
			minActionCount = 10;
		for (TaskSmellAnalysis task : tasks) {
			List<SessionGraph> detectedSessions = new ArrayList<SessionGraph>();
			for (SessionResultDataMining session : task.getSessions()) {				
				if (session.getActions().size() > minActionCount
						&& session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)){
					DirectedPseudograph<String, DefaultWeightedEdge> sessionGraph = sessionGraph(session);
					CycleDetector<String, DefaultWeightedEdge> cycleFinder = new CycleDetector<String, DefaultWeightedEdge>(sessionGraph);
					Set<String> verticesContainedInCycles = cycleFinder.findCycles();
					int cyclicActionsCount = 0;
					for (String vertex : verticesContainedInCycles) {
						for (PageViewActionDataMining action : session.getActions()) {
							if (action.getPageViewActionUnique().equals(vertex))
								cyclicActionsCount++;
						}
					}
					double rate = (double) cyclicActionsCount/session.getActions().size();
					if (rate > maxCycleRate) {
						Map<String, String> sessionInfo = new LinkedHashMap<String, String>();
						sessionInfo.put("datamining.smells.testes.cyclerate", String.format("%.2f", rate*100));
						detectedSessions.add(new SessionGraph(session.getId(), session, sessionGraph, sessionInfo));
					}
				}
			}
			if (detectedSessions.size() > 0){
				Collections.sort(detectedSessions, new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						Double t1 = Double.valueOf(((SessionGraph) o1).getInfo().get("datamining.smells.testes.cyclerate").substring(0, 4).replace(",", "."));
						Double t2 = Double.valueOf(((SessionGraph) o2).getInfo().get("datamining.smells.testes.cyclerate").substring(0, 4).replace(",", "."));
						return t2 < t1 ? -1 : (t2 > t1 ? +1 : 0);
					}
				});
				detections.add(new TaskSmellAnalysisResult(task.getName(), detectedSessions, (double)detectedSessions.size()/task.getSessions().size()));
			}
		}
		return detections;
	}
	
	/**
	 * Retorna uma lista de informações sobre as ações onde o smell foi detectado. A detecção é 
	 * determinada pelo atributo maxOccurreceRate, onde a ação é considerada como contendo o smell 
	 * caso ultrapasse o valor estabelecido. A taxa de ocorrência determina a proporção de 
	 * ocorrência de uma determinada ação em uma determinada url em comparação às demais ações que 
	 * ocorrem nessa mesma url. As ações do tipo "mouseover" e "onload" são desconsideradas nessa 
	 * análise, pois não são diretamente executadas pelo usuário.
	 * 
	 * @param	actions				uma lista de ações
	 * @param	maxOccurrenceRate	a taxa de ocorrência máxima que uma ação sem o smell pode ter
	 * @param	minOccurrenceCount	o número mínimo de ações que uma url deve ter para a análise
	 * @return						a lista de ações onde o smell foi detectado
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> detectLonelyAction (List<ActionDataMining> actions, double maxOccurrenceRate, int minOccurrenceCount) throws IOException{
		Map<String, Map<String, String>> detections = new HashMap<String, Map<String,String>>();
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		Collection<String> allUrls = CollectionUtils.collect(sessionFreeActions, TransformerUtils.invokerTransformer("getsUrl"));
		Set<String> uniqueUrls = new HashSet<String>(allUrls);
		if (minOccurrenceCount == DEFAULT_VALUE) {
			List<Integer> urlsActionCount = new ArrayList<Integer>();
			for (String url : uniqueUrls) {
				urlsActionCount.add(filteredActionsIds(sessionFreeActions, url).size());
			}
			minOccurrenceCount = (int)median(urlsActionCount.toArray(new Integer[0]));
		}
		boolean recalcOcurrenceRate = false;
		if (maxOccurrenceRate == DEFAULT_VALUE)
			recalcOcurrenceRate = true;
		for (String url : uniqueUrls) {
			List<String> urlActionsIds = filteredActionsIds(sessionFreeActions, url);
			if (urlActionsIds.size() < minOccurrenceCount)
				continue;
			Multiset<String> urlActionsIdsFrequencyMapping = HashMultiset.create();
			urlActionsIdsFrequencyMapping.addAll(urlActionsIds);			
			if (recalcOcurrenceRate) {
				if (urlActionsIdsFrequencyMapping.elementSet().size() > 1) {
					List<Double> urlActionsFrequencyRates = new ArrayList<Double>();
					for (String id : urlActionsIdsFrequencyMapping.elementSet()) {
						double rate = (double)urlActionsIdsFrequencyMapping.count(id)/urlActionsIds.size();
						urlActionsFrequencyRates.add(rate);
					}
					Collections.sort(urlActionsFrequencyRates);
					if (urlActionsFrequencyRates.size()-2 < 0) {
						System.out.println(urlActionsIdsFrequencyMapping.size() + " " + urlActionsFrequencyRates);
						System.in.read();
					}
					maxOccurrenceRate = 5*urlActionsFrequencyRates.get(urlActionsFrequencyRates.size()-2);
				} else {
					maxOccurrenceRate = 0;
				}
			}			
			for (String actionId : urlActionsIdsFrequencyMapping.elementSet()) {
				int actionOccurrenceCount = urlActionsIdsFrequencyMapping.count(actionId);
				double actionOccurrenceRate = (double)actionOccurrenceCount/urlActionsIds.size();
				if (actionOccurrenceRate > maxOccurrenceRate){
					Map<String, String> actionInfo = new LinkedHashMap<String, String>();
					actionInfo.put("datamining.smells.testes.occurrencerate", String.format("%.2f", actionOccurrenceRate*100));
					actionInfo.put("datamining.smells.testes.occurrencecount", String.valueOf(actionOccurrenceCount));
					actionInfo.putAll(actionCommonAttributes(actionId, sessionFreeActions));					
					detections.put(actionId, actionInfo);
					break;
				}
			}
		}
//		List<Map.Entry<String, Map<String, String>>> list = new LinkedList<Map.Entry<String, Map<String, String>>>(detections.entrySet());
//        Collections.sort( list, new Comparator<Map.Entry<String, Map<String, String>>>() {
//            public int compare(Map.Entry<String, Map<String, String>> o1, Map.Entry<String, Map<String, String>> o2) {
//            	return (o2.getValue().get("datamining.smells.testes.occurrencerate")).compareTo( o1.getValue().get("datamining.smells.testes.occurrencerate") );
//            }
//        });
//
//        Map<String, Map<String, String>> sortedDetections = new LinkedHashMap<String, Map<String, String>>();
//        for (Map.Entry<String, Map<String, String>> entry : list) {
//            sortedDetections.put(entry.getKey(), entry.getValue());
//        }
        return detections;
	}
	
	/**
	 * Retorna a lista de grafos das sessões onde o smell foi detectado. A detecção é determinada 
	 * pelo atributo maxLayerCount, onde a sessão é considerada como contendo o smell caso 
	 * ultrapasse o valor estabelecido. A quantidade de camadas é determinada pelo número de urls 
	 * diferentes na sessão, portanto, páginas diferentes com urls iguais são contabilizadas como 
	 * sendo uma única página. Em adição, somente as sessões completas são analisadas.
	 * 
	 * @param	tasks			uma lista de tarefas
	 * @param	maxLayerCount	o número máximo de camadas que uma sessão sem o smell pode ter
	 * @return					a lista de grafos das sessões detectadas com o smell
	 * @throws IOException
	 */
	public List<TaskSmellAnalysisResult> detectTooManyLayers (List<TaskSmellAnalysis> tasks, int maxLayerCount) throws IOException{
		if (maxLayerCount == DEFAULT_VALUE) {
			maxLayerCount = 5;
		}
		List<TaskSmellAnalysisResult> detections = new ArrayList<TaskSmellAnalysisResult>();
		for (TaskSmellAnalysis task : tasks) {
			List<SessionGraph> detectedSessions = new ArrayList<SessionGraph>();
			for (SessionResultDataMining session : task.getSessions()) {
				if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)){
					Set<String> sessionUniqueUrls = new HashSet<String>();
					for (PageViewActionDataMining action : session.getActions()) {
						sessionUniqueUrls.add(action.getLocal());
					}
					if (sessionUniqueUrls.size() > maxLayerCount) {
						Map<String, String> sessionInfo = new HashMap<String, String>();
						sessionInfo.put("datamining.smells.testes.layercount", String.valueOf(sessionUniqueUrls.size()));
						detectedSessions.add(new SessionGraph(session.getId(), session, sessionGraph(session), sessionInfo));
					}
				}
			}
			if (detectedSessions.size() != 0){
				Collections.sort(detectedSessions, new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						Integer t1 = Integer.valueOf(((SessionGraph) o1).getInfo().get("datamining.smells.testes.layercount"));
						Integer t2 = Integer.valueOf(((SessionGraph) o2).getInfo().get("datamining.smells.testes.layercount"));;
						return t2 < t1 ? -1 : (t2 > t1 ? +1 : 0);
					}
				});
				detections.add(new TaskSmellAnalysisResult(task.getName(), detectedSessions, (double)detectedSessions.size()/task.getSessions().size()));
			}
		}
		return detections;
	}
	
	/**
	 * Retorna uma lista de informações sobre as ações onde o smell foi detectado. A detecção é 
	 * determinada pelos atributos maxAttemptCount e maxTimeInterval, onde a detecção é realizada 
	 * caso a ação ultrapasse o valor máximo dentro do intervalo de tempo estabelecido. O número de
	 *  tentativas diz respeito à quantidade de tentativas de "tooltip" em um determinado elemento.
	 *  O intervalo de tempo estabelece a diferença máxima de tempo entre a ocorrência de dois 
	 * eventos para que estes sejam considerados em uma mesma análise.
	 * 
	 * @param	actions			uma lista de ações
	 * @param	maxAttemptCount	o número máximo de tentativas qeu uma ação sem o smell pode ter
	 * @param	maxTimeInterval	o intervalo de tempo máximo (em dias) para a análise
	 * @return					a lista de ações onde o smell foi detectado
	 * @throws IOException
	 */
	public Map<String, Map<String, String>> detectUndescriptiveElement (List<ActionDataMining> actions, int maxAttemptCount, int maxTimeInterval) throws IOException {
		if (maxAttemptCount == DEFAULT_VALUE) {
			maxAttemptCount = 30;
		}
		if (maxTimeInterval == DEFAULT_VALUE) {
			maxTimeInterval = 5;
		}
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		Map<String, Map<String, String>> detections = new HashMap<String, Map<String,String>>();
		long maxStartDate = Long.MAX_VALUE, maxEndDate = Long.MIN_VALUE;
		for (ActionDataMining action : sessionFreeActions) {
			if (action.getsTime() < maxStartDate)
				maxStartDate = action.getsTime();
			if (action.getsTime() > maxEndDate)
				maxEndDate = action.getsTime();
		}
		long startDate = maxStartDate, endDate = maxStartDate + TimeUnit.DAYS.toMillis(maxTimeInterval-1);
		final long oneDay = TimeUnit.DAYS.toMillis(1);
		do{
			List<String> actionsIds = new ArrayList<String>();
			for (ActionDataMining action : sessionFreeActions) {
				if (action.getsActionType().equals("mouseover") && action.getsTime() >= startDate && action.getsTime() <= endDate)
					actionsIds.add(actionId(action));
			}
			Multiset<String> actionsFrequencyMapping = HashMultiset.create();
			actionsFrequencyMapping.addAll(actionsIds);
			for (String actionId : actionsFrequencyMapping.elementSet()) {
				int actionCount = actionsFrequencyMapping.count(actionId);
				if (actionCount > maxAttemptCount) {
					Map<String, String> actionInfo = new HashMap<String, String>();
					actionInfo.put("datamining.smells.testes.tooltipattempts", String.valueOf(actionCount));
					actionInfo.putAll(actionCommonAttributes(actionId, sessionFreeActions));
					detections.put(actionId, actionInfo);
				}
			}
			startDate += oneDay;
			endDate += oneDay;
		} while (endDate <= maxEndDate);
		return detections;
	}
	
	/**
	 * Retorna uma lista de informações sobre as ações onde o smell foi detectado. A detecção é 
	 * determinada pelo atributo maxRepetitionCount, onde a ação é considerada como contendo o 
	 * smell caso ultrapasse o valor máximo estabelecido. O número de repetições representa a 
	 * quantidade de vezes que uma ação é realizada em sequência, ou seja, repetidas vezes. As 
	 * ações do tipo "mouseover" não são analisadas nem contabilizadas.
	 * 
	 * @param	actions				uma lista de ações
	 * @param	maxRepetitionCount	o número máximo de repetições que uma ação sem o smell pode ter
	 * @param	minOccurrenceCount	mínimo de ocorrências não-sequenciais de uma ação para análise
	 * @return						a lista de ações onde o smell foi detectado
	 */
	public Map<String, Map<String, String>> detectMissingFeedback (List<ActionDataMining> actions, int maxRepetitionCount, int minOccurrenceCount) {
		if (maxRepetitionCount == DEFAULT_VALUE)
			maxRepetitionCount = 3;
		if (minOccurrenceCount == DEFAULT_VALUE)
			minOccurrenceCount = 5;
		Map<String, Map<String, String>> detections = new HashMap<String, Map<String,String>>();
		Map<String, List<ActionDataMining>> usersActions = new HashMap<String, List<ActionDataMining>>();
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		for (ActionDataMining action : sessionFreeActions) {
			if (action.getsActionType().equals("mouseover"))
				continue;
			if (usersActions.get(action.getsUsername()) == null) {
				usersActions.put(action.getsUsername(), new ArrayList<ActionDataMining>());
				usersActions.get(action.getsUsername()).add(action);
			} else {
				usersActions.get(action.getsUsername()).add(action);
			}
		}
		for (List<ActionDataMining> userActions : usersActions.values()) {
			Collections.sort(userActions, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Long t1 = ((ActionDataMining) o1).getsTime();
					Long t2 = ((ActionDataMining) o2).getsTime();
					return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
				}
			});
		}
		Map<String, List<Integer>> actionsRepetitions = new HashMap<String, List<Integer>>();
		for (List<ActionDataMining> userActions : usersActions.values()) {
			int repetitionCount = 1;
			String currentAction = "";
			String previousAction = "";
			for (int i = 1; i < userActions.size(); i++) {
				currentAction = actionId(userActions.get(i));
				previousAction = actionId(userActions.get(i-1));
				if (currentAction.equals(previousAction)) {
					repetitionCount++;
				} else {
					if (actionsRepetitions.get(previousAction) == null) {
						actionsRepetitions.put(previousAction, new ArrayList<Integer>());
						actionsRepetitions.get(previousAction).add(repetitionCount);
					} else {
						actionsRepetitions.get(previousAction).add(repetitionCount);
					}
					repetitionCount = 1;
				}
			}
			if (actionsRepetitions.get(currentAction) == null) {
				actionsRepetitions.put(currentAction, new ArrayList<Integer>());
				actionsRepetitions.get(currentAction).add(repetitionCount);
			} else {
				actionsRepetitions.get(currentAction).add(repetitionCount);
			}
		}
		for (String actionId : actionsRepetitions.keySet()) {
			if (actionsRepetitions.get(actionId).size() > minOccurrenceCount) {
				double actionRepetitionsMedian = median(actionsRepetitions.get(actionId).toArray(new Integer[actionsRepetitions.get(actionId).size()]));
				if (actionRepetitionsMedian > maxRepetitionCount) {
					Map<String, String> actionInfo = new LinkedHashMap<String, String>();
					actionInfo.put("datamining.smells.testes.repetitionmedian", String.valueOf(actionRepetitionsMedian));
					actionInfo.put("datamining.smells.testes.instances", String.valueOf(actionsRepetitions.get(actionId).size()));
					actionInfo.putAll(actionCommonAttributes(actionId, sessionFreeActions));
					detections.put(actionId, actionInfo);
				}				
			}
		}
		return detections;
	}
	
	/**
	 * Retorna uma lista de informações sobre as URLs onde o smell foi detectado. A detecção é
	 * determinada pelo atributo maxElementCount, onde a URL é considerada como contendo o smell 
	 * caso ultrapasse o valor estabelecido. A quantidade de elementos (diferentes) máxima
	 * determina quantos elementos distintos podem ter ações executadas sobre eles por um usuário
	 * em determinada URL. Os elementos sobre os quais ocorrem apenas ações do tipo "mouseover" e
	 * "onload" são desconsideradas nessa análise, pois esssas ações nem sempre são diretamente
	 * executadas pelo usuário.
	 * 
	 * @param	actions			uma lista de ações
	 * @param	maxElementCount	a quantidade de elementos máxima que uma url sem o smell pode ter
	 * @return					a lista de urls onde o smell foi detectado
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> detectFatInterface (List<ActionDataMining> actions, double maxElementCount) {
		Map<String, Map<String, String>> detections = new HashMap<String, Map<String,String>>();
		List<ActionDataMining> sessionFreeActions = sessionFreeActions(actions);
		Collection<String> allUrls = CollectionUtils.collect(sessionFreeActions, TransformerUtils.invokerTransformer("getsUrl"));
		Set<String> uniqueUrls = new HashSet<String>(allUrls);
		if (maxElementCount == DEFAULT_VALUE)
			maxElementCount = 30;
		for (String url : uniqueUrls) {
			Set<String> urlElementsIds = new HashSet<String>(elementsIds(filteredActionsIds(sessionFreeActions, url)));
			int urlElementCount = urlElementsIds.size();
			if (urlElementCount > maxElementCount) {
				Map<String, String> urlInfo = new LinkedHashMap<String, String>();
				urlInfo.put("datamining.smells.testes.diffelementcount", String.valueOf(urlElementCount));
				List<String> formattedUrlElementsIds = new ArrayList<String>();
				int elementPosition = 0;
				for (String element : urlElementsIds) {
					elementPosition++;
					formattedUrlElementsIds.add("\n" + elementPosition + " - " + element);
				}
				//MILL urlInfo.put("datamining.smells.testes.elements", String.join("", formattedUrlElementsIds));
				//MILL urlInfo.put("datamining.smells.testes.elements", formattedUrlElementsIds);
				detections.put(url+" |  | ", urlInfo);
			}
		}
        return detections;
	}
	
	//*********************************************************************************************
	//************************************ MÉTODOS AUXILIARES *************************************
	//*********************************************************************************************
	
	/**
	 * Retorna uma série de pontos (x,y) reprentando a distribuição de cada valor do conjunto de 
	 * dados de entrada em relação ao todo. Os dados são ordenados em ordem crescente para 
	 * facilitar a visualização. Independente da quantidade de dados de entrada, sempre são 
	 * retornados 100 pontos (x,y), permitindo que múltiplas séries sejam plotadas em um mesmo 
	 * gráfico. Se não houver valores no conjunto de entrada, o retorno é nulo.
	 *
	 * @param	serieKey	uma chave para identificar a série
	 * @param	dataset		um conjunto de valores
	 * @return				a série (x,y) representando a distribuição dos valores de entrada
	 */
	private XYSerie datasetDistributionSerie (String serieKey, List<Double> dataset) {
		if (dataset.size() == 0)
			return null;
		List<Double> formattedDataset = new ArrayList<Double>(dataset);
		XYSerie distribution = new XYSerie(serieKey);
		int proportionCount = 1;
		while (formattedDataset.size() < 100)
			formattedDataset.addAll(new ArrayList<Double>(dataset));
		Collections.sort(formattedDataset);
		if (formattedDataset.size() == 100) {
			for (int i = 0; i < formattedDataset.size(); i++) {
				distribution.addCoordinate(new XYCoordinate(proportionCount, formattedDataset.get(i)));
				proportionCount++;
			}
		} else {
			if (formattedDataset.size() < 100) {
				distribution.addCoordinate(new XYCoordinate(proportionCount, formattedDataset.get(0)/2));
				proportionCount++;
			}		
			for (int i = 1; i < formattedDataset.size(); i++) {
				double sampleProportion = ((double)(i+1)/formattedDataset.size())*100;
				if (sampleProportion > proportionCount) {
					distribution.addCoordinate(new XYCoordinate(proportionCount, (formattedDataset.get(i)+formattedDataset.get(i-1))/2));
					proportionCount++;
				} else if (sampleProportion == proportionCount) {
					distribution.addCoordinate(new XYCoordinate(proportionCount, formattedDataset.get(i)));
					proportionCount++;
				}
			}
		} 
		return distribution;
	}
	
	/**
	 * Retorna uma série de pontos (x,y) reprentando a distribuição de cada valor do conjunto de 
	 * dados de entrada em relação ao todo. Os dados são ordenados em ordem crescente para 
	 * facilitar a visualização. A quantidade de pontos retornados corresponde diretamente ao 
	 * tamanho do conjunto de entrada. Se não houver valores no conjunto de entrada, o retorno é 
	 * nulo.
	 *
	 * @param	serieKey	uma chave para identificar a série
	 * @param	dataset		um conjunto de valores
	 * @return				a série (x,y) representando a distribuição dos valores de entrada
	 */
	private XYSerie datasetDistributionLiteralSerie (String serieKey, List<Double> dataset) {
		if (dataset.size() == 0)
			return null;
		List<Double> formattedDataset = new ArrayList<Double>(dataset);
		Collections.sort(formattedDataset);
		XYSerie distribution = new XYSerie(serieKey);
		for (int i = 0; i < formattedDataset.size(); i++) {
			distribution.addCoordinate(new XYCoordinate(i+1, formattedDataset.get(i)));
		}
		return distribution;
	}
	
	/**
	 * @param	map	um mapa de elementos
	 * @return		a chave do elemento com menor valor no mapa de entrada
	 */
	private String minKey (Map<String, Double> map) {
		String minKey = "";
		double minValue = Double.MAX_VALUE;
		for (Entry<String, Double> entry : map.entrySet()) {
			if (entry.getValue() < minValue) {
				minValue = entry.getValue();
				minKey = entry.getKey();
			}
		}
		return minKey;
	}
	
	/**
	 * @param	session	uma sessão de usuário
	 * @return			a lista de ações da sessão de entrada ordenadas pela data/hora de execução
	 */
	private List<PageViewActionDataMining> orderedActions (SessionResultDataMining session) {
		List<PageViewActionDataMining> actions = new ArrayList<PageViewActionDataMining>(session.getActions());
		Collections.sort(actions, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Long t1 = ((PageViewActionDataMining) o1).getTime();
				Long t2 = ((PageViewActionDataMining) o2).getTime();
				return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
			}
		});
		return actions;
	}
	
	/**
	 * @param	map	um mapa de elementos
	 * @return		o mapa de entrada ordenado pelo valor
	 */
	private Map<String, Double> sortedMap (Map<String, Double> map) {
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(map.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b){
				return a.getValue().compareTo(b.getValue());
			}
		});
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	/**
	 * @param	action	uma ação
	 * @return			o identificador único da ação de entrada
	 */
	private String actionId (ActionDataMining action) {
		return action.getsUrl() + " | " + action.getsXPath() + " | " + action.getsActionType();
	}
	
	/**
	 * Retorna a taxa de ciclos de uma sessão. A taxa de ciclos é dada pela divisão entre a 
	 * quantidade de ações contidas em ciclos e o total de ações da sessão.
	 *
	 * @param	session	uma sessão de usuário
	 * @return				a taxa de ciclos da sessão de entrada
	 */
	private double sessionCycleRate (SessionResultDataMining session) {
		DirectedPseudograph<String, DefaultWeightedEdge> sessionGraph = sessionGraph(session);
		CycleDetector<String, DefaultWeightedEdge> cycleFinder = new CycleDetector<String, DefaultWeightedEdge>(sessionGraph);
		Set<String> verticesContainedInCycles = cycleFinder.findCycles();
		int actionsContainedInCyclesCount = 0;
		for (String vertex : verticesContainedInCycles) {
			for (PageViewActionDataMining action : session.getActions()) {
				if (action.getPageViewActionUnique().equals(vertex))
					actionsContainedInCyclesCount++;
			}
		}
		return (double) actionsContainedInCyclesCount/session.getActions().size();
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
	
	/**
	 * Retorna uma lista de ações com o atributo url alterado para remover elementos referentes à 
	 * sessão do usuário. Atualmente, esse método funciona apenas para a remoção de elementos do 
	 * tipo "jsessionid". Caso não haja elementos referentes à sessão na url, ela é retornada sem 
	 * alterações.
	 * 
	 * @param	actions	uma lista de ações
	 * @return			a lista de ações de entrada com o atributo url sem elementos de sessão
	 */
	private List<ActionDataMining> sessionFreeActions (List<ActionDataMining> actions) {
		List<ActionDataMining> sessionFreeActions = new ArrayList<ActionDataMining>(actions);
		for (ActionDataMining action : sessionFreeActions) {
			if (action.getsUrl().contains("jsessionid"))
				action.setsUrl(action.getsUrl().split(";jsessionid")[0]);
		}
		return sessionFreeActions;
	}
		
	/**
	 * Retorna uma lista de ids únicos (da forma "URL | XPath | Tipo de Ação") representando todas 
	 * as ações que têm o atributo url igual ao parâmetro de entrada, desconsiderando-se todas as 
	 * ações do tipo "mouseover" e "onload".
	 * 
	 * @param	actions	uma lista de todas as ações ocorridas em um determinado intervalo de tempo
	 * @param	url		a url onde ocorrem as ações que se deseja filtrar
	 * @return			a lista de ids únicos representando as ações que ocorrem na url de entrada
	 */
	private List<String> filteredActionsIds(List<ActionDataMining> actions, String url) {
		List<String> actionIds = new ArrayList<String>();
		for (ActionDataMining action : actions) {
			if (action.getsUrl().equals(url) && !action.getsActionType().equals("mouseover") && !action.getsActionType().equals("onload"))
				actionIds.add(url + " | " + action.getsXPath() + " | " + action.getsActionType());
		}
		return actionIds;
	}
	
	/**
	 * @param	values	um array de valores inteiros
	 * @return			a mediana do array de entrada
	 */
	private double median (Integer values[]) {
		Integer sortedValues[] = Arrays.copyOf(values, values.length);
		Arrays.sort(sortedValues);
		if (sortedValues.length == 1)
			return sortedValues[0];
		double median;
		if (sortedValues.length%2==0)
			median = (double)(sortedValues[sortedValues.length/2] + sortedValues[sortedValues.length/2-1])/2;
		else
			median = (double)sortedValues[sortedValues.length/2];		
		return median;
	}
	
	/**
	 * Retorna a quantidade de ações executadas explicitamente e intencionalmente pelo usuário em 
	 * uma determinada sessão. Basicamente, desconsideram-se as ações do tipo "mouseover" e 
	 * "onload" na contagem.
	 * 
	 * @param	session	uma sessão de usuário
	 * @return			a quantidade de ações explicitamente executadas na sessão de entrada
	 */
	private int explicitActionCount (SessionResultDataMining session) {
		int actionCount = 0;
		for (PageViewActionDataMining action : session.getActions()) {
			if (!action.getAction().equals("mouseover") && !action.getAction().equals("onload"))
				actionCount++;
		}
		return actionCount;
	}
	
	/**
	 * @param	actions		uma lista de ações
	 * @param	actionId	o id de uma ação presente na lista de entrada
	 * @return				a ação da lista de entrada com o id correspondente
	 */
	private ActionDataMining getActionById (String actionId, List<ActionDataMining> actions){
		for (ActionDataMining action : actions) {
			if (actionId.equals(actionId(action))) {
				return action;
			}
		}
		return null;
	}
	
	/**
	 * Retorna uma proporção (normalizada) representando a similaridade entre as duas sessões 
	 * passadas como parâmetro. O cálculo em questão leva em em consideração as diferenças entre as
	 * transições de ações (arestas do grafo) que ocorrem em cada sessão, bem como as diferenças
	 * entre a quantidade de vezes em que essas ações ocorrem (pesos das arestas).
	 * 
	 * @param	session1	um grafo de sessão
	 * @param	session2	um grafo de sessão
	 * @return				uma proporção representando a similaridade entre as sessões de entrada
	 */
	private double sessionsSimilarity (DirectedPseudograph<String, DefaultWeightedEdge> session1, DirectedPseudograph<String, DefaultWeightedEdge> session2) {
		int total = 0, dif = 0;
		Set<String> allEdges = new HashSet<String>();
		for (DefaultWeightedEdge edge : session1.edgeSet()) {
			allEdges.add(edge.toString());
		}
		for (DefaultWeightedEdge edge : session2.edgeSet()) {
			allEdges.add(edge.toString());
		}
		for (String edge : allEdges) {
			String vertices[] = edge.substring(1, edge.length()-1).split(" : ");
			if (session1.containsEdge(vertices[0], vertices[1])) {
				if (session2.containsEdge(vertices[0], vertices[1])) {
					if (session1.getEdgeWeight(session1.getEdge(vertices[0], vertices[1]))
							> session2.getEdgeWeight(session2.getEdge(vertices[0], vertices[1])))
						total += session1.getEdgeWeight(session1.getEdge(vertices[0], vertices[1]));
					else
						total += session2.getEdgeWeight(session2.getEdge(vertices[0], vertices[1]));
				} else
					total += session1.getEdgeWeight(session1.getEdge(vertices[0], vertices[1]));
			} else
				total += session2.getEdgeWeight(session2.getEdge(vertices[0], vertices[1]));
		}
		for (DefaultWeightedEdge edge : session1.edgeSet()) {
			String vertices[] = edge.toString().substring(1, edge.toString().length()-1).split(" : ");
			if (session2.containsEdge(vertices[0], vertices[1])) {
				dif += Math.abs(session1.getEdgeWeight(session1.getEdge(vertices[0], vertices[1]))
						- session2.getEdgeWeight(session2.getEdge(vertices[0], vertices[1])));
			} else {
				dif += session1.getEdgeWeight(session1.getEdge(vertices[0], vertices[1]));
			}
		}
		for (DefaultWeightedEdge edge : session2.edgeSet()) {
			String vertices[] = edge.toString().substring(1, edge.toString().length()-1).split(" : ");
			if (!session1.containsEdge(session1.getEdge(vertices[0], vertices[1]))) {
				dif += session2.getEdgeWeight(session2.getEdge(vertices[0], vertices[1]));
			}
		}
		return (double)(total-dif)/total;
	}
	
	/**
	 * Retorna a lista de resultados (referentes a uma análise de smells) de entrada com as sessões
	 * detectadas agrupadas por similaridade. Duas sessões são consideradas como similares caso
	 * a taxa de similaridade calculada entre elas seja igual ou superior ao valor definido como
	 * parâmetro. Por questões de desempenho (visto que as sessões têm de ser comparadas par a
	 * par), durante a formação dos grupos, para definir se determinada sessão será integrada a um
	 * grupo preexistente ou encabeçar a criação de um novo, a mesma é comparada com uma amostra 
	 * (sessão) aleatória pertencente a cada um dos grupos já formados. Assim, duas execuções 
	 * diferentes deste método realizadas com um mesmo conjunto de entrada podem gerar agrupamentos
	 * diferentes, contudo essa probabilidade é bem pequena.
	 * 
	 * @param	taskAnalysisResults	uma lista de resultados referentes a uma análise de smells
	 * @param	minSimilarityRate	a taxa (normalizada) que define se duas sessões são similares
	 * @return						a lista de entrada com as sessões agrupadas por similaridade
	 */
	public List<TaskSmellAnalysisGroupedResult> sessionsGroupedBySimilarity (List<TaskSmellAnalysisResult> taskAnalysisResults, double minSimilarityRate) {
		Random rand = new Random();
		List<TaskSmellAnalysisGroupedResult> groupedResult = new ArrayList<TaskSmellAnalysisGroupedResult>();
		for (TaskSmellAnalysisResult result : taskAnalysisResults) {
			List<List<SessionGraph>> groups = new ArrayList<List<SessionGraph>>();
			groups.add(new ArrayList<SessionGraph>());
			groups.get(0).add(result.getSessions().get(0));
			for (SessionGraph session : result.getSessions().subList(1, result.getSessions().size())) {
				boolean createNewGroup = true;
				for (int i = 0; i < groups.size(); i++) {
					SessionGraph groupSampleSession = groups.get(i).get(rand.nextInt(groups.get(i).size()));
					if (sessionsSimilarity(session.getGraph(), groupSampleSession.getGraph()) >= minSimilarityRate) {
						groups.get(i).add(session);
						createNewGroup = false;
						break;
					}
				}
				if (createNewGroup) {
					List<SessionGraph> newGroup = new ArrayList<SessionGraph>();
					newGroup.add(session);
					groups.add(newGroup);
				}
			}
			groupedResult.add(new TaskSmellAnalysisGroupedResult(result.getName(), groups, result.getDetectionRate()));
		}
		return groupedResult;
	}
	
	/**
	 * Retorna a lista de atributos comuns de uma ação passada como parâmetro. Considera-se como
	 * atributos comuns aqueles que são importantes independentemento do tipo de smell que está
	 * sendo analisado. Os atributos que se encaixam nessa categoria são JHM, JHM Step, JHM Action
	 * e conteúdo. Apenas os atributos que não possuem uma String vazia como valor são retornados.
	 * 
	 * @param	actionId	o id da ação
	 * @param	actions		uma lista de ações
	 * @return				os atributos da ação com o id, de acordo com a lista de entrada
	 */
	private Map<String, String> actionCommonAttributes (String actionId, List<ActionDataMining> actions) {
		ActionDataMining action = getActionById(actionId, actions);
		Map<String, String> actionAttributes = new LinkedHashMap<String, String>();
		if (action.getsContent() != null && !action.getsContent().equals("")) actionAttributes.put("datamining.smells.testes.content", action.getsContent());
		if (action.getsJhm() != null && !action.getsJhm().equals("")) actionAttributes.put("datamining.smells.testes.jhm", action.getsJhm());
		if (action.getsStepJhm() != null && !action.getsStepJhm().equals("")) actionAttributes.put("datamining.smells.testes.jhmstep", action.getsStepJhm());
		if (action.getsActionJhm() != null && !action.getsActionJhm().equals("")) actionAttributes.put("datamining.smells.testes.jhmaction", action.getsActionJhm());
		return actionAttributes;
	}
	
	/**
	 * Retorna uma cópia da lista de entrada apenas com a identificação do XPath de cada elemento. 
	 * 
	 * @param	actionsIds	uma lista de ids de ações
	 * @return				uma lista de ids de elementos, de acordo com a lista de entrada
	 */
	private List<String> elementsIds (List<String> actionsIds) {
		List<String> elementsIds = new ArrayList<String>();
		for (String actionId : actionsIds)
			elementsIds.add(actionId.split(Pattern.quote(" | "))[1]);
		return elementsIds;
	}
	
	//*********************************************************************************************
	//********************************** MÉTODOS PARA REFATORAR ***********************************
	//*********************************************************************************************
	
	
	public static void main(String[] args) {
		DirectedPseudograph<String, DefaultWeightedEdge> graphA = new DirectedPseudograph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
    	graphA.addVertex("A");
    	graphA.addVertex("B");
    	graphA.addVertex("C");
    	graphA.addEdge("A", "B");
    	graphA.addEdge("B", "C");
    	
		DirectedPseudograph<String, DefaultWeightedEdge> graphB = new DirectedPseudograph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		graphB.addVertex("A");
		graphB.addVertex("B");
		graphB.addVertex("C");
		graphB.addVertex("D");
		graphB.addVertex("E");
		graphB.addVertex("F");
		graphB.addEdge("A", "B");
		graphB.addEdge("B", "D");
		graphB.addEdge("B", "E");
		graphB.addEdge("C", "D");
		graphB.addEdge("D", "E");
		graphB.addEdge("E", "F");
		
		DirectedPseudograph<String, DefaultWeightedEdge> graphC = new DirectedPseudograph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		graphC.addVertex("A");
		graphC.addVertex("B");
		graphC.addVertex("C");
		graphC.addVertex("D");
		graphC.addVertex("E");
		graphC.addVertex("F");
		graphC.addEdge("A", "B");
		graphC.addEdge("B", "D");
		graphC.addEdge("B", "E");
		graphC.addEdge("C", "D");
		graphC.addEdge("D", "E");
		graphC.addEdge("E", "F");
		graphC.addEdge("F", "A");
		
		System.out.println(new UsabilitySmellDetector().sessionsSimilarity(graphA, graphC));
		
	}
	
	private void executeCommand(final String command) throws IOException {
        
        final ArrayList<String> commands = new ArrayList<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add(command);
        
        BufferedReader br = null;        
        
        try {                        
            final ProcessBuilder p = new ProcessBuilder(commands);
            final Process process = p.start();
            final InputStream is = process.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            
            String line;            
            while((line = br.readLine()) != null) {
                System.out.println("Retorno do comando = [" + line + "]");
            }
        } catch (IOException ioe) {
            log.severe("Erro ao executar comando shell" + ioe.getMessage());
            throw ioe;
        } finally {
            secureClose(br);
        }
    }
    
    private void secureClose(final Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (IOException ex) {
            log.severe("Erro = " + ex.getMessage());
        }
    }
	
	private void saveGraph(Graph<String, DefaultWeightedEdge> graph, String graphName, String folderName) throws IOException{
		List<String> vertices = new ArrayList<String>();
		String edges = graph.edgeSet().toString().replace(" ", "");
		vertices.add(edges.substring(1, edges.length()-1));
		/*MILL Path file = Paths.get("vertices.txt");
		Files.write(file, vertices, Charset.forName("UTF-8"));
		
        executeCommand("python graph_drawer.py vertices.txt");
        new File("grafo.png").renameTo(new File(folderName + "/" + graphName + ".png"));*/
	}
	
	public List<TaskSmellAnalysis> detectLaboriousTasksDeprecated(List<TaskSmellAnalysis> tasks, int maxActionCount, long maxTime) throws IOException{
		
		List<TaskSmellAnalysis> laboriousTasks = new ArrayList<TaskSmellAnalysis>();
		
		for (TaskSmellAnalysis task : tasks) {
			
			//Calcula o valor ideal para o atributo
			if (maxActionCount == NUMBER_DEFAULT) {
				//Constroi uma lista com todos os tamanhos de sessao
				List<Long> sessionSizes = new ArrayList<Long>();
				for (SessionResultDataMining session : task.getSessions()) {
					if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
						sessionSizes.add((long)session.getActions().size());
					}
				}
				//Calcula a barreira externa e define como valor otimo
				maxActionCount = (int)upperOuterFence(sessionSizes);
			}
			
			//Calcula o valor ideal para o atributo
			if (maxTime == TIME_DEFAULT) {
				//Constroi uma lista com todos os tempos de sessao
				List<Long> sessionTimes = new ArrayList<Long>();
				for (SessionResultDataMining session : task.getSessions()) {
					if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)) {
						sessionTimes.add(session.getTime());
					}
				}
				//Calcula a barreira externa e define como valor otimo
				maxTime = upperOuterFence(sessionTimes);
			}
			
			//Cria um diretorio para armazenar os resultados das analises
			String folderName = task.getName() + "_LS_" + maxActionCount + "actions_" + maxTime + "milis";
			File dir = new File(folderName);
			dir.mkdir();
			
			//Cria uma variavel para armazenar o log
			StringBuilder log = new StringBuilder();
			
			List<SessionResultDataMining> laboriousSessions = new ArrayList<SessionResultDataMining>();
			int laboriousSessionCount = 0;
			int actionId[] = {1};
			Map<String, String> idMapping = new LinkedHashMap<String, String>();
			
			//Percorre cada sessao
			for (SessionResultDataMining session : task.getSessions()) {
				//TODO perguntar pro matheus se a quantidade de acoes de uma sessao pode ser = 0
				//e discutir sobre o estabelecimento de um limiar minimo para considerar a analise das sessoes, como um modo de eliminar o lixo
				if (session.getActions().size() > 0) {
					//Transforma a sessao em um grafo
					DirectedPseudograph<String, DefaultWeightedEdge> g = createGraphFromSession(session, idMapping, actionId);
					
					//Filtra as sessoes problematicas
					if (session.getActions().size() > maxActionCount && session.getTime() > maxTime) {
						laboriousSessions.add(session);
						laboriousSessionCount++;
						log.append("\nDetectada sessão " + session.getId() + "\n");
						log.append("Número de ações: " + session.getActions().size() + "\n");
						Long millis = session.getTime();
						log.append("Duração da sessão:" + String.format("%d min, %d sec", 
								TimeUnit.MILLISECONDS.toMinutes(millis),
								TimeUnit.MILLISECONDS.toSeconds(millis) - 
								TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
								) + "\n");
						
						saveGraph(g, session.getId(), folderName);
						
					}
				}
			}
			
			log.append("\nTotal de sessões: " + task.getSessions().size() + "\n");
			log.append("Sessões com grande esforço: " + laboriousSessionCount + "\n");
			
			//Salva o log em arquivo
			saveToFile(log.toString(), folderName + "/log.txt");
			
			//Cria um arquivo contendo todas os ids usados nos grafos e as respectivas ações que eles representam
			StringBuilder idMappingLog = new StringBuilder();
			for (Map.Entry<String, String> entry : idMapping.entrySet()) {
				idMappingLog.append(entry.getValue() + ";" + entry.getKey() + "\n");
			}
			saveToFile(idMappingLog.toString(), folderName + "/idMapping.csv");
			laboriousTasks.add(new TaskSmellAnalysis(task.getName(), laboriousSessions));
			maxActionCount = NUMBER_DEFAULT;
			maxTime = TIME_DEFAULT;
		}
		
		return laboriousTasks;		
	}
		
	public List<SessionResultDataMining> detectCyclicSessionsDeprecated(List<SessionResultDataMining> sessions, double maxCycleRate) throws IOException{
		
		int actionId[] = {1};
		Map<String, String> idMapping = new LinkedHashMap<String, String>();

		//Cria uma lista com todos os tamnhos de sessao
		List<Long> sessionSizes = new ArrayList<Long>();
		for (SessionResultDataMining session : sessions) {			
			if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS))
				sessionSizes.add((long)session.getActions().size());
		}
		//Determina um numero minimo de acoes para
		long lowerActionFence = lowerInnerFence(sessionSizes);
		
		//Define a barreira interna como valor otimo
		if (maxCycleRate == RATE_DEFAULT)
			maxCycleRate = 0.9;
		
		//Cria um diretorio para armazenar os resultados das analises
		String folderName = "CS_" + maxCycleRate + "rate";
		File dir = new File(folderName);
		dir.mkdir();
		
		//Cria uma variavel para armazenar o log
		StringBuilder log = new StringBuilder();
		
		List<SessionResultDataMining> cyclicSessions = new ArrayList<SessionResultDataMining>();
		
		//Percorre cada sessao
		for (SessionResultDataMining session : sessions) {
			//Exclui as sessoes de renicio e muito pequenas dos resultados
			if (session.getActions().size() > lowerActionFence && session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS)
					/*&& !session.getClassification().equals(SessionClassificationDataMiningEnum.REPEAT)*/
					/*&& session.getActions().size() > 0*/){
				//Transforma a sessao em um grafo
				DirectedPseudograph<String, DefaultWeightedEdge> g = createGraphFromSession(session, idMapping, actionId);
				
				//Encontra o conjunto de vertices contidos em ao menos um ciclo
				CycleDetector<String, DefaultWeightedEdge> cycleFinder = new CycleDetector<String, DefaultWeightedEdge>(g);
				Set<String> verticesContainedInCycles = cycleFinder.findCycles();
				
				//Conta a quantidade de acoes da sessao que correspondem aos vertices contidos em ciclos
				int actionCount = 0;
				for (String vertex : verticesContainedInCycles) {
					for (PageViewActionDataMining action : session.getActions()) {
						if (idMapping.get(action.getPageViewActionUnique()).equals(vertex)) {
							actionCount++;
						}
					}
				}
				
				//Calcula a proporcao entre a quantidade de vezes em que as acoes contidas em ciclos sao realizadas
				//e a quantidade total de acoes da sessao e verifica se essa proporcao nao excede o limiar
				double rate = (double) actionCount/session.getActions().size();
				if (rate > maxCycleRate) {
					log.append("\nDetectada sessão " + session.getId() + "\n");
					log.append("Taxa de ações cíclicas: " + (rate*100) + "%\n");
					cyclicSessions.add(session);
					saveGraph(g, session.getId(), folderName);
				}
			}
			
		}
		
		log.append("\n" + ((double)cyclicSessions.size()/sessions.size()*100) + "% de sessões cíclicas detectadas: "
				+ cyclicSessions.size() + " em um total de " + sessions.size() + "\n");
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
				
		//Cria um arquivo contendo todas os ids usados nos grafos e as respectivas ações que eles representam
		StringBuilder idMappingLog = new StringBuilder();
		for (Map.Entry<String, String> entry : idMapping.entrySet()) {
			idMappingLog.append(entry.getValue() + ";" + entry.getKey() + "\n");
		}
		saveToFile(idMappingLog.toString(), folderName + "/idMapping.csv");
		
		return cyclicSessions;
	}
	
	public List<String> detectLonelyActionsDeprecated(List<ActionDataMining> actions, double maxOccurrenceRate, int minNumberOfOccurrences) throws IOException{
			
		//Armazena todo o conjunto de urls diferentes da aplicacao
		List<ActionDataMining> filteredActions = sessionFreeActions(actions);
		Collection<String> allUrls = CollectionUtils.collect(filteredActions, TransformerUtils.invokerTransformer("getsUrl"));
		Set<String> uniqueUrls = new HashSet<String>(allUrls);
		
		//Define o numero minimo de acoes como sendo a media do numero de acoes nas urls
		if (minNumberOfOccurrences == NUMBER_DEFAULT) {
			List<Long> sizes = new ArrayList<Long>();
			for (String url : uniqueUrls) {
				sizes.add((long)filteredActionsIds(filteredActions, url).size());
			}
			minNumberOfOccurrences = (int)longMedian(sizes);
		}

		//Cria uma variavel para armazenar o log
		StringBuilder log = new StringBuilder();
		
		//Cria um diretorio para armazenar os resultados das analises e define se a taxa de ocorrencia deve ser realculada
		String folderName = "LA_" + maxOccurrenceRate + "rate" + minNumberOfOccurrences + "occurrences";
		boolean recalcOcurrenceRate = false;
		if (maxOccurrenceRate == RATE_DEFAULT) {
			folderName = folderName.replace("-1.0", "VARIABLE");
			recalcOcurrenceRate = true;
		}
		File dir = new File(folderName);
		dir.mkdir();
		
		//Armazena as acoes detectadas
		List<String> lonelyActions = new ArrayList<String>();
		
		//Percorre o conjunto de urls
		for (String url : uniqueUrls) {
			
			//Constroi uma lista de ids unicos representando as acoes que ocorrem na url
			List<String> actionIds = filteredActionsIds(filteredActions, url);
			Map<String, String> actionsMapContent = getUrlActionsContent(filteredActions, url);
			
			//Verifica se a quantidade de acoes que ocorrem na url eh maior que o limiar e passa para a proxima iteracao (url) caso contrario
			if (actionIds.size() < minNumberOfOccurrences)
				continue;
			
			//Cria uma estrutura do tipo Multiset que armazena os ids diferentes e quantidade de ocorrencias de cada um
			Multiset<String> idsFrequencyMapping = HashMultiset.create();
			idsFrequencyMapping.addAll(actionIds);
			
			//Recalcula a taxa de ocorrencia para cada url
			if (recalcOcurrenceRate) {
				//Percorre o conjunto de ids unicos caculando a taxa de ocorrencia da acao que cada um  representa
				//e adcionando essas taxas em uma lista para calcular a barreira externa
//				List<Double> frequencyRates = new ArrayList<Double>();
//				for (String id : idsFrequencyMapping.elementSet()) {
//					double rate = (double)idsFrequencyMapping.count(id)/actionIds.size();
//					frequencyRates.add(rate);
//				}
//				maxOccurrenceRate = fences(frequencyRates, FENCE_TYPE_OUTER)[1];
				if (idsFrequencyMapping.size() > 1) {
					List<Double> frequencyRates = new ArrayList<Double>();
					for (String id : idsFrequencyMapping.elementSet()) {
						double rate = (double)idsFrequencyMapping.count(id)/actionIds.size();
						frequencyRates.add(rate);
					}
					Collections.sort(frequencyRates);
					maxOccurrenceRate = 5*frequencyRates.get(frequencyRates.size()-2);
				} else {
					maxOccurrenceRate = 0;
				}
			}
			
			String detectedActionId = "";
			double detectedActionRate = 0;
			int detectedActionCount = 0; 
			
			//Percorre o conjunto de ids unicos caculando a taxa de ocorrencia da acao que cada um  representa
			//e verificando se essa taxa é maior que o limiar passado como parametro
			for (String id : idsFrequencyMapping.elementSet()) {
				double rate = (double)idsFrequencyMapping.count(id)/actionIds.size();
				if (rate > maxOccurrenceRate){
					detectedActionId = id;
					detectedActionRate = rate;
					detectedActionCount++;
				}
				if (detectedActionCount > 1)
					break;
			}
			if (detectedActionCount == 1) {
				log.append("\nDetectada ação solitária: " + detectedActionId + " / content: " + actionsMapContent.get(detectedActionId) + "\n");
				log.append("Quantidade de ocorrências: " + idsFrequencyMapping.count(detectedActionId) + "\n");
				log.append("Taxa de ocorrência: " + (detectedActionRate*100) + "%\n");
				lonelyActions.add(detectedActionId);
			}
		}
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
		
		return lonelyActions;
		
	}
	
	public List<SessionResultDataMining> detectTooManyLayersDeprecated(List<SessionResultDataMining> sessions, int maxNumberOfLayers) throws IOException{
		
		if (maxNumberOfLayers == NUMBER_DEFAULT) {
			maxNumberOfLayers = 25;
		}
		
		//Cria um diretorio para armazenar os resultados das analises
		String folderName = "TML_" + maxNumberOfLayers + "layers";
		File dir = new File(folderName);
		dir.mkdir();
		
		//Cria uma variavel para armazenar o log
		StringBuilder log = new StringBuilder();
		
		//Cria uma lista apenas com as sessoes bem sucedidas
		List<SessionResultDataMining> successSessions = new ArrayList<SessionResultDataMining>();
		for (SessionResultDataMining session : sessions) {
			if (session.getClassification().equals(SessionClassificationDataMiningEnum.SUCCESS))
				successSessions.add(session);
		}
		
		List<Double> sessionsUrlCount = new ArrayList<Double>();
		List<SessionResultDataMining> detectedSessions = new ArrayList<SessionResultDataMining>();
		
		for (SessionResultDataMining session : successSessions) {
			Set<String> uniqueUrls = new HashSet<String>();
			for (PageViewActionDataMining action : session.getActions()) {
				uniqueUrls.add(action.getLocal());
			}
			sessionsUrlCount.add((double)uniqueUrls.size());
			if (uniqueUrls.size() > maxNumberOfLayers) {
				detectedSessions.add(session);
			}
		}
		
		if (median(sessionsUrlCount) > maxNumberOfLayers)
			log.append("\nSmell detectado: " + ((double)detectedSessions.size()/successSessions.size()*100) + "% das sessões afetadas\n");
		else
			log.append("\nSmell não detectado: " + ((double)detectedSessions.size()/successSessions.size()*100) + "% das sessões afetadas\n");
		log.append("Média de URLs da tarefa: " + (median(sessionsUrlCount)) + "\n");
		log.append("Lista de sessões detectadas:\n");
		for (SessionResultDataMining session : detectedSessions) {
			log.append("\n" + session.getId());
		}
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
		
		return detectedSessions;
	}
	
	public List<String> detectUndescriptiveElementDeprecated (List<ActionDataMining> actions, int maxNumberOfAttempts) throws IOException {
		
		//Cria um diretorio para armazenar os resultados das analises
		String folderName = "UE_" + maxNumberOfAttempts + "attempts";
		File dir = new File(folderName);
		dir.mkdir();
		
		//Cria uma variavel para armazenar o log
		StringBuilder log = new StringBuilder();
		
		List<String> ids = new ArrayList<String>();
		List<String> undescriptiveElements = new ArrayList<String>();
		
		for (ActionDataMining action : actions) {
			if (action.getsActionType().equals("mouseover")) {
				ids.add(action.getsXPath() + " " + action.getsUrl());
			}
		}
		
		Multiset<String> elementsFrequencyMapping = HashMultiset.create();
		elementsFrequencyMapping.addAll(ids);
		
		for (String id : elementsFrequencyMapping.elementSet()) {
			if (elementsFrequencyMapping.count(id) > maxNumberOfAttempts) {
				log.append("\nDetectado elemento pouco descritivo: " + id + "\n");
				log.append("Quantidade de tentativas de tooltip: " + elementsFrequencyMapping.count(id) + "\n");
				undescriptiveElements.add(id);
			}
		}
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
		
		return undescriptiveElements;
	}
	
	
	/**
	 * Detecta o smell Missing Feedback
	 *
	 * @param sessions					lista de sessões relativas a uma tarefa.
	 * @param maxNumberOfRepetitions	número máximo de repetições sequenciais tolerado para uma ação que não contenha o smell.
	 * @param minNumberOfOcurrences		número mínimo de sessões contendo uma determinada ação para que ela possa ser considerada na análise.
	 */
	public Map<String, Double> detectMissingFeedbackDeprecated (List<SessionResultDataMining> sessions, int maxNumberOfRepetitions, int minNumberOfOcurrences) throws FileNotFoundException {
		//Cria diretorio para armazenar os resultados
		String folderName = "MF_" + maxNumberOfRepetitions + "repetitions";
		File dir = new File(folderName);
		dir.mkdir();
		
		//Armazena o log
		StringBuilder log = new StringBuilder();
		
		//Conjunto das acoes que se repetem em sequencia ao menos uma vez
		Set<String> repeatedActions = new HashSet<String>();
		
		//Percorre cada sessao adicionando ao conjunto as acoes correspondentes
		for (SessionResultDataMining session : sessions) {
			List<PageViewActionDataMining> actions = orderedActions(session);
			String previousAction = null;
			for (PageViewActionDataMining action : actions) {
				if (action.getPageViewActionUnique().equals(previousAction)) {
					repeatedActions.add(previousAction);
				}
				previousAction = action.getPageViewActionUnique();
			}
		}
		
		//Mapeamento de cada acao do conjunto com a lista de numeros de ocorrencia da mesma em cada sessao
		Map<String, List<Integer>> actionMapping = new HashMap<String, List<Integer>>();
		
		//Preenche o mapeamento com o maior numero de ocorrencias seguidas de cada acao em cada sessao
		for (String repeatedAction : repeatedActions) {
			actionMapping.put(repeatedAction, new ArrayList<Integer>());
			for (SessionResultDataMining session : sessions) {
				List<PageViewActionDataMining> actions = orderedActions(session);
				int repetition = 0, maxRepetition = 0;
				for (PageViewActionDataMining action : actions) {
					if (action.getPageViewActionUnique().equals(repeatedAction))
						repetition++;
					else
						repetition = 0;
					
					if (repetition > maxRepetition)
						maxRepetition = repetition;
				}
				actionMapping.get(repeatedAction).add(maxRepetition);
			}
		}
		
		//Mapeamento das acoes detectadas com o smell e mediana do seu respectivo numero de ocorrencias na tarefa
		Map<String, Double> detectedActions = new HashMap<String, Double>();
		
		//Preenche o mapeamento de acoes detectadas, desconsiderando as sessoes com numero de ocorrencias igual a zero
		for (String key : actionMapping.keySet()) {
			actionMapping.get(key).removeAll(Arrays.asList(0));
			if (actionMapping.get(key).size() >= minNumberOfOcurrences) {
				double median = median(actionMapping.get(key).toArray(new Integer[actionMapping.get(key).size()]));
				if (median > maxNumberOfRepetitions) {
					detectedActions.put(key, median);
					log.append("\nAção detectada: " + key + "\nNúmero de repetições: " + median + "\n");
				}
			}
		}
		
		//Salva o log em arquivo
		saveToFile(log.toString(), folderName + "/log.txt");
		
		return detectedActions;
		
	}
	
	//TODO eu nao sei o que era pra ser isso mas vou transformar na deteccao de uma acao que ocorre muito pouco em comparacao com as outras da mesma pagina
	public PageViewActionDataMining detectInvisibleAction(SessionResultDataMining referenceSession, List<SessionResultDataMining> userSessions){
		
		//Extrai a lista de acoes da sessao de referencia e ordena pelo tempo
		List<PageViewActionDataMining> referenceSessionActions = referenceSession.getActions();
		Collections.sort(referenceSessionActions, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Long t1 = ((PageViewActionDataMining) o1).getTime();
				Long t2 = ((PageViewActionDataMining) o2).getTime();
				return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
			}
		});
		
		//Conta em quantas sessoes cada acao da sessao de referencia eh realizada incorretamente
		int incorrectActionCount[] = new int[referenceSessionActions.size()];
		
		//Percorre cada sessao de usuario para fazer a comparacao das acoes com as da sessao de referencia
		for (SessionResultDataMining userSession : userSessions) {
			//Extrai a lista de acoes de cada sessao de usuario e ordena pelo tempo
			List<PageViewActionDataMining> userSessionActions = userSession.getActions();
			Collections.sort(userSessionActions, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					Long t1 = ((PageViewActionDataMining) o1).getTime();
					Long t2 = ((PageViewActionDataMining) o2).getTime();
					return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
				}
			});
			
			//Compara a acao da sessao de referencia com a acao da sessao de usuario na mesma posicao
			//e incrementa o contador, na posicao correspondente, caso sejam diferentes
			for (int i = 0; i < incorrectActionCount.length; i++) {
				if (!userSessionActions.get(i).getPageViewActionUnique()
						.equals(referenceSessionActions.get(i).getPageViewActionUnique())) {
					incorrectActionCount[i] += 1;
				}
			}
			
		}
		
		//Verifica a contagem e retorna a primeira acao da sessao de referencia que eh realizada incorretamente
		//em mais de 50% das sessoes
		for (int i = 0; i < incorrectActionCount.length; i++) {
			if (incorrectActionCount[i] > (userSessions.size()/2)) {
				System.out.println("A ação " + referenceSession.getActions().get(i).getPageViewActionUnique() +
						" não foi realizada no momento correto em " + (incorrectActionCount[i]/userSessions.size()) +
						"% das sessões");
				return referenceSession.getActions().get(i);
			}
		}
		
		//Senao encontrar nenhuma acao que satisfacam as condicoes estabelecidas acima, retorna nulo
		System.out.println("Não foi encontrada nenhuma ação invisível, o retorno é nulo");
		return null;
	}
	
	private DirectedPseudograph<String, DefaultWeightedEdge> createGraphFromSession(SessionResultDataMining session, Map<String, String> idMapping, int actionId[]){
		
		String lastAction = null;
		
		//Pega a lista de acoes da sessao e ordena pelo tempo
		List<PageViewActionDataMining> actions = session.getActions();
		Collections.sort(actions, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Long t1 = ((PageViewActionDataMining) o1).getTime();
				Long t2 = ((PageViewActionDataMining) o2).getTime();
				return t1 < t2 ? -1 : (t1 > t2 ? +1 : 0);
			}
		});
		
		//Cria um grafo para representar a sessao e adiciona o primeiro no de acordo com o mapeamento de ids
		DirectedPseudograph<String, DefaultWeightedEdge> g =
				new DirectedPseudograph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		if(idMapping.containsKey(actions.get(0).getPageViewActionUnique())){
			lastAction = idMapping.get(actions.get(0).getPageViewActionUnique());
			g.addVertex(lastAction);
		}
		else{
			idMapping.put(actions.get(0).getPageViewActionUnique(), String.valueOf(actionId[0]));
			lastAction = idMapping.get(actions.get(0).getPageViewActionUnique());
			g.addVertex(lastAction);
			actionId[0] += 1;
		}
		
		//Percorre a lista de acoes, a partir da segunda, e vai adicionando ao grafo e ligando com as anteriores
		for (PageViewActionDataMining action : actions.subList(1, actions.size())) {
			
			if (idMapping.containsKey(action.getPageViewActionUnique()))
				g.addVertex(idMapping.get(action.getPageViewActionUnique()));
			else{
				idMapping.put(action.getPageViewActionUnique(), String.valueOf(actionId[0]));
				g.addVertex(idMapping.get(action.getPageViewActionUnique()));
				actionId[0] += 1;
			}
							
			g.addEdge(lastAction, idMapping.get(action.getPageViewActionUnique()));
			lastAction = idMapping.get(action.getPageViewActionUnique());
			
		}
		
		return g;
		
	}
	
	private double sessionCycleRate (SessionResultDataMining session, Map<String, String> idMapping, int[] actionId) {
		//Transforma a sessao em um grafo
		DirectedPseudograph<String, DefaultWeightedEdge> g = createGraphFromSession(session, idMapping, actionId);
		
//		SzwarcfiterLauerSimpleCycles<String, DefaultWeightedEdge> cycleFinder = new SzwarcfiterLauerSimpleCycles<String, DefaultWeightedEdge>(g);
//		//Cria um conjunto com todos os vertices que fazem parte de ciclos nessa sessao
//		Set<String> verticesContainedInCycles = new HashSet<String>();
//		for (List<String> cycleVertices : cycleFinder.findSimpleCycles()) {
//			for (String vertice : cycleVertices) {
//				verticesContainedInCycles.add(vertice);
//			}
//		}
		
		CycleDetector<String, DefaultWeightedEdge> cycleFinder = new CycleDetector<String, DefaultWeightedEdge>(g);
		Set<String> verticesContainedInCycles = cycleFinder.findCycles();
		
		int actionCount = 0;
		for (String vertex : verticesContainedInCycles) {
			for (PageViewActionDataMining action : session.getActions()) {
				if (idMapping.get(action.getPageViewActionUnique()).equals(vertex)) {
					actionCount++;
				}
			}
		}
		
		return (double) actionCount/session.getActions().size();
	}
	
	private double longMedian (List<Long> values) {
		double median;
		if (values.size()%2==0)
			median = (double)(values.get(values.size()/2) + values.get(values.size()/2-1))/2;
		else
			median = (double)values.get(values.size()/2);		
		return median;
	}
	
	private long upperOuterFence (List<Long> values){		
		List<Long> numbers = new ArrayList<Long>(values);	
		Collections.sort(numbers, new Comparator<Long>() {
			@Override
			public int compare(Long n1, Long n2) {
				return n1.compareTo(n2);
			}
		});
//		double mean = mean(sessionSizes);
		double q1 = longMedian(numbers.subList(0, numbers.size()/2-1));
		double q3;
		if (numbers.size()%2==0)
			q3 = longMedian(numbers.subList(numbers.size()/2, numbers.size()-1));
		else
			q3 = longMedian(numbers.subList(numbers.size()/2+1, numbers.size()-1));
		return (long)(q3 + (q3-q1)*3);
	}
	
	private long lowerInnerFence (List<Long> values){		
		List<Long> numbers = new ArrayList<Long>(values);	
		Collections.sort(numbers, new Comparator<Long>() {
			@Override
			public int compare(Long n1, Long n2) {
				return n1.compareTo(n2);
			}
		});
//		double mean = mean(sessionSizes);
		double q1 = longMedian(numbers.subList(0, numbers.size()/2-1));
		double q3;
		if (numbers.size()%2==0)
			q3 = longMedian(numbers.subList(numbers.size()/2, numbers.size()-1));
		else
			q3 = longMedian(numbers.subList(numbers.size()/2+1, numbers.size()-1));
		return (long)(q1 - (q3-q1)*1.5);
	}
	
	private double median (List<Double> values) {
		if (values.size()%2==0)
			return (values.get(values.size()/2) + values.get(values.size()/2-1))/2;
		else
			return values.get(values.size()/2);		
	}
	
	private double[] fences (List<Double> dataset, double fenceType){		
		double[] fences = new double[2];
		List<Double> orderedDataset = new ArrayList<Double>(dataset);	
		Collections.sort(orderedDataset, new Comparator<Double>() {
			@Override
			public int compare(Double n1, Double n2) {
				return n1.compareTo(n2);
			}
		});
		double q1 = median(orderedDataset.subList(0, orderedDataset.size()/2-1));
		double q3;
		if (orderedDataset.size()%2==0)
			q3 = median(orderedDataset.subList(orderedDataset.size()/2, orderedDataset.size()-1));
		else
			q3 = median(orderedDataset.subList(orderedDataset.size()/2+1, orderedDataset.size()-1));
		fences[0] = (q1 - (q3-q1)*fenceType);
		fences[1] = (q3 + (q3-q1)*fenceType);
		return fences;
	}
	
	private List<Double> standardScores (List<Double> values) {
		List<Double> scores = new ArrayList<Double>();
		
		double average = 0;
		for (Double value : values)
			average += value;
		average = average/values.size();
		
		double variance = 0;
		for (Double value : values)
			variance += Math.pow((value - average), 2);
		variance = variance/(values.size()-1);
		
		double standardDeviation = Math.sqrt(variance);
		
		for (Double value : values) {
			scores.add((value-average)/standardDeviation);
		}
		
		return scores;
	}
	
	private void saveToFile (String text, String name) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File(name));
		writer.write(text);
		writer.close();	
	}
	
	private Map<String, String> getUrlActionsContent(List<ActionDataMining> actions, String url) {
		Map<String, String> actionContent = new HashMap<String, String>();
		for (ActionDataMining action : actions) {
			if (action.getsUrl().equals(url) && !action.getsActionType().equals("mouseover") && !action.getsActionType().equals("onload")) {
				String actionId = url + " | " + action.getsXPath() + " | " + action.getsActionType();
				if (actionContent.get(actionId) == null) {
					actionContent.put(actionId, action.getsContent());
					System.out.println(actionId + " -> " + action.getsContent());
				}
			}
		}
		return actionContent;
	}
	
}
