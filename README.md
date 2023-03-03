The clojure side of this is based on the first video of Parens of the Dead (https://www.parens-of-the-dead.com/s2e1.html),
but substitutes reitit for compojure.

Other resources:
- https://shadow-cljs.github.io/docs/UsersGuide.html#_using_deps_edn_with_custom_repl_intialization
  Explains how to integrate shadow-cljs in a deps.edn build
- https://cljdoc.org/d/metosin/reitit/0.5.18/doc/ring/ring-router
  https://cljdoc.org/d/metosin/reitit/0.5.18/doc/ring/static-resources
  How to integrate reitit in a ring-based server (e.g. http-kit) and serve static resources
- https://docs.cider.mx/cider/config/project_config.html
  Explains the .dir-locals.el file
- https://github.com/vega/vega-embed
  Regarding the js-side.

** Usage
Note: I'm using Emacs and `cider-jack-in` to start the clojure process currently. When you start it from the terminal or
in some other way, you might have to add dependencies to `:extra-deps` in deps.edn, as described [here](https://docs.cider.mx/cider/basics/middleware_setup.html).

With Emacs and cider, call `cider-jack-in-clj` to start the clojure process. With the definition for `cider-clojure-cli-global-options`, the `:dev`-alias is automatically launched. Also, per clojure default, the `user`-namespace is required.
In the REPL that should open in Emacs, start the server and the shadow-cljs watch:
```clojure
(start)
(http-server-status) ;; should print the port the server is listening on
(shadow-start-and-watch :frontend) ;; nREPL server started on port ....
```
The server will connect to the port defined in config.edn or, if none is defined there, default to 3000.

Now to connect to the cljs-process, call `cider-jack-in-cljs` and select localhost, the port shadow-cljs started its server on, and the `:frontend` build id. You should now have two REPLs between which you can toggle with `cider-repl-switch-to-other`. Also, `cider-switch-to-repl-buffer` should bring you to the shadow-cljs repl from a cljs-file, to the clojure repl from a clj-file. The app is running on `localhost:<http-server-port>`.
