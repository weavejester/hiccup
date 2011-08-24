/* Hiccup related jQuery UI code. Assumes jQuery and jQuery UI have
   been loaded 
*/

jQuery(document).ready(function() {

  // for (make-sortable tag)
  $(".hiccup-jquery-sortable").sortable();

  // for (rank-options ...)
  $("ul.hiccup-jquery-rank-options").sortable("options", "placeholder", "ui-state-highlight")
    .bind("sortupdate", function(event, ui) {
      $("input", this).each(function(index, txtfield) {
        $(txtfield).val(index);
      });
    });
});
