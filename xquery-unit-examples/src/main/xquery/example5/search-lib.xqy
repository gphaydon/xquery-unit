xquery version "1.0-ml";
module namespace search="http://marklogic.com/search";

import module namespace mlsearch = "http://marklogic.com/appservices/search"
    at "/MarkLogic/appservices/search/search.xqy";

declare function search($request as element(search)) {
    let $text := fn:data($request)
    let $options := 
        <options xmlns="http://marklogic.com/appservices/search">
          <return-results>true</return-results>
          <return-facets>false</return-facets>
        </options>
    let $results := mlsearch:search($text, $options)
    return <response>{$results}</response>
};
