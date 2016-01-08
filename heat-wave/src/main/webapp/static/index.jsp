<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
	<title>URL Shortener</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="stylesheet" type="text/css" href="webjars/bootstrap/3.3.5/css/bootstrap.min.css" />
	<script type="text/javascript" src="webjars/jquery/2.1.4/jquery.min.js"></script>
	<script type="text/javascript" src="webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="js/app.js"></script>
</head>
<body>
	<div class="container-full">
		<div class="row">
			<div class="col-lg-12 text-center">
				<h1>URL Shortener</h1>
				<p class="lead">Feel free to shorten your URL!</p>
				<br>
				<c:choose>
					<c:when test="${(authThrough != null) && (authAs != null) && (authAs != 'anonymousUser')}">
						<c:choose>
							<c:when test="${authThrough == 'local'}"><div class="center-block">Logged as ${authAs}</div></c:when>
							<c:when test="${authThrough == 'twitter'}"><div class="center-block">Logged through <span class="bg-info">Twitter</span> as ${authAs}</div></c:when>
							<c:when test="${authThrough == 'facebook'}"><div class="center-block">Logged through <span class="bg-primary">Facebook</span> as ${authAs}</div></c:when>
							<c:when test="${authThrough == 'google'}"><div class="center-block">Logged through <span class="bg-danger">Google</span> as ${authAs}</div></c:when>
							<c:otherwise></c:otherwise>
						</c:choose>
					</c:when>
					<c:otherwise>
						<div class="row">
							<div class="col-sm-offset-4 col-lg-2 col-md-2 col-sm-12 col-xs-12">
								<div class="col-lg-12">
									<p class="lead">Register form</p>
									<form role="form" id="register" action="/user/register" method="POST">
										<div class="input-group input-group-lg col-sm-12">
											<input type="text" class="center-block form-control input-lg" title="Enter a username" placeholder="Enter a username" id="username" name="username">
										</div>
										<br>
										<div class="input-group input-group-lg col-sm-12">
											<input type="password" class="center-block form-control input-lg" title="Enter a password" placeholder="Enter a password" id="password" name="password">
										</div>
										<br>
										<div class="input-group input-group-lg col-sm-12">
											<input type="submit" class="btn" value="Register!">
										</div>
									</form>
								</div>
							</div>
							<div class="col-lg-2 col-md-2 col-sm-12 col-xs-12">
								<div class="col-lg-12">
									<p class="lead">Login form</p>
									<form role="form" id="login" action="/user/login" method="POST">
										<div class="input-group input-group-lg col-sm-12">
											<input type="text" class="center-block form-control input-lg" title="Enter a username" placeholder="Enter a username" id="username" name="username">
										</div>
										<br>
										<div class="input-group input-group-lg col-sm-12">
											<input type="password" class="center-block form-control input-lg" title="Enter a password" placeholder="Enter a password" id="password" name="password">
										</div>
										<br>
										<div class="input-group input-group-lg col-sm-12">
											<input type="submit" class="btn" value="Login!">
										</div>
									</form>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-sm-offset-4 col-xs-12 col-sm-4 col-md-4 col-lg-4">
								<p class="lead text-center">Social Login</p>
							</div>
							<div class="col-sm-offset-4 col-xs-12 col-sm-4 col-md-4 col-lg-4">
						        <div class="col-xs-12 col-sm-4 col-md-4 col-lg-4">
						        	<form id="googleLogin" action="/connect/google" method="POST">
						        		<input type="hidden" name="scope" value="email">
						        		<button type="submit" id="googleLoginRedirect" class="btn btn-lg btn-danger btn-block">Google</button>
						        	</form>
						        </div>
						        <div class="col-xs-12 col-sm-4 col-md-4 col-lg-4">
						        	<form id="twitterLogin" action="/connect/twitter" method="POST">
						        		<input type="hidden" name="scope" value="email">
						        		<button type="submit" id="twitterLoginRedirect" class="btn btn-lg btn-info btn-block">Twitter</button>
						        	</form>
						        </div>
						        <div class="col-xs-12 col-sm-4 col-md-4 col-lg-4">
						        	<form id="facebookLogin" action="/connect/facebook" method="POST">
						        		<input type="hidden" name="scope" value="email">
						        		<button type="submit" id="facebookLoginRedirect" class="btn btn-lg btn-primary btn-block">Facebook</button>
						        	</form>
						        </div>
					        </div>
				    	</div>
					</c:otherwise>
				</c:choose>
				<div class="row col-lg-12 col-md-12 col-sm-12 col-xs-12"><hr></div>
				<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
					<form class="col-lg-12" role="form" id="shortener" action="">
						<div class="input-group input-group-lg col-sm-offset-4 col-sm-4">
							<input type="text" class="center-block form-control input-lg" title="Enter a URL" placeholder="Enter a URL" id="url" name="url">
							<span class="input-group-btn"><button class="btn btn-lg btn-primary" type="submit">Short me!</button></span>
						</div>
						<br>
						<div class="input-group input-group-lg col-sm-offset-4 col-sm-4">
							<div class="input-group">
						 		<span class="input-group-addon">
						 			<input type="checkbox" aria-label="..." name="enableAd" id="enableAd">
						 		</span>
						 		<input readonly type="text" class="form-control" value="The shortened URL will show an advertisement">
						 	</div>
					  	</div>
					  	<div id="authUsers" class="input-group input-group-lg col-sm-offset-4 col-sm-4"></div>
						<br>
						<div class="input-group input-group-lg col-sm-offset-4 col-sm-4">
							<div class="input-group">
								<span class="input-group-addon">
									<input id="checkPersonalizar" onclick="personalizar()" type="checkbox" aria-label="...">
								</span>
								<input readonly type="text" class="form-control" value="I want a custom URL tag">
							</div>
						</div>
						<br>
						<div id="divPersonalizar" style="visibility: hidden;" class="input-group input-group-lg col-sm-offset-4 col-sm-4">
							<div class="input-group">
								<span id="divPersonalizar" class="input-group-addon" id="basic-addon3">http://shorturl.es/</span>
								<input type="text" name="customTag" class="form-control" id="urlPerson" onkeyup="comprobarSugerencias(this)" aria-describedby="basic-addon3">
							</div>
							<br>
							<div id="sugerencia"></div>
							<div id="sugerencias" class="btn-group" role="group" /></div>
						</div>
					</form>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-lg-12 text-center">
				<div class="col-sm-offset-4 col-sm-4 text-center">
					<br>
					<div id="result"></div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
