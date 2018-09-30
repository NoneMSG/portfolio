<!DOCTYPE html>
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
      <title>예제</title>
      // 스크립트 파일을 불러오는 곳
      
      
      <script src="https://apis.skplanetx.com/tmap/js?version=1&format=javascript&appKey=af0939ae-0294-3284-ab0d-88b7831e6716"></script>
      <link rel="stylesheet" href="./css/style.css"  Type="text/css">
      <script src="./sc/jquery-3.0.0.min.js"></script>
      <script src="./js/variables.js"></script>
      <script src="./js/initialize.js"></script>
      <script src="./js/markers.js"></script>
      <script src="./js/popups.js"></script>
      <script src="./js/listbox.js"></script>
      <script src="./js/searchroute.js"></script>
      <script src="./js/reset.js"></script>
      
    </head>
    <!--html의 body부분에 지도 데이터를 불러온다.--!>
  <body onload="initTmap()">
  
         <div id="pages_wrraper">
            <div class="page-wrap" >
           
           <div id="map_div" style="float: right; width:100%; height: 100%; position:relative;">
               </div>
          
            <div id="first">
               <div align="center">출발지</div>
               <select name="startListBox" style="display:inline; width:100% " id ="startListBox"  size='3'></select>
            </div>
            
              <div id ="middle">
                 <div align="center" >경유지</div>
                 <select name="middleListBox" style="display:inline; width:100% "  id ="middleListBox"   size='6'></select>
                <div>
                   <input type="button" id ="moveUp" name="moveUp" onclick='MoveUp("middleListBox")'  value="moveUp"> 
                   <input type="button" id ="moveDown" name="moveDown" onclick='MoveDown("middleListBox")'  value="moveDown">   
                </div>
              </div>
             
              <div id="end"><div align="center">도착지</div>
            <select name="endListBox" style="display:inline; width:100% " id ="endListBox"  size='3'></select>
              </div>
              
          
           <div id="menu_wrap" class="bg_white">         
               <input type="text" id="tag" name="Tag"  onfocus="this.value=''" value="조랑말" style="width: 60%" onkeydown="javascript: if (event.keyCode == 13) {searchPOI();}">
               <input type="button" id ="search" name="search" onclick="searchPOI()"  value="검색">
               <br><br>
               <input type="button" onclick="makeroute()" name="search" value="길찾기 시작">
               <input type="button" onclick="customRoute()" name="user" value="사용자 지정">
               
               <input type="button" onclick="resetLayers()" name="resetbtn" value="reset"> 
           </div>
           
           <div style="float: right; background-color: yellow; width: 80%; height: 400px">
              <div id="roadbutton">
               
            </div>
            <div id="marker_name">
                 <p align="left">여행지 리스트: <p id="name" align ="left"></p></p></div>
            </div>
            <div style="float: left; background-color: skyblue; width: 20%; height: 400px"></div> 
           </div> 
       </div>      
   </body>
</html>