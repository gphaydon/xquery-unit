xquery version "1.0-ml";

declare variable $uri as xs:string external;
declare variable $new-title as xs:string external;

let $doc := fn:doc($uri)
let $title := $doc/MedlineCitation/Article/ArticleTitle
return xdmp:node-replace($title, <ArticleTitle>{$new-title}</ArticleTitle>)