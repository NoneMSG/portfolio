//화면상 리스트 박스의 기능들을 담당하는 스크립트 함수들


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
//출발지 리스트박스 기능
function showListBoxStart() {
	var list = document.getElementById("startListBox");
	var option = document.createElement("option");

	if (list[0] != null) {
		list.remove(0);
	}

	option.text = start.name;
	option.value = 0;

	list.add(option);
}
//경유지 리스트박스 기능
function showListBox() {
	var list = document.getElementById("middleListBox");
	for (var i = mList.length - 1; i < mList.length; ++i) {// 지역변수로 할당.
		var option = document.createElement("option");

		option.text = mList[i].name;
		option.value = i + 1;

		list.add(option);
	}
}
//목적지 리스트박스 기능
function showListBoxDestination() {
	var list = document.getElementById("endListBox");
	var option = document.createElement("option");
	if (list[0] != null) {
		list.remove(0);
	}

	option.text = destination.name;
	option.value = 0;

	list.add(option);
}
//경유지리스트박스의 데이터를 지우는 기능 부분
function showListBox1() {
	var list = document.getElementById("middleListBox");
	console.log(list[0]);
	for (var i = 0; i < list.length; i++) {
		if (name == list[i].text) {
			list.remove(i);
		}
	}
}

//경유지 리스트박스의 원소를 이동시키는 함수
function MoveUp(comboname)// 하나의 원소만 바꿈
{
	console.log(comboname);
	var combo = document.getElementById(comboname);
	console.log(combo);
	var i = combo.selectedIndex;
	console.log(i);
	if (i > 0) {
		swapListData(combo, i, i - 1);
		combo.options[i - 1].selected = true;

		combo.options[i].selected = false;
	}
}
//경유지 리스트박스의 원소를 이동시키는 함수
function MoveDown(comboname)// 아래 하나의 원소만 바꿈
{
	var combo = document.getElementById("middleListBox");
	i = combo.selectedIndex;
	if (i < combo.length - 1 && i > -1) {

		swapListData(combo, i + 1, i);
		combo.options[i + 1].selected = true;
		combo.options[i].selected = false;
	}
}

//this function is used to swap between elements
//원소간 데이터를 교환하는 기능 함수
function swapListData(combo, index1, index2) {

	var savedValue = combo.options[index1].value;
	var savedText = combo.options[index1].text;
	var savedData = mList[index1];
	combo.options[index1].value = combo.options[index2].value;
	combo.options[index1].text = combo.options[index2].text;
	mList[index1] = mList[index2];

	combo.options[index2].value = savedValue;
	combo.options[index2].text = savedText;
	mList[index2] = savedData;
}
