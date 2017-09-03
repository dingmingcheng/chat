<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> 
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Insert title here</title>
<script src="js/jquery-1.11.0.min.js" type="text/javascript"></script>
<script src="js/jquery-1.8.2.min.js" type="text/javascript"></script> 
<link href="css/smohan.face.css" type="text/css" rel="stylesheet">
<script type="text/javascript" src="js/smohan.face.js" charset="utf-8"></script>
<link rel="stylesheet" href="css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="css/normalize.css">
<link rel="stylesheet" type="text/css" href="css/zzsc-demo.css">
<link rel="stylesheet" href="css/dice-menu.min.css">
<link rel="stylesheet" href="css/style.css" media="screen" type="text/css">
</head>
<body>
<%
	Cookie[] x = request.getCookies();
	String id = x[0].getValue();
%>
<div>
<script type="text/javascript">
$(function (){
	$("a.face").smohanfacebox({
		Event : "click",	//触发事件	
		divid : "Smohan_FaceBox", //外层DIV ID
		textid : "Smohan_text" //文本框 ID
	});
	//解析表情  $('#Zones').replaceface($('#Zones').html());
});

</script>
<script type="text/javascript">
	var socket;
	var isFirst = 0;
	var requestId;
	if (!window.WebSocket) {
		window.WebSocket = window.MozWebSocket;
	}
	if (window.WebSocket) {
		requestId = "<%=id %>";
		socket = new WebSocket("ws://192.168.0.104:9090/websocket");
		socket.onmessage = function(event) {
			var name, message;
			var e=event.data;
			//
			var jsonobj = eval("("+e+")");
			//json todo					
			//
			if(jsonobj.flag = 0) {
				appendMessage(jsonobj.piccode,jsonobj.message);
			}
			else if(jsonobj.flag = 1) {
				$('#allthepeople').html("");
				var t = jsonobj.peo;
				for(var i in jsonobj.peo) {
					alert(jsonobj.peo[i]);
					appendMessage1(jsonobj.peo[i]);
				}
			}
			//ta.value = event.data;
		};
		socket.onopen = function(event) {
			
			appendMessage("System", "打开websocket正常,已连接服务器");
			send("none", "0");
			//console.log("已连接服务器");
		};
		socket.onclose = function(event) { // WebSocket 关闭
			appendMessage("System", "close");
			//Todo
			/*
			注销用户
			*/
			//console.log("WebSocket已经关闭!");
		};
	} 
	else {
		alert("抱歉，您的浏览器不支持WebSocket协议!");
	}
	
	function appendMessage(name, message) {
	/*var style = document.createElement("style");
	var text = document.createTextNode("ul li:before{content:"*";}");
	style.appendChild(text);
	document.body.appendChild(style);*/
		$("#responseText").append("<li>"+name+" : "+message +"</li>");
	}
	
	function appendMessage1(name) {
		alert("adas");
		$("#allthepeople").append("<li style=\"margin:10px 0;list-style-type:none;\">"+name+"</li>");
	}
	
	function send(message, statuscode) { // statuscode,0:初次请求，注册入map, 1:发送消息
		//alert(JSON.stringify({"requestId":id, "message":message}));
		message=JSON.stringify({"requestId":requestId, "message":message, "statuscode":statuscode});
		if (!window.WebSocket) { return; }
		if (socket.readyState == WebSocket.OPEN) {
			socket.send(message);
		} else {
			alert("您还未连接上服务器，请刷新页面重试");
		}
	}
	/*
	$("#Smohan_Showface').click(function() {
		alert("asdasfasdfa");
		var x=$("#Smohan_text").val();
		send(x, "1");
	});
	*/
	function fc1() {
		var x=$("#Smohan_text").val();
		send(x, "1");
	}
</script>
<ul style="float:left; height:300px;margin:50px;overflow-y: scroll;" id="allthepeople">
	<li style="margin:10px 0;list-style-type:none;">sdfslkdfjsldfj</li>
	<li style="margin:10px 0;list-style-type:none;">asdasdas</li>
</ul>

<ul class="chat-thread" id="responseText">
	<li>欢迎来到群聊1</li>
</ul>

</div>
<!-- Dice Menu -->
<ul class="dice-menu">
	<li><span class="fa fa-circle-o-notch"></span></li>
	<li style="opacity: 0.8;"><span class="fa fa-comment-o"></span></li>
	<li style="opacity: 0.8;"><span class="fa fa-envelope"></span></li>
	<li style="opacity: 0.8;"><span class="fa fa-paw" ></span></li>
	<li style="opacity: 0.8;"><span >12</span></li>
	<li style="opacity: 0.8;"><span >34</span></li>
</ul>

<div id="Smohan_FaceBox">
   <textarea name="text" id="Smohan_text" class="smohan_text" spellcheck="false";></textarea>
   <p>
   <a href="javascript:void(0)" class="face" title="表情"></a>
   <button class="button" id="Smohan_Showface" onclick="fc1();">发送</button>
   </p>
</div>




<script src="js/jquery.easing.min.js"></script>
<script src="js/dice-menu.min.js"></script>

</body></html>