$(document).ready(function() {
  $("#shortener").submit(function(event) {
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
	if(mensaje.localeCompare("La URL a personalizar ya existe")==0 || 
  mensaje.localeCompare("La URL a acortar no es válida")==0){
		$("#result").html( "<div class='alert alert-danger lead'>"+mensaje+"</div>");
         	document.getElementById("sugerencias").style.display = "inline";
         	document.getElementById("sugerencia").style.display = "inline";
		mostrarSugerencias();

	}
        else{
          $("#result").html( "<div class='alert alert-danger lead'>Error de conexión</div>");
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

function mostrarSugerencias () {
//document.getElementById("urlPerson").value=id;
      document.getElementById("sugerencias").style.display = "none";
      $("#sugerencia").html( "<img src='./images/ring.svg' alt='Cargando'></br>");

      $.get( "/sugerencias/recomendadas",
        { url: document.getElementById("url").value,
       customTag: document.getElementById("urlPerson").value } )
       //cambiar a $("url").value
       //$("boton").keydown()
       .done(function(data) {
        if(data.length>1){
          var sugerencias=0;
          $("#sugerencia").html( "<h5>Esa url personalizada ya está ocupada, aquí hay unas cuantas sugerencias que te podrían interesar:<h5> </br>");
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
}

var timer;
function comprobarSugerencias(input) {
  $("#sugerencia").html( "<img src='./images/ring.svg' alt='Cargando'></br>");
  if ($(input).val().length>0){
    if (timer != null) {
      clearTimeout(timer);
    }
    // esperamos un segundo
    timer = setTimeout(mostrarSugerencias(), 400);
  }
  else {
    clearTimeout(timer);
    document.getElementById("sugerencias").style.display = "none";
    document.getElementById("sugerencia").style.display = "none";
  }
}
