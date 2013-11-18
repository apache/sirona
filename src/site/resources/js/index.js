$(document).ready(function(){
  $('#screenshots-carousel').carousel({
    interval: 4000
  })

  $('#carousel-prev' ).on("click", function(){
    $('#screenshots-carousel').carousel('prev');
  })

  $('#carousel-next' ).on("click", function(){
    $('#screenshots-carousel').carousel('next');
  })


});

