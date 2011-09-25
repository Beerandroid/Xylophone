Xylophone is a library implementing the command pattern for client-server communication in GWT. It combines elements of several existing libraries which serve this purpose, but the code is entirely rewritten. The library has several dependencies which may be specific to my own usage of it, so it may be necessary to use internal forks to remove these dependencies.
The two projects in particular from which this library is drawn are [gwt-dispatch](http://code.google.com/p/gwt-dispatch/) and [gwt-remote-action](http://code.google.com/p/gwt-remote-action/).
This project is licensed under the Apache License, version 2.0.

Features
========
* Event dispatching
* Client-side filters (target: 0.2-beta)
* Events and continuation support (target: 0.3-beta)
