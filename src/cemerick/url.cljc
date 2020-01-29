(ns cemerick.url
  (:require
    #?(:cljs [goog.Uri])
    [clojure.string :as string]
    [pathetic.core :as pathetic])
  #?(:clj (:import
            (java.net URLDecoder URLEncoder))))

(defn url-encode
  [string]
  (some-> string
          str
          #?(:clj  (URLEncoder/encode "UTF-8")
             :cljs (js/encodeURIComponent))
          (.replace "+" "%20")))

(defn url-decode
  [string]
  (some-> string
          str
          #?(:clj  (URLDecoder/decode "UTF-8")
             :cljs (js/decodeURIComponent))))

(defn- kv->url-query [[k v]]
  (let [k= (str (url-encode (name k)) \=)]
    (if (coll? v)
      (->> v
           (map url-encode)
           (interpose (str \& k=))
           (apply str k=))
      (str k= (url-encode v)))))

(defn map->query [query-map]
  (some->> (seq query-map)
           (mapcat (partial tree-seq (comp map? val) val))
           (map kv->url-query)
           (interpose "&")
           flatten
           (apply str)))

(defn split-param [param]
  (->
    (string/split param #"=")
    (concat (repeat ""))
    (->> (take 2))))

(defn- params->map [params]
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
             params->map)))

(defn- port-str [protocol port]
  (when (and (not= nil port)
             (not= -1 port)
             (not (and (== port 80) (= protocol "http")))
             (not (and (== port 443) (= protocol "https"))))
    (str ":" port)))

(defn- url-credentials [username password]
  (when username
    (str username ":" password)))

(defn- query->str [query]
  (when (seq query)
    (str \? (if (string? query)
              query
              (map->query query)))))

(defn- split-anchor [anchor]
  (when-not (string/blank? anchor)
    (string/split anchor #"\?")))

(defrecord URL [protocol username password host port path query anchor anchor-query]
  Object
  (toString [this]
    (let [creds (url-credentials username password)]
      (str protocol "://"
        creds
        (when creds \@)
        host
        (port-str protocol port)
        path
        (query->str query)
        (when (or anchor (seq anchor-query)) (str \# anchor))
        (query->str anchor-query)))))

#?(:cljs (defn translate-default [s old-default new-default]
           (if (= s old-default)
             new-default
             s)))

#?(:clj  (defn- url* [url]
           (let [url (java.net.URL. url)
                 [user pass] (string/split (or (.getUserInfo url) "") #":" 2)
                 [anchor anchor-query] (split-anchor (.getRef url))]
             (URL. (.toLowerCase (.getProtocol url))
               (and (seq user) user)
               (and (seq pass) pass)
               (.getHost url)
               (.getPort url)
               (pathetic/normalize (.getPath url))
               (query->map (.getQuery url))
               anchor
               (query->map anchor-query))))

   :cljs (defn- url* [url]
           (let [url (goog.Uri. url)
                 [user pass] (string/split (or (.getUserInfo url) "") #":" 2)
                 anchor (translate-default (.getFragment url) "" nil)
                 [anchor anchor-query] (split-anchor anchor)]
             (URL. (.getScheme url)
               (and (seq user) user)
               (and (seq pass) pass)
               (.getDomain url)
               (translate-default (.getPort url) nil -1)
               (pathetic/normalize (.getPath url))
               (query->map (translate-default (.getQuery url) "" nil))
               anchor
               (query->map anchor-query)))))

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
