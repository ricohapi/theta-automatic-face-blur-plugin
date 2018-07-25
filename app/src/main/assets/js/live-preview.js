var timerTask;
var READYSTATE_COMPLETED = 4;
var READYSTATE_LOADING = 3;
var HTTP_STATUS_OK = 200;
var POST = 'POST';
var CONTENT_TYPE = 'content-Type';
var TYPE_JSON = 'application/json';
var COMMAND = 'blur/commands/execute';
var PREVIEW = 'blur/commands/execute';
var status;

function startLivePreview() {
  var command = {};
  command.name = 'camera.startLivePreview';
  var xmlHttpRequest = new XMLHttpRequest();
  xmlHttpRequest.onreadystatechange = function() {
    if (this.readyState === READYSTATE_COMPLETED &&
      this.status === HTTP_STATUS_OK) {
      console.log(this.responseText);
    } else {
      console.log('start live preview failed');
    }
    getPreviewPicture();
  };
  xmlHttpRequest.open(POST, COMMAND, true);
  xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
  xmlHttpRequest.send(JSON.stringify(command));
}

function getPreviewPicture() {
    var command = {};
    command.name = 'camera.getLivePreview';

    var xmlHttpRequest = new XMLHttpRequest();
    xmlHttpRequest.onreadystatechange = function() {

      if (this.readyState === READYSTATE_COMPLETED &&
        this.status === HTTP_STATUS_OK) {
        var reader = new FileReader();
        reader.onloadend = function onLoad() {
          var img = document.getElementById('previewImage');
          img.src = reader.result;
        };
        reader.readAsDataURL(this.response);
        repeat()
      }
    };
    xmlHttpRequest.open(POST, COMMAND, true);
    xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
    xmlHttpRequest.responseType = 'blob';
    xmlHttpRequest.send(JSON.stringify(command));
}

function repeat() {
  const d1 = new Date();
  while (true) {
    const d2 = new Date();
    if (d2 - d1 > 30) {
      break;
    }
  }
  getPreviewPicture();
}

function init360LiveView() {
  const width = window.innerWidth,
    height = window.innerHeight;

  var scene = new THREE.Scene();

  var geometry = new THREE.SphereGeometry(5, 60, 40);
  geometry.scale(-1, 1, 1);

  var video = document.getElementById("previewImage");
  var texture = new THREE.VideoTexture(video);
  texture.minFilter = THREE.LinearFilter;

  var material = new THREE.MeshBasicMaterial({
    map: texture
  });
  sphere = new THREE.Mesh(geometry, material);
  scene.add(sphere);

  var ambient = new THREE.AmbientLight(0x550000);
  scene.add(ambient);

  var camera = new THREE.PerspectiveCamera(75, width / height, 1, 1000);
  camera.position.set(7, 0, 0, 1);
  camera.lookAt(sphere.position);

  var renderer = new THREE.WebGLRenderer();
  renderer.setSize(width, height);
  renderer.setClearColor({
    color: 0x000000
  });
  document.getElementById('stage').appendChild(renderer.domElement);
  renderer.render(scene, camera);

  var controls = new THREE.OrbitControls(camera, renderer.domElement);

  function render() {
    requestAnimationFrame(render);
    window.addEventListener('resize', onWindowResize, false);
    renderer.render(scene, camera);

    controls.update();
  }

  render();

  function onWindowResize() {
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
    renderer.setSize(width, height);
  }
}

function checkStatus() {
  setInterval(getStatus, 2000);
}

function getStatus() {
  var command = {};
  command.name = 'camera.getStatus';
  var xmlHttpRequest = new XMLHttpRequest();
  xmlHttpRequest.onreadystatechange = function() {
    if (this.readyState === READYSTATE_COMPLETED &&
      this.status === HTTP_STATUS_OK) {
      var json = JSON.parse(this.responseText);
      status = json.status;
      console.log(status);
      if (status !== 'idle') {
        disableShutterButton();
      } else {
        enableShutterButton();
      }
    }
  };
  xmlHttpRequest.open(POST, COMMAND, true);
  xmlHttpRequest.setRequestHeader(CONTENT_TYPE, TYPE_JSON);
  xmlHttpRequest.send(JSON.stringify(command));
}