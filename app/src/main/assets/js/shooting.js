function takePicture() {
  disableShutterButton();
  var command = {};
  command.name = "camera.takePicture";

  var xmlHttpRequest = new XMLHttpRequest();
  xmlHttpRequest.onreadystatechange = function() {
    var READYSTATE_COMPLETED = 4;
    var HTTP_STATUS_OK = 200;

    if (this.readyState == READYSTATE_COMPLETED &&
      this.status == HTTP_STATUS_OK) {
      console.log(this.responseText);
      startLivePreview();
    }
  }
  xmlHttpRequest.open('POST', '/blur/commands/execute', true);
  xmlHttpRequest.setRequestHeader('Content-Type', 'application/json');
  xmlHttpRequest.send(JSON.stringify(command));
}