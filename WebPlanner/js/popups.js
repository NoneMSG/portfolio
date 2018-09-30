//아이콘에 마우스클릭시 팝업이 나타난다
function onOverMouse(e) {
	this.popup.show();
}
//팝업이 없어지게 하는 부분
function onOutMouse(e) {
	this.popup.hide();
}
//지역정보를 입력후 해당하는 좌표의 데이터들을 지도에 아이콘으로 표현해주는 부분
function addMarker(options) {
	var size = new Tmap.Size(12, 19);
	var offset = new Tmap.Pixel(-(size.w / 2), -(size.h / 2));
	var icon = new Tmap.Icon("https://developers.skplanetx.com/upload/tmap/marker/pin_b_s_simple.png", size, offset);
	var marker = new Tmap.Markers(options.lonlat, icon, options.label);

	markerLayer.addMarker(marker);

	marker.events.register("mouseover", marker, onOverMouse);
	marker.events.register("mouseout", marker, onOutMouse);
	marker.events.register("click", marker, onClickMarker);

}

function getSelected(e) {

	console.log("==========================");
	for ( i = 0; i < mList.length; ++i) {
		console.log(mList[i].lon);
		console.log(mList[i].lat);
	}
	for ( i = 0; i < mList.length; ++i) {
		if (mList[i].lon == e.object.lonlat.lon && mList[i].lat == e.object.lonlat.lat) {
			return 1;
		}
	}
	return 0;
}

function closecallback() {
	popup.hide();
	map.removePopup(popup);
	markertLayer.removeMarker(marker);
	hcount--;
}
//마커를 클릭하면 발생하는 이벤트 함수
function onClickMarker(e) {
	hcount++;
	var size = new Tmap.Size(15, 25);
	var offset = new Tmap.Pixel(-(size.w / 2), -(size.h / 2));
	var icon = new Tmap.Icon("https://developers.skplanetx.com/upload/tmap/marker/pin_b_s_simple.png", size, offset);
	marker = new Tmap.Markers(e.object.lonlat, icon);
	markerLayer.addMarker(marker);

	console.log(e.object.labelHtml);
	console.log(e.object.lonlat.lon);
	console.log(e.object.lonlat.lat);

	var ranNum = Math.floor(Math.random() * 100) + 1;
	var key = ranNum.toString();

	var selected = getSelected(e);

	console.log("selected : " + selected);
	if (selected == 1) {
		popup = new Tmap.Popup(key, new Tmap.LonLat(e.object.lonlat.lon, e.object.lonlat.lat), new Tmap.Size(200, 50), "<div class=enterleave width: 200px; height: 70px><p id=pp></p><input type=button id=start value='출발지' onclick=addStart()><input type=button id=e value='경유지제거' onclick=deleteStop()><input type=button id=desti value='목적지' onclick=addDestination()></div>", true, closecallback);
	} else if (selected == 0) {
		popup = new Tmap.Popup(key, new Tmap.LonLat(e.object.lonlat.lon, e.object.lonlat.lat), new Tmap.Size(200, 50), "<div class=enterleave width: 200px; height: 70px><p id=pp></p><input type=button id=start value='출발지' onclick=addStart()><input type=button id=e value='경유지추가' onclick=addStop()><input type=button id=desti value='목적지' onclick=addDestination()></div>", true, closecallback);
	}
	var n = 0;

	if (popup.id != null) {
		map.addPopup(popup);
		$("div.enterleave").mouseenter(function() {
			n += 1;
		}).mouseleave(function() {
		});
	}
	var s = document.getElementById("pp");
	s.innerHTML = e.object.labelHtml;
	tempMarkerName = e.object.labelHtml;
	name = e.object.labelHtml;
}

//경유지를 지우기 위한 기능
function deleteStop() {

	if (mList == null || mList.length == 0) {
		alert("삭제할 경유지가 없습니다.");
		map.removePopup(popup);
		popup.hide();
		return;
	}

	if (popup.id != null) {
		for (var i = 0; i < mList.length; i++) {
			if (mList[i].lon == popup.lonlat.lon) {
				mList.splice(i, 1);
				count--;
			}
		}
		map.removePopup(popup);
		popup.hide();
	}
	markerLayer.removeMarker(marker);
	showListBox1();
}
//경유지 추가 기능
function addStop(e) {
	if ((popup.lonlat.lon == start.lon) || (popup.lonlat.lon == destination.lon)) {
		alert("경유지가 출발지 또는 목적지와 같습니다.");
		popup.hide();
		map.removePopup(popup);
		return;
	}
	console.log("dd", popup.lonlat.lon);
	if (mList.length > 3) {
		alert("경유지 초과 ");
		popup.hide();
		map.removePopup(popup);
		return;
	}

	if (popup.id == null) {
		alert("좌표추가 실패 팝업객체 생성 실패.");
		popup.hide();
		map.removePopup(popup);
		return;
	}
	if (popup.id != null) {
		mList.push(new structMinfo());
		mList[count].lon = popup.lonlat.lon;
		mList[count].lat = popup.lonlat.lat;
		mList[count].name = tempMarkerName;

		count++;

		for (var i = 0; i < mList.length; i++)
			console.log("mList[" + i + "]" + mList[i].lon + ", " + mList[i].lat + ", " + mList[i].name);

		console.log("length" + mList.length);
		popup.hide();
		map.removePopup(popup);
		markerLayer.removeMarker(marker);
		showListBox();
	}
}
//출발지 추가
function addStart() {
	if (popup.id == null) {
		alert("좌표추가 실패 팝업객체 생성 실패.");
		popup.hide();
		map.removePopup(popup);
		return;
	}
	if (popup.id != null) {
		start.lon = popup.lonlat.lon;
		start.lat = popup.lonlat.lat;
		start.name = tempMarkerName;
		popup.hide();
		map.removePopup(popup);
		markerLayer.removeMarker(marker);
		showListBoxStart();
	}

}

function addDestination() {//목적지 추가
	if (popup.id == null) {
		alert("좌표추가 실패 팝업객체 생성 실패.");
		popup.hide();
		map.removePopup(popup);
		return;
	}
	if (popup.id != null) {
		destination.lon = popup.lonlat.lon;
		destination.lat = popup.lonlat.lat;
		destination.name = tempMarkerName;
		popup.hide();
		map.removePopup(popup);
		markerLayer.removeMarker(marker);
		showListBoxDestination();
	}
}