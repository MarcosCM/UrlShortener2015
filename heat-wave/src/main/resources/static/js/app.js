    $(document).ready(
        function() {
            $("#shortener").submit(
                function(event) {
                    event.preventDefault();
                    $.ajax({
                        type : "POST",
                        url : "/link",
                        data : $(this).serialize(),
                        success : function(msg,status,jqXHR) {
                          console.log(jqXHR);
                            $("#result").html(
                                "<div class='alert alert-success lead'><a id='link' target='_blank' href='"
                                + msg.uri
                                + "'>"
                                + msg.uri
                                + "</a></div>");
                                document.getElementById("sugerencias").style.display = "none";
                                document.getElementById("sugerencia").style.display = "none";
                        },
                        error : function(jqXHR) {
                          console.log(jqXHR);
                          var obj = jQuery.parseJSON( jqXHR.responseText );
                          var mensaje=obj.message;
                          var atributos= mensaje.split(":");
                          if(atributos.length>1){
                            $("#result").html( "<div class='alert alert-danger lead'>" +  atributos[0] + "</div>");
                            var sugerencias=1;
                            $("#sugerencia").html( "<h4>Sugerencias, elige la que más te guste:<h4> </br>");
                            var sugerenciasBotones="";
                            while(sugerencias<atributos.length){
                              sugerenciasBotones+="<button id='"+atributos[sugerencias]+"' onclick='elegirSugerencia(this.id)' type='button' class='btn btn-default'>"+atributos[sugerencias]+" </button>";
                              sugerencias=sugerencias+1;
                            }
                            $("#sugerencias").html( sugerenciasBotones);

                            document.getElementById("sugerencias").style.display = "inline";
                            document.getElementById("sugerencia").style.display = "inline";
                          }
                          else{
                            $("#result").html( "<div class='alert alert-danger lead'>" +  atributos[0] + "</div>");
                            document.getElementById("sugerencias").style.display = "none";
                            document.getElementById("sugerencia").style.display = "none";
                          }
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

      function elegirSugerencia (id) {
        document.getElementById("urlPerson").value=id;

}
