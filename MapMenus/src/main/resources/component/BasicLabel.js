var text;

function init(t) {
    text = t;
}

// Called when this element is rendered
function render(graphics,player) {
   var bounds = component.getBounds();
    graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    renderer.drawStringCentered(graphics, text, bounds);
}