<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Redirecting...</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="stylesheet" type="text/css" href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" />
		<script type="text/javascript" src="/webjars/jquery/2.1.4/jquery.min.js"></script>
		<script type="text/javascript" src="/webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				var countDownSelector = $('#countDown').children().first();
				var countDown = ${countDown};
				var interv = setInterval(function(){
					countDown-=1;
					countDownSelector.text("" + countDown);
					if (countDown == 0) {
						clearInterval(interv);
						window.location='${targetURL}';
					}
				}, 1000);
			});
		</script>
	</head>
	<body>
		<div class="container-full">
			<c:if test="${enableAds}">
			<div id="header_container">
				<img src="${advertisement}" alt="Ad" style="width: 100%; max-width: 100%; max-height: 150px">
			</div>
			<br/>
			</c:if>
			<div class="row">
				<div class="col-lg-12 text-center" style="font-size: 250%">
					Redirecting in <span id="countDown"><strong>${countDown}</strong></span>...
				</div>
			</div>
		</div>
	</body>
</html>