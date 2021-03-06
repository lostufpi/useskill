
//var domainUseSkill = "http://localhost:8080/Usabilidade";
var domainUseSkill = "http://www.useskill.com";
var SEND_BROWSER_EVENT = false;

//----------------------------------------------------------------------------------------------------------------------------------------------
//OMNIBOX
var suggestions = {
	settings : /settings|options|preferencias|opcoes/gi,
	help: /h|help|ajuda/gi,
}

chrome.omnibox.onInputChanged.addListener(function(text, suggest) {
	var sugestoes = new Array(), sug;
	//iterar sobre sugestoes
	for(var x in suggestions){
		sug = new String(suggestions[x]).replace(/\/gi|\/|\|/g," ");
		if(sug.indexOf(text)!=-1){
			sugestoes.push({content: x, description: text + " - " + x})
		}
	}
	suggest(sugestoes);
});

chrome.omnibox.onInputEntered.addListener(function(text) {
	omniboxEnteredFunction(text);
});
function omniboxEnteredFunction(text){
	var search = text.toLowerCase().replace(/\s/,"");
	if(suggestions.settings.test(text)){
		openPage('options.html');
		suggestions.settings.test(text);
	}else if(suggestions.help.test(text)){
		openPage('help.html');
		suggestions.help.test(text);
	}
}

function openPage(page) {
    var options_url = chrome.extension.getURL(page);
    console.log("Open: %s",page);
    chrome.tabs.query({
        url: options_url,
    }, function(tabs) {
        if (tabs.length)
            chrome.tabs.update(tabs[0].id, {active:true});
        else
            chrome.tabs.create({url:options_url});
    });
}

//----------------------------------------------------------------------------------------------------------------------------------------------

Array.prototype.contains = function(o){
	if(this.indexOf(o)!=-1){
		return true;
	}else{
		return false;
	}
}
Array.prototype.add = function(o){
	if(this.indexOf(o)==-1){
		this.push(o);
	}
}
Array.prototype.remove = function(i){
	if(i<this.length){
		this.splice(i,1);
	}
}
Array.prototype.removeElement = function(o){
	var index = this.indexOf(o);
	if(index!=-1){
		this.splice(index,1);
	}
}
Array.prototype.removeBetween = function(from, to){
	var dif = to - from + 1;
	if(from>=0 && to<this.length && dif>0){
		this.splice(from,dif);
	}	
}
Array.prototype.toConsole = function(){
	for(var i in this){
		if(new String(this[i]).indexOf("function")==-1){
			console.log("["+i+"]->"+this[i]+";");
		}
	}
}

//----------------------------------------------------------------------------------------------------------------------------------------------
localStorage.clear("BG");
var acoes = parseJSON(localStorage.getItem("BG"));

if(!acoes){
	acoes = new Array();
}

function addAcao(stringAcao){
	var acao = parseJSON(stringAcao);
	//console.log(stringAcao);
	//console.log("ADD ACAO");
	console.log(acao);
	acoes.push(acao);
	localStorage.setItem("BG",stringfyJSON(acoes));
}

function clearAcoes(){
	console.log("CLEAR:");
	console.log(acoes);
	acoes = new Array();
}

function acoesContainsConcluir(){
	for(var i in acoes){
		if(acoes[i].sActionType!=null){
			if(new String(acoes[i].sActionType).toUpperCase()=="CONCLUIR"){
				return true;
			}
		}
	}
	return false;
}

//----------------------------------------------------------------------------------------------------------------------------------------------

function Store () {
    this.listaElementos;
    this.elementoAtualLista;
    this.gravando = false;
    this.tabs = new Array();
}

var storage = new Store();

var isOpenned = true;

chrome.extension.onRequest.addListener(function(request, sender, sendResponse){
	if(request.useskill){
		switch(request.useskill){
			case "nextElement":
			  	nextElement(request.atual, request.lista);
			  	break;
			case "getStorageAndAcoes":
				sendResponse({storage: storage, acoes: acoes, isOpenned: isOpenned});
				break;
			case "getStorage":
			  	sendResponse({dados: storage});
			  	break;
			case "getAcoes":
				//console.log("Retornou: ");
				//console.log(acoes);
				sendResponse({dados: acoes});
				break;
			case "addAcao":
				addAcao(request.acao);
				break;
			case "clearAcoes":
				clearAcoes();
				sendResponse({clear: true});
				break;
			case "setNewTab":
			  	//vem de abas com new target
	    		chrome.tabs.create({'url': request.url}, function(tab) {
					if(storage.gravando){
						//adiciono a tab para a lista das tabs monitoradas
						storage.tabs.add(tab.id);
					}
				});
			  	break;
			case "testFinish":
				//finalizar o teste que está em execução
				suspendTest();
			  	break;
			case "showNotification":
				showNotification({title: request.title, message: request.message, timeout: request.timeout});
				break;
			case "getDomain":
				sendResponse({domain: domainUseSkill});
				break;
			case "setOpenned":
				console.log("setOpenned");
				console.log(request);
				if(request.isOpenned!=null){
					isOpenned = request.isOpenned;
				}
				break;
		}
	}else if(request.useskillserver){

		/*area para comunicacao com o servidor*/
		switch(request.useskillserver){
			case "getRoteiro":
				var retorno = null;
				if(request.idTarefa){
					retorno = ajax(domainUseSkill+"/tarefa/roteiro", 'POST', {'idTarefa' : request.idTarefa});
					console.log(retorno)
				}
				sendResponse({dados: retorno});
			  	break;

			case "concluirTarefa":
				var enviado = false;
				var intervalo = window.setInterval(function(){
					console.log(acoesContainsConcluir());
					if(acoesContainsConcluir() && !enviado){
						var dados = concluirOuPularTarefa(request.idTarefa, true, "");
						enviado = true;
						sendResponse({success: dados});
						clearInterval(intervalo);
					}
				},200);
				break;
			case "pularTarefa":
				var dados = concluirOuPularTarefa(request.idTarefa, false, request.justificativa);
				sendResponse({success: dados});
				break;
			case "enviarComentario":
				if(request.idTarefa){
					/*
					processXHR(domainUseSkill+"/tarefa/enviarcomentario", 'POST', {
						'idTarefa' : request.idTarefa,
						'texto' : request.texto,
						'qualificacao' : request.quali,
					});
					*/
					
					$.ajax({
						url: domainUseSkill+"/tarefa/enviarcomentario",
						cache: false,
						type: 'POST',
						dataType : 'json',
						async: false,
						data: {
							'idTarefa' : request.idTarefa,
							'texto' : request.texto,
							'qualificacao' : request.quali,
						},
						success: function(dados){
							sendResponse({success: true});
						},
						error: function(jqXHR, status, err){
							console.log(jqXHR);
							sendResponse({success: false});
						}
					});
				}
				break;
			case "responderPergunta":
				console.log("resposta: ");
				console.log(request);
				var urlResp = domainUseSkill + request.url;
				console.log(urlResp);
				if(request.idPergunta){
					$.ajax({
						url : urlResp,
						cache: false,
						type : 'POST',
						dataType : 'json',
						async : false,
						data : {
							'perguntaId' 	: request.idPergunta,
							'resposta'		: request.resposta
							},
						success : function(json) {
							sendResponse({success: true});
						},
						error : function(jqXHR, textStatus, errorThrown){
							console.log(jqXHR);
							sendResponse({success: false});
						}
					});
				}else{
					sendResponse({success: false});
				}
				break;
		}
	}else if(request.useskillsettings){
		switch(request.useskillsettings){
			case "getURL":
				var urlUseSkillLocalStorage = localStorage["useskill_url"];
				sendResponse({url: domainUseSkill});
				break;
			case "setURL":
				var newValue = request.newValue;
				if (newValue.slice(-1)=="/"){
					newValue = newValue.slice(0,-1);
				}
				if (newValue.indexOf('http://') != 0 && newValue.indexOf('https://') != 0){
					newValue = 'http://' + newValue; 
				}
				if (newValue){
					localStorage["useskill_url"] = newValue;
					domainUseSkill = newValue;
					sendResponse({success: true});
					showNotification({title: "UseSkill", message: 'Endereço da UseSkill alterado com sucesso!', timeout: 3000});
				}else{
					sendResponse({success: false});
				}
				break;
			case "omniboxEntered":
				omniboxEnteredFunction(request.text);
				break;
				
		}
	}
});

//sempre que criar uma nova tab durante a gravação do teste, esta página deve ser monitorada
//mas estamos acompanhando isto no onupdate, visto que sempre que crio ele atualiza a página
chrome.tabs.onCreated.addListener(function(tab) {
	console.log("onCreated: "+tab.id);
});

chrome.tabs.onRemoved.addListener(function(tabId) {
	if(storage.gravando){
		if(storage.tabs.contains(tabId)){
			console.log("remove: "+tabId);
			removeTab(tabId);
		}
	}
});

//para evitar que página seja criada ou refreshed e perca os js nela
chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
	if(storage.gravando){
		if(changeInfo.status == "loading"){
			//adicionar abas abertas durante os testes
			storage.tabs.add(tabId);
			if(storage.tabs.contains(tabId)){
				console.log("update: "+tabId);
				insertOnPage(tabId);
			}
		}
	}
});

//----------------------------------------------------------------------------------------------------------------------------------------------
//WEBNAVEGATION


function Action(action, time, url, content, tag, tagIndex, id, classe, name, xPath, posX, posY, viewportX, viewportY, useragent) {
	this.sActionType = action;
	this.sTime = time;
	this.sRealTime = new Date().getTime();
	this.sTimezoneOffset = new Date().getTimezoneOffset();
	this.sUrl = url;
	this.sContent = String(content).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
	this.sContent = content;
	this.sTag = tag;
	this.sTagIndex = tagIndex;
	this.sId = id;
	this.sClass = classe;
	this.sName = name;
	this.sXPath = xPath;
	this.sPosX = posX;
	this.sPosY = posY;
	this.sViewportX = viewportX;
	this.sViewportY = viewportY;
	this.sUserAgent = useragent;
}

//metodo que captura os eventos de transição de página:
//click em link, form_submit, url digitada, reload, forward ou back
chrome.webNavigation.onCommitted.addListener(function(details){
	if(SEND_BROWSER_EVENT){
		var action, transType;
		//capturar eventos de back e forward
		var qualifiers = details.transitionQualifiers;
		if(qualifiers && qualifiers[0] == "forward_back"){
			var url = details.url;
			transType = "forward_back";
			action = true;
		}else{
			//capturar evento que redirecionam para outra página (link, form_submit, typed)
			transType = details.transitionType.toLowerCase();
			if(transType == 'link' || transType == 'form_submit' || transType == 'typed' || transType == 'reload'){
				var url = details.url;
				action = true;
			}
		}

		if(action){
			action = new Action(transType, new Date().getTime(), url, "", "", "", "", "", "", "", 0, 0, 0, 0, navigator.userAgent);
			var stringAcao = stringfyJSON(action);
			addAcao(stringAcao);
		}
	}
});

//----------------------------------------------------------------------------------------------------------------------------------------------

//função para carregar o próximo elemento do teste ou encerra-lo
function nextElement(atual, lista){
	var listaElementos;
	var proxUrl;

	//verificar se está no inicio do teste, para setar no storage a sequencia de elementos a serem percorridos
	if(lista){
		listaElementos = parseJSON(lista.listaElementos);
		storage.listaElementos = lista.listaElementos;
	}else{
		listaElementos = parseJSON(storage.listaElementos);
	}

	//atualizo o que está armazenado no storage
	storage.elementoAtualLista = atual;
	
	//se ainda há elementos do teste a serem realizados
	if(listaElementos.length > atual){
		var elemento = listaElementos[atual];

		storage.gravando = true;
		console.log("storage.gravando true");
		//iniciando nova tarefa/pergunta... reinicia as acoes
		clearAcoes();

		//diferenciando tarefas, perguntas e quais quer outros tipos
		if(elemento.tipo == "T"){
			var objJson = ajax(domainUseSkill+"/tarefa/"+elemento.id+"/json", "GET");
			var tarefa = objJson.tarefaVO;
			proxUrl = tarefa.url;
		}else if(elemento.tipo == "P"){
			proxUrl = domainUseSkill+"/teste/responder/pergunta/"+elemento.id;
		}
	}else{
		storage.gravando = false;
		console.log("TESTE CONCLUIDO");
		console.log("storage.gravando false");

		proxUrl = domainUseSkill+"/teste/participar/termino"; //URL pós teste
	}

	//abrindo a nova aba e fechando as antigas
	chrome.tabs.create({'url': proxUrl}, function(tab) {
		if(!lista){
			removeTabsGravando();
		}
		//se ainda for gravar
		if(storage.gravando){
			//adiciono a tab para a lista das tabs monitoradas
			storage.tabs.add(tab.id);			
		}
	});
}

/**
Função que remove uma aba das abas que estão sendo analisadas
*/
function removeTab(id){
	storage.tabs.removeElement(id);
	console.log("removeTab: "+id);
	console.log(storage.tabs);
	if(storage.tabs.length<=0){
		storage.gravando = false;
		console.log("storage.gravando false")
	}
}

/**
Função que remove todas as abas que estavam sendo gravadas pelo plugin
*/
function removeTabsGravando(){
	var count = storage.tabs.length;	
	if(count>=0){
		for(var i = 0; i < count; i++){
			console.log("ChromeRemoveTabO: "+storage.tabs[i]);
			chrome.tabs.remove(storage.tabs[i]);
		}
	}
	//reset isOpenned
	isOpenned = true;
}

function suspendTest(){
	var responseUseSkill = ajax(domainUseSkill+"/teste/participar/adiar", "GET");
	console.log(responseUseSkill);
	if(responseUseSkill.boolean){
		console.log("Teste Adiado");
		clearAcoes();
		removeTabsGravando();
		storage = new Store();
	}
}


function insertOnPage(tabId){
	
	//capture script
	chrome.tabs.executeScript(tabId, {file: "js/capt.mining.useskill.nojquery.js"}, function(){
		chrome.tabs.executeScript(tabId, {file: "js/capt.plugin.init.js"});
	});
	
	//insert html of useskill in page
	//http://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js
	chrome.tabs.executeScript(tabId, {file: "js/jquery.js"}, function(){
		chrome.tabs.executeScript(tabId, {file: "js/useskill.js"});
	});
	
	chrome.tabs.insertCSS(tabId, {file: "css/useskill.css"});
	
	
	chrome.browserAction.setIcon({path: 'images/icon16on.png', tabId: tabId});
}

function insertOnQuestion(){
	chrome.tabs.executeScript(tabId, {file: "js/jquery.js"});
	chrome.tabs.executeScript(tabId, {file: "js/inserquestion.js"});
	chrome.browserAction.setIcon({path: 'images/icon16on.png', tabId: tabId});
}

//----------------------------------------------------------------------------------------------------------------------------------------------
/*método genérico para realizar ajax*/
function ajax(caminho, tipo, dados){
	var retorno;
	$.ajax({
		url: caminho,
		cache: false,
		type: tipo,
		dataType : 'json',
		async: false,
		data: dados,
		success: function(dados){
			retorno = dados;
		},
		error: function(jqXHR, status, err){
			console.log(jqXHR);
		}
	});
	return retorno;
}
function processXHR(caminho, tipo, dados) {
    var formData = new FormData();
    var xhr = new XMLHttpRequest();

    for(var d in dados){
    	formData.append(d, dados[d]);
    }

    xhr.open(tipo, caminho, true);
    //xhr.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xhr.onload = function(e) {
        console.log("loaded!")
    };

    xhr.send(formData);
    return false;
}
function parseJSON(data) {
	return window.JSON && window.JSON.parse ? window.JSON.parse(data) : (new Function("return " + data))(); 
}
function stringfyJSON(data){
	return window.JSON && window.JSON.stringify ? window.JSON.stringify(data) : (new Function("return " + data))();
}

//----------------------------------------------------------------------------------------------------------------------------------------------
function showNotification(obj){
	if(obj && obj.title && obj.message){
		chrome.notifications.create("useskillPlugin", {   
				type: 'basic', 
				iconUrl: '../images/icon48.png',
				title: obj.title, 
				message: obj.message,
				priority: 0,
			},
			function(newId) {
				if(obj.timeout){
					setTimeout(function(){
						chrome.notifications.clear(newId, function(){});
					}, obj.timeout);
				}
			}
		);
	}
}

//----------------------------------------------------------------------------------------------------------------------------------------------
/*métodos para auxiliar comunicacao com o servidor*/
function concluirOuPularTarefa(idTarefa, isFinished, justificativa){
	var retorno = null;
	var acoesString = stringfyJSON(acoes);
	var dados = {
		'tarefaId': idTarefa, 
		'dados': acoesString,
		'isFinished': isFinished,
	}
	if(justificativa){
		dados['comentario'] = justificativa;
	}
	console.log(dados);
	if(idTarefa){
		retorno = ajax(domainUseSkill+"/tarefa/save/fluxo", 'POST', dados);
		if(retorno.string==true||retorno.string=="true"){
			clearAcoes();
			retorno = true;
		}else{
			//problema no servidor
			retorno = false;
			console.log('prob servidor');
		}
	}else{
		//problema de id = null
		retorno = false;
		console.log('id null');
	}
	return retorno;
}