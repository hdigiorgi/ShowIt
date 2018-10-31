$(() => {
    $(".landing-image-grid-container .cols").on("touchstart touchend", function(e){
      const target = $(e.currentTarget).children(".title")
      window.t = target
      console.log(target)
      $(target).toggleClass("title-hover");
    });
})