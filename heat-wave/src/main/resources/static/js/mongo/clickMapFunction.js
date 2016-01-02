function clickMapFunction(){
	if (this.hasOwnProperty("date")){
		// round date to 5 minutes interval
		// 1000 milliseconds * 60 seconds * 5 minutes
		var rounded = 1000 * 60 * 5;
		var date = new Date(this["date"]);
		// use Math.floor or Math.trunc to truncate the date, as dates are always positive
		emit("date", new Date(Math.floor(date.getTime() / rounded) * rounded));
	}
	else emit("date", "unknown");
	emit("country", this.hasOwnProperty("country") ? this["country"] : "unknown");
	emit("browser", this.hasOwnProperty("browser") ? this["browser"] : "unknown");
	emit("platform", this.hasOwnProperty("platform") ? this["platform"] : "unknown");
}