(ns cemerick.url
  (:require
    [clojure.string :as string]
    [pathetic.core :as pathetic])
  #?(:cljs (:require [goog.Uri :as uri])
     :clj  (:import
             (java.net URLEncoder URLDecoder))))

(defn url-encode
  [string]
  (some-> string
          str
          #?(:clj (URLEncoder/encode "UTF-8") :cljs (js/encodeURIComponent))
          (.replace "+" "%20")))

#?(:clj  (defn url-decode
           ([string] (url-decode string "UTF-8"))
           ([string encoding]
            (some-> string str (URLDecoder/decode encoding))))

   :cljs (defn url-decode
           [string]
           (some-> string str (js/decodeURIComponent))))

(defn map->query [m]
  (some->> (seq m)
           sort
           (map (fn [[k v]]
                  [(url-encode (name k))
                   "="
                   (url-encode (str v))]))
           (interpose "&")
           flatten
           (apply str)))

(defn split-param [param]
  (->
    (string/split param #"=")
    (concat (repeat ""))
    (->> (take 2))))

(defn reduce-params [params]
  (reduce
    (fn [m [k v]]
      (if-let [existing-v (get m k)]
        (if (coll? existing-v)
          (update m k conj v)
          (assoc m k [existing-v v]))
        (assoc m k v)))
    {}
    (partition 2 params)))

(defn query->map [qstr]
  (when (not (string/blank? qstr))
    (some->> (string/split qstr #"&")
             seq
             (mapcat split-param)
             (map url-decode)
             reduce-params)))

(defn- port-str [protocol port]
  (when (and (not= nil port)
          (not= -1 port)
          (not (and (== port 80) (= protocol "http")))
          (not (and (== port 443) (= protocol "https"))))
    (str ":" port)))

(defn- url-creds [username password]
  (when username
    (str username ":" password)))

(defrecord URL [protocol username password host port path query anchor]
  Object
  (toString [this]
    (let [creds (url-creds username password)]
      (str protocol "://"
        creds
        (when creds \@)
        host
        (port-str protocol port)
        path
        (when (seq query) (str \? (if (string? query)
                                    query
                                    (map->query query))))
        (when anchor (str \# anchor))))))

#?(:cljs (defn translate-default [s old-default new-default]
           (if (= s old-default)
             new-default
             s)))

#?(:clj  (defn- url* [url]
           (let [url (java.net.URL. url)
                 [user pass] (string/split (or (.getUserInfo url) "") #":" 2)]
             (URL. (.toLowerCase (.getProtocol url))
               (and (seq user) user)
               (and (seq pass) pass)
               (.getHost url)
               (.getPort url)
               (pathetic/normalize (.getPath url))
               (query->map (.getQuery url))
               (.getRef url))))

   :cljs (defn- url* [url]
           (let [url (goog.Uri. url)
                 [user pass] (string/split (or (.getUserInfo url) "") #":" 2)]
             (URL. (.getScheme url)
               (and (seq user) user)
               (and (seq pass) pass)
               (.getDomain url)
               (translate-default (.getPort url) nil -1)
               (pathetic/normalize (.getPath url))
               (query->map (translate-default (.getQuery url) "" nil))
               (translate-default (.getFragment url) "" nil)))))

(defn url
  "Returns a new URL record for the given url string(s).

   The first argument must be a base url — either a complete url string, or
   a pre-existing URL record instance that will serve as the basis for the new
   URL.  Any additional arguments must be strings, which are interpreted as
   relative paths that are successively resolved against the base url's path
   to construct the final :path in the returned URL record. 

   This function does not perform any url-encoding.  Use `url-encode` to encode
   URL path segments as desired before passing them into this fn."
  ([url]
   (if (instance? URL url)
     url
     (url* url)))
  ([base-url & path-segments]
   (let [base-url (if (instance? URL base-url) base-url (url base-url))]
     (assoc base-url :path (pathetic/normalize (reduce pathetic/resolve
                                                 (:path base-url)
                                                 path-segments))))))
