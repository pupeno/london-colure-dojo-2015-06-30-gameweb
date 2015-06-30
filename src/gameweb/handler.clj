(ns gameweb.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.util.response :refer [redirect-after-post]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.core :as h]
            [hiccup.form :as form]
            [hiccup.element :as element]))

(def users (atom {}))

(def badges {:logged-in    {:name        "Logged in",
                            :description "You logged in into the app for the first time",
                            :image       "http://png-1.findicons.com/files/icons/990/vistaico_toolbar/128/login.png"},
             :pizza-eater  {:name        "Pizza eater"
                            :description "You eat pizza"
                            :image       "https://s3.amazonaws.com/ODNUploads/5431308c0d073Cheese_Pizza_Pepperoni.jpg"},
             :beer-drinker {:name        "Beer"
                            :description "Got drunk"
                            :image       "http://i.huffpost.com/gen/1294334/images/o-JUST-ADD-WATER-BEER-facebook.jpg"},
             :clojurian    {:name        "Clojure"
                            :description "Best language EVER!"
                            :image       "http://verse.aasemoon.com/images/thumb/5/51/Clojure-Logo.png/250px-Clojure-Logo.png"}
             :idea-smith   {:name        "Idea smith"
                            :description "You generate ideas"
                            :image       "http://a5.files.biography.com/image/upload/c_fill,cs_srgb,dpr_1.0,g_face,h_300,q_80,w_300/MTIwNjA4NjMzNTM4NTEyMzk2.jpg"}})

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
          (if username
            [:div
             [:p "You have these badges: " (keywords->sentence (:badges (@users username)))]
             (map #(element/image {:width 140 :height 140} (:image (badges %))) (:badges (@users username)))
             (form/form-to [:post "/"]
                           (anti-forgery-field)
                           (form/hidden-field "username" username)
                           (map (fn [badge] (form/submit-button {:name "badge-name"} badge)) (keys badges)))]
            [:div
             (form/form-to [:get "/"]
                           [:span "Username"]
                           (form/text-field "username")
                           (form/submit-button "Log in"))])))


(defroutes app-routes
           (GET "/" [username] (homepage username))
           (POST "/" [username badge-name] (do (assign-badge username (keyword badge-name))
                                               (redirect-after-post (str "/?username=" username))))
           (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
