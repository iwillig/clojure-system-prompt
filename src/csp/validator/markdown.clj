(ns csp.validator.markdown
  "Parse Markdown files with YAML frontmatter using CommonMark."
  (:require [clojure.string :as str])
  (:import (org.commonmark.parser Parser)
           (org.commonmark.ext.front.matter YamlFrontMatterExtension YamlFrontMatterVisitor)))

(defn create-parser
  "Create a CommonMark parser with YAML frontmatter support."
  []
  (-> (Parser/builder)
      (.extensions [(YamlFrontMatterExtension/create)])
      (.build)))

(defn parse-frontmatter
  "Extract YAML frontmatter from markdown content.

   Returns a map with:
   - :frontmatter - map of frontmatter key-value pairs
   - :content - full markdown content including frontmatter
   - :body - markdown content without frontmatter"
  [markdown-content]
  (let [parser (create-parser)
        doc (.parse parser markdown-content)
        visitor (YamlFrontMatterVisitor.)
        _ (.visit visitor doc)
        fm-data (.getData visitor)]
    {:frontmatter (into {} (map (fn [[k v]] [(keyword k) (first v)]) fm-data))
     :content markdown-content
     :body (str/replace-first markdown-content #"(?s)^---\n.*?\n---\n" "")}))

(defn extract-code-blocks
  "Extract code blocks from parsed markdown.

   Returns a sequence of maps with:
   - :language - language identifier (e.g., 'clojure', 'shell')
   - :content - code block content"
  [markdown-content]
  ;; Simple regex-based extraction for now
  ;; Could enhance with full AST traversal if needed
  (let [pattern #"(?s)```(\w+)?\n(.*?)```"
        matches (re-seq pattern markdown-content)]
    (map (fn [[_ lang content]]
           {:language (or lang "")
            :content content})
         matches)))
