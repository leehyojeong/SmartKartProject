
var AWS = require('aws-sdk');
AWS.config.region = 'ap-northeast-1';
var keys = require('./credential-keys.json');
var s3 = new AWS.S3({
	'accessKeyId':keys.accessKeyId,
	'secretAccessKey':keys.secretAccessKey
});

var lambda = new AWS.Lambda({
	"apiVersion":'2015-03-31'});

var frameNum = 0
const exp = {"frameNum":frameNum};

var params = {
	FunctionName : "FruitLambda",
	InvocationType : "RequestResponse",
	Payload : JSON.stringify(exp)
};

//다이나모 DB
AWS.config.update({
	region : "ap-northeast-2"
});
var dydb = new AWS.DynamoDB.DocumentClient();
var table = "product";
var fruit_id = ['6000095799','6000095919','6000095904','6000095921'];

// 파이썬 스크립트 수행
var pyshell = require('python-shell');
//let {pythonshell} = require('python-shell');
var options = {
    mode : 'text',
    pythonPath : '/usr/bin/python2.7',
    pythonOptions : ['-u'],
    scriptPath : './',
    args : []
};


setInterval(function(){
	// console.log(exp);
	if(frameNum>=10){
		frameNum = 0;
	}
	exp["frameNum"]=frameNum;
	params['Payload']=JSON.stringify(exp);
	console.log(params['Payload']);
	
	lambda.invoke(params,function(err,data){
		if(err) console.log(err);
		else{
			console.log("Invoke 성공");
			console.log(JSON.parse(data.Payload));
			var all_data = JSON.parse(data.Payload);
			var kartList = all_data['kart_list'];
			var targetImage = all_data['photoname'];
			
			var category_id = [];
			if(kartList&&kartList.length){
				for (var i=0;i<kartList.length;i++){
					if(kartList[i]=='Apple') 
						category_id.push('6000095799');
					else if(kartList[i]=='Orange')
						category_id.push('6000095919');
					else if(kartList[i]=='Banana'){
						category_id.push('6000095904');
						//console.log(dicObject[fruit_id[2]]);
					}
					else if(kartList[i]=='Lemon')
						category_id.push('6000095921');
				}
			}
			console.log(category_id);

			//var options = {
			//	mode : 'text',
			//	pythonPath : '/usr/bin/python2.7',
			//	pythonOptions : ['-u'],
			//	scriptPath : './',
			//	args : category_id
			//};

			category_id.push(targetImage);
			options['args'] = category_id;

			if(frameNum==9){

			pyshell.PythonShell.run('similar.py',options,function(err,results){
				if(err) throw err;
					//console.log('유사도 코드 실행');
					console.log("9번째")
					console.log(JSON.stringify(results))
					//console.log('results:%j',results);
				});

			}
			frameNum++;
		}
	});
},1000);
