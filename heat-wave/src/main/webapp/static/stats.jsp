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
		google.setOnLoadCallback(drawCharts);

		// Charts params
		var charts_data = [];
		var charts_options = [];
		var charts_chart = [];
		// Initial charts data JSON
		var detailedStatsJSON = ${detailedStatsJSON}['charts'];
		function drawCharts(){
			for(var outer_key in detailedStatsJSON){
				(function(i){
					// Set data
					charts_data[i] = new google.visualization.DataTable();
					charts_data[i].addColumn('string', 'Name');
					charts_data[i].addColumn('number', 'Quantity');
					var rows = [];
					for(var inner_data_key in detailedStatsJSON[i]['data']){
						(function(j){
							rows.push([j, detailedStatsJSON[i]['data'][j]]);
						})(inner_data_key);
					}
					charts_data[i].addRows(rows);

					// Set options
					for(var inner_options_key in detailedStatsJSON[i]['options']){
						charts_options[i] = [];
						(function(j){
							charts_options[i][j] = detailedStatsJSON[i]['options'][j];
						})(inner_options_key);
					}

					// Set type
					switch(detailedStatsJSON[i]['type']){
						case "PieChart":
							charts_chart[i] = new google.visualization.PieChart(document.getElementById('chart_'+i));
							break;
						case "LineChart":
							charts_chart[i] = new google.visualization.LineChart(document.getElementById('chart_'+i));
							break;
						case "BarChart":
							charts_chart[i] = new google.visualization.BarChart(document.getElementById('chart_'+i));
							break;
						default:
							charts_chart[i] = new google.visualization.PieChart(document.getElementById('chart_'+i));
							break;
					}

					// Draw chart
					charts_chart[i].draw(charts_data[i], charts_options[i]);
				})(outer_key);
			}
		}

		var stompClient = null;
		var socket = null;
		$(document).on('ready', function(){
			socket = new SockJS('/statistics');
			stompClient = Stomp.over(socket);
			stompClient.debug = null;
			stompClient.connect({}, function(frame) {
				// Subscribe to the shortened URL socket
				var subscripcion = '/sockets/${hash}';
				stompClient.subscribe(subscripcion, function(stats){
					// Refresh charts on data receipt
					refreshCharts(JSON.parse(stats.body));
				});
		  	});

			var timer;
			function resetTimer(){
				if (timer != null) clearTimeout(timer);
				timer = setInterval(peticionFiltrada, 5000);
			}
			resetTimer();

		  	$("#filterButton").click(function(){
				resetTimer();
		  	});
		});

		function peticionFiltrada(){
			var from = $("#from").val();
			var to = $("#to").val();
			stompClient.send("/app/statistics", {}, JSON.stringify({ 'id' : '${hash}',
																	'from' : from,
																	'to' : to
																}));
		}

		function refreshCharts(updated_charts_data){
			for(var i in updated_charts_data){
				charts_data[i] = new google.visualization.DataTable();
				charts_data[i].addColumn('string', 'Name');
				charts_data[i].addColumn('number', 'Quantity');
				var rows = [];
				for(var inner_data_key in updated_charts_data[i]){
					(function(j){
						rows.push([j, updated_charts_data[i][j]]);
					})(inner_data_key);
				}
				charts_data[i].addRows(rows);
				charts_chart[i].draw(charts_data[i], charts_options[i]);
			}
		}
	</script>
</head>
<body>
	<c:forEach items="${detailedStats.getCharts()}" var="entry" varStatus="loop">
	<div id="chart_${entry.key}"></div>
	</c:forEach>

	<div class="row">
		<div class="input-group input-group-lg col-sm-offset-4 col-sm-4">
			<div class="input-group">
				<span id="fromLabel" class="input-group-addon">From date</span>
				<input type="date" class="form-control" id="from" name="from" placeholder="dd/mm/yyyy">
			</div>
		</div>
		<div class="input-group input-group-lg col-sm-offset-4 col-sm-4">
			<div class="input-group">
				<span id="fromLabel" class="input-group-addon">To date</span>
				<input type="date" class="form-control" id="to" name="to" placeholder="dd/mm/yyyy">
			</div>
		</div>
		<div class="input-group input-group-lg col-sm-offset-4 col-sm-4" style="text-align: center">
			<button id="filterButton" type="button" class="btn btn btn-primary">Filter</button>
		</div>
	</div>
</body>
</html>
