(ns pallet.crate.java-test
  (:use pallet.crate.java)
  (:require
   [pallet.core :as core]
   [pallet.resource :as resource]
   [pallet.resource.package :as package]
   [pallet.stevedore :as stevedore]
   [pallet.script :as script]
   [pallet.target :as target]
   [pallet.template :as template]
   [pallet.utils :as utils])
  (:use clojure.test
        pallet.test-utils))

(use-fixtures :once with-ubuntu-script-template)

(defn pkg-config [request]
  (-> request
      (package/package-manager :universe)
      (package/package-manager :multiverse)
      (package/package-manager :update)))

(def noninteractive
  (script/with-template [:ubuntu]
    (stevedore/script (package-manager-non-interactive))))

(defn debconf [request pkg]
  (package/package-manager
   request
   :debconf
   (str pkg " shared/present-sun-dlj-v1-1 note")
   (str pkg " shared/accepted-sun-dlj-v1-1 boolean true")))

(deftest java-default-test
  (is (= (first
          (resource/build-resources
           []
           (pkg-config)
           (debconf "sun-java6-bin")
           (package/package "sun-java6-bin")
           (debconf "sun-java6-jdk")
           (package/package "sun-java6-jdk")))
         (first
          (resource/build-resources
           []
           (java))))))

(deftest java-sun-test
  (is (= (first
          (resource/build-resources
           []
           (pkg-config)
           (debconf "sun-java6-bin")
           (package/package "sun-java6-bin")
           (debconf "sun-java6-jdk")
           (package/package "sun-java6-jdk")))
         (first
          (resource/build-resources
           []
           (java :sun :bin :jdk))))))

(deftest java-openjdk-test
  (is (= (first
          (resource/build-resources
           []
           (package/package "openjdk-6-jre")))
         (first
          (resource/build-resources
           []
           (java :openjdk :jre)))))
  (is (= (first
          (resource/build-resources
           [:node-type {:image {:packager :pacman}}]
           (package/package "openjdk6")))
         (first
          (resource/build-resources
           [:node-type {:image {:packager :pacman}}]
           (java :openjdk :jre))))))


(deftest invoke-test
  (is
   (resource/build-resources
    []
    (java :openjdk :jdk)
    (jce-policy-file "f" :content ""))))
