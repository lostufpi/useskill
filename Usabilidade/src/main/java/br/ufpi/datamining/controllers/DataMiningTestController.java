package br.ufpi.datamining.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;

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
import br.ufpi.datamining.analisys.Fuzzy;
import br.ufpi.datamining.analisys.WebUsageMining;
import br.ufpi.datamining.models.ActionDataMining;
import br.ufpi.datamining.models.EvaluationTaskDataMining;
import br.ufpi.datamining.models.EvaluationTestDataMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.TestDataMining;
import br.ufpi.datamining.models.auxiliar.CountActionsAux;
import br.ufpi.datamining.models.auxiliar.FieldSearch;
import br.ufpi.datamining.models.auxiliar.FieldSearchComparatorEnum;
import br.ufpi.datamining.models.enums.ActionTypeDataMiningEnum;
import br.ufpi.datamining.models.enums.ReturnStatusEnum;
import br.ufpi.datamining.models.vo.EvaluationTestDataMiningVO;
import br.ufpi.datamining.models.vo.ReturnVO;
import br.ufpi.datamining.models.vo.TestDataMiningVO;
import br.ufpi.datamining.repositories.ActionDataMiningRepository;
import br.ufpi.datamining.repositories.EvaluationTaskDataMiningRepository;
import br.ufpi.datamining.repositories.EvaluationTestDataMiningRepository;
import br.ufpi.datamining.repositories.TaskDataMiningRepository;
import br.ufpi.datamining.repositories.TestDataMiningRepository;
import br.ufpi.datamining.utils.UsabilityUtils;
import br.ufpi.models.Tarefa;
import br.ufpi.models.Teste;
import br.ufpi.models.Usuario;
import br.ufpi.repositories.TesteRepository;
import br.ufpi.util.Paginacao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path(value = "datamining")
@Resource
public class DataMiningTestController extends BaseController {

	private final TesteRepository testeRepository;
	private final TestDataMiningRepository testeDataMiningRepository;
	private final TaskDataMiningRepository taskDataMiningRepository;
	private final ActionDataMiningRepository actionDataMiningRepository;
	private final EvaluationTaskDataMiningRepository evaluationTaskDataMiningRepository;
	private final EvaluationTestDataMiningRepository evaluationTestDataMiningRepository;
	
	public DataMiningTestController(Result result, Validator validator,
			TesteView testeView, UsuarioLogado usuarioLogado,
			ValidateComponente validateComponente,
			TestDataMiningRepository testeDataMiningRepository,
			TaskDataMiningRepository taskDataMiningRepository,
			ActionDataMiningRepository actionDataMiningRepository,
			EvaluationTaskDataMiningRepository evaluationTaskDataMiningRepository,
			EvaluationTestDataMiningRepository evaluationTestDataMiningRepository,
			TesteRepository testeRepository) {
		super(result, validator, testeView, usuarioLogado, validateComponente);
		this.testeDataMiningRepository = testeDataMiningRepository;
		this.taskDataMiningRepository = taskDataMiningRepository;
		this.actionDataMiningRepository = actionDataMiningRepository;
		this.evaluationTaskDataMiningRepository = evaluationTaskDataMiningRepository;
		this.evaluationTestDataMiningRepository = evaluationTestDataMiningRepository;
		this.testeRepository = testeRepository;
	}
	
	@Get("/")
	@Logado
	public void index() {}
	
	@Get("/testes/")
	@Logado
	public void list() {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		List<TestDataMining> tests = testeDataMiningRepository.getTests(usuarioLogado.getUsuario().getId());
		List<TestDataMiningVO> testsVO = new ArrayList<TestDataMiningVO>();
		for(TestDataMining test : tests){
			testsVO.add(new TestDataMiningVO(test));
		}
		String json = gson.toJson(testsVO);
		
		result.use(Results.json()).from(json).serialize();
	}
	
	@Get("/testes/{idTeste}")
	@Logado
	public void view(Long idTeste) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		TestDataMining testPertencente = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), idTeste);
		validateComponente.validarNotNull(testPertencente, "datamining.accessdenied");
		validator.onErrorRedirectTo(this).list();
		
		result.use(Results.json()).from(gson.toJson(new TestDataMiningVO(testPertencente))).serialize();
	} 
	
	@Post("/testes/salvar")
	@Consumes("application/json")
	@Logado
	public void salvar(TestDataMining test, Long testConrolId) {
		Gson gson = new Gson();
		//TestDataMining test = gson.fromJson(json, TestDataMining.class);
		validateComponente.validarString(test.getTitle(), "datamining.testes.title");
		validateComponente.validarString(test.getClientAbbreviation(), "datamining.testes.abbrev");
		validateComponente.validarURL(test.getUrlSystem());
		
		ReturnVO returnvo; 
		
		if(!validator.hasErrors()){
			if(test.getId() != null){
				TestDataMining testUpdate = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), test.getId());
				testUpdate.setTitle(test.getTitle());
				testUpdate.setClientAbbreviation(test.getClientAbbreviation());
				testUpdate.setUrlSystem(test.getUrlSystem());
				testUpdate.setIsIgnoreURL(test.getIsIgnoreURL());
				
				if (testConrolId != null) {
					Teste teste = testeRepository.find(testConrolId);
					testUpdate.setTestControl(teste);
					testUpdate.setIsControl(true);
				} else {
					testUpdate.setTestControl(null);
					testUpdate.setIsControl(false);
				}
				
				testeDataMiningRepository.update(testUpdate);
				returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.testes.edit.success");
			}else{
				ArrayList<Usuario> users = new ArrayList<Usuario>();
				users.add(usuarioLogado.getUsuario());
				test.setUsers(users);
				test.setUserCreated(usuarioLogado.getUsuario());
				
				if (testConrolId != null) {
					Teste teste = testeRepository.find(testConrolId);
					test.setTestControl(teste);
					test.setIsControl(true);
				} else {
					test.setTestControl(null);
					test.setIsControl(false);
				}
				
				testeDataMiningRepository.create(test);
				returnvo = new ReturnVO(ReturnStatusEnum.SUCESSO, "datamining.testes.new.success");
			}
			
			result.use(Results.json()).from(gson.toJson(returnvo)).serialize();
		}else{
			returnvo = new ReturnVO(ReturnStatusEnum.ERRO, "erro");
			returnvo.setErrorsMessage(validator.getErrors());
			
			validator.onErrorUse(Results.json()).from(gson.toJson(returnvo)).serialize();
		}
		
	}
	
	@Get("/testes/remover/{idTeste}")
	@Logado
	public void exclude(Long idTeste) {
		//TODO
	}
	
	@Get("/testes/{idTeste}/maisacessados/{field}/init/{initDate}/end/{endDate}")
	@Logado
	public void mostView(Long idTeste, String field, Long initDate, Long endDate) {
		Gson gson = new Gson();
		
		TestDataMining testPertencente = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), idTeste);
		validateComponente.validarNotNull(testPertencente, "datamining.accessdenied");
		validator.onErrorRedirectTo(this).list();
		
		Date initialDate = new Date(initDate);
		Date finalDate = new Date(endDate);
		FieldSearch fieldSearch = new FieldSearch(field, field, null, null);
		
		List<CountActionsAux> counts = WebUsageMining.countActionsByRestrictions(idTeste, fieldSearch, testeDataMiningRepository, actionDataMiningRepository, initialDate, finalDate);
		List<CountActionsAux> countsResult = new ArrayList<CountActionsAux>();
		CountActionsAux empty = new  CountActionsAux("-", fieldSearch, 0l);
		long count = 0l;
		for (CountActionsAux c : counts) {
			System.out.println(c.getDescription() + " -> " + c.getCount());
			if (c.getDescription() == null || c.getDescription().trim().equals("")) {
				count += c.getCount();
			} else {
				countsResult.add(c);
			}
		}
		empty.setCount(count);
		countsResult.add(empty);
		
		result.use(Results.json()).from(gson.toJson(countsResult)).serialize();
	}
	
	@Get("/testes/{idTeste}/sessaoespecifica/{username}/local/{local}/init/{initDate}/end/{endDate}/limit/{limit}")
	@Logado
	public void actionsBetweenDates(Long idTeste, String username, String local, Long initDate, Long endDate, Long limit) {
		Gson gson = new Gson();
		
		TestDataMining testPertencente = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), idTeste);
		validateComponente.validarNotNull(testPertencente, "datamining.accessdenied");
		validator.onErrorRedirectTo(this).list();
		
		Date initialDate = new Date(initDate);
		Date finalDate = new Date(endDate);
		
		
		FieldSearch fieldLocation = new FieldSearch("sJhm", "local", local, FieldSearchComparatorEnum.EQUALS);
		fieldLocation = null;
		List<ActionDataMining> listActionsBetweenDates = WebUsageMining.listActionsFromUserBetweenDates(testPertencente.getClientAbbreviation(), username, fieldLocation, actionDataMiningRepository, initialDate, finalDate, limit);
		
		result.use(Results.json()).from(gson.toJson(listActionsBetweenDates)).serialize();
	}
	
	@Post("/testes/newevaluationtest")
	@Consumes("application/json")
	@Logado
	public void newevaluationtest(Long idTeste, Long initDate, Long endDate) {
		Gson gson = new GsonBuilder()
			.setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		TestDataMining testPertencente = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), idTeste);
		
		EvaluationTestDataMining eval = new EvaluationTestDataMining();
		eval.setTest(testPertencente);
		eval.setInitDate(new Date(initDate));
		eval.setLastDate(new Date(endDate));
		
		List<EvaluationTestDataMining> evaluationTests = evaluationTestDataMiningRepository.getEvaluationTests(testPertencente, eval.getInitDate(), eval.getLastDate());
		
		if (evaluationTests.size() == 0) {
			evaluationTestDataMiningRepository.create(eval);
			result.use(Results.json()).from(gson.toJson(new TestDataMiningVO(testPertencente))).serialize();
		} else {
			System.out.println("already exists");
			result.use(Results.json()).from(gson.toJson(new ReturnVO(ReturnStatusEnum.ERRO, "datamining.testes.evaluations.newdates.alreadyexists"))).serialize();
		}
	}
	
	@Get("/testes/{idTeste}/avaliacao/{idEvalTest}")
	@Logado
	public void getTestEvaluation(Long idTeste, Long idEvalTest) {
		Gson gson = new GsonBuilder()
			.setExclusionStrategies(EvaluationTestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		EvaluationTestDataMining evaluationTestDataMining = evaluationTestDataMiningRepository.find(idEvalTest);
		validateComponente.validarEquals(evaluationTestDataMining.getTest().getId(), idTeste, "datamining.accessdenied");
		
		ReturnVO returnvo;
		
		if(!validator.hasErrors()){
			result.use(Results.json()).from(gson.toJson(new EvaluationTestDataMiningVO(evaluationTestDataMining))).serialize();
		} else {
			returnvo = new ReturnVO(ReturnStatusEnum.ERRO, "erro");
			returnvo.setErrorsMessage(validator.getErrors());
			
			validator.onErrorUse(Results.json()).from(gson.toJson(returnvo)).serialize();
		}
	}
	
	@Get("/testes/{idTeste}/avaliacao/{idEvalTest}/alterarativo")
	@Logado
	public void changeActivateEvaluation(Long idTeste, Long idEvalTest) {
		Gson gson = new GsonBuilder()
			.setExclusionStrategies(EvaluationTestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		EvaluationTestDataMining evaluationTestDataMining = evaluationTestDataMiningRepository.find(idEvalTest);
		validateComponente.validarEquals(evaluationTestDataMining.getTest().getId(), idTeste, "datamining.accessdenied");
		
		Boolean isHidden = evaluationTestDataMining.getIsHidden();
		if (isHidden == null || isHidden != true) {
			evaluationTestDataMining.setIsHidden(true);
		} else {
			evaluationTestDataMining.setIsHidden(false);
		}
		
		evaluationTestDataMiningRepository.update(evaluationTestDataMining);
		
		ReturnVO returnvo;
		
		if(!validator.hasErrors()){
			result.use(Results.json()).from(gson.toJson(new EvaluationTestDataMiningVO(evaluationTestDataMining))).serialize();
		} else {
			returnvo = new ReturnVO(ReturnStatusEnum.ERRO, "erro");
			returnvo.setErrorsMessage(validator.getErrors());
			
			validator.onErrorUse(Results.json()).from(gson.toJson(returnvo)).serialize();
		}
	}
	
//	private boolean testePertenceUsuarioLogado(Long idTeste) {
//		validateComponente.validarId(idTeste);
//		TestDataMining teste = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), idTeste);
//		return teste != null;
//	}
	
	//CONTROL
	
	@Get("/testes/normalize/{idTeste}")
	@Logado
	public void normalize(Long idTeste) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		TestDataMining testPertencente = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), idTeste);
		List<TaskDataMining> tarefasTeste = taskDataMiningRepository.getTarefasTeste(idTeste);
		testPertencente.setTasks(new ArrayList<TaskDataMining>());
		for (TaskDataMining t : tarefasTeste) {
			t.setTestDataMining(testPertencente);
			testPertencente.getTasks().add(t);
			taskDataMiningRepository.update(t);
		}
		testeDataMiningRepository.update(testPertencente);
		validateComponente.validarNotNull(testPertencente, "datamining.accessdenied");
		validator.onErrorRedirectTo(this).list();
		
		result.use(Results.json()).from(gson.toJson(new TestDataMiningVO(testPertencente))).serialize();
	} 
	
	
	@Get("/testes/control")
	@Logado
	public void listControl() {
		Gson gson = new GsonBuilder()
	        .create();
		
		List<Teste> testesControl = testeRepository.findAll();
		HashMap<Long, String> testes = new HashMap<Long, String>();
		for (Teste t : testesControl) {
			if (t.getUsuarioCriador().getId().equals(usuarioLogado.getUsuario().getId())) {
				testes.put(t.getId(), t.getTituloPublico());
			}
		}
		String json = gson.toJson(testes);
		
		result.use(Results.json()).from(json).serialize();
	}
	
	@Get("/testes/errorgenerator")
	@Logado
	public void errorgenerator() {
		Integer x = null;
		x.byteValue();
	}
	
	@Get("/testes/control/{idTeste}/taskupdate")
	@Logado
	public void updateTaskControl(Long idTeste) {
		Gson gson = new GsonBuilder()
	        .setExclusionStrategies(TestDataMiningVO.exclusionStrategy)
	        .serializeNulls()
	        .create();
		
		TestDataMining testPertencente = testeDataMiningRepository.getTestPertencente(usuarioLogado.getUsuario().getId(), idTeste);
		List<TaskDataMining> tasks = testPertencente.getTasks();
		
		Teste testeControl = testPertencente.getTestControl();
		List<Tarefa> tarefas = testeControl.getTarefas();
		
		testPertencente.setTasks(new ArrayList<TaskDataMining>());
		for (TaskDataMining t : tasks) {
			taskDataMiningRepository.destroy(t);
		}
		
		Set<ActionTypeDataMiningEnum> disregardActions = new HashSet<ActionTypeDataMiningEnum>();
		disregardActions.add(ActionTypeDataMiningEnum.mouseover);
		
		for (Tarefa t : tarefas) {
			TaskDataMining task = new TaskDataMining();
			task.setTitle(t.getNome());
			task.setThreshold(20);
			task.setDisregardActions(disregardActions);
			task.setTaskControl(t);
			task.setIsControl(true);
			task.setTestDataMining(testPertencente);
			
			testPertencente.getTasks().add(task);
			
			taskDataMiningRepository.create(task);
		}
		testeDataMiningRepository.update(testPertencente);
		
		TestDataMiningVO testVO = new TestDataMiningVO(testPertencente);
		String json = gson.toJson(testVO);
		
		result.use(Results.json()).from(json).serialize();
	}

}
