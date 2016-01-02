function clickReduceFunction(key, value){
	var res = {};
	for(var i = 0; i < value.length; i++){
		if (res.hasOwnProperty(value[i])) res[value[i]] += 1;
		else res[value[i]] = 1;
	}
	return res;
}