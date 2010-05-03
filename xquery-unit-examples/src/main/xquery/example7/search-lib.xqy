xquery version "1.0-ml";
module namespace search="http://marklogic.com/search";

import module namespace mlsearch = "http://marklogic.com/appservices/search"
    at "/MarkLogic/appservices/search/search.xqy";

declare function search(
        $query-str as xs:string, 
        $start as xs:integer,
        $page-length as xs:integer) as element(mlsearch:response) {

    let $options := 
        <options xmlns="http://marklogic.com/appservices/search">
          <return-results>true</return-results>
          <return-facets>false</return-facets>
        </options>
    let $results := mlsearch:search($query-str, $options, $start, $page-length)
    return $results
};
