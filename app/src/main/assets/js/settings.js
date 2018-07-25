function setOptions() {
  var option = new Object();
  option.exposureProgram = $("input[name='action']:checked").val()
  if (typeof option.exposureProgram === "undefined" || option.exposureProgram === "auto") {
    option.exposureProgram = "2";
  }
  if (option.exposureProgram === "shutter") {
    option.exposureProgram = "4";
  }
  if (option.exposureProgram === "iso") {
    option.exposureProgram = "9";
  }
  if (option.exposureProgram === "manual") {
    option.exposureProgram = "1";
  }
  option.iso = $("input[name='iso']:checked").val()
  option.shutterSpeed = $("input[name='shutterspeed']:checked").val()
  option.whiteBalance = $("input[name='wb']:checked").val()
  option.colorTemperature = $("input[name='wbmanual']:checked").val()
  option.exposureCompensation = $("input[name='ev']:checked").val()
  option._filter = $("input[name='filter']:checked").val()

  var memberfilter = new Array();

  memberfilter.push('exposureProgram')
  if (typeof option.iso != "undefined") {
    memberfilter.push('iso')
  }
  if (typeof option.shutterSpeed != "undefined") {
    memberfilter.push('shutterSpeed')
  }
  if (typeof option.whiteBalance != "undefined") {
    memberfilter.push('whiteBalance')
  }
  if (typeof option.colorTemperature != "undefined") {
    memberfilter.push('colorTemperature')
  }
  if (typeof option.exposureCompensation != "undefined") {
    memberfilter.push('exposureCompensation')
  }
  if (typeof option._filter != "undefined") {
    memberfilter.push('_filter')
  }

  var setOptions = JSON.stringify(option, memberfilter, "\t");
  console.log(setOptions);
  var setOptionsJSON = JSON.parse(setOptions);

  var _parameters = {
    options: setOptionsJSON
  };

  var _commands = {
    name: 'camera.setOptions',
    parameters: _parameters
  };

  var xmlHttpRequest = new XMLHttpRequest();
  xmlHttpRequest.onreadystatechange = function() {
    var READYSTATE_COMPLETED = 4;
    var HTTP_STATUS_OK = 200;

    if (this.readyState == READYSTATE_COMPLETED &&
      this.status == HTTP_STATUS_OK) {
      console.log('setOptions:' + this.responseText);
      getOptions(false);
    }
  }

  xmlHttpRequest.open('POST', '/blur/commands/execute', true);
  xmlHttpRequest.setRequestHeader('Content-Type', 'application/json');
  xmlHttpRequest.send(JSON.stringify(_commands));
}

function getOptions(EXPFlag) {

  var _parameters = {
    optionNames: ['exposureProgram', 'iso', 'shutterSpeed', 'whiteBalance', '_colorTemperature', 'exposureCompensation', '_filter']
  };

  var _commands = {
    name: 'camera.getOptions',
    parameters: _parameters
  };

  var xmlHttpRequest = new XMLHttpRequest();
  xmlHttpRequest.onreadystatechange = function() {
    var READYSTATE_COMPLETED = 4;
    var HTTP_STATUS_OK = 200;

    if (this.readyState == READYSTATE_COMPLETED &&
      this.status == HTTP_STATUS_OK) {
      var response = JSON.parse(this.responseText);
      var options = response.results.options;
      var exposurePrograms = [];

      switch (options.exposureProgram) {
        case 2:
          if (EXPFlag) {
            changeMenu("auto");
          }
          var htmlvalue = '<p class="btn--data__value">' + options.exposureCompensation + '</p>';
          $("#evlabel1").html(htmlvalue);
          $("#evlabel1").val(options.exposureCompensation);
          var evindex = mEvSupport.indexOf(options.exposureCompensation);
          $('input[name=ev]:eq(' + evindex + ')').prop('checked', true);

          htmlvalue = setWBLabel(options.whiteBalance);
          $("#wblabel0").html(htmlvalue);
          $("#wblabel0").val(options.whiteBalance);
          var wbindex = getWBMenuIndex(options.whiteBalance);
          $('input[name=wb]:eq(' + wbindex + ')').prop('checked', true);

          htmlvalue = setOPLabel(options._filter);
          $("#ftlabel").html(htmlvalue);
          var opindex = getOPIndex(options._filter);
          $('input[name=filter]:eq(' + opindex + ')').prop('checked', true);
          break;
        case 4:
          if (EXPFlag) {
            changeMenu("shutter");
          }
          $("#shutter_menu_wrap").html(mShutterSpeedPriorityData);
          var htmlvalue = setSPLabel(options.shutterSpeed);
          $("#splabel1").html(htmlvalue);
          $("#splabel1").val(options.shutterSpeed);
          var spindex = getSPModeIndex(options.shutterSpeed);
          $('input[name=shutterspeed]:eq(' + spindex + ')').prop('checked', true);

          htmlvalue = '<p class="btn--data__value">' + options.exposureCompensation + '</p>';
          $("#evlabel2").html(htmlvalue);
          $("#evlabel2").val(options.exposureCompensation);
          var evindex = mEvSupport.indexOf(options.exposureCompensation);
          $('input[name=ev]:eq(' + evindex + ')').prop('checked', true);

          htmlvalue = setWBLabel(options.whiteBalance);
          $("#wblabel1").html(htmlvalue);
          $("#wblabel1").val(options.whiteBalance);
          var wbindex = getWBMenuIndex(options.whiteBalance);
          $('input[name=wb]:eq(' + wbindex + ')').prop('checked', true);

          break;
        case 9:
          if (EXPFlag) {
            changeMenu("iso");
          }
          var htmlvalue = '<p class="btn--data__value">' + options.iso + '</p>';
          $("#isolabel2").html(htmlvalue);
          $("#isolabel2").val(options.iso);
          var isoindex = mIsoSupport.indexOf(options.iso);
          $('input[name=iso]:eq('+ isoindex +')').prop('checked', true);

          htmlvalue = '<p class="btn--data__value">' + options.exposureCompensation + '</p>';
          $("#evlabel3").html(htmlvalue);
          $("#evlabel3").val(options.exposureCompensation);
          var evindex = mEvSupport.indexOf(options.exposureCompensation);
          $('input[name=ev]:eq(' + evindex + ')').prop('checked', true);

          htmlvalue = setWBLabel(options.whiteBalance);
          $("#wblabel2").html(htmlvalue);
          $("#wblabel2").val(options.whiteBalance);
          var wbindex = getWBMenuIndex(options.whiteBalance);
          $('input[name=wb]:eq(' + wbindex + ')').prop('checked', true);
          break;
        case 1:
          if (EXPFlag) {
            changeMenu("manual");
          }
          var htmlvalue = '<p class="btn--data__value">' + options.iso + '</p>';
          $("#isolabel3").html(htmlvalue);
          $("#isolabel3").val(options.iso);
          var isoindex = mIsoSupport.indexOf(options.iso);
          $('input[name=iso]:eq('+ isoindex +')').prop('checked', true);

          htmlvalue = setSPLabel(options.shutterSpeed);
          $("#splabel2").html(htmlvalue);
          $("#splabel2").val(options.shutterSpeed);
          var spindex = getSPIndex(options.shutterSpeed);
          $('input[name=shutterspeed]:eq(' + spindex + ')').prop('checked', true);

          htmlvalue = setWBLabel(options.whiteBalance);
          $("#wblabel3").html(htmlvalue);
          $("#wblabel3").val(options.whiteBalance);
          var wbindex = getWBMenuIndex(options.whiteBalance);
          $('input[name=wb]:eq(' + wbindex + ')').prop('checked', true);
          break;
      }
    }
  }

  xmlHttpRequest.open('POST', '/blur/commands/execute', true);
  xmlHttpRequest.setRequestHeader('Content-Type', 'application/json');
  xmlHttpRequest.send(JSON.stringify(_commands));
}

function setActionsAuto() {
  var options = new Object();
  options.exposureProgram = $("input[name='action']:checked").val()
  if (typeof options.exposureProgram === "undefined" || options.exposureProgram === "auto") {
    setOptions(true);
  }
}

