var db = require('../models')

function getValueTypeFromView(view) {
	switch(view){
		case 'id':
			return 'id';
		case 'time':
			return 'sTime';
		case 'username':
			return 'sUsername';
		case 'role':
			return 'sRole';
		case 'version':
			return 'sVersion';
		case 'jhm':
			return 'sJhm';
		case 'tag':
			return 'sTag';
		case 'type':
			return 'sActionType';
		case 'client':
			return 'sClient';
		default:
			return null;
	}
	return null;
}

function isNumeric(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

function isArray(obj) {
	return Object.prototype.toString.call( obj ) === '[object Array]';
}

function isObject (obj) {
	return obj !== null && typeof obj === 'object';
}

function isEmptyObject (obj) {
	if(isObject(obj) && Object.keys(obj).length == 0){
		return true;
	}else{
		return false;
	}
}

exports.actions = function(req, res){
	doSearch(req, res, function(actions) {
		res.json({ actions: actions });
	});
}

exports.index = function(req, res){
	doSearch(req, res, function(actions) {
		res.render('index', {
			title: 'Actions',
			actions: actions,
			client: ''
		});
	});
}

exports.athena = function(req, res){
	var client = 'Athena';
	doSearch(req, res, function(actions) {
		res.render('index', {
			title: 'Actions',
			actions: actions,
			client: client
		});
	}, client);
}

function doSearch (req, res, callback, client) {
	var error = false;
	
	if(!isEmptyObject(req.query) || client){
		
		var password = req.query['passwordParam'];
		if (password == null || password != 'server123456') {
			error = true;
		}
		
		var findData = {
			limit: 10,
			order: [['id', 'DESC']]
		};
		
		var 
		orderParam = getValueTypeFromView(req.query['orderParam']),
		orderValue = req.query['orderValue'],
		limit = req.query['limit'],
		paramSearch = req.query['paramSearch'],
		operationSearch = req.query['operationSearch'],
		valueSearch = req.query['valueSearch']
		;
		
		if (req.query['clientParam'] != null) {
			client = req.query['clientParam'];
		}

		if(orderParam != null && orderValue != null){
			findData['order'] = [[orderParam, orderValue]];
		}
		if(isNumeric(limit) && limit > 0){
			findData['limit'] = limit;
		}
		if(isArray(paramSearch) && paramSearch.length > 0){
			var where = {};
			for(var i in paramSearch){
				var param = getValueTypeFromView(paramSearch[i]);
				if(param != null){
					var op = operationSearch[i];
					if(op == 'equal'){
						where[param] = valueSearch[i];
					}else{
						var val = {};
						val[op] = op == 'like' ? '%'+valueSearch[i]+'%' : valueSearch[i];
						where[param] = val;
					}
				}
			}
			if(client){
				where['sClient'] = {'sClient' : client };
			}
			
			findData['where'] = where;
		}else if(client){
			findData['where'] = {'sClient' : client };
		}
		
		db.ActionDataMinings.findAll(findData).success(function(actions) {
			if (callback && typeof callback === 'function'){
				callback.call(this, actions);
			}
		});
		
	} else {
		error = true;
	}
	
	if (error && callback && typeof callback === 'function'){
		callback.call(this, []);
	}
}
