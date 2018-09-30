//사용자가 이름으로 원하는 데이터를 검색하는 기능
function searchPOI() {
	markerLayer.clearMarkers();
	searchDataIdx = 0;
	// 매번 검색인덱스 초기화 ( 전역변수)
	var id = $('#tag').val();
	// 태그의( 텍스트 칸의) 밸류를 받아와서 쿼리 검색을 실시한다.
	tdata = new Tmap.TData();
	tdata.events.register("onComplete", tdata, onCompleteTData);

	var center = map.getCenter();

	tdata.getPOIDataFromSearch(encodeURIComponent(id), {////////////////////////////////////////"에서" id 로
		centerLon : center.lon,
		centerLat : center.lat
	});
}
//검색된 데이터의 이벤트 처리부분
function onCompleteTData(e) {

	if (jQuery(this.responseXML).find("searchPoiInfo pois poi").text() != '') {
		jQuery(this.responseXML).find("searchPoiInfo pois poi").each(function() {
			var upperAddrName = jQuery(this).find("upperAddrName").text();

			if (upperAddrName == "제주")// 필터링을 이걸로
			{
				var name = jQuery(this).find("name").text();
				var lon = jQuery(this).find("frontLon").text();
				var lat = jQuery(this).find("frontLat").text();

				var options = {
					label : new Tmap.Label(name),
					lonlat : new Tmap.LonLat(lon, lat)
				};
				searchData.push(new structMinfo());
				// 서치데이터 라는 배열에 새로운 구조체를 넣어서 푸쉬
				searchData[searchDataIdx].name = name;
				// 값넣고
				searchData[searchDataIdx].lon = lon;
				searchData[searchDataIdx].lat = lat;
				searchDataIdx++;
				// 인덱스 증가
				addMarker(options);
			}

		});
	} else {
		alert('검색결과가 없습니다.');
	}
	map.setCenter(new Tmap.LonLat(14081257.293714, 3947485.458282), 10);
} 