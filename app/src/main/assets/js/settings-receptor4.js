function setOptions() {
  var option = new Object();
  option.exposureProgram = $("input[name='action']:checked").val()
    var memberfilter = new Array();

    if (typeof option.exposureProgram === "undefined" || option.exposureProgram === "auto") {
        option.exposureProgram = "2";
        option.whiteBalance = $("input[name='wb']:checked").val()
        option._filter = $("input[name='filter']:checked").val()
        option.exposureCompensation = $("input[name='ev']:checked").val()
        memberfilter.push('exposureProgram')
        if (typeof option.whiteBalance != "undefined") {
            memberfilter.push('whiteBalance')
        }
        if (typeof option._filter != "undefined") {
            memberfilter.push('_filter')
        }
        if (typeof option.exposureCompensation != "undefined") {
            memberfilter.push('exposureCompensation')
        }
    }

    if (option.exposureProgram === "shutter") {
      option.exposureProgram = "4";
      option.shutterSpeed = $("input[name='shutterspeed']:checked").val()
      option.exposureCompensation = $("input[name='ev']:checked").val()
      option.whiteBalance = $("input[name='wb']:checked").val()
      memberfilter.push('exposureProgram')
      if (typeof option.shutterSpeed != "undefined") {
        memberfilter.push('shutterSpeed')
      }
      if (typeof option.exposureCompensation != "undefined") {
        memberfilter.push('exposureCompensation')
      }
      if (typeof option.whiteBalance != "undefined") {
        memberfilter.push('whiteBalance')
      }
    }

    if (option.exposureProgram === "iso") {
      option.exposureProgram = "9";
      option.iso = $("input[name='iso']:checked").val()
      option.exposureCompensation = $("input[name='ev']:checked").val()
      option.whiteBalance = $("input[name='wb']:checked").val()
      memberfilter.push('exposureProgram')
      if (typeof option.iso != "undefined") {
        memberfilter.push('iso')
      }
      if (typeof option.exposureCompensation != "undefined") {
          memberfilter.push('exposureCompensation')
      }
      if (typeof option.whiteBalance != "undefined") {
          memberfilter.push('whiteBalance')
      }
    }

    if (option.exposureProgram === "manual") {
      option.exposureProgram = "1";
      option.shutterSpeed = $("input[name='shutterspeed']:checked").val()
      option.whiteBalance = $("input[name='wb']:checked").val()
      option.iso = $("input[name='iso']:checked").val()
      memberfilter.push('exposureProgram')
        if (typeof option.shutterSpeed != "undefined") {
          memberfilter.push('shutterSpeed')
        }
        if (typeof option.whiteBalance != "undefined") {
          memberfilter.push('whiteBalance')
        }
        if (typeof option.iso != "undefined") {
          memberfilter.push('iso')
        }
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
    optionNames: ['exposureProgram', 'iso', 'shutterSpeed', 'whiteBalance', '_colorTemperature', 'exposureCompensation', '_filter', "fileFormat"]
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
              hideAllMenu();

              $("#mode_btn").removeClass();

              $("#shoot__setting-auto").addClass("hide");
              $("#shoot__setting-shutter").addClass("hide");
              $("#shoot__setting-iso").addClass("hide");
              $("#shoot__setting-manual").addClass("hide");
          }
          var evindex = mEvSupport.indexOf(options.exposureCompensation);
          var wbindex = getWBMenuIndex(options.whiteBalance);
          var opindex = getOPIndex(options._filter);

          $('#ev_menu_wrap').slick("unslick");
          $('#wb_menu_wrap').slick("unslick");
          $('#option_menu_wrap').slick("unslick");

          $("#ev_menu_wrap").html(mEvData);
          $('#ev_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            variableWidth: true,
            initialSlide: evindex,
            swipeToSlide: true
          });
          $("#wb_menu_wrap").html(mWBData);
          $('#wb_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            variableWidth: true,
            initialSlide: wbindex,
            swipeToSlide: true
          });
          $("#option_menu_wrap").html(mOptionData);
          $('#option_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            initialSlide: opindex,
            swipeToSlide: true
          });
    	  $("#mode_btn").addClass("btn--mode-auto-normal");
		  $("#shoot__setting-auto").removeClass("hide");

          $("input[name='action']:eq(0)").prop('checked', true);
          $("#mode_btn").addClass("btn");

          var htmlvalue = '<p class="btn--data__value">' + getNumber(options.exposureCompensation.toFixed(1)) + '</p>';
          $("#evlabel1").html(htmlvalue);
          $("#evlabel1").val(options.exposureCompensation);
          $('input[name=ev]:eq(' + evindex + ')').prop('checked', true);

          htmlvalue = setWBLabel(options.whiteBalance);
          $("#wblabel0").html(htmlvalue);
          $("#wblabel0").val(options.whiteBalance);
          $('input[name=wb]:eq(' + wbindex + ')').prop('checked', true);

          htmlvalue = setOPLabel(options._filter);
          $("#ftlabel").html(htmlvalue);
          $('input[name=filter]:eq(' + opindex + ')').prop('checked', true);
          break;
        case 4:
          if (EXPFlag) {
              hideAllMenu();

              $("#mode_btn").removeClass();

              $("#shoot__setting-auto").addClass("hide");
              $("#shoot__setting-shutter").addClass("hide");
              $("#shoot__setting-iso").addClass("hide");
              $("#shoot__setting-manual").addClass("hide");
          }
          var spindex = getSPIndex(options.shutterSpeed);
          var wbindex = getWBMenuIndex(options.whiteBalance);
          var evindex = mEvSupport.indexOf(options.exposureCompensation);

          $('#shutter_menu_wrap').slick("unslick");
          $('#wb_menu_wrap').slick("unslick");
          $('#ev_menu_wrap').slick("unslick");

          $('input[name="shutterspeed"]').on('click',function(event) {
            var label = "";
            for (var i = 0; i < mShutterSpeedSupport.length; i++){
              if (mShutterSpeedSupport[i].val == $(event.target).val()) {
                label = mShutterSpeedSupport[i].text;
                break;
              }
            }
            var htmlvalue = '<p class="btn--data__value">'+ label +'</p>';
            $("#splabel1").html(htmlvalue);
            $("#splabel2").html(htmlvalue);
            $("#splabel3").html(htmlvalue);
          });

          $("#shutter_menu_wrap").html(mShutterSpeedData);
          $('#shutter_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 5,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            initialSlide: spindex,
            swipeToSlide: true
          });

          $("#wb_menu_wrap").html(mWBData);
          $('#wb_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            variableWidth: true,
            initialSlide: wbindex,
            swipeToSlide: true
          });
          $("#ev_menu_wrap").html(mEvData);
          $('#ev_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            variableWidth: true,
            initialSlide: evindex,
            swipeToSlide: true
          });

          $("#mode_btn").addClass("btn--mode-shutter-normal");
          $("#shoot__setting-shutter").removeClass("hide");

          $("input[name='action']:eq(1)").prop('checked', true);
          $("#mode_btn").addClass("btn");

          var htmlvalue = setSPLabel(options.shutterSpeed);
          $("#splabel1").html(htmlvalue);
          $("#splabel1").val(options.shutterSpeed);
          $('input[name=shutterspeed]:eq(' + spindex + ')').prop('checked', true);

          htmlvalue = '<p class="btn--data__value">' + getNumber(options.exposureCompensation.toFixed(1)) + '</p>';
          $("#evlabel2").html(htmlvalue);
          $("#evlabel2").val(options.exposureCompensation);

          $('input[name=ev]:eq(' + evindex + ')').prop('checked', true);

          htmlvalue = setWBLabel(options.whiteBalance);
          $("#wblabel1").html(htmlvalue);
          $("#wblabel1").val(options.whiteBalance);

          $('input[name=wb]:eq(' + wbindex + ')').prop('checked', true);

          break;
        case 9:
          if (EXPFlag) {
              hideAllMenu();

              $("#mode_btn").removeClass();

              $("#shoot__setting-auto").addClass("hide");
              $("#shoot__setting-shutter").addClass("hide");
              $("#shoot__setting-iso").addClass("hide");
              $("#shoot__setting-manual").addClass("hide");
          }

          var isoindex = mIsoSupport.indexOf(options.iso);
          var evindex = mEvSupport.indexOf(options.exposureCompensation);
          var wbindex = getWBMenuIndex(options.whiteBalance);

          $('#iso_menu_wrap').slick("unslick");
          $('#wb_menu_wrap').slick("unslick");
          $('#ev_menu_wrap').slick("unslick");

          $("#iso_menu_wrap").html(mIsoData);
          $('#iso_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            initialSlide: isoindex,
            swipeToSlide: true
          });
          $("#wb_menu_wrap").html(mWBData);
          $('#wb_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
           focusOnSelect: true,
            variableWidth: true,
            initialSlide: wbindex,
            swipeToSlide: true
          });
          $("#ev_menu_wrap").html(mEvData);
          $('#ev_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            variableWidth: true,
            initialSlide: evindex,
            swipeToSlide: true
          });

          $("#mode_btn").addClass("btn--mode-iso-normal");
          $("#shoot__setting-iso").removeClass("hide");

          $("input[name='action']:eq(2)").prop('checked', true);
          $("#mode_btn").addClass("btn");

          var htmlvalue = '<p class="btn--data__value">' + options.iso + '</p>';
          $("#isolabel2").html(htmlvalue);
          $("#isolabel2").val(options.iso);

          $('input[name=iso]:eq('+ isoindex +')').prop('checked', true);

          htmlvalue = '<p class="btn--data__value">' + getNumber(options.exposureCompensation.toFixed(1)) + '</p>';
          $("#evlabel3").html(htmlvalue);
          $("#evlabel3").val(options.exposureCompensation);

          $('input[name=ev]:eq(' + evindex + ')').prop('checked', true);

          htmlvalue = setWBLabel(options.whiteBalance);
          $("#wblabel2").html(htmlvalue);
          $("#wblabel2").val(options.whiteBalance);

          $('input[name=wb]:eq(' + wbindex + ')').prop('checked', true);
          break;
        case 1:
          if (EXPFlag) {
              hideAllMenu();
              $("#mode_btn").removeClass();

              $("#shoot__setting-auto").addClass("hide");
              $("#shoot__setting-shutter").addClass("hide");
              $("#shoot__setting-iso").addClass("hide");
              $("#shoot__setting-manual").addClass("hide");
          }
          var spindex = getSPIndex(options.shutterSpeed);
          var isoindex = mIsoSupport.indexOf(options.iso);
          var wbindex = getWBMenuIndex(options.whiteBalance);

          $('#shutter_menu_wrap').slick("unslick");
          $('#iso_menu_wrap').slick("unslick");
          $('#wb_menu_wrap').slick("unslick");

          $('input[name="shutterspeed"]').on('click',function(event) {
            var label = "";
            for (var i = 0; i < mShutterSpeedSupport.length; i++){
              if (mShutterSpeedSupport[i].val == $(event.target).val()) {
                label = mShutterSpeedSupport[i].text;
                break;
              }
            }
            var htmlvalue = '<p class="btn--data__value">'+ label +'</p>';
            $("#splabel1").html(htmlvalue);
            $("#splabel2").html(htmlvalue);
            $("#splabel3").html(htmlvalue);
          });

          $("#shutter_menu_wrap").html(mShutterSpeedData);
          $('#shutter_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 5,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            initialSlide: spindex,
            swipeToSlide: true
          });

          $("#iso_menu_wrap").html(mIsoData);
          $('#iso_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            initialSlide: isoindex,
            swipeToSlide: true
          });

          $("#wb_menu_wrap").html(mWBData);
          $('#wb_menu_wrap').slick({
            slidesToShow: 3,
            slidesToScroll: 1,
            infinite: false,
            dots: false,
            arrows: false,
            centerMode: true,
            focusOnSelect: true,
            variableWidth: true,
            initialSlide: wbindex,
            swipeToSlide: true
          });

          $("#mode_btn").addClass("btn--mode-manual-normal");
          $("#shoot__setting-manual").removeClass("hide");
          $("input[name='action']:eq(3)").prop('checked', true);

          $("#mode_btn").addClass("btn");
          var htmlvalue = '<p class="btn--data__value">' + options.iso + '</p>';
          $("#isolabel3").html(htmlvalue);
          $("#isolabel3").val(options.iso);
          $('input[name=iso]:eq('+ isoindex +')').prop('checked', true);

          htmlvalue = setSPLabel(options.shutterSpeed);
          $("#splabel2").html(htmlvalue);
          $("#splabel2").val(options.shutterSpeed);
          $('input[name=shutterspeed]:eq(' + spindex + ')').prop('checked', true);

          htmlvalue = setWBLabel(options.whiteBalance);
          $("#wblabel3").html(htmlvalue);
          $("#wblabel3").val(options.whiteBalance);
          $('input[name=wb]:eq(' + wbindex + ')').prop('checked', true);
        break;
      }
    }
  }

  xmlHttpRequest.open('POST', '/blur/commands/execute', true);
  xmlHttpRequest.setRequestHeader('Content-Type', 'application/json');
  xmlHttpRequest.send(JSON.stringify(_commands));
}