(ns event.models.event
  (:use clojure.contrib.sql))

(def connection-props
     {:classname "org.h2.Driver"
      :subprotocol "h2"
      :subname "test.db"})

(with-connection connection-props
  (let [table-names (into #{} (with-query-results res
			  ["SHOW TABLES"]
			  (doall (map :table_name res))))]
    (transaction
     (when-not (table-names "TICKET")
       (create-table "public.ticket"
		     [:id :bigint "GENERATED BY DEFAULT AS IDENTITY" "PRIMARY KEY"]
		     [:event_id :bigint]
		     [:diet "varchar(400)"]
		     [:email "varchar(400)"]))

     (when-not (table-names "EVENT")
       (create-table "public.event"
		     [:id :bigint "GENERATED BY DEFAULT AS IDENTITY" "PRIMARY KEY"]
		     [:name "varchar(200)"]
		     [:date "date"]
		     [:description "text"]
		     [:tickets :int])))))

(defn create-event [name date tickets description]
  (with-connection connection-props
    (transaction
     (insert-records "public.event"
		     {:name name :description description :date date :tickets tickets}))))

(defn list-events []
  (with-connection connection-props
    (with-query-results res
      ["SELECT * FROM public.event"]
      (into [] res))))

(defn find-event [id]
  (with-connection connection-props
    (with-query-results res
      ["SELECT * FROM public.event WHERE id=?" id]
      (if (empty? res)
	nil
	(update-in (first res)
		   [:description]
		   #(slurp (.getCharacterStream %)))))))

(defn has-event? [id]
  (with-connection connection-props
    (with-query-results res
      ["SELECT id FROM public.event WHERE id=?" id]
      (not (empty? res)))))

(defn register [id email diet]
  (with-connection connection-props
    (insert-records "public.ticket"
		    {:event_id id :email email :diet diet})))

(defn tickets-used [id]
  (with-connection connection-props
    (with-query-results res
      ["SELECT COUNT(*) FROM public.ticket WHERE event_id=?" id]
      ((keyword "count(*)") (first res)))))


(defn tickets-for-event [id]
  (with-connection connection-props
    (with-query-results res
      ["SELECT * FROM public.ticket WHERE event_id=?" id]
      (into [] res))))
