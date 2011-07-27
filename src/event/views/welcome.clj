(ns event.views.welcome
  (:require [event.views.common :as common]
            [noir.content.pages :as pages]
            [noir.validation :as validation]
	    [noir.response :as response]
	    [noir.session :as session])
  
  (use noir.core
       clojure.contrib.sql
       hiccup.core
       hiccup.page-helpers))

(def atendees (atom
	       (read-string
		(slurp (java.io.File. "data.txt")))))


(defn add-atendee [attributes] 
  (swap! atendees conj attributes)
  (spit (java.io.File. "data.txt")
	(prn-str @atendees)))

(def max-participants 5)


(defpage "/welcome" []
         (common/layout
           [:p "Welcome to event"]))

(defpage "/" []
   (common/layout
     [:h2 "London Clojure Dojo"]
     (let [spaces (- max-participants (count @atendees))]
       (if (> spaces 0)
         [:div
         (if-let [flash (session/get :flash)] (do (session/remove! :flash) [:p flash]))
           [:form {:method "post" }  
            [:label "Email"]
            [:input {:type "text" :name "email"}]
            [:br]
            [:label {:for "special-requirements"} "Special requirements"]
            [:br]
            [:textarea {:name "special-requirements"}]
            [:br]
            [:input {:type :submit} ]
            [:p "There " (if (= spaces 1) " is " " are ") spaces (if (= spaces 1) " space" " spaces")  " remaining"]]]
          [:p "Sorry, there are no more spaces"]))))

(defpage "/registered" []
  (common/layout
   [:p "Welcome " (:email (last @atendees))]))

(defpage "/admin" []
  (common/layout
    [:table
      [:tr
        [:td "Email"]
        [:td "Special requirements"]
      ]
    (map (fn [atendee]
      [:tr
        [:td (atendee :email)]
        [:td (atendee :special-requirements)]
      ]) @atendees )]))


(defpage [:post "/"] params
  (if (validation/is-email? (params :email))
    (do
      (add-atendee params)
      (response/redirect "/registered")
    )
    (do
      (session/put! :flash "invalid email")
      (response/redirect "/"))))



