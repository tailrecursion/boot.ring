# boot.ring

This project contains a number of tasks for the [boot][2] Clojure build tool
that can be used to construct [Ring][4] servers for development.

### Dependency

Artifacts are published on Clojars.

[![latest version][3]][1]

### Tasks

| Task           | Description                                                 |
|----------------|-------------------------------------------------------------|
| files          | Middleware to serve static files.                           |
| head           | Middleware to handle HEAD requests.                         |
| session-cookie | Middleware to manage client sessions via cookies.           |
| dev-mode       | Middleware to add the "X-Dev-Mode" header to responses.     |
| jetty          | Start a Jetty server with composition of middleware.        |
| dev-server     | The "kitchen sink" task to start a dev server.              |

For more info about a task do `boot [help <task>]`.

### Usage

```clojure
#!/usr/bin/env boot

;; build.boot file

#tailrecursion.boot.core/version "..."

(set-env!
  :project ...
  :version ...
  :dependencies [[tailrecursion/boot.ring "..."] ...]
  ...)

(require '[tailrecursion.boot.task.ring :as r] ...)

;; Ring tasks can be composed to create the server you desire:
(deftask my-server
  []
  (comp (r/head) (r/dev-mode) (r/files) (r/jetty)))

(defn wrap-foo [handler]
  (fn [req]
    ;; ...
    (handle req)))

;; You can create your own middleware task to insert your middleware:
(deftask my-middleware
  []
  (r/ring-task wrap-foo))
  
;; And use your task, composing it with other ring tasks:
(deftask my-other-server
  []
  (comp (head) (dev-mode) (my-middleware) (files) (jetty)))
```

## License

Copyright © 2013 Alan Dipert and Micha Niskin

Distributed under the Eclipse Public License, the same as Clojure.

[1]: https://clojars.org/tailrecursion/boot.ring
[2]: https://github.com/tailrecursion/boot
[3]: https://clojars.org/tailrecursion/boot.ring/latest-version.svg
[4]: https://github.com/ring-clojure/ring
