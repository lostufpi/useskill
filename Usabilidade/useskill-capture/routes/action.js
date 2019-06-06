var db = require('../models')

exports.create = function(req, res) {
	var status = "fail";

	function propComparator(prop) {
	    return function(a, b) {
	        return a[prop] - b[prop];
	    }
	}

	if(req.body){
		var acoes = JSON.parse(req.body.acoes);
		acoes.sort(propComparator('sTime'));

		console.log("Mais "+acoes.length+" acoes:");
		if(acoes[0]){
			console.log(acoes[0].sUsername + " - " + acoes[0].sRole);
		}

		if(acoes){
			for(var idx in acoes){
				var acao = acoes[idx];
				console.log(acao)
				db.ActionOracle.create({
			  		sActionType: acao['sActionType'],
			  		sContent: acao['sContent'],
			  		sPosX: acao['sPosX'],
			  		sPosY: acao['sPosY'],
			  		sTag: acao['sTag'],
			  		sTagIndex: acao['sTagIndex'],
			  		sTime: acao['sTime'],
			  		sUrl: acao['sUrl'],
			  		sContentText: acao['sContentText'],
			  		sClass: acao['sClass'],
			  		sId: acao['sId'],
			  		sName: acao['sName'],
			  		sXPath: acao['sXPath'],
			  		sUserAgent: acao['sUserAgent'],

			  		sClient: acao['sClient'],
			  		sVersion: acao['sVersion'],

			  		sUsername: acao['sUsername'],
			  		sRole: acao['sRole'],

			  		sJhm: acao['sJhm'],
			  		sActionJhm: acao['sActionJhm'],
			  		sSectionJhm: acao['sSectionJhm'],
			  		sStepJhm: acao['sStepJhm'],
			  		
			  		sOracleElements: acao['sOracleElements'],
			  		sOracleUrl: acao['sOracleUrl'],
			  		sOracleVisibleElements: acao['sOracleVisibleElements'],
			  		sOracleVeredict: acao['sOracleVeredict']
			  	});
			}
			status = "success";
		}
	}
	res.json({ status: status });
}

exports.clear = function(req, res) {
	var result = { status: false };

	var hours = req.params.hour;
	var date = new Date();
	if(hours){
		result.val = date.getHours();

		if(hours == date.getHours()){
			//db.Action.destroy();
			result.status = true;
		}
	}
	res.json(result);
}
