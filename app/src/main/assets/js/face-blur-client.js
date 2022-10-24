var actions;
var actionsEn = {
	mode: {
		name: "mode",
		items: [
			{
				title: "Auto",
				val: "auto"
			},
			{
				icon: "",
				title: "Shutter priority",
				val: "shutter"
			},
			{
				icon: "",
				title: "ISO priority",
				val: "iso"
			},
			{
				icon: "",
				title: "Manual",
				val: "manual"
			}
		]
	}
}
var actionsJp = {
	mode: {
		name: "mode",
		items: [
            {
                title: "オート",
				val: "auto"
            },
            {
                icon: "",
                title: "シャッター優先",
				val: "shutter"
            },
            {
                icon: "",
                title: "ISO優先",
				val: "iso"
            },
            {
                icon: "",
                title: "マニュアル",
				val: "manual"
            }
        ]
	}
}
var mIsoSupport = [64, 80, 100, 125, 160, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 2000, 2500, 3200];

var mShutterSpeedSupport = [
	{ val : "60", text : "60" },
	{ val : "30", text : "30" },
    { val : "25", text : "25" },
	{ val : "20", text : "20" },
	{ val : "15", text : "15" },
	{ val : "13", text : "13" },
	{ val : "10", text : "10" },
	{ val : "8", text : "8" },
	{ val : "6", text : "6" },
	{ val : "5", text : "5" },
	{ val : "4", text : "4" },
	{ val : "3.2", text : "3.2" },
	{ val : "2.5", text : "2.5" },
	{ val : "2", text : "2" },
	{ val : "1.6", text : "1.6" },
	{ val : "1.3", text : "1.3" },
	{ val : "1", text : "1" },
	{ val : "0.76923076", text : "1/1.3" },
	{ val : "0.625", text : "1/1.6" },
	{ val : "0.5", text : "1/2" },
	{ val : "0.4", text : "1/2.5" },
	{ val : "0.33333333", text : "1/3" },
	{ val : "0.25", text : "1/4" },
	{ val : "0.2", text : "1/5" },
	{ val : "0.16666666", text : "1/6" },
	{ val : "0.125", text : "1/8" },
	{ val : "0.1", text : "1/10" },
	{ val : "0.07692307", text : "1/13" },
	{ val : "0.06666666", text : "1/15" },
	{ val : "0.05", text : "1/20" },
	{ val : "0.04", text : "1/25" },
	{ val : "0.03333333", text : "1/30" },
	{ val : "0.025", text : "1/40" },
	{ val : "0.02", text : "1/50" },
	{ val : "0.01666666", text : "1/60" },
	{ val : "0.0125", text : "1/80" },
	{ val : "0.01", text : "1/100" },
	{ val : "0.008", text : "1/125" },
	{ val : "0.00625", text : "1/160" },
	{ val : "0.005", text : "1/200" },
	{ val : "0.004", text : "1/250" },
	{ val : "0.003125", text : "1/320" },
	{ val : "0.0025", text : "1/400" },
	{ val : "0.002", text : "1/500" },
	{ val : "0.0015625", text : "1/640" },
	{ val : "0.00125", text : "1/800" },
	{ val : "0.001", text : "1/1000" },
	{ val : "0.0008", text : "1/1250" },
	{ val : "0.000625", text : "1/1600" },
	{ val : "0.0005", text : "1/2000" },
	{ val : "0.0004", text : "1/2500" },
	{ val : "0.0003125", text : "1/3200" },
	{ val : "0.00025", text : "1/4000" },
	{ val : "0.0002", text : "1/5000" },
	{ val : "0.00015625", text : "1/6400" },
	{ val : "0.000125", text : "1/8000" },
	{ val : "0.0001", text : "1/10000" },
    { val : "0.00008", text : "1/12500" },
	{ val : "0.0000625", text : "1/16000" },
    { val : "0.00005", text : "1/20000" },
    { val : "0.00004", text : "1/25000" }
];

var mShutterSpeedSupportOnShutterSpeedMode = [
	{ val : "15", text : "15" },
	{ val : "13", text : "13" },
	{ val : "10", text : "10" },
	{ val : "8", text : "8" },
	{ val : "6", text : "6" },
	{ val : "5", text : "5" },
	{ val : "4", text : "4" },
	{ val : "3.2", text : "3.2" },
	{ val : "2.5", text : "2.5" },
	{ val : "2", text : "2" },
	{ val : "1.6", text : "1.6" },
	{ val : "1.3", text : "1.3" },
	{ val : "1", text : "1" },
	{ val : "0.76923076", text : "1/1.3" },
	{ val : "0.625", text : "1/1.6" },
	{ val : "0.5", text : "1/2" },
	{ val : "0.4", text : "1/2.5" },
	{ val : "0.33333333", text : "1/3" },
	{ val : "0.25", text : "1/4" },
	{ val : "0.2", text : "1/5" },
	{ val : "0.16666666", text : "1/6" },
	{ val : "0.125", text : "1/8" },
	{ val : "0.1", text : "1/10" },
	{ val : "0.07692307", text : "1/13" },
	{ val : "0.06666666", text : "1/15" },
	{ val : "0.05", text : "1/20" },
	{ val : "0.04", text : "1/25" },
	{ val : "0.03333333", text : "1/30" },
	{ val : "0.025", text : "1/40" },
	{ val : "0.02", text : "1/50" },
    { val : "0.01666666", text : "1/60" },
	{ val : "0.0125", text : "1/80" },
	{ val : "0.01", text : "1/100" },
	{ val : "0.008", text : "1/125" },
	{ val : "0.00625", text : "1/160" },
	{ val : "0.005", text : "1/200" },
	{ val : "0.004", text : "1/250" },
	{ val : "0.003125", text : "1/320" },
	{ val : "0.0025", text : "1/400" },
	{ val : "0.002", text : "1/500" },
	{ val : "0.0015625", text : "1/640" },
	{ val : "0.00125", text : "1/800" },
	{ val : "0.001", text : "1/1000" },
	{ val : "0.0008", text : "1/1250" },
	{ val : "0.000625", text : "1/1600" },
	{ val : "0.0005", text : "1/2000" },
	{ val : "0.0004", text : "1/2500" },
	{ val : "0.0003125", text : "1/3200" },
	{ val : "0.00025", text : "1/4000" },
	{ val : "0.0002", text : "1/5000" },
	{ val : "0.00015625", text : "1/6400" },
	{ val : "0.000125", text : "1/8000" },
	{ val : "0.0001", text : "1/10000" },
	{ val : "0.00008", text : "1/12500" },
    { val : "0.0000625", text : "1/16000" },
    { val : "0.00005", text : "1/20000" },
    { val : "0.00004", text : "1/25000" }
];

var mEvSupport = [ -2.0, -1.7, -1.3, -1.0, -0.7, -0.3, 0, +0.3, +0.7, +1.0, +1.3, +1.7, +2.0];

var mWbSupport = [
	{ val : "auto", text : "Auto" ,icon: "icon--wb_auto"},
	{ val : "daylight", text : "Outdoor" ,icon: "icon--wb_sun-current"},
	{ val : "shade", text : "Shade" ,icon: "icon--wb_shade-current"},
	{ val : "cloudy-daylight", text : "Cloudy" ,icon: "icon--wb_cloud-current"},
	{ val : "incandescent", text : "Incandescent light 1" ,icon: "icon--wb_inc_1"},
	{ val : "_warmWhiteFluorescent", text : "Incandescent light 2" ,icon: "icon--wb_inc_2"},
	{ val : "_dayLightFluorescent", text : "Daylight color fluorescent light" ,icon: "icon--wb_fluorescent-d-current"},
	{ val : "_dayWhiteFluorescent", text : "Natural white fluorescent light" ,icon: "icon--wb_fluorescent-n-current"},
	{ val : "fluorescent", text : "White fluorescent light" ,icon: "icon--wb_fluorescent-w-current"},
	{ val : "_bulbFluorescent", text : "Light bulb color fluorescent light" ,icon: "icon--wb_fluorescent-l-current"}
];

var mOptionSupport;
var mOptionSupportEn = [
	{ val: "off", text: "OFF"},
	{ val: "DR Comp", text: "DR Compensat."},
	{ val: "Noise Reduction", text: "Noise reduct."},
	{ val: "hdr", text: "HDR Rendering."}
];
var mOptionSupportJp = [
	{ val: "off", text: "OFF"},
	{ val: "DR Comp", text: "DR補正"},
	{ val: "Noise Reduction", text: "ノイズ低減"},
	{ val: "hdr", text: "HDR合成"}
];

var mShutterSpeedData = "";
var mShutterSpeedPriorityData = "";
 
$(function(){
	var language = (window.navigator.languages && window.navigator.languages[0]) ||
                window.navigator.language ||
                window.navigator.userLanguage ||
                window.navigator.browserLanguage;
    if (language === 'ja' || language === 'ja-JP') {
        actions = actionsJp;
        mOptionSupport = mOptionSupportJp;
    } else {
        actions = actionsEn;
        mOptionSupport = mOptionSupportEn;
    }

	$("#shoot__setting-auto").removeClass("hide");
	$("#select_cancel_btn").hide();

	initEvMenu();
	initISOMenu();
	initWBMenu();
	initWBManualMenu();
	initOptionMenu();
	initShutterSpeedMenu();
	initModeActionSheet();

	$("#selectmode_btn").on("click", function(e){
		e.preventDefault();

		$(".checkbox").addClass("show");
		$("#list_tableview").addClass("hasmenu");
		$("#list_footermenu").addClass("show");
		$("#selectmode_btn").hide();
		$("#select_cancel_btn").show();
	});

	$("#select_cancel_btn").on("click", function(e){
		e.preventDefault();

		$(".checkbox").removeClass("show");
		$("#list_tableview").removeClass("hasmenu");
		$("#list_footermenu").removeClass("show");
		$("#selectmode_btn").show();
		$("#select_cancel_btn").hide();
	});

	$(".btn--data").on("click", function(e){
		e.preventDefault();

		var setting = $(this).attr('data-setting');
		var position = $(this).attr('data-position');

		if(!$( "#" + setting + "_menu" ).hasClass( "show" )){
			hideAllMenu();
			console.log("setting:" + setting);
			console.log("position:" + position);
			$("#" + setting + "_menu").addClass("show");
			$("#" + setting + "_menu").addClass("position" + position);
			//initMenus(setting);
		}else{
			hideAllMenu();
		}

		$("#" + setting + "_menu_wrap").slick("getSlick").reinit();

	});

	$("#aspect_btn").on("click", function(e){
		e.preventDefault();

		showActionSheet(actions.setting.aspect);
	});

	$("#interval_btn").on("click", function(e){
		e.preventDefault();

		showActionSheet(actions.setting.interval);
	});

	$("#shootcount_btn").on("click", function(e){
		e.preventDefault();

		showActionSheet(actions.setting.shootcount);
	});

	$("#zenith_btn").on("click", function(e){
		e.preventDefault();

		showActionSheet(actions.setting.zenith);
	});

	$("#stitch_btn").on("click", function(e){
		e.preventDefault();

		showActionSheet(actions.setting.stitch);
	});

	$("#mode_btn").on("click", function(e){
		e.preventDefault();

		showActionSheet(actions.mode);

	});

	$(".actionsheet_cancel_btn").on("click", function(e){
		e.preventDefault();

		hideActionSheet();
	});

	$("#setting_btn").on("click", function(e){
		e.preventDefault();

		showSetting();
	});

	$("#setting_close_btn").on("click", function(e){
		e.preventDefault();

		hideSetting();
	});

	$("#back_btn").on("click", function(e){
		e.preventDefault();

		hideCameraImageList();
	});

	$( 'input[name="action"]:radio' ).on("change", function(e){
		console.log("s");
	});

	$("#camera_image_btn").on("click", function(e){
		e.preventDefault();

		showCameraImageList();
	});

	$("#shutter_btn").on("click", function(e){
		e.preventDefault();

		var btnstate = $.data($("#shutter_btn").get(0), "state");
		if(btnstate == "off" || btnstate == undefined){
			btnstate = "on";
			$.data($("#shutter_btn").get(0), "state", "on");
			$("#shutter_btn").removeClass("btn--shutter-normal");
			$("#shutter_btn").addClass("btn--shutter_recording-normal");
		}else{
			btnstate = "off";
			$.data($("#shutter_btn").get(0), "state", "off");
			$("#shutter_btn").removeClass("btn--shutter_recording-normal");
			$("#shutter_btn").addClass("btn--shutter-normal");
		}

		changeRecoadingState(btnstate);
	});

    $(window).on('orientationchange',function(){
      window.location.reload();
    });

});

function initEvMenu(){
	var setData = "";
	for (var i = 0; i < mEvSupport.length; i++){
		setData += "<label class='scroll_btn'>";
		setData += "	<input name='ev' type='radio' value='" + getNumber(mEvSupport[i]) + "' id='ev" + i + "'>";
		setData += "	<i for='ev" + i + "'>" + getNumber(mEvSupport[i].toFixed(1)) + "</i>";
		setData += "</label>";		
	}

	$("#ev_menu_wrap").html(setData);

	$('#ev_menu_wrap').slick({
	  slidesToShow: 3,
	  slidesToScroll: 1,
	  infinite: false,
	  dots: false,
	  arrows: false,
	  centerMode: true,
	  focusOnSelect: true,
	  variableWidth: true,
	  initialSlide: 5,
	  swipeToSlide: true
	});

  $("input[name='ev']:eq(5)").prop('checked', true);

    $('input[name="ev"]').on('click',function(event) {
       var htmlvalue = '<p class="btn--data__value">'+ $(event.target).toFixed(1).val() +'</p>';
        switch($("input[name='action']:checked").val()){
        case "auto":
          $("#evlabel1").html(htmlvalue);
          break;
        case "shutter":
          $("#evlabel2").html(htmlvalue);
          break;
        case "iso":
          $("#evlabel3").html(htmlvalue);
          break;
        case "manual":
          break;
       }
    });

	setTimeout(function(){
		$("#ev_menu").removeClass("inithide");
		$("#ev_menu").removeClass("show");
	}, 1000);


}

function initISOMenu(){
	var setData = "";
	for (var i = 0; i < mIsoSupport.length; i++){
		setData += "<label class='scroll_btn'>";
		setData += "	<input name='iso' type='radio' value='" + mIsoSupport[i] + "' id='iso" + i + "'>";
		setData += "	<i for='iso" + i + "'>" + mIsoSupport[i] + "</i>";
		setData += "</label>";		
	}

	$("#iso_menu_wrap").html(setData);

	$('#iso_menu_wrap').slick({
	  slidesToShow: 3,
	  slidesToScroll: 1,
	  infinite: false,
	  dots: false,
	  arrows: false,
	  centerMode: true,
	  focusOnSelect: true,
	  initialSlide: 2,
	  swipeToSlide: true
	});	

  $('input[name=iso]:eq(2)').prop('checked', true);

	setTimeout(function(){
		$("#iso_menu").removeClass("inithide");
		$("#iso_menu").removeClass("show");
	}, 1000);

    $('input[name="iso"]').on('click',function(event) {
       var htmlvalue = '<p class="btn--data__value">'+ $(event.target).val() +'</p>';
        switch($("input[name='action']:checked").val()){
        case "auto":
          break;
        case "shutter":
          break;
        case "iso":
          $("#isolabel2").html(htmlvalue);
          break;
        case "manual":
          $("#isolabel3").html(htmlvalue);
          break;
       }
    });

$("#iso_menu_wrap").click(function() {
    if(this.checked) {
       document.getElementById("iso").innerHTML =
             '<p class="btn--data__value">'+ $("input[name='iso']:checked").val() +'</p>';
    }
});
}

function initWBMenu(){	
	var setData = "";
	for (var i = 0; i < mWbSupport.length; i++){
		setData += "<label class='scroll_btn wb'>";
		setData += "	<input name='wb' type='radio' value='" + mWbSupport[i].val + "' id='wb" + i + "'>";
		setData += "	<i for='wb" + i + "' class='wbicon" + i + "'>" + mWbSupport[i].text + "</i>";
		setData += "</label>";		
	}

	$("#wb_menu_wrap").html(setData);

	$('#wb_menu_wrap').slick({
	  slidesToShow: 3,
	  slidesToScroll: 1,
	  infinite: false,
	  dots: false,
	  arrows: false,
	  centerMode: true,
	  focusOnSelect: true,
	  variableWidth: true,
	  initialSlide: 5,
	  swipeToSlide: true
	});

  $('input[name=wb]:eq(5)').prop('checked', true);

	setTimeout(function(){
		$("#wb_menu").removeClass("inithide");
		$("#wb_menu").removeClass("show");
	}, 1000);
}

function initWBManualMenu(){
	var wb = 2500;
	var setData = "";
	for (var i = 0; i < 76; i++){
		setData += "<label class='scroll_btn'>";
		setData += "	<input name='wbmanual' type='radio' value='" + wb + "' id='wbm" + i + "'>";
		setData += "	<i for='wbm" + i + "'>" + wb + "</i>";
		setData += "</label>";
		wb += 100;	
	}

	$("#wbmanual_menu_wrap").html(setData);

   var htmlvalue = "<p class='btn--data__value--icon icon--wb_inc_2'></p>";
      $("#wblabel0").html(htmlvalue);
      $("#wblabel1").html(htmlvalue);
      $("#wblabel2").html(htmlvalue);
      $("#wblabel3").html(htmlvalue);

	$('#wbmanual_menu_wrap').slick({
	  slidesToShow: 3,
	  slidesToScroll: 1,
	  infinite: false,
	  dots: false,
	  arrows: false,
	  centerMode: true,
	  focusOnSelect: true,
	  variableWidth: true,
	  initialSlide: 5,
	  swipeToSlide: true
	});

  $('input[name=wbmanual]:eq(5)').prop('checked', true);

    $('input[name="wb"]').on('click',function(event) {
       var htmlvalue = setWBLabel($(event.target).val());
        switch($("input[name='action']:checked").val()){
        case "auto":
          $("#wblabel0").html(htmlvalue);
          break;
        case "shutter":
          $("#wblabel1").html(htmlvalue);
          break;
        case "iso":
          $("#wblabel2").html(htmlvalue);
          break;
        case "manual":
          $("#wblabel3").html(htmlvalue);
          break;
       }
    });

	setTimeout(function(){
		$("#wbmanual_menu").removeClass("inithide");
		$("#wbmanual_menu").removeClass("show");
	}, 1000);
}

function setWBLabel(value) {
        var label = "";
        var index = 0;
        var icon ="";
      	for (var i = 0; i < mWbSupport.length; i++){
          if (mWbSupport[i].val == value) {
            label = mWbSupport[i].text;
            icon = mWbSupport[i].icon;
            index = i;
            break;
          }
        }

       return "<p class='btn--data__value--icon " + icon + "'>" + label +"</p>";
}

function getWBMenuIndex(value) {
        var label = "";
        var index = 0;
        var icon ="";
      	for (var i = 0; i < mWbSupport.length; i++){
          if (mWbSupport[i].val == value) {
            index = i;
            break;
          }
        }

       return index;
}

function initShutterSpeedMenu(){
	mShutterSpeedData = "";
	for (var i = 0; i < mShutterSpeedSupport.length; i++){
		mShutterSpeedData += "<label class='scroll_btn'>";
		mShutterSpeedData += "	<input name='shutterspeed' type='radio' value='" + mShutterSpeedSupport[i].val + "' id='shutter" + i + "'>";
		mShutterSpeedData += "	<i for='shutter" + i + "'>" + mShutterSpeedSupport[i].text + "</i>";
		mShutterSpeedData += "</label>";
	}

	mShutterSpeedPriorityData = "";
	for (var i = 0; i < mShutterSpeedSupportOnShutterSpeedMode.length; i++){
    		mShutterSpeedPriorityData += "<label class='scroll_btn'>";
    		mShutterSpeedPriorityData += "	<input name='shutterspeed' type='radio' value='" + mShutterSpeedSupportOnShutterSpeedMode[i].val + "' id='shutter" + i + "'>";
    		mShutterSpeedPriorityData += "	<i for='shutter" + i + "'>" + mShutterSpeedSupportOnShutterSpeedMode[i].text + "</i>";
    		mShutterSpeedPriorityData += "</label>";
    }
	$("#shutter_menu_wrap").html(mShutterSpeedData);

	$('#shutter_menu_wrap').slick({
	  slidesToShow: 3,
	  slidesToScroll: 5,
	  infinite: false,
	  dots: false,
	  arrows: false,
	  centerMode: true,
	  focusOnSelect: true,
	  initialSlide: 5,
	  swipeToSlide: true
	});

  $("input[name='shutterspeed']:eq(5)").prop('checked', true);

    $('input[name="shutterspeed"]').on('click',function(event) {
       var htmlvalue = setSPLabel($(event.target).val());
        switch($("input[name='action']:checked").val()){
        case "auto":
          break;
        case "shutter":
          $("#splabel1").html(htmlvalue);
          break;
        case "iso":
          break;
        case "manual":
          $("#splabel2").html(htmlvalue);
          break;
       }
    });

	setTimeout(function(){
		$("#shutter_menu").removeClass("inithide");
		$("#shutter_menu").removeClass("show");
	}, 1000);
}

function setSPLabel(value) {
        var label = "";
      	for (var i = 0; i < mShutterSpeedSupport.length; i++){
          if (mShutterSpeedSupport[i].val == value) {
            label = mShutterSpeedSupport[i].text;
            break;
          }
        }
       return '<p class="btn--data__value">' + label +"</p>";
}

function getSPIndex(value) {
        var index = 0;
      	for (var i = 0; i < mShutterSpeedSupport.length; i++){
          if (mShutterSpeedSupport[i].val == value) {
            index = i;
            break;
          }
        }
        return index;
}

function getSPModeIndex(value) {
        var index = 0;
      	for (var i = 0; i < mShutterSpeedSupportOnShutterSpeedMode.length; i++){
          if (mShutterSpeedSupportOnShutterSpeedMode[i].val == value) {
            index = i;
            break;
          }
        }
        return index;
}

function changeShutterSpeedSupport(menu) {
	$('#shutter_menu_wrap').slick("unslick");

    if (menu === "shutter") {
        $("#shutter_menu_wrap").html(mShutterSpeedPriorityData);
        	$('#shutter_menu_wrap').slick({
        	  slidesToShow: 3,
        	  slidesToScroll: 5,
        	  infinite: false,
        	  dots: false,
        	  arrows: false,
        	  centerMode: true,
        	  focusOnSelect: true,
        	  initialSlide: 5,
        	  swipeToSlide: true
        	});

          $("input[name='shutterspeed']:eq(5)").prop('checked', true);

    } else {
        $("#shutter_menu_wrap").html(mShutterSpeedData);
        $('#shutter_menu_wrap').slick({
          slidesToShow: 3,
          slidesToScroll: 5,
          infinite: false,
          dots: false,
          arrows: false,
          centerMode: true,
          focusOnSelect: true,
        	  initialSlide: 15,
          swipeToSlide: true
        });
          $("input[name='shutterspeed']:eq(15)").prop('checked', true);
    }

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
}

function initOptionMenu(){
	var setData = "";
	for (var i = 0; i < mOptionSupport.length; i++){
        setData += "<label class='scroll_btn'>";
        setData += "	<input name='filter' type='radio' value='" + mOptionSupport[i].val + "' id='option" + i + "'>";
        setData += "	<i for='option" + i + "'>" + mOptionSupport[i].text + "</i>";
        setData += "</label>";
	}

	$("#option_menu_wrap").html(setData);

	$('#option_menu_wrap').slick({
	  slidesToShow: 3,
	  slidesToScroll: 1,
	  infinite: false,
	  dots: false,
	  arrows: false,
	  centerMode: true,
	  focusOnSelect: true,
	  initialSlide: 0,
	  swipeToSlide: true,
  respondTo: 'slider',
  responsive: [
    {
      breakpoint: 2048,     // 1024〜2048px
      settings: {
        slidesToShow: 5,
        slidesToScroll: 5,
        infinite: true,
        dots: true
      }
    },
    {
      breakpoint: 1024,     // 600〜1023px
      settings: {
        slidesToShow: 4,
        slidesToScroll: 4,
        infinite: true,
        dots: true
      }
    },
    {
      breakpoint: 600,      // 480〜599px
      settings: {
        slidesToShow: 3,
        slidesToScroll: 3
      }
    },
    {
      breakpoint: 480,      // 〜479px
      settings: {
        slidesToShow: 2,
        slidesToScroll: 2
      }
    }
  ],	});

  $("input[name='filter']:eq(0)").prop('checked', true);

    $('input[name="filter"]').on('click',function(event) {
       var htmlvalue = setOPLabel($(event.target).val());
       $("#ftlabel").html(htmlvalue);
    });

	setTimeout(function(){
		$("#option_menu").removeClass("inithide");
		$("#option_menu").removeClass("show");
	}, 1000);
}

function setOPLabel(value) {
      	for (var i = 0; i < mOptionSupport.length; i++){
          if (mOptionSupport[i].val == value) {
            label = mOptionSupport[i].text;
            break;
          }
        }
        return  '<p class="btn--data__value">'+ label +'</p>';
}

function getOPIndex(value) {
        var index = 0;
      	for (var i = 0; i < mOptionSupport.length; i++){
          if (mOptionSupport[i].val == value) {
            index = i;
            break;
          }
        }
        return  index;
}

function hideAllMenu(){
	$("#ev_menu").removeAttr('class');
	$("#iso_menu").removeAttr('class');
	$("#wb_menu").removeAttr('class');
	$("#wb_menu").removeAttr('class');
	$("#shutter_menu").removeAttr('class');
	$("#wbmanual_menu").removeAttr('class');
	$("#option_menu").removeAttr('class');

	$("#ev_menu").addClass("pluginctrl__shoot__setting__detail");
	$("#iso_menu").addClass("pluginctrl__shoot__setting__detail");
	$("#wb_menu").addClass("pluginctrl__shoot__setting__detail");
	$("#shutter_menu").addClass("pluginctrl__shoot__setting__detail");
	$("#option_menu").addClass("pluginctrl__shoot__setting__detail");
	$("#wbmanual_menu").addClass("pluginctrl__shoot__setting__detail");
	$("#option_menu").addClass("pluginctrl__shoot__setting__detail");
}

function showActionSheet(data){
	$("#action_sheet__btn_list").empty();
	for (var i = 0; i < data.items.length; i++){
		var id = "action" + i;
		if(data.items[i].icon == undefined){
			$("#action_sheet__btn_list").append("<li><input type='radio' name='action' value='" + data.items[i].val + "' id='" + id + "'/><label for='" + id + "' class='action'>" + data.items[i].title + "</label></li>");
		}else{
			$("#action_sheet__btn_list").append("<li><input type='radio' name='action' value='" + data.items[i].val + "' id='" + id + "'/><label for='" + id + "' class='action'>" + data.items[i].title + "</label></li>");
		}
	}

	console.log(data);

	$('input[name="action"]:radio').change( function() {  
		changeMenu($(this).val());
		hideActionSheet();
	}); 

	$(".action_sheet").css("transform", "translateY(0%)");
	$("#setting_action").addClass("show");
}

function initModeActionSheet(){
	$("#action_sheet__btn_list").empty();
	var data = actions.mode;
	for (var i = 0; i < data.items.length; i++){
		var id = "action" + i;
		if(data.items[i].val == "auto"){
			$("#action_sheet__btn_list").append("<li><input type='radio' name='action' checked='checked' value='" + data.items[i].val + "' id='" + id + "'/><label for='" + id + "' class='action'>" + data.items[i].title + "</label></li>");
		}else{
			$("#action_sheet__btn_list").append("<li><input type='radio' name='action' value='" + data.items[i].val + "' id='" + id + "'/><label for='" + id + "' class='action'>" + data.items[i].title + "</label></li>");
		}
	}
}

function hideActionSheet(){
	$("#setting_action").removeClass("show");
	setTimeout(function(){
		$(".action_sheet").css("transform", "translateY(100%)");
	}, 500);
}

function changeMenu(menu){
	hideAllMenu();

	$("#mode_btn").removeClass();

	$("#shoot__setting-auto").addClass("hide");
	$("#shoot__setting-shutter").addClass("hide");
	$("#shoot__setting-iso").addClass("hide");
	$("#shoot__setting-manual").addClass("hide");
	
  changeShutterSpeedSupport(menu);
	switch(menu){
    case "auto":
    	$("#mode_btn").addClass("btn--mode-auto-normal");
		  $("#shoot__setting-auto").removeClass("hide");
      break;
    case "shutter":
    	$("#mode_btn").addClass("btn--mode-shutter-normal");
		  $("#shoot__setting-shutter").removeClass("hide");

      $("input[name='shutterspeed']:eq(15)").prop('checked', true);
      $("input[name='ev']:eq(5)").prop('checked', true);
      $('input[name=wb]:eq(5)').prop('checked', true);
      $('input[name=wbmanual]:eq(5)').prop('checked', true);
      break;
    case "iso":
    	$("#mode_btn").addClass("btn--mode-iso-normal");
		  $("#shoot__setting-iso").removeClass("hide");

      $("input[name='iso']:eq(2)").prop('checked', true);
      $("input[name='ev']:eq(5)").prop('checked', true);
      $('input[name=wb]:eq(5)').prop('checked', true);
      $('input[name=wbmanual]:eq(5)').prop('checked', true);
      break;
    case "manual":
    	$("#mode_btn").addClass("btn--mode-manual-normal");
		  $("#shoot__setting-manual").removeClass("hide");

      $("input[name='iso']:eq(2)").prop('checked', true);
      $("input[name='shutterspeed']:eq(5)").prop('checked', true);
      $('input[name=wb]:eq(5)').prop('checked', true);
      $('input[name=wbmanual]:eq(5)').prop('checked', true);
      break;
    }
  $("#mode_btn").addClass("btn");
}

function showSetting(){
	$("#setting_page").addClass("show");
}

function hideSetting(){
	$("#setting_page").removeClass("show");
}

function showCameraImageList(){
	$("#list_page").addClass("show");
	$("#shooting_page").addClass("hide");
}

function hideCameraImageList(){
	$("#list_page").removeClass("show");
	$("#shooting_page").removeClass("hide");
}

function changeRecoadingState(state){
	console.log(state);
}

function disableShutterButton() {
    $(".btn.btn--shutter-normal").prop("disabled", true);
}

function enableShutterButton() {
    $(".btn.btn--shutter-normal").prop("disabled", false);
}

function getNumber(theNumber)
{
    if(theNumber > 0){
        return "+" + theNumber;
    }else{
        return theNumber.toString();
    }
}
