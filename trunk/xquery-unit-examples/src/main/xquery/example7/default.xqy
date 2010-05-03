xquery version "1.0-ml";

import module namespace s="http://marklogic.com/search" at "/example7/search-lib.xqy";
declare namespace search = "http://marklogic.com/appservices/search";

declare function local:transform-snippet($snippet as element(search:snippet)) 
as element(p)
{
    <p class="snippet">
    {    
        for $match in $snippet/search:match
        return 
          for $node in $match/node()
          return 
            typeswitch($node)
              case element(search:highlight) 
                return (<span class="highlight">{fn:data($node)}</span>, " ")
              case text() 
                return 
                    if ($node/preceding-sibling::search:highlight) then $node
                    else fn:concat("...",$node)
              default return xs:string($node)
    }
    </p>
};

declare variable $q := xdmp:get-request-field("q");

let $set := xdmp:set-response-content-type("text/html")
let $start := xs:integer(xdmp:get-request-field("start", "1"))
let $end := xs:integer(xdmp:get-request-field("end", "10"))
let $page-length := ($end - $start + 1)

let $results := s:search($q, $start, $page-length)

let $start := xs:unsignedLong($results/@start)
let $length := xs:unsignedLong($results/@page-length)
let $total := xs:unsignedLong($results/@total)
let $last := xs:unsignedLong($start + $length -1)
let $end := if ($total > $last) then $last else $total

return
<html>
<head>
<style type="text/css">
#result {{
  border:1px solid blue;
  margin: 10px;
}}
.title {{
  font-weight: bold;
}}
</style>
</head>
<body>
<h1>Medline Search</h1>
<form action="default.xqy">
<input type="text" name="q" value="{$q}" size="50"/>
<input type="submit" value="Search"/>
</form>
{
if (fn:exists($q)) then
<div id="search-results">
<div>
Results <b>{$start}</b> to <b>{$end}</b> of <b>{$total}</b> for <b>{$q}</b> 
</div>
{
    for $r in $results/search:result
    let $log := xdmp:log($r)
    let $uri := $r/@uri
    let $doc := fn:doc($uri)
    let $title := fn:string($doc//MedlineCitation/Article/ArticleTitle)
    let $abstract := local:transform-snippet($r/search:snippet)
    return 
    <div id="result">
      <span class="title">{$title}</span>,
      <div>{$abstract}</div>
    </div>
}
</div>

else ()
}
</body>
</html>
