# Clojure application template and common libs

## Overview

TBD:

    :lint-as {
      app.database.hugsql/declare-fn clojure.core/declare
      rum.core/defc  clj-kondo.lint-as/def-catch-all
      rum.core/defcs clj-kondo.lint-as/def-catch-all
      rum.core/defcc clj-kondo.lint-as/def-catch-all
    }

    [org.clojure/core.match "1.0.0"]

[![Clojars Project](https://img.shields.io/clojars/v/maxp/mlib.svg)](https://clojars.org/maxp/mlib)

## Changelog

- **4.2**  
  http server, middleware, util

- **4.1**  
  next-jdbc

- **4.0.11**  
  bundled for Clojars in new repo

## Notes

- Run `make javac` once before making other tasks.

- Replace `example_app` with your real package name.

- jBCrypt sources included in package  
  Copyright (c) 2006 Damien Miller <djm@mindrot.org>

---

(C) 2015-2020 [Maxim Penzin](https://maxp.dev)
