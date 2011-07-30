(ns event.views.welcome
  (:require [event.views.common :as common]
            [noir.content.pages :as pages]
            [noir.validation :as validation]
	    [noir.response :as response]
	    [noir.session :as session]
	    [event.models.event :as model]
	    [hiccup.form-helpers :as form])
  (use noir.core
       hiccup.core
       hiccup.page-helpers))

(defn- format-date [date]
  (let [format (java.text.SimpleDateFormat. "EEE, dd MMM yy")]
    (.format format date)))


(defn- input-field
  ([name label value type] (input-field name label value type {}))
  ([name label value type attrs]
     (let [tvalue (or value "")]
       [:div.underlaid
	[:label {:for name :style (str "display: " (if (empty? tvalue) "block" "none"))} label]
	[(if (= type "textarea") :textarea :input)
	 (merge {:type type :name name :id name :onfocus "makeLabelOpaque(this);" :onblur "makeLabelSolid(this);" :onkeydown "hideLabel(this);" :value tvalue}
		attrs)]])))

(defn- text-input
  ([name label] (text-input name label nil))
  ([name label value] (input-field name label value "text")))

(defn- text-area
  ([name label] (text-area name label nil))
  ([name label value] (input-field name label value "textarea" {:rows 5 :cols 40})))

(def nav-admin
     [:nav
      [:span [:a {:href "/admin/event/new"} "New Event" ]]
      [:span [:a {:href "/admin/event/list"} "List Events" ]]
      [:br {:style "clear:both"}]])

(defpage "/event/:id" {:keys [id email diet]}
  (if-let [event (model/find-event id)]
    (let [tickets (- (:tickets event) (model/tickets-used id))]
      (common/layout
       [:div
	[:h2 (:name event)]
	[:p (format-date (:date event))]
	[:p (cond
	     (= 0 tickets) "The event is all full. Sorry!"
	     (= 1 tickets) "Just a single place left."
	     :otherwise (str "There are " tickets " places left."))]
	[:p (:description event)]]

       (when (> tickets 0)
	 [:form#signup {:method "post"}
	  [:h2 "Signup"]
	  (if-let [error (first (validation/get-errors :email))]
	    [:span.validation error])
	  (text-input "email" "Email" email)
	  (text-input "diet" "Dietary requirement" diet)
	  (form/submit-button "Register")])))
    
    (common/layout
     [:div
      [:h2 "No such event!"]])))

(defpage [:post "/event/:id"] {:keys [id email diet]}
  (if (model/has-event? id)
    (do
      (validation/rule (validation/is-email? email) [:email "Please enter a valid email address"])
      (if (validation/errors? :email)
	(render [:get (str "/event/:id")] {:id id :email email :diet diet})
	(do
	  (model/register id email diet)
	  (response/redirect (str "/event/" id)))))
    (common/layout
     [:h2 "No such event!"])))

(defpage "/" []
  (response/redirect "/event/list"))

(defn- list-events [show-admin-links]
  (common/layout
   (when show-admin-links nav-admin)
   [:table
    [:tr
     [:th "Name"]
     [:th "Date"]
     [:th "Tickets Left"]]
    (map (fn [event]
	   [:tr
	    [:td (if show-admin-links
		   [:a {:href (str "/admin/event/show/" (:id event))} (:name event)]
		   [:a {:href (str "/event/" (:id event))} (:name event)])]
	    [:td (format-date (:date event))]
	    [:td (- (:tickets event) (model/tickets-used (:id event)))]])
	 (model/list-events))]))

(defpage "/event/list" []
  (list-events false))


(defpage "/admin/event/new" []
  (common/layout
   nav-admin
   [:h2 "Create new event"]
   [:form {:method "post"}
    (text-input "name" "Name")
    (text-input "date" "DD/MM/YYYY")
    (text-input "tickets" "Tickets")
    (text-area "description" "Description")
    (form/submit-button "Create")]))

(defpage [:post "/admin/event/new"] {:keys [name date description tickets]}
  (let [[day month year] (map #(Integer/parseInt %) (seq (.split date "/")))
	calendar (java.util.Calendar/getInstance)
	tickets (Integer/parseInt tickets)]
    (.set calendar year month day)
    (model/create-event name
			(.getTime calendar)
			tickets
			description)
    (response/redirect "/admin/event/list")))

(defpage "/admin/event/show/:id" {idstr :id}
  (if-let [event (model/find-event idstr)]
    (let [tickets (model/tickets-for-event idstr)]
      (common/layout
       nav-admin
       [:h2 (:name event)]
       [:p "Date: " (format-date (:date event))]
       [:p "Tickets: " (:tickets event)]
       [:p (:description event)]

       [:div#registrations
	(if (empty? tickets)
	  [:p "No registrations yet."]
	  [:table
	   [:tr
	    [:th "Email"]
	    [:th "Diet"]]
	   (map (fn [t] [:tr [:td (:email t)] [:td (:diet t)]]) tickets)])]))
    (common/layout
     nav-admin
     [:h2 "No such event"])))

(defpage "/admin/event/list" []
  (list-events true))






