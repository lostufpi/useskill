module.exports = function(sequelize, DataTypes) {
	var ActionDataMining = sequelize.define('ActionDataMinings', {
			sActionType: DataTypes.STRING,
			sContent: DataTypes.TEXT,
			sPosX: DataTypes.BIGINT,
			sPosY: DataTypes.BIGINT,
			sTag: DataTypes.STRING,
			sTagIndex: DataTypes.STRING,
			sTime: DataTypes.BIGINT,
			sUrl: DataTypes.STRING,
			sContentText: DataTypes.TEXT,
			sClass: DataTypes.STRING,
			sId: DataTypes.STRING,
			sName: DataTypes.STRING,
			sXPath: DataTypes.TEXT,
			sUserAgent: DataTypes.TEXT,

			sClient: DataTypes.STRING,
			sVersion: DataTypes.BIGINT,

			sUsername: DataTypes.STRING,
			sRole: DataTypes.STRING,

			sJhm: DataTypes.STRING,
			sActionJhm: DataTypes.STRING,
			sSectionJhm: DataTypes.STRING,
			sStepJhm: DataTypes.STRING,
			
			sOracleElements: DataTypes.BIGINT,
			sOracleUrl: DataTypes.STRING,
			sOracleVisibleElements: DataTypes.BIGINT,
			sOracleVeredict: DataTypes.STRING,

			sDeleted: {
				type: DataTypes.BOOLEAN,
				defaultValue: false
			},
			sMobileConf: DataTypes.STRING, //MILL
			sMobile: DataTypes.INTEGER,	//MILL
			sDuration: DataTypes.INTEGER	//MILL

	  	}, {
	    associate: function(models) {
	   		//Action.hasMany(models.Task)
	    }
	});

  	return ActionDataMining;
}