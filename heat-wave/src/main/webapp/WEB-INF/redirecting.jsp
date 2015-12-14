<!DOCTYPE html>
<html>
	<head>
		<title>Redirigiendo...</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<!-- If the browser disabled JS -->
		<noscript><meta http-equiv="Refresh" content="${countDown};url=${targetURL}" /></noscript>
		<link rel="stylesheet" type="text/css"
			href="webjars/bootstrap/3.3.5/css/bootstrap.min.css" />
		<script type="text/javascript" src="webjars/jquery/2.1.4/jquery.min.js"></script>
		<script type="text/javascript" src="webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
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
			<br/>
			<div class="row">
				<div class="col-lg-12 text-center">
					Redirigiendo en <span id="countDown"><strong>${countDown}</strong><span>...
				</div>
			</div>
		</div>
	</body>
</html>