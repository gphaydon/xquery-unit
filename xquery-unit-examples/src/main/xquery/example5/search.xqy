xquery version "1.0-ml";

import module namespace cm = "http://marklogic.com/common" at "/example5/common.xqy";
import module namespace search="http://marklogic.com/search" at "/example5/search-lib.xqy";

declare variable $NO-REQ as xs:QName := xs:QName("NO-REQUEST");
declare variable $INV-REQ as xs:QName := xs:QName("INVALID-REQUEST");

xdmp:set-response-content-type("text/xml"),
try {
  let $request := cm:get-request-element(())
  let $action := $request/element()
  let $check := if ($action) then () else 
    fn:error($NO-REQ, "No request...") 
  let $result :=
    typeswitch ($action)
      case element(search) return search:search($action)
      default return fn:error($INV-REQ, "Invalid request...")
  let $complete := cm:request-complete()
  return $result
} catch($e) {
    (xdmp:set-response-code(400,"Bad Request"),
    xdmp:log(xdmp:quote($e)),
    <error>400 Bad Request {$e}</error>)
}


