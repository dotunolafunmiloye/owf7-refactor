package integration

public class WidgetDefinitionPostParams {

	static def generatePostParamsA() {
		//A set of post parameters to be used for create.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890a0"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget"                                ,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generatePostParamsB() {
		//A set of post parameters to be used for updating the item created in A.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890a0"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget Updated"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generatePostParamsC() {
		//An entity unique from A to test multiple adds and the list functionality
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890a1"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 2"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsA1() {
		//For use in DashboardServiceTests.
		//Matches "A" DashboardWidgetState "1" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890a2"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 3"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsA2() {
		//For use in DashboardServiceTests.
		//Matches "A" DashboardWidgetState "2" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890b2"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 4"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsA3() {
		//For use in DashboardServiceTests.
		//Matches "A" DashboardWidgetState "3" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890c2"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 5"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsB1() {
		//For use in DashboardServiceTests.
		//Matches "B" DashboardWidgetState "2" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890e2"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 6"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsB2() {
		//For use in DashboardServiceTests.
		//Matches "V" DashboardWidgetStaBe "1" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890f2"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 7"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsC1() {
		generateDSTPostParamsB1()
	}

	static def generateDSTPostParamsC2() {
		generateDSTPostParamsB2()
	}

	static def generateDSTPostParamsD1() {
		//For use in DashboardServiceTests.
		//Matches "B" DashboardWidgetState "2" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890e4"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 8"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsD2() {
		//For use in DashboardServiceTests.
		//Matches "V" DashboardWidgetStaBe "1" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890f4"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 9"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsEF1() {
		//For use in DashboardServiceTests.
		//Matches "V" DashboardWidgetStaBe "1" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890f6"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 10"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsEF2() {
		//For use in DashboardServiceTests.
		//Matches "V" DashboardWidgetStaBe "1" in DST.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-1234567890e6"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 11"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsI1() {
		return generateDSTPostParamsB1()
	}

	static def generateDSTPostParamsI2() {
		return generateDSTPostParamsB2()
	}

	static def generateDSTPostParamsI3() {
		//For use in DashboardServiceTests.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-123456789102"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 12"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsI4() {
		//For use in DashboardServiceTests.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-123456789112"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 13"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsI5() {
		//For use in DashboardServiceTests.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-123456789122"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 14"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:200                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsI6() {
		//For use in DashboardServiceTests.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-123456789132"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 15"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:500                                        ,
					"height"       	:200
				]
	}

	static def generateDSTPostParamsI7() {
		//For use in DashboardServiceTests.
		def retVal = [
					"widgetGuid"	:"12345678-1234-1234-1234-123456789142"		,
					"widgetVersion"	:"1.0"		,
					"displayName"  	:"My Widget 16"						,
					"widgetUrl"    	:"http://foo.com/widget"                    ,
					"imageUrlSmall"	:"http://foo.com/widget/images/small.jpg"   ,
					"imageUrlLarge"	:"http://foo.com/widget/images/large.jpg"   ,
					"width"        	:400                                        ,
					"height"       	:200
				]
	}


	static def generateDSTPostParamsJ1() {
		return generateDSTPostParamsB1()
	}

	static def generateDSTPostParamsJ2() {
		return generateDSTPostParamsB2()
	}

	static def generateDSTPostParamsJ3() {
		return generateDSTPostParamsI3()
	}
}