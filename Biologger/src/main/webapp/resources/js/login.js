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

/* global $this, $window */

$(function(){
    updateContainer();
    $(window).resize(function() {
        updateContainer();
    });
});

function updateContainer() {
    $(window).height() < $("body").height() ? 
    $("html").removeClass("h-100") : $("html").addClass("h-100");
}

