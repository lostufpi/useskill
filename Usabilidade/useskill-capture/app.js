
var express = require('express')
  , routes  = require('./routes')
  , action    = require('./routes/action')
  , http    = require('http')
  , path    = require('path')
  , db      = require('./models');

//subdomain = '' //localhost
subdomain = '/useskill-capture'

console.log('node app is now running with subdomain: '+subdomain);

var app = express();
app.locals.moment = require('moment');

// all environments
app.set('port', process.env.PORT || 3000);
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.json());
app.use(express.urlencoded());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' === app.get('env')) {
  app.use(express.errorHandler());
}

app.all('*', function(req, res, next) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE');
  res.setHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type");
  next();
});

app.get('/', routes.index);
app.get('/athena', routes.athena);
app.get('/actions', routes.actions);
app.post('/actions/create', action.create);
app.get('/actions/clear/:hour', action.clear);
//app.post('/actions/:user_id/tasks/create', task.create);

db.sequelize
  .sync({ force: false })
  .complete(function(err) {
    if (err) {
      throw err
    } else {
      http.createServer(app).listen(app.get('port'), function(){
        console.log('Express server listening on port ' + app.get('port'))
      })
    }
  });
