angular.module('useskill', 
		['ngRoute', 
		 'ngAnimate',
		 'ngResource',
		 'pascalprecht.translate', 
		 'tableSort', 
		 'chart.js', 
		 'datePicker', 
		 'ui.bootstrap', 
		 'angularMoment', 
		 'angular-loading-bar',
		 'nvd3'
		 ])

//.constant('env', 'dev')
.constant('env', 'prod')
		
.constant('config', {
    appVersion: 0.1,
    dev: {
    	apiUrl: '/Usabilidade'
    },
    prod: {
    	apiUrl: '/useskill'
    }
})
		
.config(['$translateProvider', '$routeProvider', '$httpProvider', 'env', 'config', 'cfpLoadingBarProvider',
         function($translateProvider, $routeProvider, $httpProvider, env, config, cfpLoadingBarProvider) {
	
	$httpProvider.interceptors.push('HttpInterceptor');

	cfpLoadingBarProvider.spinnerTemplate = '<div class="loader__bg usdm-animate-show-hide"><span class="loader__img"/></div>';
	
	$translateProvider.useUrlLoader(config[env].apiUrl+'/jscripts/app/messages.json');
	$translateProvider.preferredLanguage('pt');
	
	var routeInit = {
		controller:'TestController as testCtrl',
		templateUrl:config[env].apiUrl+'/templates/tests/list.html',
		resolve: {
			tests: function (ServerAPI) {
	        	return ServerAPI.getTests();
	        }
	    }
	};
	
	$routeProvider
		.when('/', routeInit)
		.when('/testes/', routeInit)
		.when('/testes/criar', {
	    	controller:'TestNewController as testCtrl',
	    	templateUrl:config[env].apiUrl+'/templates/tests/create.html',
	    	resolve: {
				testsControl: function (ServerAPI) {
		        	return ServerAPI.getTestsControl();
		        }
		    }
	    })
	    .when('/testes/editar/:testId', {
	    	controller:'TestEditController as testCtrl',
	    	templateUrl:config[env].apiUrl+'/templates/tests/create.html',
	    	resolve: {
				test: function (ServerAPI, $route, $rootScope) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        },
		        testsControl: function (ServerAPI) {
		        	return ServerAPI.getTestsControl();
		        }
		    }
	    })
		.when('/testes/:testId', {
			controller:'TestViewController as testCtrl',
			templateUrl:config[env].apiUrl+'/templates/tests/detail.html',
			resolve: {
				test: function (ServerAPI, $route, $rootScope) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    .when('/testes/:testId/maisacessadas', {
			controller:'TestMostAccessController as testCtrl',
			templateUrl:config[env].apiUrl+'/templates/tests/mostaccess.html',
			resolve: {
		        test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    .when('/testes/:testId/sessaoespecifica', {
			controller:'TestSpecificSessionController as testCtrl',
			templateUrl:config[env].apiUrl+'/templates/tests/viewsession.html',
			resolve: {
		        test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    .when('/testes/:testId/tarefas', {
			controller:'TestTasksDatesViewController as testCtrl',
			templateUrl:config[env].apiUrl+'/templates/tests/tasklistdates.html',
			resolve: {
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    .when('/testes/:testId/avaliar/:evalId', {
			controller:'TestTasksViewController as testCtrl',
			templateUrl:config[env].apiUrl+'/templates/tests/tasklist.html',
			resolve: {
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        },
		        evalTest: function (ServerAPI, $route) {
		        	return ServerAPI.getEvaluationTest($route.current.params.testId, $route.current.params.evalId);
		        }
		    }
	    })
	    
	    //Tasks
	    .when('/testes/:testId/tarefas/criar', {
	    	controller:'TaskNewController as taskCtrl',
	    	templateUrl:config[env].apiUrl+'/templates/tasks/create.html',
	    	resolve: {
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    .when('/testes/:testId/tarefas/editar/:taskId', {
	    	controller:'TaskEditController as taskCtrl',
	    	templateUrl:config[env].apiUrl+'/templates/tasks/create.html',
	    	resolve: {
		        task: function (ServerAPI, $route) {
		        	return ServerAPI.getTask($route.current.params.testId, $route.current.params.taskId);
		        }
		    }
	    })
	    .when('/testes/:testId/tarefas/:taskId', {
	    	controller:'TaskViewController as taskCtrl',
	    	templateUrl:config[env].apiUrl+'/templates/tasks/detail.html',
	    	resolve: {
		        task: function (ServerAPI, $route) {
		        	return ServerAPI.getTask($route.current.params.testId, $route.current.params.taskId);
		        }
		    }
	    })
	    .when('/testes/:testId/avaliacao/:evalTestId/tarefas/:taskId/avaliar', {
	    	controller:'TaskEvaluateController as taskCtrl',
	    	templateUrl:config[env].apiUrl+'/templates/tasks/evaluate.html',
	    	resolve: {
		        evaluate: function (ServerAPI, $route) {
		        	return ServerAPI.evaluateTask($route.current.params.testId, $route.current.params.evalTestId, $route.current.params.taskId);
		        },
		        evalTestId: function ($route) {
		        	return $route.current.params.evalTestId;
		        },
		        sessionFilterParam: function ($route) {
		        	return $route.current.params.sessionFilter;
		        }
		    },
		    data: {
		        longLoading: true
		    }
	    })
	    .when('/testes/:testId/avaliacao/:evalTestId/tarefas/:taskId/avaliar/sessoes/:sessionFilter/minsup/:minSup/minitens/:minItens/', {
	    	controller:'TaskEvaluateController as taskCtrl',
	    	templateUrl:config[env].apiUrl+'/templates/tasks/evaluate.html',
	    	resolve: {
		        evaluate: function (ServerAPI, $route) {
		        	return ServerAPI.evaluateTaskParams($route.current.params.testId, $route.current.params.evalTestId, $route.current.params.taskId,
		        							$route.current.params.sessionFilter, $route.current.params.minSup, $route.current.params.minItens);
		        },
		        evalTestId: function ($route) {
		        	return $route.current.params.evalTestId;
		        },
		        sessionFilterParam: function ($route) {
		        	return $route.current.params.sessionFilter;
		        }
		    },
		    data: {
		        longLoading: true
		    }
	    })
	    
	    //Actions
	    .when('/testes/:testId/tarefas/:taskId/acoes/add', {
	    	controller:'ActionNewController as actionCtrl',
	    	templateUrl:config[env].apiUrl+'/templates/actions/create.html',
	    	resolve: {
	    		task: function (ServerAPI, $route) {
		        	return ServerAPI.getTask($route.current.params.testId, $route.current.params.taskId);
		        }
		    }
	    })
	    
	    //Smells
	    .when('/testes/:testId/smells/statistics', {
			controller:'SmellsStatisticFormController as smellCtrl',
			templateUrl:config[env].apiUrl+'/templates/smells/statisticsform.html',
			resolve: {
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    .when('/testes/:testId/smells/detection', {
			controller:'SmellsDetectionFormController as smellCtrl',
			templateUrl:config[env].apiUrl+'/templates/smells/detectionform.html',
			resolve: {
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    .when('/testes/:testId/smells/detection/result', {
			controller:'SmellsDetectionResultController as smellCtrl',
			templateUrl:config[env].apiUrl+'/templates/smells/detectionresult.html',
			resolve: {
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    .when('/testes/:testId/smells/config/:smellId', {
			controller:'SmellConfigController as smellCtrl',
			templateUrl:config[env].apiUrl+'/templates/smells/config.html',
			resolve: {
				parameters: function (ServerAPI, $route) {
		        	return ServerAPI.getConfigSmell($route.current.params.testId, $route.current.params.smellId);
		        },
		        idSmell: function ($route) {
		        	return $route.current.params.smellId;
		        }
		    }
	    })
	    
	    /*Cambios MILL*/
	    /*PAG PRINCIPAL*/
	    .when('/testes/:testId/smells/detectionMobile', {
			controller:'SmellsMobileController as smellCtrl',
			templateUrl:config[env].apiUrl+'/templates/smellsMobile/tasklistdates.html',
			resolve: {
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        },
		        smells: function (ServerAPI, $route) {
		        	return ServerAPI.getSmellMobile($route.current.params.testId);
		        }
		    }
	    })
	    /*CREA UNO */
	    .when('/testes/:testId/smells/mobile/new', {
			controller:'NewSmellsMobileController as smellCtrl',
			templateUrl:config[env].apiUrl+'/templates/smellsMobile/create.html',
			resolve: {
				test: function (ServerAPI, $route) {
					return ServerAPI.getTest($route.current.params.testId);
		        }
		    }
	    })
	    
	    /*EDITA UNO */
	    .when('/testes/:testId/smells/mobile/:smellId', {
			controller:'EditSmellsMobileController as smellCtrl',
			templateUrl:config[env].apiUrl+'/templates/smellsMobile/create.html',
			resolve: {				
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        },
		        smell: function (ServerAPI, $route) {
		        	return ServerAPI.getSmellMobilebyId($route.current.params.testId, $route.current.params.smellId);
		        }
		    }
	    })   
	    
	    /*VISUALIZA OPCIONES PARA MOSTRAR LOS MOBILE USABILITY SMELLS*/
	    .when('/testes/:testId/smells/detectionMobileList', {
			controller:'SmellsDetectionMobileController as smellCtrl',
			templateUrl:config[env].apiUrl+'/templates/smellsMobile/detectionform.html',
			resolve: {
				test: function (ServerAPI, $route) {
		        	return ServerAPI.getTest($route.current.params.testId);
		        },
		        smells: function (ServerAPI, $route) {
		        	return ServerAPI.getSmellMobile($route.current.params.testId);
		        }
		    }
	    })
	    /*Fin MILL*/
	    
	    .when('/error', {
	        templateUrl: config[env].apiUrl+'/templates/error.html',
	        controller: 'ErrorController'
	    })
	    .when('/informacoes', {
	        templateUrl: config[env].apiUrl+'/templates/informations.html',
	        controller: 'InformationsController'
	    })
	    
	    
	    .otherwise({
	    	redirectTo:'/'
	    });
	
}])
.run(function($rootScope) {
	$rootScope.clearErrors = function() {
		$rootScope.errors = null;
	}
	$rootScope.clearSuccess = function() {
		$rootScope.success = null;
	}
})
.factory('HttpInterceptor', ['$q', '$rootScope', '$filter', '$location', 'env', 'config', '$timeout',
                     function($q, $rootScope, $filter, $location, env, config, $timeout) {
	var responseInterceptor = {
		response: function(response) {
			$rootScope.errors = null;
			$rootScope.success = null;
			//console.log(response);
			if(response.status === 200 && angular.isDefined(response.data.string)){
				resp = JSON.parse(response.data.string);
				if(resp.status == 'ERRO'){
					$rootScope.errors = [];
					if (resp.message) {
						console.log($filter('translate')(resp.message));
						console.log(resp.message);
						$rootScope.errors.push($filter('translate')(resp.message));
					}
					$rootScope.errors.concat(resp.errors);
					console.log($rootScope.errors);
				}else if(resp.status == 'SUCESSO'){
					$rootScope.success = $filter('translate')(resp.message);
				}
			}
			
			if($rootScope.mgsRealod){
				$rootScope.success = $rootScope.success != null ? $rootScope.success + ". " + $rootScope.mgsRealod.success : $rootScope.mgsRealod.success;
				$rootScope.error = $rootScope.error != null ? $rootScope.error + ". " + $rootScope.mgsRealod.error : $rootScope.mgsRealod.error;
				//gambis para evitar apagar msgReload antes de carregar
				setTimeout(function(){$rootScope.mgsRealod = null;},100);
			}
			
            return ( response );
        },
        responseError: function(error) {
        	console.log('FALHOU!', error);
        	$location.url('/error');
        	//$location.path(config[env].apiUrl+"/");
        	$rootScope.errors = [error.status+' - '+error.statusText];
        }
    };
    return responseInterceptor;
}])
.factory("ShowMessage", ['$rootScope', '$filter', function($rootScope, $filter){
	var showMessage = {
		clear: function () {
			$rootScope.success = null;
			$rootScope.errors = null;
		},
		success: function (msg) {
			showMessage.clear();
			if (msg) {
				$rootScope.success = $filter('translate')(msg);
			}
		},
		error: function (error) {
			showMessage.clear();
			if (error) {
				var msgs = [];
				msgs.push(error);
				showMessage.errors(msgs);
			}
		},
		errors: function (errors) {
			showMessage.clear();
			$rootScope.errors = [];
			if (errors) {
				for (var i in errors) {
					var msg = errors[i];
					console.log('error: ', msg, $filter('translate')(msg));
					$rootScope.errors.push($filter('translate')(msg));
				}
			}
			$rootScope.errors.concat(resp.errors);
		}
	};
	
	return showMessage;
}])
.factory("ServerAPI", ['$rootScope', '$q', '$http', 'env', 'config', 'cfpLoadingBar',
                function($rootScope, $q, $http, env, config, cfpLoadingBar) {
	
	function doRequest(type, url, data) {
		var deferred = $q.defer();
		
		$rootScope.onRequest = true;
		cfpLoadingBar.start();
		
		var req, reqUrl = config[env].apiUrl + url;
		
		if (type.toLowerCase() == 'post') {
			req = $http.post(reqUrl, data);
		} else {
			req = $http({ method: type, url: reqUrl });
		}
		
		req.then(function successCallback(response) {
			console.log('request ok: ', response);
			deferred.resolve(response);
		}, function errorCallback(response) {
			console.log('request error: ', response);
			deferred.reject(response);
		}).finally(function(){
			$rootScope.onRequest = false;
			cfpLoadingBar.complete();
		});
		return deferred.promise;
	};
	
    return {
    	getTests: function(){
            return doRequest('GET', '/datamining/testes');
        },
        getTest: function(testId){
            return doRequest('GET', '/datamining/testes/'+testId);
        },
        saveTest: function(test){
        	return doRequest('POST', '/datamining/testes/salvar', test);
        },
        getTestMostAccess: function(testId, field, init, end){
            return doRequest('GET', '/datamining/testes/'+testId+'/maisacessados/'+field+'/init/'+init+'/end/'+end);
        },
        getSpecificSession: function(testId, user, local, init, end, limit){
            return doRequest('GET', '/datamining/testes/'+testId+'/sessaoespecifica/'+user+'/local/'+local+'/init/'+init+'/end/'+end+'/limit/'+limit);
        },
        
        
        saveNewEvaluationTest: function(evaluationTest){
        	return doRequest('POST', '/datamining/testes/newevaluationtest', evaluationTest);
        },
        getEvaluationTest: function(testId, evalId) {
        	return doRequest('GET', '/datamining/testes/'+testId+'/avaliacao/'+evalId);
        },
        changeActivateEvaluationTest: function(testId, evalId) {
        	return doRequest('GET', '/datamining/testes/'+testId+'/avaliacao/'+evalId+'/alterarativo');
        },
        
        
        getTask: function(testId, taskId) {
            return doRequest('GET', '/datamining/testes/'+testId+'/tarefas/'+taskId);
        },
        saveTask: function(task) {
        	return doRequest('POST', '/datamining/testes/tarefas/salvar', task);
        },
        evaluateTask: function(testId, evalTestId, taskId){
        	return doRequest('GET', '/datamining/testes/'+testId+'/avaliacao/'+evalTestId+'/tarefas/'+taskId+'/avaliar');
        },
        evaluateTaskParams: function(testId, evalTestId, taskId, sessionFilter, minSup, minItens){
        	return doRequest('GET', '/datamining/testes/'+testId+'/avaliacao/'+evalTestId+'/tarefas/'+taskId+'/avaliar/sessoes/'+sessionFilter+'/minsup/'+minSup+'/minitens/'+minItens);
        },
        
        
        saveAction: function(actionJHeatVO){
        	return doRequest('POST', '/datamining/testes/tarefas/acoes/salvar', actionJHeatVO);
        },
        deleteAction: function(actionJHeatVO, list){
        	var promise = $http.post(config[env].apiUrl+'/datamining/testes/tarefas/acoes/deletar', { actionId: actionJHeatVO.id });
        	promise.success(function(data){
        		var resp = JSON.parse(data.string);
    			if(resp.status == 'SUCESSO' && list != undefined){
    				angular.forEach(list, function(obj, idx){
        				if(obj == actionJHeatVO){
        					list.splice(idx, 1);
        				}
        			});
    			}
    		});
        	return promise;
        },
        
        //CLUSTERS
        clusterSessions: function(testId, evalTestId, taskId, numClusters, thresholdClusters) {
        	return doRequest('GET', '/datamining/testes/'+testId+'/avaliacao/'+evalTestId+'/tarefas/'+taskId+'/clusterizar/numclusters/'+numClusters+'/threshold/'+thresholdClusters);
        },
        
        //COMPARE PATTERNS
        comparePatterns: function(patternData) {
        	return doRequest('POST', '/datamining/testes/tarefas/compararpadroes', patternData);
        },
        
        
        //CONTROL
        getTestsControl: function(){
        	return doRequest('GET', '/datamining/testes/control');
        },
        updateTaskControl: function(testId){
        	return doRequest('GET', '/datamining/testes/control/'+testId+'/taskupdate');
        },
        saveActionSituation: function(testId, evalTestId, taskId, actions){
        	var data = {
        		testId: testId, 
        		evalTestId: evalTestId, 
        		taskId: taskId, 
        		actions: actions
        	}
        	return doRequest('POST', '/datamining/testes/avaliacao/tarefas/saveActionSituation', data);
        },
        
        //SMELLS
        viewSmellsStatistics: function(obj){
        	return doRequest('POST', '/datamining/testes/smells/statistics', obj);
        },
        detectSmells: function(obj){
        	return doRequest('POST', '/datamining/testes/smells/detection', obj);
        },
        updateIgnoredUrls: function(obj){
        	return doRequest('POST', '/datamining/testes/smells/detection/ignoredurls/update', obj);
        },
        getIgnoredUrls: function(obj){
        	return doRequest('POST', '/datamining/testes/smells/detection/ignoredurls/get', obj);
        },
        getConfigSmell: function(testId, smellId){
        	return doRequest('GET', '/datamining/testes/'+testId+'/smells/'+smellId+'/parameters');
        },
        changeSmellParameter: function(obj) {
        	return doRequest('POST', '/datamining/testes/smells/parameters/change', obj);
        },
        /*CAMBIOS MILL*/
        getSmellMobile: function(testId){
        	return doRequest('GET', '/datamining/testes/'+testId+'/smells/detectionMobile');
        },
        getSmellMobilebyId: function(testId,smellId){
        	return doRequest('GET', '/datamining/testes/'+testId+'/smells/detectionMobile/get/'+smellId);
        },        
        saveSmellMobile: function(obj){
        	return doRequest('POST', '/datamining/testes/smells/mobile/save',obj);
        },
        detectSmellMobile: function(obj){
        	return doRequest('POST', '/datamining/testes/smells/mobile/detection', obj);
        }
        /*FIN MILL*/
        
    };
}])
.factory("ValidateService", function(){
	var validate = {
		isNumeric: function(n) {
			return !isNaN(parseFloat(n)) && isFinite(n);
		},
		task : function(task) {
			if (task.title != null && task.title != "" &&
				task.threshold != null && validate.isNumeric(task.threshold)) {
				return true;
			}
			return false;
		},
		action: function(action) {
			if (action != null &&
				action.momentType != null && action.momentType != ""  &&
				action.actionType != null && action.actionType != ""  &&
				action.description != null && action.description != ""  &&
				action.element != null && action.element != ""  &&
				action.url != null && action.url != "") {
				return true;
			}
			return false;			
		},
		//MILL
		smell : function(smell) {
			if (smell.sName != null && smell.sName != "" &&
				smell.sContent != null && smell.sContent != "") {
				for (var a in smell.detail) {
					if (smell.detail[a].sEvent == null || smell.detail[a].sEvent == "" ||
						smell.detail[a].sQuantity == null || smell.detail[a].sQuantity == "") {
						return false;
					}
				}				
				return true;
			}
			return false;
		}
		//FIN MILL
		
	};
	return validate;
})
.factory("UtilsService", ['ValidateService', function(ValidateService){
	var utils = {
		formatDate : function(date) {
			return moment(date, 'MMM DD, YYYY hh:mm:ss A').format('DD/MM/YYYY, HH:mm:ss');
		},
		momentTimestamp : function(date) {
			return moment(date, 'MMM DD, YYYY hh:mm:ss A').valueOf();
		},
		datepickerToTimestamp: function(n) {
			if (!ValidateService.isNumeric(n)) {
				var time = moment(n, "DD-MM-YYYY HH:mm");
				return time.valueOf();
			} else {
				return n;
			}
		},
		trim: function (str) {
			if (str) {
				str = str.replace(/^\s+/, '');
			    for (var i = str.length - 1; i >= 0; i--) {
			        if (/\S/.test(str.charAt(i))) {
			            str = str.substring(0, i + 1);
			            break;
			        }
			    }
			}
		    return str;
		}
	};
	return utils;
}])

//Enums

.factory("ActionTypeEnum", function(){
	return {
		types : [
	 	    {name:'Carregamento', value:'onload'},
	 	    {name:'Clique', value:'click'},
	 	    {name:'Envio de Formulário', value:'form_submit'},
	 	    {name:'Preenchimento de Campo', value:'focusout'},
	 	  	{name:'Mouse Sobre', value:'mouseover'},
	 	 	{name:'Voltar', value:'back'},
	 		{name:'Recarregar', value:'reload'}
	 	]
	};
})
.factory("MomentTypeEnum", function(){
	return {
		moments : [
	 	    {name:'Início', value:'START'},
	 	    {name:'Obrigatória (durante a funcionalidade)', value:'REQUIRED'},
	 	    {name:'Fim', value:'END'}
	 	]
	};
})
.factory("MostAccessTypeEnum", function(){
	return {
		datatypes : [
	 	    {name:'MetaDados', value:'sJhm', isIgnoreURL: true},
	 	    {name:'URL', value:'sUrl', isIgnoreURL: false}
	 	]
	};
})
.factory("SmellsMetricsEnum", function(){
	return {
		metrics : [
	 	    {name:'datamining.smells.testes.actioncount', value:'1'},
	 	    {name:'datamining.smells.testes.time', value:'2'},
	 	    {name:'datamining.smells.testes.cyclerate', value:'3'},
   			{name:'datamining.smells.testes.occurrencerate', value:'4'},
	 	    {name:'datamining.smells.testes.layercount', value:'5'},
	 	    {name:'datamining.smells.testes.attemptcount', value:'6'},
   			{name:'datamining.smells.testes.repetitioncount', value:'7'},
   			{name:'datamining.smells.testes.diffelementcount', value:'8'},
   			{name:'datamining.smells.testes.actioncountmobile', value:'9'}, //MILL
   			{name:'datamining.smells.testes.mobilecontext', value:'91'} //MILL
	 	]
	};
})
.factory("SmellsEnum", function(){
	return {
		smells : [
	 	    {name:'Laborious Task', value:'1'},
	 	    {name:'Cyclic Task', value:'2'},
	 	    {name:'Lonely Action', value:'3'},
	 	    {name:'Too Many Layers', value:'4'},
	 	    {name:'Undescriptive Element', value:'5'},
	 	    {name:'Missing Feedback', value:'6'},
	 	    {name:'Fat Interface', value:'7'}
	 	]
	};
})

//CAMBIOS MILL
.factory("TouchEventsEnum", function(){
	return {
		events : [
	 	    {name:'Double Tap', value:'doubletap'},
	 	    {name:'Single Tap', value:'singletap'},
	 	    {name:'Press', value:'press'},
	 	    {name:'Swipe Up', value:'swipeup'},
	 	    {name:'Swipe Down', value:'swipedown'},
	 	    {name:'Swipe Left', value:'swipeleft'},
	 	    {name:'Swipe Rigth', value:'swiperigth'},
	 	    {name:'Orientation Change', value:'orientationchange'},
	 	    {name:'Pinch In', value:'pinchin'},
	 	    {name:'Pinch Out', value:'pinchout'},
	 	    {name:'Pinch', value:'pinch'},
	 	    {name:'Swipe', value:'swipe'},
	 	    {name:'Focus', value:'focusout'}
	 	]
	};
})

.factory("TouchEventsDurationEnum", function(){
	return {
		durations : [
	 	    {name:'zero or more times', value:'*'},
	 	    {name:'one or more times', value:'+'},
	 	    {name:'no or one times', value:'?'},
	 	    {name:'X number of times', value:'{X}'},
	 	    {name:'between X and Y times', value:'{X,Y}'}
	 	]
	};
})
//FIN MILL

.controller('ErrorController', function($scope, cfpLoadingBar, config, env) {
	console.log('error controller');
	$scope.urlapp = config[env].apiUrl;
	cfpLoadingBar.complete();
})
.controller('InformationsController', function($scope, config, env) {
	console.log('info controller');
	$scope.urlapp = config[env].apiUrl;
})

//Smells Controllers
.controller('SmellsStatisticFormController', function(test, UtilsService, SmellsMetricsEnum, ServerAPI, $timeout, $filter) {
	var smellCtrl = this;
	smellCtrl.test = JSON.parse(test.data.string);
	console.log(smellCtrl.test);
	
	smellCtrl.minDate = new Date().getTime();
	smellCtrl.maxDate = new Date().getTime();
	
	smellCtrl.formatDate = UtilsService.formatDate;
	smellCtrl.momentTimestamp = UtilsService.momentTimestamp;
	
	smellCtrl.metrics = SmellsMetricsEnum.metrics;
	smellCtrl.metricsSelected = [];
	
	smellCtrl.useLiteral = false;
	smellCtrl.maxResultCount = 10;
	
	smellCtrl.ignoredUrls = [];
	
	for (var i = 0; i < Object.keys(SmellsMetricsEnum.metrics).length; i++) {
		/*smellCtrl.metricsSelected.push(i+1); MILL*/
		smellCtrl.metricsSelected.push(SmellsMetricsEnum.metrics[i].value);
	}
	
	var obj = angular.toJson({
		testId: smellCtrl.test.id
	});
	
	ServerAPI.getIgnoredUrls(obj).then(function(data) {			
		var ignoredUrls = JSON.parse(data.data.string);
		for (var i = 0; i < ignoredUrls.length; i++) {
			smellCtrl.ignoredUrls.push(ignoredUrls[i].url);
		}
	}, function(data) {
		console.error('ignoredUrls', data);
	});
	
	smellCtrl.addIgnoredUrl = function () {
		var obj = angular.toJson({
			url: {
				testId: smellCtrl.test.id,
				url: smellCtrl.ignoreUrl
			}, 
			remove: false
		});
		ServerAPI.updateIgnoredUrls(obj).then(function(data) {			
//			console.log(data);
		}, function(data) {
			console.error(data);
		});
		smellCtrl.ignoredUrls.push(smellCtrl.ignoreUrl);
		smellCtrl.ignoreUrl = "";
	}
	smellCtrl.removeIgnoredUrl = function (index) {
		var obj = angular.toJson({
			url: {
				testId: smellCtrl.test.id,
				url: smellCtrl.ignoredUrls[index]
			}, 
			remove: true
		});
		ServerAPI.updateIgnoredUrls(obj).then(function(data) {			
//			console.log(data);
		}, function(data) {
			console.error(data);
		});
		smellCtrl.ignoredUrls.splice(index, 1);
	}
	
	smellCtrl.view = function(){
		var minDate = UtilsService.datepickerToTimestamp(smellCtrl.minDate);
		var maxDate = UtilsService.datepickerToTimestamp(smellCtrl.maxDate);
		
		var obj = angular.toJson({
			test: smellCtrl.test,
			initDate: minDate,
			endDate: maxDate,
			metrics: smellCtrl.metricsSelected,
			useLiteral: smellCtrl.useLiteral,
			maxResultCount: smellCtrl.maxResultCount,
			ignoredUrls: smellCtrl.ignoredUrls
		});
		
		ServerAPI.viewSmellsStatistics(obj).then(function(data) {
			if (data.status == 200) {
				var charts = JSON.parse(data.data.string);
				smellCtrl.areaCharts = [];
				for (var i = 0; i < charts.areaCharts.length; i++) {
					var statistics = [];
					for (var d in charts.areaCharts[i].series) {
						var area = charts.areaCharts[i].series[d];
						var areaData = {
								"key" : area.description,
								"values" : [],
								"area" : true
						};
						for (var a in area.coordinates) {
							areaData.values.push({x: area.coordinates[a].x, y: area.coordinates[a].y});
						}
						statistics.push(areaData);
					}
					smellCtrl.areaCharts.push({
						"description" : charts.areaCharts[i].description,
						"statisticsOptions" : {
				            chart: {
				                type: 'lineChart',
				                height: 450,
				                margin : {
				                    top: 20,
				                    right: 20,
				                    bottom: 40,
				                    left: 55
				                },
				                x: function(d){ return d.x; },
				                y: function(d){ return d.y; },
				                useInteractiveGuideline: true,
//				                dispatch: {
//				                    stateChange: function(e){ console.log("stateChange"); },
//				                    changeState: function(e){ console.log("changeState"); },
//				                    tooltipShow: function(e){ console.log("tooltipShow"); },
//				                    tooltipHide: function(e){ console.log("tooltipHide"); }
//				                },
				                xAxis: {
				                    axisLabel: $filter('translate')(charts.areaCharts[i].descX)
				                },
				                yAxis: {
				                    axisLabel: $filter('translate')(charts.areaCharts[i].descY),
				                    tickFormat: function(d){
				                        return d3.format('.01f')(d);
				                    },
				                    axisLabelDistance: -10
				                }//,
//				                callback: function(chart){
//				                    console.log("!!! lineChart callback !!!");
//				                }
				            }//,
//				            title: {
//				                enable: true,
//				                text: charts[i].description
//				            },
//				            subtitle: {
//				                enable: true,
//				                text: 'Subtitle for simple line chart. Lorem ipsum dolor sit amet, at eam blandit sadipscing, vim adhuc sanctus disputando ex, cu usu affert alienum urbanitas.',
//				                css: {
//				                    'text-align': 'center',
//				                    'margin': '10px 13px 0px 7px'
//				                }
//				            },
//				            caption: {
//				                enable: true,
//				                html: '<b>Figure 1.</b> Lorem ipsum dolor sit amet, at eam blandit sadipscing, <span style="text-decoration: underline;">vim adhuc sanctus disputando ex</span>, cu usu affert alienum urbanitas. <i>Cum in purto erat, mea ne nominavi persecuti reformidans.</i> Docendi blandit abhorreant ea has, minim tantas alterum pro eu. <span style="color: darkred;">Exerci graeci ad vix, elit tacimates ea duo</span>. Id mel eruditi fuisset. Stet vidit patrioque in pro, eum ex veri verterem abhorreant, id unum oportere intellegam nec<sup>[1, <a href="https://github.com/krispo/angular-nvd3" target="_blank">2</a>, 3]</sup>.',
//				                css: {
//				                    'text-align': 'justify',
//				                    'margin': '10px 13px 0px 7px'
//				                }
//				            }
				        },
					   "statisticsData" : statistics
					});
				}
				
				smellCtrl.barCharts = [];
				for (var i = 0; i < charts.barCharts.length; i++) {
					var statistics = [];
					for (var d in charts.barCharts[i].series) {
						statistics.push({
							"label" : d,
							"value" : charts.barCharts[i].series[d]
						});
					}
					
					//MILL
					if (charts.barCharts[i].description ==  'datamining.smells.testes.actioncountmobile'){
						smellCtrl.barCharts.push({
							"description" : charts.barCharts[i].description,
							"statisticsOptions" : {
					            chart: {
					                type: 'discreteBarChart',
					                height: 450,
					                margin : {
					                    top: 20,
					                    right: 20,
					                    bottom: 50,
					                    left: 55
					                },
					                x: function(d){return d.label;},
					                //y: function(d){return d.value + (1e-10);},
					                y: function(d){return d.value;},
					                showValues: true,
					                valueFormat: function(d){
					                    //return d3.format(',.1f')(d);
					                    return d3.format(',.0f')(d);
					                },
					                duration: 500,
					                xAxis: {
					                    axisLabel: $filter('translate')(charts.barCharts[i].descX)
					                },
					                yAxis: {
					                    axisLabel: $filter('translate')(charts.barCharts[i].descY),
					                    axisLabelDistance: -10
					                },
					                showXAxis: true,
					                useInteractiveGuideline: false,
					                interactive: true
					            }
					        },
					        "statisticsData" : [{
					        	key : charts.barCharts[i].description,
					        	values : statistics
					        }]
						});
					}
					else if (charts.barCharts[i].description ==  'datamining.smells.testes.mobilecontext'){
					smellCtrl.barCharts.push({
						"description" : charts.barCharts[i].description,
						"statisticsOptions" : {
				            chart: {
				                type: 'discreteBarChart',
				                height: 450,
				                margin : {
				                    top: 20,
				                    right: 20,
				                    bottom: 50,
				                    left: 55
				                },
				                x: function(d){return d.label;},
				                y: function(d){return d.value + (1e-10);},
				                showValues: true,
				                valueFormat: function(d){
				                    return d3.format(',.1f')(d);
				                },
				                duration: 500,
				                xAxis: {
				                    axisLabel: $filter('translate')(charts.barCharts[i].descX)
				                },
				                yAxis: {
				                    axisLabel: $filter('translate')(charts.barCharts[i].descY),
				                    axisLabelDistance: -10
				                },
				                showXAxis: false,
				                useInteractiveGuideline: false,
				                interactive: true,
				                tooltip: {
				                	enabled: true,
				                	contentGenerator: function(d) {
				                		var actionAttributes;
				                		for (var i = 0; i < charts.barCharts.length; i++) {
				        					if(charts.barCharts[i].actionsAttributes[d.data.label] != undefined) {
				        						actionAttributes = charts.barCharts[i].actionsAttributes[d.data.label];
				        						break;
				        					}
				                		}
				                		var idElements = d.data.label.split(" | ");
				                		var html = '<div style="padding: 8px;"><b>' + $filter('translate')('datamining.smells.testes.mobiletype') + '</b>: ' + idElements[0];
				                		if (idElements[1] != "")
				                			html += '<br><b>' + $filter('translate')('datamining.smells.testes.mobileos') + '</b>: ' + idElements[1];
				                		if (idElements[2] != "")
				                			html += '<br><b>' + $filter('translate')('datamining.smells.testes.mobilebrowser') + '</b>: ' + idElements[2];
				                		if (idElements[3] != "")
				                			html += '<br><b>' + $filter('translate')('datamining.smells.testes.mobileresolution') + '</b>: ' + idElements[3];
				                		return html += '</div>';
				                	}
				                }
				            }
				        },
				        "statisticsData" : [{
				        	key : charts.barCharts[i].description,
				        	values : statistics
				        }]
					});
					}
					else {
					//FIN MILL
					
					smellCtrl.barCharts.push({
						"description" : charts.barCharts[i].description,
						"statisticsOptions" : {
				            chart: {
				                type: 'discreteBarChart',
				                height: 450,
				                margin : {
				                    top: 20,
				                    right: 20,
				                    bottom: 50,
				                    left: 55
				                },
				                x: function(d){return d.label;},
				                y: function(d){return d.value + (1e-10);},
				                showValues: true,
				                valueFormat: function(d){
				                    return d3.format(',.1f')(d);
				                },
				                duration: 500,
				                xAxis: {
				                    axisLabel: $filter('translate')(charts.barCharts[i].descX)
				                },
				                yAxis: {
				                    axisLabel: $filter('translate')(charts.barCharts[i].descY),
				                    axisLabelDistance: -10
				                },
				                showXAxis: false,
				                //showXAxis: true,
				                useInteractiveGuideline: false,
				                interactive: true,
				                tooltip: {
				                	enabled: true,
				                	contentGenerator: function(d) {
				                		var actionAttributes;
				                		for (var i = 0; i < charts.barCharts.length; i++) {
				        					if(charts.barCharts[i].actionsAttributes[d.data.label] != undefined) {
				        						actionAttributes = charts.barCharts[i].actionsAttributes[d.data.label];
				        						break;
				        					}
				                		}
				                		var idElements = d.data.label.split(" | ");
				                		var html = '<div style="padding: 8px;"><b>' + $filter('translate')('datamining.smells.testes.place') + '</b>: ' + idElements[0];
				                		if (idElements[1] != "")
				                			html += '<br><b>' + $filter('translate')('datamining.smells.testes.element') + '</b>: ' + idElements[1];
				                		if (idElements[2] != "")
				                			html += '<br><b>' + $filter('translate')('datamining.smells.testes.actiontype') + '</b>: ' + idElements[2];
				                		for (var attribute in actionAttributes)
				                			if (actionAttributes[attribute] != " ")
				                				html += '<br><b>' + $filter('translate')(attribute) + '</b>: ' + actionAttributes[attribute];
				                		return html += '</div>';
				                	}
				                }
				            }
				        },
				        "statisticsData" : [{
				        	key : charts.barCharts[i].description,
				        	values : statistics
				        }]
					});
					
					}
					
				}
//				$timeout(function(){
//					for (var i = 0; i < charts.length; i++) {
//						smellCtrl.charts[i].chartApi.refresh();
//					}
//				});
			}
		}, function(data) {
			console.log(data);
		});
		
	}
	
	smellCtrl.toggleSelection = function toggleSelection(id) {
		id = parseInt(id);
		var findIdx = -1;
		for (var i = 0; i < smellCtrl.metricsSelected.length; i++) {
			if (smellCtrl.metricsSelected[i] == id) {
				findIdx = i;
			}
		}
		if (findIdx == -1) {
			smellCtrl.metricsSelected.push(id);
		} else {
			smellCtrl.metricsSelected.splice(findIdx, 1);
		}
	};
	
	smellCtrl.setChartId = function (id) {
		smellCtrl.chartId = id;
		smellCtrl.actionUrl = "";
		smellCtrl.actionElement = "";
		smellCtrl.actionType = "";
	};
	
})
.controller('SmellsDetectionFormController', function(test, UtilsService, SmellsEnum, ServerAPI, $filter, $scope) {
	
	var smellCtrl = this;
	smellCtrl.test = JSON.parse(test.data.string);
	
	smellCtrl.minDate = new Date().getTime();
	smellCtrl.maxDate = new Date().getTime();
	
	smellCtrl.formatDate = UtilsService.formatDate;
	smellCtrl.momentTimestamp = UtilsService.momentTimestamp;
	
	smellCtrl.smells = SmellsEnum.smells;
	smellCtrl.smellsSelected = [];
	smellCtrl.tasksSelected = [];
	
	smellCtrl.groupSessions = false;
	smellCtrl.similarityRate = 60;
	
	smellCtrl.ignoredUrls = [];
	smellCtrl.nodesDesc = [];
	smellCtrl['smell-data-graph'] = [];
	
	for (var i = 0; i < Object.keys(SmellsEnum.smells).length; i++) {
		smellCtrl.smellsSelected.push(i+1);
	}
	for (var i = 0; i < smellCtrl.test.tasks.length; i++) {
		smellCtrl.tasksSelected.push(smellCtrl.test.tasks[i].id);
	}
	
	var obj = angular.toJson({
		testId: smellCtrl.test.id
	});
	
	ServerAPI.getIgnoredUrls(obj).then(function(data) {			
		var ignoredUrls = JSON.parse(data.data.string);
		for (var i = 0; i < ignoredUrls.length; i++) {
			smellCtrl.ignoredUrls.push(ignoredUrls[i].url);
		}
	}, function(data) {
		console.error('ignoredUrls', data);
	});
	
	smellCtrl.view = function(){
		var minDate = UtilsService.datepickerToTimestamp(smellCtrl.minDate);
		var maxDate = UtilsService.datepickerToTimestamp(smellCtrl.maxDate);
		
		var obj = angular.toJson({
			test: smellCtrl.test,
			initDate: minDate,
			endDate: maxDate,
			smells: smellCtrl.smellsSelected,
			tasks: smellCtrl.tasksSelected,
			groupSessions: smellCtrl.groupSessions,
			similarityRate: smellCtrl.similarityRate/100,
			ignoredUrls: smellCtrl.ignoredUrls
		});
		
		ServerAPI.detectSmells(obj).then(function(data) {			
			smellCtrl.detectionResult = JSON.parse(data.data.string);
		}, function(data) {
			console.log(data);
		});
		
	}
	
	smellCtrl.showGraphSmell = function(smellName, taskName, session, index) {
		var elemId = 'graph-'+ smellCtrl.formattedId(smellName, taskName, session.description);
		var graphData = generateGraphSmells(session.graph);
//		smellCtrl['smellgraphid'] = elemId;
		smellCtrl.nodesDesc[index] = null;
		smellCtrl['smell-data-graph'][index] = drawGraph(elemId, graphData, [], {}, {}, 5, false);
//		smellCtrl['smell-data-nodes'] = session.graph;
		smellCtrl.resetGraph(index);
	}
	
	smellCtrl.resetGraph = function(index) {
		smellCtrl['smell-data-graph'][index].on("selectNode", function (params) {
      	  	var node = smellCtrl['smell-data-graph'][index].body.nodes[params.nodes[0]];
      	  	var nodeDesc = node.options.desc.split(" | ");
      	  	smellCtrl.nodesDesc[index] = {
      	  			place: nodeDesc[0],
      	  			element: nodeDesc[1],
      	  			actiontype: nodeDesc[2],
      	  			occurrences: node.options.value
      	  	}
      	  	$scope.$apply();
        });
		smellCtrl['smell-data-graph'][index].on("selectEdge", function (params) {
    		var edge = smellCtrl['smell-data-graph'][index].body.edges[params.edges[0]];
    		var from = smellCtrl['smell-data-graph'][index].body.nodes[edge.options.from].options.desc.split(" | ");
    		var to = smellCtrl['smell-data-graph'][index].body.nodes[edge.options.to].options.desc.split(" | ");
    		smellCtrl.nodesDesc[index] = {
      	  			place: from[0] + ' --> ' + to[0],
      	  			element: from[1] + ' --> ' + to[1],
      	  			actiontype: from[2] + ' --> ' + to[2],
      	  			occurrences: edge.options.value
      	  	}
    		$scope.$apply();
        });
	}
	
	smellCtrl.toggleSelection = function toggleSelection(id) {
		id = parseInt(id);
		var findIdx = -1;
		for (var i = 0; i < smellCtrl.smellsSelected.length; i++) {
			if (smellCtrl.smellsSelected[i] == id) {
				findIdx = i;
			}
		}
		if (findIdx == -1) {
			smellCtrl.smellsSelected.push(id);
		} else {
			smellCtrl.smellsSelected.splice(findIdx, 1);
		}
	};
	
	smellCtrl.toggleTaskSelection = function toggleSelection(id) {
		id = parseInt(id);
		var findIdx = -1;
		for (var i = 0; i < smellCtrl.tasksSelected.length; i++) {
			if (smellCtrl.tasksSelected[i] == id) {
				findIdx = i;
			}
		}
		if (findIdx == -1) {
			smellCtrl.tasksSelected.push(id);
		} else {
			smellCtrl.tasksSelected.splice(findIdx, 1);
		}
	};
	
	smellCtrl.formattedRate = function (rate) {
		return (rate*100).toFixed(2) + "%";
	}
	
	smellCtrl.smellInfo = function (smell) {
		return 'datamining.smells.testes.detection.' + smell.replace(/ /g, '').toLowerCase() + '.info';
	}
	
	smellCtrl.smellHint = function (smell) {
		return 'datamining.smells.testes.detection.hint.' + smell.replace(/ /g, '').toLowerCase();
	}
	
	smellCtrl.smellProblems = function (smell) {
		var i = 1;
		var problem = 'datamining.smells.testes.detection.problem' + i + '.' + smell.replace(/ /g, '').toLowerCase();
		var problems = [];
		while (problem != $filter('translate')(problem)) {
			problems.push(problem);
			i++;
			var problem = 'datamining.smells.testes.detection.problem' + i + '.' + smell.replace(/ /g, '').toLowerCase();
		}
		return problems;
	}
	
	smellCtrl.actionPlace = function (action) {
		return action.split(" | ")[0];
	}
	
	smellCtrl.actionElement = function (action) {
		return action.split(" | ")[1];
	}
	
	smellCtrl.actionType = function (action) {
		return action.split(" | ")[2];
	}
	
	smellCtrl.problemTitle = function (problem) {
		return $filter('translate')(problem).split(":")[0];
	}
	
	smellCtrl.problemDescription = function (problem) {
		return $filter('translate')(problem).split(":")[1];
	}
	
	smellCtrl.formattedId = function (smell, task, session) {
		return (smell + '-' + task + '-' + session).replace(/[{()}\. ]/g, '');
	}
	
	smellCtrl.addIgnoredUrl = function () {
		var obj = angular.toJson({
			url: {
				testId: smellCtrl.test.id,
				url: smellCtrl.ignoreUrl
			}, 
			remove: false
		});
		ServerAPI.updateIgnoredUrls(obj).then(function(data) {			
//			console.log(data);
		}, function(data) {
			console.error(data);
		});
		smellCtrl.ignoredUrls.push(smellCtrl.ignoreUrl);
		smellCtrl.ignoreUrl = "";
	}
	smellCtrl.removeIgnoredUrl = function (index) {
		var obj = angular.toJson({
			url: {
				testId: smellCtrl.test.id,
				url: smellCtrl.ignoredUrls[index]
			}, 
			remove: true
		});
		ServerAPI.updateIgnoredUrls(obj).then(function(data) {			
//			console.log(data);
		}, function(data) {
			console.error(data);
		});
		smellCtrl.ignoredUrls.splice(index, 1);
	}
	
	smellCtrl.objectSize = function (object) {
		return Object.keys(object).length;
	}
})
.controller('SmellConfigController', function(parameters, idSmell, UtilsService, SmellsEnum, ServerAPI) {
	var smellCtrl = this;
	var smell = null;
	
	for (var i in SmellsEnum.smells) {
		if (SmellsEnum.smells[i].value == idSmell) {
			smell = SmellsEnum.smells[i];
			break;
		}
	}
	
	smellCtrl.smell = smell;
	smellCtrl.parameters = JSON.parse(parameters.data.string);
	
	smellCtrl.changeValueParam = function(){
		var obj = angular.toJson({
			parameters: smellCtrl.parameters
		});
		ServerAPI.changeSmellParameter(obj).then(function(data) {
//			console.log(data);
		}, function(data) {
			console.error(data);
		});
	}
	
	smellCtrl.redefineDefault = function(){
		for (var i = 0; i < smellCtrl.parameters.length; i++) {
			smellCtrl.parameters[i].value = -1;
		}
	}
	
})

/*CAMBIOS MILL*/

.controller('SaveSmellsMobileController', function($scope, $filter, smell, test, ServerAPI, ShowMessage, ValidateService) {
	
	var smellCtrl = this;
	smellCtrl.smell = smell;
	smellCtrl.test = test;
	
	smellCtrl.save = function() {		
		var smell = angular.toJson({
			'smell' : this.smell
		});		
		if (ValidateService.smell(smellCtrl.smell)) {
			ServerAPI.saveSmellMobile(smell).then(function(data) {	
				ShowMessage.success('datamining.tasks.new.success');
			}, function(data) {
				console.log(data);
			});
			
		} else {
			ShowMessage.error('datamining.validate.default');
		}		
	};		
	
	smellCtrl.incluir = function (details) {
		var len = details.length;
		var ultIdSmellPattern = details[len -1].idSmellPattern;
		var detail={ idSmellPattern:ultIdSmellPattern,
					sEvent:"",
			      	sQuantity:""
			    	} ;
		smellCtrl.smell.detail.push(detail);
    };
    
    smellCtrl.incluirPadrao = function (details) {
    	var len = details.length;
		var ultIdSmellPattern = parseInt(details[len -1].idSmellPattern) + 1;		
		var detail={ idSmellPattern:ultIdSmellPattern,
					sEvent:"",
			      	sQuantity:""
			    	} ;
		smellCtrl.smell.detail.push(detail);
    };
	
	smellCtrl.removeRow = function (details) {
        var detailArray = [];
        angular.forEach(details, function (value) {
            if (!value.Remove) {
                detailArray.push(value);
            }
        });
        smellCtrl.smell.detail = detailArray;
    };    

	smellCtrl.gestures = '';	
	smellCtrl.durations = '';
	/*smellCtrl.gestures = TouchEventsEnum.events;	
	smellCtrl.durations = TouchEventsDurationEnum.durations;*/	
	
})

.controller('NewSmellsMobileController', function($scope, test, $controller, $filter, TouchEventsEnum, TouchEventsDurationEnum) {
	
	var smellCtrl = this;	
	smellCtrl.actionTitle = $filter('translate')('datamining.smells.testes.mobile.new');		
	var details=[{idSmellPattern:"1", sEvent:"", sQuantity:""}];	
	smellCtrl.smell = { sName:"", sContent:"", detail:[]} 	
	smellCtrl.smell.detail = details;		
	/*angular.extend(this, $controller('SaveSmellsMobileController', {$scope: $scope, smell: smellCtrl.smell}));*/
	angular.extend(this, $controller('SaveSmellsMobileController', {$scope: $scope, smell: smellCtrl.smell, test: test}));
})

.controller('EditSmellsMobileController', function($scope, smell, test, $controller, $filter, TouchEventsEnum,TouchEventsDurationEnum) {
	var smellCtrl = this;
	smellCtrl.smell = JSON.parse(smell.data.string);
	smellCtrl.actionTitle = $filter('translate')('datamining.smells.testes.mobile.edit');	
	angular.extend(this, $controller('SaveSmellsMobileController', {$scope: $scope, smell: JSON.parse(smell.data.string), test: test}));	
})

.controller('SmellsMobileController', function(test, smells, UtilsService, SmellsEnum, ServerAPI, $filter, $scope) {
	var smellCtrl = this;
	smellCtrl.test = JSON.parse(test.data.string);
	smellCtrl.smells = JSON.parse(smells.data.string);
	smellCtrl.showSmellsMobile = false;			
})

.controller('SmellsDetectionMobileController', function(test, smells, UtilsService, ServerAPI, $filter, $scope) {
	var smellCtrl = this;
	smellCtrl.test = JSON.parse(test.data.string);
	smellCtrl.smells = JSON.parse(smells.data.string);
	smellCtrl.showSmellsMobile = false;
	
	smellCtrl.smellsSelected = [];
	smellCtrl.tasksSelected = [];
	smellCtrl.nodesDesc = [];
	smellCtrl.nodesDescTime = [];
	smellCtrl.showlist = [];

	smellCtrl['smell-data-graph-time'] = [];
	smellCtrl['smell-data-graph'] = [];
	
	smellCtrl.tamMin = 4;
	smellCtrl.tamMax = 5;
	smellCtrl.cantRep = 3;
	
	for (var i = 0; i < smellCtrl.smells.length; i++) {
		smellCtrl.smellsSelected.push(smellCtrl.smells[i].id);
	}
	for (var i = 0; i < smellCtrl.test.tasks.length; i++) {
		smellCtrl.tasksSelected.push(smellCtrl.test.tasks[i].id);
	}
	
	smellCtrl.toggleSelection = function toggleSelection(id) {
		id = parseInt(id);
		var findIdx = -1;
		for (var i = 0; i < smellCtrl.smellsSelected.length; i++) {
			if (smellCtrl.smellsSelected[i] == id) {
				findIdx = i;
			}
		}
		if (findIdx == -1) {
			smellCtrl.smellsSelected.push(id);
		} else {
			smellCtrl.smellsSelected.splice(findIdx, 1);
		}
	};
	
	smellCtrl.toggleTaskSelection = function toggleSelection(id) {
		id = parseInt(id);
		var findIdx = -1;
		for (var i = 0; i < smellCtrl.tasksSelected.length; i++) {
			if (smellCtrl.tasksSelected[i] == id) {
				findIdx = i;
			}
		}
		if (findIdx == -1) {
			smellCtrl.tasksSelected.push(id);
		} else {
			smellCtrl.tasksSelected.splice(findIdx, 1);
		}
	};
	
	smellCtrl.view = function(){
		var selectedSmellsPrincipal = document.getElementById("selectedSmellsPrincipal").checked;
		var minDate = UtilsService.datepickerToTimestamp(smellCtrl.minDate);
		var maxDate = UtilsService.datepickerToTimestamp(smellCtrl.maxDate);
		
		var obj = angular.toJson({
			test: smellCtrl.test,
			initDate: minDate,
			endDate: maxDate,
			cantRep: smellCtrl.cantRep,
			tamMin: smellCtrl.tamMin,
			tamMax: smellCtrl.tamMax,	
			selectedSmells: smellCtrl.smellsSelected,
			selectedTasks: smellCtrl.tasksSelected,
			smellGeneral: selectedSmellsPrincipal
		});
		
		ServerAPI.detectSmellMobile(obj).then(function(data) {			
			smellCtrl.detectionResult = JSON.parse(data.data.string);
		}, function(data) {
			console.log(data);
		});				
	}
	
	smellCtrl.formattedId = function (smell, task, session) {
		return (smell + '-' + task + '-' + session).replace(/[{()}\. ]/g, '');
	}
	
	smellCtrl.formattedId2 = function (smell, task, session) {
		var smellsParse = JSON.parse(smell);
		return (smellsParse.sName + '-' + task + '-' + session).replace(/[{()}\. ]/g, '');
	}
	
	smellCtrl.formattedId3 = function (smell, task, session) {
		var smellsParse = JSON.parse(smell);
		var retorna = (smellsParse.sName + '-' + task + '-' + session).replace(/[{()}\. ]/g, '');
		var retorna2 = smellCtrl.formattedWithOutSpaces(retorna);
		retorna2 = retorna2.replace(/[@\. ]/g, '');
		return retorna2;
	}
	
	smellCtrl.formattedWithOutSpaces = function (patron){
		var patron2 = patron.replace(/\//g, "");
		var patron3 = patron2.replace(/:/g, "");
		var patron4 = patron3.replace(/\./g, "");
		var patron5 = patron4.replace(/[{()}\. ]/g, '');
		return (patron5.replace(/ /g, ""));
	}
	
	smellCtrl.formattedPattern = function (patron, opcion) {				
		var valores = patron.split("-")		
		return valores[opcion];		
	}
	
	smellCtrl.totalAction = function (listActions) {
		var total = 0;
		for( index in listActions){
			total += 1;
		}
		return total;
	}
	
	smellCtrl.smellName = function (smell) {
		var smellsParse = JSON.parse(smell);		
		return smellsParse.sName;
	}
	
	smellCtrl.smellContent = function (smell) {
		var smellsParse = JSON.parse(smell);		
		return smellsParse.sContent;
	}
	
	smellCtrl.smellPattern = function (smell) {
		var smellsParse = JSON.parse(smell);		
		return smellsParse.pattern;
	}
	
	smellCtrl.getUser = function (listAction) {
		var user = listAction[0].sUsername;
		return user;
	}
	
	smellCtrl.getContexto = function (listAction) {
		var contexto = listAction[0].sMobileConf;
		return contexto;
	}
	
	smellCtrl.showGraphSmell = function(smell, taskName, session, index) {
		var elemId = 'graph-'+ smellCtrl.formattedId3(smell, taskName, session.description)
		var graphData = generateGraphSmells(session.graph);
		smellCtrl.nodesDesc[index] = null;
		smellCtrl['smell-data-graph'][index] = drawGraph(elemId, graphData, [], {}, {}, 5, false);
		smellCtrl.resetGraph(index);
	}
	smellCtrl.resetGraph = function(index) {
		smellCtrl['smell-data-graph'][index].on("selectNode", function (params) {
      	  	var node = smellCtrl['smell-data-graph'][index].body.nodes[params.nodes[0]];
      	  	var nodeDesc = node.options.desc.split(" | ");
      	  	smellCtrl.nodesDesc[index] = {
      	  			place: nodeDesc[0],
      	  			element: nodeDesc[1],
      	  			actiontype: nodeDesc[2],
      	  			occurrences: node.options.value
      	  	}
      	  	$scope.$apply();
        });
		smellCtrl['smell-data-graph'][index].on("selectEdge", function (params) {
    		var edge = smellCtrl['smell-data-graph'][index].body.edges[params.edges[0]];
    		var from = smellCtrl['smell-data-graph'][index].body.nodes[edge.options.from].options.desc.split(" | ");
    		var to = smellCtrl['smell-data-graph'][index].body.nodes[edge.options.to].options.desc.split(" | ");
    		smellCtrl.nodesDesc[index] = {
      	  			place: from[0] + ' --> ' + to[0],
      	  			element: from[1] + ' --> ' + to[1],
      	  			actiontype: from[2] + ' --> ' + to[2],
      	  			occurrences: edge.options.value
      	  	}
    		$scope.$apply();
        });
	}
	
	smellCtrl.showTimelineSmell = function(smell, taskName, session, index, listeventos) {			
		var description = 'panelVis-' + smellCtrl.formattedId3(smell, taskName, session.description);		
		var container = document.getElementById(description);
		
		if(container !== null){
	        while (container.hasChildNodes()){
	        	container.removeChild(container.lastChild);
	        }
	    }	
		var data = new vis.DataSet();
		var options = {
				  width: '100%',
				  /*autoResize: 'true'*/
				  height: '300px'/*,
				  margin: {
				    item: 20
				  }*/
				};
		var dataSelected = new Array();
		var eventosArr = null;
		var sizePattern = 0;
		
		data.on('*', function (event, properties, senderId) {
		    console.log('event:', event, 'properties:', properties, 'senderId:', senderId);
		  });
		  
		for (var i = 0; i < session.session.actions.length; i++) {			
			var date = moment(session.session.actions[i].time).format("YYYY-MM-DD HH:mm:ss");
			//data.add({id: i+1, content: session.session.actions[i].action, start: date });
			/*var duracion = session.session.actions[i].sDuration;
			var dateFinal = moment(session.session.actions[i].time+duracion).format("YYYY-MM-DD HH:mm:ss");
			if (duracion !=null &&duracion !=0){
				data.add({id: i+1, content: session.session.actions[i].action, start: date , end: dateFinal});
			}else{
				data.add({id: i+1, content: session.session.actions[i].action, start: date });
			}  */
			var info = "Clase:" + session.session.actions[i].sClass + ", Elemento:" + session.session.actions[i].element + ", Contenido:" + session.session.actions[i].infoContent+ ", Tag:" + session.session.actions[i].infoTag;
	  		data.add({id: i+1, content: session.session.actions[i].action, start: date, title: info });
		}
		for (var i = 0; i < listeventos.length; i++) {		
			if ((listeventos[i])[session.session.id]!= null ){
				eventosArr = (listeventos[i])[session.session.id].split(',');
				for (var i = 0; i < eventosArr.length; i += 2) {
					if (!isNaN(parseInt(eventosArr[i]))) {
						sizePattern = parseInt(eventosArr[i+1]) - parseInt(eventosArr[i]) +1;
						for (var l = 1; l < sizePattern+1; l++) {
							dataSelected.push(parseInt(eventosArr[i])+l);
						}
					}
				}
			}				
		}
		// Create a Timeline
		var timeline = new vis.Timeline(container, data, options);
		timeline.setSelection(dataSelected, {focus: true, animation:true});  
		smellCtrl['smell-data-graph-time'][index] = drawGraph(container, null, [], {}, {}, 5, false);
		smellCtrl.resetGraphTime(index);
	}
	
	smellCtrl.resetGraphTime = function(index) {
		smellCtrl['smell-data-graph-time'][index].on("selectNode", function (params) {
      	  	var node = smellCtrl['smell-data-graph-time'][index].body.nodes[params.nodes[0]];
      	  	var nodeDesc = node.options.desc.split(" | ");
      	  	smellCtrl.nodesDesc[index] = {
      	  			place: nodeDesc[0],
      	  			element: nodeDesc[1],
      	  			actiontype: nodeDesc[2],
      	  			occurrences: node.options.value
      	  	}
      	  	$scope.$apply();
        });
		smellCtrl['smell-data-graph-time'][index].on("selectEdge", function (params) {
    		var edge = smellCtrl['smell-data-graph-time'][index].body.edges[params.edges[0]];
    		var from = smellCtrl['smell-data-graph-time'][index].body.nodes[edge.options.from].options.desc.split(" | ");
    		var to = smellCtrl['smell-data-graph-time'][index].body.nodes[edge.options.to].options.desc.split(" | ");
    		smellCtrl.nodesDesc[index] = {
      	  			place: from[0] + ' --> ' + to[0],
      	  			element: from[1] + ' --> ' + to[1],
      	  			actiontype: from[2] + ' --> ' + to[2],
      	  			occurrences: edge.options.value
      	  	}
    		$scope.$apply();
        });
	}
		
	smellCtrl.showTimelinePattern = function(patron, listActions,listEventosAction) {			
		var arrayDeCadenas = patron.split("-");
		var sizePattern = 0;
		var a, b = null;
		var c = null;
		
		/*for (var l = 0; l < arrayDeCadenas.length-1; l++) {
			if (arrayDeCadenas[l].indexOf("(") != -1){
				a = arrayDeCadenas[l].indexOf("(");
				b = arrayDeCadenas[l].indexOf(")")
				c = arrayDeCadenas[l].substring(a+1, b);
				sizePattern += parseInt(c);

			} else {
				sizePattern += 1;
			}
		}	*/
		sizePattern = parseInt(arrayDeCadenas[2]);
		
		for( index in listActions){
			var options = {};
			var dataSelected = new Array();
			var data = new vis.DataSet();
			data.on('*', function (event, properties, senderId) {
			    console.log('event:', event, 'properties:', properties, 'senderId:', senderId);
			  });
			
			var container = document.getElementById(patron+index);
			if(container !== null){
		        while (container.hasChildNodes()){
		        	container.removeChild(container.lastChild);
		        }
		    }	
			var listAction = listActions[index];
			var listEventoAc = listEventosAction[index];
			for (var i = 0; i < listAction.length; i++) {	
					var date = moment(listAction[i].sTime).format("YYYY-MM-DD HH:mm:ss");
					var duracion = listAction[i].sDuration;
					var dateFinal = moment(listAction[i].sTime+duracion).format("YYYY-MM-DD HH:mm:ss");
					/*if (duracion !=0){
						data.add({id: i+1, content: listAction[i].sActionType, start: date , end: dateFinal});
					}else{
						data.add({id: i+1, content: listAction[i].sActionType, start: date });
					} */
					var info = "Clase:" + listAction[i].sClass + ", Elemento:" + listAction[i].sXPath + ", Contenido:" + listAction[i].Content + ", Tag:" + listAction[i].sTag;
      	  			data.add({id: i+1, content: listAction[i].sActionType, start: date, title: info });
					for (var j = 0; j < listEventoAc.length; j++) {
						if (listAction[i].id==listEventoAc[j])
							for (var l = 1; l < sizePattern+1; l++) {
								dataSelected.push(i+l);
							}
					}
			}
			var timeline = new vis.Timeline(container, data, options);
			timeline.setSelection(dataSelected, {focus: true, animation:true});  	 
		}
	}	
	
	smellCtrl.exportExcel = function(resultSmellAnalysisGeneral){		
		var csvData = '';
		for (var i = 0; i < resultSmellAnalysisGeneral.length; i++) {
			var patronTotal = resultSmellAnalysisGeneral[i].patron;
			var valores = patronTotal.split("-")
			var patron = valores[0];
			var url = valores[1];
			var tamano = valores[2];
			var total = smellCtrl.totalAction(resultSmellAnalysisGeneral[i].listActions);
			var row = url + ';' + patron + ';' + tamano + ';' + total;		 
			csvData += row + '\r\n';
		}
		var a = document.createElement("a");
	    a.setAttribute('style', 'display:none;');
	    document.body.appendChild(a);
	    var blob = new Blob([csvData], { type: 'text/csv' });
	    var url= window.URL.createObjectURL(blob);
	    a.href = url;
	    a.download = 'ExportResultGeneral.csv';
	    a.click();		
	}
	
    smellCtrl.exportExcel2 = function(tasksMobileAnalysisResult){		
		var csvData = '';
		for( index in tasksMobileAnalysisResult){	
			var smellName = smellCtrl.smellName(index);
			var result = tasksMobileAnalysisResult[index];				
			for (var i = 0; i < result.length; i++) {
				var task = result[i];
				var url = task.name;
				var patron = task.patron;				
				for (var k = 0; k < task.sessions.length; k++) {
					var actions = task.sessions[k];
					var username = actions.description;
					var info = actions.info["datamining.smells.testes.mobile.contexto"];					
					var info2 = actions.info["datamining.smells.testes.actioncount"];					
					var info3 = actions.info["datamining.smells.testes.mobile.usuario"];					
					for (var j = 0; j < actions.session.actions.length; j++) {
						var data = actions.session.actions[j]; 
						var row = smellName + ';' + patron +';' + url +';'+data.id+';'+username+';'+ info +';'+data.action+';'+data.element+';'+/*data.infoContent+';'+ */data.infoTag+ ';'+data.sClass+ ';'+ data.sDuration+';'+ data.infoPosX+';'+data.infoPosY+ ';'+ data.time;		 
						csvData += row + '\r\n';
					}
				}
			}
		}		
		var a = document.createElement("a");
	    a.setAttribute('style', 'display:none;');
	    document.body.appendChild(a);
	    var blob = new Blob([csvData], { type: 'text/csv' });
	    var url= window.URL.createObjectURL(blob);
	    a.href = url;
	    a.download = 'ExportResult.csv';
	    a.click();		
	}
})
/*FIN MILL*/


//Tests Controllers

.controller('TestController', function(tests) {
	var testCtrl = this;
	testCtrl.tests = JSON.parse(tests.data.string);
})
.controller('TestViewController', function(test, ServerAPI, $route, $rootScope, $filter) {
	var testCtrl = this;
	testCtrl.test = JSON.parse(test.data.string);
})
.controller('TestTasksDatesViewController', function($filter, test, ServerAPI, UtilsService) {
	var testCtrl = this;
	testCtrl.test = JSON.parse(test.data.string);
	console.log(testCtrl.test);
	testCtrl.actionTitle = $filter('translate')('datamining.testes.evaluations');
	
	testCtrl.showTasks = false;
	testCtrl.showEvaluations = false;
	testCtrl.showHidden = false;
	
	testCtrl.minDate = new Date().getTime();
	testCtrl.maxDate = new Date().getTime();
	
	testCtrl.formatDate = UtilsService.formatDate;
	testCtrl.momentTimestamp = UtilsService.momentTimestamp;
	
	for (var e in testCtrl.test.evaluations) {
		var eval = testCtrl.test.evaluations[e];
		eval.initTimestamp = testCtrl.momentTimestamp(eval.initDate);
		eval.lastTimestamp = testCtrl.momentTimestamp(eval.lastDate);
	}
	
	testCtrl.createDates = function(){
		var minDate = UtilsService.datepickerToTimestamp(testCtrl.minDate);
		var maxDate = UtilsService.datepickerToTimestamp(testCtrl.maxDate);
		
		var newEval = angular.toJson({
			idTeste: testCtrl.test.id, 
			initDate: minDate, 
			endDate: maxDate
		});
		
		ServerAPI.saveNewEvaluationTest(newEval).then(function(data) {
			console.log(data);
			testCtrl.test = JSON.parse(data.data.string);
		}, function(data) {
			console.log(data);
		});
	}
	
	testCtrl.updateTasksControl = function(){
		ServerAPI.updateTaskControl(testCtrl.test.id).then(function(data){
			console.log(data);
			testCtrl.test = JSON.parse(data.data.string);
		}, function(data) {
			console.log(data);
		});
	}
	
	testCtrl.toogleHidden = function(eval){
		ServerAPI.changeActivateEvaluationTest(testCtrl.test.id, eval.id).then(function(data){
			var newEval = JSON.parse(data.data.string);
			console.log(newEval);
			eval.isHidden = newEval.isHidden;
		}, function(data) {
			console.log(data);
		});
	}
	
})
.controller('TestTasksViewController', function(test, evalTest, ServerAPI, $route, $scope, $rootScope, $filter, UtilsService) {
	var testCtrl = this;
	
	testCtrl.test = JSON.parse(test.data.string);
	testCtrl.evalTest = JSON.parse(evalTest.data.string);
	
	angular.forEach(testCtrl.evalTest.evaluationsTask, function(eval){
		angular.forEach(testCtrl.test.tasks, function(task){
			if (task.id == eval.idTaskDataMining) {
				task.actualEvaluation = eval;
			}
		});
	});
	
	testCtrl.formatDate = UtilsService.formatDate;
	
	testCtrl.popup = {
		  options: {
			  html: true,
			  trigger: 'hover',
			  placement: 'bottom'
		  }
	};
	
	angular.forEach(testCtrl.test.tasks, function(task){
		task.popovercontent = contentPopover(task);
	});
	
	function reduceNumber(n){
		return $filter('number')(n, 2);
	}
	
	//TODO: melhoria no método, renderizando um popup com html
	function contentPopover(task){
		var content = "";
		task = task.actualEvaluation;
		if (task) {
			content = 'Data da Avaliação = '+UtilsService.formatDate(task.evalLastDate)+'<br/>';
			content += 'Média de Ações = '+reduceNumber(task.evalMeanActions)+' [z='+reduceNumber(task.evalZScoreActions)+']<br/>';
			content += 'Média de Tempos = '+reduceNumber(task.evalMeanTimes)+', [z='+reduceNumber(task.evalZScoreTime)+']<br/>';
			content += 'Completude = '+reduceNumber(task.evalMeanCompletion)+', Exatidão = '+reduceNumber(task.evalMeanCorrectness)+'<br/>';
			
			content += 'Sessões = '+reduceNumber(task.evalCountSessions)+', ['+reduceNumber(task.evalCountSessionsNormalized)+']<br/>';
			//content += 'Eficácia = (Completude * Exatidão)/100<br/>';
			content += 'Eficácia = '+reduceNumber(task.evalEffectiveness)+', ['+reduceNumber(task.evalEffectivenessNormalized)+']<br/>';
			//content += 'Eficiência = Eficácia / ((AçõesZscore + TemposZscore)/2)<br/>';
			content += 'Eficiência = '+reduceNumber(task.evalEfficiency)+', ['+reduceNumber(task.evalEfficiencyNormalized)+']<br/>';
			content += 'Prioridade (Fuzzy) = '+reduceNumber(task.evalFuzzyPriority)+'<br/>';
			content += "Tempo Estimado da Avaliação = " + $filter('msConverter')(task.meanTimeLoading || 0);
		} else {
			content = $filter('translate')('datamining.testes.evaluations.none');
		}
		return content;
	}
	
	$scope.setLongLoaderTime = function(meanTimeLoading){
		if (meanTimeLoading) {
			$rootScope.longLoadingTime = meanTimeLoading/1000;
		}
	}
})
.controller('TestMostAccessController', function($scope, $rootScope, $filter, $timeout, test, MostAccessTypeEnum, ServerAPI, UtilsService) {
	var testCtrl = this;
	testCtrl.actionTitle = $filter('translate')('datamining.testes.featuresmostaccessed.search');
	testCtrl.test = JSON.parse(test.data.string);
	testCtrl.testId = testCtrl.test.id;
	
	testCtrl.minDate = new Date().getTime();
	testCtrl.maxDate = new Date().getTime();
	
	testCtrl.actions = null;
	testCtrl.datatypes = MostAccessTypeEnum.datatypes;
	
	//definir tipo inicial
	for (var d in MostAccessTypeEnum.datatypes) {
		var datatype =  MostAccessTypeEnum.datatypes[d],
			isIgnoreURL = datatype.isIgnoreURL;
		if (testCtrl.test.isIgnoreURL && isIgnoreURL) {
			testCtrl.datatype = datatype;
		} else if (!testCtrl.test.isIgnoreURL && !isIgnoreURL) {
			testCtrl.datatype = datatype;
		}
	}
	
	
	
	testCtrl.mostaccess = function() {
		
		var minDate = UtilsService.datepickerToTimestamp(testCtrl.minDate);
		var maxDate = UtilsService.datepickerToTimestamp(testCtrl.maxDate);
		
		if (testCtrl.datatype !== undefined) {
			console.log(testCtrl.testId, testCtrl.datatype.value, new Date(minDate), new Date(maxDate));
			ServerAPI.getTestMostAccess(testCtrl.testId, testCtrl.datatype.value, minDate, maxDate).then(function(data){
				console.log(data.data);
				testCtrl.actions = JSON.parse(data.data.string);
				
				//sort actions desc
				testCtrl.actions.sort(function(a, b) {
				    return b.count - a.count;
				});
				
				var idx = 0;
				for (var a in testCtrl.actions) {
					idx++;
					testCtrl.actions[a].pos = idx;
					if (testCtrl.actions[a].description == '-') {
						testCtrl.actions[a].description = 'Início (-)'
					}
				}
				
				$scope.pieOptions = {
		            chart: {
		                type: 'pieChart',
		                height: 300,
		                x: function(d){
		                	return d.description;
		                },
		                y: function(d){
		                	return d.count;
		                },
		                //color: ['#0D3C55', '#0F5B78', '#117899', '#1395BA', '#5CA793', '#A5A8AA'],
		                showLabels: false,
		                transitionDuration: 500,
		                labelThreshold: 0.01,
		                showLegend: true,
		                legend: {
		                    margin: {
		                        top: 5,
		                        right: 35,
		                        bottom: 5,
		                        left: 0
		                    }
		                },
		                tooltip: {
		                	contentGenerator: function(obj) {
		                        return '<h4>' + obj.data.description + '</h4>' +'<p>' + obj.data.count + '</p>';
		                    }
		                },
		            }
		        };
				
				var MIN = 3, MAX = 25;
				$scope.sizePie = 10;
				$scope.canChange = true;
				
				$scope.renderPieMost = function(size) {
					size = size == null ? 5 : size;
					size = size < MIN ? MIN : size;
					size = size > MAX ? MAX : size;
					
					if ($scope.canChange) {
						$scope.canChange = false;
						//best 5
						$scope.pieData = testCtrl.actions.slice(0,size);
						
						//others
						var sum = 0;
						angular.forEach(testCtrl.actions.slice(size), function(elem){
							sum += elem.count;
						});
						$scope.pieData.push({
							description: $filter('translate')('others...'), 
							count: sum
						});
						
						//rerender
						$timeout(function(){
							$scope.pieApi.refresh();
							$timeout(function(){ $scope.canChange = true; }, 1500);
						});
					}
				}
				
				$scope.renderPieMost($scope.sizePie);
				
				
			}, function(data){
				console.log(data);
			});
		} else {
			$rootScope.errors = [];
    		$rootScope.errors.push('Selecione o tipo de "Campo Base da Busca" antes de avançar!');
    		return;
		}
	}
	
})
.controller('TestSpecificSessionController', function($filter, test, MostAccessTypeEnum, ServerAPI, UtilsService) {
	var testCtrl = this;
	testCtrl.actionTitle = $filter('translate')('datamining.testes.specificsession.search');
	testCtrl.test = JSON.parse(test.data.string);
	testCtrl.testId = testCtrl.test.id;
	
	testCtrl.minDate = new Date().getTime();
	testCtrl.maxDate = new Date().getTime();
	
	testCtrl.actions = [];
	testCtrl.specificSession = function() {
		var minDate = UtilsService.datepickerToTimestamp(testCtrl.minDate);
		var maxDate = UtilsService.datepickerToTimestamp(testCtrl.maxDate);
		
		console.log(testCtrl.testId, testCtrl.username, minDate, maxDate, testCtrl.limit);
		ServerAPI.getSpecificSession(testCtrl.testId, testCtrl.username, testCtrl.local, minDate, maxDate, testCtrl.limit).then(function(data){
			testCtrl.actions = JSON.parse(data.data.string);
			console.log(testCtrl.actions);
		}, function(data){
			console.log(data);
		});
	}
	//getSpecificSession
})
.controller('TestNewController', function($filter, ServerAPI, testsControl) {
	var testCtrl = this;
	testCtrl.test = {};
	testCtrl.actionTitle = $filter('translate')('datamining.testes.new');
	testCtrl.testsControl = JSON.parse(testsControl.data.string);
	console.log(testCtrl);
	testCtrl.save = function() {
		var test = angular.toJson({
			'test' : testCtrl.test,
			'testConrolId' : testCtrl.testControl
		});
		ServerAPI.saveTest(test);
	};
})
.controller('TestEditController', function(test, $filter, ServerAPI, testsControl) {
	var testCtrl = this;
	console.log(test);
	testCtrl.test = JSON.parse(test.data.string);
	testCtrl.actionTitle = $filter('translate')('datamining.testes.edit');
	testCtrl.testsControl = JSON.parse(testsControl.data.string);
	testCtrl.save = function() {
		var test = angular.toJson({
			'test' : testCtrl.test,
			'testConrolId' : testCtrl.testControl
		});
		ServerAPI.saveTest(test);
	};
})

//Tasks Controllers
.controller('TaskViewController', function($scope, task, ServerAPI, ActionTypeEnum, $rootScope) {
	var taskCtrl = this;
	taskCtrl.task = JSON.parse(task.data.string);
	console.log(taskCtrl.task);
	
	$scope.deleteAction = function(action, list){
		ServerAPI.deleteAction(action, list);
	}
	
	$scope.selectAction = function(action) {
		$scope.actionSelectedContent = contentPopover(action);
	}
	
	//gambys para a apresentação
	jQuery('.popover').remove();
	
	taskCtrl.popup = {
	  options: {
		  html: true,
		  trigger: 'hover',
		  placement: 'top'
	  }
	};
	
	angular.forEach(taskCtrl.task.actionsInitial, function(action){
		action.popovercontent = contentPopover(action);
	});
	angular.forEach(taskCtrl.task.actionsEnd, function(action){
		action.popovercontent = contentPopover(action);
	});
	angular.forEach(taskCtrl.task.actionsRequired, function(action){
		action.popovercontent = contentPopover(action);
	});
	
	taskCtrl.getActionTypeName = function(obj){
		var str = "-";
		angular.forEach(ActionTypeEnum.types, function(type){
			if(obj === type.value){
				str = type.name;
			}
		});
		return str;
	}
	
	function contentPopover(action){
		var content = 'Ação = '+action.actionType+'<br/>';
		angular.forEach(action.elementFiedlSearch, function(elem){
			content += elem.field + ' = ' + elem.value + '<br/>';
		});
		angular.forEach(action.urlFieldSearch, function(url){
			content += url.field + ' = ' + url.value + '<br/>';
		});
		return content;
	}
})
.controller('TaskSaveController', function($scope, test, ServerAPI, ActionTypeEnum, ShowMessage, ValidateService) {
	this.task = {};
	this.test = test;
	this.actionTypes = ActionTypeEnum.types;
	this.actionTypes = ActionTypeEnum.types;
	this.task.disregardActions = [];

	// toggle selection for a given fruit by name
	this.toggleTypeSelection = function toggleSelection(type) {
	    var idx = this.task.disregardActions.indexOf(type.value);
	    if (idx !== -1) {
	    	this.task.disregardActions.splice(idx, 1);
	    } else {
	    	this.task.disregardActions.push(type.value);
	    }
	};
	
	this.save = function() {
		this.task.testDataMining = this.test;
		if (ValidateService.task(this.task)) {
			var task = angular.toJson({
				'task' : this.task
			});
			ServerAPI.saveTask(task);
		} else {
			ShowMessage.error('datamining.validate.default');
		}
	};
})
.controller('TaskNewController', function($scope, test, $controller, $filter) {
	angular.extend(this, $controller('TaskSaveController', {$scope: $scope, test: JSON.parse(test.data.string)}));
	var taskCtrl = this;
	taskCtrl.actionTitle = $filter('translate')('datamining.tasks.new');
})
.controller('TaskEditController', function($scope, task, $controller, $filter) {
	var task = JSON.parse(task.data.string);
	angular.extend(this, $controller('TaskSaveController', {$scope: $scope, test: task.testDataMining}));
	var taskCtrl = this;
	taskCtrl.task = task;
	taskCtrl.actionTitle = $filter('translate')('datamining.tasks.edit');
})
.controller('TaskEvaluateController', function($scope, $rootScope, evaluate, evalTestId, sessionFilterParam, $filter, ServerAPI, $timeout, $sce, UtilsService) {
	var taskCtrl = this;
	
	taskCtrl.formatDate = UtilsService.formatDate;
	taskCtrl.evalTestId = evalTestId;
	var map = evaluate.data.string;
	map = JSON.parse( map );
	
	console.log(map);
	
	for (var i in map) {
		taskCtrl[i] = map[i];
	}
	
	console.log("init taskEvaluate: ", evaluate, evalTestId);
	console.log(taskCtrl);
	
	//gambys para a apresentação
	jQuery('.popover').remove();
	
	var modesArr = [{
				name: 'users', 
				title: $filter('translate')('datamining.tasks.evaluate.users.sessions.title'),
				desc: $filter('translate')('datamining.tasks.evaluate.group.users'),
				notAnOption: false
			},{
				name: 'userSessions', 
				title: $filter('translate')('datamining.tasks.evaluate.user.sessions.title'),
				desc: '-',
				notAnOption: true
			},{
				name: 'sessions', 
				title: $filter('translate')('datamining.tasks.evaluate.sessions.title'),
				desc: $filter('translate')('datamining.tasks.evaluate.group.sessions'),
				notAnOption: false
			},{
				name: 'actions', 
				title: $filter('translate')('datamining.tasks.evaluate.user.session.actions.title'),
				desc: '-',
				notAnOption: true
			},{
				name: 'actionsCount', 
				title: $filter('translate')('datamining.tasks.evaluate.actions.count.title'),
				desc: $filter('translate')('datamining.tasks.evaluate.actions.count.title'),
				notAnOption: true
			},{
				name: 'actionsRequiredCount', 
				title: $filter('translate')('datamining.tasks.evaluate.actions.required.count.title'),
				desc: $filter('translate')('datamining.tasks.evaluate.actions.required.count.title'),
				notAnOption: false
			},{
				name: 'frequentPatterns', 
				title: $filter('translate')('datamining.patterns'),
				desc: $filter('translate')('datamining.patterns'),
				notAnOption: false
			},{
				name: 'pattern', 
				title: $filter('translate')('datamining.pattern'),
				desc: $filter('translate')('datamining.pattern'),
				notAnOption: true
	}];
	
	var modes = {
			'users': modesArr[0],
			'userSessions': modesArr[1],
			'sessions': modesArr[2],
			'actions': modesArr[3],
			'pattern': modesArr[7]
	}
	
	taskCtrl.modes = modes;
	taskCtrl.modesArr = modesArr;
	taskCtrl.mode = modes.users;
	taskCtrl.actionsSituation = {};
	taskCtrl.actionsSpecialSituation = {};
	taskCtrl.actionsSituationChanged = {};
	taskCtrl.actionsSpecialSituationChanged = {};
	
	
	//count actions
	var actionsArr = $filter('toArray')(taskCtrl.result.pageViewActionIds),
		maxCount = 0;
	angular.forEach(actionsArr, function(action){
		action.count = taskCtrl.result.pageViewActionCount[action.$key];
		if(maxCount < action.count){
			maxCount = action.count;
		}
	});
	
	taskCtrl.actionsMaxCount = maxCount;
	taskCtrl.actionsArr = actionsArr;
	taskCtrl.actionsRequiredArr = $filter('toArray')(taskCtrl.result.actionsRequiredTask);
	for (var i in taskCtrl.actionsRequiredArr) {
		var act = taskCtrl.actionsRequiredArr[i];
		for (var a in actionsArr) {
			if (actionsArr[a].value == act.$key) {
				taskCtrl.actionsRequiredArr[i].id = actionsArr[a].$key;
				break;
			}
		}
	}
	
	//adjust frequentPatterns
	function adjustFrequentPatterns(patterns) {
		angular.forEach(patterns, function(fp, keyFp){
			fp.key = keyFp;
			fp.itemsetsFormatted = [];
			fp.sessionsFormatted = [];
			fp.effectivenessMean = 0;
			fp.efficiencyMean = 0;
			fp.successMean = 0;
			fp.requiredMean = 0;
			fp.usersSessions = {};
			
			angular.forEach(fp.itemsets, function(itset, keyItset){
				angular.forEach(itset.items, function(item){
					//update action referente to frequentpatterns and itemsets
					var action = {
						idAction: item,
						desc: taskCtrl.result.pageViewActionIds[item],
						count: taskCtrl.result.pageViewActionCount[item],
						itemset: keyItset
					};
					//actionsArr
					fp.itemsetsFormatted.push(action);
				});
			});
			
			angular.forEach(fp.sequencesIds, function(seq){
				var sess = taskCtrl.result.sessions[seq];
				fp.sessionsFormatted.push(sess);
				
				fp.effectivenessMean += (sess.effectivenessNormalized * 100);
				fp.efficiencyMean += (sess.efficiencyNormalized * 100);
				fp.successMean += sess.userRateSuccess;
				fp.requiredMean += sess.userRateRequired;
				
				if (fp.usersSessions[sess.username] === undefined) {
					fp.usersSessions[sess.username] = {
						count: 0,
						sessions: []
					};
				}
				fp.usersSessions[sess.username].count++;
				fp.usersSessions[sess.username].sessions.push(sess.id);
			});
			
			var seqIds = fp.sequencesIds != null ? fp.sequencesIds.length : 1;
			fp.effectivenessMean = fp.effectivenessMean / seqIds;
			fp.efficiencyMean = fp.efficiencyMean / seqIds;
			fp.successMean = fp.successMean / seqIds;
			fp.requiredMean = fp.requiredMean / seqIds;
			fp.itemsetsFormattedText = fp.itemsetsFormatted.map(function(elem){
			    return elem.idAction;
			}).join(", ");
		});
	}
	
	adjustFrequentPatterns(taskCtrl.frequentPatterns);
	
	
	
	/************ Classifications ************/
	
	$scope.classificationEnum = {
		success : {
			id: "SUCCESS",
			desc: $filter('translate')('datamining.tasks.evaluate.complete'),
			val: 0,
			classLabel: 'label-success'
		},
		threshold : {
			id: "THRESHOLD",
			desc: $filter('translate')('datamining.tasks.evaluate.threshold'),
			val: 2,
			classLabel: 'label-important'
		},
		repeat : {
			id: "REPEAT",
			desc: $filter('translate')('datamining.tasks.evaluate.restart'),
			val: 1,
			classLabel: 'label-warning'
		},
		error : {
			id: "ERROR",
			desc: $filter('translate')('datamining.tasks.evaluate.error'),
			val: 3,
			classLabel: 'label-important'
		}
	}
	
	$scope.getClassificationFromEnum = function(classif) {
		for (var c in $scope.classificationEnum) {
			if ($scope.classificationEnum[c].id == classif) {
				return $scope.classificationEnum[c];
			}
		}
		return {
			id: "-",
			desc: '-',
			val: 9
		};
	}
	
	/************ Sitations ************/
	$scope.situationsEnum = {
    	OK: {
    		id: 'ok',
    		desc: 'Correta',
    		group: 'green',
    		classLabel: 'label-success'
    	},
    	ERROR: {
    		id: 'error',
    		desc: 'Incorreta',
    		group: 'red',
    		classLabel: 'label-important'
    	},
    	ALERT: {
    		id: 'alert',
    		desc: 'Alerta',
    		group: 'yellow',
    		classLabel: 'label-warning'
    	},
    	DEFAULT: {
    		id: 'default',
    		desc: 'Situação Indefinida',
    		classLabel: ''
    	}
    }
	$scope.resetSendDataChange = function(){
		$scope.sendDataChange = false;
	}
	$scope.resetAlertChangeTask = function(){
		$scope.alertChangeTask = false;
	}
	function resetSituationAux(actionId) {
		var sit = taskCtrl.actionsSituation[actionId];
		$scope.situationAux = sit !== undefined ? sit : $scope.situationsEnum.DEFAULT;
	}
	$scope.setSituationAction = function(actionId, situation) {
		for (var i in $scope.situationsEnum) {
			if ($scope.situationsEnum[i].id === situation.id) {
				var sit = taskCtrl.actionsSituation[actionId];
				if (sit !== undefined && sit.id === situation.id){
					taskCtrl.actionsSituation[actionId] = undefined;
				}else {
					taskCtrl.actionsSituation[actionId] = situation;
				}
				//$scope.sendDataChange = true;
				taskCtrl.actionsSituationChanged[actionId] = taskCtrl.actionsSituation[actionId];
				break;
			}
		}
		resetSituationAux(actionId);
	}
	$scope.isSituationActionOrUndefined = function(actionId, situation) {
		var sit = taskCtrl.actionsSituation[actionId];
		if (sit == undefined || $scope.isSituationAction(actionId, situation)) {
			return true;
		}
		return false;
	}
	$scope.isSituationAction = function(actionId, situation) {
		var sit = taskCtrl.actionsSituation[actionId];
		if (sit !== undefined && sit.id === situation.id) {
			return true;
		}
		return false;
	}
	$scope.selectSituationAction = function(actionId, situation) {
		if (actionId != null && situation != null) {
			$scope.setSituationAction(actionId, situation);
			$scope.redrawAll();
		}
	}
	$scope.descSituationAction = function(actionId) {
		var sit = taskCtrl.actionsSituation[actionId];
		return sit !== undefined ? sit.desc : '-';
	}
	$scope.idSituationAction = function(actionId) {
		var sit = taskCtrl.actionsSituation[actionId];
		return sit !== undefined ? sit.id : '';
	}
	$scope.getActionInfoFromId = function(actionId) {
		for (var s in taskCtrl.result.sessions) {
			var session = taskCtrl.result.sessions[s];
			for (var a in session.actions) {
				if (session.actions[a].identifier == actionId) {
					return session.actions[a];
				}
			}
		}
		return null;
	}
	$scope.getSessionFromId = function(sessionId) {
		for (var s in taskCtrl.result.sessions) {
			var session = taskCtrl.result.sessions[s];
			if (session.id == sessionId) {
				return session;
			}
		}
		return null;
	}
	$scope.getSessionFromActionId = function(actionId) {
		var sessions = [];
		var sessionsMap = {};
		for (var s in taskCtrl.result.sessions) {
			var session = taskCtrl.result.sessions[s];
			for (var a in session.actions) {
				if (session.actions[a].identifier == actionId) {
					sessionsMap[session.id] = session;
				}
			}
		}
		for (var s in sessionsMap) {
			sessions.push(sessionsMap[s]);
		}
		return sessions;
	}
	
	//acoes especiais (inicial/final/obrigatória):
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
	//taskCtrl.result.sessions
	$scope.sendDataChange = false;
	$scope.alertChangeTask = false;
	for (var s in taskCtrl.result.sessions) {
		var sess = taskCtrl.result.sessions[s];
		for (var a in sess.actions) {
			if (sess.actions[a].required) {
				taskCtrl.actionsSpecialSituation[sess.actions[a].identifier] = $scope.specialActionsEnum.REQUIRED;
			}
		}
	}
	function setSpecialSituationNetwork(network, actionId, sit) {
		if (network) {
			var arrNodes = network.body.nodes;
			for (var n in arrNodes) {
				if (arrNodes[n].options.action != null && arrNodes[n].options.action.identifier == actionId) {
					arrNodes[n].options.action[$scope.specialActionsEnum.INITIAL.id] = null;
					arrNodes[n].options.action[$scope.specialActionsEnum.END.id] = null;
					arrNodes[n].options.action[$scope.specialActionsEnum.REQUIRED.id] = null;
					if (sit) {
						arrNodes[n].options.action[sit.id] = true;
					}
				}
			}
		}
	}
	function setSpecialSituationRealSessions(actionId, sit) {
		for (var s in taskCtrl.result.sessions) {
			var sess = taskCtrl.result.sessions[s];
			for (var a in sess.actions) {
				if (sess.actions[a].identifier == actionId) {
					sess.actions[a][$scope.specialActionsEnum.INITIAL.id] = null;
					sess.actions[a][$scope.specialActionsEnum.END.id] = null;
					sess.actions[a][$scope.specialActionsEnum.REQUIRED.id] = null;
					if (sit) {
						sess.actions[a][sit.id] = true;
					}
				}
			}
		}
	}
	function resetSpecialSituationAux(actionId) {
		var sit = taskCtrl.actionsSpecialSituation[actionId];
		$scope.specialSituationAux = sit !== undefined ? sit : $scope.specialActionsEnum.DEFAULT;
	}
	$scope.setSpecialSituationAction = function(actionId, situation) {
		for (var i in $scope.specialActionsEnum) {
			if ($scope.specialActionsEnum[i].id === situation.id) {
				//var sit = taskCtrl.actionsSpecialSituation[actionId];
				$scope.sendDataChange = true;
				taskCtrl.actionsSpecialSituation[actionId] = situation;
				taskCtrl.actionsSpecialSituationChanged[actionId] = situation;
				setSpecialSituationRealSessions(actionId, situation);
				setSpecialSituationNetwork($scope[$scope.graphIdsEnum.GRAPH_DEFAULT], actionId, situation);
				setSpecialSituationNetwork($scope[$scope.graphIdsEnum.GRAPH_SESSION], actionId, situation);
				break;
			}
		}
	}
	$scope.isSpecialSituationActionOrUndefined = function(actionId, situation) {
		var sit = taskCtrl.actionsSpecialSituation[actionId];
		if (sit == undefined || $scope.isSpecialSituationAction(actionId, situation)) {
			return true;
		}
		return false;
	}
	$scope.isSpecialSituationAction = function(actionId, situation) {
		var sit = taskCtrl.actionsSpecialSituation[actionId];
		if (sit !== undefined && sit.id === situation.id) {
			return true;
		}
		return false;
	}
	$scope.selectSpecialSituationAction = function(actionId, situation) {
		if (actionId != null && situation != null) {
			$scope.setSpecialSituationAction(actionId, situation);
			$scope.redrawAll();
		}
	}
	
	
	
	
	$scope.showUserSessions = function(user){
		var sessions = [];
		for(var i in user.sessionsId){
			var userSession = user.sessionsId[i];
			for(var s in taskCtrl.result.sessions){
				var session = taskCtrl.result.sessions[s];
				if(session.id == userSession){
					sessions.push(session);
					break;
				}
			}
		}
		taskCtrl.userSessions = sessions;
		changeMode('userSessions');
	}
	
	$scope.showActionsSession = function(session){
		$scope.showContentAdvanced();
		taskCtrl.userSession = session;
		changeMode('actions');
	}
	
	$scope.showUsers = function(){
		changeMode('users');
	}
	
	$scope.showSessions = function(){
		changeMode('sessions');
	}
	
	$scope.showFrequentPattern = function(pattern){
		taskCtrl.frequentPatternActive = pattern;
		changeMode('pattern');
		$scope.renderGraph(false);
	}
	
	function changeMode(newMode){
		$scope.actionViewSelected = null;
		taskCtrl.previousMode = taskCtrl.mode;
		taskCtrl.mode = modes[newMode];
	}
	
	$scope.backMode = function(){
		taskCtrl.mode = taskCtrl.previousMode;
	}
	
	/*************   LISTA DE AÇÕES   *************/
	
	$scope.setActionViewSelected = function(action, isTask){
		if (isTask) {
			if (!taskCtrl.actionViewSelected) {
				taskCtrl.actionViewSelected = {};
			}
			taskCtrl.actionViewSelected.action = action;
			taskCtrl.actionViewSelected.positionStep = null;
			var graph = $scope[$scope.graphIdsEnum.GRAPH_SESSION];
			var node = nodeFromGraph(action.identifier, graph);
			updateNodeGraph(node, graph);
	    	selectNode(node, "actionViewSelected", "edgeSelectedSession");
		} else {
			$scope.actionViewSelected = action;
			$scope.actionViewSelected.positionStep = null;
			resetSituationAux(action.identifier);
	  	    resetSpecialSituationAux(action.identifier);
		}
		
		$scope.redraw($scope.graphIdsEnum.GRAPH_SESSION);
	}
	$scope.setActionViewGuideSelected = function(actionId){
		$scope.actionViewSelected = $scope.getActionInfoFromId(actionId);
		resetSituationAux(actionId);
  	    resetSpecialSituationAux(actionId);
	}
	$scope.setActionViewSelectedMostRealized = function(actionId){
		$scope.actionViewSelectedMost = $scope.getActionInfoFromId(actionId);
		resetSituationAux(actionId);
  	    resetSpecialSituationAux(actionId);
	}
	
	//history graph
	$scope.historyLabels = ["January", "February", "March", "April", "May", "June", "July"];
	$scope.historySeries = ['Series A', 'Series B'];
	$scope.historyData = [
	    [65, 59, 80, 81, 56, 55, 40],
	    [28, 48, 40, 19, 86, 27, 90]
	 ];
	
	//success graph
	console.log(taskCtrl.result);
	$scope.graphSuccessLabels = [
	                             $filter('translate')('datamining.tasks.evaluate.complete'), 
	                             $filter('translate')('datamining.tasks.evaluate.restart'),
	                             $filter('translate')('datamining.tasks.evaluate.threshold'),
	                             $filter('translate')('datamining.tasks.evaluate.error')];
    $scope.graphSuccessData = [
                               	taskCtrl.result.countSessionsSuccess, 
                               	taskCtrl.result.countSessionsRepeat,
                               	taskCtrl.result.countSessionsThreshold,
                               	taskCtrl.result.countSessionsError];
    $scope.graphSuccessColours = ["#46BFBD", "#FDB45C", "#F7464A", "#999"];
    $scope.graphSuccessType = 'Pie';
    $scope.graphSuccessToggle = function () {
    	$scope.graphSuccessType = $scope.graphSuccessType === 'Pie' ? 'PolarArea' : 'Pie';
    };
    
    
    /*************   CONTEUDO GERAL AVALIACAO   *************/
    var contentShowEnum = {
    	GRAPH: 'graph',
    	GUIDE: 'guide',
    	ADVANCED: 'advanced'
    }
    
    $scope.contentShow = contentShowEnum.GRAPH;
    $scope.showContentGraph = function() {
    	$scope.redraw($scope.graphIdsEnum.GRAPH_DEFAULT);
    	$scope.contentShow = contentShowEnum.GRAPH;
    	//adjust to focus initial node
    	console.log('showContentGraph');
    	$scope.resetZoomGraphType($scope.graphIdsEnum.GRAPH_DEFAULT);
    }
    $scope.showContentGuide = function() {
    	if (!$scope.step) {
    		$scope.goToGuideStep("1");
    	}
    	$scope.contentShow = contentShowEnum.GUIDE;
    }
    $scope.showContentAdvanced = function() {
    	$scope.contentShow = contentShowEnum.ADVANCED;
    }
    $scope.isShowContentGraph = function() {
    	return $scope.contentShow === contentShowEnum.GRAPH;
    }
    $scope.isShowContentGuide = function() {
    	return $scope.contentShow === contentShowEnum.GUIDE;
    }
    $scope.isShowContentAdvanced = function() {
    	return $scope.contentShow === contentShowEnum.ADVANCED;
    }
    $scope.resetZoomGraphType = function(typeGraph) {
    	//$scope.graphIdsEnum.GRAPH_DEFAULT
    	$timeout(function(){ 
    		$scope.resetZoomGraph(typeGraph, 0.5);
        }, 50);
    }
    
    /*************   GRAFO   *************/
    //ALL(0), SUCCESS(1), WITH_PROBLEM(2), ERROR(3), REPEAT(4), THRESHOLD(5);
    $scope.sessionClassificationFilterEnum = {
    	ALL: {
    		val: 0,
    		desc: 'Todas as Sessões'
    	},
    	SUCCESS: {
    		val: 1,
    		desc: 'Sessões "Completas"'
    	},
    	WITH_PROBLEM: {
    		val: 2,
    		desc: 'Sessões "Problemáticas" (Erro, Limiar e Repetição)'
    	},
    	ERROR: {
    		val: 3,
    		desc: 'Sessões "Erro"'
    	},
    	REPEAT: {
    		val: 4,
    		desc: 'Sessões "Reinício"'
    	},
    	THRESHOLD: {
    		val: 5,
    		desc: 'Sessões "Limiar"'
    	},
    }
    
    $scope.sessionKeyByRequest = getEnumKeyFromValue(sessionFilterParam);
    $scope.minSup = (taskCtrl.result.lastMinSup * 100);
    $scope.minItens = taskCtrl.result.lastMinItens;
    
    if (!$scope.sessionKeyByRequest) {
    	$scope.sessionKeyByRequest = 'ALL';
    }
    $scope.sessionFilter = $scope.sessionKeyByRequest;
    
    function getEnumKeyFromValue(val) {
    	if (val) {
    		var keys = Object.keys($scope.sessionClassificationFilterEnum);
        	for (var k in keys) {
        		if ($scope.sessionClassificationFilterEnum[keys[k]].val == val) {
        			return keys[k]; 
        		}
        	}
    	}
    	return null;
    }
    
    
    $scope.factorScaleXAux = 5;
    $scope.factorScaleXAuxMin = 0.01;
    $scope.factorScaleXAuxMax = 10;
    $scope.factorScaleXAuxStep = 0.05;
    
    $scope.factorScaleX = $scope.factorScaleXAux;
    $scope.changeScaleX = function(graphName) {
    	$scope.factorScaleX = $scope.factorScaleXAux;
    	$scope.renderGraphByType(graphName, true);
    }
    
    
    function isNumeric(n) {
    	return !isNaN(parseFloat(n)) && isFinite(n);
    }
    
    $scope.graphTypeEnum = {
    	PATTERNS: {
    		id: 'patterns',
    		desc: 'Padrões de Uso'
    	},
    	SESSIONS: {
    		id: 'sessions',
    		desc: 'Sessões dos Usuários'
    	}
    }
    $scope.graphIdsEnum = {
		GRAPH_DEFAULT: 'graph',
		GRAPH_SESSION: 'graphsession'
    }
    var isPatternsActual;
    $scope.changeGraphType = function(isPatterns) {
    	isPatternsActual = isPatterns;
    	$scope.renderGraph();
    }
    
    $scope.renderGraphByType = function(graphname, preserveY) {
    	if (graphname == $scope.graphIdsEnum.GRAPH_SESSION) {
    		$scope.renderGraphSession($scope.sessionSelected, preserveY);
    	} else if (graphname == $scope.graphIdsEnum.GRAPH_DEFAULT) {
    		$scope.renderGraph(preserveY);
    	}
    }
    
    $scope.renderGraphSession = function(session, preserveY) {
    	var graphData = generateGraphSession(taskCtrl.frequentPatterns, session);
    	var graphname = $scope.graphIdsEnum.GRAPH_SESSION;
    	preserveY = getPreserveY(preserveY, graphname);
    	
    	$scope[graphname] = drawGraph('mynetworkSession', graphData, taskCtrl.result.sessions, taskCtrl.actionsSituation, $scope.situationsEnum, $scope.factorScaleX, preserveY);
    	
    	$scope.resetGraph(graphname, "actionViewSelected", "edgeSelectedSession");
    	verifyInitialZoom($scope[graphname]);
    	$scope[graphname].positionStep = 0;
    }
    
    $scope.renderGraph = function(preserveY) {
    	if (isPatternsActual) {
    		$scope.graphType = $scope.graphTypeEnum.PATTERNS;
    	} else {
    		$scope.graphType = $scope.graphTypeEnum.SESSIONS;
    	}
    	
    	var graphData = generateGraphFrequentPatterns($scope.graphType.id, taskCtrl.frequentPatterns, taskCtrl.result.sessions);
    	preserveY = getPreserveY(preserveY, $scope.graphIdsEnum.GRAPH_DEFAULT);
    	
    	$scope[$scope.graphIdsEnum.GRAPH_DEFAULT] = drawGraph('mynetwork', graphData, taskCtrl.result.sessions, taskCtrl.actionsSituation, $scope.situationsEnum, $scope.factorScaleX, preserveY);
    	
    	$scope.resetGraph($scope.graphIdsEnum.GRAPH_DEFAULT, "nodeSelected", "edgeSelected");
    	verifyInitialZoom($scope[$scope.graphIdsEnum.GRAPH_DEFAULT]);
    	$scope[$scope.graphIdsEnum.GRAPH_DEFAULT].positionStep = 0;
    }
    
    function getPreserveY (preserveY, graphname) {
    	return preserveY ? ($scope[graphname] ? $scope[graphname].preserveY : false) : false;
    }
    
    function selectNode(node, nodeSelectedName, edgeSelectedName) {
    	var edgesFrom = [], edgesTo = [];
    	if (node != undefined) {
    		resetSituationAux(node.id);
      	    resetSpecialSituationAux(node.id);
      	    
      	  	angular.forEach(node.edges, function(edge){
      	  		if (edge.from.id != node.id) {
      	  			edgesFrom.push(edge.from.id);
      	  		} else if (edge.to.id != node.id) {
      	  			edgesTo.push(edge.to.id);
      	  		} else {
      	  			edgesFrom.push(edge.from.id);
      	  			edgesTo.push(edge.to.id);
      	  		}
      	  	});
      	  
      	  	taskCtrl[nodeSelectedName] = {
      	  		'id': node.id,
      	  		'name': node.options.label,
      	  		'value': node.options.value,
    			'edgesFrom': edgesFrom.join(", "),
    			'edgesTo': edgesTo.join(", "),
    			'sessions': node.options.sessions,
    			'patternsCount': node.options.countPatterns,
    			'action': node.options.action,
    			'positionStep': null
      	  	}
    	}
    }
    
    $scope.resetGraph = function(graphName, nodeSelectedName, edgeSelectedName) {
    	$scope[graphName].on("selectNode", function (params) {
      	  	var node = $scope[graphName].body.nodes[params.nodes[0]];
      	  	selectNodeGraph(node, nodeSelectedName, edgeSelectedName);
        });
    	$scope[graphName].on("selectEdge", function (params) {
    		var edge = $scope[graphName].body.edges[params.edges[0]];

    		//taskCtrl[nodeSelectedName] = null;
      	  	taskCtrl[edgeSelectedName] = null;
    		
    		taskCtrl[edgeSelectedName] = {
	  			'fromId' : edge.fromId,
	  			'toId' : edge.toId,
	  			'value' : edge.options.value,
	  			'sessions': edge.options.sessions,
	  			'patternsCount' : edge.options.countPatterns
	      	}
	  		console.log(edge, taskCtrl[edgeSelectedName]);
    		$scope.$apply();
        });
    	$scope[graphName].on("deselectNode", function (evt) {
    		taskCtrl[nodeSelectedName] = null;
    		//var nodeid = evt.previousSelection.nodes[0];
    		//console.log(nodeid, $scope[graphName].body.nodes[nodeid].y);
    		$scope.$apply();
    	});
    	$scope[graphName].on("deselectEdge", function () {
    		taskCtrl[edgeSelectedName] = null;
    		$scope.$apply();
    	});
    	$scope[graphName].on("dragStart", function (params) {
      	  	//var node = $scope[graphName].body.nodes[params.nodes[0]];
      	  	//selectNodeGraph(node, nodeSelectedName, edgeSelectedName);
    	});
    	$scope[graphName].on("dragEnd", function (params) {
      	  	var node = $scope[graphName].body.nodes[params.nodes[0]];
      	  	if (!$scope[graphName].preserveY) {
      	  		$scope[graphName].preserveY = {};
      	  	}
      	  	if (node && node.id != undefined) {
      	  		$scope[graphName].preserveY[node.id] = node.y;
      	  	}
      	  selectNodeGraph(node, nodeSelectedName, edgeSelectedName);
    	});
    }
    
    function selectNodeGraph(node, nodeSelectedName, edgeSelectedName) {
    	taskCtrl[nodeSelectedName] = null;
  	  	taskCtrl[edgeSelectedName] = null;
  	  	
  	  	selectNode(node, nodeSelectedName, edgeSelectedName);
  		$scope.$apply();
    }
    
    var factorScale = 0.1;
    $scope.zoomOutGraph = function(graphName){
    	$scope[graphName].moveTo({
            scale: $scope[graphName].getScale() + factorScale
        });
    }
    $scope.zoomInGraph = function(graphName){
    	var newScale = $scope[graphName].getScale() - factorScale;
    	$scope[graphName].moveTo({
            scale: newScale >= 0 ? newScale : 0
        });
    }
    $scope.resetZoomGraph = function(graphName, zoomOptions){
    	var zoom = zoomOptions ? zoomOptions : $scope[graphName].initialZoom;
    	console.log($scope[graphName]);
    	$scope[graphName].moveTo({
    		position: $scope[graphName].initialPosition,
    		scale: zoomOptions
    	});
    }
    
    $scope.focusNodePosition = function(graphName, nodepos){
    	nodepos--;
    	var idFirstNode = Object.keys($scope[graphName].body.nodes)[nodepos];
    	$scope[graphName].focus(idFirstNode);
    }
    
    function verifyInitialZoom(graph){
    	graph.initialZoom = graph.initialZoom != null ? graph.initialZoom : graph.getScale();
    	graph.initialPosition = graph.initialPosition != null ? graph.initialPosition : graph.getViewPosition();
    }
    
    
    
    
    $scope.redrawAll = function() {
    	$scope.redraw($scope.graphIdsEnum.GRAPH_DEFAULT);
    	$scope.redraw($scope.graphIdsEnum.GRAPH_SESSION);
    }
    $scope.redraw = function(graphName) {
    	refreshGraph($scope[graphName], taskCtrl.actionsSituation, $scope.situationsEnum);
    }
    $scope.showActionsSessionFromGraph = function(session){
    	$scope.showContentAdvanced();
    	var sessions = taskCtrl.result.sessions, realSession;
    	for (var s in sessions) {
    		if (sessions[s].id == session.id) {
    			realSession = sessions[s];
    		}
    	}
		taskCtrl.userSession = realSession;
		changeMode('actions');
	}
    
    /** STEPS ON GRAPH **/
    $scope.positionStepView = {};
    $scope.positionStepView[$scope.graphIdsEnum.GRAPH_SESSION] = 0;
    
    $scope.forwardStepGraph = function(graphName, session) {
    	if (session.actions.length > $scope[graphName].positionStep) {
    		$scope[graphName].positionStep++;
    	}
    	setStepGraph(session, graphName);
    }
    
    $scope.backwardStepGraph = function(graphName, session) {
    	if ($scope[graphName].positionStep > 1) {
    		$scope[graphName].positionStep--;
    	}
    	setStepGraph(session, graphName);
    }
    
    $scope.repeatStepsGraph = function(graphName, session) {
    	$scope[graphName].positionStep = 1;
    	setStepGraph(session, graphName);
    }
    
    function setStepGraph(session, graphName) {
    	var graph = $scope[graphName];
    	var action = session.actions[graph.positionStep - 1];
    	var node = nodeFromGraph(action.identifier, graph);
    	
    	$scope.positionStepView[$scope.graphIdsEnum.GRAPH_SESSION] = graph.positionStep;
    	
    	updateNodeGraph(node, graph);
		selectNode(node, "actionViewSelected", "edgeSelectedSession");
		taskCtrl.actionViewSelected.positionStep = graph.positionStep;
    }
    
    function updateNodeGraph(node, graph){
    	graph.unselectAll();
		graph.selectNodes([node.id]);
    }
    
    function nodeFromGraph(actionId, graph) {
    	var nodes = graph.body.nodes;
    	for (var n in nodes) {
    		if (actionId == nodes[n].id) {
    			return nodes[n];
    		}
    	}
    	return null;
    }
    
    /*************   GUIDE   *************/
    
    $scope.goToGuideStep = function(step, data){
    	$scope.step = step;
    	if (step == "1") {
    		//ordenar prioridade: classificacao > exatidão > completude > tempo > qtd.acoes
    		var sess = taskCtrl.result.sessions;
    		sess.sort(function(a, b) {
    		    return a.actions.length - b.actions.length; //menor
    		});
    		sess.sort(function(a, b) {
    		    return a.time - b.time; //menor
    		});
    		sess.sort(function(a, b) {
    		    return b.userRateSuccess - a.userRateSuccess; //maior
    		});
    		sess.sort(function(a, b) {
    		    return b.userRateRequired - a.userRateRequired; //maior
    		});
    		sess.sort(function(a, b) {
    		    return classificationPoints(a.classification) - classificationPoints(b.classification); //menor
    		});
    		for (var s in sess) {
    			sess[s].order = s;
    		}
    		$scope.sessionsGuide = sess;
    	} else if (step == "1.1") {
    		$scope.sessionSelected = data;
    		
    		//filter for required actions dont done
    		$scope.sessionActionsReqStr = taskCtrl.task.actionsRequiredOrder;
    		if (taskCtrl.task.actionsRequiredOrder) {
    			var groupActionsReq = taskCtrl.task.actionsRequiredOrder.split(","),
	    			actionsRequiredSession = [],
	    			actionsFound = [];
	    		
	    		//identify actions req has been done
	    		for (var gar in groupActionsReq) {
	    			var strActionsReqId = groupActionsReq[gar],
	    				actionsReqId = strActionsReqId.replace(/\[|\]/g,"").split(";"),
	    				containsAnyActionGroup = false;
	    			
	    			for (var ari in actionsReqId) {
	    				var actionReqId = actionsReqId[ari];
	    				for (var t in taskCtrl.task.actionsRequired) {
	    					var action = taskCtrl.task.actionsRequired[t];
	    					if (action.id == actionReqId) {
	    						var actionRes = findActionResultFromActionSingleDataMining(action);
	    						actionRes.isFound = false;
	            				for (var a in data.actions) {
	            					if (data.actions[a].identifier == actionRes.identifier) {
	            						containsAnyActionGroup = true;
	            						actionRes.isFound = true;
	            						break;
	            					}
	            				}
	            				actionsFound.push(actionRes);
	    					}
	    				}
	    			}
	    			
	    			//mark only when hasnt any actionreq of group
	    			if (!containsAnyActionGroup) {
	    				$scope.sessionActionsReqStr = $scope.sessionActionsReqStr.replace(strActionsReqId, '<b style="color: #C80707">'+strActionsReqId+'</b>');
	    			}
	    		}
    		}
    		
    		$scope.sessionActionsRequired = actionsFound;
    		
    		$scope.renderGraphSession(data, false);
    		$scope.repeatStepsGraph($scope.graphIdsEnum.GRAPH_SESSION, data);
    		
        	//adjust to focus initial node
        	$timeout(function(){ 
        		$scope.resetZoomGraph($scope.graphIdsEnum.GRAPH_SESSION, 0.5);
            }, 50);
    	}
    }
    
    function findActionResultFromActionSingleDataMining (action) {
    	var actionResult = {}, 
    		sXPath = '', sJhm = '', sStepJhm = '', actResume = '';
	
		//TODO: tornar essa parte genérica (montar de acordo com regra do servidor)
		for (var u in action.urlFieldSearch) {
			var ufs = action.urlFieldSearch[u];
			if (ufs.field == 'sJhm') {
				sJhm = ufs.value;
			} else if (ufs.field == 'sStepJhm') {
				sStepJhm = ufs.value;
			}
		}
		for (var e in action.elementFiedlSearch) {
			var el = action.elementFiedlSearch[e];
			if (el.field == 'sXPath') {
				sXPath = el.value;
			}
		}
		actResume = sJhm+'-'+sStepJhm+' | '+sXPath+' | '+action.actionType;
		actionResult.resume = actResume;
		actionResult.iddb = action.id;
		
		for (var tar in taskCtrl.actionsRequiredArr) {
			var act = taskCtrl.actionsRequiredArr[tar];
			if (act.$key == actResume) {
				actionResult.identifier = act.id || '-';
				actionResult.action = act;
			}
		}
		
		return actionResult;
    };
    
    function classificationPoints(classification) {
    	return $scope.getClassificationFromEnum(classification).val;
    }
    
    function addMoreInfoSession(session) {
    	var countUserSessions = 0;
    	for (var s in taskCtrl.result.sessions) {
    		if (taskCtrl.result.sessions[s].username == session.username) {
    			countUserSessions++;
    		}
    	}
    	return countUserSessions;
    }
    
//    $scope.showActionsSessionGuide = function(session){
//    	if (session) {
//    		$scope.goToGuideStep("1.1", session);
//    	} else {
//    		alert("Selecione uma sessão e tente novamente...");
//    		$scope.goToGuideStep("1");
//    	}
//    }
    
    $scope.saveChangeSituations = function(){
    	//taskCtrl.actionsSituationChanged
    	//taskCtrl.actionsSpecialSituationChanged
    	var actionInfoMap = {};
    	for (var a in taskCtrl.actionsSpecialSituationChanged) {
    		var action = getActionFromActionsArr(a);
    		var actionInfo = splitActionValue(action.value);
    		
    		actionInfo.id = action.$key;
    		actionInfo.type = taskCtrl.actionsSpecialSituationChanged[a].id;
    		
    		actionInfoMap[actionInfo.id] = actionInfo;
    	}
    	
    	for (var a in taskCtrl.actionsSituationChanged) {
    		var action = getActionFromActionsArr(a);
    		var actionInfo = actionInfoMap[action.$key];
    		
    		if (!actionInfo) {
    			actionInfo = splitActionValue(action.value);
        		actionInfo.id = action.$key;
    		}
    		
    		actionInfo.situation = taskCtrl.actionsSituationChanged[a].id;
    		actionInfoMap[actionInfo.id] = actionInfo;
    	}
    	
    	console.log(actionInfoMap);
    	var resultActionMap = [];
    	for (var a in actionInfoMap) {
    		resultActionMap.push(actionInfoMap[a]);
    	}
    	
    	console.log(angular.toJson(resultActionMap));
    	
    	ServerAPI.saveActionSituation(
    			Number.parseInt(taskCtrl.task.testDataMining.id), 
    			Number.parseInt(taskCtrl.evalTestId), 
    			Number.parseInt(taskCtrl.task.id),
    			angular.toJson(resultActionMap)
		).then(function(data) {
			result = JSON.parse(data.data.string);
			if (result.status == "SUCESSO") {
				taskCtrl.actionsSpecialSituationChanged = {};
				$scope.sendDataChange = false;
				$scope.alertChangeTask = true;
				$rootScope.success = $filter('translate')(result.message);
			}
		}, function(data) {
			console.log(data);
		});
    	
    }
    
    function getActionFromActionsArr (id) {
    	for (var a in taskCtrl.actionsArr) {
    		if (taskCtrl.actionsArr[a].$key == id) {
    			return taskCtrl.actionsArr[a];
    		}
    	}
    }
    
    function splitActionValue (str) {
    	var data = str.split(' | ');
    	return {
    		location: data[0],
			element: data[1],
			action: data[2]
    	};
    }
    
    // ETAPA 2 - CLUSTERING
    
    taskCtrl.minclusters = 4;
    taskCtrl.distancecenters = 20;
    taskCtrl.lastClustering = null;
    
    $scope.clusterOptions = {
        chart: {
            type: 'scatterChart',
            height: 350,
            color: d3.scale.category10().range(),
            scatter: {
                onlyCircles: false
            },
            showDistX: true,
            showDistY: true,
            tooltip: {
            	contentGenerator: function(obj) {
            		return '<h3><span style="color: ' + obj.point.color + ';">' + obj.point.id + '</span></h3><hr style="margin: 2px 0"/><h5 style="text-align: center;">Eficácia: ' + obj.point.x + '; Eficiência: ' + obj.point.y + ';</h5>';
            	}
            },
            duration: 350,
            xAxis: {
                axisLabel: $filter('translate')('datamining.tasks.effectiveness'),
                tickFormat: function(d){
                    return d3.format('.02f')(d);
                }
            },
            yAxis: {
                axisLabel: $filter('translate')('datamining.tasks.efficiency'),
                tickFormat: function(d){
                    return d3.format('.02f')(d);
                }
                //axisLabelDistance: 30
            },
            zoom: {
                enabled: false,
                scaleExtent: [1, 10],
                useFixedDomain: true,
                useNiceScale: false,
                horizontalOff: false,
                verticalOff: false,
                unzoomEventType: 'dblclick.zoom'
            },
            forceY: [0,100],
            forceX: [0,100]
        }
    };
	
	//$scope.clusterData = generateData(4,40);
	//console.log('clusterData', $scope.clusterData);
	var initialClusterData = [{ values: [] }];
	$scope.clusterData = initialClusterData;
	
	function arrayUnique(array) {
	    var a = array.concat();
	    for(var i=0; i<a.length; ++i) {
	        for(var j=i+1; j<a.length; ++j) {
	            if(a[i] === a[j])
	                a.splice(j--, 1);
	        }
	    }
	    return a;
	}
	
	function getSessionsFromPointsGroup(pointsGroup) {
		var sessionsResult = [];
		for (var v in pointsGroup.values) {
			var point = pointsGroup.values[v];
			var session = $scope.getSessionFromId(point.id);
			if (session != null) {
				session.point = point;
				sessionsResult.push(session);
			}
		}
		return sessionsResult;
	}
	
	var lastGroupBest = null, lastGroupOthers = null;
	taskCtrl.changePointGroup = function(session) {
		console.log(session);
		console.log($scope.clusterData);
		
		if (lastGroupBest != null && lastGroupOthers != null) {
			var isBest = session.point.isBest;
			session.point.isBest = !isBest;
			if (isBest) {
				for (var v in lastGroupBest.values) {
					if (lastGroupBest.values[v].id == session.id) {
						lastGroupBest.values.splice(v, 1);
						break;
					}
				}
				lastGroupOthers.values.push(session.point);
			} else {
				for (var v in lastGroupOthers.values) {
					if (lastGroupOthers.values[v].id == session.id) {
						lastGroupOthers.values.splice(v, 1);
						break;
					}
				}
				lastGroupBest.values.push(session.point);
			}
		}
		refreshSessionsDataPoints();
	}
	
	function refreshSessionsDataPoints() {
		taskCtrl.lastClustering.sessionsBest = getSessionsFromPointsGroup(lastGroupBest);
		taskCtrl.lastClustering.sessionsOthers = getSessionsFromPointsGroup(lastGroupOthers);
		taskCtrl.lastClustering.sessions = arrayUnique(taskCtrl.lastClustering.sessionsBest.concat(taskCtrl.lastClustering.sessionsOthers));
	}
	
	$scope.showActionsSessionGuide = function(session){
		//$scope.showContentAdvanced();
		session = $scope.getSessionFromId(session.id);
		$scope.goToGuideStep("1.1", session)
		$scope.showContentGuide();
		$scope.stepEval = 2;
	}
    
    taskCtrl.clustering = function() {
    	ServerAPI.clusterSessions(
    			Number.parseInt(taskCtrl.task.testDataMining.id), 
    			Number.parseInt(taskCtrl.evalTestId), 
    			Number.parseInt(taskCtrl.task.id),
    			taskCtrl.minclusters, 
    			taskCtrl.distancecenters
    	).then(function(data) {
			var clustering = JSON.parse(data.data.string);
			console.log(clustering);
			
			//recebe quem é "oráculo" e quem não é
			var graphData = [],
				groupBest = {
					isBest: true,
	                key: $filter('translate')('dataminint.step.clustering.best'),
	                values: [],
	                color: '#1f77b4'
	            },
	            groupOthers = {
					isBest: false,
	                key: $filter('translate')('dataminint.step.clustering.others'),
	                values: [],
	                color: '#ff7f0e'
	            };
			
			for (var c in clustering.clusters) {
				var cluster = clustering.clusters[c];
				
				for (var p in cluster.points) {
					var point = cluster.points[p];
					var pointChart = {
						id: point.id,
						x: point.x,
	                    y: point.y,
	                    size: 100,
	                    shape: 'circle'
					};
					
					//atualizar dados locais de eficacia e eficiencia
					for (var s in taskCtrl.result.sessions) {
						var session = taskCtrl.result.sessions[s];
						if (session.id == point.id) {
							taskCtrl.result.sessions[s].effectiveness = point.x;
							taskCtrl.result.sessions[s].efficiency = point.y;
							break;
						}
					}
					
					if (cluster.isBest) {
						pointChart.isBest = true;
						groupBest.values.push(pointChart);
					} else {
						pointChart.isBest = false;
						groupOthers.values.push(pointChart);
					}
				}
			}
			
			lastGroupBest = groupBest;
			lastGroupOthers = groupOthers;
			
			graphData.push(lastGroupBest);
			graphData.push(lastGroupOthers);
			
			$scope.clusterData = graphData;
			
			taskCtrl.lastClustering = clustering;
			refreshSessionsDataPoints();
			
			
				
			//rerender
			$timeout(function(){
				$scope.clusterApi.refresh();
				
				/*
				$scope.$on('tooltipShow.directive', function(event){
	                console.log('scope.tooltipShow', event);
	            });
	            */
				
				//$timeout(function(){ $scope.canChange = true; }, 1000);
			});
			
		}, function(data) {
			console.log(data);
		});
    }
    
    // ETAPA 3 - FSPM
    function generateFSPM(group) {
    	var strResult = "";
    	for (var v in group.values) {
    		var strSession = "";
    		var session = $scope.getSessionFromId(group.values[v].id);
			if (session != null) {
				for (var a in session.actions) {
					var action = session.actions[a];
					strSession += action.identifier + " -1 ";
				}
				strSession += "-2\n";
			}
			strResult += strSession;
    	}
    	return strResult;
    }
    
    function getActionsFromSessionsPoint(group) {
    	var result = [], resultMap = {};
    	for (var v in group.values) {
    		var session = $scope.getSessionFromId(group.values[v].id);
			if (session != null) {
				for (var a in session.actions) {
					var action = session.actions[a];
					if (!resultMap[action.identifier]) {
						resultMap[action.identifier] = action;
						result.push(action);
					}
				}
			}
    	}
    	return result;
    }
    
    function getMapActionsPattern(frequentPattern) {
    	var mapActions = [];
		for (var p in frequentPattern) {
			var pattern = frequentPattern[p];
			for (var i in pattern.itemsetsFormatted) {
				var item = pattern.itemsetsFormatted[i];
				mapActions[item.idAction] = {
					id: item.idAction,
					count: item.count
				};
			}
		}
		return mapActions;
    }
    
    function filterActionsGroup(arrayActions1, arrayActions2) {
    	var result = [];
    	var arrayActions = arrayActions1.filter(function(obj) {
		    return !arrayActions2.some(function(obj2) {
		        return obj.identifier == obj2.identifier;
		    });
		});
		for (var a in arrayActions) {
			var action = arrayActions[a];
			result.push(action);
		}
		return result;
    }
    
    function changeSituationFromType(type, actionId) {
    	if (type == $scope.typePatternsResultArrayEnum.REQUIRED) {
    		$scope.setSpecialSituationAction(actionId, $scope.specialActionsEnum.REQUIRED)
    	} else if (type == $scope.typePatternsResultArrayEnum.PROBLEM) {
    		$scope.setSituationAction(actionId, $scope.situationsEnum.ERROR);
    	} else if (type == $scope.typePatternsResultArrayEnum.CORRECT) {
    		$scope.setSituationAction(actionId, $scope.situationsEnum.OK);
    	} else if (type == $scope.typePatternsResultArrayEnum.ALERT) {
    		$scope.setSituationAction(actionId, $scope.situationsEnum.ALERT);
    	}
    }
    
    taskCtrl.changeAll = function() {
    	var actions = $scope.typePatternsResultSelected.val;
    	for (var a in actions) {
    		var action = actions[a];
    		changeSituationFromType($scope.typePatternsResultSelected, action.identifier);
    	}
    }
    
    taskCtrl.changeNormal = function() {
    	var actions = $scope.typePatternsResultSelected.val;
    	for (var a in actions) {
    		var action = actions[a];
    		if ($scope.isSituationActionOrUndefined(action.identifier, $scope.situationsEnum.DEFAULT) &&
    			$scope.isSpecialSituationActionOrUndefined(action.identifier, $scope.specialActionsEnum.DEFAULT)
    			) {
    			changeSituationFromType($scope.typePatternsResultSelected, action.identifier);
    		}
    	}
    }
    
    taskCtrl.minSupBest = 0.75;
    taskCtrl.minItensBest = 4;
    taskCtrl.minSupOthers = 0.4;
    taskCtrl.minItensOthers = 4;
    
    taskCtrl.lastPatternsResult = null;
    
    $scope.typePatternsResultSelected = {};
    $scope.typePatternsResultArrayEnum = {
    	REQUIRED: {
    		id: 1,
    		desc: '"Obrigatórias"',
    		info: 'Ações dos Padrões Frequentes de Sessões Referência'
    	},
    	PROBLEM: {
    		id: 2,
    		desc: '"Problemáticas"',
    		info: 'Ações das Padrões Frequentes de Demais Sessões que não estão nos Padrões Frequentes de Sessões Referência'
    	},
    	CORRECT: {
    		id: 3,
    		desc: '"Corretas"',
    		info: 'Ações das Sessões Referência'
    	},
    	ALERT: {
    		id: 4,
    		desc: '"Alertas"',
    		info: 'Ações das Demais Sessões que não estão Sessões Referência'
    	}
    };
    
    $scope.changeTypePatternsResultSelected = function(type) {
    	$scope.typePatternsResultSelected = type;
    	$scope.actionViewPatternResult = null;
    }
    
    $scope.actionViewPatternResult = null;
    $scope.setActionViewPatternResult = function(action) {
    	$scope.actionViewPatternResult = action;
		$scope.actionViewPatternResult.positionStep = null;
		resetSituationAux(action.identifier);
  	    resetSpecialSituationAux(action.identifier);
  	    $scope.setSessionsActionPatternResult(action.identifier);
    }
    
    $scope.sessionsActionPatternResult = null;
    $scope.setSessionsActionPatternResult = function(actionId) {
    	$scope.sessionsActionPatternResult = $scope.getSessionFromActionId(actionId);
    }
    
    taskCtrl.comparing = function() {
    	
    	if (taskCtrl.lastClustering == null) {
    		$rootScope.errors = [];
    		$rootScope.errors.push('Realize a Etapa 3 (Clusterizar Sessões) antes de prosseguir!');
    		return;
    	}
    	
    	console.log("----- comparing -----");
    	console.log(taskCtrl.result.sessions);
    	console.log(lastGroupBest);
    	console.log(lastGroupOthers);
    	
    	var obj = {
    		'patternsBest' : 	generateFSPM(lastGroupBest),
    		'patternsOthers' : 	generateFSPM(lastGroupOthers),
    		'testId' : 			Number.parseInt(taskCtrl.task.testDataMining.id), 
    		'evalTestId' : 		Number.parseInt(taskCtrl.evalTestId), 
    		'taskId' : 			Number.parseInt(taskCtrl.task.id),
    		'minSupBest' : 		taskCtrl.minSupBest,
    		'minItensBest' : 	taskCtrl.minItensBest,
    		'minSupOthers' : 	taskCtrl.minSupOthers,
    		'minItensOthers' : 	taskCtrl.minItensOthers,
    		'minItens' : 		4
    	};
    	
    	var patterns = angular.toJson({
			'padroes' : obj
		});
    	
    	ServerAPI.comparePatterns(patterns).then(function(data) {
			console.log(data);
			
			var patternsResult = JSON.parse(data.data.string);
			adjustFrequentPatterns(patternsResult.resultFSPMBest.frequentPatterns);
			adjustFrequentPatterns(patternsResult.resultFSPMOthers.frequentPatterns);
			
			patternsResult.mapActionsBest = getMapActionsPattern(patternsResult.resultFSPMBest.frequentPatterns);
			patternsResult.mapActionsOthers = getMapActionsPattern(patternsResult.resultFSPMOthers.frequentPatterns);
			

			//generate arrays
			patternsResult.arrayActionsBest = [];
			for (var actId in patternsResult.mapActionsBest) {
				var action = $scope.getActionInfoFromId(actId);
				patternsResult.arrayActionsBest.push(action);
			}
			patternsResult.arrayActionsOthers = [];
			for (var actId in patternsResult.mapActionsOthers) {
				var action = $scope.getActionInfoFromId(actId);
				patternsResult.arrayActionsOthers.push(action);
			}
			
			//filtrar acoes problematicas
			patternsResult.arrayActionsOthers = patternsResult.arrayActionsOthers.filter(function(obj) {
				return obj != null;
			});
			patternsResult.arrayActionsBest = patternsResult.arrayActionsBest.filter(function(obj) {
				return obj != null;
			});
			var arrayActionsGroupBest = getActionsFromSessionsPoint(lastGroupBest);
			var arrayActionsGroupOthers = getActionsFromSessionsPoint(lastGroupOthers);
			
			
			//3.1 - FSPM G1 = "obrigatória";
			patternsResult.arrayActionsRequired = [];
			for (var a in patternsResult.arrayActionsBest) {
				var action = patternsResult.arrayActionsBest[a];
				patternsResult.arrayActionsRequired.push(action);
			}
			
			//3.2 - FSPM G2 que não está no FSPM G1 = "problemática";
			patternsResult.arrayActionsProblems = filterActionsGroup(patternsResult.arrayActionsOthers, 
																		patternsResult.arrayActionsBest);
			patternsResult.arrayActionsProblems = filterActionsGroup(patternsResult.arrayActionsProblems, 
																		arrayActionsGroupBest);
			
			//3.3 - Ações do G1 não contidas no FSPM G1 = "corretas" 
			patternsResult.arrayActionsGroupBest = filterActionsGroup(arrayActionsGroupBest, 
																		patternsResult.arrayActionsBest);
			
			//3.4 - Ações do G2 não continas no G1 = "alerta";
			patternsResult.arrayActionsGroupOthers = filterActionsGroup(arrayActionsGroupOthers, 
																		arrayActionsGroupBest);
			
			
			$scope.typePatternsResultArrayEnum.REQUIRED.val = patternsResult.arrayActionsRequired;
			$scope.typePatternsResultArrayEnum.PROBLEM.val = patternsResult.arrayActionsProblems;
			$scope.typePatternsResultArrayEnum.CORRECT.val = patternsResult.arrayActionsGroupBest;
			$scope.typePatternsResultArrayEnum.ALERT.val = patternsResult.arrayActionsGroupOthers;

			console.log(patternsResult);
			
			$scope.changeTypePatternsResultSelected($scope.typePatternsResultArrayEnum.PROBLEM);
			taskCtrl.lastPatternsResult = patternsResult;
			
		}, function(data) {
			console.log(data);
		});
    	
    }
    
    //initialize
    $timeout(function(){ 
    	$scope.changeGraphType(false);
    }, 500);
    $scope.initialGraphFSPM = true;
    $scope.showInitialGraphFSPM = function() {
    	if ($scope.initialGraphFSPM) {
    		$scope.initialGraphFSPM = false;
    		$scope.resetZoomGraphType($scope.graphIdsEnum.GRAPH_DEFAULT);
    	}
    }
    
    $scope.stepEval = 1;
    $scope.stepMaxEval = 5; //diretiva; evaluate; less;
    $scope.stepEvalBack = function() {
    	$scope.stepEval = $scope.stepEval > 1 ? $scope.stepEval-1 : $scope.stepEval;
    }
    $scope.stepEvalNext = function() {
    	$scope.stepEval = $scope.stepEval < $scope.stepMaxEval ? $scope.stepEval+1 : $scope.stepEval;
    }
    $scope.changeStepEval = function(newVal) {
    	$scope.stepEval = newVal;
    	if (newVal == 2) {
    		$scope.showInitialGraphFSPM();
    	}
    }
    
})

//Actions Controllers
//ActionsNewController
.controller('ActionViewController', function(action) {
	//todo
})
.controller('ActionNewController', function(task, $filter, ServerAPI, ActionTypeEnum, MomentTypeEnum, ValidateService, ShowMessage, UtilsService) {
	var actionCtrl = this;
	actionCtrl.action = {};
	actionCtrl.types = ActionTypeEnum.types;
	actionCtrl.moments = MomentTypeEnum.moments;
	actionCtrl.task = JSON.parse(task.data.string);
	actionCtrl.actionTitle = $filter('translate')('datamining.tasks.actions.add');
	console.log(task.data.string);
	
	actionCtrl.save = function() {
		actionCtrl.action.task = actionCtrl.task;
		if(actionCtrl.action.actionType){
			actionCtrl.action.actionType = actionCtrl.action.actionType.value;
		}
		if(actionCtrl.action.momentType){
			actionCtrl.action.momentType = actionCtrl.action.momentType.value;
		}
		
		//trim values
		actionCtrl.action.element = UtilsService.trim(actionCtrl.action.element);
		actionCtrl.action.url = UtilsService.trim(actionCtrl.action.url);
		actionCtrl.action.jhm = UtilsService.trim(actionCtrl.action.jhm);
		actionCtrl.action.step = UtilsService.trim(actionCtrl.action.step);
		
		if (ValidateService.action(actionCtrl.action)) {
			var action = angular.toJson({
				'actionJHeatVO' : actionCtrl.action
			});
			ServerAPI.saveAction(action);
		} else {
			ShowMessage.error('datamining.validate.default');
		}
	};
})
.controller('ActionEditController', function(action, $filter, ServerAPI) {
	//todo
})

/* DIRECTIVES */
/* <a href="#" ng-click="confirmClick() && deleteItem(item)" confirm-click>Delete</a> */
.directive('usdmActionInfo', function(config, env) {
  return {
    restrict: 'E',
    scope: {
      actionInfo: '=action',
      momento: '=momento',
      
      situationsEnum: '=situationsEnum',
      specialActionsEnum: '=specialActionsEnum',
      situationAux: '=situationAux',
      situationSpecialAux: '=situationSpecialAux',
      
      selectSituation: '&onSelectSituation',
      selectSpecialSituation: '&onSelectSpecialSituation'
    },
    templateUrl: config[env].apiUrl+'/templates/directives/usdm-action-info.html'
  };
})
.directive('usdmSessionInfo', function(config, env) {
  return {
    restrict: 'E',
    scope: {
    	sessionInfo: '=session',
    	graphsession: '=graphsession',
    	getClassificationFromEnum: '&getClassificationFromEnum'
    },
    templateUrl: config[env].apiUrl+'/templates/directives/usdm-session-info.html'
  };
})
.directive('usdmProgressHeader', function(config, env) {
  return {
    restrict: 'E',
    scope: {
    	title: '=title',
    	stepNumber: '=stepNumber',
    	showInfo: '=showInfo',
    	info: '=info',
    	stepEvalBack: '&onStepEvalBack',
    	stepEvalNext: '&onStepEvalNext'
    },
    templateUrl: config[env].apiUrl+'/templates/directives/usdm-progress-header.html'
  };
})
.directive('usdmMessagesAlert', function(config, env) {
  return {
    restrict: 'E',
    templateUrl: config[env].apiUrl+'/templates/directives/usdm-messages-alert.html'
  };
})
.directive('usdmFilterTable', function(config, env) {
  return {
    restrict: 'E',
    transclude: true,
    scope: {
    	placeholderModel: '=usdmPlaceholder',
    	searchModel: '=usdmSearchModel'
    },
    templateUrl: config[env].apiUrl+'/templates/directives/usdm-filter-table.html'
  };
})
.directive('confirmClick', ['$q', 'dialogModal', '$filter', function($q, dialogModal, $filter) {
    return {
        link: function (scope, element, attrs) {
            var ngClick = attrs.ngClick.replace('confirmClick()', 'true')
                .replace('confirmClick(', 'confirmClick(true,');
            // setup a confirmation action on the scope
            scope.confirmClick = function(msg) {
                // if the msg was set to true, then return it (this is a workaround to make our dialog work)
                if (msg===true) {
                    return true;
                }
                // confirm-click attribute on the <a confirm-click="Are you sure?"></a>
                msg = msg || attrs.confirmClick || $filter('translate')('datamining.areyousure');
                // open a dialog modal, and then continue ngClick actions if it's confirmed
                dialogModal(msg).result.then(function() {
                    scope.$eval(ngClick);
                });
                // return false to stop the current ng-click flow and wait for our modal answer
                return false;
            };
        }
    }
}])
.service('dialogModal', ['$modal', '$filter', function($modal, $filter) {
    return function (message, title, okButton, cancelButton) {
        // setup default values for buttons
        // if a button value is set to false, then that button won't be included
        okButton = okButton===false ? false : (okButton || $filter('translate')('datamining.areyousure.confirm'));
        cancelButton = cancelButton===false ? false : (cancelButton || $filter('translate')('datamining.areyousure.cancel'));

        // setup the Controller to watch the click
        var ModalInstanceCtrl = function ($scope, $modalInstance, settings) {
            // add settings to scope
            angular.extend($scope, settings);
            // ok button clicked
            $scope.ok = function () {
                $modalInstance.close(true);
            };
            // cancel button clicked
            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };

        // open modal and return the instance (which will resolve the promise on ok/cancel clicks)
        var modalInstance = $modal.open({
            template: '<div class="dialog-modal"> \
                <div class="modal-header" ng-show="modalTitle"> \
                    <h3 class="modal-title">{{modalTitle}}</h3> \
                </div> \
                <div class="modal-body">{{modalBody}}</div> \
                <div class="modal-footer"> \
                    <button class="btn btn-primary" ng-click="ok()" ng-show="okButton">{{okButton}}</button> \
                    <button class="btn btn-warning" ng-click="cancel()" ng-show="cancelButton">{{cancelButton}}</button> \
                </div> \
            </div>',
            controller: ModalInstanceCtrl,
            resolve: {
                settings: function() {
                    return {
                        modalTitle: title,
                        modalBody: message,
                        okButton: okButton,
                        cancelButton: cancelButton
                    };
                }
            }
        });
        // return the modal instance
        return modalInstance;
    }
}])
.directive('popup', function() {
	  return {
		    restrict: 'A',
		    require: 'ngModel',
		    scope: {
		      ngModel: '=',
		      options: '=popup',
		      popupTitle: '='
		    },
		    link: function(scope, element) {
		      scope.$watch('ngModel', function(val) {
		    	  element.attr('data-content', val);
		      });
		      
		      var options = scope.options || {} ; 
		      var title = (scope.popupTitle || options.title) || null;
		      var placement = options.placement || 'right';
		      var html = options.html || false;
		      var delay = options.delay ? angular.toJson(options.delay) : null;
		      var trigger = options.trigger || 'hover';
		      
		      element.attr('title', title);
		      element.attr('data-placement', placement);
		      element.attr('data-html', html);
		      element.attr('data-delay', delay);
		      element.popover({ trigger: trigger });
		    }
	  };
})
.directive('colorAction', function() {
	  return {
		    restrict: 'A',
		    scope: {
		    	colorActionElem: '=',
		    	colorCtrl: '='
		    },
		    link: function(scope, element) {
		    	
		    	var id = (scope.colorActionElem.$key || scope.colorActionElem.identifier);
		    	var count = scope.colorCtrl.result.pageViewActionCount[id];
		    	var a = count > 0 ? (count / scope.colorCtrl.actionsMaxCount) : 0;
		    	
		    	
		    	jQuery(element).style('background', 'rgba(66,99,146, '+a+')', 'important')
		    	if(a > .5){
		    		element.css('color', 'white');
		    	}
		    	
		    	/*
		    	var a = (scope.colorActionElem.count/taskCtrl.actionsMaxCount || 0);
		    	element.css('background', 'rgba(66,99,146, '+a+')');
		    	if(a >= .6){
		    		element.css('color', 'white');
		    	}
		    	*/
		    }
	  };
})
.directive('longLoader', function($rootScope) {
	  return {
		    restrict: 'A',
		    scope: { },
		    link: function(scope, element) {
		    	
		    	var DEFAULT_PADDING_TIME = 20, 
		    		DEFAULT_VELOCITY = 100,
		    		DEFAULT_TIME_MSGS = 2;
		    	
		    	var v = 0, endFunc = false;
		    	function fakeLoading($obj, seconds) {
		    		if (typeof seconds == "undefined") seconds = DEFAULT_VELOCITY;
		    		seconds += DEFAULT_PADDING_TIME;
		    		endFunc = false;
		    		var messages = {
		    			'0.25': {
		    				message: 'Carregando...' 
		    			},
		    			'0.5': {
		    				message: 'Opa, chegou na metade! :)' 
		    			},
		    			'0.7': {
		    				message: 'Z z Z z ...' 
		    			},
		    			'0.85': {
		    				message: 'Tá pertinho... :D' 
		    			}
		    		};
		    		v = 0;
		    		var l = function() {
		    				var newadd = 0.2 / seconds;
		    				newadd = v >= 0.6 ? 0.1 / seconds : newadd;
		    				newadd = v >= 0.82 ? 0.05 / seconds : newadd;
		    				
		    				for (var m in messages) {
		    					if (v >= parseFloat(m) && messages[m].done != true) {
		    						var timeMsg = messages[m].time || DEFAULT_TIME_MSGS;
		    						messages[m].done = true;
		    						$obj.ElasticProgress("showMessage", messages[m].message, timeMsg);
		    					}
		    				}
		    				
		    				if (v == 2) {
		    					v = 1;
		    					endFunc = true;
		    				}else if (v >= 1) {
		    					v = 0.999;
		    					endFunc = true;
		    				} else {
		    					v += newadd;
		    				}
		    				
		    				if (!loading) {
		    					return;
		    				}
		    				
		    				if (typeof $obj.jquery != "undefined") {
		    						$obj.ElasticProgress("setValue", v);
		    				} else {
		    						$obj.setValue(v);
		    				}
		    				
		    				if (!endFunc) {
		    						TweenMax.delayedCall(0.05 + (Math.random() * 0.14), l);
		    				}
		    		};
		    		l();
			    }
		    	
		    	var velocity = DEFAULT_VELOCITY;
		    	var progress = jQuery(element).show().ElasticProgress({
	    			align: "center",
	    			bleedTop: 110,
	    			bleedBottom: 40,
	    			buttonSize: 50,
	    			labelTilt: 70,
	    			arrowDirection: "down",
	    			fontFamily: "Montserrat",
	    			colorBg: "#80ACFD",
	    			colorFg: "#425b92",
	    			textComplete: 'Últimos detalhes...',
	    			onClick: function(event) {
	    					console.log("onClick");
	    					$(this).ElasticProgress("open");
	    			},
	    			onOpen: function(event) {
	    					console.log("onOpen", new Date());
	    					fakeLoading($(this), velocity);
	    			},
	    			onComplete: function(event) {
	    					console.log("onComplete", new Date());
	    			},
	    			onClose: function(event) {
	    					console.log("onClose");
	    			},
	    			onFail: function(event) {
	    					console.log("onFail");
	    					$(this).ElasticProgress("open");
	    			},
	    			onCancel: function(event) {
	    					console.log("onCancel");
	    					$(this).ElasticProgress("open");
	    			}
	    		}).hide();
		    	
		    	var loading = false;
		    	
		    	function initLongLoading() {
		    		loading = true;
		    		v = 0;
		    		endFunc = false;
		    		progress.ElasticProgress("setValue", 0);
		    		velocity = $rootScope.longLoadingTime || DEFAULT_VELOCITY;
		    		progress.show().ElasticProgress("open");
		    	}
		    	
		    	function endLongLoading() {
		    		loading = false;
		    		v = 0;
		    		endFunc = true;
		    		progress.ElasticProgress("setValue", 0);
		    		progress.hide().ElasticProgress("close");
		    	}		    	
		    	
		    	$rootScope.$on('$routeChangeStart', function(event, next, current) {
		    		if (next.data && next.data.longLoading) {
		    			initLongLoading();
		    		} else {
		    			endLongLoading();
		    		}
		    	});
		    	
		    	$rootScope.$on('cfpLoadingBar:loading', function(){
		    		var x = jQuery('.loader__img');
		    		if (loading && x.length) {
		    			x.hide();
		    		} else {
		    			x.show();
		    		}
		    	});
		    	
		    	$rootScope.$on('cfpLoadingBar:completed', function(){
		    		endLongLoading();
		    	});
		    }
	  };
})

//Smells
.directive('defaultHolder', function($filter)
{
    return{
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attrs, ngModelController){
            ngModelController.$formatters.push(function(value){
            	if (value == -1)
            		return $filter('translate')('pattern');
            	else
            		return value;
            })
        }
    }
})

/*MILL*//*
.directive('parseInt', [function () {
	    return {
	        restrict: 'A',
	        require: 'ngModel',
	        link: function (scope, elem, attrs, controller) {
	            controller.$formatters.push(function (modelValue) {
	                //console.log('model', modelValue, typeof modelValue);
	                return '' + modelValue;
	            });

	            controller.$parsers.push(function (viewValue) {
	                //console.log('view', viewValue, typeof viewValue);
	                return parseInt(viewValue,10);
	            });
	        }
	    }
	} ])*/
/*FIN MILL*/
	
/* FILTER */

.filter('msConverter', function() {
  return function(n) {
	  var numberTwoDigits = function(n) {
		  return (n < 10 ? '0' : '') + n;
	  }
	  n = Math.round(n);
	  var minutes = Math.floor(n / 60000);
	  var seconds = ((n % 60000) / 1000).toFixed(0);
	  if (minutes < 60 ){
		  return numberTwoDigits(minutes) + "m:" + numberTwoDigits(seconds) + "s";
	  } else {
		  var hours = Math.floor(minutes / 60);
		  minutes = (minutes % 60).toFixed(0);
		  return hours + "h:" + numberTwoDigits(minutes) + "m:" + numberTwoDigits(seconds) + "s";
	  }
  }
})
.filter('booleanText', function($filter) {
  return function(n) {
	  if (n == true) {
		  return $filter('translate')('true');
	  } else {
		  return $filter('translate')('false');
	  }
  }
})
.filter('numberSize', function() {
  return function(n, size) {
	  while((new String(n)).length < size){
		  n = "0"+n;
	  }
	  return n; 
  }
})
.filter('toArray', function () {
  return function (obj, addKey) {
    if (!(obj instanceof Object)) {
      return obj;
    }

    if ( addKey === false ) {
      return Object.values(obj);
    } else {
      return Object.keys(obj).map(function (key) {
    	  if((obj[key] instanceof Object)){
    		  return Object.defineProperty(obj[key], '$key', { enumerable: false, value: key});
    	  }else{
    		  return {'value': obj[key], '$key': key};
    	  }
      });
    }
  };
})
.filter("sanitize", ['$sce', function($sce) {
  return function(htmlCode){
    return $sce.trustAsHtml(htmlCode);
  }
}])
.filter('cut', function () {
    return function (value, wordwise, max, tail) {
        if (!value) return '';

        max = parseInt(max, 10);
        if (!max) return value;
        if (value.length <= max) return value;

        value = value.substr(0, max);
        if (wordwise) {
            var lastspace = value.lastIndexOf(' ');
            if (lastspace != -1) {
                value = value.substr(0, lastspace);
            }
        }

        return value + (tail || ' …');
    };
});

(function($) {    
  if ($.fn.style) {
    return;
  }

  // Escape regex chars with \
  var escape = function(text) {
    return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
  };

  // For those who need them (< IE 9), add support for CSS functions
  var isStyleFuncSupported = !!CSSStyleDeclaration.prototype.getPropertyValue;
  if (!isStyleFuncSupported) {
    CSSStyleDeclaration.prototype.getPropertyValue = function(a) {
      return this.getAttribute(a);
    };
    CSSStyleDeclaration.prototype.setProperty = function(styleName, value, priority) {
      this.setAttribute(styleName, value);
      var priority = typeof priority != 'undefined' ? priority : '';
      if (priority != '') {
        // Add priority manually
        var rule = new RegExp(escape(styleName) + '\\s*:\\s*' + escape(value) +
            '(\\s*;)?', 'gmi');
        this.cssText =
            this.cssText.replace(rule, styleName + ': ' + value + ' !' + priority + ';');
      }
    };
    CSSStyleDeclaration.prototype.removeProperty = function(a) {
      return this.removeAttribute(a);
    };
    CSSStyleDeclaration.prototype.getPropertyPriority = function(styleName) {
      var rule = new RegExp(escape(styleName) + '\\s*:\\s*[^\\s]*\\s*!important(\\s*;)?',
          'gmi');
      return rule.test(this.cssText) ? 'important' : '';
    }
  }

  // The style function
  $.fn.style = function(styleName, value, priority) {
    // DOM node
    var node = this.get(0);
    // Ensure we have a DOM node
    if (typeof node == 'undefined') {
      return this;
    }
    // CSSStyleDeclaration
    var style = this.get(0).style;
    // Getter/Setter
    if (typeof styleName != 'undefined') {
      if (typeof value != 'undefined') {
        // Set style property
        priority = typeof priority != 'undefined' ? priority : '';
        style.setProperty(styleName, value, priority);
        return this;
      } else {
        // Get style property
        return style.getPropertyValue(styleName);
      }
    } else {
      // Get CSSStyleDeclaration
      return style;
    }
  };
})(jQuery);