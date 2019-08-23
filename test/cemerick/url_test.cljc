(ns cemerick.url-test
  (:require
    [cemerick.url :as url]
    [clojure.test :refer [deftest testing is]])
  #?(:clj
     (:import java.net.URL)))

(def url-str (comp str url/url))

(deftest map->query-test

  (testing "map->query"
    (is (= "a=1&b=2&c=3"
           (url/map->query {:a 1
                            :b 2
                            :c 3})))
    (is (= "a=1&b=2&c=3"
           (url/map->query {:a "1"
                            :b "2"
                            :c "3"})))
    (is (= "a=1&b=2"
           (url/map->query {"a" "1"
                            "b" "2"})))
    (is (= "a="
           (url/map->query {"a" ""}))))

  (testing "Swagger Array examples"
    (is (= "id=3&id=4&id=5"
           (url/map->query {:id [3 4 5]})))
    (is (= "id=3%2C4%2C5"
           (url/map->query {:id "3,4,5"})))
    (is (= "id=3%204%205"
           (url/map->query {:id "3 4 5"})))
    (is (= "id=3%7C4%7C5"
           (url/map->query {:id "3|4|5"})))))

(deftest query->map-test

  (testing "query->map"
    (is (= {"a" "b"} (url/query->map "a=b")))
    (is (= {"a" "1" "b" "2" "c" "3"} (url/query->map "a=1&b=2&c=3")))
    (is (= {"a" ""} (url/query->map "a=")))
    (is (= {"a" ""} (url/query->map "a")))
    (is (nil? (url/query->map nil)))
    (is (nil? (url/query->map ""))))

  (testing "Swagger Array examples"
    (is (= {"id" ["3" "4" "5"]} (url/query->map "id=3&id=4&id=5")))
    (is (= {"id" "3,4,5"} (url/query->map "id=3,4,5")))
    (is (= {"id" "3 4 5"} (url/query->map "id=3%204%205")))
    (is (= {"id" "3|4|5"} (url/query->map "id=3|4|5")))))

(deftest url-round-trip

  (testing "url round trip"
    (let [aurl (url/url "https://username:password@some.host.com/database?query=string")]
      (is (= "https://username:password@some.host.com/database?query=string" (str aurl)))
      (is (== -1 (:port aurl)))
      (is (= "username" (:username aurl)))
      (is (= "password" (:password aurl)))
      (is (= "https://username:password@some.host.com" (str (assoc aurl :path nil :query nil)))))))

(deftest url-segments

  (testing "url segments"
    (is (= "http://localhost:5984/a/b" (url-str "http://localhost:5984" "a" "b")))
    (is (= "http://localhost:5984/a/b/c" (url-str "http://localhost:5984" "a" "b" "c")))
    (is (= "http://localhost:5984/a/b/c" (url-str (url/url "http://localhost:5984" "a") "b" "c")))))

(deftest port-normalization

  (testing "Port normalization"
    #?(:clj (is (== -1 (-> "https://foo" url-str URL. .getPort))))
    (is (= "http://localhost" (url-str "http://localhost")))
    (is (= "http://localhost" (url-str "http://localhost:80")))
    (is (= "http://localhost:8080" (url-str "http://localhost:8080")))
    (is (= "https://localhost" (url-str "https://localhost")))
    (is (= "https://localhost" (url-str "https://localhost:443")))
    (is (= "https://localhost:8443" (url-str "https://localhost:8443")))
    (is (= "http://localhost" (str (url/map->URL {:host "localhost" :protocol "http"}))))))

(deftest user-info-edgecases
  (let [user-info (fn [url-str] (->> url-str url/url ((juxt :username :password))))]

    (testing "user-info-edgecases"
      (is (= ["a" nil] (user-info "http://a@foo")))
      (is (= ["a" nil] (user-info "http://a:@foo")))
      (is (= ["a" "b:c"] (user-info "http://a:b:c@foo"))))))

(deftest path-normalization

  (testing "Path normalization"
    (is (= "http://a/" (url-str "http://a/b/c/../..")))

    (is (= "http://a/b/c" (url-str "http://a/b/" "c")))
    (is (= "http://a/b/c" (url-str "http://a/b/.." "b" "c")))
    (is (= "http://a/b/c" (str (url/url "http://a/b/..////./" "b" "c" "../././.." "b" "c"))))
    (is (= "http://a/" (str (url/url "http://a/b/..////./" "b" "c" "../././.." "b" "c" "/"))))

    (is (= "http://a/x" (str (url/url "http://a/b/c" "/x"))))
    (is (= "http://a/" (str (url/url "http://a/b/c" "/"))))
    (is (= "http://a/" (str (url/url "http://a/b/c" "../.."))))
    (is (= "http://a/x" (str (url/url "http://a/b/c" "../.." "." "./x"))))))

(deftest anchors

  (testing "anchors"
    (is (= "http://a#x" (url-str "http://a#x")))
    (is (= "http://a?b=c#x" (url-str "http://a?b=c#x")))
    (is (= "http://a?b=c#x" (-> "http://a#x" url/url (assoc :query {:b "c"}) str)))))

(deftest anchor-query

  (testing "anchor query"
    (is (= "http://a.com/#b?c=d" (url-str "http://a.com/#b?c=d")))
    (is (= "http://a.com/?e=f#b?c=d" (url-str "http://a.com/?e=f#b?c=d")))
    (is (= "http://a.com/?e=f#b?c=d" (-> "http://a.com/#b"
                                         url/url
                                         (assoc
                                           :query {:e "f"}
                                           :anchor-query {:c "d"})
                                         str)))))

(deftest no-bare-?

  (testing "no trailing query"
    (is (= "http://a" (-> "http://a?b=c" url/url (update-in [:query] dissoc "b") str)))))
