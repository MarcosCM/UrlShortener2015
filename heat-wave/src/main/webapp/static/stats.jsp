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
	</script>
</head>
<body>
	<c:forEach items="${detailedStats.getCharts()}" var="entry" varStatus="loop">
	<div id="chart_${entry.key}"></div>
	</c:forEach>
</body>
</html>
