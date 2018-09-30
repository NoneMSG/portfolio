/*
ms('2 days')  // 172800000
ms('1d')      // 86400000
ms('10h')     // 36000000
ms('2.5 hrs') // 9000000
ms('2h')      // 7200000
ms('1m')      // 60000
ms('5s')      // 5000
ms('1y')      // 31557600000
ms('100')     // 100
*/

const loginErrUri='/node/login/err';
var crypto = require('crypto');

var fcm = require("firebase-admin");
var serviceAccount = require("./sms112-179207-firebase-adminsdk-ubg4f-418b7c09ce.json");
fcm.initializeApp({
  credential: fcm.credential.cert(serviceAccount),
  databaseURL: "removed for sercurity reason"
});

var port = 3000;
var calendar = require('node-calendar');
var fs =require('fs');
var mime = require('mime');
var multer = require('multer');
var express = require('express');
var bodyParser=require('body-parser');
var jwt = require('jsonwebtoken');
var mysql = require('mysql');
var nodemailer = require('nodemailer');
var sharp = require('sharp');
var shell = require('shelljs');
var app = express();

//private키 값
var key = 'TEST_KEY123'; //fs.readFileSync('./tokenKey/key.txt', 'utf8');
//file upload option values
const maxSize=314572800; //300MB
const fileCount = 5;



//db 연결정보입력
var mysqlConnection = mysql.createConnection({
  host :'localhost',
  user : 'sms',
  password : 'sms',
  database : 'sms',
  multipleStatements: true,
  dateStrings:true,
  port:3306
});

//email smtp option
var transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 465,
    secure: true, // secure:true for port 465, secure:false for port 587
    auth: {
        user: 'removed for sercurity reason',
        pass: 'removed for sercurity reason'
    }
});

// db연결
mysqlConnection.connect();

//express 설정

app.use(bodyParser.urlencoded({limit:'50mb', extended:true,  parameterLimit: 1000000}));
app.use(bodyParser.json({limit:'50mb',  parameterLimit: 1000000}));

var storage = multer.diskStorage({
  destination: function (req, file, cb) {
    var date = new Date();
    var year = date.getFullYear();
    var month =date.getMonth()+1;
    var date = date.getDate();
    if(month.toString().length==1){
      month = '0'+month.toString();
    }
    if(date.toString().length==1){
      date = '0'+date.toString();
    }
    console.log(year+'-'+month+'-'+date);
    var path = '/uploads/'+year+'/'+month+'/'+date+'/'+req.headers.index+'/';
    if(!fs.existsSync(path)){
      shell.mkdir('-p',path);
      shell.chmod('777', '/uploads/'+year);
      shell.chmod('777', '/uploads/'+year+'/'+month);
      shell.chmod('777', '/uploads/'+year+'/'+month+'/'+date);
      shell.chmod('777', path);
    }
    cb(null, path );
  },
  filename: function (req, file, cb) {
    var extName = file.originalname.substr(file.originalname.lastIndexOf('.'),file.originalname.length);
    cb(null, req.headers.index+Date.now()+extName)
  }
});

var upload = multer({ storage: storage, limits:{fileSize:maxSize} });


//테스트 페이지
app.post('/node/test', (req,res)=>{
  //console.log(JSON.stringify(req.body||0));
  //console.log(req.body.token?undefined:1);
  res.send(req.body);
});
app.get('/node/welcome',function(req,res){
    res.send('welcome sms!');
});
//로그인 중 토큰발행 에러 토큰 만료시
app.get(loginErrUri,(req,res)=>{
  console.log('token err');
  res.send('false');
  return;
});

//DB변경 성공시
app.get('/node/changed',(req,res)=>{
  console.log('db changed successfuly');
  res.send('changed');
  return;
});

app.get('/node/sqlErr',(req,res)=>{
  console.log('sql err');
  res.send('sqlErr');
  return;
});

/*
//////////////////// start user log in code
*/
//로그인 및 토큰발행 5회 틀리면 로그인 및 토큰 발행 x


//log out
app.post('/node/logout',function(req,res){
  //token validation and mysql connection
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
          res.redirect(loginErrUri);
      }else{
          console.log('로그아웃 토큰검증 완료');
          //console.log(decoded);
          var userNo = decoded.no;
          var clientFcmkey = req.body.fcmkey;
        //  console.log("client"+clientFcmkey);
          var checkfmckey = "select fcmkey from user where no=?";
          mysqlConnection.query(checkfmckey, [userNo],function(err,data,next){
              if(err){
                  console.log(err);
              }else{
                  var serverFcmkey = data[0].fcmkey;
                  //  console.log("server"+serverFcmkey);
                  if(serverFcmkey == clientFcmkey ){
                    var sqlStr = "update user set fcmkey=0 where no=?";
                    mysqlConnection.query(sqlStr,[userNo],function(err,data){
                      if(err){
                        console.log(err);
                        res.redirect('/sqlErr');
                      }else{
                      //  console.log(data);
                        if(data.affectedRows==1){
                            res.send('logout');
                        }else{
                            res.send('can not logout');
                        }
                    }
                  });
                }else{
                  res.send('can not logout');
                }
              }
          });
      }
  });
});

//teamleader login check authority
app.post('/node/checkauthority',function(req,res){
  //token validation and mysql connection
var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
      //  console.log('checkauth 토큰검증 완료');
        var role = decoded.role;
        if(role==0){
          res.send('0');
        }else{
          res.send('1');
        }
    }
  });
});

 //fcmkey check
app.post('/node/uidcheck', function(req,res){
    var token = req.body.token;

    //console.log(req.body.fcmkey);
    jwt.verify(token,key,function(err,decoded){
    	if(err){
    		  res.redirect(loginErrUri);
    	}else{
        //admin can't log in mobile device
      //  console.log(decoded.role);
        if(decoded.role==2){
          res.send('denied');
          return;
        }
          var clientfcmkey = req.body.fcmkey||0;
          //console.log(' clientfcmkey  :\n'+clientfcmkey);
    		  console.log(' validateUser 토큰검증 완료');
          var no = decoded.no;
          var sqlStr = "select fcmkey from user where no=?";
          mysqlConnection.query(sqlStr,[no ],function(err,data){
      	    if(err){
          	 	console.log(err);
          		res.redirect('/sqlErr');
        	  }else{
          	//	console.log("server: " + data[0].fcmkey);
            //  console.log("client: " + clientfcmkey);
          		var serverfcmkey = data[0].fcmkey||0;
              //  console.log(serverfcmkey);
                  if(clientfcmkey!=serverfcmkey){
                  //    console.log('uid!=fcmkey');
                      res.send('denied');
                  }else{
                //      console.log('uid==fcmkey');
                      res.send('accept');
                      //res.send('denied');
                  }
            }
      	});
    	}
  });
});

//login
app.post('/node/login', function(req, res){

  //요청된 데이터 body에서 추출
   var id = req.body.id || '_noid';
   var password = req.body.password || '_nopw';
   var uniqueId = req.body.uniqueToken||'no' ;
   var home ;
   //console.log("validation id: "+id+"  password: "+password); //check id and pw
   //console.log("=======");
   //console.log("unique     "+uniqueId);
  if(id == '_noid' || password == '_nopw'){ //checking empty field request, set default id and pw
    console.log('err');
    res.send('request empty field id and pw');
    return;
  }

    var idCheck = "select role ,id, fail_count from user where id=?";
    mysqlConnection.query(idCheck,[id],function(err,data){
        if(err){
          console.log(err);
          res.redirect('/sqlErr');
          return;
        }else{
        //  console.log('id checking data '+data);
          //id check
          if(data[0]!=null){
              if(id==data[0].id){
            //    console.log('id validated !');

              if(data[0].role==3){
            //    console.log('account blocked');
                res.send('blocked'); return;
              }
                //checking fail count if it's more than 5, an account can't log in
                if(data[0].fail_count>=5){
            //        console.log('로그인 실패 5회 초과 계정 로그인 불가능');
                    res.send('5'); return;
                }

                // password checking
                var pwCheck = "select *from user where id=? and password=?";
                mysqlConnection.query(pwCheck,[id, password ],function(err,data){ //user info data
              	    if(err){
                  	 	console.log(err);
                  		res.redirect('/sqlErr');
                        return;
                	  }else{
                  	//	console.log(data);

                  		if(data[0]==null){ // wrong password, update fail count +1
                        console.log('wrong pw');

                          var updateFailCount = "update user set fail_count = fail_count+1 where id=?";
                          mysqlConnection.query(updateFailCount,[id],function(err,data){
                        	    if(err){
                            	 	console.log(err);
                            		res.redirect('/sqlErr');
                          	  }else{
                      //      		console.log(data);
                            		//get fail count for client
                                var selectFailCount = "select fail_count from user where id=?";
                                mysqlConnection.query(selectFailCount,[id],function(err,data){
                              	    if(err){
                                  	 	console.log(err);
                                  		res.redirect('/sqlErr');
                                	  }else{
                        //          		console.log(data);
                                  		res.send((data[0].fail_count).toString()); //send fail count to client
                                	}
                              	});
                          	}
                        	});

                      }else{ //id, pw validated, restore fail count be default value and send token to client
                          home = data[0].home;
                        /* token information*/
                          var payLoad ={};
                          payLoad.no=data[0].no;
                          // payLoad.id = data[0].id;
                          payLoad.password = data[0].password;
                          payLoad.role = data[0].role;
                          payLoad.depNo = data[0].dep_no;
                      //    console.log(payLoad);
                          /*end token infomation*/

                          //token 만드는 함수
                          var token = jwt.sign(payLoad,key,{
                              algorithm: 'HS256', expiresIn:8640000000
                          });
                          console.log("token value :\n"+token);

                          //reset fail_count to 0
                          var resetFailCount = "update user set fail_count=0, fcmkey=? where id=?";
                          mysqlConnection.query(resetFailCount,[uniqueId, id],function(err,data){
                              if(err){
                                console.log(err);
                                res.redirect('/sqlErr');
                              }else{
                    //            console.log(data);
                                res.send([{"token":token,"home":home}]); //토큰값 전송
                            }
                          });
                      }
                	}
              	});
              }else{
                console.log('no match id');
                res.send('no match id');
              }
            }else{
              console.log('no id in the server');
              res.send('no id in the server');
            }
          }
    });
});

// getToken = function(){
//     var token =jwt.sign({
//       id:this.id,
//       pw:this.pw
//     },key);
//     return token;
// };

//토큰 검증 페이지
app.post('/node/authtoken',function(req,res,next){
  var token = req.body.token||0;
  var userId = req.body.id||null;
  //토큰 검증
  jwt.verify(token,key,function(err,decoded){
    if(err){
      console.log('토큰만료됨');
        res.redirect(loginErrUri);
      }else{
        var userNo = decoded.no;
        console.log('토큰검증 api 토큰유효함');
        //console.log(decoded);
        var str = "select *from user where no=?";
        mysqlConnection.query(str,[userNo],function(err,data){
              if(err){

              }else{
                  if(data[0]!=null){
                      res.send('true');
                  }else{
                      console.log('일치하는 정보 없음');
                      res.send('false');
                  }
              }
        });
      }
    });
});


/*
//////////////////// end user log in code
*/

/*
//////////////////// start modify profile code
*/
//profile설정의 사용중인 비밀번호 확인
app.post('/node/profile',function(req,res){
  var token = req.body.token||0;
  console.log("profile checking token value: "+token);

    jwt.verify(token,key,function(err,decoded){
      if(err){
          res.redirect(loginErrUri);
      }else{
          var pwInToken=decoded.password;
          var inputPw = req.body.password||0;
          if(inputPw==0){res.redirect('password_err');return;}
      //    console.log("token내의 비번:"+pwInToken);
      //    console.log("입력받은 비번:"+inputPw);

            if(pwInToken==inputPw){
              console.log(decoded.no+' correct password');
              res.send('correct');
            }else{
              console.log(decoded.no+' wrong password');
              res.send('incorrect');
            }
          }
      });
});

//profile 유저정보출력 : 이름 소속 소속팀장 메일주소 팀이름 팀장 역할은 1번
app.post('/node/profile/info',function(req,res){
    var token = req.body.token||0;
    //console.log(token);
    jwt.verify(token,key,function(err,decoded){

    if(err){
        console.log('토큰만료됨');
        res.redirect(loginErrUri);
      }else{
          var no = decoded.no;
          var role = 1; //팀장
          var sql = "select u.home as home, u.name as name,(select dep_name from department where no=(select dep_no from user where no=?) ) as depName, u.email as email, (select name from user where role=? and dep_no=(select dep_no from user where no=?)) as teamleader from user u where u.no=? and u.dep_no=(select dep_no from user where no=?)";

          mysqlConnection.query(sql,[no,role,no,no,no],function(err,data){
            if(err){
              //res.send('SQL_ERR');
              console.log(err);
            }else{
      //        console.log(data);
              res.send(data);
            }
          });
        }
    });
});

//profile 비번수정
app.put('/node/profile/pw',function(req,res){
  var token = req.body.token||0;
  //console.log(token);
  jwt.verify(token,key,function(err,decoded){
    if(err){
        console.log('토큰만료됨');
        res.redirect(loginErrUri);
      }else{
        console.log('토큰유효함');
        //토큰내부 정보
       var no = decoded.no;

       //바꿀비밀번호
       var newPw = req.body.password||0;
       if(newPw==0){res.redirect(loginErruri);return;}
       //query string ; 유저 비밀번호 수정
       var sqlquery = 'update user set password=? where no=?';
        mysqlConnection.query(sqlquery,[newPw,no],function(err,data){
            if(err){
              res.send('SQL_ERR');
              console.log(err);
            }else{
              console.log('비밀번호 바뀜');
      //        console.log(data);
              res.send('changed');
            }
        });
      }
    });
});

//프로필 홈화면 수정
app.put('/node/profile/home',function(req,res){
  var token = req.body.token||0;
  //token검증
  jwt.verify(token,key,function(err,decoded){
      if(err){
        console.log('토큰만료됨');
        res.redirect(loginErrUri);
      }else{
        console.log('토큰유효함');
        // 0:graph 1:calendar 2: list
        var homePathFlag=req.body.flag||0;
        var userNo = decoded.no;
    //    console.log(homePathFlag);

        //query string ; 유저 홈정보 수정
        var sqlquery = 'update user set home=? where no=?';
        mysqlConnection.query(sqlquery,[homePathFlag, userNo],function(err,data){
            if(err){
              res.send('SQLErr');
              console.log(err);
            }else{
              res.send('changed');
            }
        });
      }
  });
});
/*
//////////////////// end modify profile code
*/


/*
//////////////////// start wrting(insert) contents code
*/
// 달력
app.post('/node/getweek',function(req,res){
    var token = req.body.token||0;

 jwt.verify(token,key,function(err,decoded){
   if(err){
     res.send('token err');
   }else{
      var userNo = decoded.no;
      var calendarFlag = req.body.calFlag||0;
      var tempdate = req.body.date;
      console.log('date from andorid : '+tempdate);
      //console.log('date from andorid length : '+tempdate.length);
      var year = tempdate.substr(0, tempdate.indexOf('-') ) ;//year date length 10중에서 구분자 '-'까지 자르기
      var month = tempdate.substr( tempdate.indexOf('-')+1,(tempdate.lastIndexOf('-')-tempdate.indexOf('-')-1) );//month 구분자 '-' 앞에서 부터 마지막 '-' 구분자 보다 -1까지
      //만약 1자리로 받으면 2자리로 변경 datetime값을 받을 때 문제가 생김
      if(month.length==1){
        var temp = month;
        month = '0'+temp;
      }
      var day = tempdate.substr(tempdate.lastIndexOf('-')+1,tempdate.length);//day; //마지막 구분자 '-'+1 부터 끝까지
      if(day.length==1){
        var temp = day;
        day = '0'+day;
      }
      var aftersubstring = year+'-'+month+'-'+day;

      console.log("after substring :"+ aftersubstring);

      var dateArray = new Array();
      var monthrange = new calendar.Calendar(1).monthdatescalendar(year, month); //연도와 월로 그달의 날짜 가져오기 인자값1은 월요일이 주의시작일
      var arr = new Array(); //1차원 배열
      for(i = 0 ; i < monthrange.length; i++){
          for(j=0; j < monthrange[i].length; j++){
            var subDateTime = ((monthrange[i][j]).toJSON()).substr(0, ((monthrange[i][j]).toJSON()).indexOf('T')); //받아온 달력을 날짜만 가져오기 위한 포맷
            arr.push(subDateTime);
          }
      }
      //매주 월요일과 일요일의 데이터
      var firstweek = arr[0];//0
      var firstendweek = arr[6]; //6
      var secondweek = arr[7]; // 7
      var secondendweek = arr[7*2-1]; //13
      var thirdweek = arr[7*2]; //14
      var thirdendweek = arr[7*3-1]; //21-1 = 20
      var fourthweek = arr[7*3]; //21
      var fourthendweek = arr[7*4-1]; //27
      var fifthweek = arr[7*4];//28
      var fifthendweek = arr[7*5-1];//34

      var sixthweek =0;
      var sixthendweek=0;
       if(arr.length==42){
          sixthweek =arr[7*5];
          sixthendweek =arr[7*6-1];
       }

      //console.log("arrlength"+arr.length);
      //console.log("fifth first :"+fifthweek);
    //  console.log("fifth end :"+fifthendweek);

      var endweekdays = new Array();
      //주의 마지막날 datetime값을 받기위한 리스트
      endweekdays.push(firstendweek);
      endweekdays.push(secondendweek);
      endweekdays.push(thirdendweek);
      endweekdays.push(fourthendweek);
      endweekdays.push(fifthendweek);
      endweekdays.push(sixthendweek);
  //주의 첫날 datetime값을 받기위한 리스트
      var weekdays = new Array();
      weekdays.push(firstweek);
      weekdays.push(secondweek);
      weekdays.push(thirdweek);
      weekdays.push(fourthweek);
      weekdays.push(fifthweek);
      weekdays.push(sixthweek);

  //datetime값 list
      var dateValue  = new Array();
      for( i = 0 ; i < 6 ; i++){
          var calDate = new Date(weekdays[i]);
          dateValue.push(calDate.getTime());
      }
      var endDateValue = new Array();
      for( i = 0 ; i< 6 ; i++){
        var calDate = new Date(endweekdays[i]);
        endDateValue.push(calDate.getTime());
      }

      var compareDate = new Date(aftersubstring); // 처음 받아온 날짜값의 dateTime값
      var customtimeDate = compareDate.getTime();

  //받아온 날짜값이 어떤 주에 속하는지 검증
      if( customtimeDate >= dateValue[0] && customtimeDate <= endDateValue[0] ){
    //    console.log(firstweek);
  //      console.log(firstendweek);
  //      console.log(1);
        var temp={};
        temp.mon=firstweek;
        temp.tue=arr[1];
        temp.wed = arr[2];
        temp.thu = arr[3];
        temp.fri = arr[4];
        temp.sat = arr[5];
        temp.sun=firstendweek;
        if(calendarFlag==1){
          res.send(temp);
          return;
        }
        var sqlstr = "select *from week_planner where (first_weekday=? and end_weekday=?) and user_no=?";
        mysqlConnection.query(sqlstr,[firstweek, firstendweek, userNo],function(err,data){
             if(err){
               console.log(err);
               res.redirect('/sqlErr');
             }else{
               if(data[0]==null){
        //         console.log(data);
                 res.send(temp);
               }else{
      //           console.log(data);
                 console.log('data already exist');
                 res.send('data already exist');
               }
           }
         });
      }else if(customtimeDate >= dateValue[1] && customtimeDate <= endDateValue[1]){
    //    console.log(secondweek);
        var temp={};
        temp.mon=secondweek;
        temp.tue=arr[8];
        temp.wed = arr[9];
        temp.thu = arr[10];
        temp.fri = arr[11];
        temp.sat = arr[12];
        temp.sun=secondendweek;

        if(calendarFlag==1){
          res.send(temp);
          return;
        }

        var sqlstr = "select *from week_planner where (first_weekday=? and end_weekday=?) and user_no=?";
        mysqlConnection.query(sqlstr,[secondweek, secondendweek, userNo],function(err,data){
             if(err){
               console.log(err);
               res.redirect('/sqlErr');
             }else{
               if(data[0]==null){
        //         console.log(data);
                  res.send(temp);
               }else{
        //         console.log(data);
                 console.log('data already exist');
                 res.send('data already exist');
               }
           }
         });
      }else if(customtimeDate >= dateValue[2] && customtimeDate <= endDateValue[2]){
    //    console.log(thirdweek);
        var temp={};
        temp.mon=thirdweek;
        temp.tue=arr[15];
        temp.wed = arr[16];
        temp.thu = arr[17];
        temp.fri = arr[18];
        temp.sat = arr[19];
        temp.sun=thirdendweek;

        if(calendarFlag==1){
          res.send(temp);
          return;
        }

        var sqlstr = "select *from week_planner where (first_weekday=? and end_weekday=?) and user_no=?";
        mysqlConnection.query(sqlstr,[thirdweek,thirdendweek, userNo],function(err,data){
             if(err){
               console.log(err);
               res.redirect('/sqlErr');
             }else{
               if(data[0]==null){
        //         console.log(data);
                  res.send(temp);
               }else{
      //           console.log(data);
                 console.log('data already exist');
                 res.send('data already exist');
               }
           }
         });
      }else if(customtimeDate >= dateValue[3] && customtimeDate <= endDateValue[3]){
    //    console.log(fourthweek);
        var temp={};
        temp.mon=fourthweek;
        temp.tue=arr[22];
        temp.wed = arr[23];
        temp.thu = arr[24];
        temp.fri = arr[25];
        temp.sat = arr[26];
        temp.sun=fourthendweek;

        if(calendarFlag==1){
          res.send(temp);
          return;
        }

        var sqlstr = "select *from week_planner where first_weekday=? and end_weekday=? and user_no=?";
        mysqlConnection.query(sqlstr,[fourthweek, fourthendweek, userNo],function(err,data){
             if(err){
               console.log(err);
               res.redirect('/sqlErr');
             }else{
      //         console.log(data);
               if(data[0]==null){
        //         console.log(data);
                 res.send(temp);
               }else{
      //           console.log(data);
                 console.log('data already exist');
                 res.send('data already exist');
               }
           }
         });
      }else if(customtimeDate >= dateValue[4] && customtimeDate <= endDateValue[4]){ //5주차 또는 4주차
        console.log('fifth week');
        //console.log(fifthweek);
        var temp={};
        temp.mon=fifthweek;
        temp.tue=arr[29];
        temp.wed = arr[30];
        temp.thu = arr[31];
        temp.fri = arr[32];
        temp.sat = arr[33];
        temp.sun=fifthendweek;

        if(calendarFlag==1){
          res.send(temp);
          return;
        }

        var sqlstr = "select *from week_planner where (first_weekday=? and end_weekday=?) and user_no=?";
        mysqlConnection.query(sqlstr,[fifthweek, fifthendweek, userNo],function(err,data){
             if(err){
               console.log(err);
               res.redirect('/sqlErr');
             }else{
               if(data[0]==null){
                 //console.log(data);
                  res.send(temp);
               }else{
              //   console.log(data);
                 console.log('data already exist');
                 res.send('data already exist');
               }
           }
         });
      }else{ //6주차
        console.log('sixth week');
        var temp={};

        if(arr.length==42){
          temp.mon=arr[7*5];
          temp.tue=arr[36];
          temp.wed = arr[37];
          temp.thu = arr[38];
          temp.fri = arr[39];
          temp.sat = arr[40];
          temp.sun=arr[7*6-1];
        }

        //console.log(temp);
        if(calendarFlag==1){
          res.send(temp);
          return;
        }

        var sqlstr = "select *from week_planner where (first_weekday=? and end_weekday=?) and user_no=?";
        mysqlConnection.query(sqlstr,[sixthweek, sixthendweek, userNo],function(err,data){
             if(err){
               console.log(err);
               res.redirect('/sqlErr');
             }else{
               if(data[0]==null){
              //   console.log(data);
                  res.send(temp);
               }else{
              //   console.log(data);
                 console.log('data already exist');
                 res.send('data already exist');
               }
           }
         });
      }
    }
  });
});


//주간계획서 입력...
var weekplnnerMiddleWare = {
  weekplan:function(req,res,next){
    var token = req.body.token||0;

    jwt.verify(token,key,function(err,decoded){
          if(err){
            console.log('토큰검증 오류');
            res.redirect(loginErrUri);
          }else{
            console.log('주간게획서 작성 토큰검증 완료');
            var userNo = decoded.no;
            var firstday=req.body.firstday||null;
            var endday = req.body.endday||null;

            var dayPList = new Array();
            dayPList.push(JSON.parse(req.body.list0||0)); //mon
            dayPList.push(JSON.parse(req.body.list1||0)); //tue
            dayPList.push(JSON.parse(req.body.list2||0)); //wed
            dayPList.push(JSON.parse(req.body.list3||0)); //thu
            dayPList.push(JSON.parse(req.body.list4||0)); //fri

        //    console.log("date value : "+firstday+', '+endday);

            var time = new Date();
            var ftime = new Date(firstday);
            var n = time.getTime();
            var f = ftime.getTime();
      //      console.log(n);
      //      console.log(f);
            if( f <= n  ){
              console.log('이번주의 계획서 작성 불가');
              res.send('이번주에 이번주 계획서 작성불가');
              return;
            }
            var checkingDate = "select *from week_planner where user_no=? and (first_weekday=? or end_weekday=?)";
            mysqlConnection.query(checkingDate,[userNo, firstday, endday],function(err,data){
                if(err){
                }else{
                  //console.log(data);
                  if(data[0]==null){

                    var title = req.body.title||0;
                    var saleGoal = parseInt(req.body.saleGoal||0);
                    var saleTotal = parseInt(req.body.saleTotal||0);

                    //주간계획서 DB에 입력
                    var sqlquery = "insert into week_planner values(null, ?, ?, ?, ?, now(),?,?)";
                    mysqlConnection.query(sqlquery,[userNo,title,saleGoal,saleTotal,firstday,endday],function(err,data){
                        if(err){
                          console.log(err);
                          res.redirect('/sqlErr');
                          return ;
                        }else{
                          var lastinsertno=data.insertId;
                        //  console.log(lastinsertno);
                          console.log('weekplanner inserted');

                          //day planner 데이터들
                          //월~금 데이터 리스트(JSON)


                          var value = new Array();
                          for(i=0; i < dayPList.length;i++){
                            value.push([null, lastinsertno, dayPList[i].day, dayPList[i].date, dayPList[i].plan, dayPList[i].saleGoal]);
                          }

                          //console.log(value);
                          //console.log(dayPList);

                          // for(i=0;i<dayPList.length;i++){
                          //
                          //   var sqlStr = "insert into day_planner values(null, ?, ?, ?, ?, ?, ?)"
                          //   //일간계획 작성 실패로 인한 주간계획서 삭제
                          //   var transcationSql = "delete from week_planner where no=?";
                          //
                          //   mysqlConnection.query(sqlStr,[ lastinsertno, dayPList[i].day.toString(), dayPList[i].date.toString(), dayPList[i].plan.toString(), dayPList[i].total ],function(err,data,next){
                          //     if(err){
                          //         console.log(err);
                          //         console.log('dayplanner db err');
                          //           //일간계획 작성 오류시 주간계획서 삭제
                          //           mysqlConnection.query(transcationSql,[ lastinsertno ],function(err,data){
                          //             if(err){
                          //                 console.log('주간삭제불가');
                          //                 res.redirect('/sqlErr');
                          //                 return;
                          //             }else{
                          //               console.log('주간삭제 성공');
                          //               var temp = "transcation";
                          //               //res.send(temp);
                          //             }
                          //           });
                          //       }else{
                          //         console.log('dayplanner db inserted');
                          //         console.log(data);
                          //
                          //       }
                          //   });
                          //
                          // }
                            var sqlStr = "insert into day_planner (no, week_no, day, date, plan, sale_Goal) values ?"
                            //일간계획 작성 실패로 인한 주간계획서 삭제
                            var transcationSql = "delete from week_planner where no=?";

                            mysqlConnection.query(sqlStr,[ value ],function(err,data){
                              if(err){
                                  console.log(err);
                                  console.log('dayplanner db err');
                                    //일간계획 작성 오류시 주간계획서 삭제
                                    mysqlConnection.query(transcationSql,[ lastinsertno ],function(err,data){
                                      if(err){
                                          console.log('주간삭제불가');
                                          res.redirect('/sqlErr');
                                          return;
                                      }else{
                                        console.log('주간삭제 성공');
                                        var temp = "transcation";
                                        res.send(temp);
                                      }
                                    });
                                }else{
                                  console.log('dayplanner db inserted');
                                }
                            });
                            var temp = "inserted";
                            //day report sale_goal update
                            for(i = 0 ; i < dayPList.length; ++i){
                              var updateSaleGoal="update day_report set sale_goal=? where report_date=? and user_no=?";
                              mysqlConnection.query(updateSaleGoal, [dayPList[i].saleGoal, dayPList[i].date , userNo],function(err,data){
                                    if(err){
                                        console.log(err);
                                    }else{
                              //        console.log(data);
                                    }
                              });
                            }
                          res.send(temp);
                        }
                    });
                  }else{
                    // var temp = { "alreadyexist":[data[0].title] };
                    // console.log(temp.alreadyexist[0]);
                    var temp = {"alreadyexist":data[0].title};
                  //  console.log(temp);
                    res.send(temp);
                  }
                }
            });
          }
    });
    next();
  }
};

//주간계획서 미들웨어 사용 2번째 인자값 선실행
app.post('/node/write/weekplan',[weekplnnerMiddleWare.weekplan],function(req,res,next){
/////////////
});

//일일보고서 작성시 그 주의 해당 일자의 일일계획서중 목표매출액 얻어오는 api
app.post('/node/write/dayreport/salesgoal', function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
        console.log('토큰검증 에러');
        res.redirect(loginErrUri);
      }else{
        console.log('일일보고서 매출액 얻기 토큰검증 완료');
        var userNo = decoded.no;
        var date = req.body.date||0;
        //var day = req.body.day;
        //console.log('날짜:'+date+' 요일 '+day);

        var dateCheck = "select *from day_report where report_date = ? and user_no=? and confirm=3 ";
        mysqlConnection.query(dateCheck,[date, userNo],function(err,data){
            if(err){
              console.log(err);
              res.redirect('/sqlErr');
            }else{
              if(data[0]==null){ //해당 날짜에 승인된게 없을 때
            //    console.log(data[0]);
                var sqlStr = "select b.sale_goal as saleGoal from week_planner a, day_planner b where (b.date=?) and (b.week_no=a.no and a.user_no=?)";
                mysqlConnection.query(sqlStr,[date, userNo],function(err,data){
                    if(err){
                      console.log(err);
                      res.redirect('/sqlErr');
                    }else{
              //        console.log(data);
                      if(data[0]==null){
                        res.send('weekplanner not exist');
                      }else{
                        res.send(data);
                      }
                    }
                });
              }else{
              //  console.log(data);
                res.send([{'already exist':data[0].title}]);
              }
            }
        });
      }
  });
});

//일일보고서 작성시 작성해 놓은 상담일지 리스트
app.post('/node/write/dayreport/attach/consultation',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
        console.log('보고서에 첨부된 상담일지 목록리스트 토큰검증 완료');
        var userNo = decoded.no;

        //var sqlStr = "select a.no, a.title, b.name as '1차', c.name as '2차' from consultation a, first_customer b, second_customer c where (a.user_no=? and a.day_no is null) and a.customer_no=b.no=c.first_customer_no order by reg_date desc";
        var sqlStr = "select no, title, (select name from customer where no=customer_no1) as firstCustomerName ,  (select name from customer where no=customer_no2) as secondCustomerName from consultation where user_no=? and day_no is null" ;
        //"select a.no, a.title, b.no as firstCustomerNo, b.name as firstCustomerName, c.no as secondCustomerNo, c.name as secondCustomerName from consultation a, first_customer b, second_customer c where a.user_no=? and(a.customer_no=b.no and a.second_customer_no=c.no) and day_no is null";
        mysqlConnection.query(sqlStr,[userNo],function(err,data){
          if(err){
            console.log(err);
            res.redirect('/sqlErr');
          }else{
        //    console.log(data);
            res.send(data);
        }
      });
    }
  });
});

//일일보고서 작성
// 0: 미승인
// 1: 승인요청
// 2: 반려
// 3: 승인
app.post('/node/write/dayreport',function(req,res){

  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
        console.log('토큰검증 오류');
        res.redirect(loginErrUri);
      }else{
          console.log('일일보고서 작성 토큰검증 완료');

          // DayReport vo
          var reportDate = req.body.reportDate||0; // yyyy-mm-dd
          var userNo = decoded.no||0;
          var title = req.body.title||0;
          var salesGoal = parseInt(req.body.saleGoal||0);
          var salesTotal = parseInt(req.body.saleTotal||0);
          var startDis = parseFloat(req.body.startDis||0);
          var endDis = parseFloat(req.body.endDis||0);
          var totalDis = parseFloat(req.body.totalDis||0);
          var description = req.body.description||'noinput';
          //var confirm = 0;
          //end of vo

          //첨부파일 관련 데이터들 몇개의 첨부파일을 보내는지 일일보고서 작성시 보내줌
          //일전에 첨부파일 업로드한 후 결과값 중 db에 들어간 첨부파일 경로들의 인덱스값들 데이터 포맷 배열[?,?,?]
          var numberOfAttachment = JSON.parse(req.body.attachmentNo||0);
          var flagOfAttachment = numberOfAttachment.length||0;
    //      console.log('=============================='+numberOfAttachment);
          //console.log(flagOfAttachment);
          //console.log(numberOfAttachment[1]);

          //상담일지 첨부 배열과 길이 데이터 포맷 배열[?,?,?]
          var consultationNo = JSON.parse(req.body.consultationNo||0);
          var countOfConsultation = consultationNo.length||0;
        //  console.log("consultationNo");
      //    console.log(consultationNo);
          // for(i=0; i < countOfConsultation; i++){
          //   console.log(consultationNo[i]);
          // }
          //console.log(Array.isArray(consultationNo));
          //var checkDuplicated = "select *from day_report where (report_date=? and (confirm = 3 or confirm = 1) ) and user_no=?";

          var time = new Date();
          var rtime = new Date(reportDate);
          console.log(reportDate);
          var current = time.getFullYear()+'-'+(time.getMonth()+1)+'-'+time.getDate();
          var now = time.getTime();
          var selected = rtime.getTime();
          if(current==reportDate){
            console.log('오늘날짜');
          }
          else if( selected < now  ){
            console.log('과거날짜 작성불가능');
            res.send('과거날짜 작성불가능');
            return;
          }

          var checkDuplicated = "select *from day_report where (report_date=? and confirm = 3) and user_no=?";
          mysqlConnection.query(checkDuplicated,[reportDate, userNo],function(err,data){
              if(err){

              }else{
                  //데이터가 없다면 새로 작성 가능
                  if(data[0]==null){

                    //일일보고서 작성 쿼리 local db column order no, user_no, title, sale_goal, sale_total, start_dis, end_dis, total_dis, confirm, reg_date, description, report_date
                    var sqlStr = "insert into day_report values(null, ?, ?, ?, ?, ?, ?, ?, 0, now(), ?, ?)";
                    //db
                    mysqlConnection.query(sqlStr,[userNo, title, salesGoal, salesTotal, startDis, endDis, totalDis, description, reportDate],function(err,dData){
                    	if(err){
                        console.log(err);
                        res.redirect('/sqlErr');
                        return;
                     	}else{

                        console.log('day_report inserted');
                        //일일보고서와 첨부파일의 관계형성을 위한 일일보고서 인덱스
                        var lastInsertNo = dData.insertId;
              //          console.log(dData);

                        //update attachment table field value 첨부파일이 있다면 실행
                        if(flagOfAttachment>0){
                          var value = new Array();
                          for(i=0; i<flagOfAttachment; i++){
                              value.push( [lastInsertNo, numberOfAttachment[i] ]);
                          }
                          //multi update 쿼리 작성
                          var quries="";
                          value.forEach(function (item){
                            quries += mysql.format("update attachment set day_no=? where no=? and day_no is null; ",item);
                          });
                          //console.log('-------------------------------');
                          //console.log(quries);
                          //var updateSqlStr = "update attachment set day_no=? where no=?";
                          mysqlConnection.query(quries,function(err,aData){
                              if(err){
                                res.redirect('/sqlErr');
                                console.log(err);
                              }else{
                                  //console.log(data);
                                  //데이터 연결 에러
                                  if(aData[0].affectedRows!=1){console.log(aData[0]); res.send('첨부파일연결실패'); return;}
                                  console.log('첨부파일 연결완료');
                          //        console.log(aData[0]);
                              }
                          });
                        } //첨부파일 없을 때 일일보고서만 저장됨
                            if(countOfConsultation>0){
                            var consulValue = new Array();
                            for(i=0; i<countOfConsultation; i++){
                                consulValue.push( [lastInsertNo, consultationNo[i] ]);
                            }
                            //상담일지 연결
                            var linkConsultationQuery="";
                            consulValue.forEach(function (item){
                              linkConsultationQuery += mysql.format("update consultation set day_no=? where no=? and day_no is null;" ,item);
                            });

                            mysqlConnection.query(linkConsultationQuery,function(err,cData){
                                if(err){
                                  console.log(err);
                                  res.redirect('/sqlErr');
                                }else{
                                  //데이터 연결 에러
                                  //console.log(cData[0].affectedRows);
                                  // var updatedResult = cData[0].affectedRows||0;
                                  // if(updatedResult!=1){console.log(cData[0]); res.send('상담일지연결실패'); return;}
                                //  console.log(cData[0]);
                                  console.log('상담일지 연결완료 out of attach');
                              }
                            });
                          }
                          res.send('inserted');
                    	}
                    });

                  }else{// 데이터가 있다면 작성 불가능
                    console.log('it is already exist');
                //    console.log(data[0]);
                    res.send({'alreadyexist':'alreadyexist'});
                  }
              }
          });
      }
  });
});

//comment of day report
app.post('/node/write/comment', function(req,res){
  //jwt token validation
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
  	if(err){
      console.log('토큰검증 에러');
      res.redirect(loginErrUri);
  	}else{
      console.log('코멘트 작성 토큰검증 완료');
      var userNo = decoded.no;
      var dayNo = parseInt(req.body.dayNo||0);
      var description = req.body.description||0;
      var sqlStr = "insert into comment values(null, ?, ?, ?, now() )";

      mysqlConnection.query(sqlStr,[ userNo, dayNo, description ],function(err,data){
      	if(err){
      		console.log(err);
          res.redirect('/sqlErr');
      	}else{
      		console.log('--- inserted');
          res.send('inserted');
      	}
      });
  	 }
  });
});

//상담일지 작성
app.post('/node/write/consultation',function(req,res){

  var token = req.body.token||0;
    jwt.verify(token,key,function(err,decoded){
      if(err){
        console.log('토큰검증 실패');
        res.redirect(loginErrUri);
      }else{
        console.log('상담일지 작성 토큰검증 완료');

        //상담일지 vo
        var userNo = decoded.no;
        //var dayNo = parseInt(req.body.dayNo);
        var customerNo = parseInt(req.body.customerNo||0);
        var secondCustomerNo = req.body.secondcustomerNo||null;
        var title = req.body.title||0;
        var description = req.body.description||0;
    //    console.log('second customer number : '+secondCustomerNo);
          //현재 로컬 db 컬럼 순서 no, day_no, user_no, customer_no, title, description, reg_date
          var sqlStr = "insert into consultation values(null, null, ?, ?, ?, ?, ?, now())";
          mysqlConnection.query(sqlStr,[userNo, customerNo, secondCustomerNo, title, description],function(err,data){
            if(err){
              console.log(err);
              res.redirect('/sqlErr');
            }else{
              console.log('inserted');
      //        console.log(data);
              res.send('inserted');
            }
        });
      }
    });
});

//상담일지 작성시 필요한 데이터 1차 고객리스트 가져오기
app.post('/node/write/consultation/listofcustomer', function(req,res){
    var token = req.body.token||0;
    jwt.verify(token,key,function(err,decoded){
        if(err){
          res.redirect(loginErrUri);
        }else{
        console.log('고객리스트(스피너) 토큰검증 완료');
        var flag = parseInt(req.body.flag||0);
        if(flag==0){
          res.send('noflag'); return;
        }
        if(flag==1){
          var sqlStr = "select *from customer where classification=1 ";
          mysqlConnection.query(sqlStr,function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
              }else{
                console.log('1차고객리스트');
      //          console.log(data);
                res.send(data);
              }
          });
        }
        if(flag==2){
          var sqlStr = "select *from customer  where classification=2 ";
          mysqlConnection.query(sqlStr,function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
              }else{
                console.log('2차고객리스트');
        //        console.log(data);
                res.send(data);
              }
          });
        }
      }
    });
});

//////////////// 일지 작성시 필요한 데이터 작성자(토큰)정보 (이름, 소속, 부서 등 )
app.post('/node/write/info', function(req,res){ //userinfo
    var token = req.body.token||0;
    //console.log(token);
    jwt.verify(token,key,function(err,decoded){
      	if(err){
      		res.redirect(loginErrUri);
      	}else{
      		console.log('일지작성 때 사용자 정보 api 토큰검증 완료');
          //글작성시 유저의 정보 쿼리 소속, 이름, 팀장, 작성일(???)
          var userNo = decoded.no;
          var role = 1;
          var sqlStr = "select u.role as role, u.name as name,(select dep_name from department where no=(select dep_no from user where no=?) ) as depName, (select name from user where role=? and dep_no=(select dep_no from user where no=?)) as teamleader from user u where u.no=? and u.dep_no=(select dep_no from user where no=?)";

        mysqlConnection.query(sqlStr,[userNo, role, userNo, userNo, userNo  ],function(err,data){
          	if(err){
          		console.log(err);
              res.redirect('/sqlErr');
          	}else{
          		console.log('일지작성시 유저정보');
        //      console.log(data[0]);
              res.send(data);
          	}
        });
    	}
  });
});
/*
//////////////////// end wrting(insert) contents code
*/


/*
//////////////////// start upload file and dowload code
*/

app.post('/node/delete/attachment',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
        console.log('토큰검증 완료');
        var userNo = decoded.no;
        var author = req.body.author;
        var attachNo = req.body.attachNo;
        if(userNo!=author){
          console.log('작성자가 아니다.');
          res.send('noPermission');
        }
        var getpath = "select path from attachment where no=?"
        mysqlConnection.query(getpath,[ attachNo],function(err,data){
          if(err){
            console.log(err);
            res.redirect('/sqlErr');
          }else{
            //console.log(data[0].path);
              var path = data[0].path;
              var sqlStr = "delete from attachment where no=?";
                mysqlConnection.query(sqlStr,[ attachNo],function(err,data){
                  if(err){
                    console.log(err);
                    res.redirect('/sqlErr');
                  }else{
                    var thumnailPath = path.substr(0, path.lastIndexOf('/'));
                    var nameofFile = path.substr(path.lastIndexOf('/')+1, path.length);
                    var thumbpath= thumnailPath+'/thumbnail/thumbnail_'+nameofFile;

                    shell.rm('-f', [thumbpath, path] );
                    res.send('done');
                }
              });
        }
      });
    }
  });
});

//md5 checksum 함수 업르드된 파일을 읽어서 md5값 추출 후 반환
md5checksum = function(file,md5){
  var checksum = crypto.createHash('MD5');
  var temp = fs.readFileSync(file.path);
  shell.chmod('777',file.path);
  //var temp = fs.readFileSync('D:/upload1/s3.jpg');
  checksum.update(temp);
  var md5str = checksum.digest('hex');
//  console.log("server : "+md5str);
//  console.log("client : "+md5);
  if(md5==md5str){
      return md5str;
  }else{
    return null;
  }
  return null;
};

//multiuploads
//app.post('/upload', mupload.array('files', fileCount), function(req,res){
app.post('/node/upload', [upload.array('files',fileCount)], function(req,res){

    var token = req.headers.token;
    var index = req.headers.index;
    var files = req.files||null;
    var author = req.headers.author;
  //  console.log(files);

      jwt.verify(token,key,function(err,decoded){
      	if(err){
      		res.redirect(loginErrUri);
      	}else{
          if( !(author == decoded.no) ){
              res.send('작성자 정보 불일치');
              return ;
            }

            console.log('파일 업로드 토큰검증 완료');
            //실제 file들의 데이터 리스트
            var md5 = JSON.parse(req.headers.md5);
            var mime = JSON.parse(req.headers.mime);
            var name = JSON.parse(req.headers.name);

            if(files==null){res.send('no data'); return;}
            //총 파일의 개수
            var filesLength= req.files.length;
            //md5 검증을 위한 리스트
            var md5List = new Array();
            for(i = 0 ; i < filesLength ; i++){
                if(null == md5checksum(files[i],md5[i])){
                    for(j=0; j<= i ; j++)
                      shell.rm('-f',[files[j].path]);
                    res.send('wrong md5');
                    return;
                }else{
                  md5List.push(md5checksum(files[i],md5[i]));
                }
            }

            if(!fs.existsSync(files[0].destination+'thumbnail')){
              shell.mkdir('-p',files[0].destination+'thumbnail');
              shell.chmod('777', files[0].destination+'thumbnail');
            }
            for(i = 0 ; i<filesLength;++i){
              if(mime[i]=='jpg'||mime[i]=='png'){
                sharp(files[i].path)
                .rotate()
                .resize(200)
                .toFile(files[i].destination+'thumbnail/thumbnail_'+files[i].filename).then().catch();
                shell.chmod('777', files[i].destination+'thumbnail/thumbnail_'+files[i].filename);
              }
            }


        //  console.log(md5List);
          // //DB에 넣기위해 첨부파일의 정보 리스트에 저장

          //DB 멀티 쿼리문 작성을 위해 새로운 배열 작성
          var value = new Array();
          for( i = 0 ; i < filesLength ; i++){
            var originalName = name[i];
            var extName = originalName.substr(originalName.lastIndexOf('.'),originalName.length);
            var path = files[i].destination+files[i].filename;
            var size = files[i].size;
            var md5 = md5List[i];
            value.push([null, index, path, originalName, extName, size, md5]);
          }
          //query of attachment insertion

          var sqlStr = "insert into attachment (no, day_no, path, original_name, ext_name, size, md5 ) values ?";
          mysqlConnection.query(sqlStr,[ value ],function(err,data){
            if(err){
              console.log(err);
                res.redirect('/sqlErr');
            }else{
                //console.log(data);
                res.send("done").end();
              //  return;
            }
          });

          // var checkConfirm = "select confirm from day_report where no=?"
          // mysqlConnection.query(checkConfirm,[ index ],function(err,data){
          //     if(err){console.log(err);res.send('sqlErr');return;}
          //       if(data[0].confirm==0){
          //
          //       }else{
          //         console.log('승인된보고서');
          //         res.send('confirmedReport').end();
          //         //return ;
          //       }
          //     });
      	}
   });
});


//file download
app.post('/node/download',function(req,res){
    var token = req.body.token||0;
  //  console.log(token);
    jwt.verify(token,key,function(err,decoded){
        if(err){
          res.redirect(loginErrUri);
        }else{
          console.log('다운로드 토큰검증 완료');
          var dayNo = req.body.dayNo||null;
          var index = req.body.index||null;
      //    console.log("dayno: "+dayNo);console.log("index: "+index);
          var queryStr  = "select *from attachment where day_no=? and no=?";
          mysqlConnection.query(queryStr, [dayNo, index], function(err,data){
              if(err){
                console.log('=============mysql err===============');
              }else{
                if(data[0]!=null){
                  //console.log(data.length);

                  var path = data[0].path;
                  var name = data[0].original_name;
                  var md5 = data[0].md5;
                  var size = data[0].size;
                  var type = data[0].ext_name;

                  var mimeValue = mime.lookup(path+type);
          //        console.log(mimeValue);
                  var temp = mimeValue.substr(0,mimeValue.lastIndexOf('/'));//day;
          //        console.log(temp);
                  var temp = mimeValue.substr(0,mimeValue.lastIndexOf('/'));//day;
        //          console.log(temp);

                  // if(temp=='image'){
                  //   res.setHeader('Content-type',mimeValue);
                  //   res.setHeader('orignalname', name);
                  //   var p1 = path.substr(0,path.lastIndexOf('/'));
                  //   var filename= path.substr(path.lastIndexOf('/')+1,path.length);
                  //   //console.log(p1);
                  //   //console.log(filename);
                  //   //console.log(p1+'/thumbnail/thumbnail_'+filename);
                  //   res.download(p1+'/thumbnail/thumbnail_'+filename);
                  // }else{
                  //   res.setHeader('Content-type',mimeValue);
                  //   res.setHeader('orignalname', name);
                  //   res.download(path);
                  // }
                  res.setHeader('Content-type',mimeValue);
                  res.setHeader('orignalname', name);
                  res.download(path);

                }else{
                  console.log('no match data');
                  res.send('no match data').end();
                }
              }
          });
        }
    });
});

/*
//////////////////// end upload file and dowload code
*/


/*
/////////////////////start seraching contents code
*/
// department listing in spinner
// app.post('/view/spinner/department', function(req,res){
//   var token = req.body.token||0;
//
//   jwt.verify(token,key,function(err,decoded){
//     if(err){
//         res.redirect(loginErrUri);
//     }else{
//         console.log('부서검색 스피너 토큰검증 완료');
//         console.log(decoded);
//         var depNo = decoded.depNo;
//         if(decoded.role!=1 ||decoded.role!='1'){
//           res.send('denied');
//           return;
//         }
//
//         var sqlStr = "select no, dep_name as depName from department where no= ?";
//         mysqlConnection.query(sqlStr,[depNo],function(err,data){
//           if(err){
//             console.log(err);
//             res.redirect('/sqlErr');
//           }else{
//             console.log('부서검색 스피너');
//             console.log(data);
//             if(data[0]==null){
//                 res.send('no data');
//             }else{
//                 res.send(data);
//             }
//         }
//       });
//     }
//   });
// });

// user listing in spinner after department list selected
app.post('/node/view/spinner/user', function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
        console.log('유저검색 스피너 토큰검증 완료');
    //    console.log(decoded);
        if(decoded.role!=1 ){
          res.send('denied');
          return;
        }
        //부서번호
        var depNo = decoded.depNo;
        var userNo = decoded.no;
        var sqlStr = "select no, name from user where dep_no=? and no!=?";
        mysqlConnection.query(sqlStr,[depNo, userNo],function(err,data){
          if(err){
            console.log(err);
            res.redirect('/sqlErr');
          }else{
      //      console.log(data);
            if(data[0]==null){
                res.send('no data');
            }else{
                res.send(data);
            }
        }
      });
    }
  });
});

//listing serach by name and department
app.post('/node/view/spinner/serachbyName', function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
        console.log('스피너검색 (이름으로)토큰검증 완료');

        //if role is not 1 send denied message
        if(decoded.role!=1 ){
          res.send('denied');
          return;
        }
        //else

        //flag; planner type 0: weekPlanner 1: dayReport 2: consultation default :0
        var flag = parseInt(req.body.flag||0);
        var userNo = parseInt(req.body.userNo);
        var index = parseInt(req.body.index||0);
        var sqlStr = "";

        switch(flag){
          case 0:{ //week planner
            //listing with index or not
            if(index==0){
              console.log('no index');
              sqlStr= "select no, title, reg_date as regDate from week_planner where user_no=? order by reg_date desc limit 0,5";
            }else{
              console.log('index exist');
              sqlStr= "select no, title, reg_date as regDate from week_planner where user_no=? and no<="+index+" order by reg_date desc limit 0,5";
            }
            mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
              }else{
      //          console.log(data);
                if(data[0]==null){
                    res.send('no data');
                }else{
                    res.send(data);
                }
              }
          });
          }break;
          case 1:{ //daily report
            //listing with index or not
            if(index==0){
              console.log('no index');
              sqlStr= "select no, title, reg_date as regDate, confirm from day_report where user_no=? and confirm !=0  order by reg_date desc limit 0,5";
            }else{
              console.log('index exist');
              sqlStr= "select no, title, reg_date as regDate, confirm from day_report where (user_no=? and confirm !=0) and no<="+index+" order by reg_date desc limit 0,5";
            }
            mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
              }else{
      //          console.log(data);
                console.log('일일보고서 검색결과');
                if(data[0]==null){
                    res.send('no data');
                }else{
                    res.send(data);
                }
              }
          });
          }break;
          case 2:{ // consultation
            if(index==0){
              console.log('no index');
              sqlStr= "select no, title, reg_date as regDate from consultation where user_no=? order by reg_date desc limit 0,5";
            }else{
              console.log('index exist');
              sqlStr= "select no, title, reg_date as regDate from consultation where user_no=? and no<="+index+" order by reg_date desc limit 0,5";
            }
            mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
              }else{
        //        console.log(data);
                console.log('consultation serach result');
                if(data[0]==null){
                    res.send('no data');
                }else{
                    res.send(data);
                }
              }
          });
          }break;
        }
    }
  });
});

//seraching by keyword and title 0:주간, 1:일일, 2:상담, default:0
app.post('/node/view/searching',function(req,res){
  //token validation and mysql connection
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
          res.redirect(loginErrUri);
          return ;
      }else{
          console.log('키워드검색 토큰검증 완료');
          var flag = parseInt(req.body.flag||0);
          var userNo = decoded.no;
          var role = decoded.role;
          var keyword ='%'+(req.body.keyword||null)+'%';
          var sqlStr = "";

          //0:주간, 1:일일, 2:상담
          switch(flag){
            case 0:{
                if(role==0){
                    sqlStr = "select *from week_planner where title like ? and user_no=?  group by no";
                }else{
                    sqlStr = "select *from week_planner where title like ?  group by no";
                }
                mysqlConnection.query(sqlStr,[keyword, userNo],function(err,data){
                  if(err){
                    console.log(err);
                    res.redirect('/sqlErr');
                  }else{
      //              console.log(data);
                    console.log('weekplanner keyword seraching result');
                    if(data[0]==null){
                        res.send('no data');
                    }else{
                        res.send(data);
                    }
                }
              });
            }break;

            case 1:{
                if(role==0){
                    sqlStr = "select *from day_report where (title like ? or description like ?) and user_no=? group by no";
                }else{
                    sqlStr = "select *from day_report where (title like ? or description like ?) and confirm=3 group by no";
                }
                mysqlConnection.query(sqlStr,[keyword, keyword, userNo  ],function(err,data){
                  if(err){
                    console.log(err);
                    res.redirect('/sqlErr');
                  }else{
            //        console.log(data);
                    console.log('dayreport keyword seraching result');
                    if(data[0]==null){
                        res.send('no data');
                    }else{
                        res.send(data);
                    }
                }
              });
            }break;

            case 2:{
                if(role==0){
                    sqlStr = "select *from consultation where (title like ? or description like ?) and user_no=?  group by no";
                }else{
                    sqlStr = "select *from consultation where (title like ? or description like ?) group by no";
                }

                mysqlConnection.query(sqlStr,[keyword, keyword, userNo  ],function(err,data){
                  if(err){
                    console.log(err);
                    res.redirect('/sqlErr');
                  }else{
            //        console.log(data);
                    console.log('consultation keyword seraching result');
                    if(data[0]==null){
                        res.send('no data');
                    }else{
                        res.send(data);
                    }
                }
              });
            }break;
          }
      }
  });
});
/*
////////////////////////end of seraching code
*/


/*
//////////////////////// start contents list code
*/
// weekplanner listing
app.post('/node/view/weekplanner',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    	if(err){
    		  res.redirect(loginErrUri);
    	}else{
    		  console.log('주간계획서 리스트 토큰검증 완료');
          var userNo= decoded.no;
          var role = decoded.role;
          var teamMemberNo= req.body.userNo;
        //  console.log('userNo : '+teamMemberNo);
          //var depNo = decoded.depNo;
          var index = parseInt(req.body.index||0);
          //console.log( 'userNo: '+userNo+' index no: '+index+' role:'+role+' depNo:'+depNo);
      //    console.log(userNo);
          var sqlStr ="";
          //user role checking
          if(role==0){

            //list indexing order checking
            if(index==0){
                console.log('no index');
                sqlStr = "select no, title, reg_date as regDate from week_planner where user_no=? order by reg_date desc limit 0, 5";
            }else{
                console.log('index exist');
                sqlStr = "select no, title, reg_date as regDate from week_planner where user_no=? and no < "+index+" order by reg_date desc limit 0, 5";
               }
               // user role = 0 mysql start
          	      mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
                    if(err){
                	 	     console.log(err);
                		     res.redirect('/sqlErr');
              	    }else{
              //          console.log(data);
                        console.log('week planner list');
                        if(data[0]==null){
                            console.log('no data');
                            res.send('no data');
                        }else{
                            res.send(data);
                        }
                    }
            	    });
              // user role = 0 mysql end

          //user role = 1 mysql start
          }else{
            if(teamMemberNo==-1){
              console.log('teamLeader listing');
              if(index==0){
                  sqlStr = "select a.no as no, a.title as title, a.reg_date as regDate, b.name as name 	from week_planner a, user b	where a.user_no = b.no and b.dep_no=(select dep_no from user where no=?) order by a.reg_date desc limit 0,5";
              }else{
                  sqlStr = "select a.no as no, a.title as title, a.reg_date as regDate, b.name as name 	from week_planner a, user b	where (a.user_no = b.no and a.no<"+index+") and b.dep_no=(select dep_no from user where no=?) order by a.reg_date desc limit 0,5";
              }
            }else{
              console.log('teamLeader listing');
              if(index==0){
                  console.log('no index');
                  sqlStr = "select no, title, reg_date as regDate from week_planner where user_no="+teamMemberNo+" order by reg_date desc limit 0, 5";
              }else{
                  console.log('index exist');
                  sqlStr = "select no, title, reg_date as regDate from week_planner where user_no="+teamMemberNo+" and no < "+index+" order by reg_date desc limit 0, 5";
                 }
            }

            mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
              }else{
            //    console.log(data);
                console.log('week planner list role 1');
                if(data[0]==null){
                    res.send('no data');
                }else{
                    res.send(data);
                }
              }//user role = 1 mysql end
          });
        }
    	}
  });
});

// dailyReport listing
app.post('/node/view/dayreport',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    	if(err){
    		  res.redirect(loginErrUri);
    	}else{
    		  console.log('일일보고서 리스트 토큰검증 완료');
          var userNo= decoded.no;
          //console.log(userNo);
          var teamMemberNo= req.body.userNo;
    //      console.log('userNo : '+teamMemberNo);
          var role = decoded.role;
          //var depNo = decoded.depNo;
          var index = parseInt(req.body.index||0);
          //console.log( 'userNo: '+userNo+' index no: '+index+' role:'+role+' depNo:'+depNo);

          var sqlStr ="";

          //user role checking
          if(role==0){ // usual user

            //list indexing order checking
          if(index==0){
              console.log('no index');
              sqlStr= "select no, title, reg_date as regDate, confirm from day_report where user_no=? order by reg_date desc limit 0,5";
            }else{
              console.log('index exist');
              sqlStr= "select no, title, reg_date as regDate, confirm from day_report where user_no=? and no<"+index+" order by reg_date desc limit 0,5";
            }
               // user role = 0 mysql start
          	      mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
                    if(err){
                	 	     console.log(err);
                		     res.redirect('/sqlErr');
              	    }else{
            //            console.log(data);
                        console.log('dayreport planner list');
                        if(data[0]==null){
                            res.send('no data');
                        }else{
                            res.send(data);
                        }
                    }
            	    });
              // user role = 0 mysql end

          //user role = 1 mysql start
          }else{
            console.log('teamLeader listing');
            if(teamMemberNo==-1){ //팀 전체
              if(index==0){
                console.log('no index');
                sqlStr= "select a.no as no, a.title as title, a.reg_date as regDate, b.name as name, a.confirm from day_report a, user b	where (a.user_no = b.no and confirm !=0) and b.dep_no=(select dep_no from user where no=?) order by a.reg_date desc limit 0,5";
              }else{
                console.log('index exist');
                sqlStr= "select a.no as no, a.title as title, a.reg_date as regDate, b.name as name, a.confirm from day_report a, user b	where (a.user_no = b.no and a.no<"+index+") and (b.dep_no=(select dep_no from user where no=?) and confirm !=0) order by a.reg_date desc limit 0,5";
              }
            }else{ //팀 개인별
              if(index==0){
                console.log('no index');
                sqlStr= "select no, title, reg_date as regDate, confirm from day_report where confirm=3 and user_no="+teamMemberNo+" order by reg_date desc limit 0,5";
              }else{
                console.log('index exist');
                sqlStr= "select no, title, reg_date as regDate, confirm from day_report where confirm=3 and user_no="+teamMemberNo+ " and no<"+index+" order by reg_date desc limit 0,5";
              }
            }
            mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
              }else{
          //      console.log(data);
                console.log('dayreport planner list role1');
                if(data[0]==null){
                    res.send('no data');
                }else{
                    res.send(data);
                }
              }//user role = 1 mysql end
          });
        }
    	}
  });
});

// consultation listing
app.post('/node/view/consultation',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    	if(err){
    		  res.redirect(loginErrUri);
    	}else{
    		  console.log('상담일지 리스트 토큰검증 완료');
          var userNo= decoded.no;
          var role = decoded.role;
          var teamMemberNo= req.body.userNo;
      //    console.log('userNo : '+teamMemberNo);

          var index = parseInt(req.body.index||0);
      //    console.log('index number:'+ index);
          //console.log( 'userNo: '+userNo+' index no: '+index+' role:'+role+' depNo:'+depNo);
          //console.log('========================================consultation');
          var sqlStr ="";
          //user role checking
          if(role==0){
            //list indexing order checking
            if(index==0){
              console.log('no index');
              //sqlStr= "select no, user_no, day_no, title, reg_date as regDate from consultation where user_no=? order by reg_date desc limit 0,5";
              sqlStr= "select no, user_no, day_no, title, reg_date as regDate from consultation where user_no=? order by reg_date desc limit 0,6";
            }else{
              console.log('index exist');
              //sqlStr= "select no, user_no, day_no, title, reg_date as regDate from consultation where user_no=? and no<"+index+" order by reg_date desc limit 0,5";
              sqlStr= "select no, user_no, day_no, title, reg_date as regDate from consultation where user_no=? and no<"+index+" order by reg_date desc limit 0,6";
            }
               // user role = 0 mysql start
          	      mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
                    if(err){
                	 	     console.log(err);
                		     res.redirect('/sqlErr');
              	    }else{
          //              console.log(data);
                        console.log('consultation planner list');
                        if(data[0]==null){
                            res.send('no data');
                        }else{
                            res.send(data);
                        }
                    }
            	    });
              // user role = 0 mysql end

          //user role = 1 mysql start
          }else{
            if(teamMemberNo==-1){
                if(index==0){
                  console.log('no index');
                  sqlStr= "select a.no as no, a.user_no as user_no, a.day_no, a.title as title, a.reg_date as regDate, b.name as name from consultation a, user b	where a.user_no = b.no and b.dep_no=(select dep_no from user where no=?) order by a.reg_date desc limit 0,5";
                }else{
                  console.log('index exist');
                  sqlStr= "select a.no as no, a.user_no as user_no, a.day_no, a.title as title, a.reg_date as regDate, b.name as name from consultation a, user b	where (a.user_no = b.no and a.no<"+index+") and b.dep_no=(select dep_no from user where no=?) order by a.reg_date desc limit 0,5";
                }
            }else{
                if(index==0){
                  console.log('no index');
                  sqlStr= "select no, user_no, day_no, title, reg_date as regDate from consultation where user_no="+teamMemberNo+" order by reg_date desc limit 0,6";
                }else{
                  console.log('index exist');
                  sqlStr= "select no, user_no, day_no, title, reg_date as regDate from consultation where user_no="+teamMemberNo+" and no<"+index+" order by reg_date desc limit 0,6";
                }
            }

            mysqlConnection.query(sqlStr,[ userNo ],function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
              }else{
            //    console.log(data);
                console.log('consultation planner list role1');
                if(data[0]==null){
                    res.send('no data');
                }else{
                    res.send(data);
                }
              }//user role = 1 mysql end
          });
        }
    	}
  });
});
/*
//////////////////////// end of contents listing code
*/

/* specific view of content */
//week plan
app.post('/node/view/weekplanner/content',function(req,res){
  //token validation and mysql connection
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    	if(err){
    		  res.redirect(loginErrUri);
    	}else{
    		  console.log('주간계획서 상세보기 토큰검증 완료');
          //게시글 인덱스 번호값
          var index = req.body.index||0;
      //    console.log("index : "+index);
          var userNo = decoded.no;
          var role = decoded.role;
          var contentData = {};

          var sqlStr="";
          //팀장 이외에 다른사람의 게시글 접근시 에러
          if(role==0){
              sqlStr = "select *from week_planner where no=? and user_no=?";
          }else{
              sqlStr = "select a.*, (select name from user where no=a.user_no)as author from week_planner a where no=?";
          }

          mysqlConnection.query(sqlStr,[ index, userNo ],function(err1,data){
      	    if(err){
          	 	console.log(err1);
          		res.redirect('/sqlErr');
        	  }else{

          		//console.log("data : "+data[0]);
              //팀장이 아닐 때 토큰값으로 유저 검증 해당유저의 게시글이 아니라면 쿼리문으로 불러올 수 없다.
              if(data[0]==null){console.log('err');res.send('denied');return;}
              var weekNo = parseInt(data[0].no);
          //    console.log(weekNo);
              contentData.week = data;
          //    console.log("=-================"+contentData.week);
              var secondSql = "select *from day_planner where week_no=?";
              mysqlConnection.query(secondSql,[ weekNo ],function(err2,sdata){
                  if(err){
                    console.log(err2);
                		res.redirect('/sqlErr');
                    return;
                  }else{
                    console.log('=====================');
            //        console.log(sdata);
                    contentData.daily=sdata;
                    res.send(contentData);
                  }
              });
        	   }
      	});
    	}
  });
});

//view comment
app.post('/node/view/comments',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    	if(err){
    		  res.redirect(loginErrUri);
    	}else{
    		  console.log('일일보고서 댓글 보기 토큰검증 완료');
          var dayNo = req.body.index||0;
          if(dayNo==0){console.log('no Data'); res.send('wrong index number'); return;}
          //var sqlStr = "select *from comment where day_no=?";
          var sqlStr = "select no, user_no, day_no, description, reg_date, (select name from user where no=user_no) as name from comment where day_no=?";
          mysqlConnection.query(sqlStr,[dayNo],function(err,data){
      	    if(err){
          	 	console.log(err);
          		res.redirect('/sqlErr');
        	  }else{
        //  		console.log(data[0]);
              console.log('comment contents');
              if(data[0]==null){
                  res.send('no data');
              }else{
                  res.send(data);
              }
        	}
      	});
    	}
  });
});



//daily report
app.post('/node/view/dayreport/content',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
          console.log('token 정보 불일치');
          res.redirect(loginErrUri);
          return;
      }else{
          console.log('보고서 상세보기 토큰검증 완료');
          var index = req.body.index||0;
          var userNo = decoded.no;
          var role = decoded.role;
          var date = req.body.date||0;

        //  console.log("get data date===== "+date);
      //    console.log("get data index===== "+index);
          //console.log("get data===== "+role);
      //    console.log("get data userNo===== "+userNo);
          var contentData={};

          //주간계획서의 일일계획서 누르면 오는 상세보기
          if(date!=0){
              console.log('주간계획서에서 dayreport 요청');
              var fromweekplanner = "select *from day_report where report_date=? and confirm=3 and user_no=?";
              mysqlConnection.query(fromweekplanner,[date, userNo],function(err,data){
                  if(err){
                    res.send('sqlErr');
                  }else{
                    if(data[0]==null){
                      res.send('no data');
                    }else{
              //          console.log(data);
                        var lastIndexNo  = data[0].no;
              //          console.log(lastIndexNo);
                        contentData.dayReport=data;
                        var attachment = "select *from attachment where day_no=?";
                        mysqlConnection.query(attachment,[lastIndexNo],function(err,data){
                            if(err){
                      //        console.log(err);
                            }else{
                              console.log('index NO :'+lastIndexNo);
                    //            console.log(data);
                                contentData.attachment = data;
                                var cunsultation = "select * from consultation where day_no=?";
                                mysqlConnection.query(cunsultation,[lastIndexNo],function(err,data){
                                    if(err){
                                        console.log(err);
                                    }else{
                        //                console.log('index NO :'+lastIndexNo);
                                        contentData.consultation = data;
                                        var commentSql = "select no, user_no, day_no, description, reg_date, (select name from user where no=user_no) as name from comment where day_no=?"
                                        mysqlConnection.query(commentSql,[ lastIndexNo ],function(err3,data){
                                            if(err){
                                              console.log(err3);
                                              res.redirect('/sqlErr');
                                            }else{
                                            //  console.log('index NO :'+lastIndexNo);
                                              contentData.comment = data;
                                          //    console.log(contentData);
                                              res.send(contentData);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                  }
              });
              return;
            }//end if

          // no date ; from list
          var strSql="";
          if(role==0){
              strSql = "select a.*, (select name from user where no=a.user_no)as author from day_report a where a.no=? and a.user_no=?";
              console.log("role 0");
          }else{
              strSql = "select a.*, (select name from user where no=a.user_no)as author from day_report a where a.no=?";
              console.log("role 1");
          }
            //일일보고서
            mysqlConnection.query(strSql,[ index, userNo ],function(err1,dData){
              if(err1){
                console.log(err1);
                res.redirect('/sqlErr');
                return;
              }else{
            //    console.log(dData);
                //팀장이 아닐 때 토큰값으로 유저 검증 해당유저의 게시글이 아니라면 쿼리문으로 불러올 수 없다.
                if(dData[0]==null){console.log('no data');res.send('denied');return;}
                contentData.dayReport = dData;

                //상담일지
                var consulSql = "select * from consultation where day_no=?";
                mysqlConnection.query(consulSql, [index], function(err,consulData){
                    if(err){
                      console.log(err2);
                      res.redirect('/sqlErr');
                      return ;
                    }else{
                      //console.log(consulData);
                      contentData.consultation = consulData;

                      //첨부파일
                      var attchSql = "select *from attachment where day_no=?";
                      mysqlConnection.query(attchSql,[ index ],function(err2,aData){
                          if(err2){
                            console.log(err2);
                            res.redirect('/sqlErr');
                          }else{
                              //console.log(aData)
                              contentData.attachment = aData
                            //코멘트 불러오기 &&&&&&& api 따로 빼야 할듯 &&&&&&&&&&&&&&&&
                            //var commentSql = "select *from comment where day_no=?"
                            var commentSql = "select no, user_no, day_no, description, reg_date, (select name from user where no=user_no) as name from comment where day_no=?"
                            mysqlConnection.query(commentSql,[ index ],function(err3,cData){
                                if(err3){
                                  console.log(err3);
                                  res.redirect('/sqlErr');
                                }else{
                                  //console.log(cData);
                                  contentData.comment = cData;
                    //              console.log(contentData);
                                  res.send(contentData);
                                }
                            });
                          }
                      });
                    }
                });
            }
        });
      }
  });
});

//consultation 수정완료
app.post('/node/view/consultation/content',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
          console.log('token 정보 불일치');
          res.redirect(loginErrUri);
          return;
      }else{
          console.log('상담일지 상세보기 토큰검증 완료');

          var index = req.body.index||0;
      //    console.log(index);
          var userNo = decoded.no;
    //      console.log(userNo);
          var role = decoded.role;
          //var contentData={};

          var sqlStr="";
          if(role==0){
              sqlStr = "select no as no, user_no as userNo, title, description, reg_date, day_no as dayNo, (select name from customer where no=customer_no1)as customer1Name, (select owner_name from customer where no=customer_no1)as customer1ownerName, (select address from customer where no=customer_no1)as customer1adress, (select name from customer where no=customer_no2)as customer2Name, (select owner_name from customer where no=customer_no2)as customer2ownerName,"
              +"(select address from customer where no=customer_no2)as customer2adress from consultation where (no=? and user_no=?) group by no";
              //"select a.no as no, a.user_no as userNo, a.title, a.description, a.reg_date, a.day_no as dayNo, (select title from day_report where no=a.day_no) as dayReportTitle, b.owner_name as firstcustomerOwnerName, b.name as firstCustomerName, b.address as firstCustomerAddress, c.name as secondCustomerName from consultation a, customer b where (a.customer_no=b.no and a.second_customer_no=c.no) and (a.no=? and a.user_no=?)";
          }else{
              sqlStr = "select no as no, user_no as userNo, title, description, reg_date, day_no as dayNo,(select name from user where no=userNo)as author, (select name from customer where no=customer_no1)as customer1Name, (select owner_name from customer where no=customer_no1)as customer1ownerName, (select address from customer where no=customer_no1)as customer1adress, (select name from customer where no=customer_no2)as customer2Name, (select owner_name from customer where no=customer_no2)as customer2ownerName, (select address from customer where no=customer_no2)as customer2adress from consultation where no=? group by no";
              //"select a.no as no, a.user_no as userNo, a.title, a.description, a.reg_date, a.day_no as dayNo, (select title from day_report where no=a.day_no) as dayReportTitle, b.owner_name as firstcustomerOwnerName, b.name as firstCustomerName, b.address as firstCustomerAddress, c.name as secondCustomerName from consultation a, customer b where (a.customer_no=b.no and a.second_customer_no=c.no) and (a.no=?)";
          }

          mysqlConnection.query(sqlStr,[ index, userNo ],function(err,data){
            if(err){
              console.log(err);
              res.redirect('/sqlErr');
            }else{
        //      console.log(data);
              if(data[0]==null){
                res.send('no data');
              }else{
                res.send(data);
              }
          }
        });
      }
  });
});


/* start modify contents */

//week planner
app.put('/node/modify/weekplanner',function(req,res){
  var token = req.body.token||0;
  console.log('weekplanner modify');

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
        console.log('주간계획서 수정 토큰검증 완료');
      //  console.log(decoded);
        /*start weekplanner vo*/
        var userNo = parseInt(req.body.userNo||0);
        var tokenNo = decoded.no;
        if(tokenNo != userNo){console.log('no permission to modify'); res.send('no permission to modify'); return;}
        var weekIndex= parseInt(req.body.index||0);
        var title = req.body.title||0;
        var saleGoal = parseInt(req.body.saleGoal||0);
        //var saleTotal = parseInt(req.body.saleTotal||0);

        var originalFirstDay = req.body.originaldate;
        var firstday = req.body.firstday||0;
        var endday = req.body.endday||0;
      //  console.log(firstday+', '+endday);
        /*end weekplanner vo*/
        if(weekIndex==0){res.send('no data'); return;}
        var now = new Date();
        var tempfirstday = new Date(firstday);
        //var tempendday = new Date(endday);
        console.log("get time : "+tempfirstday.getTime(firstday)+", "+now.getTime());
        //console.log("current time : "+now.getFullYear()+"-"+(now.getMonth()+1)+"-"+now.getDate());
        var ctime = now.getFullYear()+"-0"+(now.getMonth()+1)+"-"+now.getDate();
    //    console.log(ctime);

        var checkDate = "";
        if(tempfirstday.getTime(firstday) > now.getTime(ctime)){
            console.log('미래시간');
        }else{
          console.log('과거시간');
          res.send('과거시간');
          return;
        }

        //var checkDate = "select *from week_planner where first_weekday > now() and user_no=?";
       checkDate = "select *from week_planner where (first_weekday=? or end_weekday=?) and user_no=? "; //날짜가 현재시간보다 늦은 날자에 입력된 데이터가 있으면
        mysqlConnection.query(checkDate,[firstday, endday, userNo],function(err,data){
           if(err){
             console.log(err);
              mysqlConnection.rollback(function(){
                res.send('linked with dayreport');
              });
            }else{ //mysql success

             if(data[0]==null){ //데이터가 없는게 주간계획서가 그 날짜에 없음
               console.log("날짜에 데이터 없음 : ");
            //   console.log(data[0]);
                var weekSql = "update week_planner set title=?, sale_goal=?, first_weekday=?, end_weekday=?  where no=? and user_no=?";
                mysqlConnection.query(weekSql,[title, saleGoal, firstday, endday, weekIndex, userNo ],function(err,wData){
                  if(err){
                    console.log(err);
                    mysqlConnection.rollback(function(){
                        //res.send('err');
                      });
                  }else{
                    //if(wData.affectedRows!=1){res.send('sqlErr or dataErr'); return;}
                    //console.log(wData);
                      var dayPList = new Array();
                      dayPList.push(JSON.parse(req.body.list0||0)); //mon
                      dayPList.push(JSON.parse(req.body.list1||0)); //tue
                      dayPList.push(JSON.parse(req.body.list2||0)); //wed
                      dayPList.push(JSON.parse(req.body.list3||0)); //thu
                      dayPList.push(JSON.parse(req.body.list4||0)); //fri
                      console.log('day planners different date');
            //          console.log(dayPList);

                    //  console.log(dayPList)
                      // {plan, slae_total, no}
                      var value = new Array();
                      for(i = 0 ; i < dayPList.length; i++)
                        value.push([dayPList[i].day, dayPList[i].date, dayPList[i].plan, dayPList[i].saleGoal, weekIndex, dayPList[i].no]);

                  //    console.log(value);
                      var daySql="";
                      value.forEach(function(item){
                          daySql += mysql.format("update day_planner set day=?, date=?, plan=?, sale_goal=? where week_no=? and no=?;",item);
                      });
                      mysqlConnection.query(daySql,function(err,dData){
                         if(err){
                           console.log(err);
                           res.redirect('/sqlErr');
                         }else{
                           //bulk형은 index로 쿼리 결과 찾아야함
                           //if(dData.affectedRows!=1){res.send('data not changed'); return;}
                  //         console.log(dData);
                       }
                     });

                     //day report sale_goal update
                     for(i = 0 ; i < dayPList.length; ++i){
                       var updateSaleGoal="update day_report set sale_goal=? where report_date=? and user_no=?";
                       mysqlConnection.query(updateSaleGoal, [dayPList[i].total, dayPList[i].date , userNo],function(err,data){
                             if(err){
                                 console.log(err);
                             }else{
                               console.log(data);
                             }
                       });
                     }
                  res.send('changed');
                }
              });
              //날짜 안 바꾸고 내용만 수정시
            }else if(data[0].first_weekday==originalFirstDay ){ // 데이터가 안 나오면 현재시간이 작성된 날짜보다 느리게됨 즉 계획서는 과거에 있다.

                console.log('같은날짜입니다');
                 var weekSql = "update week_planner set title=?, sale_goal=?, first_weekday=?, end_weekday=?  where no=? and user_no=?";
                 mysqlConnection.query(weekSql,[title, saleGoal, firstday, endday, weekIndex, userNo ],function(err,wData){
                   if(err){
                     console.log(err);
                     mysqlConnection.rollback(function(){
                         //res.send('err');
                       });
                   }else{
                     //if(wData.affectedRows!=1){res.send('sqlErr or dataErr'); return;}
                     //console.log(wData);
                       var dayPList = new Array();
                       dayPList.push(JSON.parse(req.body.list0||0)); //mon
                       dayPList.push(JSON.parse(req.body.list1||0)); //tue
                       dayPList.push(JSON.parse(req.body.list2||0)); //wed
                       dayPList.push(JSON.parse(req.body.list3||0)); //thu
                       dayPList.push(JSON.parse(req.body.list4||0)); //fri
                       console.log('day planners same date');
                  //     console.log(dayPList);
                       // {day, date plan, sale_goal, no}
                       var value = new Array();
                       for(i = 0 ; i < dayPList.length; i++)
                       value.push([dayPList[i].day, dayPList[i].date, dayPList[i].plan, dayPList[i].saleGoal, weekIndex, dayPList[i].no]);

                //       console.log(value);
                       var daySql="";
                       value.forEach(function(item){
                           daySql += mysql.format("update day_planner set day=?, date=?, plan=?, sale_goal=?, where week_no=? and no=?;",item);
                       });
                       mysqlConnection.query(daySql,function(err,dData){
                          if(err){
                            console.log(err);
                            //res.redirect('/sqlErr');
                          }else{
                            //bulk형은 index로 쿼리 결과 찾아야함
                            //if(dData.affectedRows!=1){res.send('data not changed'); return;}
                  //          console.log(dData);
                        }
                      });

                      //day report sale_goal update
                      for(i = 0 ; i < dayPList.length; ++i){
                        var updateSaleGoal="update day_report set sale_goal=? where report_date=? and user_no=?";
                        mysqlConnection.query(updateSaleGoal, [dayPList[i].total, dayPList[i].date , userNo],function(err,data){
                              if(err){
                                  console.log(err);
                              }else{
                //                console.log(data);
                              }
                        });
                      }
                   res.send('1changed');
                 }
               });
               }else if(data[0]!=null){
                 console.log("데이터가 있습니다 : ");
            //     console.log(data);
                 res.send({'denied':data[0].title});
                 //return;
               }//if end
            }
        });
      }
  });
});


app.put('/node/modify/dayreport',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
        console.log('일일보고서 수정 토큰검증 완료');

        var confirmStatus = (req.body.confirmStatus||3);
        /* dayreport vo  */
        var userNo = parseInt(req.body.userNo||0)
        var tokenNo = decoded.no;
        if(tokenNo != userNo){console.log('no permission to modify'); res.send('no permission to modify'); return;}

        var title = req.body.title||null;
        var saleGoal = parseInt(req.body.saleGoal||0);
        var saleTotal = parseInt(req.body.saleTotal||0);

        var startDis = parseFloat(req.body.startDis||0);
        var endDis = parseFloat(req.body.endDis||0);
        var totalDis = parseFloat(req.body.totalDis||0);
        var description = req.body.description||null;
        var index = parseInt(req.body.index||0);
        var reportDate = req.body.reportDate||null;
        var newreportDate = req.body.newreportDate||null;

        var addAttachList = JSON.parse(req.body.addAttachList||0); //추가용 리스트
        var delAttachList = JSON.parse(req.body.delAttachList||0); //삭제용 리스트
      //  var flagAttach = req.body.flagAttach||0; // 1: 없앨 때 2: 새로 추가 3: 없애고 추가

        var addConsulList = JSON.parse(req.body.addConsulList||0); //추가용 리스트
        var delConsulList = JSON.parse(req.body.delConsulList||0); //삭제용 리스트
        //console.log(delConsulList);
      //  console.log(addConsulList);
      //  var flagConsul = req.body.flagConsul||0; // 1: 없앨 때 2: 새로 추가 3: 없애고 추가
      //  console.log(flagConsul); console.log(flagAttach);
        /* end dayreport vo   */

        //0: 미승인 1: 승인요청 2: 반려 3: 승인
        if(confirmStatus == 3 || confirmStatus==2){
          console.log('미승인이 아닌 문서'); res.send('already confirmed or rejected'); return ;
        }else{ // 미승인문서일때 수정이 가능

            //고치려는 날짜가 승인된 보고서가 있는지 체크 데이터가 안 나오면 고칠 수 있다.
            var checkdate = "select *from day_report where (report_date=? and user_no=?) and confirm =3";
            mysqlConnection.query(checkdate,[reportDate, tokenNo], function(err,data){
                if(err){
                    console.log(err);
                    res.redirect('/sqlErr');
                }else{
                          //그날에 승인된게 없음
                          if(data[0]==null){

                          console.log('미승인 문서');
                          var sqlStr = "update day_report set report_date=? ,title=?, sale_goal=?, sale_total=?,  start_distance=?, end_distance=?, total_distance=?, description=? where (no=? and user_no=?) and confirm!=3";
                          mysqlConnection.query(sqlStr,[newreportDate, title, saleGoal, saleTotal, startDis, endDis, totalDis, description, index, userNo],function(err,dData){
                            if(err){
                              console.log(err);
                              res.redirect('/sqlErr');
                            }else{ //update report data result
            //                  console.log(dData);
                              //if(dData.affectedRows!=1){console.log('승인된 문서를 고치려 했습니다.'); res.send({"changed":'fail'}); return;}

                              //상담일지부분
                              //상담일지 연결 설정 1. 상담일지를 없앨때 2. 상담일지를 추가 할 때 3. 있던 상담일지를 없애고 추가 할 때
                              if(delConsulList!=0 || addConsulList!=0){ //상담일지 신호가 있을 때

                                //if(flagConsul==1 || falgConsul==3){ //삭제 기능
                                if(delConsulList!=0){
                                    console.log('상담일지 삭제');
                                    //업데이트 쿼리
                                    var query = "";
                                    var value = new Array();
                                    for(i=0; i<delConsulList.length; i++)
                                        value.push( [delConsulList[i], index]);

                                    value.forEach(function(item){
                                      query += mysql.format("update consultation set day_no=null where no=? and day_no=?; ", item);
                                    });
                //                    console.log(query);
                                    mysqlConnection.query(query,function(err,data){
                                        if(err){
                                          mysqlConnection.rollback(function(){
                                            res.send('roll back del consul');
                                          });
                                        }else{
                                        //  console.log(data);
                                      }
                                    });
                                }
                                if(addConsulList!=0){ //추가 기능
                                  console.log('상담일지 추가');
                                  //없는 상태에서 추가시
                                  var query= "";
                                  var value = new Array();
                                  for(i=0; i<addConsulList.length; i++)
                                      value.push( [ index, addConsulList[i] ] );

                                  value.forEach(function(item){
                                    query += mysql.format("update consultation set day_no=? where no=? and day_no is null; ", item);
                                  });

                                  mysqlConnection.query(query,function(err,data){
                                      if(err){
                                        mysqlConnection.rollback(function(){
                                          res.send('roll back add consul');
                                        });
                                      }else{
                                        //console.log(data);
                                      }
                                  });

                              }
                            }//if ended
                            //end of consultation attaching

                          res.send({"changed":'changed'});
                        }
                      });//end of dayreport updating

                    }else{ //데이터가 나오면 그날 승인된게 있음 그날걸로 못 고침 하지만 승인되지 않은 데이터는 내용수정은 가능
                        var tempIndex = data[0].no;
                        if(tempIndex!=index){
                            var updateContents = "update day_report set title=?, sale_goal=?, sale_total=?, start_distance=?, end_distance=?, total_distance=?, description=? where (no=? and user_no=?) and confirm!=3";
                             mysqlConnection.query(updateContents,[ title, saleGoal, saleTotal, startDis, endDis, totalDis, description, index, userNo ],function(err,data){
                               if(err){
                                 console.log(err);
                                 res.redirect('/sqlErr');
                               }else{
                //                 console.log(data);
                                res.send('changed');
                               }
                            });
                        }else{
                          console.log('already exist'+data[0].title);
                          res.send({'already exist':data[0].title});
                        }
                      }
                }
            });
        }
    }
  });
});

//consultation 2차고객리스트 같이 수정 필요
app.put('/node/modify/consultation',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
        console.log('상담일지 수정 토큰검증 완료');
        var tokenNo = decoded.no;
        var userNo = parseInt(req.body.userNo||0);
        var index = parseInt(req.body.index||0);
        var title = req.body.title||0;
        var description = req.body.description||0;
        var customerNo = parseInt(req.body.customerNo||0);
        var secondCustomerNo = req.body.secondcustomerNo||null;
        var dayNo = parseInt(req.body.dayNo||0);

        if(tokenNo != userNo){console.log('no permission to modify'); res.send('no permission to modify'); return;}
        if(index==0){console.log('no Data');return;}

        var sqlStr = "update consultation set day_no="+dayNo+", customer_no1=?, title=?, description=?, customer_no2=? where user_no=? and no=?";
        if(dayNo==0){sqlStr="update consultation set day_no=null, customer_no1=?, title=?, description=?, customer_no2=? where user_no=? and no=?";}
        mysqlConnection.query(sqlStr,[customerNo, title, description, secondCustomerNo, userNo, index ],function(err,data){
          if(err){
            console.log(err);
            res.redirect('/sqlErr');
          }else{
        //    console.log(data);
            if(data.affectedRows!=1){
                res.send({"changed":'fail'});
            }else{
                res.send({"changed":'changed'});
            }

        }
      });
    }
  });
});

//comment
app.put('/node/modify/comment',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
          res.redirect(loginErrUri);
      }else{
          console.log('코멘트 수정 토큰검증 완료');
          var tokenNo = decoded.no;
          var userNo = parseInt(req.body.userNo||0);
          var description = req.body.description;
          var index = req.body.index||0;
          if(tokenNo != userNo){console.log('no permission to modify'); res.send('no permission to modify'); return;}
          if(index==0){console.log('no Data'); res.send('wrong index number'); return;}

          var sqlStr = "update comment set description=? where user_no=? and no=?";
          mysqlConnection.query(sqlStr,[description, userNo, index ],function(err,data){
            if(err){
              console.log(err);
              res.redirect('/sqlErr');
              return;
            }else{
          //    console.log(data);
              if(data.affectedRows==0){
                res.send({"changed":'fail'});
              }else{
                res.send({"changed":'changed'});
            }
          }
        });
      }
  });
});
/* end modify code */



/*start 일일보고서 confirm 관련*/
// 0: 미승인
// 1: 승인요청
// 2: 반려
// 3: 승인
// usual worker(user) do request confirmation
app.post('/node/dayreport/requestconfirm',function(req, res){

    var token = req.body.token||0;

    jwt.verify(token,key,function(err,decoded){
        if(err){
            res.redirect(loginErrUri);
        }else{
            console.log('일일보고서 승인요청 토큰검증 완료');

            var userVo = {};
            userVo.role = decoded.role;
            userVo.no = decoded.no;
            userVo.depNo=decoded.depNo;
            var index = parseInt(req.body.index||0);
            var userNo = parseInt(req.body.userNo||0);
            var reportDate = req.body.reportDate||null;

            var date = new Date();
            var currentDate={};
            currentDate.year = date.getFullYear().toString();
            currentDate.month = (date.getMonth()+1);
            currentDate.date = date.getDate();
            //var currentTime = date.getFullYear()+'-'+(date.getMonth()+1)+'-'+date.getDate();
            if( (currentDate.month).toString().length <2 ){
             currentDate.month = '0'+(currentDate.month);
            }
            if( (currentDate.date).toString().length <2 ){
             currentDate.date = '0'+(currentDate.date);
            }
            var now = currentDate.year+'-'+currentDate.month+'-'+currentDate.date;

            //권한 체크
            if( userVo.role == 1 || userNo!=userVo.no ){
                console.log('no permission to request confirmation'); res.send('no permission to request confirmation');
                return;
            }

        //    console.log('날짜 비교중 : '+now+',,,'+reportDate);
            if(now!=reportDate){
              res.send('보고서는 당일만 승인 요청 가능'); return;
            }
            console.log('보고날짜와 승인요청날짜 동일');



            //해당일에 승인된 보고서 또는 승인요청 상태 보고서 체크
            var confirmStatusCheck = "select description, report_date, no, title ,confirm from day_report where user_no=? and report_date=?";
            mysqlConnection.query(confirmStatusCheck,[ userVo.no, reportDate ],function(err,data){
              if(err){
                console.log(err);
                res.redirect('/sqlErr');
                return;
              }else{
                console.log('select confirms');
        //        console.log(data);
        //        console.log(data.length);
                var title ="";
                var no;
                var report_date="";
                var description = "";
                //데이터 정보가 틀렸을 때
                if(data[0]==undefined || data[0]==null){ console.log('wrong input'); res.send('wrong input'); return;}
                //그날에 요청되거나 컨펌된게 있는지 체크
                for(i = 0 ; i< data.length ; i++){
                  if(data[i].no==index){
                    title=data[i].title;
                    no = (data[i].no).toString();
                    report_date = (data[i].report_date).toString();
                    description = (data[i].description).toString();
                  }
                  if(data[i].confirm==3 || data[i].confirm==1 ){
                      console.log('already requeseted or already confirmed'); res.send('already requeseted or already confirmed'); return;
                  }
                }

            //    console.log(title);
                //컨펌요청 DB 수정
                var requestConfirm = "update day_report set confirm=1 where no=? and report_date=?";
                mysqlConnection.query(requestConfirm,[index, reportDate ],function(err,data){
                    if(err){
                      console.log(err);
                      res.redirect('/sqlErr');
                      return;
                    }else{
                  //      console.log(data);

                        if(data.affectedRows!=1){
                          res.send('fail to request confirmation');
                        }else{

                           //var emailQuery = "select email from user where dep_no=? and role=1";
                           var emailQuery = "select name as leadername, email,(select name from user where no=?)as username,(select dep_name from department where no=?)as departmentname from user where dep_no=? and role=1";
                           mysqlConnection.query(emailQuery,[userVo.no, userVo.depNo, userVo.depNo],function(err,data){
                              if(err){
                                console.log(err);
                                res.redirect('/sqlErr');
                                return;
                              }else{
                        //          console.log(data[0].email);
                                  var teamLeaderEmailAddress = data[0].email;
                                  var team = data[0].departmentname;
                                  var leader = data[0].leadername;
                                  var username = data[0].username;
                                  var html ="<a href=http:/nodeServer:port/view/dayreport/content>link go to the report</a>";

                                  var date = new Date();
                                  var time = "시간:"+date.getHours()+":"+date.getMinutes();
                                  var mailOptions = {
                                      from: '"smsNodejsServer" <smsmailserver@sms.com>', // sender address
                                      to: teamLeaderEmailAddress, // list of receivers
                                      subject: 'Report title : reportNumber: '+no+' ['+title+'] Report confirmation requested !', // Subject line
                                      //text: 'please confirm the report', // plain text body
                                      html:  '<!DOCTYPE html>'+
                                              '<html>'+
                                              '<head>'+
                                              '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">'+
                                              '<meta http-equiv="Content-Script-Type" content="text/xxxxjavascript">'+
                                              '<meta http-equiv="Content-Style-Type" content="text/css">'+
                                              '<meta http-equiv="X-UA-Compatible" content="IE=edge">'+
                                              '</head>'+
                                              '<body style="width: 730px;color: #010101;font-family: &#39;돋움&#39;;font-size: 12px;">'+
                                              '<div style="width: 810px; background: #fff; border: 1px solid #ccc; border-radius: 4px 4px 4px 4px;overflow: hidden; cursor: default;">'+
                                              '<div style="background: #f5f5f5;border-bottom: 1px solid #ccc;box-sizing: border-box;height: 56px;margin: 0;overflow: hidden;padding: 15px 20px;position: relative;text-overflow: ellipsis;white-space: nowrap;">'+
                                              '<h2 style="color: #333; font-weight: normal; font-size: 20px; line-height: 1.5; margin: 0; overflow: hidden; padding: 0; text-overflow: ellipsis; white-space: nowrap;border: 0; vertical-align: baseline; font-family: Arial, sans-serif; visibility: visible;">'+
                                              '일일 보고서'+
                                              '</h2>'+
                                              '</div>'+
                                              '<div style="position: relative; overflow: auto; padding: 20px; max-height: 500px; margin: 0;border: 0;font: inherit;vertical-align: baseline;font-family: Arial, sans-serif;font-size: 14px">'+
                                              '<div style="border-bottom: 1px solid #ddd;padding-bottom: 10px;margin-bottom: 10px;">'+
                                              '<div style="margin-bottom: 10px;">'+
                                              '<div style="box-sizing: border-box;clear: both;position: relative;display: inline-block;width: 22%;cursor: context-menu; text-align: center;">'+
                                              '<label style="word-wrap: break-word;color: #707070;">Team:<label>'+
                                              '<span>'+team+'</span>'+
                                              '</div>'+
                                              '<div style="box-sizing: border-box;clear: both;position: relative;display: inline-block;width: 22%;cursor: context-menu; text-align: center;">'+
                                              '<label style="word-wrap: break-word;color: #707070;">Leader:<label>'+
                                              '<span>'+leader+'</span>'+
                                              '</div>'+
                                              '<div style="box-sizing: border-box;clear: both;position: relative;display: inline-block;width: 22%;cursor: context-menu; text-align: center;">'+
                                              '<label style="word-wrap: break-word;color: #707070;">Reporter:<label>'+
                                              '<span>'+username+'</span>'+
                                              '</div>'+
                                              '<div style="box-sizing: border-box;clear: both;position: relative;display: inline-block;width: 22%;cursor: context-menu; text-align: center;">'+
                                              '<label style="word-wrap: break-word;color: #707070;">Report Date:<label>'+
                                              '<span>'+report_date+'</span>'+
                                              '</div>'+
                                              '</div>'+
                                              '</div>'+
                                              '<div>'+
                                              '<div style="box-sizing: border-box;clear: both;padding: 4px 0 4px 145px;position: relative;margin: 1px 0;width: 100%;margin-bottom: 0 !important;">'+
                                              '<label style="word-wrap: break-word;color: #707070;float: left;text-align: right;width: 130px;margin-left: -145px;position: relative;padding: 5px 0 0 0;">Title:</label>'+
                                              '<span style="max-width: 500px;height: 2.14285714em;line-height: 1.4285714285714;padding: 4px 5px;font-size: inherit;margin: 0;vertical-align: baseline;width: 100%;background: #fff;color: #333;font-family: inherit;"> '+
                                              title+
                                              '</span>'+
                                              '</div>'+
                                              '<div style="box-sizing: border-box;clear: both;padding: 4px 0 4px 145px;position: relative;margin: 1px 0;width: 100%;margin-bottom: 0 !important;">'+
                                              '<label style="word-wrap: break-word;color: #707070;float: left;text-align: right;width: 130px;margin-left: -145px;position: relative;padding: 5px 0 0 0;">Description:</label>'+
                                              '<span style="max-width: 500px;height: 2.14285714em;line-height: 1.4285714285714;padding: 4px 5px;font-size: inherit;margin: 0;vertical-align: baseline;width: 100%;background: #fff;color: #333;font-family: inherit;">'+
                                              description+
                                              '</span>'+
                                              '</div>'+
                                              '</div>'+
                                              '</div>'+
                                              '<div style="overflow: visible;min-height: 51px;height: 100%;margin: 0;padding: 10px;box-sizing: border-box;clear: both;position: relative;width: 100%;text-align: right;white-space: nowrap;border-top: 1px solid #ccc;background: #f5f5f5;">'+
                                              '<div style="margin-top: 0; float: right;">'+
                                              '<a href="${url}" target="_blank"'+
                                              'style="cursor: pointer;display: inline-block;font-family: inherit;font-size: 14px;font-variant: normal;font-weight: normal;height: 2.14285714em;line-height: 1.42857143;margin: 0;padding: 4px 10px;text-decoration: none;vertical-align: baseline;white-space: nowrap; border-radius: 3.01px;box-sizing: border-box;background: #f5f5f5;border: 1px solid #ccc;margin-bottom: 0;color: #000;font-family: Arial, sans-serif;">'+
                                              '보고서 바로가기'+
                                              '</a>'+
                                              '</div>'+
                                              '</div>'+
                                              '</div>'+
                                              '</body>'+
                                              '</html>'
                                  };

                                  // send mail with defined transport object
                                  transporter.sendMail(mailOptions, (error, info) => {
                                      if (error) {
                                          return console.log(error);
                                      }
                              //        console.log('Message %s sent: %s', info.messageId, info.response);
                                  });

                                  var getFcmkey = "select fcmkey from user where dep_no=? and role=1";
                                  mysqlConnection.query(getFcmkey,[userVo.depNo],function(err,data){
                                      if(err){
                                        console.log(err);
                                        res.redirect('/sqlErr');
                                        return;
                                      }else{
                              //          console.log("!!!!!!!!!!!!!!!!!!!!!!!!!"+no);
                                        //  console.log(data);
                                          var fcmkey = data[0].fcmkey||0;
                                          if(fcmkey==0){
                                            console.log('nofcmkey');
                                            return;
                                          }
                                          var date = new Date();
                                          var time = "요청된 시간:"+ date.getHours().toString()+":"+ ((date.getMinutes()).toString().length==1 ? '0'+(date.getMinutes()).toString() : (date.getMinutes()).toString()); ;
                                          var payload ={
                                            notification:{
                                              title:title+" 보고서 승인요청 알림",
                                              body:time
                                            },
                                            data : {
                                              index : no
                                              //time : time,
                                            }
                                          };

                                          fcm.messaging().sendToDevice(fcmkey, payload)
                                            .then(function(response) {
                                              // See the MessagingDevicesResponse reference documentation for
                                              // the contents of response.
                                              console.log("Successfully sent message:", response);
                                            })
                                            .catch(function(error) {
                                              console.log("Error sending message:", error);
                                            });
                                      }

                                  });
                                  res.send({"update":'updated and request sent'});
                              }
                           });

                        }
                    }
                });
            }
          });


        }
    });
});


//team leaders do confirm
app.post('/node/dayreport/confirm',function(req,res){
  //token validation and mysql connection
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
    if(err){
        res.redirect(loginErrUri);
    }else{
        console.log('일일보고서 팀장 승인 토큰검증 완료');
        var teamLeaderVo = {};
        teamLeaderVo.role =  decoded.role;
        teamLeaderVo.depNo = decoded.depNo;
      //  console.log(teamLeaderVo);
        var authorNo = parseInt(req.body.userNo||0);
        var index = parseInt(req.body.index||0);
        var reportDate = (req.body.reportDate||undefined);
        var saleTotal = parseInt(req.body.saleTotal||0);
        var flag = parseInt(req.body.flag||0); //flag 1: 승인 flag :2 반려

        //var confirmStatus = parseInt(req.body.confirmStatus||3);
        //권한 검증
        if(teamLeaderVo.role != 1){
            console.log('no permission to confirm'); res.send('no permission to confirm'); return;
        }
        // var checkWeekPlanner = "select *from day_planner where date=?";
        //   mysqlConnection.query(checkWeekPlanner,[reportDate],function(err,data){
        //       if(err){
        //         res.send('err');
        //         return;
        //       }else{
        //           console.log(data);
        //           if(data[0]==null){
        //             console.log('no planner!!!!!!!!!!!!!!');
        //             res.send('no planner');
        //             return;
        //           }else{
        //
        //           }
        //       }
        //   });
        var confirmStatusCheck = "select no, title, confirm from day_report where (user_no=? and no=?)and report_date=?";
        mysqlConnection.query(confirmStatusCheck, [authorNo, index, reportDate] , function(err,data){
            if(err){
              console.log(err);
              res.redirect('/sqlErr');
              return;
            }else{//sql

              console.log(data[0]);
              var title = data[0].title;
              var no = (data[0].no).toString();
                if(data[0].confirm==3){
                  console.log('already confirmed'); res.send('you already confirmed this date'); return;
                }else if(data[0].confirm==1){

                  var checkDepNo = "select * from user where no=? and dep_no=? ";
                  mysqlConnection.query(checkDepNo,[authorNo, teamLeaderVo.depNo], function(err,data){
                      if(err){
                        console.log(err);
                        res.redirect('/sqlErr');
                        return;
                      }else{
                        //console.log(data[0]);
                          if(data[0]==null){
                            console.log('다른부서의 서류를 승인 할 수 없다.'); res.send('no permission to confirm another team\'s paper '); return;
                          }
                          console.log('승인 권한 인증 완료');
                          if(flag==1){ //승인
                              var confirmQuery = "update day_report set confirm=3 where no=? and user_no=?";
                              mysqlConnection.query(confirmQuery,[index, authorNo],function(err,data){
                                  if(err){
                                    console.log(err);
                                    res.redirect('/sqlErr');
                                    return;
                                  }else{
                                      //console.log(data);
                                      if(data.affectedRows==1){
                                        console.log('changed confirmation');

                                        //update sales total on weekPlanner
                                        var sumSalesTotalQuery = "update week_planner set sale_total=(sale_total+?) where no=(select week_no from day_planner where date=? and user_no=?) and user_no=?";
                                        mysqlConnection.query(sumSalesTotalQuery, [saleTotal, reportDate, authorNo, authorNo],function(err,data){
                                          if(err){
                                            console.log(err);
                                            res.redirect('/sqlErr');
                                            return;
                                          }else{
                                      //        console.log(data);

                                                  var getuserFCM = "select fcmkey from user where no=?";
                                                  mysqlConnection.query(getuserFCM, [authorNo],function(err,data){
                                                      if(err){
                                                          console.log(err);
                                                      }else{
                                                        var fcmkey = data[0].fcmkey;
                                                        var date = new Date();
                                                        var time = "결제 시간:"+ date.getHours().toString()+":"+ ((date.getMinutes()).toString().length==1 ? '0'+(date.getMinutes()).toString() : (date.getMinutes()).toString()); ;
                                                        var payload ={
                                                          notification:{
                                                            title: reportDate+" 의 보고서 제목: "+title,
                                                            body : time
                                                          },
                                                          data : {
                                                            index : no
                                                          }
                                                        };

                                                        fcm.messaging().sendToDevice(fcmkey, payload)
                                                          .then(function(response) {
                                                            console.log("Successfully sent message:", response);
                                                          })
                                                          .catch(function(error) {
                                                            console.log("Error sending message:", error);
                                                          });
                                                        res.send({"changed":'confirmed'});
                                                      }
                                                  });
                                          }
                                        });
                                      }
                                  }
                              });
                            }else if(flag==2){  //반려
                              var rejectmQuery = "update day_report set confirm=2 where no=? and user_no=?";
                              mysqlConnection.query(rejectmQuery,[index, authorNo],function(err,data){
                                  if(err){
                                    console.log(err);
                                    res.redirect('/sqlErr');
                                    return;
                                  }else{
                                    if(data.affectedRows==1){
                                      var getuserFCM = "select fcmkey from user where no=?";
                                      mysqlConnection.query(getuserFCM, [authorNo],function(err,data){
                                          if(err){
                                            console.log(err);
                                          }else{

                                            var fcmkey = data[0].fcmkey;
                                            var date = new Date();
                                            var time = "결제 시간:"+ date.getHours().toString()+":"+ ((date.getMinutes()).toString().length==1 ? '0'+(date.getMinutes()).toString() : (date.getMinutes()).toString()); ;
                                            var payload ={
                                              notification:{
                                                title: reportDate+" 의 보고서 제목: "+title+' 반려됨',
                                                body : time
                                              },
                                              data : {
                                                index : no
                                              }
                                            };

                                            fcm.messaging().sendToDevice(fcmkey, payload)
                                              .then(function(response) {
                                                console.log("Successfully sent message:", response);
                                              })
                                              .catch(function(error) {
                                                console.log("Error sending message:", error);
                                              });
                                            res.send({"changed":'rejected'});

                                          }
                                      });
                                      //res.send({"changed":'rejected'});
                                    }
                                  }
                              });
                            }
                      }
                  });
                }else{ console.log('승인요청 하지 않은 보고서');res.send('승인요청 하지 않은 보고서');return;}
            }
        });


    }
  });
});

/*end 일일보고서 confirm 관련*/



/*start delete contents*/

app.post('/node/delete/weekplanner', function(req,res){
  //token validation and mysql connection

    var token =req.body.token;
  //  console.log(token);
    jwt.verify(token,key,function(err,decoded){
        if(err){
            //res.redirect(loginErrUri);
        }else{
            console.log('주간계획 삭제 토큰검증 완료');
            var index = parseInt(req.body.no||0);
            var userNo = decoded.no;
            var authorNo = parseInt(req.body.userNo||0);

            if(index==0){console.log('no index'); res.send('no index sent'); return;}
            if(authorNo != userNo){console.log('no permission to delete');  res.send('no permission to delete'); return;}

            var deleteWeek = "delete from day_planner where week_no=? ";
            mysqlConnection.query(deleteWeek,[index ],function(err,data){
              if(err){
                console.log(err);
                mysqlConnection.rollback(function(){
                  res.send('roll back');
                });
              }else{
            //    console.log(data);
                console.log('week_planner deleted');
                if(!data.affectedRows){
                  mysqlConnection.rollback(function(){
                      res.send('roll back');
                  });
                }
                var deleteDay  = "delete from week_planner where no=? and user_no=?" ;
                mysqlConnection.query(deleteDay, [index, userNo], function(err,data){
                    if(err){
                      mysqlConnection.rollback(function(){
                      });
                    }else{
                      console.log(data);
                      if(!data.affectedRows){
                        mysqlConnection.rollback(function(){
                            res.send('roll back');
                          });
                        }
                        res.send(data);
                    }
                });
              }
          });
        }
    });
});

app.post('/node/delete/dayreport',function(req,res){

  var token = req.body.token||0;
//  console.log(token);
  jwt.verify(token,key,function(err,decoded){
      if(err){
          res.redirect(loginErrUri);
      }else{
          console.log('일일보고서 삭제 토큰검증 완료');
          var userNo = decoded.no;
          var authorNo = parseInt(req.body.userNo||0);
          var index = parseInt(req.body.no||0);
          var flag = parseInt(req.body.flag||0);
          var commentNo = req.body.commentNo||0;
          //1: 첨부파일 있고 상담일지 없음 2: 상담일지 있고 첨부파일 없음 3: 둘다 있음 4: 둘다없음
          if(index==0){console.log('no index'); res.send('no index sent'); return;}
          if(authorNo != userNo){console.log('no permission to delete');  res.send('no permission to delete'); return;}
    //      console.log("flagflag"+flag);
          if(commentNo==0){
          var commentquery = "delete from comment where day_no=?";
          mysqlConnection.query(commentquery, [index], function(req,data){
              if(err){
                console.log(err);
                mysqlConnection.rollback(function(){
                  console.log('roll back');
                  res.send('roll back');
                });
              }else{
    //              console.log(data);
              }
          });
        }

          if(flag==2 || flag==3 ){
          var consultationQuery = "update consultation set day_no=null where day_no=?";
          mysqlConnection.query(consultationQuery, [index], function(req,data){
              if(err){
                console.log(err);
                mysqlConnection.rollback(function(){
                  console.log('roll back');
                  res.send('roll back');
                });
              }else{
                  console.log('상담일지 연결해제');
          //        console.log(data);
              }
          });
        }
        if(flag==1 || flag==3 ){
          var attachmentQuery = "delete from attachment where day_no = ?";
          mysqlConnection.query(attachmentQuery, [index], function(err,data){
              if(err){
                console.log(err);
                mysqlConnection.rollback(function(){
                  console.log('roll back');
                  res.send('roll back');
                });
              }else{
                  console.log('첨부파일 연결해제');
          //        console.log(data);
              }
          });
        }

          var commentQuery = "delete from comment where day_no=?";
          mysqlConnection.query(commentQuery,[index],function(err,data){
            if(err){
              console.log(err);
              res.redirect('/sqlErr');
            }else{
          //    console.log(data);
              console.log('코멘트삭제');

              var sqlStr = "delete from day_report where no=? and user_no = ?";
              mysqlConnection.query(sqlStr,[index, userNo ],function(err,data){
                if(err){
                  console.log(err);
                  res.redirect('/sqlErr');
                }else{
          //        console.log(data);
                  res.send({"delete":'deleted'});
              }
            });
          }
        });

      }
  });
});

app.post('/node/delete/consultation',function(req,res){
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
          res.redirect(loginErrUri);
      }else{
          console.log('상담일지 삭제 토큰검증 완료');
          var userNo = decoded.no;
          var authorNo = parseInt(req.body.userNo||0);
          var index = parseInt(req.body.no||0);

          if(index==0){console.log('no index'); res.send('no index sent'); return;}
          if(authorNo != userNo){console.log('no permission to delete');  res.send('no permission to delete'); return;}

          var sqlStr = "delete from consultation where (no=? and user_no=?) and day_no is null";
          mysqlConnection.query(sqlStr,[index, userNo ],function(err,data){
            if(err){
              console.log(err);
              mysqlConnection.rollback(function(){
                console.log('roll back');
                res.send('roll back');
              });
            }else{
          //    console.log(data);
              if(!data.affectedRows){
                mysqlConnection.rollback(function(){
                    res.send('linked with dayreport');
                  });
              }else{res.send({"delete":'deleted'});}
          }
        });
      }
  });
});

app.post('/node/delete/comment',function(req,res){
  //token validation and mysql connection
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
          res.redirect(loginErrUri);
      }else{
          console.log('코멘트 삭제 토큰검증 완료');
          var userNo = decoded.no;
          var authorNo = req.body.userNo;
          var index = req.body.no;

          if(index==0){console.log('no index'); res.send('no index sent'); return;}
          if(authorNo != userNo){console.log('no permission to delete');  res.send('no permission to delete'); return;}

          var sqlStr = "delete from comment where no=? and user_no=?";
          mysqlConnection.query(sqlStr,[index, userNo ],function(err,data){
            if(err){
              console.log(err);
              res.redirect('/sqlErr');
            }else{
            //  console.log(data);
              res.send(data);
          }
        });
      }
  });
});
/*end delete contents*/

app.post('/node/calendar/weekplanner',function(req,res){
  //token validation and mysql connection
  var token = req.body.token||0;
//  console.log(token);
  jwt.verify(token,key,function(err,decoded){
    	if(err){
    		  //res.redirect(loginErrUri);
    	}else{
    		  console.log('캘린더 주간계획서 토큰검증 완료');

          var userNo = decoded.no;
          var role = decoded.role;
          var firstDay = req.body.firstDay;
          var endDay = req.body.endDay;
         console.log("first day: "+firstDay);
         console.log("last day: "+endDay);
          var contentData = {};

          var sqlStr="";
          //팀장 이외에 다른사람의 게시글 접근시 에러
          if(role==0){
              sqlStr = "select *from week_planner where user_no=? and (first_weekday=? and end_weekday=?)";
          }else{
              //sqlStr = "select *from week_planner where no=?";
          }

          mysqlConnection.query(sqlStr,[userNo, firstDay, endDay ],function(err1,data){
      	    if(err){
          	 	console.log(err1);
          		res.redirect('/sqlErr');
        	  }else{
        //      console.log(data[0]);
              if(data[0]==null){console.log('err');res.send('denied');return;}
              var weekNo = parseInt(data[0].no);
          //    console.log(weekNo);
              contentData.week = data;
          //    console.log("=-================"+contentData.week);
              var secondSql = "select *from day_planner where week_no=?";
              mysqlConnection.query(secondSql,[ weekNo ],function(err2,sdata){
                  if(err){
                    console.log(err2);
                		res.redirect('/sqlErr');
                    return;
                  }else{
              //      console.log('=====================');
            //        console.log(sdata);
                    contentData.daily=sdata;
                    res.send(contentData);
                  }
              });
        	   }
      	});
    	}
  });
});

/*calendar listing day_planners*/
app.post('/node/calendar/dayreport',function(req,res){
  //token validation and mysql connection
  var token = req.body.token||0;

  jwt.verify(token,key,function(err,decoded){
      if(err){
          res.redirect(loginErrUri);
      }else{
          console.log('달력에 일일계획 데이터 출력 토큰검증 완료');
          var date = (req.body.date||undefined);
          var role = decoded.role;
          var depNo= decoded.depNo;
          if(role==0){
            console.log('no permission');
            res.send('nopermission');  return;
          }
          var sqlStr = "select a.no, a.user_no, a.confirm, a.title, a.description, b.name from day_report a, user b where a.report_date = ? and a.confirm!=0 and a.user_no=b.no and b.dep_no=? order by a.reg_date";
          mysqlConnection.query(sqlStr,[date, depNo ],function(err,data){
            if(err){
              console.log(err);
              res.redirect('/sqlErr');
            }else{
          //    console.log(data);
              console.log('calendar data');
              if(data[0]==null){
                  res.send('no data');
              }else{
                  res.send(data);
              }
          }
        });
      }
  });
});


//
// app.post('/node/graph/saletotal',function(req,res){ //month
//
//   //token validation and mysql connection
//   var token = req.body.token||0;
//
//   jwt.verify(token,key,function(err,decoded){
//       if(err){
//           res.redirect(loginErrUri);
//       }else{
//           console.log('그래프 주간계획서 매출액 토큰검증 완료');
//
//           var role = decoded.role;
//           var startDate = req.body.startDate||'';
//           var endDate = req.body.endDate||'';
//           var userNo= decoded.no;
//           //select a.sale_total from week_planner a, user b where (a.first_weekday='2017-09-25' and a.end_weekday='2017-10-01') and a.user_no=5 group by a.no; //개인
//           //select a.sale_total from week_planner a, user b where (a.first_weekday >= '2017-08-28' and a.end_weekday < '2017-10-08') and dep_no=1 group by a.no; //팀
//           console.log(startDate);
//           console.log(endDate);
//           //팀장
//           if(role==1){
//               var leadersql = "select a.sale_goal goal, a.sale_total from week_planner a, user b where (a.first_weekday >= ? and a.first_weekday <= ?) and b.dep_no=1 group by a.no";
//               mysqlConnection.query(leadersql,[startDate, endDate ],function(err,data){
//                 if(err){
//                   console.log(err);
//                   res.redirect('/sqlErr');
//                 }else{
//                   console.log(data);
//                   res.send(data);
//                 }
//               });
//           }else if(role==0){ //개인
//               var workersql ="select a.sale_goal goal, a.sale_total as total from week_planner a, user b where (a.first_weekday >= ? and a.first_weekday <= ?) and a.user_no=? group by a.no";
//               mysqlConnection.query(workersql,[startDate, endDate, userNo ],function(err,data){
//                 if(err){
//                   console.log(err);
//                   res.redirect('/sqlErr');
//                 }else{
//                   console.log(data);
//                   res.send(data);
//                 }
//               });
//           }
//       }
//   });
// });

app.post('/node/graph/sales/year',function(req,res){
    //token validation and mysql connection
  var token = req.body.token||0;
  console.log("token:"+token);
    jwt.verify(token,key,function(err,decoded){
        if(err){
            res.redirect(loginErrUri);
        }else{
            console.log('그래프 년도 토큰검증 완료');
            var year = req.body.year;
            var userNo = req.body.userNo||0;
            var role = decoded.role;
            if(role==0){
              userNo=decoded.no;
            }
            // if(role==2){
            //   res.sendStatus(404);
            //   return;
            // }
        //    console.log(role);
        //    console.log("연도값 : "+year);
        //    console.log("유저값  : "+userNo);
            var saleGoal = new Array();
            var saleTotal = new Array();
            var disTotal = new Array();

            if(userNo==-1){
              for(i = 1 ; i < 13;i++){
                var  sqlStr = "select sum(sale_total) as saleTotal, (select sum(sale_goal) as saleGoal from day_report where year(report_date)=? and month(report_date)=? and confirm=3 ) as saleGoal, (select sum(total_distance) as totalDis from day_report where year(report_date)=? and month(report_date)=? and confirm=3 ) as disTotal from day_report where year(report_date)=? and month(report_date)=? and confirm=3" ;
                mysqlConnection.query(sqlStr,[year, i, year, i, year, i ],function(err,data){
                  if(err){
                    console.log(err);
                    res.redirect('/sqlErr');
                  }else{
                    saleGoal.push(data[0].saleGoal);
                    saleTotal.push(data[0].saleTotal)
                    disTotal.push(data[0].disTotal);
                    if(saleGoal.length==12){
                      //res.send({saleGoal, saleTotal});
                      // console.log(saleGoal);
                      // console.log(saleTotal);
                      // console.log(disTotal);
                      res.send({saleGoal, saleTotal, disTotal});
                      return;
                    }
                  }
                });
              }
            }else{
              console.log("in role 0  "+ userNo);
              for(i = 1 ; i< 13;i++){
                var  sqlStr = "select sum(sale_total) as saleTotal, (select sum(sale_goal) as saleGoal from day_report where year(report_date)=? and month(report_date)=? and confirm=3 and user_no=? ) as saleGoal, (select sum(total_distance) as totalDis from day_report where year(report_date)=? and month(report_date)=? and confirm=3 and user_no=? ) as disTotal from day_report where year(report_date)=? and month(report_date)=? and confirm=3 and user_no=?" ;
                mysqlConnection.query(sqlStr,[year, i, userNo, year, i, userNo, year, i, userNo ],function(err,data){
                  if(err){
                    console.log(err);
                    res.redirect('/sqlErr');
                  }else{
                    saleGoal.push(data[0].saleGoal);
                    saleTotal.push(data[0].saleTotal)
                    disTotal.push(data[0].disTotal);
                    //  console.log(saleGoal);
                    //  console.log(saleTotal);
                    //   console.log(disTotal);
                    if(saleGoal.length==12){
                      // console.log(saleGoal);
                      // console.log(saleTotal);
                      // console.log(disTotal);
                      res.send({saleGoal, saleTotal, disTotal});
                      return;
                    }
                  }
                });
              }
            }
        }
    });
});

app.post('/node/graph/sales/month',function(req,res){
    //token validation and mysql connection
  var token = req.body.token||0;

    jwt.verify(token,key,function(err,decoded){
        if(err){
            res.redirect(loginErrUri);
        }else{
            console.log('그래프 달 토큰검증 완료');
            var year = req.body.year;
            var month = req.body.month+'-01';
            var userNo = req.body.userNo||0;;
            var role = decoded.role;
            // if(role==2){
            //   res.sendStatus(404);
            //   return;
            // }
            if(role==0){
              userNo=decoded.no;
            }
            // console.log("년 값 "+ year);
            // console.log("월 값 : "+month);
            // console.log("유저값  : "+userNo);

            var getWeek = "select week(?) as fweek, (select week(LAST_DAY(?))) as eweek ";
            mysqlConnection.query(getWeek,[month,month ],function(err,data){
      	    if(err){
          	 	console.log(err);
          		res.redirect('/sqlErr');
        	  }else{
          		console.log(data);
              var start=data[0].fweek;
              var end = data[0].eweek;
              var loopC = end+1-start;
              var saleGoal = new Array();
              var saleTotal = new Array();
              var disTotal = new Array();

              if(userNo==-1){
                for(  start ; start<= end; ++start){
                  //console.log(start);
                  var getD = "select sum(sale_goal) as saleGoal, (select sum(sale_total) from day_report where week(report_date)=? and confirm=3 and year(report_date)=? ) as saleTotal, (select sum(total_distance) from day_report where week(report_date)=? and confirm=3 and year(report_date)=? ) as disTotal from day_report where week(report_date)=? and confirm=3 and year(report_date)=? ";
                  mysqlConnection.query(getD,[start, year, start, year, start, year ],function(err,data){
                      if(err){
                        console.log(err);
                        res.redirect('/sqlErr');
                      }else{
                        saleGoal.push(data[0].saleGoal);
                        saleTotal.push(data[0].saleTotal);
                        disTotal.push(data[0].disTotal);
                        if(loopC==saleGoal.length){
                          // console.log(saleGoal);
                          // console.log(saleTotal);
                          // console.log(disTotal);
                          res.send({saleGoal, saleTotal, disTotal});
                          // console.log(saleGoal);
                          // console.log(saleTotal);
                          // console.log(disTotal);
                          return;
                        }
                    }
                  });
                }
              }else{
                for(  start ; start<= end; ++start){
                  //console.log(start);
                  var getD = "select sum(sale_goal) as saleGoal, (select sum(sale_total) from day_report where week(report_date)=? and confirm=3 and user_no=?  and year(report_date)=? ) as saleTotal, (select sum(total_distance) from day_report where week(report_date)=? and confirm=3 and user_no=?  and year(report_date)=? ) as disTotal from day_report where week(report_date)=? and confirm=3 and user_no=?  and year(report_date)=? ";
                  mysqlConnection.query(getD,[start, userNo, year, start, userNo, year, start, userNo, year ],function(err,data){
                	    if(err){
                    	 	console.log(err);
                    		res.redirect('/sqlErr');
                  	  }else{
                    		saleGoal.push(data[0].saleGoal);
                        saleTotal.push(data[0].saleTotal);
                        disTotal.push(data[0].disTotal);
                        // console.log(saleGoal);
                        // console.log(saleTotal);
                        // console.log(disTotal);
                        if(loopC==saleGoal.length){
                          res.send({saleGoal, saleTotal, disTotal});
                          // console.log(saleGoal);
                          // console.log(saleTotal);
                          // console.log(disTotal);
                          return;
                        }
                  	}
                	});
                }
              }
          	}
        	});
        }
    });
});



//접속
app.listen(port,()=>{
  console.log('server port ' +port+ ' listening');
});
