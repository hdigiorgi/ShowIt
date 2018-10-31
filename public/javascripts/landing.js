$(() => {
    $(".landing-image-grid-container .cols").on("touchstart touchend", function(e){
      const target = $(e.currentTarget).children(".title")
      $(target).toggleClass("title-hover");
    });
})