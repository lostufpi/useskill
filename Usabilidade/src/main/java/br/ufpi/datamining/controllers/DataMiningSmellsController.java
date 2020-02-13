package br.ufpi.datamining.controllers;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.caelum.vraptor.Consumes;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.view.Results;
import br.ufpi.annotation.Logado;
import br.ufpi.componets.TesteView;
import br.ufpi.componets.UsuarioLogado;
import br.ufpi.componets.ValidateComponente;
import br.ufpi.controllers.BaseController;
import br.ufpi.datamining.analisys.WebUsageMining;
import br.ufpi.datamining.models.ActionDataMining;
import br.ufpi.datamining.models.IgnoredUrlDataMining;
import br.ufpi.datamining.models.ParameterSmellTestDataMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.TestDataMining;
import br.ufpi.datamining.models.auxiliar.BarChart;
import br.ufpi.datamining.models.auxiliar.ResultDataMining;
import br.ufpi.datamining.models.auxiliar.StackedAreaChart;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysis;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysisGroupedResult;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysisResult;
import br.ufpi.datamining.models.enums.ReturnStatusEnum;
import br.ufpi.datamining.models.enums.SessionClassificationDataMiningFilterEnum;
import br.ufpi.datamining.models.vo.ChartsVO;
import br.ufpi.datamining.models.vo.ReturnVO;
import br.ufpi.datamining.models.vo.SmellAnalysisGroupedResultVO;
import br.ufpi.datamining.models.vo.SmellAnalysisResultVO;
import br.ufpi.datamining.models.vo.TaskDataMiningVO;
import br.ufpi.datamining.models.vo.TestDataMiningVO;
import br.ufpi.datamining.repositories.ActionDataMiningRepository;
import br.ufpi.datamining.repositories.IgnoredUrlDataMiningRepository;
import br.ufpi.datamining.repositories.ParameterSmellDataMiningRepository;
import br.ufpi.datamining.repositories.ParameterSmellTestDataMiningRepository;
import br.ufpi.datamining.repositories.TaskDataMiningRepository;
import br.ufpi.datamining.utils.EntityDefaultManagerUtil;
import br.ufpi.datamining.utils.EntityManagerUtil;
import br.ufpi.datamining.utils.ParameterSmellUtils;
import br.ufpi.util.UsabilitySmellDetector;

@Path(value = "datamining")
@Resource
public class DataMiningSmellsController extends BaseController {
	
	private boolean debug = true;
	
	//metricas
	private static final int TASK_ACTION_COUNT = 1;
	private static final int TASK_TIME = 2;
	private static final int TASK_CYCLE_RATE = 3;
	private static final int ACTION_OCCURRENCE_RATE = 4;
	private static final int TASK_LAYER_COUNT = 5;
	private static final int ACTION_TOOLTIP_COUNT = 6;
	private static final int ACTION_REPETITION_COUNT = 7;
	private static final int URL_DIFF_ACTION_COUNT = 8;
	private static final int ACTION_COUNT = 9; //MILL
	private static final int CONTEXT_COUNT = 91; //MILL
	
	//parametros
	private static final int MAX_ACTION_COUNT = 1;
	private static final int MAX_TIME = 2;
	private static final int MAX_CYCLE_RATE = 3;
	private static final int MAX_OCCURRENCE_RATE = 4;
	private static final int MIN_OCCURRENCE_COUNT_LA = 5;
	private static final int MAX_LAYER_COUNT = 6;
	private static final int MAX_ATTEMPT_COUNT = 7;
	private static final int MAX_REPETITION_COUNT = 8;
	private static final int MIN_OCCURRENCE_COUNT_MF = 9;
	private static final int MIN_SESSION_COUNT = 10;
	private static final int MIN_ACTION_COUNT = 11;
	private static final int MAX_TIME_INTERVAL = 12;
	private static final int MAX_DIFF_ACTION_COUNT = 13;
	
	//smells
	private static final int LABORIOUS_TASK = 1;
	private static final int CYCLIC_TASK = 2;
	private static final int LONELY_ACTION = 3;
	private static final int TOO_MANY_LAYERS = 4;
	private static final int UNDESCRIPTIVE_ELEMENT = 5;
	private static final int MISSING_FEEDBACK = 6;
	private static final int FAT_INTERFACE = 7;
	
	private final ParameterSmellDataMiningRepository parameterSmellDataMiningRepository;
	private final ParameterSmellTestDataMiningRepository parameterSmellTestDataMiningRepository;
	private final IgnoredUrlDataMiningRepository ignoredUrlRepository;
	
	public DataMiningSmellsController(Result result, Validator validator,
			TesteView testeView, UsuarioLogado usuarioLogado,
			ValidateComponente validateComponente,
			ParameterSmellDataMiningRepository parameterSmellDataMiningRepository,
			ParameterSmellTestDataMiningRepository parameterSmellTestDataMiningRepository,
			IgnoredUrlDataMiningRepository ignoredUrlRepository
			) {
		super(result, validator, testeView, usuarioLogado, validateComponente);
		this.parameterSmellDataMiningRepository = parameterSmellDataMiningRepository;
		this.parameterSmellTestDataMiningRepository = parameterSmellTestDataMiningRepository;
		this.ignoredUrlRepository = ignoredUrlRepository;
	}
	
	@Post("/testes/smells/statistics")
	@Consumes("application/json")
	@Logado
	public void view(TestDataMining test, Long initDate, Long endDate, Integer[] selectedMetrics, Boolean useLiteral, Integer maxResultCount, String[] ignoredUrls) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, InterruptedException {
		Gson gson = new GsonBuilder()
			.setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		EntityManager entityManager = EntityManagerUtil.getEntityManager();
		EntityManager entityDafaultManager = EntityDefaultManagerUtil.getEntityManager();
		
		TaskDataMiningRepository taskDataMiningRepository = new TaskDataMiningRepository(entityDafaultManager);
		ActionDataMiningRepository actionDataMiningRepository = new ActionDataMiningRepository(entityManager);
		
		UsabilitySmellDetector usm = new UsabilitySmellDetector();
		List<TaskSmellAnalysis> tasks = new ArrayList<TaskSmellAnalysis>();
		List<ActionDataMining> actions = new ArrayList<ActionDataMining>();
		
		List<StackedAreaChart> areaCharts = new ArrayList<StackedAreaChart>();
		List<BarChart> barCharts = new ArrayList<BarChart>();
		
		boolean mineSessions =
				ArrayUtils.contains(selectedMetrics, TASK_ACTION_COUNT) || ArrayUtils.contains(selectedMetrics, TASK_TIME) ||
				ArrayUtils.contains(selectedMetrics, TASK_CYCLE_RATE) || ArrayUtils.contains(selectedMetrics, TASK_LAYER_COUNT);
		boolean mineActions =
				ArrayUtils.contains(selectedMetrics, ACTION_OCCURRENCE_RATE) || ArrayUtils.contains(selectedMetrics, ACTION_TOOLTIP_COUNT) ||
				ArrayUtils.contains(selectedMetrics, ACTION_REPETITION_COUNT) || ArrayUtils.contains(selectedMetrics, URL_DIFF_ACTION_COUNT) || 
				ArrayUtils.contains(selectedMetrics, ACTION_COUNT); //MILL
		
		if (mineSessions) {
			for (TaskDataMining task : test.getTasks()) {
				if (debug) System.out.println("Mining sessions for task " + task.getTitle() + "...");
				ResultDataMining resultDataMining = WebUsageMining.analyze(task.getId(), initDate, endDate,
						SessionClassificationDataMiningFilterEnum.SUCCESS_ERROR_REPEAT, taskDataMiningRepository, actionDataMiningRepository);
				if (debug) System.out.println(resultDataMining.getSessions().size() + " sessions found.");
				if (resultDataMining.getSessions().size() > 0)
					tasks.add(new TaskSmellAnalysis(task.getTitle(), resultDataMining.getSessions()));
			}
		}
		
		if (mineActions) {
			if (debug) System.out.println("Mining actions...");
			actions = WebUsageMining.listActionsBetweenDates(
					test.getClientAbbreviation(), taskDataMiningRepository, actionDataMiningRepository,
					new Date(initDate), new Date(endDate), null);
			if (debug) System.out.println(actions.size() + " actions found.");
			for (String url : ignoredUrls) {
				Iterator<ActionDataMining> i = actions.iterator();
				while (i.hasNext()) {
					ActionDataMining action = i.next();
					if (action.getsUrl().contains(url))
						i.remove();
				}				
			}
		}
		
		if (ArrayUtils.contains(selectedMetrics, TASK_ACTION_COUNT)) {
			if (debug) System.out.println("Generating action count chart...");
			areaCharts.add(usm.generateTaskActionCountChart(tasks, useLiteral));
		}
		if (ArrayUtils.contains(selectedMetrics, TASK_TIME)) {
			if (debug) System.out.println("Generating time chart...");
			areaCharts.add(usm.generateTaskTimeChart(tasks, useLiteral));
		}
		if (ArrayUtils.contains(selectedMetrics, TASK_CYCLE_RATE)) {
			if (debug) System.out.println("Generating cycle rate chart...");
			areaCharts.add(usm.generateTaskCycleRateChart(tasks, useLiteral));
		}
		if (ArrayUtils.contains(selectedMetrics, ACTION_OCCURRENCE_RATE)) {
			if (debug) System.out.println("Generating action occurrence rate chart...");
			barCharts.add(usm.generateActionOccurrenceRateChart(actions, maxResultCount));
		}
		if (ArrayUtils.contains(selectedMetrics, TASK_LAYER_COUNT)) {
			if (debug) System.out.println("Generating layer count chart...");
			areaCharts.add(usm.generateTaskLayerCountChart(tasks, useLiteral));
		}
		if (ArrayUtils.contains(selectedMetrics, ACTION_TOOLTIP_COUNT)) {
			if (debug) System.out.println("Generating tooltip count chart...");
			barCharts.add(usm.generateActionTooltipCountChart(actions, maxResultCount));
		}
		if (ArrayUtils.contains(selectedMetrics, ACTION_REPETITION_COUNT)) {
			if (debug) System.out.println("Generating action repetition count chart...");
			barCharts.add(usm.generateActionRepetitionCountChart(actions, maxResultCount));
		}
		if (ArrayUtils.contains(selectedMetrics, URL_DIFF_ACTION_COUNT)) {
			if (debug) System.out.println("Generating url different action count chart...");
			barCharts.add(usm.generateUrlElementCountChart(actions, maxResultCount));
		}
		
		//MILL
		if (ArrayUtils.contains(selectedMetrics, ACTION_COUNT)) {
			if (debug) System.out.println("Generating action count chart...");
			barCharts.add(usm.generateCountChart(actions, maxResultCount));
		}
		if (ArrayUtils.contains(selectedMetrics, CONTEXT_COUNT)) {
			if (debug) System.out.println("Generating context count chart...");
			barCharts.add(usm.generateContextChart(actions, maxResultCount));
		}
		//FIN MILL
		
		if (areaCharts.size() > 0 || barCharts.size() > 0) {
			String json = gson.toJson(new ChartsVO(areaCharts, barCharts));
			result.use(Results.json()).from(json).serialize();
		} else {
			ReturnVO returnvo = new ReturnVO(ReturnStatusEnum.ERRO, "datamining.smells.error.default");
			result.use(Results.json()).from(gson.toJson(returnvo)).serialize();
		}
		
	}
	
	@Post("/testes/smells/detection")
	@Consumes("application/json")
	@Logado
	public void view(TestDataMining test, Long initDate, Long endDate, Integer[] selectedSmells, Long[] selectedTasks, Boolean groupSessions, Double similarityRate, String[] ignoredUrls) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
		Gson gson = new GsonBuilder()
			.setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		ParameterSmellUtils.findParametersSmellValues(test.getId(), parameterSmellDataMiningRepository, parameterSmellTestDataMiningRepository);
		List<ParameterSmellTestDataMining> smellsParametersValues = ParameterSmellUtils.findParametersSmellValues(test.getId(),
				parameterSmellDataMiningRepository,
				parameterSmellTestDataMiningRepository);
		
		EntityManager entityManager = EntityManagerUtil.getEntityManager();
		EntityManager entityDafaultManager = EntityDefaultManagerUtil.getEntityManager();
		
		TaskDataMiningRepository taskDataMiningRepository = new TaskDataMiningRepository(entityDafaultManager);
		ActionDataMiningRepository actionDataMiningRepository = new ActionDataMiningRepository(entityManager);
		
		UsabilitySmellDetector usm = new UsabilitySmellDetector();
		List<TaskSmellAnalysis> tasks = new ArrayList<TaskSmellAnalysis>();
		List<ActionDataMining> actions = new ArrayList<ActionDataMining>();
		
		Map<String, List<TaskSmellAnalysisResult>> tasksAnalysisResult = new LinkedHashMap<String, List<TaskSmellAnalysisResult>>();
		Map<String, Map<String, Map<String, String>>> actionsAnalysisResult = new LinkedHashMap<String, Map<String,Map<String,String>>>();
		
		boolean mineSessions =
				ArrayUtils.contains(selectedSmells, LABORIOUS_TASK) || ArrayUtils.contains(selectedSmells, CYCLIC_TASK) ||
				ArrayUtils.contains(selectedSmells, TOO_MANY_LAYERS);
		boolean mineActions =
				ArrayUtils.contains(selectedSmells, LONELY_ACTION) || ArrayUtils.contains(selectedSmells, UNDESCRIPTIVE_ELEMENT) ||
				ArrayUtils.contains(selectedSmells, MISSING_FEEDBACK) || ArrayUtils.contains(selectedSmells, FAT_INTERFACE);
		
		Stopwatch tasksTimer = Stopwatch.createStarted();
		if (mineSessions) {
			for (TaskDataMining task : test.getTasks()) {
				if (ArrayUtils.contains(selectedTasks, task.getId())) {
					if (debug) System.out.println("Mining sessions for task " + task.getTitle() + "...");
					Stopwatch taskTimer = Stopwatch.createStarted();
					ResultDataMining resultDataMining = WebUsageMining.analyze(task.getId(), initDate, endDate,
							SessionClassificationDataMiningFilterEnum.SUCCESS_ERROR_REPEAT, taskDataMiningRepository, actionDataMiningRepository);
					System.out.println("Finished in " + taskTimer.stop());
					if (debug) System.out.println(resultDataMining.getSessions().size() + " sessions found.");
					if (resultDataMining.getSessions().size() > 0)
						tasks.add(new TaskSmellAnalysis(task.getTitle(), resultDataMining.getSessions()));				
				}
			}
		}
		System.out.println("Task mining took " + tasksTimer.stop());
		
		Stopwatch actionsTimer = Stopwatch.createStarted();
		if (mineActions) {
			if (debug) System.out.println("Mining actions...");
			actions = WebUsageMining.listActionsBetweenDates(
					test.getClientAbbreviation(), taskDataMiningRepository, actionDataMiningRepository,
					new Date(initDate), new Date(endDate), null);
			if (debug) System.out.println(actions.size() + " actions found.");
			for (String url : ignoredUrls) {
				Iterator<ActionDataMining> i = actions.iterator();
				while (i.hasNext()) {
					ActionDataMining action = i.next();
					if (action.getsUrl().contains(url))
						i.remove();
				}				
			}
		}
		System.out.println("Action mining took " + actionsTimer.stop());
		
		Stopwatch smellsTimer = Stopwatch.createStarted();
		if (ArrayUtils.contains(selectedSmells, LABORIOUS_TASK)) {
			int maxActionCount = UsabilitySmellDetector.DEFAULT_VALUE, minSessionCount = UsabilitySmellDetector.DEFAULT_VALUE;
			long maxTime = UsabilitySmellDetector.DEFAULT_VALUE;
			for (ParameterSmellTestDataMining parameter : smellsParametersValues) {
				if (parameter.getParameterSmell().getId() == MAX_ACTION_COUNT)
					maxActionCount =  (int)(double)parameter.getValue();
				if (parameter.getParameterSmell().getId() == MAX_TIME)
					maxTime =  (long)(double)parameter.getValue();
				if (parameter.getParameterSmell().getId() == MIN_SESSION_COUNT)
					minSessionCount =  (int)(double)parameter.getValue();
			}
			if (debug) System.out.println("Detecting Laborious Task...");
			List<TaskSmellAnalysisResult> result = usm.detectLaboriousTask(tasks, maxActionCount, maxTime, minSessionCount);
			if (result.size() > 0)
				tasksAnalysisResult.put("Laborious Task", result);
		}
		if (ArrayUtils.contains(selectedSmells, CYCLIC_TASK)) {
			double maxCycleRate = UsabilitySmellDetector.DEFAULT_VALUE;
			int minActionCount = UsabilitySmellDetector.DEFAULT_VALUE;
			for (ParameterSmellTestDataMining parameter : smellsParametersValues) {
				if (parameter.getParameterSmell().getId() == MAX_CYCLE_RATE)
					maxCycleRate =  (double)parameter.getValue();
				if (parameter.getParameterSmell().getId() == MIN_ACTION_COUNT)
					minActionCount =  (int)(double)parameter.getValue();
			}
			if (debug) System.out.println("Detecting Cyclic Task...");
			List<TaskSmellAnalysisResult> result = usm.detectCyclicTask(tasks, maxCycleRate, minActionCount);
			if (result.size() > 0)
				tasksAnalysisResult.put("Cyclic Task", result);
		}
		if (ArrayUtils.contains(selectedSmells, LONELY_ACTION)) {
			double maxOccurrenceRate = UsabilitySmellDetector.DEFAULT_VALUE;
			int minOccurrenceCount = UsabilitySmellDetector.DEFAULT_VALUE;
			for (ParameterSmellTestDataMining parameter : smellsParametersValues) {
				if (parameter.getParameterSmell().getId() == MAX_OCCURRENCE_RATE)
					maxOccurrenceRate =  parameter.getValue();
				if (parameter.getParameterSmell().getId() == MIN_OCCURRENCE_COUNT_LA)
					minOccurrenceCount =  (int)(double)parameter.getValue();
			}
			if (debug) System.out.println("Detecting Lonely Action...");
			Map<String, Map<String, String>> result = usm.detectLonelyAction(actions, maxOccurrenceRate, minOccurrenceCount);
			if (result.size() > 0)
				actionsAnalysisResult.put("Lonely Action", result);
		}
		if (ArrayUtils.contains(selectedSmells, TOO_MANY_LAYERS)) {
			int maxLayerCount = UsabilitySmellDetector.DEFAULT_VALUE;
			for (ParameterSmellTestDataMining parameter : smellsParametersValues) {
				if (parameter.getParameterSmell().getId() == MAX_LAYER_COUNT)
					maxLayerCount =  (int)(double)parameter.getValue();
			}
			if (debug) System.out.println("Detecting Too Many Layers...");
			List<TaskSmellAnalysisResult> result = usm.detectTooManyLayers(tasks, maxLayerCount);
			if (result.size() > 0)
				tasksAnalysisResult.put("Too Many Layers", result);
		}
		if (ArrayUtils.contains(selectedSmells, UNDESCRIPTIVE_ELEMENT)) {
			int maxAttemptCount = UsabilitySmellDetector.DEFAULT_VALUE, maxTimeInterval = UsabilitySmellDetector.DEFAULT_VALUE;
			for (ParameterSmellTestDataMining parameter : smellsParametersValues) {
				if (parameter.getParameterSmell().getId() == MAX_ATTEMPT_COUNT);
					maxAttemptCount =  (int)(double)parameter.getValue();
				if (parameter.getParameterSmell().getId() == MAX_TIME_INTERVAL);
					maxTimeInterval =  (int)(double)parameter.getValue();
			}
			if (debug) System.out.println("Detecting Undescriptive Element...");
			Map<String, Map<String, String>> result = usm.detectUndescriptiveElement(actions, maxAttemptCount, maxTimeInterval);
			if (result.size() > 0)
				actionsAnalysisResult.put("Undescriptive Element", result);
		}
		if (ArrayUtils.contains(selectedSmells, MISSING_FEEDBACK)) {
			int maxRepetitionCount = UsabilitySmellDetector.DEFAULT_VALUE, minOccurrenceCount = UsabilitySmellDetector.DEFAULT_VALUE;
			for (ParameterSmellTestDataMining parameter : smellsParametersValues) {
				if (parameter.getParameterSmell().getId() == MAX_REPETITION_COUNT)
					maxRepetitionCount =  (int)(double)parameter.getValue();
				if (parameter.getParameterSmell().getId() == MIN_OCCURRENCE_COUNT_MF)
					minOccurrenceCount =  (int)(double)parameter.getValue();
			}
			if (debug) System.out.println("Detecting Missing Feedback...");
			Map<String, Map<String, String>> result = usm.detectMissingFeedback(actions, maxRepetitionCount, minOccurrenceCount);
			if (result.size() > 0)
				actionsAnalysisResult.put("Missing Feedback", result);
		}
		if (ArrayUtils.contains(selectedSmells, FAT_INTERFACE)) {
			double maxActionCount = UsabilitySmellDetector.DEFAULT_VALUE;
			for (ParameterSmellTestDataMining parameter : smellsParametersValues) {
				if (parameter.getParameterSmell().getId() == MAX_DIFF_ACTION_COUNT)
					maxActionCount =  parameter.getValue();
			}
			if (debug) System.out.println("Detecting Fat Interface...");
			Map<String, Map<String, String>> result = usm.detectFatInterface(actions, maxActionCount);
			if (result.size() > 0)
				actionsAnalysisResult.put("Fat Interface", result);
		}
		System.out.println("Smells analysis took " + smellsTimer.stop());
		
		String json;		
		if (tasksAnalysisResult.size() == 0 && actionsAnalysisResult.size() == 0)
			json = gson.toJson(new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.smells.testes.detection.appok"));
		else {
			if (groupSessions) {
				if (debug) System.out.println("Grouping sessions...");
				Map<String, List<TaskSmellAnalysisGroupedResult>> taskAnalysisGroupedResult = new LinkedHashMap<String, List<TaskSmellAnalysisGroupedResult>>();
				for (Map.Entry<String, List<TaskSmellAnalysisResult>> entry : tasksAnalysisResult.entrySet()) {
					taskAnalysisGroupedResult.put(entry.getKey(), usm.sessionsGroupedBySimilarity(entry.getValue(), similarityRate));
				}
				json = gson.toJson(new SmellAnalysisGroupedResultVO(taskAnalysisGroupedResult, actionsAnalysisResult));
			} else
				json = gson.toJson(new SmellAnalysisResultVO(tasksAnalysisResult, actionsAnalysisResult));
		}
		
		result.use(Results.json()).from(json).serialize();
	}
	
	@Get("/testes/{idTeste}/smells/{idSmell}/parameters")
	@Logado
	public void parameters(Long idTeste, Long idSmell) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TaskDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		List<ParameterSmellTestDataMining> listParametersSmellValues = ParameterSmellUtils.findParametersSmellValues(idTeste,
				parameterSmellDataMiningRepository,
				parameterSmellTestDataMiningRepository);
		
		List<ParameterSmellTestDataMining> listParametersValuesFromSmell = new ArrayList<ParameterSmellTestDataMining>();
		for (ParameterSmellTestDataMining p : listParametersSmellValues) {
			if (p.getParameterSmell().getIdSmell().equals(idSmell)) {
				listParametersValuesFromSmell.add(p);
			}
		}
		
		result.use(Results.json()).from(gson.toJson(listParametersValuesFromSmell)).serialize();
	}
	
	@Post("/testes/smells/parameters/change")
	@Consumes("application/json")
	@Logado
	public void changeParameter(ParameterSmellTestDataMining[] parameters) {
		Gson gson = new GsonBuilder()
	        .serializeNulls()
	        .create();
		
		ReturnVO returnvo;
		if (parameters != null) {
			for (ParameterSmellTestDataMining parameter : parameters) {
				parameterSmellTestDataMiningRepository.update(parameter);
			}
			returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.smells.testes.config.saveok");
		} else {
			returnvo = new ReturnVO(ReturnStatusEnum.ERRO, "datamining.smells.error.default");
		}
		
		result.use(Results.json()).from(gson.toJson(returnvo)).serialize();
	}
	
	@Post("/testes/smells/detection/ignoredurls/update")
	@Consumes("application/json")
	@Logado
	public void updateIgnoredUrls(IgnoredUrlDataMining url, Boolean remove) {
		Gson gson = new GsonBuilder()
	        .serializeNulls()
	        .create();
		
		ReturnVO returnvo;
		if (!remove) {
			ignoredUrlRepository.create(url);
			returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.smells.testes.detection.saveok");
		} else {
			ignoredUrlRepository.destroy(url);
			returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.smells.testes.detection.removeok");
		}
		
		result.use(Results.json()).from(gson.toJson(returnvo)).serialize();
	}
	
	@Post("/testes/smells/detection/ignoredurls/get")
	@Consumes("application/json")
	@Logado
	public void ignoredUrls(Long testId) {
		Gson gson = new GsonBuilder()
	        .serializeNulls()
	        .create();
		
		List<IgnoredUrlDataMining> ignoredUrls = ignoredUrlRepository.getIgnoredUrls(testId);
		if (ignoredUrls != null)
			result.use(Results.json()).from(gson.toJson(ignoredUrls)).serialize();
		else
			result.use(Results.json()).from(gson.toJson(new ArrayList<IgnoredUrlDataMining>())).serialize();
	}
	
		
}
