#include <SPI.h>
#include <Ethernet2.h> //웹서버 라이브러리
#include <TextFinder.h> //문자를 검색하기 위한 라이브러리


//Ethernet Shield MAC address 
byte mac[] = {0x90, 0xA2, 0xDA, 0x10, 0x52, 0x7A}; //아두이노 이더넷2 MAC ADDRESS
IPAddress ip(192,168,0,24); //웹서버 주소
IPAddress subnet(255,255,0,0);
IPAddress gateway(172,31,0,1);
IPAddress dns_local(168,126,63,1);

char buffer[8];

EthernetServer server(80); //이더넷 서버통신을 위한 포트번호


int IN1=6; //핀번호 설정
int IN2=7;
int SPEED=3;

void setup() {
   pinMode(IN1,OUTPUT); //도어락 모터제어를위한 핀번호 설정
   pinMode(IN2,OUTPUT);
   pinMode(SPEED,OUTPUT);
   digitalWrite(SPEED,200); //모터드라이버 속도 설정
   Serial.begin(9600);

  Ethernet.begin(mac, ip, dns_local, gateway, subnet); //이더넷 연결하기위한 정보들 초기화

  
   Serial.print("IP address: ");
  ip = Ethernet.localIP();
  for (byte thisByte = 0; thisByte < 4; thisByte++) {
    // print the value of each byte of the IP address:
    Serial.print(ip[thisByte], DEC);
    Serial.print("."); 
  }
  Serial.println();
  server.begin();
}

void loop() {
  EthernetClient client = server.available(); //이더넷 웹서버 연결 함수
  if (client) { //서버 접속이 되면 아래 문장 실행
    TextFinder finder(client); //client에 있는 문자를 검색할 수 있다
    int type = 0;
    while (client.connected()) { 
      if (client.available()) {
        //http request start with "GET / HTTP/1.1"
        if (finder.getString("","/", buffer, sizeof(buffer))) { //버퍼의 문자열을 가지고옴
          if(strcmp(buffer, "POST ") == 0) { // POST 텍스트를 받아온다
            finder.find("\n\r");
            while (finder.findUntil("pinD", "\n\r")) { //해당 pinder함수를 이용해서 pinD에 해당하는 이름을 가져온다
              //int pin = finder.getValue(); //
              int val = finder.getValue(); //val에 사용자로부터 입력받은 값을 넣는다
               
               if(val==0) //사용자로부터 open버튼이 클릭되면 아래 기능수행
              {
                Serial.println("The door is open");
                //정회전
                digitalWrite(IN1,HIGH); 
                digitalWrite(IN2,LOW); 
                delay(1000);
                //다음 동작을 위한 전류 차단
                digitalWrite(IN1,LOW);
                digitalWrite(IN2,LOW);
                delay(1000);

              }
              if(val==1) //사용자로부터 close버튼이 클릭되면 아래 기능 수행
              {
                Serial.println("The door is close");
                //역회전
                digitalWrite(IN1,LOW);
                digitalWrite(IN2,HIGH);
                delay(1000);
                //다음 동작을 위한 전류 차단    
                digitalWrite(IN1,LOW);
                digitalWrite(IN2,LOW);
                delay(1000);

              }
             
            }
          }
          
          sendHeader(client,"Post example"); //웹서버 HTML&CSS&JavaScript
          //create HTML button to control pin 9
          client.println("<center>");
          client.println("<h2>Click buttons to open the door</h2>");

          client.print("<form action='/' method='POST'><p><input type='hidden' name='pinD9'");
          client.println(" value='0'><input type='submit' value='OPEN' id='op'/></form>");
          //create HTML button to turn on pin 9
          client.print("<form action='/' method='POST'><p><input type='hidden' name='pinD9'");
          client.print(" value='1'><input type='submit' value='CLOSE' id='cl'/></form>");
          client.println("<center>");
          client.println("</body></html>");
          client.stop();
          break;
        }//if finder
      }//if client available
     
    }//while connected
   delay(1);
    client.stop();
  }//if client
 
}
//웹서버 HTML&CSS&JavaScript//웹서버 HTML&CSS&JavaScript
void sendHeader(EthernetClient client, char *title) {
  // send a standard http response header
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html");
  client.println();
  client.print("<html lnag='KO'><head><title>");
  client.print(title);
  
  client.println("</title><style type='text/css'>");
  client.println("#op {width:100px; height:30px; background-color:white; cursor:pointer; } ");
  client.println("#cl { width:100px; height:30px; background-color:white; cursor:pointer; }");
  client.println("</style>");

  client.println("<script type='text/javascript'>");
  client.println("window.onload = function(){");
  
  client.println("document.getElementById('op').onclick = function(){");
  client.println("this.style.backgroundColor = 'yellow';");
  client.println("document.getElementById('cl').style.backgroundColor = 'white';");
  client.println("alert('open');}");
  
  client.println("document.getElementById('cl').onclick = function(){");
  client.println("this.style.backgroundColor = 'yellow';");
  client.println("document.getElementById('op').style.backgroundColor = 'white';");
  client.println(" alert('close');}}</script>");
  client.println("</head>");
  client.println("<body>");
}
