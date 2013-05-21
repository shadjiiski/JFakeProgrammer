JFakeProgrammer
===============

Connection interface and GUI for a home-made AT89Cx051 programmer device
Licensed under the GPL v3 License

Uses the nrjavaserial library (LGPL v2) for serial comunication, see https://code.google.com/p/nrjavaserial/
Application purpose is simplifying communication with the programmer device via simple protocol. May be used
as source of examples for serial communication through the nrjavaserial or RXTX library. If you happen to have
our hardware device, launching the main class (org.jfakeprog.gui.JFakeProg) will give you a fully functional
graphic user interface for controling the programmer i.e. to program supported microcontrollers, erase their
memory or verify memory consistency. (Bad news: log messages and tooltip texts are still only in Bulgarian)

Supported microcontrollers are AT89C4051 and AT89C2051. The programmer is a device of the same type itself.
Assembly sources are still under construction but available on demand.

Screenshots
-----------
![screenshot](JFakeProg/screenshot/screenshot.png "Programmer GUI")
