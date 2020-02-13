package br.ufpi.datamining.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

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
import br.ufpi.datamining.models.SmellMobileDetMining;
import br.ufpi.datamining.models.SmellMobileMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.TestDataMining;
import br.ufpi.datamining.models.auxiliar.ResultDataMining;
import br.ufpi.datamining.models.auxiliar.TaskMobileSmellAnalysisResult;
import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysis;
import br.ufpi.datamining.models.enums.ReturnStatusEnum;
import br.ufpi.datamining.models.enums.SessionClassificationDataMiningFilterEnum;
import br.ufpi.datamining.models.vo.ReturnVO;
import br.ufpi.datamining.models.vo.SmellMobileAnalysisResultVO;
import br.ufpi.datamining.models.vo.SmellMobileMiningVO;
import br.ufpi.datamining.models.vo.TestDataMiningVO;
import br.ufpi.datamining.repositories.ActionDataMiningRepository;
import br.ufpi.datamining.repositories.IgnoredUrlDataMiningRepository;
import br.ufpi.datamining.repositories.ParameterSmellDataMiningRepository;
import br.ufpi.datamining.repositories.ParameterSmellTestDataMiningRepository;
import br.ufpi.datamining.repositories.SmellMobileDetRepository;
import br.ufpi.datamining.repositories.SmellMobileRepository;
import br.ufpi.datamining.repositories.TaskDataMiningRepository;
import br.ufpi.datamining.utils.EntityDefaultManagerUtil;
import br.ufpi.datamining.utils.EntityManagerUtil;
import br.ufpi.util.MobileUsabilitySmellDetector;

/** 
 * Controller para el mining de WMUS
 * @author María Isabel Limaylla Lunarejo
*/

@Path(value = "datamining")
@Resource
public class DataMiningSmellsMobileController extends BaseController {
	
	private boolean debug = true;
		
	private final SmellMobileRepository smellMobileRepository;
	private final SmellMobileDetRepository smellMobileDetRepository;
	
	public DataMiningSmellsMobileController(Result result, Validator validator,
			TesteView testeView, UsuarioLogado usuarioLogado,
			ValidateComponente validateComponente,
			ParameterSmellDataMiningRepository parameterSmellDataMiningRepository,
			ParameterSmellTestDataMiningRepository parameterSmellTestDataMiningRepository,
			IgnoredUrlDataMiningRepository ignoredUrlRepository,
			SmellMobileRepository smellMobileRepository,
			SmellMobileDetRepository smellMobileDetRepository
			) {
		super(result, validator, testeView, usuarioLogado, validateComponente);
		
		this.smellMobileRepository = smellMobileRepository;
		this.smellMobileDetRepository = smellMobileDetRepository;
	}
	
	/**
	 * Lista los Web Mobile Usability Smells
	 * 
	 * @param	TestDataMining			
	 * @return	Listado los Smells y Patrones
	 */	
	@Get("/testes/{idTeste}/smells/detectionMobile")
	@Logado
	public void listSmellsMobile(TestDataMining test) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		MobileUsabilitySmellDetector usm = new MobileUsabilitySmellDetector();
		List<SmellMobileMining> smellMobs = smellMobileRepository.getSmellMobile();
		List<SmellMobileMiningVO> smellMobsVO = new ArrayList<SmellMobileMiningVO>();
		
		for(SmellMobileMining smellMob : smellMobs){
			List<SmellMobileDetMining> detailSmell = smellMobileDetRepository.getSmellMobileDet(smellMob.getId());
			smellMob.setDetail(detailSmell);	
			//smellMob.setPattern(usm.getPatternOrig(detailSmell));
			SmellMobileMiningVO smellMobVO = new SmellMobileMiningVO(smellMob);
			smellMobVO.setPattern(usm.getPatternOrig(detailSmell));
			smellMobsVO.add(smellMobVO);
		}
		
		String json = gson.toJson(smellMobsVO);
		
		result.use(Results.json()).from(json).serialize();
	}
	
	/**
	 * Lista los Web Mobile Usability Smells
	 * 
	 * @param	String			
	 * @param	String			
	 * @return	Listado los Smells y Patrones
	 */	
	@Get("/testes/{idTeste}/smells/detectionMobile/get/{smellId}")
	@Logado
	public void getSmellsMobile(String idTeste, Long smellId) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		MobileUsabilitySmellDetector usm = new MobileUsabilitySmellDetector();
		SmellMobileMiningVO smellMobsVO = null;
		
		SmellMobileMining smellMob = smellMobileRepository.getSmellMobilebyId(smellId);
		List<SmellMobileDetMining> detailSmell = smellMobileDetRepository.getSmellMobileDet(smellMob.getId());
		
		smellMob.setDetail(detailSmell);	
		smellMobsVO = new SmellMobileMiningVO(smellMob);
		smellMobsVO.setPattern(usm.getPatternOrig(detailSmell));		
				
		String json = gson.toJson(smellMobsVO);
		
		result.use(Results.json()).from(json).serialize();
	}
		
	@Post("/testes/smells/mobile/save")
	@Consumes("application/json")
	@Logado
	public void saveSmellsMobile(SmellMobileMining smellMobile ) {
		Gson gson = new Gson();
		List<SmellMobileDetMining> smellDetMobileList = null;
		List<SmellMobileDetMining> smellDetMobileListOrigen = null;
		
		//result.use(Results.json()).from(gson.toJson(returnvo)).serialize();		
		//validateComponente.validarString(task.getTitle(), "datamining.tasks.title");		
		//if(!validator.hasErrors()){
		
		smellDetMobileList = smellMobile.getDetail();
		smellMobile.setDetail(null);
						
		if(smellMobile.getId() != null){
				SmellMobileMining smellUpdate = smellMobileRepository.getSmellMobilebyId(smellMobile.getId());
				smellUpdate.setsName(smellMobile.getsName());
				smellUpdate.setsSmellType(smellMobile.getsSmellType());
				smellUpdate.setsContent(smellMobile.getsContent());			
				smellUpdate.setUpdatedAt(new Date());
				smellUpdate.setsUsername("");
				smellMobileRepository.update(smellUpdate);
				//returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.tasks.edit.success");
		}else{				
				smellMobile.setCreatedAt(new Date());
				smellMobile.setUpdatedAt(new Date());
				smellMobileRepository.create(smellMobile);				
				//returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.tasks.new.success");
		}
		
		smellDetMobileListOrigen = smellMobileDetRepository.getSmellMobileDet(smellMobile.getId());
				
		if (smellDetMobileList != null) {
			
				for(SmellMobileDetMining smellMobDet : smellDetMobileList){
					
					if(smellMobDet.getId() != null){
						SmellMobileDetMining smellDetUpdate = smellMobileDetRepository.getSmellMobileDetbyId(smellMobDet.getId());
						
						if (smellDetMobileListOrigen.contains(smellDetUpdate)) {
							smellDetMobileListOrigen.remove(smellDetUpdate);
						}
						
						smellDetUpdate.setsEvent(smellMobDet.getsEvent());
						smellDetUpdate.setsQuantity(smellMobDet.getsQuantity());
						smellDetUpdate.setsDuration(smellMobDet.getsDuration());						
						smellDetUpdate.setUpdatedAt(new Date());						
						smellDetUpdate.setsIndTag(smellMobDet.getsIndTag());						
						smellMobileDetRepository.update(smellDetUpdate);
						
					}else {
						smellMobDet.setCreatedAt(new Date());
						smellMobDet.setUpdatedAt(new Date());
						smellMobDet.setIdSmell(smellMobile.getId());
						smellMobileDetRepository.create(smellMobDet);					
					}
				}
				
				for(SmellMobileDetMining smellMobDet : smellDetMobileListOrigen) {
					smellMobileDetRepository.destroy(smellMobDet);
				}				
		}
		
		smellMobile.setDetail(smellDetMobileList);		
		String json = gson.toJson(smellMobile);			
		result.use(Results.json()).from(json).serialize();		
	}
	
	
	@Post("/testes/smells/mobile/detection")
	@Consumes("application/json")
	@Logado
	public void detectiontMobile(TestDataMining test, Long initDate, Long endDate, int cantRep, int tamMin, int tamMax, 
								Long[] selectedSmells, Long[] selectedTasks, boolean smellGeneral) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, ParseException {
		Gson gson = new GsonBuilder()
				.setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
		        .serializeNulls()
		        .create();
			
		
		EntityManager entityManager = EntityManagerUtil.getEntityManager();
		EntityManager entityDafaultManager = EntityDefaultManagerUtil.getEntityManager();
		
		TaskDataMiningRepository taskDataMiningRepository = new TaskDataMiningRepository(entityDafaultManager);
		ActionDataMiningRepository actionDataMiningRepository = new ActionDataMiningRepository(entityManager);
		
		MobileUsabilitySmellDetector usm = new MobileUsabilitySmellDetector();
		List<TaskSmellAnalysis> tasks = new ArrayList<TaskSmellAnalysis>();
		List<ActionDataMining> actions = new ArrayList<ActionDataMining>();
		
		boolean mineSessions = false;
		boolean mineActions = false;
		boolean selectSmells = false;
		
		if (selectedTasks.length != 0) mineSessions = true;
		if (selectedSmells.length != 0) selectSmells = true;
		if ((selectedSmells.length != 0) || smellGeneral) mineActions = true;
		
		Map<String, List<TaskMobileSmellAnalysisResult>> tasksAnalysisResult = new LinkedHashMap<String, List<TaskMobileSmellAnalysisResult>>();
		
		List<TaskMobileSmellAnalysisResult> resultSmellAnalysisbySessions = null;
		List<TaskMobileSmellAnalysisResult> resultSmellAnalysis = null;
		List<TaskMobileSmellAnalysisResult> resultSmellAnalysisGeneral = new ArrayList<TaskMobileSmellAnalysisResult>();
		ResultDataMining resultDataMining = null;
		List<SmellMobileDetMining> detailSmell = null;
		SmellMobileMining smell = null;
		String jsonSmell = null;
	
		//Se obtiene la información tanto en forma de sesiones como en acciones
		Stopwatch actionsTimer = Stopwatch.createStarted();
		if (mineActions) {
			if (debug) System.out.println("Mining actions...");
			actions = WebUsageMining.listActionsBetweenDatesOrder(test.getClientAbbreviation(), taskDataMiningRepository, actionDataMiningRepository,
					  new Date(initDate), new Date(endDate), null);
			if (debug) System.out.println(actions.size() + " actions found.");
			System.out.println("Action mining took " + actionsTimer.stop());
		}
		
		if (mineSessions) {
			Stopwatch tasksTimer = Stopwatch.createStarted();
			
			for (TaskDataMining task : test.getTasks()) {
				if (ArrayUtils.contains(selectedTasks, task.getId())) {
					if (debug) System.out.println("Mining sessions for task " + task.getTitle() + "...");
					Stopwatch taskTimer = Stopwatch.createStarted();
					resultDataMining = WebUsageMining.analyze(task.getId(), initDate, endDate,
										SessionClassificationDataMiningFilterEnum.SUCCESS_ERROR_REPEAT, taskDataMiningRepository, actionDataMiningRepository);
					System.out.println("Finished in " + taskTimer.stop());
					if (debug) System.out.println(resultDataMining.getSessions().size() + " sessions found.");
					if (resultDataMining.getSessions().size() > 0)
						tasks.add(new TaskSmellAnalysis(task.getTitle(), resultDataMining.getSessions()));				
				}
			}
			System.out.println("Task mining took " + tasksTimer.stop());
		}
		
		//se busca patrones en las acciones
		Stopwatch generalTimer = Stopwatch.createStarted();
		if (smellGeneral) {
			resultSmellAnalysisGeneral = usm.analizePattern(actions, cantRep, tamMin, tamMax);	
			System.out.println("General mining took " + generalTimer.stop());
		}
		
		//se busca los patrones de los smells seleccionados
		if (selectSmells) {
			Stopwatch smellsTimer = Stopwatch.createStarted();			
			if (debug) System.out.println("Smells identificacion...");
				
			for (long smellId : selectedSmells) {				
				smell = smellMobileRepository.getSmellMobilebyId(smellId);
				detailSmell = smellMobileDetRepository.getSmellMobileDet(smellId);
				//smell.setPattern(usm.getPatternOrig(detailSmell));
				jsonSmell = gson.toJson(new SmellMobileMiningVO(smell)); 
					
				//buscar en las sessiones
				resultSmellAnalysisbySessions = usm.analizePatternbySession(tasks, detailSmell, actionDataMiningRepository);				
								
				//buscar en las acciones, sin las tareas
				resultSmellAnalysis = usm.analizePatternbyActions(actions, detailSmell, smell.getsSmellType(),actionDataMiningRepository);
				
				if ((resultSmellAnalysisbySessions.size() > 0 && (resultSmellAnalysis.size() > 0))) {
					resultSmellAnalysisbySessions.addAll(resultSmellAnalysis);
					tasksAnalysisResult.put(jsonSmell, resultSmellAnalysisbySessions);
				}
				if ((resultSmellAnalysisbySessions.size() > 0 && (resultSmellAnalysis.size() == 0)))
					tasksAnalysisResult.put(jsonSmell, resultSmellAnalysisbySessions);
				
				if ((resultSmellAnalysisbySessions.size() == 0 && (resultSmellAnalysis.size() > 0)))
					tasksAnalysisResult.put(jsonSmell, resultSmellAnalysis);
			}					
			if (debug) System.out.println("Finished in " + smellsTimer.stop());
			
		}
				
		String json = "";		
		try {
			if ((tasksAnalysisResult.size() == 0)&&(resultSmellAnalysisGeneral.size() == 0)) {
				json = gson.toJson(new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.smells.testes.detection.appok"));
			}else {
				json = gson.toJson(new SmellMobileAnalysisResultVO(tasksAnalysisResult,resultSmellAnalysisGeneral));
				//json = gson.toJson(new SmellMobileAnalysisResultVO(tasksAnalysisResult));			
			}

			result.use(Results.json()).from(json).serialize();
			
		}catch(OutOfMemoryError err){
			if (debug) System.out.println(Runtime.getRuntime().freeMemory());
			ReturnVO returnvo = new ReturnVO(ReturnStatusEnum.ERRO, "datamining.smells.error.mobile.smells");
			result.use(Results.json()).from(gson.toJson(returnvo)).serialize();
		}		
		
	}
	
	public void writeJsonStream(OutputStream out,List<TaskMobileSmellAnalysisResult> messages, Gson gson) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writer.beginArray();
        
        for (TaskMobileSmellAnalysisResult message : messages) {
            gson.toJson(message, TaskMobileSmellAnalysisResult.class, writer);
        }
        writer.endArray();
        writer.close();
    }


	
	
}
