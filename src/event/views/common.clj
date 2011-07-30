(ns event.views.common
  (use noir.core
       hiccup.core
       hiccup.page-helpers))

(defpartial layout [& content]
  (html5
   [:head
    [:title "event"]
    (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js")
    (include-js "/js/event.js")
    (include-css "/css/reset.css")
    (include-css "/css/event.css")]
	      
   [:body content]))

