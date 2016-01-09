<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
	<title>Rules</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="stylesheet" type="text/css" href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" />
	<!-- JQuery -->
	<script type="text/javascript" src="/webjars/jquery/2.1.4/jquery.min.js"></script>
	<script type="text/javascript" src="/js/stomp.js"></script>
	<script type="text/javascript" src="/js/sockjs-0.3.4.js"></script>
	<!-- Bootstrap -->
	<script type="text/javascript" src="/webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
<body>
	<div class="container">
		<div class="row">
			<div class="col-lg-offset-4 col-sm-offset-4 col-lg-4 col-md-4 col-sm-12 col-xs-12">
				<form action="/${url_id}/update_rules" method="POST">
					<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12" style="text-align: center">
						<button class="btn btn-lg btn-primary" type="submit">Update Rules!</button>
					</div>
					<c:forEach items="${reglas}" var="entry" varStatus="loop">
						<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
							<div id="rule_${entry.key}_container">
								<p class="lead">${url_id}/${entry.key}</p>
								<div class="row">
									<div class="col-lg-10 col-md-10 col-sm-10 col-xs-10">
										<textarea id="rule_${entry.key}" rows="6" cols="30" name="rule_${entry.key}">${entry.value}</textarea>
									</div>
									<div class="col-lg-2 col-md-2 col-sm-2 col-xs-2">
										<input type="checkbox" name="delete_${entry.key}"> Eliminar
									</div>
								</div>
							</div>
						</div>
					</c:forEach>
				</form>
			</div>
		</div>
		<div class="row" style="text-align: center">
			<button type='button' id="add" class='btn btn-default'><span class='glyphicon glyphicon-plus'></span></button>
		</div>
	</div>
</body>
</html>
