//지도 이니셜부분
function initTmap() {
	map = new Tmap.Map({// tmap생성
		div : 'map_div',
		width : '100%',
		height : '600px',
		animation : false
	});
	map.setCenter(new Tmap.LonLat(14081257.293714, 3947485.458282), 10);
	//생성한 맵 기준좌표 설정
	addMarkerLayer();
}
//지도객체에 필요한 레이어들을 추가하기 위한 함수들
function addMarkerLayer() {

	markerLayer = new Tmap.Layer.Markers("marker");
	iconLayer = new Tmap.Layer.Markers("icon");
	highlightLayer = new Tmap.Layer.Markers("highlight");
	map.addLayer(iconLayer);
	map.addLayer(markerLayer);
	map.addLayer(highlightLayer);
}