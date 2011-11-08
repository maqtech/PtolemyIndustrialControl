/* JavaScript library for Ptolemy Export to Web functionality.
 *
 */
function writeText(text) {
  document.getElementById("afterImage").innerHTML = text;
};
// The following anonymous function will be executed when the document
// is "ready" (but images are not necessarily loaded).
// JQuery ensures that the DOM object is fully constructed.
// The argument to "ready" is a callback function to call
// after the parent has completed.
$(document).ready(function(){
  // The following says to select all elements that are
  // areas of class lightbox (use # instead of . to specify an ID instead
  // of a class), and apply the function fancybox to them with the specified
  // arguments.
  // FIXME: This should be inserted by the classes that use it!
  // The following requires the jquery fancybox extension.
  // FIXME: The width and height below are hardwired. Should
  // be parameterized. Can be parameterized individually
  // by using an object ID instead of the iframe class.
  // Many more options can be set, as shown in http://fancybox.net/api.
  $("area.lightbox").fancybox({'width':700,'height':500});
});