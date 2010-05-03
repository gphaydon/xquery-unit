xquery version "1.0-ml";
import module namespace search="http://marklogic.com/search" at "search-lib.xqy";
import module namespace xq = "http://marklogic.com/xqunit" at "/lib/xqunit.xqy";
declare function local:test-get-query() {
  let $query as schema-element(cts:query)? := search:get-query("foobar")
  let $expected := document{cts:word-query("foobar")}/node()
  return xq:assert-equal("test-get-query", $query, $expected)
};
declare function local:test-get-query-empty() {
  let $query as schema-element(cts:query)? := search:get-query(())
  return xq:assert-equal("test-get-query-empty", $query, ())
};
declare function local:test-get-query-empty-string() {
  let $query as schema-element(cts:query)? := search:get-query("")
  return xq:assert-equal("test-get-query-empty-string", $query, ())
};
<results>
{local:test-get-query(), local:test-get-query-empty(), local:test-get-query-empty-string()}
</results>