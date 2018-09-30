#include <Servo.h>   
#include <math.h> 
#include <FlexiTimer2.h> //타이머
#include <SerialCommand.h>
#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(); //꼬리 모터를 제어하기 위한 드라이버
SerialCommand SCmd;  

//좌표값을 받기 위한 구조체
struct Point 
{
  float x; //x좌표
  float y; //y좌표
  int mark; //꼬리 상하모터 제어를 위한 그리기 진행상황
};

//몸체 4개의 다리 총 12개 서보모터
Servo servo[4][3];

//다리부분 12개 모터 핀번호 설정
const int servo_pin[4][3] = { { 2, 3, 4 },{ 5, 6, 7 },{ 8, 9, 10 },{ 11, 12, 13 } };

//로봇의 사이즈
const float length_a = 55; //다리 가운데 프레임 길이
const float length_b = 80; //다리 끝 프레임 길이 
const float length_c = 27.5; //다리 몸체쪽 프레임 길이
const float length_side = 71; //몸체 왼쪽,오른쪽 다리사이 거리
const float length_tail1 = 60; //꼬리 첫번째 프레임 길이 
const float length_tail2 = 61; //꼬리 두번째 프레임 길이

//움직임을 위한 상수
const float z_default = -50, z_up = -30, z_more_up = - 10, z_down=-80, z_boot = -28; //z축 기준과 움직일 상수값
const float x_default = 62, x_offset = 0; //x축 기준과 움직일 상수값
const float y_start = 0, y_step = 40; //y축 기준과 움직일 상수값

//움직임을 위한 변수
volatile float site_now[4][3]; //실제 다리의 위치
volatile float site_expect[4][3]; //움직일 예상 다리의 위치
float temp_speed[4][3]; //움직이기 전에 재계산될 각 축의 속도
float move_speed; //움직이는 속도 변수
float speed_multiple = 1; //움직일 속도의 배수
float spot_turn_speed = 4; //회전 속도
float leg_move_speed = 8; //다리 속도
float body_move_speed = 3; //몸체 속도
float stand_seat_speed = 1; //앉았다 일어나는 속도
volatile int rest_counter; //+1/0.02s, 자동으로 휴식
const float KEEP = 255; //움직이지 않기위한 상수

//회전을 위한 상수///////////////////////////////////////////////////////////////////////
//현재 다리와 움직일 예상 다리의 길이들을 구한 뒤 제2코사인 법칙을 이용하여 움직일 각도를 구하기
//제2코사인 법칙 : 각 변의 길이가 a, b, c 일때 사잇각의 각도 구하기
//cos(alpha) = (a^2 + b^2 - c^2)/2ab
const float temp_a = sqrt(pow(2 * x_default + length_side, 2) + pow(y_step, 2));
const float temp_b = 2 * (y_start + y_step) + length_side;
const float temp_c = sqrt(pow(2 * x_default + length_side, 2) + pow(2 * y_start + y_step + length_side, 2));
const float temp_alpha = acos((pow(temp_a, 2) + pow(temp_b, 2) - pow(temp_c, 2)) / 2 / temp_a / temp_b);
//위의 값들을 이용해서 회전할때 움직일 범위구하기
const float turn_x1 = (temp_a - length_side) / 2;
const float turn_y1 = y_start + y_step / 2;
const float turn_x0 = turn_x1 - temp_b * cos(temp_alpha);
const float turn_y0 = temp_b * sin(temp_alpha) - turn_y1 - length_side;
///////////////////////////////////////////////////////////////////////////////////////

//흡착판을 떼기 위한 모터 
float taking_off_motor; 
float taking_off_motor_len;

//그리기 관련 변수
Point ptArray[60]; //Point형식을 저장하기 위한 배열
int count = 0; //Point배열 보관 카운트

float theta0; //꼬리 상하모터 각도
float theta1; //꼬리 첫번째 좌우모터 각도
float theta2; //꼬리 두번째 좌우모터 각도
float theta0len;
float theta1len; 
float theta2len;

//모드 변경 변수
int mode_change = 1; //1:일반주행 2:장애물넘기 3:벽오르기

void setup()
{
  Serial.begin(9600); //시리얼 통신

  SCmd.addCommand("m", moving_action_cmd); //걷기 모드
  SCmd.addCommand("p", painting_action_cmd); //그릴 좌표 받기
  SCmd.addCommand("q", start_painting_cmd); //그리기 시작
  SCmd.addCommand("s", sp_control); //속도 제어
  SCmd.addCommand("c", mode_change_cmd); //모드 변경
  SCmd.addCommand("t", tail_control_cmd); //꼬리 제어
  
  SCmd.setDefaultHandler(unrecognized); //지정한 명령이 아닌 경우 unrecognized() 호출

  pwm.begin(); //드라이버 사용 시작
  pwm.setPWMFreq(60);  // 모터 고유의 주파수 값 설정
  
  //몸체 초기값 설정
  set_site(0, x_default - x_offset, y_start + y_step, z_boot);
  set_site(1, x_default - x_offset, y_start + y_step, z_boot);
  set_site(2, x_default + x_offset, y_start, z_boot);
  set_site(3, x_default + x_offset, y_start, z_boot);
  
  for (int i = 0; i < 4; i++)
  {
    for (int j = 0; j < 3; j++)
    {
      site_now[i][j] = site_expect[i][j];
    }
  }
  
  //타이머 시작
  FlexiTimer2::set(20, servo_service);
  FlexiTimer2::start();

  //서보모터 연결
  servo_attach();
}

void servo_attach(void)
{
  //몸체 모터 연결
  for (int i = 0; i < 4; i++)
  {
    for (int j = 0; j < 3; j++)
    {
      servo[i][j].attach(servo_pin[i][j]);
      delay(100);
    }
  }

  //흡착판 떼는 모터 초기값
  taking_off_motor = 0;
  taking_off_motor_len = map(taking_off_motor,0,180,150,600);
  for(int i = 0 ; i < 4 ; ++i)
  {
    pwm.setPWM(i+4,0,taking_off_motor_len);
  }
  
  //꼬리 초기값
  theta0 = 30;
  theta1 = 45;
  theta2 = 45;
  
  theta0len = map(theta0,0,180,150,600);
  theta1len = map(theta1,0,180,150,600);  
  theta2len = map(theta2,0,180,150,600);

  pwm.setPWM(0,0,theta0len);
  pwm.setPWM(1,0,theta1len);
  pwm.setPWM(2,0,theta2len);
}

void loop()
{
  SCmd.readSerial(); //명령 받기
}

// m 0 1: stand
// m 0 0: sit
// m 1 x: forward x step
// m 2 x: back x step
// m 3 x: right turn x step
// m 4 x: left turn x step
#define M_STAND_SIT    0
#define M_FORWARD      1
#define M_BACKWARD     2
#define M_LEFT         3
#define M_RIGHT        4
void moving_action_cmd(void)
{
  char *arg;
  int action_mode, n_step;

  arg = SCmd.next(); //행동 받기
  action_mode = atoi(arg);

  arg = SCmd.next(); //step 받기
  n_step = atoi(arg);

  switch (action_mode)
  {
  case M_FORWARD:
    if(mode_change == 1)
    {
      if (!is_stand())
        stand();
      step_forward(n_step);
    }
    else if(mode_change == 2)
    {
      overcome_step_forward(n_step);
    }
    else if(mode_change == 3)
    {
      climbing_step_forward(n_step);
    }
    break;
  case M_BACKWARD:
    if(mode_change == 1)
    {
      if (!is_stand())
        stand();
      step_back(n_step);
    }
    else if(mode_change == 2)
    {
      overcome_step_back(n_step);
    }
    else if(mode_change == 3)
    {
      climbing_step_back(n_step);
    }
    break;
  case M_LEFT:
    if(mode_change == 1)
    {
      if (!is_stand())
        stand();
      turn_left(n_step);
    }
    else if(mode_change == 2)
    {
      overcome_turn_left(n_step);
    }
    else if(mode_change == 3)
    {
      Serial.println("nothing");
    }
    break;
  case M_RIGHT:
    if(mode_change == 1)
    {
      if (!is_stand())
        stand();
      turn_right(n_step);
    }
    else if(mode_change == 2)
    {
      overcome_turn_right(n_step);
    }
    else if(mode_change == 3)
    {
      Serial.println("nothing");
    }
    break;
  case M_STAND_SIT:
    if (n_step)
    {
      if(mode_change == 1)
      {
          stand();
      }
      else if(mode_change == 2)
      {
        overcome_stand();
      }
      else if(mode_change == 3)
      {
        climbing_stand();
      }
    }
    else
      sit();
    break;
  default:
    Serial.println("Error");
    break;
  }
}

//받은 좌표들을 저장
void painting_action_cmd(void)
{
  String arg;  

  arg = SCmd.next(); //x좌표 받기
  ptArray[count].x = (arg).toFloat();

  arg = SCmd.next(); //y좌표 받기
  ptArray[count].y = (arg).toFloat();

  arg = SCmd.next(); //mark 받기
  if(arg == "s")
  {
    ptArray[count].mark = 1;
    count++;
  }
  else if(arg == "i")
  {
    ptArray[count].mark = 0;
    count++;
  }
  else if(arg == "f")
  {
    ptArray[count].mark = -1;
    count++;
  }
}

//저장된 좌표를 이용해서 그리기 시작
void start_painting_cmd(void)
{
  float temp_th1 = 0; //꼬리 첫번째 좌우모터 각도를 계산하기 위한 임시 변수
  float temp_th2 = 0; //꼬리 두번째 좌우모터 각도를 계산하기 위한 임시 변수

  //안정적으로 그리기위한 자세
  sit();
  delay(500);

  theta0 = 30;
  theta1 = 90;
  theta2 = 0;
  
  theta0len = map(theta0,0,180,150,600);
  theta1len = map(theta1,0,180,150,600);  
  theta2len = map(theta2,0,180,150,600);

  pwm.setPWM(0,0,theta0len);
  pwm.setPWM(1,0,theta1len);
  pwm.setPWM(2,0,theta2len);
  delay(1000);
  
  for (int i = 0; i < count; ++i)
  {
    //역기구학 공식 이용
    //원하는 지점의 좌표값을 알고있을 때 두개의 좌우모터의 각도를 계산하는 공식
    temp_th2 = 2 * atan(sqrt(((length_tail1 + length_tail2)*(length_tail1 + length_tail2) - (ptArray[i].x * ptArray[i].x + ptArray[i].y * ptArray[i].y)) / ((ptArray[i].x * ptArray[i].x) + (ptArray[i].y * ptArray[i].y) - (length_tail1 - length_tail2)*(length_tail1 - length_tail2))));
    temp_th1 = atan2(ptArray[i].y, ptArray[i].x) - atan(length_tail2*sin(temp_th2) / (length_tail1 + length_tail2*cos(temp_th2)));

    //호도법을 60분법으로 변경
    theta1 = temp_th1 * 180 / PI;
    theta2 = temp_th2 * 180 / PI;

    theta1len = map(theta1,0,180,150,600);  
    theta2len = map(theta2,0,180,150,600);

    //모터 움직이기
    pwm.setPWM(1,0,theta1len);
    delay(10);
    pwm.setPWM(2,0,theta2len);
    delay(10);

    if (ptArray[i].mark == 1) //그리기의 시작일때 꼬리 상하모터 내리기
    {
      delay(500);
      theta0 = 15;
      theta0len = map(theta0,0,180,150,600);
      pwm.setPWM(0,0,theta0len);
      delay(500);
    }
    else if (ptArray[i].mark == -1) //그리기의 종료일때 꼬리 상하모터 올리기
    {
      delay(500);
      theta0 = 30;
      theta0len = map(theta0,0,180,150,600);
      pwm.setPWM(0,0,theta0len);
      delay(500);
    }

    delay(200);
  }

  //그리기가 끝난 후 초기화
  for (int i = 0; i < count; ++i) //배열 초기화
  {
    Point pt; 
    ptArray[i] = pt;
  }
  
  count = 0; //카운트 초기화
  
  theta0 = 30;
  theta1 = 45;
  theta2 = 45;
  
  theta0len = map(theta0,0,180,150,600);
  theta1len = map(theta1,0,180,150,600);  
  theta2len = map(theta2,0,180,150,600);

  pwm.setPWM(0,0,theta0len);
  delay(10);
  pwm.setPWM(1,0,theta1len);
  delay(10);
  pwm.setPWM(2,0,theta2len);
  delay(10);
}

//기본 핸들러 얻어오기, 설정과 다른 명령을 입력했을때 호출
void unrecognized(const char *command) {
  Serial.println("No Match Command!");
}

//속도 제어
void sp_control()
{
  char* arg;
  int temp_sp; 

  arg = SCmd.next();
  temp_sp = atoi(arg);

  if(temp_sp==1)
  {
    leg_move_speed =3;
    body_move_speed=1;
    spot_turn_speed = 1;
  }
  else if(temp_sp==2)
  {
    leg_move_speed =5;
    body_move_speed=2;
    spot_turn_speed = 2;
  }
  else if(temp_sp==3)
  {
    leg_move_speed =7;
    body_move_speed=3;
    spot_turn_speed = 3;
  }
  else if(temp_sp==4)
  {
    leg_move_speed = 9;
    body_move_speed = 4;
    spot_turn_speed = 4;
  }
  else if(temp_sp==5)
  {
    leg_move_speed = 11;
    body_move_speed = 5;
    spot_turn_speed = 5;
  }
}

void tail_control_cmd(void)
{
  char* arg;
  char tail_mode; 

  arg = SCmd.next();
  tail_mode = *arg;

  if(tail_mode == 'b')
  {
    theta0 = 120;
    theta1 = 120;
    theta2 = 120;
  
    theta0len = map(theta0,0,180,150,600);
    theta1len = map(theta1,0,180,150,600);  
    theta2len = map(theta2,0,180,150,600);

    pwm.setPWM(0,0,theta0len);
    pwm.setPWM(1,0,theta1len);
    pwm.setPWM(2,0,theta2len);
  }
  else if(tail_mode == 's')
  {
    theta0 = 30;
    theta1 = 90;
    theta2 = 0;
  
    theta0len = map(theta0,0,180,150,600);
    theta1len = map(theta1,0,180,150,600);  
    theta2len = map(theta2,0,180,150,600);

    pwm.setPWM(0,0,theta0len);
    pwm.setPWM(1,0,theta1len);
    pwm.setPWM(2,0,theta2len);
  }
}

//흡착판 떼는 모터 내리기
void attach_leg(int leg)
{
  taking_off_motor = 0;
  taking_off_motor_len = map(taking_off_motor,0,180,150,600);
  pwm.setPWM(leg+4,0,taking_off_motor_len);
}

//흡착판 떼는 모터 올리기
void detach_leg(int leg)
{
  taking_off_motor = 120;
  taking_off_motor_len = map(taking_off_motor,0,180,150,600);
  pwm.setPWM(leg+4,0,taking_off_motor_len);
}

//앉았는지 안앉았는지 확인
bool is_stand(void)
{
  if (site_now[0][2] == z_default)
    return true;
  else
    return false;
}

void sit(void)
{
  move_speed = stand_seat_speed;
  for (int leg = 0; leg < 4; leg++)
  {
    set_site(leg, KEEP, KEEP, z_boot);
  }
  wait_all_reach();
}

void stand(void)
{
  move_speed = stand_seat_speed;
  for (int leg = 0; leg < 4; leg++)
  {
    set_site(leg, KEEP, KEEP, z_default);
  }
  wait_all_reach();
}

void turn_left(unsigned int step)
{
  move_speed = spot_turn_speed;
  while (step-- > 0)
  {
    if (site_now[3][1] == y_start)
    {
      //leg 3&1 move
      detach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_up);
      wait_all_reach();

      set_site(0, turn_x1 - x_offset, turn_y1, z_default);
      set_site(1, turn_x0 - x_offset, turn_y0, z_default);
      set_site(2, turn_x1 + x_offset, turn_y1, z_default);
      set_site(3, turn_x0 + x_offset, turn_y0, z_up);
      wait_all_reach();

      attach_leg(3);
      set_site(3, turn_x0 + x_offset, turn_y0, z_default);
      wait_all_reach();

      set_site(0, turn_x1 + x_offset, turn_y1, z_default);
      set_site(1, turn_x0 + x_offset, turn_y0, z_default);
      set_site(2, turn_x1 - x_offset, turn_y1, z_default);
      set_site(3, turn_x0 - x_offset, turn_y0, z_default);
      wait_all_reach();

      
      detach_leg(1);
      set_site(1, turn_x0 + x_offset, turn_y0, z_up);
      wait_all_reach();

      set_site(0, x_default + x_offset, y_start, z_default);
      set_site(1, x_default + x_offset, y_start, z_up);
      set_site(2, x_default - x_offset, y_start + y_step, z_default);
      set_site(3, x_default - x_offset, y_start + y_step, z_default);
      wait_all_reach();
  
      attach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_default);
      wait_all_reach();
    }
    else
    {
      //leg 0&2 move
      detach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_up);
      wait_all_reach();

      set_site(0, turn_x0 + x_offset, turn_y0, z_up);
      set_site(1, turn_x1 + x_offset, turn_y1, z_default);
      set_site(2, turn_x0 - x_offset, turn_y0, z_default);
      set_site(3, turn_x1 - x_offset, turn_y1, z_default);
      wait_all_reach();

      attach_leg(0);
      set_site(0, turn_x0 + x_offset, turn_y0, z_default);
      wait_all_reach();

      set_site(0, turn_x0 - x_offset, turn_y0, z_default);
      set_site(1, turn_x1 - x_offset, turn_y1, z_default);
      set_site(2, turn_x0 + x_offset, turn_y0, z_default);
      set_site(3, turn_x1 + x_offset, turn_y1, z_default);
      wait_all_reach();

      
      detach_leg(2);
      set_site(2, turn_x0 + x_offset, turn_y0, z_up);
      wait_all_reach();

      set_site(0, x_default - x_offset, y_start + y_step, z_default);
      set_site(1, x_default - x_offset, y_start + y_step, z_default);
      set_site(2, x_default + x_offset, y_start, z_up);
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();

      attach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_default);
      wait_all_reach();
    }
  }
}

void turn_right(unsigned int step)
{
  move_speed = spot_turn_speed;
  while (step-- > 0)
  {
    if (site_now[2][1] == y_start)
    {
      //leg 2&0 move
      detach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_up);
      wait_all_reach();

      set_site(0, turn_x0 - x_offset, turn_y0, z_default);
      set_site(1, turn_x1 - x_offset, turn_y1, z_default);
      set_site(2, turn_x0 + x_offset, turn_y0, z_up);
      set_site(3, turn_x1 + x_offset, turn_y1, z_default);
      wait_all_reach();

      attach_leg(2);
      set_site(2, turn_x0 + x_offset, turn_y0, z_default);
      wait_all_reach();

      set_site(0, turn_x0 + x_offset, turn_y0, z_default);
      set_site(1, turn_x1 + x_offset, turn_y1, z_default);
      set_site(2, turn_x0 - x_offset, turn_y0, z_default);
      set_site(3, turn_x1 - x_offset, turn_y1, z_default);
      wait_all_reach();

      detach_leg(0);
      set_site(0, turn_x0 + x_offset, turn_y0, z_up);
      wait_all_reach();

      set_site(0, x_default + x_offset, y_start, z_up);
      set_site(1, x_default + x_offset, y_start, z_default);
      set_site(2, x_default - x_offset, y_start + y_step, z_default);
      set_site(3, x_default - x_offset, y_start + y_step, z_default);
      wait_all_reach();

      attach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_default);
      wait_all_reach();
    }
    else
    {
      //leg 1&3 move
      detach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_up);
      wait_all_reach();

      set_site(0, turn_x1 + x_offset, turn_y1, z_default);
      set_site(1, turn_x0 + x_offset, turn_y0, z_up);
      set_site(2, turn_x1 - x_offset, turn_y1, z_default);
      set_site(3, turn_x0 - x_offset, turn_y0, z_default);
      wait_all_reach();

      attach_leg(1);
      set_site(1, turn_x0 + x_offset, turn_y0, z_default);
      wait_all_reach();

      set_site(0, turn_x1 - x_offset, turn_y1, z_default);
      set_site(1, turn_x0 - x_offset, turn_y0, z_default);
      set_site(2, turn_x1 + x_offset, turn_y1, z_default);
      set_site(3, turn_x0 + x_offset, turn_y0, z_default);
      wait_all_reach();

      detach_leg(3);
      set_site(3, turn_x0 + x_offset, turn_y0, z_up);
      wait_all_reach();

      set_site(0, x_default - x_offset, y_start + y_step, z_default);
      set_site(1, x_default - x_offset, y_start + y_step, z_default);
      set_site(2, x_default + x_offset, y_start, z_default);
      set_site(3, x_default + x_offset, y_start, z_up);
      wait_all_reach();

      attach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();
    }
  }
}

void step_forward(unsigned int step)
{
  move_speed = leg_move_speed;
  while (step-- > 0)
  {
    if (site_now[2][1] == y_start)
    {
      //leg 2&1 move
      detach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_up);
      wait_all_reach();
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();
      attach_leg(2);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();

      move_speed = body_move_speed;
      
      set_site(0, x_default + x_offset, y_start, z_default);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_default);
      set_site(2, x_default - x_offset, y_start + y_step, z_default);
      set_site(3, x_default - x_offset, y_start + y_step, z_default);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(1);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();
      set_site(1, x_default + x_offset, y_start, z_up);
      wait_all_reach();
      attach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_default);
      wait_all_reach();
    }
    else
    {
      //leg 0&3 move
      detach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_up);
      wait_all_reach();
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();
      attach_leg(0);
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default - x_offset, y_start + y_step, z_default);
      set_site(1, x_default - x_offset, y_start + y_step, z_default);
      set_site(2, x_default + x_offset, y_start, z_default);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(3);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();
      set_site(3, x_default + x_offset, y_start, z_up);
      wait_all_reach();
      attach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();
    }
  }
}

void step_back(unsigned int step)
{
  move_speed = leg_move_speed;
  while (step-- > 0)
  {
    if (site_now[3][1] == y_start)
    {
      //leg 3&0 move
      detach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_up);
      wait_all_reach();
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();
      attach_leg(3);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_default);
      set_site(1, x_default + x_offset, y_start, z_default);
      set_site(2, x_default - x_offset, y_start + y_step, z_default);
      set_site(3, x_default - x_offset, y_start + y_step, z_default);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(0);
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();
      set_site(0, x_default + x_offset, y_start, z_up);
      wait_all_reach();
      attach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_default);
      wait_all_reach();
    }
    else
    {
      //leg 1&2 move
      detach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_up);
      wait_all_reach();
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();
      attach_leg(1);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default - x_offset, y_start + y_step, z_default);
      set_site(1, x_default - x_offset, y_start + y_step, z_default);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_default);
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(2);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();
      set_site(2, x_default + x_offset, y_start, z_up);
      wait_all_reach();
      attach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_default);
      wait_all_reach();
    }
  }
}

//모드 변경 일반주행->장애물넘기->벽오르기
void mode_change_cmd(void)
{
  mode_change++;
  
  if(mode_change == 4)
  {
    mode_change = 1;
  }
  
  if(mode_change == 1)
  {
    stand();
  }
  else if(mode_change == 2)
  {
    overcome_stand();
  }
  else if(mode_change == 3)
  {
    climbing_stand();
  }
}

void overcome_stand(void)
{
  move_speed = stand_seat_speed;
  for (int leg = 0; leg < 4; leg++)
  {
    set_site(leg, KEEP, KEEP, z_down);
  }
  wait_all_reach();
}

void overcome_turn_left(unsigned int step)
{
  move_speed = spot_turn_speed;
  while (step-- > 0)
  {
    if (site_now[3][1] == y_start)
    {
      //leg 3&1 move
      detach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_down);
      wait_all_reach();

      set_site(0, turn_x1 - x_offset, turn_y1, z_down);
      set_site(1, turn_x0 - x_offset, turn_y0, z_down);
      set_site(2, turn_x1 + x_offset, turn_y1, z_down);
      set_site(3, turn_x0 + x_offset, turn_y0, z_default);
      wait_all_reach();

      attach_leg(3);
      set_site(3, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      set_site(0, turn_x1 + x_offset, turn_y1, z_down);
      set_site(1, turn_x0 + x_offset, turn_y0, z_down);
      set_site(2, turn_x1 - x_offset, turn_y1, z_down);
      set_site(3, turn_x0 - x_offset, turn_y0, z_down);
      wait_all_reach();

      detach_leg(1);
      set_site(1, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      set_site(0, x_default + x_offset, y_start, z_down);
      set_site(1, x_default + x_offset, y_start, z_default);
      set_site(2, x_default - x_offset, y_start + y_step, z_down);
      set_site(3, x_default - x_offset, y_start + y_step, z_down);
      wait_all_reach();

      attach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
    else
    {
      //leg 0&2 move
      detach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_down);
      wait_all_reach();

      set_site(0, turn_x0 + x_offset, turn_y0, z_default);
      set_site(1, turn_x1 + x_offset, turn_y1, z_down);
      set_site(2, turn_x0 - x_offset, turn_y0, z_down);
      set_site(3, turn_x1 - x_offset, turn_y1, z_down);
      wait_all_reach();

      attach_leg(0);
      set_site(0, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      set_site(0, turn_x0 - x_offset, turn_y0, z_down);
      set_site(1, turn_x1 - x_offset, turn_y1, z_down);
      set_site(2, turn_x0 + x_offset, turn_y0, z_down);
      set_site(3, turn_x1 + x_offset, turn_y1, z_down);
      wait_all_reach();

      detach_leg(2);
      set_site(2, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      set_site(0, x_default - x_offset, y_start + y_step, z_down);
      set_site(1, x_default - x_offset, y_start + y_step, z_down);
      set_site(2, x_default + x_offset, y_start, z_default);
      set_site(3, x_default + x_offset, y_start, z_down);
      wait_all_reach();

      attach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
  }
}

void overcome_turn_right(unsigned int step)
{
  move_speed = spot_turn_speed;
  while (step-- > 0)
  {
    if (site_now[2][1] == y_start)
    {
      //leg 2&0 move
      detach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_down);
      wait_all_reach();

      set_site(0, turn_x0 - x_offset, turn_y0, z_down);
      set_site(1, turn_x1 - x_offset, turn_y1, z_down);
      set_site(2, turn_x0 + x_offset, turn_y0, z_default);
      set_site(3, turn_x1 + x_offset, turn_y1, z_down);
      wait_all_reach();

      attach_leg(2);
      set_site(2, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      set_site(0, turn_x0 + x_offset, turn_y0, z_down);
      set_site(1, turn_x1 + x_offset, turn_y1, z_down);
      set_site(2, turn_x0 - x_offset, turn_y0, z_down);
      set_site(3, turn_x1 - x_offset, turn_y1, z_down);
      wait_all_reach();

      detach_leg(0);
      set_site(0, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      set_site(0, x_default + x_offset, y_start, z_default);
      set_site(1, x_default + x_offset, y_start, z_down);
      set_site(2, x_default - x_offset, y_start + y_step, z_down);
      set_site(3, x_default - x_offset, y_start + y_step, z_down);
      wait_all_reach();

      attach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
    else
    {
      //leg 1&3 move
      detach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_down);
      wait_all_reach();

      set_site(0, turn_x1 + x_offset, turn_y1, z_down);
      set_site(1, turn_x0 + x_offset, turn_y0, z_default);
      set_site(2, turn_x1 - x_offset, turn_y1, z_down);
      set_site(3, turn_x0 - x_offset, turn_y0, z_down);
      wait_all_reach();

      attach_leg(1);
      set_site(1, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      set_site(0, turn_x1 - x_offset, turn_y1, z_down);
      set_site(1, turn_x0 - x_offset, turn_y0, z_down);
      set_site(2, turn_x1 + x_offset, turn_y1, z_down);
      set_site(3, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      detach_leg(3);
      set_site(3, turn_x0 + x_offset, turn_y0, z_down);
      wait_all_reach();

      set_site(0, x_default - x_offset, y_start + y_step, z_down);
      set_site(1, x_default - x_offset, y_start + y_step, z_down);
      set_site(2, x_default + x_offset, y_start, z_down);
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();

      attach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
  }
}

void overcome_step_forward(unsigned int step)
{
  move_speed = leg_move_speed;
  while (step-- > 0)
  {
    if (site_now[2][1] == y_start)
    {
      //leg 2&1 move
      detach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      attach_leg(2);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_down);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default + x_offset, y_start, z_down);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_down);
      set_site(2, x_default - x_offset, y_start + y_step, z_down);
      set_site(3, x_default - x_offset, y_start + y_step, z_down);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(1);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      set_site(1, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      attach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
    else
    {
      //leg 0&3 move
      detach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      attach_leg(0);
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_down);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default - x_offset, y_start + y_step, z_down);
      set_site(1, x_default - x_offset, y_start + y_step, z_down);
      set_site(2, x_default + x_offset, y_start, z_down);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_down);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(3);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      attach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
  }
}

void overcome_step_back(unsigned int step)
{
  move_speed = leg_move_speed;
  while (step-- > 0)
  {
    if (site_now[3][1] == y_start)
    {
      //leg 3&0 move
      detach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      attach_leg(3);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_down);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_down);
      set_site(1, x_default + x_offset, y_start, z_down);
      set_site(2, x_default - x_offset, y_start + y_step, z_down);
      set_site(3, x_default - x_offset, y_start + y_step, z_down);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(0);
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      set_site(0, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      attach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
    else
    {
      //leg 1&2 move
      detach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      attach_leg(1);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_down);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default - x_offset, y_start + y_step, z_down);
      set_site(1, x_default - x_offset, y_start + y_step, z_down);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_down);
      set_site(3, x_default + x_offset, y_start, z_down);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(2);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      set_site(2, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      attach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
  }
}

void climbing_stand(void)
{
  move_speed = stand_seat_speed;
  
  set_site(0, KEEP, KEEP, z_up);
  set_site(1, KEEP, KEEP, z_down);
  set_site(2, KEEP, KEEP, z_up);
  set_site(3, KEEP, KEEP, z_down);
  
  wait_all_reach();

  theta0 = 40;
  theta1 = 90;
  theta2 = 0;
  
  theta0len = map(theta0,0,180,150,600);
  theta1len = map(theta1,0,180,150,600);  
  theta2len = map(theta2,0,180,150,600);

  pwm.setPWM(0,0,theta0len);
  pwm.setPWM(1,0,theta1len);
  pwm.setPWM(2,0,theta2len);
}

void climbing_step_forward(unsigned int step)
{
  move_speed = leg_move_speed;
  while (step-- > 0)
  {
    if (site_now[2][1] == y_start)
    {
      //leg 2&1 move
      detach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_more_up);
      wait_all_reach();
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_more_up);
      wait_all_reach();
      attach_leg(2);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();

      move_speed = body_move_speed;
      
      set_site(0, x_default + x_offset, y_start, z_up);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_down);
      set_site(2, x_default - x_offset, y_start + y_step, z_up);
      set_site(3, x_default - x_offset, y_start + y_step, z_down);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(1);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      set_site(1, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      attach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
    else
    {
      //leg 0&3 move
      detach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_more_up);
      wait_all_reach();
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_more_up);
      wait_all_reach();
      attach_leg(0);
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_up);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default - x_offset, y_start + y_step, z_up);
      set_site(1, x_default - x_offset, y_start + y_step, z_down);
      set_site(2, x_default + x_offset, y_start, z_up);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_down);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(3);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      attach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_down);
      wait_all_reach();
    }
  }
}

void climbing_step_back(unsigned int step)
{
  move_speed = leg_move_speed;
  while (step-- > 0)
  {
    if (site_now[3][1] == y_start)
    {
      //leg 3&0 move
      detach_leg(3);
      set_site(3, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      attach_leg(3);
      set_site(3, x_default + x_offset, y_start + 2 * y_step, z_down);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_up);
      set_site(1, x_default + x_offset, y_start, z_down);
      set_site(2, x_default - x_offset, y_start + y_step, z_up);
      set_site(3, x_default - x_offset, y_start + y_step, z_down);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(0);
      set_site(0, x_default + x_offset, y_start + 2 * y_step, z_more_up);
      wait_all_reach();
      set_site(0, x_default + x_offset, y_start, z_more_up);
      wait_all_reach();
      attach_leg(0);
      set_site(0, x_default + x_offset, y_start, z_up);
      wait_all_reach();
    }
    else
    {
      //leg 1&2 move
      detach_leg(1);
      set_site(1, x_default + x_offset, y_start, z_default);
      wait_all_reach();
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_default);
      wait_all_reach();
      attach_leg(1);
      set_site(1, x_default + x_offset, y_start + 2 * y_step, z_down);
      wait_all_reach();

      move_speed = body_move_speed;

      set_site(0, x_default - x_offset, y_start + y_step, z_up);
      set_site(1, x_default - x_offset, y_start + y_step, z_down);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_up);
      set_site(3, x_default + x_offset, y_start, z_down);
      wait_all_reach();

      move_speed = leg_move_speed;

      detach_leg(2);
      set_site(2, x_default + x_offset, y_start + 2 * y_step, z_more_up);
      wait_all_reach();
      set_site(2, x_default + x_offset, y_start, z_more_up);
      wait_all_reach();
      attach_leg(2);
      set_site(2, x_default + x_offset, y_start, z_up);
      wait_all_reach();
    }
  }
}

void servo_service(void)
{
  //전역 인터럽트 허가 받기 
  sei(); //인터럽트란? 컴퓨터 작동 중에 예기치 않은 문제가 발생한 경우라도 업무 처리가 계속될 수 있도록 하는 컴퓨터 운영체계의 한 기능.
  static float alpha, beta, gamma;

  for (int i = 0; i < 4; i++)
  {
    for (int j = 0; j < 3; j++)
    {
      if (abs(site_now[i][j] - site_expect[i][j]) >= abs(temp_speed[i][j])) //실제 위치와 예상 위치의 차이가 축의 임시속도보다 크다면
        site_now[i][j] += temp_speed[i][j]; //추가시켜서 늘려준다
      else
        site_now[i][j] = site_expect[i][j]; //아니라면 도착했으므로 예상 위치를 실제 위치에 넣는다
    }

    //각도 구하기
    cartesian_to_polar(alpha, beta, gamma, site_now[i][0], site_now[i][1], site_now[i][2]);
    polar_to_servo(i, alpha, beta, gamma);
  }

  rest_counter++;
}

void set_site(int leg, float x, float y, float z)
{
  float length_x = 0, length_y = 0, length_z = 0;

  if (x != KEEP)
    length_x = x - site_now[leg][0]; //입력받은 움직일 위치에서 실제 위치를 뺀다.
  if (y != KEEP)
    length_y = y - site_now[leg][1];
  if (z != KEEP)
    length_z = z - site_now[leg][2];

  float length = sqrt(pow(length_x, 2) + pow(length_y, 2) + pow(length_z, 2)); //원점에서 3차원 공간상 입력받은 가야할 좌표의 거리

  temp_speed[leg][0] = length_x / length * move_speed * speed_multiple; //거리에 따라 속도가 비례
  temp_speed[leg][1] = length_y / length * move_speed * speed_multiple;
  temp_speed[leg][2] = length_z / length * move_speed * speed_multiple;

  if (x != KEEP)
    site_expect[leg][0] = x; //입력받은 움직일 위치는 예상 위치에 넣는다.
  if (y != KEEP)
    site_expect[leg][1] = y;
  if (z != KEEP)
    site_expect[leg][2] = z;
}

void wait_reach(int leg)
{
  while (1) //가야할 예상 위치와 실제 위치가 같아질때까지 무한루프를 돌며 빠져나오지 않는다.
    if (site_now[leg][0] == site_expect[leg][0])
      if (site_now[leg][1] == site_expect[leg][1])
        if (site_now[leg][2] == site_expect[leg][2])
          break;
}

void wait_all_reach(void)
{
  for (int i = 0; i < 4; i++) //모든 다리 기다리기
    wait_reach(i);
}

//모터 각도 구하기
void cartesian_to_polar(volatile float &alpha, volatile float &beta, volatile float &gamma, volatile float x, volatile float y, volatile float z)
{
  //alpha 모터에서 다리 끝점까지 길이 구하기
  float v, w;
  w = (x >= 0 ? 1 : -1) * (sqrt(pow(x, 2) + pow(y, 2)));
  v = w - length_c;

  //제2코사인 법칙을 이용하여 alpha, beta 구하기
  //제2코사인 법칙 : 각 변의 길이가 a, b, c 일때 사잇각의 각도 구하기
  //cos(alpha) = (a^2 + b^2 - c^2)/2ab
  alpha = atan2(z, v) + acos((pow(length_a, 2) - pow(length_b, 2) + pow(v, 2) + pow(z, 2)) / 2 / length_a / sqrt(pow(v, 2) + pow(z, 2)));
  beta = acos((pow(length_a, 2) + pow(length_b, 2) - pow(v, 2) - pow(z, 2)) / 2 / length_a / length_b);
  
  //x와 y를 이용하여 gamma구하기
  gamma = (w >= 0) ? atan2(y, x) : atan2(-y, -x); //tan(gamma)로 기울기 구하기
  
  //호도법을 60분법으로 변경
  alpha = alpha / PI * 180;
  beta = beta / PI * 180;
  gamma = gamma / PI * 180;
}

//모터 움직이기
void polar_to_servo(int leg, float alpha, float beta, float gamma)
{
  if (leg == 0)
  {
    alpha = 90 - alpha;
    beta = beta;
    gamma = gamma + 90;
  }
  else if (leg == 1)
  {
    alpha = alpha + 90;
    beta = 180 - beta;
    gamma = 90 - gamma;
  }
  else if (leg == 2)
  {
    alpha = alpha + 90;
    beta = 180 - beta;
    gamma = 90 - gamma;
  }
  else if (leg == 3)
  {
    alpha = 90 - alpha;
    beta = beta;
    gamma = gamma + 90;
  }

  //서보모터 움직이기
  servo[leg][0].write(alpha);
  servo[leg][1].write(beta);
  servo[leg][2].write(gamma);
}
