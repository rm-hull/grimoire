(ns grimoire.web.layout
  (:require [hiccup.page :as page]
            [grimoire.web.config :as cfg]
            [grimoire.web.caches :as c]))

(defn header [{:keys [base-url css] :as c}]
  [:head
   [:link {:rel "profile" :href "http://gmpg.org/xfn/11"}]
   [:meta {:content "IE=edge" :http-equiv "X-UA-Compatible"}]
   [:meta {:content "text/html; charset=utf-8" :http-equiv "content-type"}]
   [:meta {:content "width=device-width initial-scale=1.0 maximum-scale=1":name "viewport"}]
   (when-let [pg (:page c)]
     (list
      (when-let [description (:description pg)]
        [:meta {:name "description"
                :content description}])
      (when-let [summary (:summary pg)]
        [:meta {:name "summary"
                :content summary}])))
   [:title (-> c :style :title)]
   (page/include-css
    (str base-url "public/css/poole.css")
    (str base-url "public/css/syntax.css")
    (str base-url "public/css/lanyon.css")
    (str base-url "public/css/headings.css"))
   (map page/include-css css)
   [:link {:href "https://fonts.googleapis.com/css?family=PT+Serif:400,400italic,700|PT+Sans:400"
           :rel "stylesheet"}]
   [:link {:href (str base-url "public/apple-touch-icon-precomposed.png") :sizes "144x144" :rel "apple-touch-icon-precomposed"}]
   [:link {:href (str base-url "public/favicon.ico") :rel "shortcut icon"}]])

(defn masthead [{:keys [base-url funding-flag] :as c}]
  [:div.masthead
   [:div.container
    [:h3.masthead-title
     [:a {:title "Home" :href base-url} (-> c :style :title)]
     [:small]]]])

(defn sidebar [{:keys [repo description author version year]}]
  (list
   [:input#sidebar-checkbox.sidebar-checkbox {:type "checkbox"}]
   [:div#sidebar.sidebar
    [:div.sidebar-item [:p description]]
    [:nav.sidebar-nav
     [:br] "Artifacts" [:br]
     (let [cfg   (cfg/site-config)
           linkf (fn [v]
                   [:a.sidebar-nav-item
                    {:href (format "%sorg.clojure/clojure/%s/" (:store-url cfg) v)}
                    (str "Clojure " v)])]
       (list
        [:a.sidebar-nav-item {:href (:store-url cfg)} "Artifact store"]
        (map linkf (c/clj-versions))))

     [:br] "About" [:br]
     [:a.sidebar-nav-item {:href "/api"} "API"]
     [:a.sidebar-nav-item {:href "/about"} "About"]
     [:a.sidebar-nav-item {:href "/contributing"} "Contributing"]

     [:br] "Bits and Bats" [:br]
     [:a.sidebar-nav-item {:href "/heatmap"} "Heatmap"]
     [:a.sidebar-nav-item {:href "/worklist"} "Worklist"]

     [:br] "Me" [:br]
     (let [{:keys [gittip me]} author]
       (list
        [:a.sidebar-nav-item {:href repo} "Github Repo"]
        [:a.sidebar-nav-item {:href me} "About Me"]
        [:a.sidebar-nav-item {:href gittip} "Support This Project"]))]
    [:div.sidebar-item
     [:p "Currently v" version]]
    [:div.sidebar-item
     [:p "© " year ". All rights reserved."]]]))

(defn foot [{:keys [google-analytics-id js]}]
  [:footer
   [:script {:type "text/javascript" :src "/public/jquery.js"}]
   [:script {:type "text/javascript" :src "/public/sidebar.js"}]
   [:script {:type "text/javascript" :src "/public/funding.js"}]
   (map #(vector :script {:type "text/javascript" :src %}) js) 
   [:script {:type "text/javascript"}
    "var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '" google-analytics-id "']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();"]])

(defn layout
  [page & content]
  (page/html5
   (header page)
   [:body
    (sidebar page)
    [:div.wrap
     (masthead page)
     [:div {:id    "funding"
            :style "padding:0.25em;background-color:#3EC53E;color:white;display:none;"}
      [:center
       [:h2 "Hey listen!"]
       [:p {:style "color:#313131;"}
        "It looks like you're getting some use out of Grimoire. Great!" [:br]
        "Please " [:a {:style "color:#A300CC;" :onclick "hasFunded()" :href "/funding"}
                   [:b "help me keep it online"]] [:br]
        "Or " [:a {:style "color:#A300CC;" :onclick "closeFunding()"}
               [:b "make this go away " [:span {:style "font-size:0.25em;"} "(for now)"]]]]]]

     [:div {:class "container content"
            :style "margin-top: 3em;"}
      (if (:page page)
        [:div {:class "page"}
         content]
        content)]]
    [:label.sidebar-toggle {:for "sidebar-checkbox"}]]
   (foot page)))
