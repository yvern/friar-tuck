(ns tuck.cellar)

#?(:clj (require '[somnium.congomongo :as mongo]
                 '[cheshire.core :refer :all]))


(defonce DB (atom nil))


(defn config-db!
  "Configures access to database 'db' (must be an atom) for the browser/js (TaffyDB) and server/jvm (MongoDB).
   Recieves a map in the format '{:db db :dbname dbname :coll coll :port port :host host}'.
   In the browser/js: 'dbname' points to/creates a database in TaffyDB.
   In the server/jvm: both 'dbname' and 'coll' are required, being 'dbname' the database name
   and 'coll' a collection in MongoDB. For MongoDB, 'db' turns into a 2-vector, being [('connection to db') ('collection')].
   If 'db' is omited, a pre-defined atom, named 'DB', holds the configured database."

   [{db :db dbname :dbname coll :coll port :port host :host}]

  #?(:clj (do (reset! (or db DB) [(mongo/make-connection dbname
                                            :host (or host "127.0.0.1")
                                            :port (or port 27017))
                          (keyword coll)]))

     :cljs (do (reset! (or db DB) (js/eval "TAFFY([])"))
               (. (or db DB) (store dbname)))))


(defn insert!
  "Recieves a map in the format '{:db db :coll coll :data data :kind kind}',
   where 'db' is the database to be inserted (default is the DB atom refering to MongoDB on the server/jvm)
   and 'coll' the collection (default is taken form DB too). On the browser/js, 'db' is the atom refering
   to the TaffyDB database (also by default taken from DB atom).
   It can receive ONE of the following: a Clojure data structure 'data',
   an edn string 'edn', XOR a JSON string 'json'."

  [{db :db coll :coll data :data json :json edn :edn}]

  (let [json (and json #?(:clj (parse-string json)
                          :cljs (->> json (.parse js/JSON) js->clj)))
        edn (and edn (read-string edn))
        obj (or data edn json)]

    #?(:clj (mongo/insert! (or (first (deref db)) (first (deref DB)))
                       (keyword (or coll (second (deref db)) (second (deref DB))))
                         obj)

       :cljs (. (or db (deref DB)) (insert (clj->js obj))))))


(defn search
  "Recieves a map in the format '{:db db :coll coll :query query}',
   where 'db' is the database and 'coll' the colletion in which to perform the query 'query'.
   The 'query' param should be a hash-map too. On the server/jvm 'db' and 'coll' both default
   to the MongoDB database and collection defined in 'config-db!'.
   On the client/js no 'db' is needed, just the 'coll' for TaffyDB."

  [{db :db coll :coll query :query}]

  #?(:clj (mongo/with-mongo (or (first (deref db)) (first (deref DB)))
                                (mongo/fetch
                                  (keyword (or coll (second (deref db)) (second (deref DB))))
                                    :where query))

     :cljs (map js->clj (. ((or db (deref DB)) (clj->js (or query {}))) (get)))))


(defn update!
  "Recieves a map in the format '{:db db :coll coll :query query :data data}',
   being 'db' the database and 'coll' the collection to look for 'query' entry.
   In the server/jvm acts on a MongoDB database and collection, in the browser/js on a TaffyDB collection.
   A Clojure data structure 'data', describing the data to update/merge with the result of searching for 'query'."

  [{db :db coll :coll query :query data :data}]

  #?(:clj (mongo/with-mongo (or (first (deref db)) (first (deref DB)))
                                (mongo/update!
                                  (keyword (or coll (second (deref db)) (second (deref DB))))))

     :cljs (. ((or db (deref DB)) (clj->js (or query {}))) (update data))))


(defn delete!
  "Recieves a map in the format '{:db db :coll coll :query query}', being 'db' the database
   and 'coll' the collection to look for 'query' entry.
   In the server/jvm acts on a MongoDB database and collection, in the browser/js on a TaffyDB collection.
   A Clojure data structure 'query', describing the data to delete/remove."

  [{db :db coll :coll query :query}]

  #?(:clj (mongo/with-mongo (or (first (deref db) (first (deref DB))))
                            (mongo/destroy!
                              (keyword (or coll (second (deref db)) (second (deref DB))))
                                query))

     :cljs (. ((or db (deref DB)) (clj->js query)) (remove))))


(defn replace!
  "Recieves a map in the format '{:db db :coll coll :query query :jsonquery jsonquery :ednquery ednquery
   :data data :json json :edn edn}', being 'db' the database and 'coll' the collection
   where to look for the 'query'/'jsonquery'/'ednquery' entry. Only one should be supplied, as it performs a search.
   In the server/jvm acts on a MongoDB database and collection, in the browser/js on a TaffyDB collection.
   Only ONE of the following should be supplied: a Clojure data structure 'data',
   an edn string 'edn' XOR a JSON string 'json'
   describing the data to replace (basically (do (delete! {...}) (insert! {...}))"

  [{db :db coll :coll query :query data :data json :json edn :edn}]

  (do
    (delete! {:db db :coll coll :query query})
    (insert! {:db db :coll coll :data data :json json :edn edn})))
