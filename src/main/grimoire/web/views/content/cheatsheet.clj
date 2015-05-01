(ns grimoire.web.views.content.cheatsheet
  (:refer-clojure :exclude [munge])
  (:require [grimoire.web.views :refer :all]
            [clojure.string :as string]
            [grimoire.api :as api]
            [grimoire.api.web :as web]
            [grimoire.either :refer [succeed? result]]
            [grimoire.things :as t]
            [grimoire.web.layout :refer [layout]]
            [grimoire.web.util :as wutil]
            [grimoire.util :refer [munge]]
            [grimoire.web.config :as cfg]
            [grimoire.github :as gh]
            [simpledb.core :as sdb]))

;; Helpers
;;------------------------------------------------------------------------------

(defn →thing [s]
  {:pre [(string? s)]}
  (let [t (t/url-path->thing s)]
    (web/make-html-url (cfg/web-config) t)))

(defn →
  ([s]
   [:a {:href s} s])

  ([a t]
   [:a {:href a} t]))

(def ^:dynamic *prefix* nil)

(defn →clj
  ([s]
   {:pre [(string? s)]}
   (if (.startsWith s "clojure.")
     (→ (→thing (str "org.clojure/clojure/latest/clj/" s)) s)
     (let [s (str *prefix* s)]
       (→ (→thing (str "org.clojure/clojure/latest/clj/clojure.core/" (munge s))) s))))

  ([l s]
   (→ (→clj l) s)))

(defn code [& forms]
  `[:code {} ~@forms])

(defn table [& forms]
  `[:table {} ~@forms])

(defmacro with-common-prefix [p & forms]
  `(with-bindings [*prefix* ~p]
     []))

;; Fragments
;;------------------------------------------------------------------------------

(def clojure.repl
  [:box {}
   [:section {:title "Documentation"}
    [:table {:title "clojure.repl/"}
     [:row {}
      (→clj "clojure.repl/doc" "doc")
      (→clj "clojure.repl/find-doc" "find-doc")
      (→clj "clojure.repl/apropos" "apropos")
      (→clj "clojure.repl/source" "source")
      (→clj "clojure.repl/pst" "pst")
      (→clj "clojure.java.javadoc/javadoc")]]]])

(def clojure.numbers
  [:box {}
   [:subsection {}
    [:table {}
     [:row {:title "Literals"}
      ;; Longs
      (→ "http://docs.oracle.com/javase/7/docs/api/java/lang/Long.html" "Long:")
      "dec:"    (code "7") ","
      "hex:"    (code "0xff") ","
      "oct:"    (code "017") ","
      "binary:" (code "2r1011") ","
      "base 36" (code "36rCRAZY")
      "BigInt:" (code "7N")
      "Ratio:"  (code "-22/7")

      ;; Doubles
      (→ "http://docs.oracle.com/javase/7/docs/api/java/lang/Double.html" "Double:")
      (code "2.78") "," (code "-1.2e-5")

      ;; BigDecimals
      (→ "http://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html" "BigDecimal:")
      (code "4.2M") "," (code "5M")]

     `[:row {:title "Arithmetic"}
       ~@(map →clj ["+" "-" "*" "/" "quot" "rem" "mod" "inc" "dec" "max" "min"
                    "+'" "-'" "*'" "inc'" "dec'"])]

     `[:row {:title "Compare"}
       ~@(map →clj ["==" "<" ">" "<=" ">=" "compare"])]

     `[:row {:title "Bitwise"}
       ~@(map →clj ["bit-and" "bit-or" "bit-xor" "bit-not" "bit-flip" "bit-set"
                    "bit-shift-right" "bit-shift-left" "bit-and-not" "bit-clear"
                    "bit-test"])

       "(1.6)" ~(→clj "bit-unsigned-bit-shift-right")
       "(see " ~(→ "http://docs.oracle.com/javase/7/docs/api/java/math/BigInteger.html" "BigInteger") " for integers larger than Long)"]

     `[:row {:title "Cast"}
       ~@(map →clj ["byte" "short" "int" "long" "float" "double" "bigdec" "bigint"
                    "num" "rationalize" "biginteger"])]

     `[:row {:title "Test"}
       ~@(map →clj ["zero?" "pos?" "neg?" "even?" "odd?" "number?" "rational?"
                    "integer?" "ratio?" "decimal?" "float?"])]

     `[:row {:title "Random"}
       ~@(map →clj ["rand" "rand-int"])]

     [:row {:title "BigDecimal"}
      (→clj "with-precision")]

     ;; TBD: Why do these not exist in Clojure?
     ;; There are -int versions, but not long
     ;; versions.  unchecked-divide
     ;; unchecked-remainder.  Filed ticket CLJ-1545
     ;; to add them.

     `[:row {:title "Unchecked"}
       ~(→clj "*unchecked-math*")
       ~@(map (comp →clj (partial str "unchecked-"))
              ["add" "dec" "inc" "multiply" "negate" "subtract"])]]]])

(def clojure.functions
  [:box {}
   [:section {:title "Functions"}
    [:table {}
     `[:row {:title "Create"}
       ~@(map →clj ["fn" "defn" "defn-" "definline" "identity" "constantly" "memfn" "comp" "complement"
                    "partial" "juxt" "memoize" "fnil" "every-pred" "some-fn"])]

     `[:row {:title "Call"}
       ~@(map →clj ["apply" "->" "->>" "trampoline" "(1.5)" "as->" "cond->" "cond->>" "some->" "some->>"])]

     `[:row {:title "Test"}
       ~@(map →clj ["fn?" "ifn?"])]]]])

(def clojure.abstractions
  [:box {:title "Abstractions (<a href=\"https://github.com/cemerick/clojure-type-selection-flowchart\">Clojure type selection flowchart</a>)"}
   [:subsection {:title "Protocols (<a href=\"http://clojure.org/protocols\">clojure.org/protocols</a>)"}
    [:table {}
     [:row {:title "Define"}
      "<code>(</code>"
      (→clj "defprotocol")
      "<code>Slicey (slice [at]))</code>"]

     [:row {:title "Extends"}
      "<code>(</code>"
      (→clj "extend-type")
      "<code>String Slicey (slice [at] ...))</code>"]

     [:row {:title "Extend null"}
      "<code>(</code>"
      (→clj "extend-type")
      "<code>nil Slicey (slice [_] nil))</code>"]

     [:row {:title "Reify"}
      "<code>(</code>"
      (→clj "reify")
      "<code>Slicey (slice [at] ...))</code>"]

     `[:row {:title "Test"}
       ~@(map →clj ["satisfies?" "extends?"])]

     `[:row {:title "Other"}
       ~@(map →clj ["extend" "extend-protocol" "extenders"])]]]

   [:subsection {:title "Records (<a href=\"http://clojure.org/datatypes\">clojure.org/datatypes</a>)"}
    [:table {}
     [:row {:title "Define"}
      "<code>(</code>"
      (→clj "defrecord")
      "<code>Pair [h t])</code>"]

     [:row {:title "Access"}
      "<code>(:h (Pair. 1 2))</code> &rarr; <code>1</code>"]

     `[:row {:title "Create"}
       ~(map code ["Pair." "->Pair" "map->Pair"])]

     [:row {:title "Test"}
      (→clj "record?")]]]

   [:subsection {:title "Types (<a href=\"http://clojure.org/datatypes\">clojure.org/datatypes</a>)"}
    [:table {}
     [:row {:title "Define"}
      "<code>(</code>"
      (→clj "deftype")
      "<code>Pair [h t])</code>"]

     [:row {:title "Access"}
      "<code>(.h (Pair. 1 2))</code> &rarr; <code>1</code>"]

     `[:row {:title "Create"}
       ~(map code ["Pair." "->Pair"])]

     [:row {:title "With methods"}
      "<code>(</code>"
      (→clj "deftype")
      "<code>Pair [h t]<br>&nbsp;&nbsp;Object<br>&nbsp;&nbsp;(toString [this] (str \"<\" h \",\" t \">\")))</code>"]]]

   [:subsection {:title "Multimethods (<a href=\"http://clojure.org/multimethods\">clojure.org/multimethods</a>)"}
    [:table {}
     [:row {:title "Define"}
      "<code>(</code>"
      (→clj "defmulti")
      "<code>my-mm dispatch-fn)</code>"]

     [:row {:title "Method define"}
      "<code>(</code>"
      (→clj "defmethod")
      "<code>my-mm :dispatch-value [args] ...)</code>"]

     `[:row {:title "Dispatch"}
       ~(map →clj ["get-method" "methods"])]

     `[:row {:title "Remove"}
       ~(map →clj ["remove-method" "remove-all-methods"])]

     `[:row {:title "Prefer"}
       ~(map →clj ["prefer-method" "prefers"])]

     `[:row {:title "Relation"}
       ~(map →clj ["derive" "isa?" "parents" "ancestors" "descendants" "make-hierarchy"])]]]])

(def clojure.macros
  [:box {:style "green"}
   [:section {:title "Macros"}
    [:table {}
     `[:row {:title "Create"}
       ~@(map →clj ["defmacro" "definline"])]

     `[:row {:title "Debug"}
       ~@(map →clj ["macroexpand-1" "macroexpand" "clojure.walk/macroexpand-all"])]
     
     `[:row {:title "Branch"}
       ~@(map →clj ["and" "or" "when" "when-not" "when-let"
                    "when-first" "if-not" "if-let" "cond" "condp"
                    "case"])
       "(1.6)"
       ~@(map →clj ["when-some" "if-some"])]
     
     `[:row {:title "Loop"}
       ~@(map →clj ["for" "doseq" "dotimes" "while"])]
     
     `[:row {:title "Arrange"}
       ~@(map →clj [".." "doto" "->" "->>"])
       "(1.5)" ~@(map →clj ["as->" "cond->" "cond->>" "some->" "some->>"])]
     
     `[:row {:title "Scope"}
       ~@(map →clj ["binding" "locking" "time" "with-in-str" "with-local-vars"
                    "with-open" "with-out-str" "with-precision" "with-redefs"
                    "with-redefs-fn"])]]
    
    `[:row {:title "Lazy"}
      ~@(map →clj ["lazy-cat" "lazy-seq" "delay"])]
    
    `[:row {:title "Doc."}
      ~@(map →clj ["assert" "comment" "clojure.repl/doc"])]]])

;; FIXME
(def clojure.reader-macros
  [:box {:title "yellow"}
   [:section {:title "Reader Macros (<a href=\"http://clojure.org/reader#The%20Reader--Macro%20characters\">clojure.org/reader</a>)"}
    [:table {}
     [:row {:title "<code>'</code>"}
      "<a href=\"http://clojure.org/special_forms#quote\">quote</a>: <code>'<var>form</var></code> &rarr; <code>(</code>"
      (→clj "quote")
      "<code><var>form</var>)</code>"]]
    
    [:row {:title "<code>\\</code>"}
     :str "Character literal"]
    [{:latex "\\cmd{;}",
      :html "<code>;</code>"}
     :str "Single line comment"]
    [{:latex "\\cmd{\\^{}}",
      :html "<code>^</code>"}
     :str "Metadata (see Metadata section)"]
    [{:latex "\\cmd{@}",
      :html "<code>@</code>"}
     :cmds '[{:latex "Deref: @form $\\to$ (",
              :html "Deref: <code>@<var>form</var></code> &rarr; <code>(</code>"}
             deref
             {:latex "form)",
              :html "<code><var>form</var>)</code>"}]]
    [{:latex "\\cmd{`}",
      :html "<code>`</code>"}
     :cmds '[{:latex "\\href{http://clojure.org/reader\\#syntax-quote}{Syntax-quote}"
              :html "<a href=\"http://clojure.org/reader#syntax-quote\">Syntax-quote</a>"}]]
    [{:latex "\\cmd{\\textasciitilde}",
      :html "<code>~</code>"}
     :cmds '[{:latex "\\href{http://clojure.org/reader\\#syntax-quote}{Unquote}"
              :html "<a href=\"http://clojure.org/reader#syntax-quote\">Unquote</a>"}]]
    [{:latex "\\cmd{\\textasciitilde@}",
      :html "<code>~@</code>"}
     :cmds '[{:latex "\\href{http://clojure.org/reader\\#syntax-quote}{Unquote-splicing}"
              :html "<a href=\"http://clojure.org/reader#syntax-quote\">Unquote-splicing</a>"}]]
    [{:latex "\\cmd{\\#\"}\\textit{p}\\cmd{\"}",
      :html "<code>#\"<var>p</var>\"</code>"}
     :str {:latex "Regex Pattern \\textit{p}  (see Strings/Regex section)",
           :html "Regex Pattern <var>p</var>  (see Strings/Regex section)"}]
    [{:latex "\\cmd{\\#$'$}",
      :html "<code>#'</code>"}
     :cmds '[{:latex "Var-quote \\#$'$x $\\to$ (",
              :html "Var-quote: <code>#'<var>x</var></code> &rarr; <code>(</code>"}
             var
             {:latex "x)",
              :html "<code><var>x</var>)</code>"}]]
    [{:latex "\\cmd{\\#()}",
      :html "<code>#()</code>"}
     :cmds [
            {:latex "\\href{http://clojure.org/reader\\#The\\%20Reader--Macro\\%20characters}{Anonymous function literal}:"
             :html "<a href=\"http://clojure.org/reader#The%20Reader--Macro%20characters\">Anonymous function literal</a>:"}
            {:latex "\\#(...) $\\to$ (fn [args] (...))",
             :html "<code>#(...)</code> &rarr; <code>(fn [args] (...))</code>"}]]
    [{:latex "\\cmd{\\#\\_}",
      :html "<code>#_</code>"}
     :str "Ignore next form"]]
   ])

;; FIXME
(def clojure.metadata
  [:box "red"
   :section {:latex "Metadata (\\href{http://clojure.org/reader\\#The\\%20Reader--Macro\\%20characters}{clojure.org/reader}, \\href{http://clojure.org/special\\_forms}{special\\_forms})"
             :html "Metadata (<a href=\"http://clojure.org/reader#The%20Reader--Macro%20characters\">clojure.org/reader</a>, <a href=\"http://clojure.org/special_forms\">special_forms</a>)"}
   :table [
           ["General" :cmds [{:latex "\\cmd{\\^{}\\{:key1 val1 :key2 val2 ...\\}}"
                              :html "<code>^{:key1 val1 :key2 val2 ...}</code>"}
                             ]]
           ["Abbrevs" :cmds [{:latex "\\cmd{\\^{}Type} $\\to$ \\cmd{\\^{}\\{:tag Type\\}},
\\cmd{\\^{}:key} $\\to$ \\cmd{\\^{}\\{:key true\\}}"
                              :html
                              "<code>^Type</code> &rarr; <code>^{:tag Type}</code><br>
<code>^:key</code> &rarr; <code>^{:key true}</code>"}
                             ]]
           ["Common" :cmds [{:latex (str
                                     "\\cmd{\\^{}:dynamic} "
                                     "\\cmd{\\^{}:private} "
                                     "\\cmd{\\^{}:doc} "
                                     "\\cmd{\\^{}:const}"
                                     )
                             :html (str
                                    "<code>"
                                    "^:dynamic "
                                    "^:private "
                                    "^:doc "
                                    "^:const"
                                    "</code>")}
                            ]]
           ["Examples" :cmds '[
                               {:latex "\\cmd{(defn \\^{}:private \\^{}String my-fn ...)}"
                                :html "<code>(defn ^:private ^String my-fn ...)</code>"}
                               {:latex " \\ \\ \\ " ; fragile hack to get 2nd example to start on next line
                                :html " <br>"}
                               {:latex "\\cmd{(def \\^{}:dynamic *dyn-var* val)}"
                                :html "<code>(def ^:dynamic *dyn-var* val)</code>"}
                               ]]
           ;;                      ["Others" :cmds [
           ;;                                       {:latex (str
           ;;                                                "\\cmd{:added}"
           ;;                                                " \\cmd{:author}"
           ;;                                                " \\cmd{:doc} "
           ;;                                                " \\cmd{:arglists} "
           ;;                                                " \\cmd{:inline}"
           ;;                                                " \\cmd{:inline-arities}"
           ;;                                                " \\cmd{:macro}"
           ;; ;                                                " (examples in Clojure source)"
           ;; ;                                                " (see Clojure source for examples.  Can use arbitrary keys for your own purposes.)"
           ;;                                                )
           ;;                                        :html (str
           ;;                                               "<code>"
           ;;                                               ":added"
           ;;                                               " :author"
           ;;                                               " :arglists "
           ;;                                               " :doc "
           ;;                                               " :inline"
           ;;                                               " :inline-arities"
           ;;                                               " :macro"
           ;;                                               "</code>"
           ;; ;                                               " (examples in Clojure source)"
           ;; ;                                               " (see Clojure source for examples.  Can use arbitrary keys for your own purposes.)"
           ;;                                               )}
           ;;                                       ]]
           ["On Vars" :cmds '[meta with-meta vary-meta
                              alter-meta! reset-meta!
                              clojure.repl/doc
                              clojure.repl/find-doc test]]
           ]
   ])

(def clojure.special-forms
  [:box "red"
   :section {:latex "Special Forms (\\href{http://clojure.org/special\\_forms}{clojure.org/special\\_forms})"
             :html "Special Forms (<a href=\"http://clojure.org/special_forms\">clojure.org/special_forms</a>)"}
   :cmds-one-line '[def if do let letfn quote var fn loop
                    recur set! throw try monitor-enter monitor-exit]
   :table [[{:latex "\\begin{tabular}[t]{@{}l@{}} Binding Forms / \\\\ Destructuring \\end{tabular}"
             :html "Binding Forms / Destructuring"}
            :cmds '[
                    {:latex "(\\href{http://clojure.org/special\\_forms\\#binding-forms}{examples})"
                     :html "(<a href=\"http://clojure.org/special_forms#binding-forms\">examples</a>)"}
                    let fn defn defmacro
                    loop for doseq if-let when-let
                    "(1.6)" if-some when-some]]
           ]
   ])

;; FIXME
(def clojure.vars
  [:box "blue2"
   :section {:latex "Vars and global environment (\\href{http://clojure.org/vars}{clojure.org/vars})"
             :html "Vars and global environment (<a href=\"http://clojure.org/vars\">clojure.org/vars</a>)"}
   :table [["Def variants" :cmds '[def defn defn- definline defmacro
                                   defmethod defmulti defonce
                                   defrecord]]
           ["Interned vars" :cmds '[declare intern binding
                                    find-var var]]
           ["Var objects" :cmds '[with-local-vars var-get var-set
                                  alter-var-root var? bound?
                                  thread-bound?]]
           ["Var validators" :cmds '[set-validator! get-validator]]
           ;; Now covered in Metadata section
           ;;                      ["Var metadata" :cmds '[meta clojure.repl/doc
           ;;                                              clojure.repl/find-doc test]]
           ]])

;; FIXME
(def clojure.namespaces
  [:box "yellow"
   :section "Namespace"
   :table [["Current" :cmds '[*ns*]]
           ["Create/Switch" :cmds '[{:latex "(\\href{http://blog.8thlight.com/colin-jones/2010/12/05/clojure-libs-and-namespaces-require-use-import-and-ns.html}{tutorial})"
                                     :html "(<a href=\"http://blog.8thlight.com/colin-jones/2010/12/05/clojure-libs-and-namespaces-require-use-import-and-ns.html\">tutorial</a>)"}
                                    ns in-ns create-ns]]
           ["Add" :cmds '[alias def import intern refer]]
           ["Find" :cmds '[all-ns find-ns]]
           ;;                      ["Examine" :cmds '[ns-name ns-aliases ns-map
           ;;                                         ns-interns ns-publics ns-refers
           ;;                                         ns-imports]]
           ["Examine" :cmds '[[:common-prefix ns-
                               name aliases map interns publics
                               refers imports]]]
           ["From symbol" :cmds '[resolve ns-resolve namespace
                                  the-ns]]
           ["Remove" :cmds '[ns-unalias ns-unmap remove-ns]]]])

;; FIXME
(def clojure.loading
  [:box "green"
   :section "Loading"
   :table [["Load libs" :cmds '[{:latex "(\\href{http://blog.8thlight.com/colin-jones/2010/12/05/clojure-libs-and-namespaces-require-use-import-and-ns.html}{tutorial})"
                                 :html "(<a href=\"http://blog.8thlight.com/colin-jones/2010/12/05/clojure-libs-and-namespaces-require-use-import-and-ns.html\">tutorial</a>)"}
                                require use import refer]]
           ["List loaded" :cmds '[loaded-libs]]
           ["Load misc" :cmds '[load load-file load-reader
                                load-string]]]])

;; FIXME
(def clojure.concurrency
  [:box "magenta"
   :section "Concurrency"
   :table [["Atoms" :cmds '[atom swap! reset! compare-and-set!]]
           ["Futures" :cmds '[future
                              [:common-prefix future-
                               call done? cancel cancelled?]
                              future?]]
           ["Threads" :cmds '[bound-fn bound-fn*
                              [:common-suffix -thread-bindings
                               get push pop]
                              thread-bound?]]
           ["Misc" :cmds '[locking pcalls pvalues pmap seque
                           promise deliver]]]
   :subsection {:latex "Refs and Transactions (\\href{http://clojure.org/refs}{clojure.org/refs})"
                :html "Refs and Transactions (<a href=\"http://clojure.org/refs\">clojure.org/refs</a>)"}
   :table [["Create" :cmds '[ref]]
           ["Examine"
            :cmds '[deref "@"
                    {:latex "\\textmd{\\textsf{(@form $\\to$ (deref form))}}",
                     :html "(<code>@<var>form</var></code> &rarr; <code>(deref <var>form</var>)</code>)"}]]
           ["Transaction" :cmds '[sync dosync io!]]
           ["In transaction" :cmds '[ensure ref-set alter commute]]
           ["Validators" :cmds '[set-validator! get-validator]]
           ["History" :cmds '[ref-history-count
                              [:common-prefix-suffix
                               ref- -history min max]]]]
   :subsection {:latex "Agents and Asynchronous Actions (\\href{http://clojure.org/agents}{clojure.org/agents})"
                :html "Agents and Asynchronous Actions (<a href=\"http://clojure.org/agents\">clojure.org/agents</a>)"}
   :table [["Create" :cmds '[agent]]
           ["Examine" :cmds '[agent-error]]
           ["Change state" :cmds '[send send-off restart-agent
                                   "(1.5)"
                                   send-via set-agent-send-executor!
                                   set-agent-send-off-executor!]]
           ["Block waiting" :cmds '[await await-for]]
           ["Ref validators" :cmds '[set-validator! get-validator]]
           ["Watchers" :cmds '[add-watch remove-watch]]
           ["Thread handling" :cmds '[shutdown-agents]]
           ["Error" :cmds '[error-handler set-error-handler!
                            error-mode set-error-mode!]]
           ["Misc" :cmds '[*agent* release-pending-sends]]]])

;; FIXME
;; FIXME: Clojurescript?
;; FIXME: ClojureCLJ?
(def clojure.interop
  [:box "orange"
   :section {:latex "Java Interoperation (\\href{http://clojure.org/java\\_interop}{clojure.org/java\\_interop})"
             :html "Java Interoperation (<a href=\"http://clojure.org/java_interop\">clojure.org/java_interop</a>)"}
   :table [["General" :cmds '[.. doto "Classname/" "Classname."
                              new bean comparator enumeration-seq
                              import iterator-seq memfn set! class
                              class? bases supers type]]
           ["Cast" :cmds '[boolean byte short char int long
                           float double bigdec bigint num cast
                           biginteger]]
           ["Exceptions" :cmds '[throw try catch finally
                                 clojure.repl/pst
                                 "(1.4)" ex-info ex-data]]]
   :subsection "Arrays"
   :table [["Create" :cmds '[make-array
                             [:common-suffix -array object
                              boolean byte short char int long
                              float double]
                             aclone to-array to-array-2d into-array]]
           ["Use" :cmds '[aget aset
                          [:common-prefix aset- boolean byte short
                           char int long float double]
                          alength amap areduce]]
           ;; TBD: This would be a good place to give an
           ;; example like ^"[Ljava.lang.BigInteger", yes?
           ;; Also the cast ^objects?  Is there a doc page
           ;; for that?
           ["Cast" :cmds '[booleans bytes shorts chars
                           ints longs floats doubles]]
           ]
   :subsection {:latex "Proxy (\\href{https://github.com/cemerick/clojure-type-selection-flowchart}{Clojure type selection flowchart})"
                :html "Proxy (<a href=\"https://github.com/cemerick/clojure-type-selection-flowchart\">Clojure type selection flowchart</a>)"}
   :table [["Create" :cmds '[proxy get-proxy-class
                             [:common-suffix -proxy
                              construct init]]]
           ["Misc" :cmds '[proxy-mappings proxy-super update-proxy]]]])

;; FIXME
(def clojure.msc
  [:box {:style "green2"}
   [:section {:title "Other"}
    [:table {}
     `[:row {:title "XML"}
       ~@(map →clj ["clojure.xml/parse" "xml-seq"])]

     `[:row {:title "REPL"}
       ~@(map →clj ["*1" "*2" "*3" "*e" "*print-dup*" "*print-length*" "*print-level*" "*print-meta*" "*print-readably*"])]

     `[:row {:title "Code"}
       ~@(map →clj ["*compile-files*" "*compile-path*" "*file*" "*warn-on-reflection*" "compile" "gen-class" "gen-interface" "loaded-libs" "test"])]

     `[:row {:title "Misc"}
       ~@(map →clj ["eval" "force" "hash" "name" "*clojure-version*" "clojure-version" "*command-line-args*"])]

     `[:row {:title "Browser / Shell"}
       ~@(map →clj ["clojure.java.browse/browse-url"
                    "clojure.java.shell/sh"
                    "clojure.java.shell/with-sh-dir"
                    "clojure.java.shell/with-sh-env"])]]]])

;; The Cheatsheet
;;------------------------------------------------------------------------------

(defn cheatsheet []
  (layout ;; FIXME: is this a standard layout instance?
   (cfg/site-config)
   [:h1 {:class "page-title"} "FIXME"]
   ))