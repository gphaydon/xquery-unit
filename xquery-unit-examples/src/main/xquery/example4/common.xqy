xquery version "1.0-ml";

module namespace cm="http://marklogic.com/common";

declare namespace qm = "http://marklogic.com/xdmp/query-meters";

(:~ The paramater that request XML will be passed in on. :)
declare variable $REQUEST-PARAMETER as xs:string := "request";


(:~
 : Get the request element for the current service call.  This assumes that it
 : is being passed in under the parameter $REQUEST-PARAMETER
 :
 : $default - default elements that should be put inside of a request element
 :)
declare function cm:get-request-element($default as element()*) as element(request) {
    let $requestString := xdmp:get-request-field($REQUEST-PARAMETER)
   
    let $retval := 
        if ($requestString) then
            let $requestElement := xdmp:unquote($requestString)/element()
            return 
                typeswitch ($requestElement) 
                case element(request)
                    return (
                        if ($requestElement/@debug = "on") then
                            (xdmp:log("common.xqy, get-request-element: turning query-trace on", "info"),
                            xdmp:query-trace(fn:true()))
                        else
    		              (),
                        $requestElement
                    )
                default return <request>{$requestElement}</request>
          
        else (: if ($requestString) :)
            if ($default) then
                <request>
                  { $default }
                </request>
            else
                let $set := xdmp:set-response-code(400,"Bad Request")
                return fn:error(xs:QName("NO-REQUEST"), fn:concat("No ", $REQUEST-PARAMETER, " parameter present and no default available."))
    let $debug := xdmp:log(text{"get-request-element: request-id:",  xdmp:request(), "request:", xdmp:quote($retval)}, "debug")
    return $retval
};

declare function cm:request-complete() {
  xdmp:log(text{"request complete, request-id:",  xdmp:request(), cm:pertinent-query-meters()}, "debug")
};

declare function cm:pertinent-query-meters() {
    let $meters := xdmp:query-meters()
    let $p := text {
        "elapsed:", $meters/qm:elapsed-time,
        "ETC hits:", $meters/qm:expanded-tree-cache-hits,
        "ETC misses:", $meters/qm:expanded-tree-cache-misses,
        "CTC hits:", $meters/qm:compressed-tree-cache-hits,
        "CTC misses:", $meters/qm:compressed-tree-cache-misses
    }
    return $p
};



