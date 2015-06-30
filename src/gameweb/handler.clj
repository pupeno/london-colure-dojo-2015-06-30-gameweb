(ns gameweb.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.util.response :refer [redirect-after-post]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.core :as h]
            [hiccup.form :as form]))

(def users (atom {}))

(def badges {:logged-in    {},
             :pizza-eater  {},
             :beer-drinker {},
             :clojurian    {}
             :idea-smith   {}})

(defn assign-badge [username badge-name]
  (when badge-name
    (swap! users (fn [user-map]
                   (update-in user-map [username :badges] #(into #{badge-name} %))))))

(defn keywords->sentence [ks]
  (clojure.string/join ", " (map name ks)))


(defn homepage [username]
  (assign-badge username :logged-in)

  (h/html [:h1 "Hello " username]
          [:p "Badges available: " (keywords->sentence (keys badges))]
          [:p "You have these badges: " (keywords->sentence (:badges (@users username)))]
          (form/form-to [:post "/"]
                        (anti-forgery-field)
                        (form/hidden-field "username" username)
                        (map (fn [badge] (form/submit-button {:name "badge-name"} badge)) (keys badges)))))


(defroutes app-routes
           (GET "/" [username] (homepage username))
           (POST "/" [username badge-name] (do (assign-badge username (keyword badge-name))
                                               (redirect-after-post (str "/?username=" username))))
           (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
