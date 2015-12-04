$(document).ready(
    function() {
        $("#shortener").submit(
            function(event) {
                event.preventDefault();
                $.ajax({
                    type : "POST",
                    url : "/link",
                    data : $(this).serialize(),
                    success : function(msg) {
                        $("#result").html(
                            "<div class='alert alert-success lead'><a target='_blank' href='"
                            + msg.uri
                            + "'>"
                            + msg.uri
                            + "</a></div>");
                    },
                    error : function(jqXHR) {
                      var obj = jQuery.parseJSON( jqXHR.responseText );
                      $("#result").html( "<div class='alert alert-danger lead'>" +  obj.message + "</div>");
                    }
                });
            });
    });

    function personalizar(){
      if(document.getElementById("checkPersonalizar").checked){
        document.getElementById("divPersonalizar").style.visibility = "visible";
      }
      else{
        document.getElementById("divPersonalizar").style.visibility = "hidden";
        document.getElementById("urlPerson").value="";
      }
    }
