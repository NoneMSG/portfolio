
//사용자정의 길찾기 기능
function customRoute() {
	if (start.lon == null || destination.lon == null) {
		alert("정보가 입력되지 않았습니다.");
		return;
	}

	var customList = new Array();
	customList.push(new structMinfo);

	customList[0] = start;
	for (var i = 0; i < mList.length; i++) {
		customList[i + 1] = mList[i];
	}
	customList[customList.length] = destination;

	tdata = new Tmap.TData();

	var option = {
		version : "1",
		format : 'xml'
	};

	var style = new Tmap.Style({
		strokeColor : "#FF0000",
		strokeWidth : 5,
		strokeOpacity : 1,
		graphicZIndex : 1
	});

	var v_option = {
		renderers : ['Canvas', 'SVG', 'VML'],
		styleMap : style
	};

	routeLayer = new Tmap.Layer.Vector("routeLayer", v_option);
	map.removeLayer(markerLayer);
	var groupLayer = new Array();
	groupLayer.push(routeLayer);
	groupLayer.push(markerLayer);
	map.addLayers(groupLayer);

	for (var i = 0; i < customList.length - 1; i++) {
		var startPoint = new Tmap.LonLat(customList[i].lon, customList[i].lat);
		var endPoint = new Tmap.LonLat(customList[i + 1].lon, customList[i + 1].lat);
		tdata.getRoutePlan(startPoint, endPoint, option);
	}

	tdata.events.register("onComplete", tdata, customXml);
	innerhtml(customList);
}
//사용자정의 길찾기 데이터를 불러와서 맵에 올리는 기능
function customXml() {
	var form = new Tmap.Format.KML().read(this.responseXML);

	var totalDistance = 0;

	for ( i = 0; i < form.length - 1; ++i) {

		if (form[i].data.distance != null) {
			totalDistance += parseInt(form[i].data.distance);
		}
	}
	realDistance = totalDistance;
	for ( i = 0; i <= form.length - 1; i++) {
		routeLayer.addFeatures([form[i]]);
	}

	var sOption = {
		lonlat : new Tmap.LonLat(start.lon, start.lat)
	};
	var gOption = {
		lonlat : new Tmap.LonLat(destination.lon, destination.lat)
	};

	var size = new Tmap.Size(30, 34);
	var offset = new Tmap.Pixel(-(size.w / 2), -size.h);
	var simage = "./icons/tstart.png";
	var gimage = "./icons/arrival.png";
	var sicon = new Tmap.Icon(simage, size, offset);
	var gicon = new Tmap.Icon(gimage, size, offset);
	routeaddMarker(sOption, sicon);
	routeaddMarker(gOption, gicon);

	for ( i = 1; i < mList.length + 1; ++i) {
		var options = {
			lonlat : new Tmap.LonLat(mList[i - 1].lon, mList[i - 1].lat)
		};
		var size = new Tmap.Size(30, 34);
		var offset = new Tmap.Pixel(-(size.w / 2), -size.h);
		var image = "./icons/" + i + ".png";
		var icon = new Tmap.Icon(image, size, offset);
		routeaddMarker(options, icon);
	}

}

//사용자가 길찾기 버튼을 누르면 실행되는 함수
//조건별로 맞는 기능을 수행한다.
function makeroute() {

	if (start.lon == null || destination.lon == null) {
		alert("정보가 입력되지 않았습니다.");
		return;
	}

	if ((start.lon == destination.lon) || (start.lat == destination.lat)) {
		destination.lon = (start.lon + 0.1).toFixed(6);
		destination.lat = (start.lat + 0.1).toFixed(6);
	}

	if (mList.length == 0) {
		betweenStartGoal();
		//between start and goal
	} else {
		var tempList = new Array();

		for ( i = 0; i < mList.length; ++i) {
			tempList.push(i);
		}
		heapsPermute(tempList, print);

		setTimeout('route()', 1000);
	}
}
//경유지 없이 길찾기를 하게 될경우
function betweenStartGoal() {
	var route = new Tmap.TData();
	var option = {
		version : "1",
		format : 'xml'
	};

	var style = new Tmap.Style({
		strokeColor : "#FF0000",
		strokeWidth : 5,
		strokeOpacity : 1,
		graphicZIndex : 1
	});
	//마커 스타일에 대한 정의
	var v_option = {
		renderers : ['Canvas', 'SVG', 'VML'],
		styleMap : style
	};
	//마커에 스타일 입히기고 지도에 그리기 위해 벡터사용
	var sPoint = new Tmap.LonLat(start.lon, start.lat);

	var ePoint = new Tmap.LonLat(destination.lon, destination.lat);

	route.getRoutePlan(sPoint, ePoint, option);

	route.events.register("onComplete", route, from_btSG);

	routeLayer = new Tmap.Layer.Vector("routeLayer", v_option);

	////////////////////////////////////////
	map.removeLayer(markerLayer);
	var groupLayer = new Array();
	groupLayer.push(routeLayer);
	groupLayer.push(markerLayer);
	map.addLayers(groupLayer);
	///////////////////////////////////////

	innerhtmlforshorterone(mList);

	routeMarkersOnTheMap();
	//경로 순서 및 출발 도착지 마커 그림 정의
}
//지도에 경로의 순서를 알려주기 위한 아이콘 입력 기능
function routeMarkersOnTheMap() {

	var sOption = {
		lonlat : new Tmap.LonLat(start.lon, start.lat)
	};
	var gOption = {
		lonlat : new Tmap.LonLat(destination.lon, destination.lat)
	};

	var size = new Tmap.Size(30, 34);
	var offset = new Tmap.Pixel(-(size.w / 2), -size.h);
	var simage = "./icons/tstart.png";
	var gimage = "./icons/arrival.png";
	var sicon = new Tmap.Icon(simage, size, offset);
	var gicon = new Tmap.Icon(gimage, size, offset);
	routeaddMarker(sOption, sicon);
	routeaddMarker(gOption, gicon);

}
//출발지 목적지만 있는경우 지도에 데이터를 그려주는 부분
function from_btSG() {
	var form = new Tmap.Format.KML().read(this.responseXML);

	for ( i = 0; i <= form.length - 1; i++) {
		routeLayer.addFeatures([form[i]]);
	}
}

//최단거리를 순차적으로 나열하기 위한 스왑 합수
var swap = function(array, pos1, pos2) {
	var temp = array[pos1];
	array[pos1] = array[pos2];
	array[pos2] = temp;
};
// 순열을 구하는 함수
var heapsPermute = function(array, output, n) {
   n = n || array.length;
   // set n default to array.length
   if (n === 1) {
      output(array);
   } else {
      for (var i = 1; i <= n; i += 1) {
         heapsPermute(array, output, n - 1);
         if (n % 2) {
            var j = 1;
         } else {
            var j = i;
         }
         swap(array, j - 1, n - 1);
         // -1 to account for javascript zero-indexing
      }
   }
};

// log에 순열을 보여주고 순열 배열을 만듬
var print = function(input) {

   console.log(input);

   var plist = new Array();

   for ( i = 0; i < input.length; ++i) {
      plist.push(input[i]);
   }

   permList.push(plist);
};

//만들어진 순열 배열을 사용하여 길찾기 실행
function route() {
   for ( k = 0; k < permList.length; ++k) {
      var list = permList[k];
      var permRealList = new Array();

      for ( i = 0; i < list.length; ++i) {
         var li = mList[list[i]];
         permRealList.push(li);
      }
      permRealList.unshift(start);
      permRealList.push(destination);
      var tdata = new Tmap.TData();
      tdata.events.register("onComplete", tdata, onLoadPerm);

      var startLonLat = new Tmap.LonLat(permRealList[0].lon, permRealList[0].lat);
      //목적지마커
      var endLonLat = new Tmap.LonLat(permRealList[permRealList.length - 1].lon, permRealList[permRealList.length - 1].lat);
      var pass = "";

      //정렬된 마커들을 경유지로 순차적으로 추가 하기 위한 for문
      for ( i = 1; i < permRealList.length - 1; ++i) {
         pass += permRealList[i].lon;
         pass += ",";
         pass += permRealList[i].lat;
         if (i != permRealList.length - 2) {
            pass += "_";
         }
      }
      //최종적으로 정렬된 마커의 좌표값들로 길찾기 함수 수행
      tdata.getRoutePlan(startLonLat, endLonLat, {
         version : "1",
         format : 'xml',
         passList : pass
      });
   }

   setTimeout('Draw()', 1000);
}

// 길과 마커를 표시해주는 함수
function Draw() {
   var style = new Tmap.Style({
      strokeColor : "#FF0000",
      strokeWidth : 5,
      strokeOpacity : 1,
      graphicZIndex : 1,
   });
   //마커 스타일에 대한 정의
   var v_option = {
      renderers : ['Canvas', 'SVG', 'VML'],
      styleMap : style
   };
   //마커에 스타일 입히기고 지도에 그리기 위해 벡터사용
   routeLayer = new Tmap.Layer.Vector("routeLayer", v_option);
   map.removeLayer(markerLayer);
   var groupLayer = new Array();
   groupLayer.push(routeLayer);
   groupLayer.push(markerLayer);
   map.addLayers(groupLayer);
   routeLayer.events.register("featuresadded", routeLayer, onDrawnFeatures);

   var form = realForm;
   var totalDistance = 0;

   for ( i = 0; i <= form.length - 1; i++) {
      routeLayer.addFeatures([form[i]]);
      if (form[i].data.distance != null) {
         var dis = parseInt(form[i].data.distance);
         totalDistance += dis;
      }
   }

   for ( i = 0; i < routeDataList.length; ++i) {
      var options = {
         lonlat : new Tmap.LonLat(routeDataList[i].lon, routeDataList[i].lat)

      };
      var size = new Tmap.Size(30, 34);
      var offset = new Tmap.Pixel(-(size.w / 2), -size.h);
      if (i == 0) {
         var image = "./icons/tstart.png";
      } else if (i == routeDataList.length - 1) {
         var image = "./icons/arrival.png";
      } else {
         var image = "./icons/" + i + ".png";

      }

      var icon = new Tmap.Icon(image, size, offset);
      routeaddMarker(options, icon);
   }
   innerhtml(routeDataList);

}

// 길찾기를 실행한 후 마커를 화면에 추가
function routeaddMarker(options, icon) {
   var marker = new Tmap.Markers(options.lonlat, icon);
   iconLayer.addMarker(marker);
}

//순열 중에 가장 가까운 길을 찾는 함수
function onLoadPerm() {
   var str = this.responseXML.URL;
   var arr = new Array();
   var strArr = str.split('&');
   var endX = parseFloat(strArr[3].substr(5));
   var endY = parseFloat(strArr[4].substr(5));
   var startX = parseFloat(strArr[5].substr(7));
   var startY = parseFloat(strArr[6].substr(7));
   var pass = strArr[7].substr(9);
   var passArr = pass.split('_');

   for ( i = 0; i < passArr.length; ++i) {
      var pass = passArr[i].split(',');
      arr.push(new structMinfo());
      arr[i].lon = parseFloat(pass[0]);
      arr[i].lat = parseFloat(pass[1]);
   }

   arr.unshift(new structMinfo());
   arr[0].lon = startX;
   arr[0].lat = startY;
   arr.push(new structMinfo());
   arr[arr.length - 1].lon = endX;
   arr[arr.length - 1].lat = endY;

   for ( i = 1; i < arr.length - 1; ++i) {
      for ( j = 0; j < mList.length; ++j) {
         if (arr[i].lon == mList[j].lon && arr[i].lat == mList[j].lat) {
            arr[i].name = mList[j].name;
         } else if (arr[i].lon + 0.1 == mList[j].lon && arr[i].lat + 0.1 == mList[j].lat) {
            arr[i].name = mList[j].name;
         }
      }
   }

   arr[0].name = start.name;
   arr[arr.length - 1].name = destination.name;

   var form = new Tmap.Format.KML().read(this.responseXML);
   var totalDistance = 0;

   for ( i = 0; i < form.length - 1; ++i) {
      if (form[i].data.distance != null) {
         totalDistance += parseInt(form[i].data.distance);
      }
   }
   if (totalDistance != null) {
      if (realDistance == 0) {
         realDistance = totalDistance;
         realForm = form;
         routeDataList = arr;
      } else if (realDistance > totalDistance) {
         realDistance = totalDistance;
         realForm = form;
         routeDataList = arr;
      }
   };
}

// 길찾기 난 후 줌
function onDrawnFeatures(e) {
   map.zoomToExtent(this.getDataExtent());
}

//화면상 출발지 경유지 목적지를 하단부에 텍스트로 보이게 하기 위한 기능
function innerhtml(markerlist) {
	var leng = markerlist.length - 1;

	for ( i = 0; i < markerlist.length; ++i) {
		if (i == leng) {
			var name = markerlist[i].name;
			document.getElementById("name").innerHTML += name;
		} else if ((i % 2) == 0) {
			var name = markerlist[i].name;
			document.getElementById("name").innerHTML += name += " -> ";
		} else {
			var name = markerlist[i].name;
			document.getElementById("name").innerHTML += name += " -> ";
		}
	}
}
//화면상 출발지 경유지 목적지를 하단부에 텍스트로 보이게 하기 위한 기능 (출발지와 목적지만 있는경우)
function innerhtmlforshorterone() {
	for ( i = 0; i < 2; ++i) {
		if (i == 1) {
			var name = destination.name;
			document.getElementById("name").innerHTML += name;
		} else {
			var name = start.name;
			document.getElementById("name").innerHTML += name += " -> ";
		}
	}
}
