$(document).ready(function() {
  // Vars init
  var authUsersSel = $("#authUsers");
  var enableAdSel = $("#enableAd");
  var authMethods = {local: {buttonId: "localAdd", description: "User", divBg: "bg-warning", method : "password"},
                      gmail: {buttonId: "gmailAdd", description: "Gmail", divBg: "bg-danger", method: "OAuth2"},
                      twitter: {buttonId: "twitterAdd", description: "Twitter", divBg: "bg-info", method: "OAuth1"},
                      facebook: {buttonId: "facebookAdd", description: "Facebook", divBg: "bg-primary", method: "OAuth2"}};

  // Form submit trigger
  $("#shortener").submit(function(event) {
    // Clear timer
    if (timer != null) clearTimeout(timer);
    // Remove inputs to prevent the form to send unneeded data
    if (enableAdSel.is(":not(:checked)")) authUsersSel.empty();
    $.ajax({
      type : "POST",
      url : "/link",
      data : $(this).serialize(),
      success : function(msg, status, jqXHR) {
          $("#result").html(
              "<div class='alert alert-success lead'><a id='link' target='_blank' href='"
              + msg.uri
              + "'>"
              + msg.uri
              + "</a></div>");
              document.getElementById("sugerencias").style.display = "none";
              document.getElementById("sugerencia").style.display = "none";
      },
      error : function(jqXHR, status, error) {
        var obj = jQuery.parseJSON(jqXHR.responseText);
        var mensaje = obj.message;
		if(mensaje.localeCompare("La URL a personalizar ya existe")==0 || mensaje.localeCompare("La URL a acortar no es válida")==0){
			$("#result").html( "<div class='alert alert-danger lead'>"+mensaje+"</div>");
	        document.getElementById("sugerencias").style.display = "inline";
	        document.getElementById("sugerencia").style.display = "inline";
			mostrarSugerencias();
		}
        else{
	        $("#result").html( "<div class='alert alert-danger lead'>" + error + "</div>");
	        document.getElementById("sugerencias").style.display = "none";
	        document.getElementById("sugerencia").style.display = "none";
        }
      }
    });
    event.preventDefault();
  });
  
  // Enable Ad button change trigger
  enableAdSel.change(function(){
    var elSel = $(this);
    if (elSel.is(":checked")){
      // Check whether the div hasn't been previously filled
      if (authUsersSel.is(":empty")){
        // Set HTML content
        var headerContent = "<br><div id='authUsersHelp' class='col-sm-12'>Please, fill with <strong>usernames</strong> of people you don't want to view ads</div><div id='authUsersHeader' class='input-group col-sm-12'>";
        var tableContent = "";
        for(var key in authMethods){
          headerContent += "<div class='col-sm-3 " + authMethods[key]['divBg'] + "'><label for='" + authMethods[key]['buttonId'] + "'>" + authMethods[key]['description'] + "</label><button type='button' id='" + authMethods[key]['buttonId'] + "' class='btn btn-default pull-right'><span class='glyphicon glyphicon-plus'></span></button></div>"
          tableContent += "<div class='col-sm-3 " + authMethods[key]['divBg'] + "' id='" + key + "Container' style='display: none'></div>";
        }
        headerContent += "</div><br>";
        authUsersSel.html(headerContent + tableContent);
        // Set triggers
        setAuthButtonsTriggers(authMethods);
      } else {
        authUsersSel.show();
      }
    }
    else{
      authUsersSel.hide();
    }
  });
});

function setAuthButtonsTriggers(authMethods){
  for(var key in authMethods){
    (function(k){
      var htmlContent = "<div class='col-sm-12'><input type='text' name='users[\"" + k + "\"][]' class='" + authMethods[k]['divBg'] + " form-control'></div>";
      $("#" + authMethods[k]['buttonId']).click(function(){
        var containerSel = $("#" + k + "Container");
        containerSel.prepend(htmlContent);
        containerSel.show();
      });
    })(key);
  }
}

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
