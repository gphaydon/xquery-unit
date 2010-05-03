xquery version "1.0-ml";
module namespace search="http://marklogic.com/search";

declare function get-query($query-str as xs:string) as schema-element(cts:query)? {
  if ($query-str eq "") then
    ()
  else
    document{cts:word-query($query-str)}/node()
};
