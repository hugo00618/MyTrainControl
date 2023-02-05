![banner](./banner.png#gh-light-mode-only "banner")
![banner](./banner_dark.png#gh-dark-mode-only "banner")

# Project TrainControl
To implement an autonomous DCC model railway control system with Arduino DCC++ command station and JMRI API.

## Basic Features

* Acceleration / Deceleration: emulates the acceleration and deceleration of the train.
* Distance Control: controls the train to move for a certain distance, based on the preset speed mapping.
* Automated Speed Profiling: automatically measures the throttle-speed mapping and generates the speed profile.  

## Block Section Operation
Enables computed [block section](https://en.wikipedia.org/wiki/Absolute_block_signalling) operation which prevents train collision.

* Layout Construction and Path Finding: decodes a JSON-formatted layout file to construct the layout in code. Path-finding algorithm integration.
* Turnout Control: controls the turnout with integration of Block Section Operation.
* Sensor Support: calibrates moving distance by reacting to IR sensor events. Improves distance control accuracy under block section operation.

## WIP Features
* Automatic Train Operation (ATO): to have trains in the system operate autonomously.
* Support for crossovers / bi-directional operation: to add support for crossovers and bi-directional operation.
