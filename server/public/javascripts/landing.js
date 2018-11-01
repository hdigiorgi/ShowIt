$(window).on("load",() => {

  function intensify(color, limit, alpha) {
    const max = Math.max(color[0],color[1],color[2])
    const addition = limit - max
    const r = color[0]+addition
    const g = color[1]+addition
    const b = color[2]+addition
    return `rgb(${r},${g},${b},${alpha})`
  }

  function soft(color, limit, alpha) {
    const min = Math.min(color[0],color[1],color[2])
    const sub = limit - min
    const r = color[0]+sub
    const g = color[1]+sub
    const b = color[2]+sub
    return `rgb(${r},${g},${b},${alpha})`
  }

  function setHeadingColors(palette) {
    const e = $("#site-data-heading")
    const first = palette[0]
    const last = palette[palette.length-1]
    const background = soft(last, 0, 0.3)
    e.css("color", intensify(first, 255, 0.95))
    e.css("background-color", background)
  }

  const firstImage = $(".landing-image-grid-container img").first()
  if(firstImage.length > 0) {
    var colorThief = new ColorThief();
    setHeadingColors(colorThief.getPalette(firstImage.get(0)))
  }

  $("body").css("opacity", "1")
})