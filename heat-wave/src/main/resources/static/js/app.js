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
                    error : function(request) {
                      $("#result").html( "<div class='alert alert-danger lead'>Url a acorta errónea</div>");
                      if(request.getResponseHeader('Personalizada')){
                        $("#result").html(
                                "<div class='alert alert-danger lead'>"+request.getResponseHeader('Personalizada')+" </div>");
                        }
                    }
                });
            });
    });
    
    function personalizar(){
      if(document.getElementById("checkPersonalizar").checked){
        document.getElementById("divPersonalizar").style.visibility = "visible";
s      }
      else{
        document.getElementById("divPersonalizar").style.visibility = "hidden";
        document.getElementById("urlPerson").value="";
      }
    }
