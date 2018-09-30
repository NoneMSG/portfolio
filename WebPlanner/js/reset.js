//데이터들을 초기화 시키기 위한 finalize
function resetLayers() {
	if (tdata != null) {

		mList = new Array();
		markerlist = new Array();
		realDistance = 0;
		count = 0;
		markerList = new Array();
		permList = new Array();
		if (routeLayer != null) {
			map.removeLayer(routeLayer);
		}
		markerLayer.clearMarkers();
		iconLayer.clearMarkers();
		highlightLayer.clearMarkers();
		start = new structMinfo();
		destination = new structMinfo();
		name = "";
		realDistance = 0;
		realForm = null;

		document.getElementById("name").innerHTML = "";

		totalDistance = 0;

		var stopList = document.getElementById("middleListBox");
		var stopnum = stopList.length;
		for (var i = 0; i < stopnum; i++) {
			stopList.remove(0);
		}

		var startList = document.getElementById("startListBox");
		startList.remove(0);

		var endList = document.getElementById("endListBox");
		endList.remove(0);

		map.setCenter(new Tmap.LonLat(14081257.293714, 3947485.458282), 10);
		tdata = null;
	} else {
		alert("Noooooo");
		return;
	}
}
