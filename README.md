[български](JFakeProg/README_BG.md)
JFakeProgrammer
===============

Connection interface and GUI for a DIY AT89Cx051 programmer device
Licensed under the [GPL v3 License](http://www.gnu.org/licenses/gpl.html)

Uses the [nrjavaserial](https://code.google.com/p/nrjavaserial/) library (LGPL v2) for serial comunication,
Application purpose is simplifying communication with the programmer device via simple protocol. May be used
as source of examples for serial communication through the nrjavaserial or [RXTX library](http://rxtx.qbang.org/).
If you happen to have
our hardware device, launching the main class (org.jfakeprog.gui.JFakeProg) will give you a fully functional
graphic user interface for controling the programmer. Supported operations:
* program supported microcontrollers
* erase their memory
* verify program
* check erased state
* Lock memory against read and verification (NOT IMPLEMENTED YET)

_(Bad news: log messages and tooltip texts are still only in Bulgarian)_

Supported microcontrollers are AT89C4051 and AT89C2051. The programmer is a device of the same type itself.
Firmware can be found on [AFakeProgrammer](https://github.com/shadjiiski/AFakeProgrammer)

System Requirements and Installation
====================================
To run this application you must have installed the JRE7 (1.7) platform. JFakeProgrammer will run on every
platform that can run JRE7 and has no problems with serial ports (or USB to Serial converters).

Installation is pretty much straightforward:
* Download [latest release](JFakeProg/out/release.zip)
* Extract to folder of your choice
* *nix users should give execute privileges to launcher.sh
* for Windows users double click the launcher.bat

Known problems
==============
All function that involve reading of mcu memory are not working under linux (tested on Ubuntu). This is due to a problem in the nrjserial library (no data available events are delivered). Will be fixed when I find some time to rewrite the code.

Screenshots
-----------
![screenshot](JFakeProg/screenshot/screenshot.png "Programmer GUI")
