<p><a href="https://commons.wikimedia.org/wiki/File:The_friar_took_Robin_on_his_back_by_Louis_Rhead_1912.png#/media/File:The_friar_took_Robin_on_his_back_by_Louis_Rhead_1912.png"><img src="https://upload.wikimedia.org/wikipedia/commons/5/5a/The_friar_took_Robin_on_his_back_by_Louis_Rhead_1912.png" alt="The friar took Robin on his back by Louis Rhead 1912.png"></a><br>By <a href="https://en.wikipedia.org/wiki/en:Louis_Rhead" class="extiw" title="w:en:Louis Rhead">Louis Rhead</a> - Rhead, Louis. "Bold Robin Hood and His Outlaw Band: Their Famous Exploits in Sherwood Forest". New York: Blue Ribbon Books, 1912., Public Domain, <a href="https://commons.wikimedia.org/w/index.php?curid=1139258">Link</a></p>

# Friar Tuck: WIP

Just as Sam did all the heavy lifting in the background for Frodo without taking enough credit, Friar Tuck helped Robin Hood.

Also, me and Tuck have more in common: we are both meadmakers!

Right now, tuck can help you manage your goodies cellar.

Enough about flavor...

friar-tuck is a Clojure(Script) library to help you solve the same problems I had myself.

As of now, only cellar.cljc works. It gives you a unified api to use [TaffyDB](http://taffydb.com/) while on the browser/cljs/js (you still need to add taffydb.min.js yourself), or [MongoDB](https://www.mongodb.com/) while on the server/clj/jvm (via [congomongo](https://github.com/aboekhoff/congomongo)).

I tried to keep it as simple as possible for new users/learners, if you need advanced features or other databases, I can't implement them right now, but help is welcome!

Why [TaffyDB](http://taffydb.com/)?
1. It is a simple and easy to use javascript database.
2. It uses HTML5 LocalStorage for persistence.
3. It works without callback orgy, just invoke and get results.
4. It also runs on NodeJS (as long as you also add the npm package).

Why [MongoDB](https://www.mongodb.com/)?
1. NoSQL
2. Easy and simple to use/setup.
3. Everyone is using it (maybe because of 2).
4. I like green (matches Robin Hood theme).

Why [congomongo](https://github.com/aboekhoff/congomongo)?
1. It worked just right for my past uses, so why not?



## Usage

I believe docs are straightforward (docstrings, you can see them in the repl calling (doc function)), but examples are never too much:

```Clojure

(ns your-namespace
  (:require [tuck.cellar :as tc]))

(def mydb (atom nil))

;; Clojure/JVM/MongoDB
(tc/config-db! {:db mydb :dbname "mydb" :coll "mycoll" :port 8080})

;; ClojureScript/JS/TaffyDB
(tc/config-db! {:db mydb :dbname "mydb"}) ;; no need for :coll or :port


;; Once mydb refers to our database, the api is the same:

(tc/insert! {:db mydb :data {:a "A" "1" [1 2 3] "B" {:b1 1 :b2 2}}})

(tc/insert! {:db mydb :json "{'1':[1,2,3],'a':'A','B':{'b1':1,'b2':2}}"})

(tc/search {:db mydb :query {:a "A"}}) ;; returns the result from querry

(tc/update! {:db mydb :query {:a "A"} :data {:A ["a" "b"]}}) ;; update! actually merges the result with the value of :data key

(tc/delete! {:db mydb :query {:a "A"}})

(tc/replace! {:db mydb :query {:a "A"} :data {"dog" "nice" "yes" [666]}}) ;; replace! deletes the query result and then inserts the value from :data key

```

Right now, only single result/exact queries were tested.

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
