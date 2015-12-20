$(document).ready(function() {
  // Vars init
  var authUsersSel = $("#authUsers");
  var authMethods = {gmail: {buttonId: "gmailAdd", description: 'Gmail', divBg: "bg-danger"},
                      twitter: {buttonId: "twitterAdd", description: 'Twitter', divBg: "bg-info"},
                      facebook: {buttonId: "facebookAdd", description: 'Facebook', divBg: "bg-primary"}};

  // Form submit trigger
  $("#shortener").submit(function(event) {
    if (timer != null) {
      clearTimeout(timer);
      // Remove inputs to prevent the form to send unneeded data
      if (authUsersSel.is(":not(:checked)")) authUsersSel.empty();
    }
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
        var obj = jQuery.parseJSON( jqXHR.responseText );
        var mensaje=obj.message;
        var atributos= mensaje.split(":");
        if(atributos.length>1){
          $("#result").html( "<div class='alert alert-danger lead'>" +  atributos[0] + "</div>");
          var sugerencias=1;
          $("#sugerencia").html( "<h4>Sugerencias, elige la que más te guste:<h4> </br>");
          var sugerenciasBotones="";
          while(sugerencias<atributos.length){
            sugerenciasBotones+="<button id='"+atributos[sugerencias]+"' onclick='elegirSugerencia(this.id)' type='button' class='btn btn-link'>"+atributos[sugerencias]+" </button>";
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
    event.preventDefault();
  });
  
  // Enable Ad button change trigger
  $("#enableAd").change(function(){
    var elSel = $(this);
    if (elSel.is(":checked")){
      // Check whether the div hasn't been previously filled
      if (authUsersSel.is(":empty")){
        // Set HTML content
        var headerContent = "<br><div id='authUsersHelp' class='col-sm-12'>Please, fill with <strong>usernames</strong> of people you don't want to view ads</div><div id='authUsersHeader' class='input-group col-sm-12'>";
        var tableContent = "";
        for(var key in authMethods){
          headerContent += "<div class='col-sm-4 " + authMethods[key]['divBg'] + "'><label for='" + authMethods[key]['buttonId'] + "'>" + authMethods[key]['description'] + "</label><button type='button' id='" + authMethods[key]['buttonId'] + "' class='btn btn-default pull-right'><span class='glyphicon glyphicon-plus'></span></button></div>"
          tableContent += "<div class='col-sm-4 " + authMethods[key]['divBg'] + "' id='" + key + "Container'></div>";
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

  // Login buttons triggers
  $.each($("[id$='LoginRedirect']"), function(){
    $(this).click(function(){
      // First: change the URL to go to
      $(this).attr("href", $(this).attr("href") + $("#goToUrl").val());
      // Then the browser will GET the URL
    });
  });
});

function setAuthButtonsTriggers(authMethods){
  for(var key in authMethods){
    (function(k){
      var htmlContent = "<div class='col-sm-12'><input type='text' name='users[\"" + k + "\"][]' class='" + authMethods[k]['divBg'] + "'></div>";
      $("#" + authMethods[k]['buttonId']).click(function(){
        $("#" + k + "Container").prepend(htmlContent);
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

var timer;
function comprobarSugerencias(input) {
  if ($(input).val().length){
    if (timer != null) {
      clearTimeout(timer);
    }
    // esperamos un segundo
    timer = setTimeout(function(){
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
    }, 1000);
  }
  else {
    clearTimeout(timer);
    document.getElementById("sugerencias").style.display = "none";
    document.getElementById("sugerencia").style.display = "none";
  }
}
