window.AudioContext = window.AudioContext || window.webkitAudioContext;

var audioContext = new AudioContext();
var audioInput = null,
    realAudioInput = null,
    inputPoint = null,
    audioRecorder = null;
// var recIndex = 0;

function gotStream(stream) {
    inputPoint = audioContext.createGain();

    // Create an AudioNode from the stream.
    realAudioInput = audioContext.createMediaStreamSource(stream);
    audioInput = realAudioInput;
    audioInput.connect(inputPoint);

//    audioInput = convertToMono( input );

    // analyserNode = audioContext.createAnalyser();
    // analyserNode.fftSize = 2048;
    // inputPoint.connect( analyserNode );

    audioRecorder = new Recorder( inputPoint );

    zeroGain = audioContext.createGain();
    zeroGain.gain.value = 0.0;
    inputPoint.connect( zeroGain );
    zeroGain.connect( audioContext.destination );
}

function initAudio() {
        if (!navigator.getUserMedia)
            navigator.getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
        if (!navigator.cancelAnimationFrame)
            navigator.cancelAnimationFrame = navigator.webkitCancelAnimationFrame || navigator.mozCancelAnimationFrame;
        if (!navigator.requestAnimationFrame)
            navigator.requestAnimationFrame = navigator.webkitRequestAnimationFrame || navigator.mozRequestAnimationFrame;

    navigator.getUserMedia(
        {
            "audio": {
                "mandatory": {
                    "googEchoCancellation": "false",
                    "googAutoGainControl": "false",
                    "googNoiseSuppression": "false",
                    "googHighpassFilter": "false"
                },
                "optional": []
            },
        }, gotStream, function(e) {
            alert('Error getting audio');
            console.log(e);
        });
}


function saveLocal(blob){
      var file = new Blob([blob], {type: 'audio/wav'});
      console.log(file);
      var fileURL = URL.createObjectURL(file);
      console.log(fileURL);
      // create an anchor and click on it.
      var ancorTag = document.createElement('a');
      ancorTag.href = fileURL;ancorTag.target = '_blank';
      ancorTag.download = 'sound.wav';
      document.body.appendChild(ancorTag);
      ancorTag.click();
      document.body.removeChild(ancorTag);
}

function doneEncoding(blob){
  var file = new Blob([blob], {type: 'audio/wav'});
  console.log(file);
  var fileURL = URL.createObjectURL(file);
  console.log(fileURL);
  saveServer();
}

function saveServer(file){

  var formData = new FormData();
  // formData.append('enctype', 'multipart/form-data');
  formData.append('file', file, 'sound.wav');
  console.log(formData);
  $.ajax({
    type: 'POST',
    url: 'GetSound',
    data: formData,
    enctype: 'multipart/form-data',
    dataType: false,
    cache: false,
    processData: false,
    success: function(data){
      console.log(data);
    },
    error: function(data){
      console.log(data);
    }
  });
}

window.addEventListener('load', initAudio );

btnStartRec.addEventListener('click', function(event){
  console.log('btnStartRec clicked');

  audioRecorder.clear();
  audioRecorder.record();
}, false);

btnStopRec.addEventListener('click', function(event){
  console.log('btnStopRec clicked');
  audioRecorder.stop();
  audioRecorder.exportWAV(doneEncoding);

}, false);
