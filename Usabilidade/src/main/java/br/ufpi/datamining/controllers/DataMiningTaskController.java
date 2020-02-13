package br.ufpi.datamining.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import br.com.caelum.vraptor.Consumes;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.core.Localization;
import br.com.caelum.vraptor.view.Results;
import br.ufpi.annotation.Logado;
import br.ufpi.componets.TesteView;
import br.ufpi.componets.UsuarioLogado;
import br.ufpi.componets.ValidateComponente;
import br.ufpi.controllers.BaseController;
import br.ufpi.datamining.analisys.FrequentSequentialPatternMining;
import br.ufpi.datamining.analisys.WebUsageMining;
import br.ufpi.datamining.analisys.kmeans.KMeansUtils;
import br.ufpi.datamining.analisys.kmeans.ResultKMeans;
import br.ufpi.datamining.models.ActionSingleDataMining;
import br.ufpi.datamining.models.EvaluationTaskDataMining;
import br.ufpi.datamining.models.EvaluationTestDataMining;
import br.ufpi.datamining.models.FieldSearchTupleDataMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.TestDataMining;
import br.ufpi.datamining.models.auxiliar.ActionSituationAux;
import br.ufpi.datamining.models.auxiliar.ComparePatternsAux;
import br.ufpi.datamining.models.auxiliar.FSPMResultAux;
import br.ufpi.datamining.models.auxiliar.ResultComparePatterns;
import br.ufpi.datamining.models.auxiliar.ResultDataMining;
import br.ufpi.datamining.models.auxiliar.ResultEvaluationDataMining;
import br.ufpi.datamining.models.auxiliar.SessionResultDataMining;
import br.ufpi.datamining.models.auxiliar.UserResultDataMining;
import br.ufpi.datamining.models.enums.MomentTypeActionDataMiningEnum;
import br.ufpi.datamining.models.enums.ReturnStatusEnum;
import br.ufpi.datamining.models.enums.SessionClassificationDataMiningFilterEnum;
import br.ufpi.datamining.models.vo.EvaluationTaskDataMiningVO;
import br.ufpi.datamining.models.vo.FrequentSequentialPatternResultVO;
import br.ufpi.datamining.models.vo.ReturnVO;
import br.ufpi.datamining.models.vo.TaskDataMiningVO;
import br.ufpi.datamining.repositories.ActionDataMiningRepository;
import br.ufpi.datamining.repositories.ActionSingleDataMiningRepository;
import br.ufpi.datamining.repositories.EvaluationTaskDataMiningRepository;
import br.ufpi.datamining.repositories.EvaluationTestDataMiningRepository;
import br.ufpi.datamining.repositories.FieldSearchTupleDataMiningRepository;
import br.ufpi.datamining.repositories.TaskDataMiningRepository;
import br.ufpi.datamining.repositories.TestDataMiningRepository;
import br.ufpi.datamining.utils.ConverterUtils;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.SequentialPattern;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Path(value = "datamining")
@Resource
public class DataMiningTaskController extends BaseController {
	
	private final TestDataMiningRepository testeDataMiningRepository;
	private final TaskDataMiningRepository taskDataMiningRepository;
	private final ActionDataMiningRepository actionDataMiningRepository;
	private final ActionSingleDataMiningRepository actionSingleDataMiningRepository;
	private final EvaluationTaskDataMiningRepository evaluationTaskDataMiningRepository;
	private final EvaluationTestDataMiningRepository evaluationTestDataMiningRepository;
	private final FieldSearchTupleDataMiningRepository fieldSearchTupleDataMiningRepository;
	
	private final Long TIMEOUT_SECONDS = 90l;
	
	private final Localization localization;
	
	public DataMiningTaskController(Result result, Validator validator,
			TesteView testeView, UsuarioLogado usuarioLogado,
			ValidateComponente validateComponente,
			Localization localization,
			TestDataMiningRepository testeDataMiningRepository,
			TaskDataMiningRepository taskDataMiningRepository,
			ActionDataMiningRepository actionDataMiningRepository,
			EvaluationTaskDataMiningRepository evaluationTaskDataMiningRepository,
			EvaluationTestDataMiningRepository evaluationTestDataMiningRepository,
			ActionSingleDataMiningRepository actionSingleDataMiningRepository,
			FieldSearchTupleDataMiningRepository fieldSearchTupleDataMiningRepository) {
		super(result, validator, testeView, usuarioLogado, validateComponente);
		this.testeDataMiningRepository = testeDataMiningRepository;
		this.taskDataMiningRepository = taskDataMiningRepository;
		this.actionDataMiningRepository = actionDataMiningRepository;
		this.evaluationTaskDataMiningRepository = evaluationTaskDataMiningRepository;
		this.evaluationTestDataMiningRepository = evaluationTestDataMiningRepository;
		this.actionSingleDataMiningRepository = actionSingleDataMiningRepository;
		this.fieldSearchTupleDataMiningRepository = fieldSearchTupleDataMiningRepository;
		this.localization = localization;
	}
	
	@Get("/testes/{idTeste}/tarefas/{idTarefa}")
	@Logado
	public void view(Long idTeste, Long idTarefa) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TaskDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		TaskDataMining task = taskDataMiningRepository.find(idTarefa);
		
		validateComponente.validarNotNull(task, localization.getMessage("datamining.notfound", "datamining.task"));
		validateComponente.validarEquals(idTeste, task.getTestDataMining().getId(), "datamining.tasks.error.dontbelongs");
		
		TaskDataMiningVO taskDataMiningVO = new TaskDataMiningVO(task);
		result.use(Results.json()).from(gson.toJson(taskDataMiningVO)).serialize();
	}
	
	@Post("/testes/tarefas/salvar")
	@Consumes("application/json")
	@Logado
	public void salvar(TaskDataMining task) {
		Gson gson = new Gson();
		validateComponente.validarString(task.getTitle(), "datamining.tasks.title");
		
		ReturnVO returnvo; 
		
		if(!validator.hasErrors()){
			if(task.getId() != null){
				TaskDataMining taskUpdate = taskDataMiningRepository.find(task.getId());
				taskUpdate.setTitle(task.getTitle());
				taskUpdate.setThreshold(task.getThreshold());
				taskUpdate.setDisregardActions(task.getDisregardActions());
				taskUpdate.setActionsRequiredOrder(task.getActionsRequiredOrder());
				
				taskDataMiningRepository.update(taskUpdate);
				returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.tasks.edit.success");
			}else{
				
				TestDataMining testDataMining = testeDataMiningRepository.find(task.getTestDataMining().getId());
				task.setTestDataMining(testDataMining);
				testDataMining.getTasks().add(task);
				
				taskDataMiningRepository.create(task);
				testeDataMiningRepository.update(testDataMining);
				returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.tasks.new.success");
			}
			result.use(Results.json()).from(gson.toJson(returnvo)).serialize();
		}else{
			returnvo = new ReturnVO(ReturnStatusEnum.ERRO, "erro");
			returnvo.setErrorsMessage(validator.getErrors());
			
			validator.onErrorUse(Results.json()).from(gson.toJson(returnvo)).serialize();
		}
	}
	
	@Post("/testes/tarefas/compararpadroes")
	@Consumes("application/json")
	public void compararPadroes(ComparePatternsAux padroes) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TaskDataMiningVO.exclusionStrategy)
	        .serializeSpecialFloatingPointValues()
	        .create();
		
		try {
			FSPMResultAux resultFSPMBest = minerarFSPM(padroes.getPatternsBest(), padroes.getMinSupBest(), padroes.getMinItensBest());
			FSPMResultAux resultFSPMOthers = minerarFSPM(padroes.getPatternsOthers(), padroes.getMinSupOthers(), padroes.getMinItensOthers());
			
			ResultComparePatterns resultCompare = new ResultComparePatterns(resultFSPMBest, resultFSPMOthers);
			result.use(Results.json()).from(gson.toJson(resultCompare)).serialize();
		} catch (IOException e) {
			e.printStackTrace();
			ReturnVO returnVO = new ReturnVO(ReturnStatusEnum.ERRO, "erro");
			validator.onErrorUse(Results.json()).from(gson.toJson(returnVO)).serialize();
		}
	}
	
	private FSPMResultAux minerarFSPM(String pattern, Double minSup, Integer minItens) throws IOException {
		FrequentSequentialPatternMining fspm = new FrequentSequentialPatternMining();
		List<FrequentSequentialPatternResultVO> frequentPatternReturn = new ArrayList<FrequentSequentialPatternResultVO>();
		
		double lastMinSup = minSup != null ? minSup : 0d;
		int defaultMinItens = minItens != null ? minItens : 5;
		
		
		boolean isUniqueSession = false;
		
		if (pattern != null) {
			int diffChars = pattern.length() - pattern.replace("-2", "").length();
			if (diffChars <= 2) {
				isUniqueSession = true;
			}
			
			if (isUniqueSession) {
				SequenceDatabase sequenceDatabase = new SequenceDatabase();
				sequenceDatabase.loadFromString(pattern, "\n");
				
				List<List<Integer>> list = sequenceDatabase.getSequences().get(0).getItemsets();
				Itemset itemSet = new Itemset(0);
				for (List<Integer> li : list) {
					for (Integer i : li) {
						itemSet.addItem(i);
					}
				}
				
				SequentialPattern seqPattern = new SequentialPattern(itemSet, sequenceDatabase.getSequenceIDs());
				frequentPatternReturn.add(new FrequentSequentialPatternResultVO(seqPattern));
			} else {
				if (minSup != null) {
					frequentPatternReturn = fspm.analyze(pattern, lastMinSup, null, defaultMinItens, TIMEOUT_SECONDS);
				} 
				if (minSup == null || frequentPatternReturn.size() == 0) {
					//automatic patterns: (100/80/60/40/20)
					double[] minSups = new double[]{1.0, 0.8, 0.6, 0.4};
					for (int i = 0; i < minSups.length; i++) {
						if (frequentPatternReturn == null || frequentPatternReturn.size() == 0) {
							lastMinSup = minSups[i];
							System.out.println("-- INICIAR SESSÃO FSPM: " + lastMinSup);
							frequentPatternReturn = fspm.analyze(pattern, minSups[i], null, defaultMinItens, TIMEOUT_SECONDS);
						}
					}
				}
			}
		} 
		
		return new FSPMResultAux(lastMinSup, defaultMinItens, isUniqueSession, frequentPatternReturn);
	}
	
	
	@Get("/testes/{idTeste}/avaliacao/{idEvaluationTest}/tarefas/{idTarefa}/clusterizar/numclusters/{numClusters}/threshold/{thresholdClusters}")
	@Logado
	public void clusterizar(Long idTeste, Long idEvaluationTest, Long idTarefa, int numClusters, double thresholdClusters) {
		Gson gson = new GsonBuilder()
	        .serializeSpecialFloatingPointValues()
	        .create();
		
		try {
			//get sessions to clustering
			EvaluationTestDataMining evaluationTest = evaluationTestDataMiningRepository.find(idEvaluationTest);
			ResultDataMining resultDataMining = WebUsageMining.analyze(idTarefa, evaluationTest.getInitDate().getTime(), evaluationTest.getLastDate().getTime(), SessionClassificationDataMiningFilterEnum.ALL, taskDataMiningRepository, actionDataMiningRepository);
			
			List<SessionResultDataMining> sessions = resultDataMining.getSessions();
			ResultKMeans resultKMeans = KMeansUtils.kmeansFindBestUsers(KMeansUtils.convertSessionsToPoints(sessions), numClusters, thresholdClusters);
			result.use(Results.json()).from(gson.toJson(resultKMeans)).serialize();
			
		} catch (Exception e) {
			e.printStackTrace();
			ReturnVO returnVO = new ReturnVO(ReturnStatusEnum.ERRO, "erro");
			validator.onErrorUse(Results.json()).from(gson.toJson(returnVO)).serialize();
		}
	}
	
	@Get("/testes/{idTeste}/avaliacao/{idEvaluationTest}/tarefas/{idTarefa}/avaliar/sessoes/{sessionFilter}/minsup/{minSup}/minitens/{minItens}")
	@Logado
	public void avaliarParams(Long idTeste, Long idEvaluationTest, Long idTarefa, Integer sessionFilter, Double minSup, Integer minItens) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TaskDataMiningVO.exclusionStrategy)
	        .serializeSpecialFloatingPointValues()
	        .create();
		
		ResultEvaluationDataMining resultEval = null;
		try {
			minSup = minSup > 0 ? minSup / 100 : 0;
			resultEval = this.avaliarTarefa(idTeste, idEvaluationTest, idTarefa, minSup, minItens, sessionFilter);
		} catch (Exception e) {
			e.printStackTrace();
			ReturnVO returnVO = new ReturnVO(ReturnStatusEnum.ERRO, "erro");
			validator.onErrorUse(Results.json()).from(gson.toJson(returnVO)).serialize();
		}
		result.use(Results.json()).from(gson.toJson(resultEval)).serialize();
	}
	
	@Get("/testes/{idTeste}/avaliacao/{idEvaluationTest}/tarefas/{idTarefa}/avaliar")
	@Logado
	public void avaliar(Long idTeste, Long idEvaluationTest, Long idTarefa) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TaskDataMiningVO.exclusionStrategy)
	        .serializeSpecialFloatingPointValues()
	        .create();
		
		ResultEvaluationDataMining resultEval = null;
		try {
			resultEval = this.avaliarTarefa(idTeste, idEvaluationTest, idTarefa, null, null, SessionClassificationDataMiningFilterEnum.ALL.getValue());
		} catch (Exception e) {
			e.printStackTrace();
			ReturnVO returnVO = new ReturnVO(ReturnStatusEnum.ERRO, "erro");
			validator.onErrorUse(Results.json()).from(gson.toJson(returnVO)).serialize();
		}
		result.use(Results.json()).from(gson.toJson(resultEval)).serialize();
	}
		
	private ResultEvaluationDataMining avaliarTarefa(Long idTeste, Long idEvaluationTest, Long idTarefa, Double minSup, Integer minItens, Integer classificationFilterValue) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
		Long init = new Date().getTime();
		ResultEvaluationDataMining result = new ResultEvaluationDataMining();
		
		System.out.println("-#-#-#-#-#-#-#-#-#-#-#-# INICIO DA AVALIACAO #-#-#-#-#-#-#-#-#-#-#-#-");
		System.out.println("-#-# Tempo Inicial: "+init);
		
		//FSPM
		SessionClassificationDataMiningFilterEnum classificationFilter = SessionClassificationDataMiningFilterEnum.getByValue(classificationFilterValue);
		if (classificationFilter == null) {
			classificationFilter = SessionClassificationDataMiningFilterEnum.ALL;
		}
		
		
		//persist results
		TaskDataMining taskDataMining = taskDataMiningRepository.find(idTarefa);
		EvaluationTestDataMining evaluationTest = evaluationTestDataMiningRepository.find(idEvaluationTest);
		
		ResultDataMining resultDataMining = WebUsageMining.analyze(idTarefa, evaluationTest.getInitDate().getTime(), evaluationTest.getLastDate().getTime(), classificationFilter, taskDataMiningRepository, actionDataMiningRepository);
		EvaluationTaskDataMining evaluation = taskDataMining.getEvaluationFromEvalTest(idEvaluationTest);
		boolean newEvaluation = evaluation == null ? true : false;
		
		System.out.println("-#-# Finalizou WebUsageMining - analise inicial: " + new Date().getTime() );
		
		if (evaluation == null) {
			evaluation = new EvaluationTaskDataMining();
			evaluation.setEvaluationTest(evaluationTest);
			evaluation.setTaskDataMining(taskDataMining);
		}
		
		evaluation.setEvalLastDate(new Date());
		evaluation.setEvalCountSessions(resultDataMining.getCountSessions());
		
		
		
		if (evaluation.getEvalCountSessions() > 0) {
			evaluation.setEvalMeanActions(resultDataMining.getActionsAverageOk());
			evaluation.setEvalMeanTimes(resultDataMining.getTimesAverageOk());
			evaluation.setEvalMeanCompletion(resultDataMining.getRateSuccess());
			evaluation.setEvalMeanCorrectness(resultDataMining.getRateRequired());
			
			double userEffectiveness = 0d, userEfficiency = 0d;
			for (UserResultDataMining u : resultDataMining.getUsers()) {
				userEffectiveness += u.getEffectiveness();
				userEfficiency += u.getEfficiency();
			}
			
			evaluation.setEvalEffectiveness(ConverterUtils.notNaN(userEffectiveness / resultDataMining.getUsers().size()));
			evaluation.setEvalEfficiency(ConverterUtils.notNaN(userEfficiency / resultDataMining.getUsers().size()));
			
			//evaluation.setEvalEffectiveness(ConverterUtils.notNaN((evaluation.getEvalMeanCompletion() * evaluation.getEvalMeanCorrectness()) / 100));
			//evaluation.setEvalEfficiency(ConverterUtils.notNaN(evaluation.getEvalEffectiveness() / (evaluation.getEvalZScoreActions() * evaluation.getEvalZScoreTime())));
			
			evaluation.setEvalZScoreActions(ConverterUtils.notNaN((resultDataMining.getMaxActionsOk() - resultDataMining.getMeanActionsOk()) / resultDataMining.getStdDevActionsOk()));
			evaluation.setEvalZScoreTime(ConverterUtils.notNaN((resultDataMining.getMaxTimesOk() - resultDataMining.getMeanTimesOk()) / resultDataMining.getStdDevTimesOk()));
			
			//System.out.println(evaluation.getEvalEffectiveness() / (evaluation.getEvalZScoreTime()));
			//System.out.println(evaluation.getEvalEffectiveness() / (evaluation.getEvalZScoreActions() * evaluation.getEvalZScoreTime()));
			//System.out.println(evaluation.getEvalEffectiveness() / ((evaluation.getEvalZScoreActions() + evaluation.getEvalZScoreTime()) / 2));
		} else {
			evaluation = EvaluationTaskDataMiningVO.zeroEvaluation(evaluation);
		}
		
		
    	
    	System.out.println("-#-# Inicio do FrequentSequentialPatternMining: " + new Date().getTime() );
		FrequentSequentialPatternMining fspm = new FrequentSequentialPatternMining();
		List<FrequentSequentialPatternResultVO> frequentPatterns = null;
		
		double lastMinSup = minSup != null ? minSup : 0d;
		int defaultMinItens = minItens != null ? minItens : 5;
		Map<String, String> usersSequences = resultDataMining.getUsersSequences();
		
		
		if (evaluation.getEvalCountSessions() > 1) {
			if (minSup != null) {
				frequentPatterns = fspm.analyze(usersSequences, lastMinSup, null, defaultMinItens, TIMEOUT_SECONDS);
			} 
			if (minSup == null || frequentPatterns.size() == 0) {
				//automatic patterns: (100/80/60/40/20)
				double[] minSups = new double[]{1.0, 0.75, 0.5, 0.25};
				for (int i = 0; i < minSups.length; i++) {
					if (frequentPatterns == null || frequentPatterns.size() == 0) {
						lastMinSup = minSups[i];
						//if (!(resultDataMining.getUsersSequences().size() < 2 && lastMinSup == 1.0)) {
						
						System.out.println("-- INICIAR SESSÃO FSPM ->  minsup: " + lastMinSup + " , minItens: " + defaultMinItens + " (inicio: " + new Date().getTime() + ")");
						System.out.println("Ações distintas: " + resultDataMining.getPageViewActionCount().keySet().size());
						frequentPatterns = fspm.analyze(usersSequences, minSups[i], null, defaultMinItens, TIMEOUT_SECONDS);
						System.out.println("-- FIM SESSÃO FSPM ->  minsup: " + lastMinSup + " , minItens: " + defaultMinItens + " (fim: " + new Date().getTime() + ")");
						
						//}
					}
				}
			}
		}
		resultDataMining.setLastMinSup(lastMinSup);
		resultDataMining.setLastMinItens(defaultMinItens);
		
		System.out.println("-#-# Fim do FrequentSequentialPatternMining: " + new Date().getTime() );
		
		Long diffTime = new Date().getTime() - init;
		if (evaluation.getMeanTimeLoading() != null) {
			evaluation.setMeanTimeLoading((evaluation.getMeanTimeLoading() + diffTime) / 2);
		} else {
			evaluation.setMeanTimeLoading(diffTime);
		}
		
		
		if (newEvaluation) {
			evaluationTaskDataMiningRepository.create(evaluation);
			
			taskDataMining.getEvaluations().add(evaluation);
			taskDataMiningRepository.update(taskDataMining);
			
			evaluationTest.getEvaluationsTask().add(evaluation);
			evaluationTestDataMiningRepository.update(evaluationTest);
			
			System.out.println("Nova avaliação cadastrada!");
		} else {
			evaluationTaskDataMiningRepository.update(evaluation);
			System.out.println("Avaliação atualizada!");
		}
		
		//sessions
		//users
		
		resultDataMining.setSessions(resultDataMining.getSessions());
		EvaluationTestDataMining evaluationTestDataMining = evaluationTestDataMiningRepository.find(idEvaluationTest);
		
		result.setFrequentPatterns(frequentPatterns);
		result.setEvalTask(evaluation);
		result.setResult(resultDataMining);
		result.setTask(new TaskDataMiningVO(taskDataMining));
		result.setEvalTestDateEnd(evaluationTestDataMining.getLastDate());
		result.setEvalTestDateInit(evaluationTestDataMining.getInitDate());
			
		return result;
	}
	
	@Post("/testes/avaliacao/tarefas/saveActionSituation")
	@Consumes("application/json")
	@Logado
	public void saveActionSituation(Long testId, Long evalTestId, Long taskId, String actionsStr) {
		TaskDataMining taskDataMining = taskDataMiningRepository.find(taskId);
		
		Gson gson = new Gson();
		List<ActionSituationAux> actions = gson.fromJson(actionsStr, new TypeToken<ArrayList<ActionSituationAux>>() {}.getType());

		List<ActionSingleDataMining> actionsRequired = taskDataMining.getActionsRequired();
		
		//buscar por outras ações obrigatórias para editar as existentes
		for (ActionSituationAux as : actions) {
			System.out.println("-----------");
			System.out.println(as.getAction());
			System.out.println(as.getElement());
			System.out.println(as.getLocation());
			System.out.println(as.getSituation());
			System.out.println(as.getType());
			
			ActionSingleDataMining asdm = as.toActionSingleDataMining(taskDataMining);
			
			//verificar se há alguma ação obrigatória já cadastrada
			for (ActionSingleDataMining asdm2 : actionsRequired) {
				if (asdm2.getActionType().equals(asdm.getActionType())) {
					int countSame = 0, 
						expected1 = asdm.getElementFiedlSearch().size(),
						expected2 = asdm.getUrlFieldSearch().size();
					
					for (FieldSearchTupleDataMining f : asdm.getElementFiedlSearch()) {
						for (FieldSearchTupleDataMining f2 : asdm2.getElementFiedlSearch()) {
							if (f.getField().equals(f2.getField()) && 
								f.getValue().equals(f2.getValue())) {
								countSame++;
							}
						}
					}
					
					if (countSame == expected1) {
						countSame = 0;
						for (FieldSearchTupleDataMining f : asdm.getUrlFieldSearch()) {
							for (FieldSearchTupleDataMining f2 : asdm2.getUrlFieldSearch()) {
								if (f.getField().equals(f2.getField()) && 
									f.getValue().equals(f2.getValue())) {
									countSame++;
								}
							}
						}
						if (countSame == expected2) {
							//são iguais
							asdm.setId(asdm2.getId());
							
							//remover do grupo anterior
							if(asdm2.getMomentType().equals(MomentTypeActionDataMiningEnum.START)){
								taskDataMining.getActionsInitial().remove(asdm2);
							}else if(asdm2.getMomentType().equals(MomentTypeActionDataMiningEnum.END)){
								taskDataMining.getActionsEnd().remove(asdm2);
							}else if(asdm2.getMomentType().equals(MomentTypeActionDataMiningEnum.REQUIRED)){
								taskDataMining.getActionsRequired().remove(asdm2);
							}
							
							break;
						}
					}
				}
			}
			
			
			asdm.setTask(taskDataMining);
			
			//setar actions no set correto
			boolean newMoment = false;
			if (asdm.getMomentType() != null) {
				if(asdm.getMomentType().equals(MomentTypeActionDataMiningEnum.START)){
					taskDataMining.getActionsInitial().add(asdm);
					newMoment = true;
				}else if(asdm.getMomentType().equals(MomentTypeActionDataMiningEnum.END)){
					taskDataMining.getActionsEnd().add(asdm);
					newMoment = true;
				}else if(asdm.getMomentType().equals(MomentTypeActionDataMiningEnum.REQUIRED)){
					taskDataMining.getActionsRequired().add(asdm);
					newMoment = true;
				}
			}
			
			if (newMoment) {
				//persistir fieldsearch
				for(FieldSearchTupleDataMining f : asdm.getElementFiedlSearch()){
					fieldSearchTupleDataMiningRepository.create(f);
				}
				for(FieldSearchTupleDataMining f : asdm.getUrlFieldSearch()){
					fieldSearchTupleDataMiningRepository.create(f);
				}
				
				//novo
				if (asdm.getId() == null) {
					actionSingleDataMiningRepository.create(asdm);
				} else {
					//editar
					actionSingleDataMiningRepository.update(asdm);
				}
			} else {
				//TODO: remover o antigo fieldsearch para evitar lixo no banco
			}
			
			taskDataMiningRepository.update(taskDataMining);
		}
		
		
		ReturnVO returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.tasks.actions.edits.success");
		
		result.use(Results.json()).from(gson.toJson(returnvo)).serialize();
		
	}
	
	/*
	actions: "{"3":{"location":"confirmarGuiaDeExamePrestador-buscarSegurados","element":"id(\"content\")/div[@class=\"panel\"]/div[@class=\"form-wrapper\"]/form[1]","action":"form_submit","id":"3","situation":"error"},"6":{"location":"confirmarGuiaDeExamePrestador-selecionarSegurado","element":"id(\"content\")/div[@class=\"panel\"]/div[@class=\"form-wrapper\"]/form[1]/span[1]/button[@class=\"actionButton\"]","action":"click","id":"6","type":"default"},"7":{"location":"confirmarGuiaDeExamePrestador-selecionarSegurado","element":"id(\"content\")/div[@class=\"panel\"]/div[@class=\"form-wrapper\"]/form[1]","action":"form_submit","id":"7","type":"required","situation":"ok"},"8":{"location":"confirmarGuiaDeExamePrestador-selecionarGuia","element":"null","action":"onload","id":"8","type":"end"}}"
	evalTestId: 10
	taskId: 14
	testId: 1
	*/
	
	/*
	 
	 $scope.specialActionsEnum = {
	    	INITIAL: {
	    		id: 'initial',
	    		desc: 'Inicial'
	    	},
	    	END: {
	    		id: 'end',
	    		desc: 'Final'
	    	},
	    	REQUIRED: {
	    		id: 'required',
	    		desc: 'Obrigatória'
	    	},
	    	DEFAULT: {
	    		id: 'default',
	    		desc: 'Ação Normal'
	    	}
	    }
	 
	 */
	
	
	/*
	@Get("/testes/{idTeste}/tarefas/criar")
	@Logado
	public void create() {
		result.include("title", localization.getMessage("datamining.tasks.new"));
	}
	
	@Get("/testes/{idTeste}/tarefas/editar/{idTeste}")
	@Logado
	public void edit(Long idTeste) {
		TestDataMining testPertencente = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), idTeste);
		validateComponente.validarNotNull(testPertencente, "datamining.accessdenied");
		validator.onErrorRedirectTo(this).list();
		result.include("test", testPertencente);
		result.include("edit", true);
		
		result.include("title", localization.getMessage("datamining.testes.edit"));
		result.of(this).create();
	}
	
	@Post("/testes/{idTeste}/tarefas/salvar")
	@Logado
	public void salvar(TestDataMining test) {
		validateComponente.validarString(test.getTitle(), "datamining.testes.title");
		validateComponente.validarString(test.getClientAbbreviation(), "datamining.testes.abbrev");
		validateComponente.validarURL(test.getUrlSystem());
		
		if(test.getId() != null){
			validator.onErrorRedirectTo(this).edit(test.getId());
			
			TestDataMining testUpdate = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), test.getId());
			testUpdate.setTitle(test.getTitle());
			testUpdate.setClientAbbreviation(test.getClientAbbreviation());
			testUpdate.setUrlSystem(test.getUrlSystem());
			
			testeDataMiningRepository.update(testUpdate);
			result.include("sucesso", localization.getMessage("datamining.testes.edit.success"));
		}else{
			validator.onErrorRedirectTo(this).create();
			
			ArrayList<Usuario> users = new ArrayList<Usuario>();
			users.add(usuarioLogado.getUsuario());
			test.setUsers(users);
			test.setUserCreated(usuarioLogado.getUsuario());
			
			testeDataMiningRepository.create(test);
			result.include("sucesso", localization.getMessage("datamining.testes.new.success"));
		}
		
		result.forwardTo(this).list();
	}
	*/
}
