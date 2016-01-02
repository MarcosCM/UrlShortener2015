function clickMapFunction(){
	emit("country", this.hasOwnProperty("country") ? this["country"] : "unknown");
	emit("browser", this.hasOwnProperty("browser") ? this["browser"] : "unknown");
	emit("platform", this.hasOwnProperty("platform") ? this["platform"] : "unknown");
}