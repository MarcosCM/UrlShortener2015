<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
	<title>Stats</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="stylesheet" type="text/css" href="webjars/bootstrap/3.3.5/css/bootstrap.min.css" />
	<!-- Google Visualization API -->
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	<!-- JQuery -->
	<script type="text/javascript" src="webjars/jquery/2.1.4/jquery.min.js"></script>
	<script type="text/javascript" src="js/stomp.js"></script>
	<script type="text/javascript" src="js/sockjs-0.3.4.js"></script>
	<!-- Bootstrap -->
	<script type="text/javascript" src="webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
	<!-- Chart utils -->
	<script type="text/javascript">
		// Load the Visualization API and the piechart package.
		google.load('visualization', '1', {'packages':['corechart']});
		// Set a callback to run when the Google Visualization API is loaded.
		google.setOnLoadCallback(drawChart);

		function drawChart(){
			<c:forEach items="${detailedStats.getCharts()}" var="outerEntry" varStatus="outerLoop">
			var data_${outerEntry.key} = new google.visualization.DataTable();
			data_${outerEntry.key}.addColumn('string', 'Name');
			data_${outerEntry.key}.addColumn('number', 'Quantity');
			data_${outerEntry.key}.addRows([
				<c:forEach items="${outerEntry.value.getData()}" var="dataEntry" varStatus="dataLoop">
				['${dataEntry.key}', ${dataEntry.value}]<c:if test="${!dataLoop.last}">,</c:if>
				</c:forEach>
			]);

			var options_${outerEntry.key} = {
			<c:forEach items="${outerEntry.value.getOptions()}" var="optionsEntry" varStatus="optionsLoop">
				'${optionsEntry.key}': '${optionsEntry.value}'<c:if test="${!optionsLoop.last}">,</c:if>
			</c:forEach>
			};

			var chart_${outerEntry.key} = new google.visualization.${outerEntry.value.getType()}(document.getElementById('chart_${outerEntry.key}'));
			chart_${outerEntry.key}.draw(data_${outerEntry.key}, options_${outerEntry.key});
			</c:forEach>
		}

		var stompClient = null;
		function connect() {
			var socket = new SockJS('/stadistics');
			stompClient = Stomp.over(socket);
			stompClient.connect({}, function(frame) {
				console.log('Connected: ' + frame);
				//lo que haces aqui es subscribirte, los mensajes destinados a /sockets/urlID
				//se duplican y llegan automaticamente a todos los subscritos
				var urlActual= document.URL.split("/");
				var idActual=urlActual[3].substring(0, urlActual[3].length-1);
				var subscripcion='/sockets/'+idActual;
				stompClient.subscribe(subscripcion, function(stats){
						if ((document.getElementById("hasta").value.localeCompare('')==0 && document.getElementById("desde").value.localeCompare('')==0) ||
							(document.getElementById("hasta").value==null && document.getElementById("desde").value==null)){
							//si los valor desde y hasta no tienen filtros->
							console.log("no hay filtros puestos");
					    	var charts = JSON.parse(stats.body).charts;
							console.log(charts);
							drawOne(charts);
						}
				});
		  	});
		}

		function sendInformation() {
		  	//si se necesita eniar informacion por socket
			stompClient.send("/app/stadistics", {}, JSON.stringify({ 'name': 'esto es lo que se en manda en Json' }));
		}

		var timer;
		function timerPeticion(){
			if (timer != null){
				clearTimeout(timer);
			}
			timer = setInterval(peticionFiltrada, 5000);
		}

		function peticionFiltrada(){
			console.log("peticion");
			if(!((document.getElementById("hasta").value.localeCompare('')==0 && document.getElementById("desde").value.localeCompare('')==0) ||
				( document.getElementById("hasta").value==null && document.getElementById("desde").value==null))){
				console.log("hay algun filtro puesto");
				var urlActual = document.URL.split("/");
				var idActual = urlActual[3].substring(0, urlActual[3].length-1);
			    $.get("/stats/Filtradas", { id: idActual,
		    		desde: document.getElementById("desde").value, hasta: document.getElementById("hasta").value
		    	})
		       	.done(function(data) {
					console.log("ajax bien");
					drawOne(data.charts);
		      	})
		      	.fail(function(data){
			    	console.log("ajax mal");
		      	});
			}
			else{
				console.log("no hay filtros puestos");
			}
		}

		function drawOne(charts){
			for(var key in charts){
				console.log(key);
				console.log(charts.Browser.data);
				var chart = new google.visualization.PieChart(document.getElementById('chart_'+key));
				var data = new google.visualization.DataTable();
				data.addColumn('string', 'Name');
				data.addColumn('number', 'Quantity');
				var array = $.map(charts.Browser.data, function(value, index){
					return [value];
				});
				var i = 0;
				for(var element in charts.Browser.data){
					data.addRow([element, array[i]]);
					i = i+1;
				}
				chart.draw(data, charts.Browser.options);
			}
		}

	</script>
</head>
<body onload="connect()">
	<c:forEach items="${detailedStats.getCharts()}" var="entry" varStatus="loop">
	<div id="chart_${entry.key}"></div>
	</c:forEach>

	<form id="fechas" action="/stats/Filtradas" method="POST">
		<input type="date" name="desde" id="desde"  value="desde"></br>
		<input type="date" name="hasta" id="hasta" value="hasta"></br>
		<button type="button" class="btn btn btn-primary " onclick='timerPeticion()'>Filtrar</button>
	</form>
</body>
</html>
