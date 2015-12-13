$(document).ready(function(){
	var countDown = 10;
	var countDownSelector = $('#countDown');
	countDownSelector.text("" + countDown);
	setInterval(function(){
		countDown-=1;
		if (countDownSelector != 0) countDownSelector.text("" + countDown);
		else window.location='URL';
	}, 1000);
});