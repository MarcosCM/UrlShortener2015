$(document).ready(function() {
      $("#shortener").submit(
          function(event) {
              if (timer != null) {
                clearTimeout(timer);
              }
              event.preventDefault();
              $.ajax({
                  type : "POST",
                  url : "/link",
                  data : $(this).serialize(),
                  success : function(msg,status,jqXHR) {
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

//onchange="setTimeout(comprobarTiempo, 1500)
function elegirSugerencia (id) {
  document.getElementById("urlPerson").value=id;

}

var timer;

// var input -> input donde se ha presionado la tecla
function comprobarSugerencias(input) {
  if ($(input).val().length){
    if (timer != null) {
      clearTimeout(timer);
    }
    // esperamos un segundo
    timer = setTimeout(function(){
      //document.getElementById("urlPerson").value=id;
      $("#sugerencia").html( "<img src='./images/ring.svg' alt='Cargando'></br>");

      $.get( "/sugerencias/recomendadas",
        { url: document.getElementById("url").value,
       personalizada: document.getElementById("urlPerson").value } )
       .done(function(data) {
          if(data.length>1){
            var sugerencias=0;
            $("#sugerencia").html( "<h4>Sugerencias, elige la que más te guste:<h4> </br>");
            var sugerenciasBotones="";
            while(sugerencias<data.length){
              obj=data[sugerencias];
              var mensaje=obj.recomendacion;
              sugerenciasBotones+="<button id='"+mensaje+"' onclick='elegirSugerencia(this.id)' type='button' class='btn btn-link'>"+mensaje+" </button>";
              sugerencias=sugerencias+1;
            }
            $("#sugerencias").html( sugerenciasBotones);

            document.getElementById("sugerencias").style.display = "inline";
            document.getElementById("sugerencia").style.display = "inline";
          }
          else{
            document.getElementById("sugerencias").style.display = "none";
            document.getElementById("sugerencia").style.display = "none";
          }
      })
      .fail(function(data) {
        document.getElementById("sugerencias").style.display = "none";
        document.getElementById("sugerencia").style.display = "none";
      });
    }, 1000);
  }
  else {
    clearTimeout(timer);
    document.getElementById("sugerencias").style.display = "none";
    document.getElementById("sugerencia").style.display = "none";
  }
}
