  $(document).ready(function() {
  	// Thread for servlet called every two seconds
  	setInterval(function(){

  		$.ajax({
  					type : "POST",
  					url : "data_updater",
  					dataType : "html",
  					async: true,
  					success : function(msg) {
  						var msg = eval(msg);
  						var temperature = msg[0];
  						var humidity = msg[1];
  						var gas = msg[2];
  						var light = msg[3];
  						var infrared = msg[4];
  						$('#temperature').text(temperature);	
  						$('#gas').text(gas);	
  						$('#humidity').text(humidity);	
  						$('#light').text(light);
  						$('#infrared').text(infrared);	
  					},
  					error : function(err) {
  						console.log("error: "+err)
  					}
  				});
  	},2000);
  	
  	$('#switcher').change(function() {
  		alert("uas");
        if(this.checked) {
        	$.ajax({
					type : "GET",
					url : "http://192.168.43.30/POWERMODEON",
					dataType : "html",
					async: true,
					success : function(msg) {
						console.log(msg);
					},
					error : function(err) {
						console.log("error: "+err)
					}
				});
        }else{
        	$.ajax({
				type : "GET",
				url : "http://192.168.43.30/POWERMODEOFF",
				dataType : "html",
				async: true,
				success : function(msg) {
					console.log(msg);
				},
				error : function(err) {
					console.log("error: "+err)
				}
			});
        }
    });

});