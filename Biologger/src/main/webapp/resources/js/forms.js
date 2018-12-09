/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * <!-- 
*******************************************************************
*********                                                **********
*********             Autor Alex Aldaco                  **********
*********                                                **********
*******************************************************************
-->
 */

$(function(){
    if($("#registerForm input:checkbox").is(':checked')) { 
        $(".ntrabajador-wrapper").removeClass("d-none");
    }
    $("#registerForm input:checkbox").change(function(){
        if($(this).is(":checked")) {
            $(".ntrabajador-wrapper").removeClass("d-none");
        } else {
           $(".ntrabajador-wrapper").addClass("d-none"); 
        }
    });
    $("#subirFoto .custom-file-input").change(function(e){
        var fileName = e. target. files[0]. name;
        if (fileName !== null) {
            $(this).parent(".custom-file").children("label").text(fileName);
            $(this).parent(".custom-file").parent(".d-flex").children("a")
                    .removeClass("disabled");
        }
    });
});

