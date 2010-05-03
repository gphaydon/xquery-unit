xquery version "1.0-ml";
module namespace search="http://marklogic.com/search";

declare function get-query($query-str as xs:string) as cts:query? {
  if ($query-str eq "") then
    ()
  else
    cts:word-query($query-str)
};

declare function search(
        $query as cts:query, 
        $start as xs:integer,
        $end as xs:integer) as element(results) {
    let $results := cts:search(fn:doc(), $query)[$start to $end]
    let $count := cts:remainder($results[1])
    return
    <results count="{$count}">
    {
      for $r in $results 
      return <result>{$r//ArticleTitle}</result>
    }
    </results>
};