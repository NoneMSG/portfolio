
var tempMarkerName = "";
var map,
    markerLayer, // 기본 마커들 들어가는 레이어
    iconLayer,
    highlightLayer,
    routeLayer;
// 루트찾는 데이터 넣는 배열
var tdata;
//경유지 개수 제한 5개
//사용자의 마커정보 임시공간
var tempmList = new Array();
// 팝업마커 임시공간
var mList = new Array();
var permList = new Array();
//마커간 정렬을 위한 리스트

var markerList = new Array();
//출발지 정렬된 경유지 목적지가 들어있는 배열
// 마커 카운트
var count = 0;
var hcount = 0;
var name;
// 마커 카운트
var realForm;
var realDistance = 0;

// 처음 검색을 한뒤 쿼리문을 이용해 나온 데이터 배열
var searchData = new Array();
var searchDataIdx = 0;

var start = new structMinfo();

var destination = new structMinfo();

//길찾기를 위한 마커정보 저장하기 위한 구조체 함수
function structMinfo() {
	var lon;
	var lat;
	var distance;
	var name;
}