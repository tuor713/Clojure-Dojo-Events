(ns event.views.common
  (use noir.core
       hiccup.core
       hiccup.page-helpers))

(defpartial layout [& content]
            (html5
              [:head
               [:title "event"]
               (include-css "/css/reset.css")]
              [:body
               [:nav
                
                  [:span [:a {:href "/admin"} "Admin" ]]
                  [:span [:a {:href "/"} "Register" ]]
                
               [:br {:style "clear:both"}]]
               [:div#wrapper
                content]]))
