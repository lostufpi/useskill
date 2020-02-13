package br.ufpi.datamining.analisys;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//MILL import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
	
import org.apache.bcel.generic.IFGE;
import org.jgrapht.alg.CycleDetector;
//MILL import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.SequentialPattern;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionPieceWiseLinear;
import br.ufpi.datamining.models.ActionDataMining;
import br.ufpi.datamining.models.ActionSingleDataMining;
import br.ufpi.datamining.models.FieldSearchTupleDataMining;
import br.ufpi.datamining.models.PageViewActionDataMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.TestDataMining;
import br.ufpi.datamining.models.auxiliar.CorrectnessTask;
import br.ufpi.datamining.models.auxiliar.CountActionsAux;
import br.ufpi.datamining.models.auxiliar.FieldSearch;
import br.ufpi.datamining.models.auxiliar.FieldSearchComparatorEnum;
import br.ufpi.datamining.models.auxiliar.OrderSearch;
import br.ufpi.datamining.models.auxiliar.ResultDataMining;
import br.ufpi.datamining.models.auxiliar.SessionResultDataMining;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysis;
import br.ufpi.datamining.models.auxiliar.UserResultDataMining;
import br.ufpi.datamining.models.enums.ActionTypeDataMiningEnum;
import br.ufpi.datamining.models.enums.SessionClassificationDataMiningEnum;
import br.ufpi.datamining.models.enums.SessionClassificationDataMiningFilterEnum;
import br.ufpi.datamining.models.vo.FrequentSequentialPatternResultVO;
import br.ufpi.datamining.repositories.ActionDataMiningRepository;
import br.ufpi.datamining.repositories.TaskDataMiningRepository;
import br.ufpi.datamining.repositories.TestDataMiningRepository;
import br.ufpi.datamining.utils.ConverterUtils;
import br.ufpi.datamining.utils.EntityDefaultManagerUtil;
import br.ufpi.datamining.utils.EntityManagerUtil;
import br.ufpi.datamining.utils.StatisticsUtils;
import br.ufpi.datamining.utils.UsabilityUtils;
import br.ufpi.models.Action;
import br.ufpi.models.Fluxo;
import br.ufpi.models.Tarefa;
import br.ufpi.util.ApplicationPath;
import br.ufpi.util.UsabilitySmellDetector;


/*
 * TODO:
 * 1 - retirar todos os \ dos xpath
 * 2 - PageViewActionDataMining amarrado com jhms, xpath, etc.
 * 3 - PageViewDataMining retirar o replace da url
 */
public class WebUsageMining {
	
	/*
	############
	
	1 - Coleta dos dados:
	2 - Pré-processamento:
	 - filtrar por dados úteis (quais ações serão analisadas?):
	 - identificar usuário (login);
	 - identificar sessão: ações iniciais / ações iniciais + threshold de X min;
	3 - Descoberta de padrões:
	 - classificar a utilização (boas / ruins):
	 		* pontos de obrigatórios (plugin que captura ações específicas);
	 			> todas ou porcentagem?
	 - classificar o usuário (experiente / novato):
	 		* qtd de fluxos;
	 		* especialista define grau do usuário;
	 - Sequential Pattern;
	 - Clusterization;
	4 - Análise dos dados:
 	 - Fuzzy;
 	 - Graphs;
	 
	############
	 
	- Selecao dos dados que fazem parte do que sera analisado;
	- Cleaning: remover dados inuteis;
	OK - Pageview Identification: Dados referentes ao JHM/URL;
	OK - PageViewAction - identificar cada acao realizada;
	OK - User Identification: Sessao do usuario logado;
	- Sessionization: tempo de sessao / tempo por pagina / referencia de onde o usuario veio (nesse caso, qual o jhm q o usuario está);
		- Neste caso seria analise de ponto inicial e ponto final (threshold de tempo maximo entre acoes e analisar se realizou outra acao);
	- Path Completition: Como saber se houve falha na captura dos dados?;
	- Data Integration: Receber mais informações para integrar aos dados capturados;
	
	** Dados importantes:
	* Diferentemente de capturas de logs no servidor, a identificacao das pageviews, do usuario e das sessoes sao feitas antes no cleaning;
	* Identificar pageView -> padronizo as url e crio o um set;
	* Identificar pageAction -> XPath;
	*/
	
	//----- NOVA ABORDAGEM -----
	//-----
	//ok- usuário: 	conjunto de sessões;
	//ok- sessão: 	classificação, quantidade de ações, tempo e todas as ações que foram realizadas;
	//-----
	//ok- média de ações por usuário;
	//ok- média de tempo por usuário;
	//ok- taxa de ok, repet e erro por usuário;
	//- evolução do usuário;
	//- média de ações por classificação;
	//- média de tempo por classificação;
	//- ações mais realizadas por classificação;
	//-----
	//- classificar o usuário como experiente/novato; (especialista ou (med.acoes, med.tempo, taxa de ok))
	//-----
	//- grafo contendo as sessões corretas (serve para validar as ações iniciais/finais e formar o modelo de interação correto);
	//- permitir sobrepor o "grafo correto" com qualquer sessão (priorizar as com mais tempo, ação e piores usuários (fuzzy));
	//- clusterizar os usuários de acordo com tempo e ação;
	//-----
	
	//----- TODO -----
	//-----
	//- criar listagem dos usuários (com seus índices), grafo com as ações de quem realizou corretamente;
	//- criar plugin para facilitar na identificação do XPath, Jhm, Step e Url (depois tornar dinâmico);
	//- criar gráficos de pizza, apresentando os resultados dos usuários e das sessões.
	
	public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException, ParseException {
		EntityManager entityManager = EntityManagerUtil.getEntityManager();
		EntityManager entityDafaultManager = EntityDefaultManagerUtil.getEntityManager();
		
		TaskDataMiningRepository taskDataMiningRepository = new TaskDataMiningRepository(entityDafaultManager);
		ActionDataMiningRepository actionDataMiningRepository = new ActionDataMiningRepository(entityManager);
		
		// 31 (1440990000000) / 15 (1439607600000) / 05 (1438743600000) / 01 (1438398000000)
		//iHealth IPMT | Marcar Consulta | 07/03/2016 (1457319600000) - 21/03/2016 (1458529200000)
		ResultDataMining resultDataMining = analyze(1l, 1457319600000l, new SimpleDateFormat("dd/MM/yyyy").parse("21/03/2016").getTime(),
				SessionClassificationDataMiningFilterEnum.SUCCESS_ERROR_REPEAT, taskDataMiningRepository, actionDataMiningRepository);
		
		ResultDataMining resultDataMining2 = analyze(2l, 1457319600000l, new SimpleDateFormat("dd/MM/yyyy").parse("21/03/2016").getTime(),
				SessionClassificationDataMiningFilterEnum.SUCCESS_ERROR_REPEAT, taskDataMiningRepository, actionDataMiningRepository);
		
//		System.out.println("Detectando sessões custosas...");
//		new UsabilitySmellDetector().detectLaboriousSessions(resultDataMining.getSessions(), UsabilitySmellDetector.NUMBER_DEFAULT, UsabilitySmellDetector.NUMBER_DEFAULT);
//		System.out.println("Detecção finalizada");
		
		List<TaskSmellAnalysis> tasks = new ArrayList<TaskSmellAnalysis>();
		tasks.add(new TaskSmellAnalysis("1", resultDataMining.getSessions()));
		tasks.add(new TaskSmellAnalysis("2", resultDataMining2.getSessions()));
		
//		System.out.println("Gerando gráfico de quantidade de ações...");
//		new UsabilitySmellDetector().generateTaskActionCountChart(tasks);
//		System.out.println("Geração finalizada");
		
//		System.out.println("Gerando gráfico de duração de sessão...");
//		new UsabilitySmellDetector().generateTaskTimeChart("Tempo", resultDataMining.getSessions());
//		System.out.println("Geração finalizada");
		
//		System.out.println("Detectando sessões cíclicas...");
//		new UsabilitySmellDetector().detectCyclicSessions(resultDataMining.getSessions(), UsabilitySmellDetector.RATE_DEFAULT);
//		System.out.println("Detecção finalizada");
		
//		System.out.println("Gerando gráfico de ciclos...");
//		new UsabilitySmellDetector().generateTaskCyclicChart(resultDataMining.getSessions());
//		System.out.println("Geração finalizada");
		
//		System.out.println("Detectado sessões com muitas camadas...");
//		new UsabilitySmellDetector().detectTooManyLayers(resultDataMining.getSessions(), UsabilitySmellDetector.NUMBER_DEFAULT);
//		System.out.println("Detecção finalizada");
		
//		List<ActionDataMining> listActionsBetweenDates = WebUsageMining.listActionsBetweenDates(
//				3l, taskDataMiningRepository, actionDataMiningRepository,
//				new SimpleDateFormat("dd/MM/yyyy").parse("7/03/2016"),
//				new SimpleDateFormat("dd/MM/yyyy").parse("21/03/2016"), null);
//		
//		System.out.println("Detectando ações...");
//		new UsabilitySmellDetector().detectLonelyActions(listActionsBetweenDates,
//				UsabilitySmellDetector.RATE_DEFAULT, UsabilitySmellDetector.NUMBER_DEFAULT);
//		System.out.println("Terminou");
		
//		System.out.println("Detectando tentatvas de tooltip...");
//		new UsabilitySmellDetector().detectUndescriptiveElement(listActionsBetweenDates, 2000);
//		System.out.println("Terminou");
		
//		System.out.println("Detectado Missing Feedback...");
//		new UsabilitySmellDetector().detectMissingFeedback(resultDataMining.getSessions(), 1, 5);
//		System.out.println("Detecção finalizada!");
		
//		List<ActionDataMining> lista = new ArrayList<ActionDataMining>();
//		
//		for (int i = 0; i < 10; i++) {
//			ActionDataMining action = new ActionDataMining();
//			action.setsXPath("abc");
//			action.setsUrl("www");
//			action.setsTime(0l);
//			lista.add(action);
//		}
//		
//		lista.get(4).setsUrl("ww1");
//		lista.get(4).setsXPath("123");
//		lista.get(2).setsUrl("ww1");
//		lista.get(6).setsUrl("ww2");
//		lista.get(6).setsXPath("xyz");
//		lista.get(3).setsUrl("ww2");
//		lista.get(5).setsXPath("123");
//		lista.get(8).setsXPath("xyz");
//		
//		for (ActionDataMining action : lista) {			
//			System.out.println(action.toString());
//		}

//		System.out.println("Deu certo");
//		new UsabilitySmellDetector().detectLonelyActions(listActionsBetweenDates, 0.8f, 5);
//		System.out.println("Terminou");
		
		//		FrequentSequentialPatternMining fspm = new FrequentSequentialPatternMining();
//		List<FrequentSequentialPatternResultVO> frequentPatterns = fspm.analyze(resultDataMining.getUsersSequences(), 1.0, null, 7, 120l);
		
//		System.out.println(frequentPatterns.size());
		
//		Gson gson = new GsonBuilder().create();
//		System.out.println(gson.toJson(frequentPatterns));
//		System.out.println(gson.toJson(resultDataMining.getSessions().get(0)));
		
		
		
		//Maximizar o support
		//Maiores sequencias
		//Menos pertençam a fluxos "corretos"
		
		
		
		//retorna as ações realizadas entre duas datas
		//List<ActionDataMining> listActionsBetweenDates = WebUsageMining.listActionsBetweenDates(14l, taskDataMiningRepository, actionDataMiningRepository, initialDate, finalDate);
		//System.out.println(listActionsBetweenDates.size());
		
		//ações mais realizadas agrupadas de acordo com filtro
//		List<CountActionsAux> counts = WebUsageMining.countActionsByRestrictions(14l, new FieldSearch("sJhm", "sJhm", null, null), taskDataMiningRepository, actionDataMiningRepository, initialDate, finalDate);
//		for (CountActionsAux c : counts) {
//			System.out.println(c.getDescription() + " -> " + c.getCount());
//		}
		
		//ações realizadas por determinado usuário
//		List<ActionDataMining> actionsFromUserBetweenDates = WebUsageMining.listActionsFromUserBetweenDates(14l, "gomes", new FieldSearch("sJhm", "local", "MarcarExameAuditor", FieldSearchComparatorEnum.EQUALS), taskDataMiningRepository, actionDataMiningRepository, initialDate, finalDate);
//		System.out.println(actionsFromUserBetweenDates.size());
//		for (ActionDataMining a : actionsFromUserBetweenDates) {
//			System.out.println(a.getId() + " = " + a.getsJhm() + ", " + a.getsStepJhm() + " ... " + a.getsActionType());
//		}
		

		//analisar a usabilidade
		
		//ResultDataMining resultDataMining = analyze(14l, taskDataMiningRepository, actionDataMiningRepository);
		//System.out.println(resultDataMining.getSessions().size());
	}
	
	public static ResultDataMining analyze(Long taskId, Long initDate, Long endDate, SessionClassificationDataMiningFilterEnum classificationFilter, TaskDataMiningRepository taskDataMiningRepository, ActionDataMiningRepository actionDataMiningRepository) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
		
		boolean DEBUG = false;//false;
		TaskDataMining taskDataMining = taskDataMiningRepository.find(taskId);
		
		//#0 - pegar as configurações da tarefa
		BigDecimal threshold = new BigDecimal(taskDataMining.getThreshold() * 60000); //minutos para milesegundos
		List<ActionDataMining> actionsEnd = new ArrayList<ActionDataMining>();
		List<ActionDataMining> actionsInitial = new ArrayList<ActionDataMining>();
		for(ActionSingleDataMining actionSingle : taskDataMining.getActionsEnd()){
			actionsEnd.add(actionSingle.toActionDataMining());
		}
		for(ActionSingleDataMining actionSingle : taskDataMining.getActionsInitial()){
			actionsInitial.add(actionSingle.toActionDataMining());
		}
		
		//#1 - pegar todas as interações que possuem esses pontos de incio e agrupar por usuarios todas as interações;
		HashMap<String, List<ActionDataMining>> initialActionOfsectionsFromUser = new HashMap<String, List<ActionDataMining>>();
		HashMap<Long, List<ActionDataMining>> actionsFromInitialActionControl = new HashMap<Long, List<ActionDataMining>>();
		if (taskDataMining.getIsControl()) {
			List<Fluxo> fluxos = taskDataMining.getTaskControl().getFluxos();
			for (Fluxo f : fluxos) {
				Action a = f.getAcoes().get(0);
				ActionDataMining adm = new ActionDataMining(a);
				if(initialActionOfsectionsFromUser.get(f.getUsuario().getNome()) == null){
					initialActionOfsectionsFromUser.put(f.getUsuario().getNome(), new ArrayList<ActionDataMining>());
				}
				initialActionOfsectionsFromUser.get(f.getUsuario().getNome()).add(adm);
				
				List<ActionDataMining> actions = new ArrayList<ActionDataMining>();
				for (Action act : f.getAcoes()) {
					actions.add(new ActionDataMining(act));
				}
				actionsFromInitialActionControl.put(a.getId(), actions);
			}
		} else {
			for(ActionSingleDataMining a : taskDataMining.getActionsInitial()){
				List<FieldSearch> fieldsSearch = new ArrayList<FieldSearch>();
				fieldsSearch.add(new FieldSearch("sActionType", "sActionType", a.getActionType().getAction(), FieldSearchComparatorEnum.EQUALS));
				for(FieldSearchTupleDataMining f : a.getElementFiedlSearch()){
					/*CAMBIO MILL*/
					if (!f.valueToObject().equals("null")){
					/*FIN MILL*/
					fieldsSearch.add(new FieldSearch(f.getField(), f.getField(), f.valueToObject(), FieldSearchComparatorEnum.EQUALS));
					/*CAMBIO MILL*/
					}
					/*FIN MILL*/
				}
				for(FieldSearchTupleDataMining f : a.getUrlFieldSearch()){
					if (taskDataMining.getTestDataMining().getIsIgnoreURL()) {
						if(!f.getField().equals("sUrl")){
							fieldsSearch.add(new FieldSearch(f.getField(), f.getField(), f.valueToObject(), FieldSearchComparatorEnum.EQUALS));
						}
					} else {
						if (f.valueToObject() != null) {
							fieldsSearch.add(new FieldSearch(f.getField(), f.getField(), f.valueToObject(), FieldSearchComparatorEnum.EQUALS));
						}
					}
				}
				//verifica janela temporal
				fieldsSearch.add(new FieldSearch("sTime", "sTimeInit", initDate, FieldSearchComparatorEnum.GREATER_EQUALS_THAN));
				fieldsSearch.add(new FieldSearch("sTime", "sTimeEnd", endDate, FieldSearchComparatorEnum.LESS_EQUALS_THAN));
				
				List<ActionDataMining> actions = actionDataMiningRepository.getActions(fieldsSearch, taskDataMining.getDisregardActions(), null, null);
				for(ActionDataMining ai : actions){
					if(initialActionOfsectionsFromUser.get(ai.getsUsername()) == null){
						initialActionOfsectionsFromUser.put(ai.getsUsername(), new ArrayList<ActionDataMining>());
					}
					initialActionOfsectionsFromUser.get(ai.getsUsername()).add(ai);
				}
			}
		}
		

		if (DEBUG) {
			System.out.println(initialActionOfsectionsFromUser.size());
			for(String u : initialActionOfsectionsFromUser.keySet()){
				System.out.println(u + " -> " + initialActionOfsectionsFromUser.get(u).size());
			}
		}
		
		//#2 - para cada sessão dos usuarios, buscar as acoes (filtradas, se necessário) realizadas
		//#3 - montar o resultdatamining
		List<UserResultDataMining> usersResult = new ArrayList<UserResultDataMining>();
		List<SessionResultDataMining> sessionsResult = new ArrayList<SessionResultDataMining>();
		int countoktotal = 0, counterrototal = 0, countinittotal = 0, countthresholdtotal = 0, countTaskSessions = 0;
		double countTaskActionsSessionsOk = 0, countTaskTimesSessionsOk = 0,
				maxTimeAverageOk = 0, maxActionsAverageOk = 0,
				maxActionOk = 0, maxTimeOk = 0,
				minActionOk = Double.MAX_VALUE, minTimeOk = Double.MAX_VALUE;
		
		//Statistics Analysis
		List<Double> actionsSize = new ArrayList<Double>(),
				actionsOkSize = new ArrayList<Double>(),
				timesSize = new ArrayList<Double>(),
				timesOkSize = new ArrayList<Double>();
		
		//Count Actions
		int countIdPageViewAction = 1;
		HashMap<String, String> pageViewActionIds = new HashMap<String, String>();
		HashMap<String, Integer> pageViewActionCount = new HashMap<String, Integer>();
		
		//Actions required
		List<String> actionsRequired = new ArrayList<String>();
		for(ActionSingleDataMining a : taskDataMining.getActionsRequired()){
			PageViewActionDataMining pageViewActionDataMining = new PageViewActionDataMining(a.toActionDataMining());
			actionsRequired.add(pageViewActionDataMining.getPageViewActionUnique());
		}
		HashMap<String, Integer> actionsRequiredTask = getActionsRequired(actionsRequired);
		Integer countActionsRequired = actionsRequired.size();
		Double countActionsRequiredTask = 0d;
		
		
		/*
		 DESCRIÇÃO DOS "FORs":
		 
		 itera todos os usuários
			- contabiliza indices (erro, ok, limiar, reinicio, mediatempo, mediaacoes, etc) por usuário;
			itera todas as sessões desse usuário
				- contabiliza índices por sessão;
				itera todas as ações de determinada sessão
					- cria labels que identificam cada ação e contabilizam qtas vezes foram realizadas; (geral, todos usuários)
					- verifica de acordo com as ações se a sessão é repetida, sucesso ou ultrapassou limiar;
		 */
		
		HashMap<String, String> usersSequences = new HashMap<String, String>();
		
		Set<String> usersWithInitialActions = initialActionOfsectionsFromUser.keySet();
		for(String username : usersWithInitialActions){
			
			if (DEBUG) {
				System.out.println("-----"+username+"-----");
			}
			
			List<ActionDataMining> userInitialActions = initialActionOfsectionsFromUser.get(username);
			
			//variaveis para contabilizar resultado dos usuarios
			int countok = 0, counterro = 0, countinit = 0, countthreshold = 0, countUserSessions = userInitialActions.size();
			double countUserActionsSessionsOk = 0, countUserTimesSessionsOk = 0;
			List<String> sessionsResultIds = new ArrayList<String>();
			
			Double countActionsRequiredUser = 0d;
			String userSequence = "";
			
			if (DEBUG) {
				System.out.println("UserSessions: "+countUserSessions);
			}
			
			//verificar cada sessao
			for(int i = 0; i < countUserSessions; i++){
				ActionDataMining initialAction = userInitialActions.get(i);
				List<ActionDataMining> userSectionActions;
				
				//se for a ultima sessão do usuário
				if (!taskDataMining.getIsControl()) {
					if(i == countUserSessions-1){
						userSectionActions = actionDataMiningRepository.getUserActions(username, initialAction.getsTime(), taskDataMining.getDisregardActions());
					}else{
						ActionDataMining nextInitialAction = userInitialActions.get(i+1);
						userSectionActions = actionDataMiningRepository.getUserActions(username, initialAction.getsTime(), nextInitialAction.getsTime(), taskDataMining.getDisregardActions());
					}
				} else {
					List<ActionDataMining> allUserSectionActions = actionsFromInitialActionControl.get(initialAction.getId());
					userSectionActions = new ArrayList<ActionDataMining>();
					for (ActionDataMining adm : allUserSectionActions) {
						boolean include = true;
						for (ActionTypeDataMiningEnum atdme : taskDataMining.getDisregardActions()) {
							if (adm.getsActionType().equals(atdme.getAction())) {
								include = false;
								break;
							}
						}
						if (include) {
							userSectionActions.add(adm);
						}
					}
				}
				
				
				//analise das acoes
				SessionClassificationDataMiningEnum classification = SessionClassificationDataMiningEnum.ERROR;
				List<PageViewActionDataMining> userSectionPageViewActions = new ArrayList<PageViewActionDataMining>();
				int countActions = 0, sectionSize = userSectionActions.size();
				long initialTime = 0l, endTime = 0l;
				boolean okThreshold = true;
				
				HashMap<String, Integer> actionsRequiredSession = getActionsRequired(actionsRequired);
				
				if (DEBUG) {
					System.out.println("Session("+ (i+1) + "/" + countUserSessions + "), size: " + sectionSize);
				}
				
				//iterar acoes do usuario na sessao
				ActionDataMining action = null;
				String userSequenceSection = "";
				
				for(int j = 0; j < sectionSize; j++){
					
					if(okThreshold){
						action = userSectionActions.get(j);
						
						//gerar uma descrição menor para identificar a ação
						PageViewActionDataMining pageViewActionDataMining = new PageViewActionDataMining(action);
						String pvaKey, pvaUnique = pageViewActionDataMining.getPageViewActionUnique();
						if(pageViewActionIds.get(pvaUnique) == null){
							pvaKey = ""+countIdPageViewAction;
							pageViewActionIds.put(pvaUnique, pvaKey);
							countIdPageViewAction++;
						}else{
							pvaKey = pageViewActionIds.get(pvaUnique);
						}
						pageViewActionDataMining.setIdentifier(pvaKey);
						
						userSequenceSection += pvaKey + " -1 ";
						
						//verificar se ação é "required"
						if(actionsRequiredTask.get(pvaUnique) != null){
							actionsRequiredTask.put(pvaUnique, (actionsRequiredTask.get(pvaUnique) + 1));
							actionsRequiredSession.put(pvaUnique, (actionsRequiredSession.get(pvaUnique) + 1));
							pageViewActionDataMining.setRequired(true);
						}
						
						userSectionPageViewActions.add(pageViewActionDataMining);
						countActions++;
						
						
						if(j == 0){
							initialTime = action.getsTime();
							//contabiliza a ação inicial
							if(pageViewActionCount.get(pvaKey) == null){
								pageViewActionCount.put(pvaKey, 1);
							}else{
								pageViewActionCount.put(pvaKey, (pageViewActionCount.get(pvaKey) + 1));
							}
						}else{
							
							//vejo limiar com o anterior
							ActionDataMining previousAction = userSectionActions.get(j-1);
							BigDecimal diff = new BigDecimal(action.getsTime() - previousAction.getsTime());
							if(diff.compareTo(threshold) == 1){
								okThreshold = false;
								classification = SessionClassificationDataMiningEnum.THRESHOLD;
								break;
							}
							
							//não ultrapassou limiar, então contabiliza a ação
							if(pageViewActionCount.get(pvaKey) == null){
								pageViewActionCount.put(pvaKey, 1);
							}else{
								pageViewActionCount.put(pvaKey, (pageViewActionCount.get(pvaKey) + 1));
							}
							
							//verifica se é ação inicial/final
							if(listContainsAction(actionsEnd, action)){
								classification = SessionClassificationDataMiningEnum.SUCCESS;
								break;
							}else if(listContainsAction(actionsInitial, action)){
								classification = SessionClassificationDataMiningEnum.REPEAT;
								break;
							}
						}
					}
				}
				
				//
				if (countActions == 0) {
					userSequenceSection += "0 -1 ";
				}
				
				userSequenceSection += "-2\n";
				
				if(action == null){
					if(DEBUG){
						System.out.println("action null: "+ username + "-" + (i+1));
					}
					endTime = 0;
				}else{
					endTime = action.getsTime();
				}
				
				if (DEBUG) {
					System.out.println(", classification: " + classification);
				}
				
				//resultados da sessao
				Integer numberSession = i+1;
				String sessionKey = username+"-"+ numberSession;
				Long sessionTime = endTime - initialTime;
				
				//required
				Double required = CorrectnessTask.calcCorrectnessSession(taskDataMining.getActionsRequired(), actionsRequiredSession, taskDataMining.getActionsRequiredOrder());
				countActionsRequiredUser += required;
				countActionsRequiredTask += required;
				sessionsResult.add(new SessionResultDataMining(sessionKey, numberSession.toString(), username, classification, sessionTime, userSectionPageViewActions, actionsRequiredSession, required, !okThreshold));
				
				
				//resultados do usuario por sessao
				sessionsResultIds.add(sessionKey);
				if(okThreshold){
					actionsSize.add((double) countActions);
					timesSize.add((double) sessionTime);
				}
				
				//statistics
				if(classification.equals(SessionClassificationDataMiningEnum.ERROR)){
					counterro++;
					
					//FSPM
					if (classificationFilter.equals(SessionClassificationDataMiningFilterEnum.ALL) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.WITH_PROBLEM) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.SUCCESS_ERROR_REPEAT) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.ERROR)) {
						userSequence += userSequenceSection;
					}
				}else if(classification.equals(SessionClassificationDataMiningEnum.SUCCESS)){
					countok++;
					countUserActionsSessionsOk += countActions;
					countUserTimesSessionsOk += sessionTime;
					
					maxTimeOk = maxTimeOk < sessionTime ? sessionTime : maxTimeOk;
					maxActionOk = maxActionOk < countActions ? countActions : maxActionOk;
					
					minTimeOk = minTimeOk > sessionTime ? sessionTime : minTimeOk;
					minActionOk = minActionOk > countActions ? countActions : minActionOk;
					
					//statistics
					actionsOkSize.add((double) countActions);
					timesOkSize.add((double) sessionTime);
					
					//FSPM
					if (classificationFilter.equals(SessionClassificationDataMiningFilterEnum.ALL) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.SUCCESS_ERROR_REPEAT) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.SUCCESS)) {
						userSequence += userSequenceSection;
					}
				}else if(classification.equals(SessionClassificationDataMiningEnum.REPEAT)){
					countinit++;
					
					//FSPM
					if (classificationFilter.equals(SessionClassificationDataMiningFilterEnum.ALL) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.WITH_PROBLEM) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.SUCCESS_ERROR_REPEAT) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.REPEAT)) {
						userSequence += userSequenceSection;
					}
				}else if(classification.equals(SessionClassificationDataMiningEnum.THRESHOLD)){
					countthreshold++;
					
					//FSPM
					if (classificationFilter.equals(SessionClassificationDataMiningFilterEnum.ALL) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.WITH_PROBLEM) ||
							classificationFilter.equals(SessionClassificationDataMiningFilterEnum.THRESHOLD)) {
						userSequence += userSequenceSection;
					}
				}
			}
			
			
			usersSequences.put(username, userSequence);
			
			//resultados somado para o total
			countoktotal += countok;
			counterrototal += counterro;
			countinittotal += countinit;
			countthresholdtotal += countthreshold;
			countTaskSessions += countUserSessions;
			
			countTaskTimesSessionsOk += countUserTimesSessionsOk;
			countTaskActionsSessionsOk += countUserActionsSessionsOk;
			
			//resultados por usuario
			if (DEBUG) {
				System.out.println("countUserActionsSessionsOk: " + countUserActionsSessionsOk);
				System.out.println("countUserTimesSessionsOk: " + countUserTimesSessionsOk);
				System.out.println("countok: " + countok);
			}
			
			Double actionsAverageOk = countok > 0 ? (countUserActionsSessionsOk / countok) : 0;
			Double timesAverageOk = countok > 0 ? (countUserTimesSessionsOk / countok) : 0;
			
			//dados para normalizar o fuzzy
			maxTimeAverageOk = maxTimeAverageOk < timesAverageOk ? timesAverageOk : maxTimeAverageOk;
			maxActionsAverageOk = maxActionsAverageOk < actionsAverageOk ? actionsAverageOk : maxActionsAverageOk;
			
			//required
			Double required = countActionsRequiredUser > 0 ? countActionsRequiredUser / countUserSessions : 0;
			
			usersResult.add(new UserResultDataMining(username, actionsAverageOk, timesAverageOk, countok, counterro, countinit, countthreshold, 0d, required, sessionsResultIds));
			
			if (DEBUG) {
				System.out.println("actionsAverageOk: " + actionsAverageOk);
				System.out.println("timesAverageOk: " + timesAverageOk);
				System.out.println("required: " + required);
				System.out.println(username + " [ok=" + countok + ", init=" + countinit + ", erro=" + counterro + "]");
			}
		}
		
		if (DEBUG) {
			System.out.println("########### RETA FINAL ###########");
		}
		
		Double actionsTaskAverageOk = (countTaskActionsSessionsOk / countoktotal);
		Double timesTaskAverageOk = (countTaskTimesSessionsOk / countoktotal);
		actionsTaskAverageOk = actionsTaskAverageOk.equals(Double.NaN) ? 0d : actionsTaskAverageOk;
		timesTaskAverageOk = timesTaskAverageOk.equals(Double.NaN) ? 0d : timesTaskAverageOk;
		
		if (DEBUG) {
			System.out.println("countTaskActionsSessionsOk: " + countTaskActionsSessionsOk);
			System.out.println("countoktotal: " + countoktotal);
			System.out.println("countTaskTimesSessionsOk: " + countTaskTimesSessionsOk);
			System.out.println("countoktotal: " + countoktotal);
		}
		
		//#4 - Fuzzy tempo e ações nas -> 
		
		//Fuzzy para priorizar análise de usuários
		for(UserResultDataMining u : usersResult){
			u.setFuzzyPriority(0d);
		}
		
		//Cálculo da média e desvio padrão de ações e tempo
		double meanActionsOk = StatisticsUtils.getMean(convertDouble(actionsOkSize));
		double meanTimesOk = StatisticsUtils.getMean(convertDouble(timesOkSize));
		double stdDevActionsOk = StatisticsUtils.getStdDevPopulation(convertDouble(actionsOkSize));
		double stdDevTimesOk = StatisticsUtils.getStdDevPopulation(convertDouble(timesOkSize));
		
		if (DEBUG) {
			System.out.println("actionsOkSize: " + actionsOkSize);
			System.out.println("meanActionsOk: " + meanActionsOk);
			System.out.println("timesOkSize: " + timesOkSize);
			System.out.println("meanTimesOk: " + meanTimesOk);
			System.out.println("actionsOkSize: " + actionsOkSize);
			System.out.println("stdDevActionsOk: " + stdDevActionsOk);
			System.out.println("timesOkSize: " + timesOkSize);
			System.out.println("stdDevTimesOk: " + stdDevTimesOk);
		}
		
		double minEffectiveness = Double.MAX_VALUE;
		double minEfficiency = Double.MAX_VALUE;
		double maxEffectiveness = Double.MIN_VALUE;
		double maxEfficiency = Double.MIN_VALUE;
		
		double minEffectivenessUser = Double.MAX_VALUE;
		double minEfficiencyUser = Double.MAX_VALUE;
		double maxEffectivenessUser = Double.MIN_VALUE;
		double maxEfficiencyUser = Double.MIN_VALUE;
		
		//se houver sessoes inicias por usuarios
		if(initialActionOfsectionsFromUser.size() > 0){
			//#4.1 - Cálculo da Eficácia e Eficiência
			
			//cálculo da eficácia e eficiência
			for(SessionResultDataMining s : sessionsResult){
				s.setEffectiveness(ConverterUtils.notNaN(UsabilityUtils.calcEffectiveness(s.getUserRateRequired())));
				//Double time = new Double(s.getTime()/1000);
				s.setEfficiency(ConverterUtils.notNaN(UsabilityUtils.calcEfficiency(s.getEffectiveness(), (double) s.getActions().size(), minActionOk, (double) s.getTime(), minTimeOk)));
				
				//no caso de sessões que ultrapassam o limiar, valores extrapolam limites
				if (s.getEfficiency() < 0) {
					s.setEfficiency(0d);
				} else if (s.getEfficiency() > 100) {
					s.setEfficiency(100d);
				}
				
				minEffectiveness = minEffectiveness > s.getEffectiveness() ? s.getEffectiveness() : minEffectiveness;
				minEfficiency = minEfficiency > s.getEfficiency() ? s.getEfficiency() : minEfficiency;
				maxEffectiveness = maxEffectiveness < s.getEffectiveness() ? s.getEffectiveness() : maxEffectiveness;
				maxEfficiency = maxEfficiency < s.getEfficiency() ? s.getEfficiency() : maxEfficiency;
			}
			//normaliza
			for(SessionResultDataMining s : sessionsResult){
				s.setEffectivenessNormalized( ConverterUtils.notNaN((s.getEffectiveness() - minEffectiveness) / (maxEffectiveness - minEffectiveness)) );
				s.setEfficiencyNormalized( ConverterUtils.notNaN((s.getEfficiency() - minEfficiency) / (maxEfficiency - minEfficiency)) );
				
				if(s.getEffectiveness().equals(Double.NaN)){
					s.setEffectiveness(0d);
				}
				if(s.getEfficiency().equals(Double.NaN)){
					s.setEfficiency(0d);
				}
				if(s.getEffectivenessNormalized().equals(Double.NaN)){
					s.setEffectivenessNormalized(0d);
				}
				if(s.getEfficiencyNormalized().equals(Double.NaN)){
					s.setEfficiencyNormalized(0d);
				}
			}
			
			
			for(UserResultDataMining u : usersResult){
				u.setMaxActionsAverageOk(maxActionsAverageOk);
				u.setMaxTimeAverageOk(maxTimeAverageOk);
				u.setzScoreActions(ConverterUtils.notNaN((u.getActionsAverageOk() - meanActionsOk)/stdDevActionsOk));
				u.setzScoreTimes(ConverterUtils.notNaN((u.getTimesAverageOk() - meanTimesOk)/stdDevTimesOk));
				
				//cálculo da eficácia e eficiência
				double userEffectiveness = 0d, userEfficiency = 0d;
				int countSessions = 0;
				for(SessionResultDataMining s : sessionsResult){
					if (u.getUsername().equals(s.getUsername())) {
						countSessions++;
						userEffectiveness += s.getEffectiveness();
						userEfficiency += s.getEfficiency();
					}
				}
				
				if (countSessions > 0) {
					u.setEffectiveness(userEffectiveness/countSessions);
					u.setEfficiency(userEfficiency/countSessions);
				} else {
					u.setEffectiveness(0d);
					u.setEfficiency(0d);
				}
				
				minEffectivenessUser = minEffectivenessUser > u.getEffectiveness() ? u.getEffectiveness() : minEffectivenessUser;
				minEfficiencyUser = minEfficiencyUser > u.getEfficiency() ? u.getEfficiency() : minEfficiencyUser;
				maxEffectivenessUser = maxEffectivenessUser < u.getEffectiveness() ? u.getEffectiveness() : maxEffectivenessUser;
				maxEfficiencyUser = maxEfficiencyUser < u.getEfficiency() ? u.getEfficiency() : maxEfficiencyUser;
			}
			//normaliza os dados dos usuários
			for(UserResultDataMining u : usersResult){
				u.setEffectivenessNormalized( (u.getEffectiveness() - minEffectivenessUser) / (maxEffectivenessUser - minEffectivenessUser));
				u.setEfficiencyNormalized( (u.getEfficiency() - minEfficiencyUser) / (maxEfficiencyUser - minEfficiencyUser));
				
				if(u.getEffectiveness().equals(Double.NaN)){
					u.setEffectiveness(0d);
				}
				if(u.getEfficiency().equals(Double.NaN)){
					u.setEfficiency(0d);
				}
				if(u.getEffectivenessNormalized().equals(Double.NaN)){
					u.setEffectivenessNormalized(0d);
				}
				if(u.getEfficiencyNormalized().equals(Double.NaN)){
					u.setEfficiencyNormalized(0d);
				}
			}
			
			//verificar taxa de sucesso por sessão
			
			
			
			/* Fuzzy CMeans */
			/*
			DEBUG = false;
			Fuzzy.executeFuzzySystemWithFCMUsersEfcEft(usersResult, IGNORE_ZERO, DEBUG);
			System.out.println("### Fuzzy CMeans Users EfcEft Ok");
			
			Fuzzy.executeFuzzySystemWithFCMSessionEfcEft(sessionsResult, IGNORE_ZERO, DEBUG);
			System.out.println("### Fuzzy CMeans Sessions EfcEft Ok");
			
			Fuzzy.executeFuzzySystemWithFCM(usersResult, IGNORE_ZERO, DEBUG);
			System.out.println("### Fuzzy CMeans Users Ok");
			
			Fuzzy.executeFuzzySystemWithFCMSession(sessionsResult, IGNORE_ZERO, DEBUG);
			System.out.println("### Fuzzy CMeans Sessions Ok");
			*/
		}
		
		//Reverse Map ids
		HashMap<String, String> pageViewActionIdsReverse = new HashMap<String, String>();
		Set<String> keySet = pageViewActionIds.keySet();
		for(String k : keySet){
			pageViewActionIdsReverse.put(pageViewActionIds.get(k), k);
		}
		
		// Verificar se os valores de maximo e minimo foram alterados, senão mudar para 0

		
		
		// pageViewActionIdsReverse
		ResultDataMining result = new ResultDataMining(usersResult, sessionsResult, actionsTaskAverageOk, timesTaskAverageOk, countoktotal, counterrototal, countinittotal, countthresholdtotal, pageViewActionIdsReverse, pageViewActionCount);
		
		//required
		Double required = countActionsRequiredTask > 0 ? countActionsRequiredTask / countTaskSessions : 0;
		
		if (DEBUG) {
			System.out.println("########### RESULT: ###########");
			System.out.println("countActionsRequiredTask: " + countActionsRequiredTask);
			System.out.println("countTaskSessions: " + countTaskSessions);
		}
		
		result.setUsersSequences(usersSequences);
		result.setActionsRequiredTask(actionsRequiredTask);
		
		if (result.getCountSessions() > 0) {
			result.setRateRequired(required);			
			
			result.setMeanActionsOk(meanActionsOk);
			result.setMeanTimesOk(meanTimesOk);
			
			//
			result.setStdDevActionsOk(stdDevActionsOk);
			result.setStdDevTimesOk(stdDevTimesOk);
			//
			result.setMinActionsOk(minDouble(actionsOkSize));
			result.setMinTimesOk(minDouble(timesOkSize));
			//
			result.setMaxActionsOk(maxDouble(actionsOkSize));
			result.setMaxTimesOk(maxDouble(timesOkSize));
		} else {
			generateResultWithoutSessions(result);
		}
		
		if (DEBUG) {
			System.out.println("result.getRateRequired(): " + result.getRateRequired());
			System.out.println("result.getMeanActionsOk(): " + result.getMeanActionsOk());
			System.out.println("result.getMeanTimesOk(): " + result.getMeanTimesOk());
			System.out.println("result.getStdDevActionsOk(): " + result.getStdDevActionsOk());
			System.out.println("result.getStdDevTimesOk(): " + result.getStdDevTimesOk());
			System.out.println("result.getMinActionsOk(): " + result.getMinActionsOk());
			System.out.println("result.getMinTimesOkk(): " + result.getMinTimesOk());
			System.out.println("result.getMaxActionsOk(): " + result.getMaxActionsOk());
			System.out.println("result.getMaxTimesOk(): " + result.getMaxTimesOk());
			
			
			System.out.println("MaxActions = "+result.getMaxActionsOk()+" ;MaxTime = "+result.getMaxTimesOk());
			System.out.println("Actions = " + (result.getMaxActionsOk() - result.getMeanActionsOk()) / result.getStdDevActionsOk());
			System.out.println("Times = " + (result.getMaxTimesOk() - result.getMeanTimesOk()) / result.getStdDevTimesOk());
		}
		
		return result;
	}
	
	private static void generateResultWithoutSessions(ResultDataMining result){
		result.setActionsAverageOk(0d);
		result.setTimesAverageOk(0d);
		result.setRateRequired(0d);
		result.setRateSuccess(0d);
		
		result.setMeanActionsOk(0d);
		result.setMeanTimesOk(0d);
		//
		result.setStdDevActionsOk(0d);
		result.setStdDevTimesOk(0d);
		//
		result.setMinActionsOk(0d);
		result.setMinTimesOk(0d);
		//
		result.setMaxActionsOk(0d);
		result.setMaxTimesOk(0d);
	}
	
	public static List<CountActionsAux> countActionsByRestrictions(Long taskId, FieldSearch fieldGroup, TestDataMiningRepository testDataMiningRepository, ActionDataMiningRepository actionDataMiningRepository, Date initialDate, Date finalDate) {
		TestDataMining taskDataMining = testDataMiningRepository.find(taskId);
		String clientAbbreviation = taskDataMining.getClientAbbreviation();
		
		List<FieldSearch> fieldsSearch = new ArrayList<FieldSearch>();
		fieldsSearch.add(new FieldSearch("sClient", "sClient", clientAbbreviation, FieldSearchComparatorEnum.EQUALS));
		if (initialDate != null) {
			fieldsSearch.add(new FieldSearch("sTime", "sTime1", initialDate.getTime(), FieldSearchComparatorEnum.GREATER_EQUALS_THAN));
		}
		if (finalDate != null) {
			fieldsSearch.add(new FieldSearch("sTime", "sTime2", finalDate.getTime(), FieldSearchComparatorEnum.LESS_EQUALS_THAN));
		}
		
		return actionDataMiningRepository.getCountActionsByRestrictions(fieldGroup, fieldsSearch, null);
	}
	
	public static List<ActionDataMining> listActionsBetweenDates(Long taskId, TaskDataMiningRepository taskDataMiningRepository, ActionDataMiningRepository actionDataMiningRepository, Date initialDate, Date finalDate, Long limit) {
		TaskDataMining taskDataMining = taskDataMiningRepository.find(taskId);
		String clientAbbreviation = taskDataMining.getTestDataMining().getClientAbbreviation();
		
		return listActionsBetweenDates(clientAbbreviation, taskDataMiningRepository, actionDataMiningRepository, initialDate, finalDate, limit);
	}
	
	public static List<ActionDataMining> listActionsBetweenDates(String clientAbbreviation, TaskDataMiningRepository taskDataMiningRepository, ActionDataMiningRepository actionDataMiningRepository, Date initialDate, Date finalDate, Long limit) {
		List<FieldSearch> fieldsSearch = new ArrayList<FieldSearch>();
		fieldsSearch.add(new FieldSearch("sClient", "sClient", clientAbbreviation, FieldSearchComparatorEnum.EQUALS));
		if (initialDate != null) {
			fieldsSearch.add(new FieldSearch("sTime", "sTime1", initialDate.getTime(), FieldSearchComparatorEnum.GREATER_EQUALS_THAN));
		}
		if (finalDate != null) {
			fieldsSearch.add(new FieldSearch("sTime", "sTime2", finalDate.getTime(), FieldSearchComparatorEnum.LESS_EQUALS_THAN));
		}
		
		return actionDataMiningRepository.getActions(fieldsSearch, null, new OrderSearch("sTime", true), limit);
	}
	
	/*MILL*/
	/**
	 * Obtiene el listado de acciones entre fechas en orden de usuario, url y tiempo
	 * 
	 * @param	clientAbbreviation			nombre del cliente
	 * @param	taskDataMiningRepository	Repositorio de las tareas
	 * @param	actionDataMiningRepository	Repositorio de las acciones
	 * @param	initialDate					fecha inicial
	 * @param	finalDate					fecha final
	 * @param	limit						cantidad maxima de registros a mostrar
	 * @return								listado de acciones ordenadas
	 */
	public static List<ActionDataMining> listActionsBetweenDatesOrder(String clientAbbreviation, TaskDataMiningRepository taskDataMiningRepository, 
																		ActionDataMiningRepository actionDataMiningRepository, Date initialDate, Date finalDate, Long limit) {
		List<FieldSearch> fieldsSearch = new ArrayList<FieldSearch>();
		List<OrderSearch> orderSearch = new ArrayList<OrderSearch>();
		fieldsSearch.add(new FieldSearch("sClient", "sClient", clientAbbreviation, FieldSearchComparatorEnum.EQUALS));
		if (initialDate != null) {
			fieldsSearch.add(new FieldSearch("sTime", "sTime1", initialDate.getTime(), FieldSearchComparatorEnum.GREATER_EQUALS_THAN));
		}
		if (finalDate != null) {
			fieldsSearch.add(new FieldSearch("sTime", "sTime2", finalDate.getTime(), FieldSearchComparatorEnum.LESS_EQUALS_THAN));
		}
		orderSearch.add(new OrderSearch("sUsername", true));
		orderSearch.add(new OrderSearch("sUrl", true));
		orderSearch.add(new OrderSearch("sTime", true));
		return actionDataMiningRepository.getActionsOrder(fieldsSearch, null, orderSearch, limit);
	}
	/*FIN MILL*/
	
	public static List<ActionDataMining> listActionsFromUserBetweenDates(String clientAbbreviation, String username, FieldSearch actionLocal, ActionDataMiningRepository actionDataMiningRepository, Date initialDate, Date finalDate, Long limit) {
		List<FieldSearch> fieldsSearch = new ArrayList<FieldSearch>();
		fieldsSearch.add(new FieldSearch("sClient", "sClient", clientAbbreviation, FieldSearchComparatorEnum.EQUALS));
		fieldsSearch.add(new FieldSearch("sUsername", "sUsername", username, FieldSearchComparatorEnum.EQUALS));
		
		if (initialDate != null) {
			fieldsSearch.add(new FieldSearch("sTime", "sTime1", initialDate.getTime(), FieldSearchComparatorEnum.GREATER_EQUALS_THAN));
		}
		if (finalDate != null) {
			fieldsSearch.add(new FieldSearch("sTime", "sTime2", finalDate.getTime(), FieldSearchComparatorEnum.LESS_EQUALS_THAN));
		}
		if (actionLocal != null) {
			fieldsSearch.add(new FieldSearch(actionLocal.getField(), actionLocal.getAlias(), actionLocal.getValue(), FieldSearchComparatorEnum.EQUALS));
		}
		
		return actionDataMiningRepository.getActions(fieldsSearch, null, new OrderSearch("sTime", true), limit);
	}
	
	private static boolean listContainsAction(List<ActionDataMining> list, ActionDataMining action){
		for(ActionDataMining a : list){
			
			boolean containsJhm = a.getsJhm() != null && action.getsJhm() != null;
			boolean actionsEqualsLocation = !containsJhm ? a.getsUrl().equals(action.getsUrl()) : (a.getsJhm().equals(action.getsJhm()) && a.getsStepJhm().equals(action.getsStepJhm()));
			
			if(a.getsActionType().equals(action.getsActionType()) && actionsEqualsLocation){
				
				if(action.getsActionType().equals(ActionTypeDataMiningEnum.click.getAction()) ||
					action.getsActionType().equals(ActionTypeDataMiningEnum.focusout.getAction()) ||
					action.getsActionType().equals(ActionTypeDataMiningEnum.mouseover.getAction())){
					
					if(a.getsXPath().replaceAll("\\\\", "").equals(action.getsXPath())){
						return true;
					}
				}else{
					return true;
				}
				
			}
		}
		return false;
	}
	
	private static HashMap<String, Integer> getActionsRequired(List<String> actionsRequired) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{
		HashMap<String, Integer> actionsRequiredSession = new HashMap<String, Integer>();
		for(String a : actionsRequired){
			actionsRequiredSession.put(a, 0);
		}
		return actionsRequiredSession;
	}
	
	private static double minMaxValToZero(Double d) {
		return (d.equals(Double.MAX_VALUE) || d.equals(Double.MIN_VALUE)) ? 0d : d;
	}
	
	private static double minDouble(List<Double> numbers){
		double min = Double.MAX_VALUE;
	    Iterator<Double> iterator = numbers.iterator();
	    while (iterator.hasNext()){
	    	double n = iterator.next().doubleValue();
	    	min = min > n ? n : min;
	    }
	    return minMaxValToZero(min);
	}
	
	private static double maxDouble(List<Double> numbers){
		double max = Double.MIN_VALUE;
	    Iterator<Double> iterator = numbers.iterator();
	    while (iterator.hasNext()){
	    	double n = iterator.next().doubleValue();
	    	max = max < n ? n : max;
	    }
	    return minMaxValToZero(max);
	}
	
	private static double[] convertDouble(List<Double> numbers){
		double[] ret = new double[numbers.size()];
	    Iterator<Double> iterator = numbers.iterator();
	    for (int i = 0; i < ret.length; i++){
	        ret[i] = iterator.next().doubleValue();
	    }
	    return ret;
	}
	
}